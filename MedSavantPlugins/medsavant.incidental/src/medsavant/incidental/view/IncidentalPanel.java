package medsavant.incidental.view;

import java.util.Calendar;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.Box;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.util.notification.VisibleMedSavantWorker;
import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * Default panel view for Incidentalome app
 * 
 * @author rammar
 */
public class IncidentalPanel {
	
	public static String PAGE_NAME = "Incidentalome";
	
	private JPanel view;
	private RoundedPanel workview;
	private JButton choosePatientButton;
	private JButton analyzeButton;
	private IndividualSelector customSelector;
	private Set<String> selectedIndividuals;
	private String currentIndividual;
	private Calendar date;
    
	public IncidentalPanel() {
		setupView();
	}

	private void setupView() {
		view= ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(view);
				
		workview= new RoundedPanel(10);
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
					
					if (customSelector.hasMadeSelection() && 
						selectedIndividuals.size() == 1) {
						
						currentIndividual= selectedIndividuals.iterator().next();
						choosePatientButton.setText(currentIndividual);
					} else if (customSelector.getHospitalIDsOfSelectedIndividuals().size() > 1){
						choosePatientButton.setText("Choose only 1 patient");
					}
				}
			}
		);
		
		
		/* Run incidental findings analysis */
		analyzeButton= new JButton("Identify Incidental Variants");
		workview.add(Box.createVerticalStrut(10));
		workview.add(analyzeButton, BorderLayout.CENTER);
		
		analyzeButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					new VisibleMedSavantWorker<Object> (IncidentalPanel.class.getCanonicalName(), 
						"Incidentalome " + date.get(Calendar.DAY_OF_WEEK_IN_MONTH) + ", " + 
						date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE) + ":" + 
						date.get(Calendar.SECOND)) {
							
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
							return null;
						}
					};
				}
			}
		);
		
		view.add(workview, BorderLayout.CENTER);
		view.add(Box.createVerticalStrut(10));
    }
	
	
	public JPanel getView() {
		return view;
	}
    
    
    
}
