package medsavant.pgx;

import com.jidesoft.swing.JideButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * Default panel for Pharmacogenomics app.
 * @author rammar
 */
public class PGXPanel extends JPanel {

	private static Log log= LogFactory.getLog(MedSavantClient.class);
	private static final int CHOOSE_PATIENT_BUTTON_WIDTH= 250;
	private final int SIDE_PANE_WIDTH= 380;
	private final int SIDE_PANE_WIDTH_OFFSET= 20;
	
	/* Patient information. */
	private String currentHosptialID;
	private String currentDNAID;
	private PGXAnalysis currentPGXAnalysis;
	
	/* UI components. */
	private JPanel appView;
	private JScrollPane patientSidePane;
	private JPanel patientSideJP;
	private JScrollPane reportPane;
	private JideButton choosePatientButton;
	private IndividualSelector patientSelector;
	private JLabel status;
	private ProgressWheel statusWheel;
	
	public PGXPanel() {
		setupApp();
	}
	
	
	/**
	 * Get the main PGx panel view.
	 * @return the main PGx JPanel
	 */
	public JPanel getView() {
		return appView;
	}
	
	
	/**
	 * Set up the main app.
	 */
	private void setupApp() {
		// initialize and set up the main app JPanel
		appView= new JPanel();
		appView.setLayout(new MigLayout("insets 0px, gapx 0px"));
		
		// Create and add components to the patient side panel
		initPatientSidePanel();
		initReportPanel();
		
		// Add all the components to the main app view
		appView.add(patientSidePane);
		appView.add(reportPane);
	}
	
	
	/**
	 * Action to perform when choose patient button is clicked.
	 * @return the ActionListener for this button
	 */
	private ActionListener choosePatientAction() {
		// create an anonymous class
		ActionListener outputAL= new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				/* Show the patient selector window and get the patient selected
				 * by user. */
				patientSelector.setVisible(true);
				Set<String> selectedIndividuals= patientSelector.getHospitalIDsOfSelectedIndividuals();
				
				/* Once the user has made a patient hospital ID selection, get 
				 * the DNA ID so we can retrieve the patient's variants. */
				if (patientSelector.hasMadeSelection()) {
					currentHosptialID= selectedIndividuals.iterator().next();
					currentDNAID= patientSelector.getDNAIDsOfSelectedIndividuals().iterator().next();
					
					if (currentDNAID != null) {
						choosePatientButton.setText(currentHosptialID);
					} else { // can't find this individual's DNA ID - may be a DB error
						errorDialog("Can't find a DNA ID for " + currentHosptialID);
					}
				}
				
				/* Perform a new pharmacogenomic analysis for this DNA ID. */
				analyzePatient();
				
				/* Update the report pane. */
				updateReportPane();
			}
		};
		
		return outputAL;
	}
	
	
	/**
	 * Initialize the patient side panel.
	 */
	private void initPatientSidePanel() {		
		patientSideJP= new JPanel();
		
		// the patient selector dialog
		patientSelector= new IndividualSelector(true);
		
		// the choose patient button
		choosePatientButton= new JideButton("Choose Patient");
		choosePatientButton.setButtonStyle(JideButton.TOOLBAR_STYLE);
		choosePatientButton.setOpaque(true);
		choosePatientButton.setFont(new Font(choosePatientButton.getFont().getName(),
			Font.PLAIN, 18));
		choosePatientButton.setMinimumSize(new Dimension(
			CHOOSE_PATIENT_BUTTON_WIDTH, choosePatientButton.getHeight()));
		choosePatientButton.addActionListener(choosePatientAction());
		
		// The status message
		status= new JLabel();
		status.setFont(new Font(status.getFont().getName(), Font.PLAIN, 15));
		statusWheel= new ProgressWheel();
		statusWheel.setIndeterminate(true);
		// hide for now
		status.setVisible(false);
		statusWheel.setVisible(false);
		
		/* Layout notes:
		 * Create a bit of inset spacing top and left, no space between 
		 * components unless explicitly specified.
		 * Also, the components will not be centred within the panel, unless the
		 * entire panel is filled widthwise - this means that if you add a small
		 * button, when you centre it, it won't be centred in the panel unless
		 * you have a full-panel-width component below/above it. I'm specifying 
		 * "fillx" for the layout to solve this issue. */
		patientSideJP.setLayout(new MigLayout("insets 10 10 0 0, gapy 0px, fillx"));
		patientSideJP.setBackground(ViewUtil.getSidebarColor());
		patientSideJP.setMinimumSize(new Dimension(SIDE_PANE_WIDTH, 0)); // minimum width for panel
		
		patientSideJP.add(choosePatientButton, "alignx center, wrap");
		patientSideJP.add(new JLabel("STUFF GOES HERE"), "alignx center, gapy 20px, wrap");
		patientSideJP.add(status, "alignx center, gapy 50px, wrap");
		patientSideJP.add(statusWheel, "alignx center, wrap");
		
		// initialize the scroll pane and set size constraints
		patientSidePane= new JScrollPane();
		patientSidePane.setBorder(BorderFactory.createEmptyBorder());
		patientSidePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		patientSidePane.setMinimumSize(new Dimension(SIDE_PANE_WIDTH, 0)); // minimum width
		patientSidePane.setPreferredSize(new Dimension(SIDE_PANE_WIDTH, 
			patientSideJP.getMaximumSize().height)); // preferred height
		patientSidePane.setViewportView(patientSideJP);
	}
	
	
	/**
	 * Initialize the report panel.
	 */
	private void initReportPanel() {
		JPanel reportJP= new JPanel();
		JLabel reportStartLabel= new JLabel("Choose a patient to start a pharmacogenomic analysis.");
		
		reportStartLabel.setFont(new Font(reportStartLabel.getFont().getName(), Font.PLAIN, 14));
		reportStartLabel.setForeground(Color.DARK_GRAY);
		
		reportJP.setLayout(new MigLayout("align 50% 50%"));
		reportJP.add(reportStartLabel);
		
		reportPane= new JScrollPane();
		reportPane.setBorder(BorderFactory.createEmptyBorder());
		reportPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		reportPane.setPreferredSize(reportPane.getMaximumSize().getSize());
		reportPane.setViewportView(reportJP);
	}
	
	
	/** 
	 * Perform a new pharmacogenomic analysis for this DNA ID in a background
	 * thread and display a progress status message. 
	 */
	private void analyzePatient() {
		// Update status message
		status.setText("Performing pharmacogenomic analysis...");
		status.setVisible(true);
		statusWheel.setVisible(true);
		
		/* Background task. */
		MedSavantWorker pgxAnalysisThread= new MedSavantWorker<Object>(PGXPanel.class.getCanonicalName()) {
			@Override
			protected Object doInBackground() throws Exception {
				/* Create and perform a new analysis. */
				try {
					currentPGXAnalysis= new PGXAnalysis(currentDNAID);
				} catch (SQLException se) {
					errorDialog(se.getMessage());
					se.printStackTrace();
				}

				return null;
			}

			@Override protected void showSuccess(Object t) {
				status.setText("Analysis complete.");
				statusWheel.setVisible(false);
			}
		};
		
		// Execute thread
		pgxAnalysisThread.execute();
	}
	
	
	/**
	 * Update the report panel.
	 */
	private void updateReportPane() {
		JPanel reportJP= new JPanel();
		reportJP.setLayout(new MigLayout("gapy 0px, fillx"));
		
		reportJP.add(new JLabel("Record retrieved for DNA ID: " + currentDNAID), "wrap");
		
		reportPane.setViewportView(reportJP);
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
