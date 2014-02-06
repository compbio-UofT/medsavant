package medsavant.discovery;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import com.jidesoft.grid.SortableTable;
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
import medsavant.discovery.localDB.DiscoveryDB;
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
 * Compute and store all discovery findings for a patient.
 * 
 * @author rammar
 */
public class DiscoveryFindings {
    private static final Log LOG = LogFactory.getLog(MedSavantClient.class);
	public static final String ALL_GENE_PANEL= "All genes";
	
	private final int DB_VARIANT_REQUEST_LIMIT= 5000;
	private final String JANNOVAR_EFFECT= BasicVariantColumns.JANNOVAR_EFFECT.getAlias();
	private final String JANNOVAR_GENE= BasicVariantColumns.JANNOVAR_SYMBOL.getAlias();
	private final String STOPGAIN= "STOPGAIN";
	private final String FRAMESHIFTS= "FS_%";
	private final String SPLICING= "SPLICING";
	private final String CLINVAR_COLUMN= "clinvar_20131105b, info";
	private final String HGMD_COLUMN= "hgmd_pro_allmut, gene";
	
	private String GENDER= null;
	private ComboCondition currentCC= new ComboCondition(ComboCondition.Op.AND);
	private VariantManagerAdapter vma= MedSavantClient.VariantManager;
	
	public Map<String, String> dbAliasToColumn;
	private int INHERITANCE_INDEX= -1;
	private int CLASSIFICATION_INDEX= -1;
	public String dnaID;
	private int coverageThreshold;
	private double hetRatio;
	private double alleleFrequencyThreshold;
	private Map<String, String> zygosityMap;
	private String genePanel= ALL_GENE_PANEL;
	
	private List<Object[]> allVariants= null;
	public TableSchema ts;
	public List<String> header;
	private int effectIndex;
	private int geneSymbolIndex;
	private int af1000gIndex;
	
	private Pattern dp4Pattern= Pattern.compile(";?DP4=([^;]+);?", Pattern.CASE_INSENSITIVE);
	private Pattern truncationPattern= Pattern.compile("STOPGAIN|FS_\\w+|SPLICING", Pattern.CASE_INSENSITIVE);
	private Pattern geneSymbolPattern= Pattern.compile("^([^:]+)");
	private Pattern formatFieldPattern= Pattern.compile(";?FORMAT=([^;]*AD[^;]*);?", Pattern.CASE_INSENSITIVE); // must contain "AD" in format
	private Pattern sampleInfoFieldPattern= Pattern.compile(";?SAMPLE_INFO=([^;]+);?", Pattern.CASE_INSENSITIVE);
	
