package medsavant.incidental;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import medsavant.incidental.localDB.IncidentalDB;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.DataRetriever;
import org.ut.biolab.medsavant.client.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.serverapi.PatientManagerAdapter;

/**
 * Compute and store all incidental findings for a patient.
 * 
 * @author rammar
 */
public class IncidentalFindings {
    private static final Log LOG = LogFactory.getLog(MedSavantClient.class);
	
	private final int DB_VARIANT_REQUEST_LIMIT= 5000;
	private final String JANNOVAR_EFFECT= "jannovar effect";
	private final String JANNOVAR_GENE= "jannovar gene symbol";
	private final String STOPGAIN= "STOPGAIN";
	private final String FRAMESHIFTS= "FS_%";
	private final String SPLICING= "SPLICING";
	private final String CLINVAR_COLUMN= "clinvar_20131105b, info";
	private final String HGMD_COLUMN= "hgmd_pro_allmut, gene";
	
	private String GENDER= null;
	
	private int INHERITANCE_INDEX= -1;
	private int CLASSIFICATION_INDEX= -1;
	private Map<String, String> dbAliasToColumn;
	private String dnaID;
	private int coverageThreshold;
	private double hetRatio;
	private double alleleFrequencyThreshold;
	private Map<String, String> zygosityMap;
	private String incidentalPanel;
	
	private List<Object[]> allVariants;
	private TableSchema ts;
	public List<String> header;
	private int effectIndex;
	private int geneSymbolIndex;
	private int af1000gIndex;
	
	private Pattern dp4Pattern= Pattern.compile(";?DP4=([^;]+);?", Pattern.CASE_INSENSITIVE);
	private Pattern truncationPattern= Pattern.compile("STOPGAIN|FS_\\w+|SPLICING", Pattern.CASE_INSENSITIVE);
	private Pattern geneSymbolPattern= Pattern.compile("^([^(]+)");
	private Pattern formatFieldPattern= Pattern.compile(";?FORMAT=([^;]*AD[^;]*);?", Pattern.CASE_INSENSITIVE); // must contain "AD" in format
	private Pattern sampleInfoFieldPattern= Pattern.compile(";?SAMPLE_INFO=([^;]+);?", Pattern.CASE_INSENSITIVE);
	
