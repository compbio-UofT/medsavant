package medsavant.pgx;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import jannovar.common.VariantType;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import medsavant.pgx.localDB.PGXDB;
import medsavant.pgx.localDB.PGXDBFunctions;
import medsavant.pgx.localDB.PGXDBFunctions.PGXMarker;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.appdevapi.DBAnnotationColumns;
import org.ut.biolab.medsavant.shared.appdevapi.Variant;
import org.ut.biolab.medsavant.shared.appdevapi.VariantIterator;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.AnnotationManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;

/**
 * Performs a pharmacogenomic analysis for this individual.
 * 
 * @author rammar
 */
public class PGXAnalysis {
	
	private static final String DBSNP_COLUMN= DBAnnotationColumns.DBSNP_TEXT;
	private static final int DB_VARIANT_REQUEST_LIMIT= 500;
	public static final double AF_THRESHOLD= 0.05;
	private static final List<String> NOVEL_MUTATIONS= Arrays.asList(
		new String[] {
		VariantType.MISSENSE.toString(), VariantType.FS_DELETION.toString(),
		VariantType.FS_INSERTION.toString(), VariantType.FS_SUBSTITUTION.toString(),
		VariantType.FS_DUPLICATION.toString(), VariantType.NON_FS_DELETION .toString(),
		VariantType.NON_FS_INSERTION.toString(), VariantType.NON_FS_SUBSTITUTION.toString(),
		VariantType.NON_FS_DUPLICATION.toString(), VariantType.SPLICING.toString(),
		VariantType.STOPGAIN.toString(), VariantType.START_LOSS.toString()
		});
	
	private static Connection pgxdbConn;
	private static TableSchema ts= ProjectController.getInstance().getCurrentVariantTableSchema();
	private static Map<String, Condition> standardPGXConditions;
	private static Map<String, String> columns= getDbToHumanReadableMap();
	private static List<PGXMarker> listOfAllMarkers;
	private static List<Condition> novelPGXConditions;
	
	private String dnaID;
	private List<PGXGene> pgxGenes= new LinkedList<PGXGene>();
	private VariantManagerAdapter vma= MedSavantClient.VariantManager;
	private boolean assumeRef; // if true, assume reference calls for missing PGx markers
	
