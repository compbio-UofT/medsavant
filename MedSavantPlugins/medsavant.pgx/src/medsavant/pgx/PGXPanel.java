package medsavant.pgx;

import com.jidesoft.swing.ButtonStyle;
import com.jidesoft.swing.JideButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import medsavant.pgx.localDB.PGXDBFunctions;
import medsavant.pgx.localDB.PGXDBFunctions.PGXMarker;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.appdevapi.AppColors;
import org.ut.biolab.medsavant.shared.appdevapi.DBAnnotationColumns;
import org.ut.biolab.medsavant.shared.appdevapi.Variant;

/**
 * Default panel for Pharmacogenomics app.
 * @author rammar
 */
public class PGXPanel extends JPanel {

	private static Log log= LogFactory.getLog(MedSavantClient.class);
	private static final int CHOOSE_PATIENT_BUTTON_WIDTH= 250;
	private final int SIDE_PANE_WIDTH= 380;
	private final int SIDE_PANE_WIDTH_OFFSET= 20;
	private static final String baseDBSNPUrl= "http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?searchType=adhoc_search&rs=";
	
	/* Patient information. */
	private String currentHospitalID;
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
	private JPanel reportInitJP;
	private JLabel reportStartLabel;
	
	
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
					currentHospitalID= selectedIndividuals.iterator().next();
					currentDNAID= patientSelector.getDNAIDsOfSelectedIndividuals().iterator().next();
					
