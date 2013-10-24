package medsavant.incidental;

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
		
	}
	
}
