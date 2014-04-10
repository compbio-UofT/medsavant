package medsavant.pgx;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import medsavant.pgx.localDB.PGXDB;
import medsavant.pgx.localDB.PGXDBFunctions;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.shared.appdevapi.DBAnnotationColumns;
import org.ut.biolab.medsavant.shared.appdevapi.VariantIterator;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;

/**
 * Performs a pharmacogenomic analysis for this individual.
 * 
 * @author rammar
 */
public class PGXAnalysis {
	
	private static final String DBSNP_COLUMN= DBAnnotationColumns.DBSNP_TEXT;
	private static final int DB_VARIANT_REQUEST_LIMIT= 500;
	
	private static Connection pgxdbConn;
	private static TableSchema ts= ProjectController.getInstance().getCurrentVariantTableSchema();
	private static Map<String, Condition> standardPGXConditions;
	private static Map<String, String> columns= getDbToHumanReadableMap();
	
	private String dnaID;
	private List<PGXGeneAndVariants> pgxVariants= new LinkedList<PGXGeneAndVariants>();
	private VariantManagerAdapter vma= MedSavantClient.VariantManager;
	
	
	/**
	 * Initiate a pharmacogenomic analysis.
	 * @param dnaID the DNA ID for this individual
	 */
	public PGXAnalysis(String dnaID) throws SQLException, RemoteException, SessionExpiredException {
		this.dnaID= dnaID;
		
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
		
		/* Query the DB for this individual's pharmacogenomic genotypes. */
		queryVariants();
	}
	
	
	/**
	 * Get the pharmacogenomic variants.
	 * @return a List of PGXGeneAndVariants objects
	 */
	public List<PGXGeneAndVariants> getVariants() {
		return pgxVariants;
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
	 * Get all pharmacogenomic variants for this individual.
	 */
	private void queryVariants() throws SQLException, RemoteException, SessionExpiredException {
		/* Iterate through all gene conditions. */
		for (String geneKey : standardPGXConditions.keySet()) {
			/* The variants for this gene. */
			PGXGeneAndVariants currentVariants= new PGXGeneAndVariants(geneKey);
			
			/* Take the standard combocondition and AND it to the DNA ID for this
			 * individual before submitting for variants. */
			ComboCondition query= new ComboCondition(ComboCondition.Op.AND);
			query.addCondition(
				BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.DNA_ID), dnaID));
			query.addCondition(standardPGXConditions.get(geneKey));

			// TESTING
			//System.out.println("[TESTING]: full query= \n" + query.toString(10000, new SqlContext())); //////////////

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
					currentVariants.addVariant(variantIterator.next());
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
			
			/* Add the current gene-variant pair to the list. */
			pgxVariants.add(currentVariants);
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
	
}
