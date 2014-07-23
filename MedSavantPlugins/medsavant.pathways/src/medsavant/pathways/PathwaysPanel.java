package medsavant.pathways;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.appdevapi.AppColors;


/**
 * Default panel for Pathways App.
 * @author rwong
 */
public class PathwaysPanel {
	
	private static final int SIDE_PANE_WIDTH= 380;
	
	private static Log log= LogFactory.getLog(MedSavantClient.class);
	
	private JPanel appView= new JPanel();
	private JPanel optionsPanel= new JPanel();
	private JScrollPane scrollPane= new JScrollPane();
	private JPanel resultsPanel= new JPanel();
	private IndividualSelector patientSelector= new IndividualSelector(true);
	private JButton choosePatientButton;
	private String currentHospitalID;
	private String currentDNAID;
	
	
	/**
	 * Create a new PathwaysPanel.
	 */
	public PathwaysPanel() {
		initView();
	}
	
	
	/**
	 * Return the main JPanel of this PathwaysPanel
	 * @return the main view JPanel
	 */
	public JPanel getView() {
		return appView;
	}
	
	
	/**
	 * Default initial view.
	 */
	private void initView() {
		// Create the options view
		optionsPanel.setLayout(new MigLayout("fillx"));
		optionsPanel.setMinimumSize(new Dimension(SIDE_PANE_WIDTH, 1));
		optionsPanel.setPreferredSize(new Dimension(SIDE_PANE_WIDTH, optionsPanel.getMaximumSize().height));
		optionsPanel.setBackground(AppColors.MountainMeadow);
		optionsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
		
		
		choosePatientButton= new JButton("Choose patient");
		choosePatientButton.addActionListener(choosePatientAction());
		
		optionsPanel.add(choosePatientButton, "alignx center, wrap");
		optionsPanel.add(new JLabel("Test Label"), "alignx center, wrap");
		
		
		// Create the results view
		resultsPanel.setLayout(new MigLayout());
		resultsPanel.setBackground(Color.WHITE);
		
		//TESTING
		for (int i= 0; i != 150; ++i) {
			resultsPanel.add(new JLabel("bladdam! #" + i), "wrap");
		}
		////
		
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportView(resultsPanel);
		
		// Add all components to our main view
		appView.setLayout(new MigLayout("insets 0"));
		appView.add(optionsPanel);
		appView.add(scrollPane);
		
		// set the preferred size once the component is displayed.
		appView.addComponentListener(new ComponentListener()
			{				
				@Override
				public void componentShown(ComponentEvent e) {
					Dimension d= appView.getSize();
					scrollPane.setPreferredSize(new Dimension(d.width - SIDE_PANE_WIDTH, d.height));
					scrollPane.setMinimumSize(new Dimension(d.width - SIDE_PANE_WIDTH, d.height));
					scrollPane.setMaximumSize(new Dimension(d.width - SIDE_PANE_WIDTH, d.height));
					appView.updateUI();
				}
				
				@Override
				public void componentResized(ComponentEvent e) {
					componentShown(e);
				}
				
				@Override public void componentHidden(ComponentEvent e) {}
				@Override public void componentMoved(ComponentEvent e) {}
			}
		);
	}
	
	
	
	/**
	 * Action to perform when choose patient button is clicked.
	 * @return the ActionListener for this button
	 */
	private ActionListener choosePatientAction() {
		// create an anonymous class
		ActionListener outputAL= new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent ae) {
					/* Show the patient selector window and get the patient selected
					 * by user. */
					patientSelector.setVisible(true);
					Set<String> selectedIndividuals= patientSelector.getHospitalIDsOfSelectedIndividuals();

					/* Once the user has made a patient hospital ID selection, get 
					 * the DNA ID so we can retrieve the patient's variants. */
					if (patientSelector.hasMadeSelection()) {
						currentHospitalID= patientSelector.getHospitalIDsOfSelectedIndividuals().iterator().next();
						String newDNAID= patientSelector.getDNAIDsOfSelectedIndividuals().iterator().next();

						if (newDNAID != null) {
							currentDNAID= newDNAID;
							choosePatientButton.setText(currentHospitalID);						
						} else { // can't find this individual's DNA ID - may be a DB error
							errorDialog("Can't find a DNA ID for " + currentHospitalID);
						}
					}

					/* Prevent further patient selection while an analysis thread is
					 * running. */
					//choosePatientButton.setEnabled(false);

					/* Perform a analysis. */
					// CALL ANALYSIS METHOD IN A NEW THREAD
				}
			};
		
		return outputAL;
	}

	
	/**
	 * Create an error dialog and output the error to the log.
	 * @param errorMessage the error message to display.
	 */
	private void errorDialog(String errorMessage) {
		DialogUtils.displayError("Oops!", errorMessage);
		log.error("[" + this.getClass().getSimpleName() + "]: " + errorMessage);
	}
	
}