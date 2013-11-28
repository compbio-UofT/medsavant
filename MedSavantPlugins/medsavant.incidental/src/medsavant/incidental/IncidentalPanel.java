package medsavant.incidental;

import com.jidesoft.swing.CheckBoxList;
import java.util.Calendar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import medsavant.incidental.localDB.IncidentalDB;
import medsavant.incidental.localDB.IncidentalHSQLServer;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;


/**
 * Default panel view for Incidentalome app
 * 
 * @author rammar
 */
public class IncidentalPanel extends JPanel {
	private static final Log LOG = LogFactory.getLog(MedSavantClient.class);
	
	private final int TOP_MARGIN= 0;
	private final int SIDE_MARGIN= 100;
	private final int BOTTOM_MARGIN= 10;
	private final int TEXT_AREA_WIDTH= 80;
	private final int TEXT_AREA_HEIGHT= 25;
	
	private int coverageThreshold= 10; // Threshold coverage for a variant
	private double hetRatio= 0.3;
	private double afThreshold= 0.05;
	
	public static final String PAGE_NAME = "Incidentalome";
	private static final String INCIDENTAL_DB_USER= "incidental_user";
	private static final String INCIDENTAL_DB_PASSWORD= "$hazam!2734"; // random password
	
	private boolean analysisRunning= false;
	private boolean dbLoaded= false;
	
	private JPanel view;
	private RoundedPanel workview;
	private JLabel topText;
	private JButton choosePatientButton;
	private JButton analyzeButton;
	private String analyzeButtonDefaultText= "Identify Incidental Variants";
	private IndividualSelector customSelector;
	private Set<String> selectedIndividuals;
	private String currentIndividual;
	private String currentIndividualDNA;
	private Calendar date;
	private MedSavantWorker MSWorker;
	private JScrollPane variantPane;
	private ProgressWheel pw;
	private JLabel progressLabel;
	private final int preferredNumColumns= 10;
	private JLabel coverageThresholdLabel= new JLabel("Min. variant coverage (X)");
	private JTextField coverageThresholdText= new JTextField(Integer.toString(coverageThreshold));
	private JButton coverageThresholdHelp;
	private JLabel hetRatioLabel= new JLabel("Min. ratio of alternate/total reads");
	private JTextField hetRatioText= new JTextField(Double.toString(hetRatio));
	private JButton hetRatioHelp;
	private JLabel afThresholdLabel= new JLabel("Max. allele frequency");
	private JTextField afThresholdText= new JTextField(Double.toString(afThreshold));
	private JButton afThresholdHelp;
	private JButton chooseAFColumns;
	private JButton chooseAFColumnsHelp;
	private CheckBoxList chooser;
	private JSeparator statusSeparator= new JSeparator(SwingConstants.HORIZONTAL);
			