	/** Find all mutations in disease genes and filter for relevance.
	 * @param dnaID	Patient's DNA ID
	 * @param cov	The minimum coverage for alt variants
	 * @param ratio	The minimum ratio of alt/total reads to be considered as possibly het
	 * @param afThreshold	Allele frequency threshold
	 * @param afColumns	The columns of the table corresponding to DBs to be used for the allele frequency cutoff
	 * @param panel	Which incidental panel to use. For example, "CGD" or "ACMG".
	 */
	public IncidentalFindings(String dnaID, int cov, double ratio, double afThreshold, List<Object> afColumns, String panel) {
		this.dnaID= dnaID;
		coverageThreshold= cov;
		hetRatio= ratio;
		alleleFrequencyThreshold= afThreshold;
		incidentalPanel= panel;
			
		allVariants= new ArrayList<Object[]>(DB_VARIANT_REQUEST_LIMIT); // initial capacity DB_VARIANT_REQUEST_LIMIT
		
		ts= ProjectController.getInstance().getCurrentVariantTableSchema();
		dbAliasToColumn= getDbToHumanReadableMap(); // Get column aliases from column names
		header= getTableHeader();
		effectIndex= header.indexOf(JANNOVAR_EFFECT);
		geneSymbolIndex= header.indexOf(JANNOVAR_GENE);
		
		// For variant DB lookup - zygosity values based on VariantRecord in org.ut.biolab.medsavant.shared.vcf
		zygosityMap= new HashMap<String, String>();
		zygosityMap.put("HomoAlt", "hom");
		zygosityMap.put("Hetero", "het");
		
		try {
			// Get gender info
			PatientManagerAdapter pma= MedSavantClient.PatientManager;
			List<Object[]> allProjectPatients= pma.getBasicPatientInfo(
					LoginController.getInstance().getSessionID(),
					ProjectController.getInstance().getCurrentProjectID(), Integer.MAX_VALUE);
			
			for (Object[] row : allProjectPatients) {
				if (dnaID.equals((String) row[BasicPatientColumns.INDEX_OF_DNA_IDS])) {
					GENDER= (String) ClientMiscUtils.genderToString(
						(Integer) row[BasicPatientColumns.INDEX_OF_GENDER]);
					break; // no need to search further
				}
			}
			
			/* Add any relevant columns based on the data that has been appended. */
			header.add("Inheritance");
			INHERITANCE_INDEX= header.size() - 1;
			header.add("Classification");
			CLASSIFICATION_INDEX= header.size() - 1;
			
			// Download and process variants 
			storeVariants(afColumns);
			
		} catch (Exception ex) {
			System.err.println("IncidentalFindings error: " + ex.toString());
			ex.printStackTrace();
		}
	}
	
		
	/** 
	 * Searchable table output for development testing. 
	 * @param selectedViewColumns Columns preselected for SearchableTablePanel output
	 */
	public SearchableTablePanel getTableOutput(int[] selectedViewColumns) {
		DataRetriever<Object[]> dr= new DataRetriever<Object[]>() {
			@Override
			public List<Object[]> retrieve(int start, int limit) throws Exception {            
				return allVariants;
			}

			@Override
			public int getTotalNum() {
				return allVariants.size();
			}

			@Override
			public void retrievalComplete() {
			}
		};
		
		Class[] STRING_ONLY_COLUMN_CLASSES= new Class[header.size()];
		for (int i= 0; i != STRING_ONLY_COLUMN_CLASSES.length; ++i)
			STRING_ONLY_COLUMN_CLASSES[i]= String.class; // FOR NOW ONLY CALLING THESE STRINGS
		
		SearchableTablePanel t;
		
		// if the selected columns use incorrect/outdated indices, default to all columns
		try {
			
			if (selectedViewColumns == null) {
				t= new SearchableTablePanel("Results", header.toArray(new String[header.size()]), 
					STRING_ONLY_COLUMN_CLASSES, new int[0], true, true, Integer.MAX_VALUE,
					false, SearchableTablePanel.TableSelectionType.ROW, Integer.MAX_VALUE, dr);
			} else {
				t= new SearchableTablePanel("Results", header.toArray(new String[header.size()]), 
					STRING_ONLY_COLUMN_CLASSES, getHiddenColumns(selectedViewColumns, header.size()), 
					true, true, Integer.MAX_VALUE, false, 
					SearchableTablePanel.TableSelectionType.ROW, Integer.MAX_VALUE, dr);
			}
		} catch (Exception e) {
			t= new SearchableTablePanel("Results", header.toArray(new String[header.size()]), 
				STRING_ONLY_COLUMN_CLASSES, new int[0], true, true, Integer.MAX_VALUE,
				false, SearchableTablePanel.TableSelectionType.ROW, Integer.MAX_VALUE, dr);
		}
		
		t.setResizeOff();
		t.setExportButtonVisible(true);
		t.setExportButtonEnabled(true);
		t.setHelpButtonVisible(false);
		//t.setChooseColumnsButtonVisible(false);
		t.forceRefreshData(); // without this, the table is empty with just a header
		
		return t;
	}
	
	
	/** Get the number of variants returned for this Incidental Findings instance. */
	public int getVariantCount() {
		return allVariants.size();
	}
	
	
	/** 
	 * Filter and store all variants for this individual.
	 * @param afColumns	A string list of the column aliases corresponding to the allele frequency DBs 
	 */
	private void storeVariants(List<Object> afColumns) throws SQLException, RemoteException, SessionExpiredException {
		VariantManagerAdapter vma= MedSavantClient.VariantManager;
		
		/* Put the conditions together with ANDs and ORs. */		
		ComboCondition cc= new ComboCondition(ComboCondition.Op.AND);
		
		Condition dnaCondition= BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.DNA_ID), dnaID);
		cc.addCondition(dnaCondition);
		
		/* Iterate through the selected allele freq DBs and add to the condition. */
		for (Object columnAlias : afColumns) {
			String columnName= dbAliasToColumn.get((String) columnAlias);
			
			ComboCondition currentAFComboCondition= new ComboCondition(ComboCondition.Op.OR);
			currentAFComboCondition.addCondition(
				BinaryCondition.lessThan(ts.getDBColumn(columnName),
					Double.toString(alleleFrequencyThreshold), true));
			currentAFComboCondition.addCondition(
				UnaryCondition.isNull(ts.getDBColumn(columnName)));
			
			cc.addCondition(currentAFComboCondition);
		}
		
		/* Report only variants that have truncation mutations or that are present in
		 * a disease variant database. So far, Clinvar and HGMD. */
		ComboCondition truncationOrDBComboCondition= new ComboCondition(ComboCondition.Op.OR);
		
		// Truncation mutations, if they exist
		if (dbAliasToColumn.get(JANNOVAR_EFFECT) != null) {
			ComboCondition truncationComboCondition= new ComboCondition(ComboCondition.Op.OR);
			truncationComboCondition.addCondition(
				BinaryCondition.like(ts.getDBColumn(dbAliasToColumn.get(JANNOVAR_EFFECT)), STOPGAIN));
			truncationComboCondition.addCondition(
				BinaryCondition.like(ts.getDBColumn(dbAliasToColumn.get(JANNOVAR_EFFECT)), SPLICING));
			truncationComboCondition.addCondition(
				BinaryCondition.like(ts.getDBColumn(dbAliasToColumn.get(JANNOVAR_EFFECT)), FRAMESHIFTS));
			
			truncationOrDBComboCondition.addCondition(truncationComboCondition);
		}

		// Clinvar DB annotations, if they exist
		if (dbAliasToColumn.get(CLINVAR_COLUMN) != null) {
			Condition clinvarCondition= UnaryCondition.isNotNull(
				ts.getDBColumn(dbAliasToColumn.get(CLINVAR_COLUMN)));
			
			truncationOrDBComboCondition.addCondition(clinvarCondition);
		}
		
		// HGMD DB annotations, if they exist
		if (dbAliasToColumn.get(HGMD_COLUMN) != null) {
			Condition hgmdCondition= UnaryCondition.isNotNull(
				ts.getDBColumn(dbAliasToColumn.get(HGMD_COLUMN)));
			
			truncationOrDBComboCondition.addCondition(hgmdCondition);
		}
		
		cc.addCondition(truncationOrDBComboCondition);
		
		Condition[][] conditionMatrix= new Condition[1][1];
		conditionMatrix[0][0]= cc;
		
		/* Get variants in chunks based on a request limit offset to save memory. */
		int position= 0;
		List<Object[]> currentVariants= null;
		while (currentVariants == null || currentVariants.size() != 0) {
			currentVariants= vma.getVariants(LoginController.getInstance().getSessionID(),
				ProjectController.getInstance().getCurrentProjectID(),
				ReferenceController.getInstance().getCurrentReferenceID(),
				conditionMatrix,
				position, DB_VARIANT_REQUEST_LIMIT);
			allVariants.addAll(filterVariants(currentVariants));
			position += DB_VARIANT_REQUEST_LIMIT;
		}
		
		
		/* Identify potential compound hets. */
		identifyPotentialCompoundHet();
	}
	
	
	/** Get gene symbol. 
	 * 
	 * @return	String of gene symbol, null if pattern does not match hgvs text from DB.
	 */
	private String getGeneSymbol(Object[] row) {
		String geneSymbol= null;
		String hgvsText= (String) row[geneSymbolIndex];
			
		Matcher geneSymbolMatcher= geneSymbolPattern.matcher(hgvsText);
		if (geneSymbolMatcher.find()) {
			geneSymbol= geneSymbolMatcher.group(1);
		}
		
		return geneSymbol;
	}
	
	
	/** Filter a list of variants and return only those variants that pass all
	 * filters.
	 */
	private List<Object[]> filterVariants(List<Object[]> input) {
		List<Object[]> filtered= new LinkedList<Object[]>();
		
		for (Object[] row : input) {
			List<String> query= getClassification(getGeneSymbol(row), 
					(String) row[BasicVariantColumns.INDEX_OF_ZYGOSITY], incidentalPanel);
			
			String classification= query.get(0);
			String inheritance= query.get(1);

			if (inheritance != null && !inheritance.equals("") && 
				coverageAndRatioPass(row, true)) {
						
				List<Object> listRow= new ArrayList<Object>(Arrays.asList(row));
				listRow.add(inheritance);
				listRow.add(classification);
				
				// Add to output list
				filtered.add(listRow.toArray());
			}
		}
		
		return filtered;		
	}
	
	
	/** Checks if this variant encodes a truncation mutation. 
	 * Expects Jannovar-style annotations in the EFFECT column. Based on Jannovar
	 * documentation (JannovarTutorial.pdf) the possibilities are:
	 * 
	 * NONSYNONYMOUS
	 * STOPGAIN
	 * STOPLOSS
	 * NON_FS_INSERTION
	 * FS_INSERTION
	 * NON_FS_SUBSTITUTION
	 * NON_FS_DELETION
	 * FS_SUBSTITUTION
	 * ncRNA_EXONIC
	 * ncRNA_SPLICING
	 * UTR3
	 * UTR5
	 * SYNONYMOUS
	 * INTRONIC
	 * ncRNA_INTRONIC
	 * UPSTREAM
	 * DOWNSTREAM
	 * INTERGENIC
	 * ERROR
	 * 
	 * 2 other critically important annotations not mentioned in the documentation
	 * are:
	 * 
	 * SPLICING
	 * FS_DELETION
	 * 
	 * @deprecated Do not use
	 */
	private boolean hasTruncationMutation(Object[] row) {
		boolean result= false;
		String effectText= (String) row[effectIndex];
			
		Matcher truncationMatcher= truncationPattern.matcher(effectText);
		if (truncationMatcher.matches()) { // use matches() rather than find() here
			result= true;
		}
		
		return result;
	}
	
	
	/** 
	 * Checks to see if variant is in a clinical database.
	 * Current DBs include Clinvar and HGMD.
	 * 
	 * IMPORTANT!
	 * CURRENTLY HARDCODED FOR DEMO PURPOSES, NEEDS TO BE INTEGRATED INTO BINARYCONDITION/
	 * COMBOCONDITION QUERY TO SERVER.
	 * 
	 * @deprecated Do not use
	 */
	private boolean inClinicalDB(Object[] row) {
		boolean result= false;
		
		String clinvarText= (String) row[header.indexOf("clinvar_20131105b, info")];
		String hgmdText= (String) row[header.indexOf("hgmd_pro_allmut, gene")];
		
		if ((clinvarText != null && clinvarText.length() > 0)
			|| (hgmdText != null && hgmdText.length() > 0)) {
			result= true;
		}
		
		return result;
	}
	
	
	/** 
	 * Checks that this variant passes coverage and heterozygote ratio thresholds. 
	 * If there is no coverage information present for a variant, it is reported 
	 * as specified by the if-absent parameter. 
	 * @param row The variant row which contains all VCF details
	 * @param outputIfAbsent If AD is absent from FORMAT file and DP4 is absent from INFO field, decides whether to output variant
	 * @return Returns if this variant passes the coverage and ratio cutoffs
	 */
	private boolean coverageAndRatioPass(Object[] row, boolean outputIfAbsent) {
		boolean result= false;
		
		String info_field= (String) row[BasicVariantColumns.INDEX_OF_CUSTOM_INFO];
	
		Matcher dp4Matcher= dp4Pattern.matcher(info_field);
		Matcher formatFieldMatcher= formatFieldPattern.matcher(info_field);
		Matcher sampleInfoFieldMatcher= sampleInfoFieldPattern.matcher(info_field);		
		
		/* Process DP4 or AD or AO text (from VCF INFO or Format columns) if present. */
		if (dp4Matcher.find()) { // NOTE: need to run find() to get group() below

			String dp4Text= dp4Matcher.group(1);

			/* From the samtools definition of the DP4 field:
			 * Number of: 
			 * 1) forward ref alleles; 
			 * 2) reverse ref; 
			 * 3) forward non-ref; 
			 * 4) reverse non-ref alleles, used in variant calling. 
			 * Sum can be smaller than DP because low-quality bases are not counted.
			 * 
			 * URL: http://samtools.sourceforge.net/mpileup.shtml
			 */

			// Split on ":" and check if 1) alt >= threshold, 2) alt/total >= ratio_threshold
			String[] delimited= dp4Text.split(",");
			int refCount= Integer.parseInt(delimited[0]) + Integer.parseInt(delimited[1]);
			int altCount= Integer.parseInt(delimited[2]) + Integer.parseInt(delimited[3]);
			double ratio= ((double) altCount)/(altCount + refCount);

			if (altCount >= coverageThreshold && ratio >= hetRatio) {
				result= true;
			} 
			
		} else if (formatFieldMatcher.find() && sampleInfoFieldMatcher.find()) { // NOTE: need to run find() to get group() below
			
			String formatFieldText= formatFieldMatcher.group(1);
			String sampleInfoFieldText= sampleInfoFieldMatcher.group(1);
			
			// Split on ":" and check if 1) alt >= threshold, 2) alt/total >= ratio_threshold
			String[] adDelimited= formatFieldText.split(":");
			int adIndex= Arrays.asList(adDelimited).indexOf("AD");
			String[] adCoverageDelimited= sampleInfoFieldText.split(":")[adIndex].split(",");
			int refCount= Integer.parseInt(adCoverageDelimited[0]);
			int altCount= Integer.parseInt(adCoverageDelimited[1]);
			double ratio= ((double) altCount)/(altCount + refCount);
			
			if (altCount >= coverageThreshold && ratio >= hetRatio) {
				result= true;
			}
			
		} else if (outputIfAbsent) { // check the default behaviour if coverage is absent
			result= true;
		}
		
		return result;
	}
	
	
	/** Print the table columns to see how it's formatted internally. 
	 * For development testing. */
	private void printTableSchema(TableSchema ts) {
		List<DbColumn> listOfColumns= ts.getColumns();
		int index= 0;
		for (DbColumn d : listOfColumns) {
			System.out.println(d.getColumnNameSQL() + "\t" + index++);
		}
	}
	
	
	/** Get the table header as a String list. */
	private List<String> getTableHeader() {
		List<String> header= new LinkedList<String>();
		
		try {
			AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
			for (AnnotationFormat af : afs) {
				for (CustomField field : af.getCustomFields()) {
					header.add(field.getAlias()); // Fields are added in correct order
				}
			}
		} catch (Exception e) {
			LOG.error(e);
		}
		
		return header;
	}
	
	
	/** Get the variant classification.
	 * Variant can be classified as: disease, complex, potential compound het
	 * or carrier.
	 * @return a list containing the disease classification and inheritance
	 */
	private List<String> getClassification(String geneSymbol, String zygosity, String panel) {
		String classification= null;
		String inheritance= null;
		String queryAddition= "";
		
		if (panel.equals("ACMG")) {
			queryAddition= "AND C.gene in (SELECT gene FROM acmg) ";
		}
		
		// MUST use single quotes		
		String sql=	"SELECT D.classification, S.synonym, C.* " +
					"FROM CGD C, disease_classification D, CGD_synonym S " +
					"WHERE C.gene LIKE '" + geneSymbol + "' " + 
					queryAddition +
					"	AND C.inheritance = S.inheritance " +
					"	AND S.synonym = D.inheritance " +
					"	AND D.zygosity LIKE '" + zygosityMap.get(zygosity) + "' " +
					"	AND (D.gender LIKE '" + GENDER + "' OR D.gender LIKE 'both') ";
		
		ResultSet rs;
		try {
			rs= IncidentalDB.executeQuery(sql);
		
			int rowCount= 0;
			while (rs.next()) {
				/* There should only be a single result from this query. */
				if (rowCount > 1)
					System.err.println(">1 row found for query: " + sql);
				
				/* First and only element is the inheritance, as specified in sql above. */
				List temp= IncidentalDB.getRowAsList(rs);
				classification= (String) temp.get(0);
				inheritance= (String) temp.get(1);
				
				rowCount++;
			}
		} catch (SQLException e) {
			System.out.println("This was just executed: " + sql);
			e.printStackTrace();
		}
		
		
		List<String> output= new ArrayList<String>();
		output.add(classification);
		output.add(inheritance);
		
		return output;
	}
	
	
	/** Get the header for the table using the column aliases. */
	public Map<String, String> getDbToHumanReadableMap() {
		Map<String, String> dbAliasToNameMap = new HashMap<String, String>();

		try {
			AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
			for (AnnotationFormat af : afs) {
				for (CustomField field : af.getCustomFields()) {
					dbAliasToNameMap.put(field.getAlias(), field.getColumnName());
				}
			}
		} catch (Exception e) {
			LOG.error(e);
		}
		
		return dbAliasToNameMap;
	}
	
	
	/** Marks all potential compound heterozygotes in the set of variants. */
	private void identifyPotentialCompoundHet() {
		/* Iterate through all variants and look for the same gene with >1 pika
		 * instance where it's marked as a carrier. */
		
		Map<String, Integer> geneCount= new HashMap<String, Integer>();
		
		// Get the count for all carriers
		for (Object[] row : allVariants) {
			String gene= getGeneSymbol(row);
			
			if (row[CLASSIFICATION_INDEX].equals("carrier")) {
				if (!geneCount.containsKey(gene))
					geneCount.put(gene, 0);

				geneCount.put(gene, (Integer) geneCount.get(gene) + 1);
			}
		}
		
		// Mark compound hets
		for (Object[] row : allVariants) {
			String gene= getGeneSymbol(row);
			
			if (((String) row[CLASSIFICATION_INDEX]).equals("carrier") &&
				((Integer) geneCount.get(gene)) > 1) {
				
				row[CLASSIFICATION_INDEX]= "potential compound het";
			}
		}
	}
	
	/**
	 * Get inverse int[]. If presented with [0,3,4] and the header has 6 elements,
	 * returns the inverse list [1,2,5].
	 * @precondition selected.length <= length
	 */
	public static int[] getHiddenColumns(int[] selected, int length) {
		int[] results= new int[length - selected.length];
		
		int selectedIndex= 0;
		int resultsIndex= 0;
		for (int i= 0; i != length; ++i) {
			if (selectedIndex < selected.length && selected[selectedIndex] == i) {
				selectedIndex++;
			} else {
				results[resultsIndex++]= i;
			}
		}
		
		return results;
	}
			
}