					if (currentDNAID != null) {
						choosePatientButton.setText(currentHospitalID);
					} else { // can't find this individual's DNA ID - may be a DB error
						errorDialog("Can't find a DNA ID for " + currentHospitalID);
					}
				}
				
				/* Perform a new pharmacogenomic analysis for this DNA ID. */
				analyzePatient();
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
		//patientSideJP.setBackground(ViewUtil.getSidebarColor());
		patientSideJP.setBackground(AppColors.HummingBird);
		// Add a light border only on the right side.
		patientSideJP.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
		patientSideJP.setMinimumSize(new Dimension(SIDE_PANE_WIDTH, 0)); // minimum width for panel
		
		patientSideJP.add(choosePatientButton, "alignx center, wrap");
		patientSideJP.add(new JLabel("This app uses the CPIC guidelines"), "alignx center, gapy 20px, wrap");
		patientSideJP.add(new JLabel("ONLY USE WGS FILES - NO EXOMES YET"), "alignx center, gapy 20px, wrap");
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
		reportInitJP= new JPanel();
		reportStartLabel= new JLabel("Choose a patient to start a pharmacogenomic analysis.");
		
		reportStartLabel.setFont(new Font(reportStartLabel.getFont().getName(), Font.PLAIN, 14));
		reportStartLabel.setForeground(Color.DARK_GRAY);
		
		reportInitJP.setLayout(new MigLayout("align 50% 50%"));
		reportInitJP.add(reportStartLabel);
		
		reportPane= new JScrollPane();
		reportPane.setBorder(BorderFactory.createEmptyBorder());
		reportPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		reportPane.setPreferredSize(reportPane.getMaximumSize().getSize());
		reportPane.setViewportView(reportInitJP);
	}
	
	
	/**
	 * Blank report panel to display when a new analysis is being run.
	 */
	private void clearReportPanel() {
		reportStartLabel.setText("Obtaining pharmacogenomic report for " + 
			this.currentHospitalID + "...");
		reportPane.setViewportView(reportInitJP);
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
		
		// Clear the report panel to avoid confusing this patient for the previous one
		clearReportPanel();
		
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
				
				/* Update the report pane. */
				updateReportPane();
			}
		};
		
		// Execute thread
		pgxAnalysisThread.execute();
	}
	
	
	/**
	 * Update the report panel.
	 */
	private void updateReportPane() {
		/* For each PGx gene, create a separate tab. */
		JTabbedPane tabs= ViewUtil.getMSTabedPane();
		
		for (PGXGene pg : currentPGXAnalysis.getGenes()) {
			JPanel reportJP= new JPanel();
			reportJP.setLayout(new MigLayout("gapy 0px, fillx"));
			
			JLabel diplotype= new JLabel("Diplotype is " + pg.getDiplotype());
			diplotype.setFont(new Font(diplotype.getFont().getName(), Font.PLAIN, 20));
			reportJP.add(diplotype, "wrap");
			
			/* Add a subpanel of tabs. */
			JTabbedPane subtabs= ViewUtil.getMSTabedPane();
			// span the entire panel width
			subtabs.setMinimumSize(new Dimension(subtabs.getMaximumSize().width, 
				subtabs.getPreferredSize().height));
			
			/* Subpanel describing the individuals haplotypes/markers for this individual. */
			JPanel haplotypesJP= new JPanel();
			haplotypesJP.setLayout(new MigLayout("gapy 0px, fillx"));
			haplotypesJP.add(new JLabel("Maternal haplotype: " + pg.getMaternalHaplotype()), "wrap");
			haplotypesJP.add(new JLabel("Paternal haplotype: " + pg.getPaternalHaplotype()), "wrap");
			haplotypesJP.add(new JLabel("Maternal markers: " + pg.getMaternalGenotypes()), "wrap");
			haplotypesJP.add(new JLabel("Paternal markers: " + pg.getPaternalGenotypes()), "wrap");
			for (Variant v : pg.getVariants()) {
				haplotypesJP.add(new JTextField(StringUtils.join(new String[] {v.getGene(), v.getChromosome(), 
					Long.toString(v.getStart()), Long.toString(v.getEnd()), v.getReference(), v.getAlternate(), 
					Integer.toString(v.getAlternateNumber()), v.getGT(),
					(String) v.getColumn(DBAnnotationColumns.DBSNP_TEXT)}, " ")), "wrap");
			}
			subtabs.addTab("Detailed haplotype info", haplotypesJP);
			
			/* Subpanel describing all the markers tested for this gene. */
			JPanel testedMarkersJP= new JPanel();
			testedMarkersJP.setLayout(new MigLayout("gapy 0px, gapx 30px")); // don't use fillx property here
			try {
				makeJPanelRow(testedMarkersJP, Arrays.asList(new String[]
					{"Marker ID", "Chromosome", "Position", "Reference nucleotide",
					"Alternate nucleotide"}), false);
				for (PGXMarker pgxm : PGXDBFunctions.getMarkerInfo(pg.getGene())) {
					makeJPanelRow(testedMarkersJP, Arrays.asList(new String[]
						{pgxm.markerID,	pgxm.chromosome, pgxm.position, pgxm.ref,
						pgxm.alt}), true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			subtabs.addTab("Tested markers for " + pg.getGene(),testedMarkersJP); 
			
			/* Add subtabs to the main report panel. */
			reportJP.add(subtabs, "gapy 100px");
			
			/* Add the main report panel for this gene to the tabs. */
			tabs.addTab(pg.getGene(), reportJP);
		}
		
		reportPane.setViewportView(tabs);
	}
	
	
	/**
	 * Create an error dialog and output the error to the log.
	 * @param errorMessage the error message to display.
	 */
	private void errorDialog(String errorMessage) {
		DialogUtils.displayError("Oops!", errorMessage);
		log.error("[" + this.getClass().getSimpleName() + "]: " + errorMessage);
	}
	
	
	/**
	 * Add a row of JLabels with the text from the input list.
	 * @param container the parent container where the JLabels will be added
	 * @param textList the list of text
	 * @precondition the container is using MigLayout layout; method doesn't check
	 */
	private void makeJPanelRow(JComponent container, List<String> textList, boolean markerIDFirst) {
		for (int i= 0; i != textList.size(); ++i) {
			if (markerIDFirst && i == 0) {
				container.add(getURLButton(textList.get(i), baseDBSNPUrl, textList.get(i), false));
			} else if (i < textList.size() - 1) {
				container.add(new JLabel(textList.get(i)));
			} else { // wrap at the end of this JPanel row
				container.add(new JLabel(textList.get(i)), "wrap");
			}
		}
	}
	
	
	/**
	 * Create a button that opens a URL in a web browser when clicked.
	 * @param buttonText Button text
	 * @param baseURL URL linked from the button
	 * @param appendToURL Append text to the URL
	 * @param doEncode encode the text using the UTF-8
	 * @return a JideButton that opens the URL in a web browser
	 */
	private JideButton getURLButton(String buttonText, final String baseURL, 
		final String appendToURL, final boolean doEncode) {
		
		final String URL_CHARSET = "UTF-8";
		
		JideButton urlButton= new JideButton(buttonText);
		urlButton.setButtonStyle(ButtonStyle.HYPERLINK_STYLE);
		urlButton.setForeground(Color.BLUE);
		urlButton.setToolTipText("Lookup " + buttonText + " on the web");
		urlButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					URL url;
					if (doEncode)
						url = new URL(baseURL + URLEncoder.encode(appendToURL, URL_CHARSET));
					else
						url = new URL(baseURL + appendToURL);
					
					java.awt.Desktop.getDesktop().browse(url.toURI());
				} catch (Exception ex) {
					ClientMiscUtils.reportError("Problem launching website: %s", ex);
				}
			}
		});
		
		return urlButton;
	}
}
