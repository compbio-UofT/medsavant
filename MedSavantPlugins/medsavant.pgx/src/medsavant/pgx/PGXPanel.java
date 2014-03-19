package medsavant.pgx;

import com.jidesoft.swing.JideButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	
	private String currentHosptialID;
	private String currentDNAID;
	private JPanel appView;
	private JScrollPane patientSidePanel;
	private JScrollPane reportPanel;
	private JideButton choosePatientButton;
	private IndividualSelector patientSelector;
	
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
		appView.add(patientSidePanel);
		appView.add(reportPanel);
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
				patientSelector.setVisible(true); // show the patient selector window

				Set<String> selectedIndividuals= patientSelector.getHospitalIDsOfSelectedIndividuals();
				
				if (patientSelector.hasMadeSelection()) {
					currentHosptialID= selectedIndividuals.iterator().next();
					currentDNAID= patientSelector.getDNAIDsOfSelectedIndividuals().iterator().next();
					
					if (currentDNAID != null) {
						choosePatientButton.setText(currentHosptialID);
					} else { // can't find this individual's DNA ID - may be a DB error
						errorDialog("Can't find a DNA ID for " + currentHosptialID);
					}
				}
			}
		};
		
		return outputAL;
	}
	
	
	/**
	 * Initialize the patient side panel.
	 */
	private void initPatientSidePanel() {		
		JPanel patientSideJP= new JPanel();
		
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
		patientSideJP.add(new JLabel("Diplotypes"), "alignx center, gapy 20px, wrap");
		
		// initialize the scroll pane and set size constraints
		patientSidePanel= new JScrollPane();
		patientSidePanel.setBorder(BorderFactory.createEmptyBorder());
		patientSidePanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		patientSidePanel.setMinimumSize(new Dimension(SIDE_PANE_WIDTH, 0)); // minimum width
		patientSidePanel.setPreferredSize(new Dimension(SIDE_PANE_WIDTH, 
			patientSideJP.getMaximumSize().height)); // preferred height
		patientSidePanel.setViewportView(patientSideJP);
	}
	
	
	/**
	 * Initialize the report panel.
	 */
	private void initReportPanel() {
		JPanel reportJP= new JPanel();
		JLabel reportStartLabel= new JLabel("ADD text here.");
		
		reportStartLabel.setFont(new Font(reportStartLabel.getFont().getName(), Font.PLAIN, 14));
		reportStartLabel.setForeground(Color.DARK_GRAY);
		
		reportJP.setLayout(new MigLayout("align 50% 50%"));
		reportJP.add(reportStartLabel);
		
		reportPanel= new JScrollPane();
		reportPanel.setBorder(BorderFactory.createEmptyBorder());
		reportPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		reportPanel.setPreferredSize(reportPanel.getMaximumSize().getSize());
		reportPanel.setViewportView(reportJP);
	}
	
	
	/**
	 * Update the report panel.
	 */
	private void updateReportPanel() {
		
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
