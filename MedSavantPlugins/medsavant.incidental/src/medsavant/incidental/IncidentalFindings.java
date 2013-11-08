package medsavant.incidental;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.rmi.RemoteException;
import java.sql.SQLException;
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
 * Compute and store all incidental findings for a patient
 * 
 * @author rammar
 */
public class IncidentalFindings {
    
	private String dnaID;
	
	private List<Object[]> allVariants;
	
	private Pattern dp4Pattern= Pattern.compile(";?DP4=([^;]+);?");
	private Matcher dp4Matcher;
	
	public IncidentalFindings(String dnaID) {
		this.dnaID= dnaID;
		
		try {
			getVariants();
		} catch (Exception ex) {
			System.err.println("IncidentalFindings error: " + ex.toString());
		}
	}
	
	private void getVariants() throws SQLException, RemoteException, SessionExpiredException {
		VariantManagerAdapter vma= MedSavantClient.VariantManager;
		
		// Use the hospital ID to search within DNA IDs
		TableSchema ts= ProjectController.getInstance().getCurrentVariantTableSchema();		
		Condition dnaCondition= BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.DNA_ID), dnaID);
			   
		Condition[][] conditionMatrix= new Condition[1][1];
		conditionMatrix[0][0]= dnaCondition;
		
		allVariants= vma.getVariants(LoginController.getInstance().getSessionID(),
			ProjectController.getInstance().getCurrentProjectID(),
			ReferenceController.getInstance().getCurrentReferenceID(),
			conditionMatrix,
			0,1000000); // Need to capture all variants - what value to set?
	}
	
	public JTable testTableOutput() {
		Object[][] rowData= allVariants.toArray(new Object[allVariants.size()][]); // List<Object[]> to Object[][]
		Object[] header= {"UPLOAD_ID", "FILE_ID", "VARIANT_ID", "DNA_ID", 
			"CHROM", "POSITION", "DBSNP_ID", "REF", "ALT", "QUAL", "FILTER",
			"VARIANT_TYPE", "ZYGOSITY", "GT", "CUSTOM_INFO"};
		
		// Iterate through table and remove all variants that fall outside of cutoff range
		List<Object[]> rowDataNew= new LinkedList<Object[]>();
		
		for (Object[] row : rowData) {
			String info_field= (String) row[BasicVariantColumns.INDEX_OF_CUSTOM_INFO];
			dp4Matcher= dp4Pattern.matcher(info_field);
			
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
					rowDataNew.add(row);
				/*
				} else {
					System.out.println("Reject variant DP4: " + dp4Text);
					System.out.println("Alt Count: " + altCount + " refCount: " + refCount);
					System.out.println("Ratio: " + hetRatio);
				*/
				}
			}
		}
		
		System.out.println("Incidental variant filtering: Before = " + 
				rowData.length + " After = " + rowDataNew.size());
		
		rowData= rowDataNew.toArray(new Object[rowDataNew.size()][]);
		
		JTable t= new JTable(rowData, header);
		
		return t;
	}
}