	/**
	 * Perform a pharmacogenomic analysis.
	 * @param dnaID the DNA ID for this individual
	 * @param assumeRef if true and a marker is missing, assumes reference nucleotide. If false, marker is left blank.
	 */
	public PGXAnalysis(String dnaID, boolean assumeRef) throws SQLException, RemoteException, SessionExpiredException, PGXException {
		this.dnaID= dnaID;
		this.assumeRef= assumeRef;
		
		/* If no connection exists, initialize the local HyperSQL database for 
		 * analyses. */
		if (pgxdbConn == null) {			
			PGXDB.initialize();
			pgxdbConn= PGXDB.getConnection();
		}
		
		/* Once the PGx DB is initialized, initialize the static standard PGx
		 * ComboCondition list if it's still empty. */
		if (standardPGXConditions == null) {
			standardPGXConditions= buildConditionList();
		}
		
		/* Once the PGx DB is initialized, initialize the static standard PGx
		 * ComboCondition list for NOVEL variants, if it's still empty. */
		if (novelPGXConditions == null) {
			novelPGXConditions= buildNovelConditionList();
		}
		
		/* Generate a static list of all PGX markers. */
		if (listOfAllMarkers == null) {
			listOfAllMarkers= getListOfAllMarkers();
		}
		
		/* Query the DB for this individual's pharmacogenomic genotypes. */
		queryVariants();
		
		/* Assign the diplotypes for this individual's genes. */
		getDiplotypes();
		
		/* Assign the activity scores/phenotypes and metabolizer class. */
		getActivities();
		
		/* Look for novel rare pharmacogenomic variants for this individual. */
		getNovelVariants();
	}
	
	
	/**
	 * Get the pharmacogenomic variants.
	 * @return a List of PGXGeneAndVariants objects
	 */
	public List<PGXGene> getGenes() {
		return pgxGenes;
	}
	
	
	/**
	 * Build the standard pharmacogenomic condition to be used when retrieving 
	 * variants for any patient's analysis.
	 * @return the ComboCondition to be used for all analyses
	 * @deprecated This method creates a mega condition that retrieves all PGx
	 *		variants from the DB. However, as the PGx DB grows, this condition
	 *		will become unreasonably large, and is limited by the
	 *		'max_allowed_packet' property in MySQL. Replaced by {@link #buildConditionList()}
	 */
	@Deprecated
	private static ComboCondition buildCondition() {
		ComboCondition query= new ComboCondition(ComboCondition.Op.OR);
		
		/* Get all relevant genes. */
		List<String> genes= new LinkedList<String>();
		try {
			genes= PGXDBFunctions.getGenes();
		} catch (SQLException se) {
			se.printStackTrace();
		}
		
		/* Get all relevant markers. */
		List<String> markers= new LinkedList<String>();
		try {
			for (String g : genes) {
				markers.addAll(PGXDBFunctions.getMarkers(g));
			}
 		} catch (Exception pe) {
			pe.printStackTrace();
		}
		
		
		/* Add all markers to the ComboCondition.
		 * NOTE: this is hardcoded for now, but will need to be changed if the
		 * dbSNP annotation DB is updated. */
		for (String m : markers) {
			query.addCondition(
				BinaryCondition.equalTo(ts.getDBColumn(columns.get(DBSNP_COLUMN)), m));
		}
		
		return query;		
	}
	
	
	/**
	 * Build the standard pharmacogenomic conditions to be used when retrieving 
	 * variants for any patient's analysis and store these in a list.
	 * @return a Map of Conditions to be used for all PGx analyses
	 * @throws SQLException
	 */
	private static Map<String, Condition> buildConditionList() throws SQLException {
		Map<String, Condition> queryMap= new HashMap<String, Condition>();
		
		/* Get all relevant markers for a particular gene and create a
		 * ComboCondition for that set. Then add it to the List. */
			for (String g : PGXDBFunctions.getGenes()) {
				// generate a new query for this gene
				ComboCondition query= new ComboCondition(ComboCondition.Op.OR);
				
				try {
					// add all the markers for this gene
					for (String m : PGXDBFunctions.getMarkers(g)) {
						query.addCondition(
							BinaryCondition.equalTo(ts.getDBColumn(columns.get(DBSNP_COLUMN)), m));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// add this gene-query pair to the list
				queryMap.put(g, query);
			}
		
		return queryMap;		
	}
	
	
	/** 
	 * Build the standard pharmacogenomic conditions for NOVEL variants.
	 * @return a List of Conditions to be used for all PGx analyses of novel variants
	 * @throws SQLException
	 */
	private static List<Condition> buildNovelConditionList() throws SQLException, RemoteException, SessionExpiredException {
			List<Condition> output= new LinkedList<Condition>();
		
			/* Get all genic non-synonymous variants. */
			ComboCondition mutationCondition= new ComboCondition(ComboCondition.Op.OR);
			for (String mutationEffect : NOVEL_MUTATIONS) {
				mutationCondition.addCondition(
					BinaryCondition.iLike(ts.getDBColumn(BasicVariantColumns.JANNOVAR_EFFECT), mutationEffect + "%"));
			}
			output.add(mutationCondition);	
			
			/* For each of the allele frequency columns, check if the allele 
			 * frequency is below threshold. If allele frequency at the position
			 * is null, also report it. */
			ComboCondition afCondition= new ComboCondition(ComboCondition.Op.OR);
			AnnotationManagerAdapter am= MedSavantClient.AnnotationManagerAdapter;
			Map<String, Set<CustomField>> fieldMap= 
				am.getAnnotationFieldsByTag(LoginController.getInstance().getSessionID(), true);
			Set<CustomField> columnNames= fieldMap.get(CustomField.ALLELE_FREQUENCY_TAG);
			for (CustomField cf : columnNames) {
				DbColumn afColumn= ts.getDBColumn(cf.getColumnName());
				// include variant if AF is below threshold
				afCondition.addCondition(
					BinaryCondition.lessThan(afColumn, AF_THRESHOLD, true));
				// include variant even if AF information is missing
				afCondition.addCondition(UnaryCondition.isNull(afColumn));
			}
			output.add(afCondition);
			
			return output;
	}
	
	
	/**
	 * Run query on remote server and return a list of Variants.
	 * @param query the query to run
	 * @return a list of Variants.
	 */
	private List<Variant> runRemoteQuery(Condition query) throws SQLException, RemoteException, SessionExpiredException{
		List<Variant> output= new LinkedList<Variant>();
		
		/* For each query, a VariantIterator will be returned. When the Iterator
		* is null, stop getting more VariantIterators. Iterate while
		* this object hasNext() and store the Variant objects in a List of
		* Variant objects. Variants are retrieved in chunks based on a request
		* limit offset to allow for a cancellation. */
		Condition[][] conditionMatrix= new Condition[1][1];
		conditionMatrix[0][0]= query;

		int position= 0;
		// initiate VariantIterator for first batch
		List<Object[]> rows= vma.getVariants(LoginController.getInstance().getSessionID(),
			ProjectController.getInstance().getCurrentProjectID(),
			ReferenceController.getInstance().getCurrentReferenceID(),
			conditionMatrix, position, DB_VARIANT_REQUEST_LIMIT);		
		VariantIterator variantIterator= new VariantIterator(rows, ProjectController.getInstance().getCurrentAnnotationFormats());
		while (variantIterator.hasNext()) {
			// add all the variants to the list from the current batch
			while (variantIterator != null && variantIterator.hasNext()) {
				output.add(variantIterator.next());
			}

			// increment the request limit
			position += DB_VARIANT_REQUEST_LIMIT;

			// Get the next batch 
			rows= vma.getVariants(LoginController.getInstance().getSessionID(),
				ProjectController.getInstance().getCurrentProjectID(),
				ReferenceController.getInstance().getCurrentReferenceID(),
				conditionMatrix, position, DB_VARIANT_REQUEST_LIMIT);		
			variantIterator= new VariantIterator(rows, ProjectController.getInstance().getCurrentAnnotationFormats());
		}
		
		return output;
	}
	
	
	/**
	 * Get all pharmacogenomic variants for this individual.
	 */
	private void queryVariants() throws SQLException, RemoteException, SessionExpiredException {
		/* Iterate through all gene conditions. */
		for (String geneKey : standardPGXConditions.keySet()) {
			/* The variants for this gene. */
			PGXGene pgxVariants= new PGXGene(geneKey);
			
			/* Take the standard combocondition and AND it to the DNA ID for this
			 * individual before submitting for variants. */
			ComboCondition query= new ComboCondition(ComboCondition.Op.AND);
			query.addCondition(
				BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.DNA_ID), dnaID));
			query.addCondition(standardPGXConditions.get(geneKey));

			/* Once query is built, run it on the remote server. */
			List<Variant> retrievedVariants= runRemoteQuery(query);
			
			/* Add variants to the list for this PGx gene. */
			for (Variant var : retrievedVariants) {
				pgxVariants.addVariant(var);
			}
			
			/* Add the current gene-variant object to the list. */
			pgxGenes.add(pgxVariants);
		}
	}
	
	
	/**
	 * Get all novel and rare pharmacogenomic variants for this individual.
	 * @precondition	pgxGenes must already have been initialized by running queryVariants()
	 */
	private void getNovelVariants() throws SQLException, RemoteException, SessionExpiredException, PGXException {
		/* Iterate through all previously stored PGXGene objects and get all 
		 * low allele frequency variants for these genes. */
		for (PGXGene pg : pgxGenes) {
			String geneSymbol= pg.getGene();
			List<PGXMarker> existingMarkers= PGXDBFunctions.getMarkerInfo(geneSymbol);
		
			ComboCondition query= new ComboCondition(ComboCondition.Op.AND);
			
			/* Get variants for this patient/DNA ID and this PGx gene. */
			query.addCondition(
				BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.DNA_ID), dnaID));
			query.addCondition(
				BinaryCondition.iLike(ts.getDBColumn(BasicVariantColumns.JANNOVAR_SYMBOL), geneSymbol + "%"));
			
