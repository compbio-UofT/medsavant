package medsavant.incidental;

import com.healthmarketscience.sqlbuilder.Condition;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.MedSavantClient;

/**
 * Compute and store all incidental findings for a patient
 * 
 * @author rammar
 */
public class IncidentalFindings {
    
	private String dnaID;
			
	public IncidentalFindings(String dnaID) {
		this.dnaID= dnaID;
		getVariants();
	}
	
	private void getVariants() {
		VariantManagerAdapter vma= MedSavantClient.VariantManager;
		
		vma.getVariants(LoginController.getInstance().getSessionID(),
			ProjectController.getInstance().getCurrentProjectID(),
			ReferenceController.getInstance().getCurrentReferenceID(),
			new Condition[][]{{r}},
			0,10000);
		
	}
	
}
