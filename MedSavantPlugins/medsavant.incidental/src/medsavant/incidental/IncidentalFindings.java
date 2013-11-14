package medsavant.incidental;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTable;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 * Compute and store all incidental findings for a patient.
 * 
 * @author rammar
 */
public class IncidentalFindings {
    
	private final int DB_VARIANT_REQUEST_LIMIT= 5000;
	
	private String dnaID;
	private int coverageThreshold;
	private double hetRatio;
	
	private List<Object[]> allVariants;
	private TableSchema ts;
	private Object[] header;
	private int effectIndex;
	private int geneSymbolIndex;
	
	private Pattern dp4Pattern= Pattern.compile(";?DP4=([^;]+);?", Pattern.CASE_INSENSITIVE);
	private Pattern truncationPattern= Pattern.compile("STOPGAIN|FS_\\w+|SPLICING", Pattern.CASE_INSENSITIVE);
	private Pattern geneSymbolPattern= Pattern.compile("^([^(]+)");
	
	
	public IncidentalFindings(String dnaID, int cov, double ratio) {
		this.dnaID= dnaID;
		coverageThreshold= cov;
		hetRatio= ratio;
		allVariants= new ArrayList<Object[]>(DB_VARIANT_REQUEST_LIMIT); // initial capacity DB_VARIANT_REQUEST_LIMIT
		
		ts= ProjectController.getInstance().getCurrentVariantTableSchema();		
		header= getTableHeader();
		effectIndex= Arrays.asList(header).indexOf("effect");
		geneSymbolIndex= Arrays.asList(header).indexOf("hgvs");
		
		try {
			storeVariants();
		} catch (Exception ex) {
			System.err.println("IncidentalFindings error: " + ex.toString());
			ex.printStackTrace();
		}
	}
	
		
	/** JTable output for development testing. */
	public JTable testTableOutput() {
		Object[][] rowData= allVariants.toArray(new Object[allVariants.size()][]); // List<Object[]> to Object[][]		
		
		//Object[] jtableHeader= {"CHROM", "POSITION", "DBSNP_ID", "REF", "ALT", "VARIANT_TYPE", "EFFECT", "GENE SYMBOL"};
		//JTable t= new JTable(rowDataSubset, jtableHeader);
		
		JTable t= new JTable(rowData, header);
		
		return t;
	}
	
	
	/* Filter and store all variants for this individual. */
	private void storeVariants() throws SQLException, RemoteException, SessionExpiredException {
		VariantManagerAdapter vma= MedSavantClient.VariantManager;
		
		Condition dnaCondition= BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.DNA_ID), dnaID);
			   
		Condition[][] conditionMatrix= new Condition[1][1];
		conditionMatrix[0][0]= dnaCondition;
		
		/* Get variants in chunks based on a request limit offset to save memory. */
		int position= 0;
		List<Object[]> currentVariants= vma.getVariants(LoginController.getInstance().getSessionID(),
			ProjectController.getInstance().getCurrentProjectID(),
			ReferenceController.getInstance().getCurrentReferenceID(),
			conditionMatrix,
			position, DB_VARIANT_REQUEST_LIMIT);
		
		while (currentVariants.size() != 0) {
			allVariants.addAll(filterVariants(currentVariants));
			
			position += DB_VARIANT_REQUEST_LIMIT;
			currentVariants= vma.getVariants(LoginController.getInstance().getSessionID(),
				ProjectController.getInstance().getCurrentProjectID(),
				ReferenceController.getInstance().getCurrentReferenceID(),
				conditionMatrix,
				position, DB_VARIANT_REQUEST_LIMIT);
		}
		
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
	
	
	/* Filter a list of variants and return only those variants that pass all
	 * filters.
	 */
	private List<Object[]> filterVariants(List<Object[]> input) {
		List<Object[]> filtered= new LinkedList<Object[]>();
		
		for (Object[] row : input) {
			if (isDiseaseGene(row) && (hasTruncationMutation(row) || inClinicalDB(row))) {
				filtered.add(row);
			}
		}
		
		return filtered;		
	}
	
	/* Checks DB to see if this variant comes from a disease gene. */
	private boolean isDiseaseGene(Object[] row) {
		// FILL IN -- TESTING NOW
		return true;
	}
	
	/* Checks if this variant encodes a truncation mutation. 
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
	
	
	/* Checks to see if variant is in a clinical database.
	 * Current DBs include Clinvar and HGMD.
	 */
	private boolean inClinicalDB(Object[] row) {
		////// FILL IN - will have to use JDBC
		return false; ///// TEMP VALUE
	}
			
	
	/* Checks that this variant passes coverage and heterozygote ratio thresholds. */
	private boolean coverageAndRatioPass(Object[] row) {
		boolean result= false;
		
		String info_field= (String) row[BasicVariantColumns.INDEX_OF_CUSTOM_INFO];
	
		Matcher dp4Matcher= dp4Pattern.matcher(info_field);

		if (dp4Matcher.find()) {

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

			// Split on "," and check if 1) alt >= 10x, 2) alt/total >= 0.3
			String[] delimited= dp4Text.split(",");
			int refCount= Integer.parseInt(delimited[0]) + Integer.parseInt(delimited[1]);
			int altCount= Integer.parseInt(delimited[2]) + Integer.parseInt(delimited[3]);
			double hetRatio= ((double) altCount)/(altCount + refCount);

			if (altCount >= 10 && hetRatio >= 0.3) {
				result= true;
			} /*else {
				System.out.println("Reject variant DP4: " + dp4Text);
				System.out.println("Alt Count: " + altCount + " refCount: " + refCount);
				System.out.println("Ratio: " + hetRatio);
			}*/
		}
		
		return result;
	}
	
	
	/* Print the table columns to see how it's formatted internally. 
	 * For development testing. */
	private void printTableSchema(TableSchema ts) {
		List<DbColumn> listOfColumns= ts.getColumns();
		int index= 0;
		for (DbColumn d : listOfColumns) {
			System.out.println(d.getColumnNameSQL() + "\t" + index++);
		}
	}
	
	
	/* Get the table header as a String array. */
	private String[] getTableHeader() {
		List<DbColumn> listOfColumns= ts.getColumns();
		String[] header= new String[listOfColumns.size()];
		
		int index= 0;
		for (DbColumn d : listOfColumns) {
			header[index++]= d.getColumnNameSQL();
		}
		
		return header;
	}
}