	private IncidentalHSQLServer server;
    
	
	public IncidentalPanel() {
		setupView();
		add(view);
		
		server= new IncidentalHSQLServer(INCIDENTAL_DB_USER, INCIDENTAL_DB_PASSWORD);
	}

	
	private void setupView() {
		view= ViewUtil.getClearPanel();
		view.setLayout(new BorderLayout());
		//view.setBorder(BorderFactory.createLineBorder(Color.RED));
		view.setBorder(BorderFactory.createEmptyBorder(TOP_MARGIN, SIDE_MARGIN, BOTTOM_MARGIN, SIDE_MARGIN));
		
		
		/* Set up the layout for the GUI. */
		workview= new RoundedPanel(10);
		GroupLayout layout= new GroupLayout(workview);
		workview.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
						
		
		topText= new JLabel("Patient selection:");
		choosePatientButton= new JButton("Choose Patient");
		analyzeButton= new JButton(analyzeButtonDefaultText);
		analyzeButton.setEnabled(false); // cannot click until valid DNA ID is selected
		
		Dimension d= new Dimension(TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT);
		coverageThresholdText.setMaximumSize(d);
		coverageThresholdText.setHorizontalAlignment(JTextField.RIGHT);
		coverageThresholdHelp= ViewUtil.getHelpButton("Coverage Threshold", 
				"Minimum number of sequence reads supporting the alternate allele.");
		hetRatioText.setMaximumSize(d);
		hetRatioText.setHorizontalAlignment(JTextField.RIGHT);
		hetRatioHelp= ViewUtil.getHelpButton("Alt/Total Ratio", 
				"In order for a variant to be included, it must exceeed this threshold, "
				+ "so as not to be excluded as an erroneous variant. "
				+ "Below this threshold, alternate alleles are not reported.");
		afThresholdText.setMaximumSize(d);
		afThresholdText.setHorizontalAlignment(JTextField.RIGHT);
		afThresholdHelp= ViewUtil.getHelpButton("Allele Frequency Threshold", 
				"The maximum allele frequency for this variant. In order for a "
				+ "variant to be reported, allele frequency must be below this "
				+ "threshold across all allele frequency databases.");
		
		
		statusSeparator.setVisible(false);
		
		progressLabel= new JLabel();
		progressLabel.setVisible(false);
		
		pw= new ProgressWheel();
		pw.setIndeterminate(true);
		pw.setVisible(false);
		
		chooser= new CheckBoxList(getDbColumnList());
		chooser.addCheckBoxListSelectedValues(new String[] 
			{"1000g2012apr_all, AnnotationFrequency",
			"esp6500_all, Score"});
		
		variantPane= new JScrollPane();
		
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
							analyzeButton.setEnabled(true);
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
		analyzeButton.addActionListener(
			new ActionListener() {
				
				@Override
				public void actionPerformed (ActionEvent e) {
					if (selectedIndividuals != null && selectedIndividuals.size() == 1
						&& !analysisRunning) {
						analysisRunning= true;
										
						MSWorker= new MedSavantWorker<Object> (
							IncidentalPanel.class.getCanonicalName()) {

							IncidentalFindings incFin;
								
							@Override
							protected Object doInBackground() throws Exception {
								/* Starts a new thread for background tasks. */
								
								statusSeparator.setVisible(true);
								progressLabel.setVisible(true);
								pw.setVisible(true);
								analyzeButton.setText("Cancel analysis");
								
								//if (!server.isRunning()) { // No need to run a local server if using JDBC driver from hsqldb
								if (!dbLoaded) {
									progressLabel.setText("Preparing local filtering database...");
									try {
										//server.startServer(); // No need to run a local server if using JDBC driver from hsqldb
										dbLoaded= true;
										IncidentalDB.populateDB(server.getURL(), INCIDENTAL_DB_USER, INCIDENTAL_DB_PASSWORD);
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
								
								progressLabel.setText("Downloading and filtering variants...");
								
								/* Get all the user settings and then get incidental findings. */
								setAllValuesFromFields();
								incFin= new IncidentalFindings(currentIndividualDNA, 
										coverageThreshold, hetRatio, afThreshold, 
										Arrays.asList(chooser.getCheckBoxListSelectedValues()));
								
								if (this.isCancelled()) {
									progressLabel.setText("Analysis Cancelled.");
									pw.setVisible(false);
									analyzeButton.setEnabled(true);
									analyzeButton.setText(analyzeButtonDefaultText);
								} else {
									progressLabel.setText(incFin.getVariantCount() + " variants. " +
											"Click on column to sort. Hold CTRL while clicking to sort by multiple columns.");
								}
								pw.setVisible(false);
								return null;
							}

							@Override
							protected void showSuccess(Object t) {	
							/* All updates to display should happen here to be run. */
								updateVariantPane(incFin);
								analyzeButton.setText(analyzeButtonDefaultText);
								analysisRunning= false;
							}
							
						};
						
						MSWorker.execute();
						
					} else if (selectedIndividuals != null && selectedIndividuals.size() == 1
						&& analysisRunning) {
						analysisRunning= false;
						MSWorker.cancel(true);
						analyzeButton.setEnabled(false);
						progressLabel.setText("Cancelling Analysis...");
					}
				}
			}
		);		
		
		
		chooseAFColumns= new JButton("Choose Allelle Frequency DBs");
		chooseAFColumnsHelp= ViewUtil.getHelpButton("Allele Frequency Database selector", 
				"Choose the databases to use when filtering for allele frequency.");
		chooseAFColumns.addActionListener(
			new ActionListener() {
				
				@Override
				public void actionPerformed (ActionEvent e) {
					JScrollPane chooserScrollPane= new JScrollPane(chooser);
					chooserScrollPane.setBorder(BorderFactory.createEmptyBorder(2,3,4,1)); // just a little bit of border
					chooserScrollPane.setBackground(Color.LIGHT_GRAY);
					
					JDialog f = new JDialog(MedSavantFrame.getInstance(),"Allele Frequency Database selector");
					f.add(chooserScrollPane);
					f.setPreferredSize(new Dimension(300, 500));
					f.setMinimumSize(new Dimension(300, 500));
					f.setLocationRelativeTo(null);
					f.setVisible(true);
				}
			}
		);
		
		
		/* Set up the layout for the UI.
		 * GroupLayout requires defintion of the same components from both 
		 * horizontal and verical perspectives. */
		layout.setAutoCreateGaps(true); // Not sure if this really makes a difference...
		
		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(topText)
						.addComponent(choosePatientButton)
						.addComponent(analyzeButton)
						)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addGroup(layout.createSequentialGroup()
							.addComponent(coverageThresholdLabel)
							.addComponent(coverageThresholdText)
							.addComponent(coverageThresholdHelp)
							)
						.addGroup(layout.createSequentialGroup()
							.addComponent(hetRatioLabel)
							.addComponent(hetRatioText)
							.addComponent(hetRatioHelp)
							)
						.addGroup(layout.createSequentialGroup()
							.addComponent(afThresholdLabel)
							.addComponent(afThresholdText)
							.addComponent(afThresholdHelp)
							)
						.addGroup(layout.createSequentialGroup()
							.addComponent(chooseAFColumns)
							.addComponent(chooseAFColumnsHelp)
							)
						)
					)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(statusSeparator)
					.addComponent(progressLabel)
					.addComponent(pw)
					)
				.addComponent(variantPane)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGroup(layout.createSequentialGroup()
						.addComponent(topText)
						.addComponent(choosePatientButton)
						.addComponent(analyzeButton)
						)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(coverageThresholdLabel)
							.addComponent(coverageThresholdText)
							.addComponent(coverageThresholdHelp)
							)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(hetRatioLabel)
							.addComponent(hetRatioText)
							.addComponent(hetRatioHelp)
							)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(afThresholdLabel)
							.addComponent(afThresholdText)
							.addComponent(afThresholdHelp)
							)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(chooseAFColumns)
							.addComponent(chooseAFColumnsHelp)
							)
						)
					)
				.addGroup(layout.createSequentialGroup()
					// special parameters for the separator when adding vertically
					.addComponent(statusSeparator, GroupLayout.PREFERRED_SIZE, 
						GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(progressLabel)
					.addComponent(pw)
					)
				.addComponent(variantPane)
		);
		
		/* Add the UI to the main app panel. */
		view.add(workview, BorderLayout.CENTER);
    }
	
		
	private void updateVariantPane (IncidentalFindings i) {
		JPanel jp= i.getTableOutput();
		variantPane.setViewportView(jp);
	}
	
	
	public JPanel getView() {
		return view;
	}
    
	
	/** Set all values from JTextFields. */
	private void setAllValuesFromFields() {
		coverageThreshold= Integer.parseInt(coverageThresholdText.getText());
		hetRatio= Double.parseDouble(hetRatioText.getText());
		afThreshold= Double.parseDouble(afThresholdText.getText());
	}
	
	
	
	/** Get the header for the table using the column aliases. */
	public Object[] getDbColumnList() {
		List<String> t= new ArrayList<String>();

		try {
			AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
			for (AnnotationFormat af : afs)
				for (CustomField field : af.getCustomFields())
					t.add(field.getAlias());
		} catch (Exception e) {
			LOG.error(e);
		}
	
		return t.toArray();
	}
    
}