	/** Initialize DiscoveryFindings object for the given patient DNA ID.
	 * @param dnaID	Patient's DNA ID
	 */
	public DiscoveryFindings(String dnaID) {
		this.dnaID= dnaID;
					
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
		} catch (Exception ex) {
			System.err.println("IncidentalFindings error: " + ex.toString());
			ex.printStackTrace();
		}
	}
	
	
	/**
	 * Define exception for DiscoveryFindings.
	 */
	public class DiscoveryFindingsException extends Exception {
		public DiscoveryFindingsException(String message) {
			super(message);
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
	
	
	/** 
	 * Get the number of variants returned for this DiscoveryFindings instance.
	 * @return the number of variants stored in this object for the current patient
	 */
	public int getFilteredVariantCount() {
		return allVariants.size();
	}
	
	/**
	 * Get the total number of variants available for this DNA ID.
	 * @ return the number of variants for the current patient
	 */
	public int getMaximumVariantCount() throws Exception {
		List<String> currentDNAIDList= new ArrayList<String>();
		currentDNAIDList.add(dnaID);
		
		return vma.getVariantCountForDNAIDs(
			LoginController.getInstance().getSessionID(),
			ProjectController.getInstance().getCurrentProjectID(),
			ReferenceController.getInstance().getCurrentReferenceID(),
			new Condition[0][0], // empty optional condition matrix here
			currentDNAIDList);
	}
	
	
	/**
	 * Create the ComboCondition for this DiscoveryFindings instance.
	 * @param afColumns	A string list of the column aliases corresponding to the allele frequency DBs to be used for the allele frequency cutoff
	 * @param cov The minimum coverage (number of reads) supporting the alternate variant
	 * @param ratio	The minimum ratio of alt/total reads to be considered as possibly het
	 * @param afThreshold	Allele frequency threshold
	 * @return the current base ComboCondition
	 */
	public ComboCondition getComboCondition(List<Object> afColumns, int cov, double ratio, double afThreshold) {
		ComboCondition cc= new ComboCondition(ComboCondition.Op.AND);
		
		coverageThreshold= cov;
		hetRatio= ratio;
		alleleFrequencyThreshold= afThreshold;

		/* Put the conditions together with ANDs and ORs. */		
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
		
		return cc;
	}
	
	
	/**
	 * Set the current ComboCondition to use.
	 * @param newCC the new ComboCondition to use in the storeVariants() method
	 */
	public void setComboCondition(ComboCondition newCC) {
		currentCC= newCC;
	}
	
	
	/** 
	 * Filter and store all variants for this individual.
	 * @param maxVariants The maximum number of variants to fetch - currently NOT implemented
	 * @precondition Assumes that the ComboCondition has been set
	 */
	public void storeVariants(int maxVariants) throws SQLException, RemoteException, 
		SessionExpiredException, DiscoveryFindingsException {
		
		// If cc has not been filled correctly
		if (currentCC.isEmpty()) {
			throw new DiscoveryFindingsException("ComboCondition is empty, cannot store variants.");
		}
		
		// Initialize/reset allVariants
		allVariants= new ArrayList<Object[]>(DB_VARIANT_REQUEST_LIMIT); // initial capacity DB_VARIANT_REQUEST_LIMIT
		
		Condition[][] conditionMatrix= new Condition[1][1];
		conditionMatrix[0][0]= currentCC;
		
		/* Get variants in chunks based on a request limit offset to save memory. */
		int position= 0;
		List<Object[]> currentVariants= null;
		while (currentVariants == null || currentVariants.size() != 0 ){			
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
	public String getGeneSymbol(Object[] row) {
		String geneSymbol= null;
		
		String hgvsText= (String) row[geneSymbolIndex];
			
		Matcher geneSymbolMatcher= geneSymbolPattern.matcher(hgvsText);
		if (geneSymbolMatcher.find()) {
			geneSymbol= geneSymbolMatcher.group(1);
		}
		
		return geneSymbol;
	}
	
	
	/** 
	 * Filter a list of variants and return only those variants that pass all
	 * filters.
	 */
	private List<Object[]> filterVariants(List<Object[]> input) {
		List<Object[]> filtered= new LinkedList<Object[]>();
		
		for (Object[] row : input) {			
			List<String> query= getClassification(getGeneSymbol(row), 
					(String) row[BasicVariantColumns.INDEX_OF_ZYGOSITY], genePanel);
			
			String classification= query.get(0);
			String inheritance= query.get(1);

			if (genePanel.equals(ALL_GENE_PANEL) || 
				(inheritance != null && !inheritance.equals("") && coverageAndRatioPass(row, true))) {
						
				List<Object> listRow= new ArrayList<Object>(Arrays.asList(row));
				listRow.add(inheritance);
				listRow.add(classification);
				
				// Add to output list
				filtered.add(listRow.toArray());
			}
		}
		
		return filtered;		
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
		
		// MUST use single quotes for HyperSQL (hsql) SQL syntax
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
			rs= DiscoveryDB.executeQuery(sql);
		
			int rowCount= 0;
			while (rs.next()) {
				/* There should only be a single result from this query. */
				if (rowCount > 1)
					System.err.println(">1 row found for query: " + sql);
				
				/* First and only element is the inheritance, as specified in sql above. */
				List temp= DiscoveryDB.getRowAsList(rs);
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
	
	
	/** 
	 * Marks all potential compound heterozygotes in the set of variants.
	 */
	private void identifyPotentialCompoundHet() {
		/* Iterate through all variants and look for the same gene with >1 
		 * instance where it's marked as a carrier. */
		
		Map<String, Integer> geneCount= new HashMap<String, Integer>();
		
		// Get the count for all carriers
		for (Object[] row : allVariants) {
			String gene= getGeneSymbol(row);
			
			if (row[CLASSIFICATION_INDEX] != null &&
				row[CLASSIFICATION_INDEX].equals("carrier")) {
				
				if (!geneCount.containsKey(gene))
					geneCount.put(gene, 0);

				geneCount.put(gene, (Integer) geneCount.get(gene) + 1);
			}
		}
		
		// Mark compound hets
		for (Object[] row : allVariants) {
			String gene= getGeneSymbol(row);
			
			if (row[CLASSIFICATION_INDEX] != null &&
				((String) row[CLASSIFICATION_INDEX]).equals("carrier") &&
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

	
	/**
	 * Sets the gene panel for this instance.
	 * @param panelString A string descriptor for this gene panel.
	 */
	public void setGenePanel(String panelString) {
		this.genePanel= panelString;
	}
	
}