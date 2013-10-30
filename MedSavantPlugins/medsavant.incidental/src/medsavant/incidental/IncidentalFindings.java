package medsavant.incidental;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JTable;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.MedSavantClient;
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
		
		Condition dnaCondition= BinaryCondition.equalTo(BasicVariantColumns.DNA_ID, dnaID);
		Condition[][] conditionMatrix= new Condition[1][1];
		conditionMatrix[0][0]= dnaCondition;
		
		allVariants= vma.getVariants(LoginController.getInstance().getSessionID(),
			ProjectController.getInstance().getCurrentProjectID(),
			ReferenceController.getInstance().getCurrentReferenceID(),
			conditionMatrix,
			0,10000);
		
		////////// TESTING OUTPUT
		System.out.println("IncidentalFindings.java: Number of variants: " + allVariants.size());
		for (Object[] row : allVariants) {
			System.out.println("Chr " + row[BasicVariantColumns.INDEX_OF_CHROM] + " " + 
					"Pos " + row[BasicVariantColumns.INDEX_OF_POSITION] + " " + 
					"Alt " + row[BasicVariantColumns.INDEX_OF_ALT]);
		}
		///////////
		
	}
	
	public JTable testTableOutput() {
		Object[][] rowData= allVariants.toArray(new Object[allVariants.size()][]); // List<Object[]> to Object[][]
		Object[] header= {"UPLOAD_ID", "FILE_ID", "VARIANT_ID", "DNA_ID", 
			"CHROM", "POSITION", "DBSNP_ID", "REF", "ALT", "QUAL", "FILTER",
			"VARIANT_TYPE", "ZYGOSITY", "GT", "CUSTOM_INFO"};
		JTable t= new JTable(rowData, header);
		
		return t;
	}
}
