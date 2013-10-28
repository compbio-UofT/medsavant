package medsavant.incidental.view;


import java.util.Calendar;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.GregorianCalendar;
import java.util.Set;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import org.ut.biolab.medsavant.client.util.notification.VisibleMedSavantWorker;
import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 * Default panel view for Incidentalome app
 * 
 * @author rammar
 */
public class IncidentalPanel extends JPanel {
	
	public static String PAGE_NAME = "Incidentalome";
	
	private JPanel view;
	private RoundedPanel workview;
	private JButton choosePatientButton;
	private JButton analyzeButton;
	private IndividualSelector customSelector;
	private Set<String> selectedIndividuals;
	private String currentIndividual;
	private String currentIndividualDNA;
	private Calendar date;
	private VisibleMedSavantWorker VMSWorker;
    
	public IncidentalPanel() {
		setupView();
		add(view);
	}

	private void setupView() {
		view= ViewUtil.getClearPanel();
				
		workview= new RoundedPanel(10);
		ViewUtil.applyVerticalBoxLayout(workview);
		workview.add(new JLabel("Patient selection:"));
		
		choosePatientButton= new JButton("Choose Patient");
		workview.add(choosePatientButton);
		
		/* Choose the patient's sample using the individual selector. */
		customSelector= new IndividualSelector();
		choosePatientButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					customSelector.setVisible(true);
					selectedIndividuals= customSelector.getHospitalIDsOfSelectedIndividuals();
					
					if (customSelector.hasMadeSelection() && selectedIndividuals.size() == 1) {
						
						currentIndividual= selectedIndividuals.iterator().next();
						currentIndividualDNA= customSelector.getDNAIDsOfSelectedIndividuals().iterator().next();
						choosePatientButton.setText(currentIndividual);
					} else if (customSelector.getHospitalIDsOfSelectedIndividuals().size() > 1){
						choosePatientButton.setText("Choose only 1 patient");
					}
				}
			}
		);
		
		
		/* Run incidental findings analysis */
		analyzeButton= new JButton("Identify Incidental Variants");
		workview.add(analyzeButton);
		
		analyzeButton.addActionListener(
			new ActionListener() {
				
				@Override
				public void actionPerformed (ActionEvent e) {
					date= new GregorianCalendar(); // update date/time
					String MSWorkerText= "Incidentalome #" + date.get(Calendar.DAY_OF_MONTH) + "d" + 
						date.get(Calendar.HOUR_OF_DAY) + "h" + date.get(Calendar.MINUTE) + "m" + 
						date.get(Calendar.SECOND) + "s";
					
					VMSWorker= new VisibleMedSavantWorker<Object> (
						IncidentalPanel.class.getCanonicalName(), MSWorkerText) {
							
						@Override
						protected void showResults() {
							///FILL
						}
						
						@Override
						protected void jobDone() {
							///FILL
						}
						
						@Override
						protected Object runInBackground() throws Exception {
							///FILL
							jokeStatusUpdate(VMSWorker);
							return null;
						}
					};
					
					VMSWorker.execute();
				}
			}
		);
		
		view.add(workview, BorderLayout.CENTER);
    }
	
	
	private void jokeStatusUpdate (VisibleMedSavantWorker VMSWorker) throws InterruptedException {
		VMSWorker.setStatusMessage("Priming flux capacitor...");
		Thread.sleep(3000); // pause for 1.5 sec
		VMSWorker.setStatusMessage("Getting to the chopper...");
		Thread.sleep(1500);
		VMSWorker.setStatusMessage("Setting phasers to stun...");
		Thread.sleep(1500);
		for (int i= 0; i < 100; i++) {
			VMSWorker.setStatusMessage(i + "% blamed on the boogie");
			Thread.sleep(100);
		}
		VMSWorker.setStatusMessage("Terminated by T1000.");
	}
	
	
	public JPanel getView() {
		return view;
	}
    
    
    
}