			/* Add all default novel Conditions to this query. */
			for (Condition c : novelPGXConditions) {
				query.addCondition(c);
			}

			// TESTING
			System.err.println("TEST query: " + query.toString()); ////////////////
			
			/* Once query is built, run it on the remote server. */
			List<Variant> potentialNovelVariants= runRemoteQuery(query);
			
			/* Check if returned variants are NOT PGx markers and then add to
			 * the novel variants. */
			for (Variant var : potentialNovelVariants) {
				if (!isKnownPGXMarker(var.getChromosome(), Long.toString(var.getStart()))) {	
					pg.addNovelVariant(var);
				}
			}
		}
	}
	
	
	/**
	 * Get diplotypes for all the PGx genes.
	 */
	private void getDiplotypes() {
		for (PGXGene pg : pgxGenes) {
			try {
				pg.setDiplotype(PGXDBFunctions.getDiplotype(pg, this.assumeRef));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Get the haplotype activity scores/phenotypes and metabolizer classes for all PGx genes.
	 */
	private void getActivities() {
		for (PGXGene pg : pgxGenes) {
			/* Set the haplotype activities. */
			pg.setMaternalActivity(PGXDBFunctions.getActivities(pg.getGene(), pg.getMaternalHaplotype()));
			pg.setPaternalActivity(PGXDBFunctions.getActivities(pg.getGene(), pg.getPaternalHaplotype()));
			
			/* Set the metabolizer class based on the haplotype activities. */
			pg.setMetabolizerClass(PGXDBFunctions.getMetabolizerClass(pg.getMaternalActivity(), pg.getPaternalActivity()));
		}
	}
	
	
	/** 
	 * Get the header for the table using the column aliases.
	 * @return a map of the column aliases to column names.
	 */
	public static Map<String, String> getDbToHumanReadableMap() {
		Map<String, String> dbAliasToNameMap = new HashMap<String, String>();

		try {
			AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
			for (AnnotationFormat af : afs) {
				for (CustomField field : af.getCustomFields()) {
					dbAliasToNameMap.put(field.getAlias(), field.getColumnName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dbAliasToNameMap;
	}
	
	
	/**
	 * Get a list of all PGX markers.
	 */
	private static List<PGXMarker> getListOfAllMarkers() {
		List<PGXMarker> output= new LinkedList<PGXMarker>();
		try {
			for (String gene : PGXDBFunctions.getGenes()) {
				output.addAll(PGXDBFunctions.getMarkerInfo(gene));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	
	/**
	 * Returns true if PGx marker at this position exists, false otherwise.
	 * @param chromosome the chromosome
	 * @param position the position stored as a string
	 * @return true if PGx marker at this position exists, false otherwise.
	 */
	private boolean isKnownPGXMarker(String chromosome, String position)
		throws PGXException, SQLException {
		
		boolean pgxMarkerExists= false;
		
		int i= 0;
		while (i != listOfAllMarkers.size() && !pgxMarkerExists) {
			PGXMarker currentMarker= listOfAllMarkers.get(i);
			if (currentMarker.chromosome.equals(chromosome) && currentMarker.position.equals(position)) {
				pgxMarkerExists= true;
			}
			++i;
		}
		
		return pgxMarkerExists;
	}
}
