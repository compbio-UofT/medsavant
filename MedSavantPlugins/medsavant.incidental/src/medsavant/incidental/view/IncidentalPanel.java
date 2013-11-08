package medsavant.incidental.view;


import java.util.Calendar;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.GregorianCalendar;
import java.util.Set;
import javax.swing.BorderFactory;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import medsavant.incidental.IncidentalFindings;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.util.notification.VisibleMedSavantWorker;
import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 * Default panel view for Incidentalome app
 * 
 * @author rammar
 */
public class IncidentalPanel extends JPanel {
	
	private int TOP_MARGIN= 0;
	private int SIDE_MARGIN= 100;
	private int BOTTOM_MARGIN= 10;
	
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
	private MedSavantWorker MSWorker;
	private JScrollPane variantPane;
    
	public IncidentalPanel() {
		setupView();
		add(view);
	}

	private void setupView() {
		view= ViewUtil.getClearPanel();
		view.setLayout(new BorderLayout());
		//view.setBorder(BorderFactory.createLineBorder(Color.RED));
		view.setBorder(BorderFactory.createEmptyBorder(TOP_MARGIN, SIDE_MARGIN, BOTTOM_MARGIN, SIDE_MARGIN));
		
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
						
						if (currentIndividualDNA != null) {
							choosePatientButton.setText(currentIndividual);
						} else {
							choosePatientButton.setText("No DNA ID for " + currentIndividual);
						}
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
					if (selectedIndividuals != null && selectedIndividuals.size() == 1) {
						date= new GregorianCalendar(); // update date/time
						String MSWorkerText= "Incidentalome #" + date.get(Calendar.DAY_OF_MONTH) + "d" + 
							date.get(Calendar.HOUR_OF_DAY) + "h" + date.get(Calendar.MINUTE) + "m" + 
							date.get(Calendar.SECOND) + "s";

						MSWorker= new MedSavantWorker<Object> (
							IncidentalPanel.class.getCanonicalName()) {

							IncidentalFindings incFin;
								
							@Override
							protected Object doInBackground() throws Exception {
								/* Starts a new thread for background tasks. */
								incFin= new IncidentalFindings(currentIndividualDNA);
								return null;
							}

							@Override
							protected void showSuccess(Object t) {	
							/* All updates to display should happen here to be run. */
								updateVariantPane(incFin);
							}
						};
						
						MSWorker.execute();
						
					} else {
						// DIALOG box to instruct user to select a single individual
					}			
				}
			}
		);		
		
		variantPane= new JScrollPane();
		workview.add(variantPane);
		
		view.add(workview, BorderLayout.CENTER);
    }
	
	
	private void jokeStatusUpdate (VisibleMedSavantWorker VMSWorker) throws InterruptedException {
		VMSWorker.setStatusMessage("Priming flux capacitor...");
		Thread.sleep(3000); // pause for 1.5 sec
		VMSWorker.setStatusMessage("Getting to the chopper...");
		Thread.sleep(1000);
		VMSWorker.setStatusMessage("Setting phasers to stun...");
		Thread.sleep(1000);
		for (int i= 0; i < 100; i++) {
			VMSWorker.setStatusMessage(i + "% blamed on the boogie");
			Thread.sleep(20);
		}
		VMSWorker.setStatusMessage("Terminated by T1000.");
	}
	
	
	private void updateVariantPane (IncidentalFindings i) {
		JTable jt= i.testTableOutput();
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		variantPane.setViewportView(jt);
	}
	
	public JPanel getView() {
		return view;
	}
    
    
    
}
