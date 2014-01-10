package medsavant.discovery;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.swing.CheckBoxList;
import com.jidesoft.swing.JideButton;
import java.util.Calendar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import javax.swing.BorderFactory;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import medsavant.discovery.localDB.DiscoveryDB;
import medsavant.discovery.localDB.DiscoveryHSQLServer;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.SplitScreenPanel;
import org.ut.biolab.medsavant.client.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.client.view.genetics.charts.Ring;
import org.ut.biolab.medsavant.client.view.genetics.charts.RingChart;
import org.ut.biolab.medsavant.client.view.genetics.inspector.ComprehensiveInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SimpleVariant;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;


/**
 * Default panel view for Incidentalome app
 * 
 * @author rammar
 */
public class DiscoveryPanel extends JPanel {
	private static final Log LOG = LogFactory.getLog(MedSavantClient.class);
	private static final Properties properties= new Properties();
	private static final String PROPERTIES_FILENAME= DirectorySettings.getMedSavantDirectory().getPath() +
				File.separator + "cache" + File.separator + "discovery_app_settings.xml";
	private static final int DEFAULT_FETCH_LIMIT= 2000;
	private static final String DEFAULT_CGD_URL= "http://research.nhgri.nih.gov/CGD/download/txt/CGD.txt.gz";
	private static final String DEFAULT_CGD_FILENAME= "CGD.txt";
	private static final int DEFAULT_COVERAGE_THRESHOLD= 10;
	private static final double	DEFAULT_HET_RATIO= 0.3;
	private static final double DEFAULT_AF_THRESHOLD= 0.05;
	private static final String[] DEFAULT_AF_DB_LIST= new String[] {
		"1000g2012apr_all, AnnotationFrequency", "esp6500_all, Score"};
	public static final String PAGE_NAME = "Incidentalome";
	private static final String INCIDENTAL_DB_USER= "incidental_user";
	private static final String INCIDENTAL_DB_PASSWORD= "$hazam!2734"; // random password		
	private static final List<String> JANNOVAR_MUTATIONS= Arrays.asList(
		"NONSYNONYMOUS", "SYNONYMOUS", "STOPGAIN", "STOPLOSS", "FS_INSERTION", 
		"FS_DELETION", "FS_SUBSTITUTION", "NON_FS_INSERTION", "NON_FS_DELETION",
		"NON_FS_SUBSTITUTION", "SPLICING", "UTR3", "UTR5", "INTRONIC", 
		"ncRNA_INTRONIC","ncRNA_EXONIC", "ncRNA_SPLICING", "UPSTREAM", 
		"DOWNSTREAM", "INTERGENIC", "ERROR");
	private static final String EXIST_KEYWORD= "Exists";
	private static final String EQUALS_KEYWORD= "=";
	private static final String LESS_KEYWORD= "<";
	private static final String GREATER_KEYWORD= ">";
	private static final String LIKE_KEYWORD= "Like";
	private static final List<String> OPERATOR_OPTIONS= Arrays.asList(
		EXIST_KEYWORD, EQUALS_KEYWORD, LESS_KEYWORD, GREATER_KEYWORD, LIKE_KEYWORD);
	private static final String GENE_COLUMN_KEYWORD= "jannovar gene symbol"; // may change later
	private static final int TIMEOUT_CONNECTION= 10000; // 10 seconds (10000 milliseconds)
	private static final int TIMEOUT_DATA_READ= 15000; // 15 seconds (15000 milliseconds)	
	private static final String ALL_GENE_PANEL= DiscoveryFindings.ALL_GENE_PANEL;
	
	private final int TOP_MARGIN= 0;
	private final int SIDE_MARGIN= 5;
	private final int BOTTOM_MARGIN= 5;
	private final int TEXT_AREA_WIDTH= 70;
	private final int TEXT_AREA_HEIGHT= 25;
	private final int PANE_WIDTH= 380;
	private final int PANE_WIDTH_OFFSET= 20;
	private final int PANE_HEIGHT= 20; // minimum, but it'll stretch down - may need to change later
	private final int RING_DIAMETER= 200;
	
	private DiscoveryFindings discFind= null;
	private int variantFetchLimit;
	private ComboCondition baseComboCondition;
	private ComboCondition newComboCondition;
	private List<FilterDetails> conditionList= new LinkedList<FilterDetails>();
	private int coverageThreshold;
	private double hetRatio;
	private double afThreshold;
	private String[] chooserAFArray;
	private String incidentalPanelString;
	private List<String> mutationFilterList= new LinkedList<String>();
	private List<String> genePanelList= Arrays.asList(ALL_GENE_PANEL, "ACMG", "CGD");
	private String currentGenePanel;
	
	private boolean analysisRunning= false;
	private DiscoveryHSQLServer server;
	private boolean dbLoaded= false;
	private Date currentDate= null;
	
	private JPanel view;
	private RoundedPanel workview;
	private JideButton choosePatientButton;
	private JideButton analyzeButton;
	private String analyzeButtonDefaultText= "Refresh";
	private IndividualSelector customSelector;
	private Set<String> selectedIndividuals;
	private String currentIndividual;
	private String currentIndividualDNA;
	private Calendar date;
	private MedSavantWorker MSWorker;
	private SearchableTablePanel stp;
	private JScrollPane variantPane;
	private ProgressWheel pw;
	private JLabel progressLabel;
	private final int preferredNumColumns= 10;
	private JLabel coverageThresholdLabel= new JLabel("Min. variant coverage");
	private JTextField coverageThresholdText;
	private JButton coverageThresholdHelp;
	private JLabel hetRatioLabel= new JLabel("Min. ratio of alternate/total reads");
	private JTextField hetRatioText;
	private JButton hetRatioHelp;
	private JLabel afThresholdLabel= new JLabel("Max. allele frequency");
	private JTextField afThresholdText;
	private JButton afThresholdHelp;
	private JButton chooseAFColumns;
	private JButton chooseAFColumnsHelp;
	private CheckBoxList chooser;
	//private JSeparator statusSeparator= new JSeparator(SwingConstants.HORIZONTAL);
	private URL cgdURL;
	private CollapsiblePane collapsible;
	private CollapsiblePane collapsibleSettings;
	private JLabel cgdURLLabel= new JLabel("Clinical Genomics Database (CGD) URL");
	private JTextField cgdText;
	private JButton cgdHelp;
	private JLabel cgdDateLabel;
	private SplitScreenPanel ssp;
	private ComprehensiveInspector vip;
	private JButton addFilterButton;
	private JPanel patientPanel;
	private JLabel fetchLimitLabel;
	private JTextField fetchLimitText;
	private JButton fetchLimitHelp;
	private JPanel progressPanel;
	private RingChart ringChart;
	private Ring ring;
    
	
	public DiscoveryPanel() {
		/* Set up the properties based on stored user preference. */
		try {
			loadProperties();
		} catch (Exception e) {
			System.err.println("Error loading properties.");
			e.printStackTrace();
		}
		
		setupView();
		this.setLayout(new BorderLayout());
		add(view, BorderLayout.CENTER);
		
		server= new DiscoveryHSQLServer(INCIDENTAL_DB_USER, INCIDENTAL_DB_PASSWORD);
	}

	
	public JPanel getView() {
		return view;
	}
	
	
	/**
	 * Inner class: From the filter panes, stores details that are required for
	 * building ComboConditions at runtime.
	 */
	private class FilterDetails {
		private String variantProperty;
		private String operator;
		private JTextField jtf;
		
		private FilterDetails() {
		}
		
		/**
		 * Set the FilterDetails fields.
		 * @param variantProperty The name of the variant property used for filtering
		 * @param operator The logical operator applied
		 * @param jtf the JTextField where the condition is being specified
		 */
		private void setDetails(String variantProperty, String operator, JTextField jtf) {
			this.variantProperty= variantProperty;
			this.operator= operator;
			this.jtf= jtf;			
		}
		
		/**
		 * Get the current Condition object based on this filter.
		 */
		private Condition getCurrentCondition() {
			Condition c= null;
			Map<String, String> columns= discFind.dbAliasToColumn;
		
			if (columns.get(variantProperty) != null) {
				if (operator.equals(DiscoveryPanel.LIKE_KEYWORD)) {
					/* Special case: For the LIKE operator, a "%" wildcard is 
					 * appended to the end of the text from the text field. */
					c= BinaryCondition.like(discFind.ts.getDBColumn(columns.get(variantProperty)), jtf.getText() + "%");
				} else if (operator.equals(DiscoveryPanel.EQUALS_KEYWORD)) {
					c= BinaryCondition.equalTo(discFind.ts.getDBColumn(columns.get(variantProperty)), jtf.getText());
				} else if (operator.equals(DiscoveryPanel.LESS_KEYWORD)) {
					c= BinaryCondition.lessThan(discFind.ts.getDBColumn(columns.get(variantProperty)), jtf.getText(), false);
				} else if (operator.equals(DiscoveryPanel.GREATER_KEYWORD)) {
					c= BinaryCondition.greaterThan(discFind.ts.getDBColumn(columns.get(variantProperty)), jtf.getText(), false);
				} else if (operator.equals(DiscoveryPanel.EXIST_KEYWORD)) {
					c= UnaryCondition.isNotNull(discFind.ts.getDBColumn(columns.get(variantProperty)));
				}	
			}
			
			return c;
		}
	}
	
	
	/**
	 * Set up the DiscoveryPanel.
	 */
	private void setupView() {
		view= ViewUtil.getClearPanel();
		view.setLayout(new BorderLayout());
		//view.setBorder(BorderFactory.createLineBorder(Color.RED));
		view.setBorder(BorderFactory.createEmptyBorder(TOP_MARGIN, SIDE_MARGIN, BOTTOM_MARGIN, SIDE_MARGIN));
		
		choosePatientButton= new JideButton("Choose Patient");
		choosePatientButton.setButtonStyle(JideButton.TOOLBOX_STYLE);
		choosePatientButton.setFont(new Font(choosePatientButton.getFont().getName(),
			Font.PLAIN, 18));
		
				
		analyzeButton= new JideButton(analyzeButtonDefaultText);
		analyzeButton.setButtonStyle(JideButton.TOOLBOX_STYLE);
		analyzeButton.setFont(new Font(analyzeButton.getFont().getName(),
			Font.BOLD, 14));
		analyzeButton.setEnabled(false); // cannot click until valid DNA ID is selected
		analyzeButton.setVisible(false);
			
		
		Dimension textFieldDimension= new Dimension(TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT);
		coverageThresholdText= new JTextField(Integer.toString(coverageThreshold));
		coverageThresholdText.setMinimumSize(textFieldDimension);
		coverageThresholdText.setHorizontalAlignment(JTextField.RIGHT);
		coverageThresholdHelp= ViewUtil.getHelpButton("Coverage Threshold", 
				"Minimum number of sequence reads supporting the alternate allele.");
		hetRatioText= new JTextField(Double.toString(hetRatio));
		hetRatioText.setMinimumSize(textFieldDimension);
		hetRatioText.setHorizontalAlignment(JTextField.RIGHT);
		hetRatioHelp= ViewUtil.getHelpButton("Alt/Total Ratio", 
				"In order for a variant to be included, it must exceeed this threshold, "
				+ "so as not to be excluded as an erroneous variant. "
				+ "Below this threshold, alternate alleles are not reported.");
		afThresholdText= new JTextField(Double.toString(afThreshold));
		afThresholdText.setMinimumSize(textFieldDimension);
		afThresholdText.setHorizontalAlignment(JTextField.RIGHT);
		afThresholdHelp= ViewUtil.getHelpButton("Allele Frequency Threshold", 
				"The maximum allele frequency for this variant. In order for a "
				+ "variant to be reported, allele frequency must be below this "
				+ "threshold across all allele frequency databases.");
		
		fetchLimitLabel= new JLabel("Variant fetch limit");
		fetchLimitText= new JTextField(Integer.toString(variantFetchLimit));
		fetchLimitText.setMinimumSize(textFieldDimension);
		fetchLimitText.setHorizontalAlignment(JTextField.RIGHT);
		fetchLimitHelp= ViewUtil.getHelpButton("Variant fetch limit", 
				"The maximum number of records to retrieve from the server " +
				"for this individual. Increasing the number makes data " +
				"retrieval take longer.");
		
		cgdText= new JTextField(cgdURL.toString());
		cgdHelp= ViewUtil.getHelpButton("Clinical Genomics Database", 
				"URL for automatic updates of CGD database");
		cgdDateLabel= new JLabel("CGD last updated on " +
			(new SimpleDateFormat("MMM dd, yyyy")).format(currentDate) + ".");
		cgdDateLabel.setFont(new Font(cgdDateLabel.getFont().getName(),
			Font.BOLD, cgdDateLabel.getFont().getSize()));
		cgdText.addKeyListener(
			new KeyListener() {
				
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						try {
							setAllValuesFromFields();
							updateCGD();
							copyCGD();
						} catch (Exception exc) {
							exc.printStackTrace();
						}
					}
				}
				
				@Override
				public void keyReleased(KeyEvent e) {}
				
				@Override
				public void keyTyped(KeyEvent e) {}
			}
		);
		
		ringChart= new RingChart();
		ringChart.setPreferredSize(new Dimension(RING_DIAMETER, RING_DIAMETER));
		
		progressLabel= new JLabel();
		progressLabel.setFont(new Font(progressLabel.getFont().getName(),
			Font.PLAIN, 15));
		progressLabel.setVisible(false);
		
		pw= new ProgressWheel();
		pw.setIndeterminate(true);
		pw.setVisible(false);
		
		chooser= new CheckBoxList(getDbColumnList());
		chooser.addCheckBoxListSelectedValues(chooserAFArray);
		
		variantPane= new JScrollPane();
		
		/* Choose the patient's sample using the individual selector. */
		customSelector= new IndividualSelector(true);
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
							analyzeButton.doClick(); // trigger button's actionPerformed() even though it's not visible
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
					if (selectedIndividuals != null && selectedIndividuals.size() == 1 && !analysisRunning) {
						analysisRunning= true;					
						MSWorker= new MedSavantWorker<Object> (
							DiscoveryPanel.class.getCanonicalName()) {
								
							@Override
							protected Object doInBackground() throws Exception {
								/* Starts a new thread for background tasks. */
								progressLabel.setVisible(true);
								pw.setVisible(true);
								analyzeButton.setText("Cancel analysis");
								analyzeButton.setVisible(true);
								progressPanel.remove(ringChart); // if it's currently added
								
								if (!dbLoaded) {
									progressLabel.setText("Preparing local filtering database");
									try {
										dbLoaded= true;
										DiscoveryDB.populateDB(server.getURL(), INCIDENTAL_DB_USER, INCIDENTAL_DB_PASSWORD, properties);
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
								
								progressLabel.setText("Downloading and filtering variants");
								
								/* Get all the user settings. */
								setAllValuesFromFields();
								
								/* Every time an analysis is run, parameters/settings are saved. */
								saveProperties();
								
								/*  Get discovery findings. */
								// Initialize or update for current DNA ID
								if (discFind == null || !currentIndividualDNA.equals(discFind.dnaID)) {
									discFind= new DiscoveryFindings(currentIndividualDNA);
								}
								baseComboCondition= discFind.getComboCondition(
									Arrays.asList(chooser.getCheckBoxListSelectedValues()),
									coverageThreshold, hetRatio, afThreshold);
								updateCondition();
								discFind.setGenePanel(currentGenePanel);
								discFind.storeVariants(variantFetchLimit);
								
								/* Update progress messages to user. */
								if (this.isCancelled()) {
									progressLabel.setText("Analysis Cancelled.");
									pw.setVisible(false);
									analyzeButton.setEnabled(true);
									analyzeButton.setText(analyzeButtonDefaultText);
								} else {
									int total= discFind.getMaximumVariantCount();
									int filtered= discFind.getFilteredVariantCount();
									
									ring= new Ring();
									ring.addItem("Pass all filters", filtered, Color.RED);
									ring.addItem("Don't pass filters", total - filtered, Color.LIGHT_GRAY);				
									ringChart.setRings(Arrays.asList(ring));
									progressPanel.add(ringChart, "wrap", 0);
									
									progressLabel.setText(total + " total variants, "
										+ filtered + " variants after filtering");
								}
								pw.setVisible(false);
								return null;
							}

							@Override
							protected void showSuccess(Object t) {	
							/* All updates to display should happen here to be run. */
								updateVariantPane();
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
						progressLabel.setText("Cancelling Analysis");
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
		
		addFilterButton= new JButton("Add variant filter");
		addFilterButton.setFocusPainted(false);
		addFilterButton.addActionListener(
			new ActionListener() {
				
				@Override
				public void actionPerformed (ActionEvent e) {
					JPopupMenu popupMenu= new JPopupMenu();
					JMenu filterMenu= new JMenu(addFilterButton.getText());
					
					for (Object columnName : getDbColumnList()) {
						final JMenuItem filter= new JMenuItem((String) columnName);
						filter.addMouseListener(
							new MouseListener() {
								@Override
								public void mousePressed(MouseEvent me) {
									patientPanel.add(addFilterPanel(filter.getText()), "wrap", 2);
								}
								// remaining methods included but do nothing
								@Override public void mouseExited(MouseEvent me) {}
								@Override public void mouseReleased(MouseEvent me) {}
								@Override public void mouseClicked(MouseEvent me) {}
								@Override public void mouseEntered(MouseEvent me) {}
							}
						);
						filterMenu.add(filter);
					}
					
					popupMenu.add(filterMenu);
					popupMenu.show(addFilterButton, 0, 0);
				}
			}
		);
		
		
		/* Set up the layout for the UI.
		 * GroupLayout requires defintion of the same components from both 
		 * horizontal and verical perspectives. */
		
		/* Set up the layout for the analysis options collapsible panel. */
		collapsible= new CollapsiblePane("Sequence coverage and allele frequency");
		collapsible.setLayout(new MigLayout());
		collapsible.add(coverageThresholdLabel);
		collapsible.add(coverageThresholdText);
		collapsible.add(coverageThresholdHelp, "wrap");
		collapsible.add(hetRatioLabel);
		collapsible.add(hetRatioText);
		collapsible.add(hetRatioHelp, "wrap");
		collapsible.add(afThresholdLabel);
		collapsible.add(afThresholdText);
		collapsible.add(afThresholdHelp, "wrap");
		collapsible.add(chooseAFColumns);
		collapsible.add(chooseAFColumnsHelp);
		collapsible.setStyle(CollapsiblePane.PLAIN_STYLE);
		collapsible.setFocusPainted(false);
		collapsible.collapse(true);	
		
		/* Set up the layout for the Advanced settings collapsible panel. */
		collapsibleSettings= new CollapsiblePane("Advanced Settings");
		collapsibleSettings.setLayout(new MigLayout());
		//collapsibleSettings.add(fetchLimitLabel, "split"); // split this cell, makes the panel less wide (see MigLayout Quickstart on splitting)
		//collapsibleSettings.add(fetchLimitText);
		//collapsibleSettings.add(fetchLimitHelp, "wrap");
		collapsibleSettings.add(cgdURLLabel); // split this cell, makes the panel less wide (see MigLayout Quickstart on splitting)
		collapsibleSettings.add(cgdHelp, "wrap");
		collapsibleSettings.add(cgdText, "span");
		collapsibleSettings.add(cgdDateLabel);		
		collapsibleSettings.setStyle(CollapsiblePane.PLAIN_STYLE);
		collapsibleSettings.setFocusPainted(false);
		collapsibleSettings.collapse(true);
		
		/* Progress bar panel. */
		//JPanel progressPanel= new JPanel(new MigLayout("insets 0", "center", "center")); // Remove borders around the panel using "insets"
		progressPanel= new JPanel(new MigLayout("", "center", ""));
		progressPanel.add(ringChart, "wrap");
		progressPanel.add(progressLabel, "gapy 25, wrap");
		progressPanel.add(pw);
		
		/* Patient selection panel. */
		patientPanel= new JPanel();
		patientPanel.setLayout(new MigLayout("insets 0px, gapy 10px"));
		patientPanel.add(choosePatientButton, "alignx center, wrap");
		patientPanel.add(addFilterButton, "alignx center, wrap");
		patientPanel.add(collapsible, "wrap");
		patientPanel.add(geneSelectionPanel(), "wrap");
		patientPanel.add(mutationCheckboxPanel(), "wrap");
		patientPanel.add(collapsibleSettings, "wrap");
		patientPanel.add(progressPanel, "alignx center, wrap");
		patientPanel.add(analyzeButton, "alignx center");
		// Put the patient panel in a JScrollPane
		JScrollPane patientJSP= new JScrollPane(patientPanel);
		patientJSP.setMinimumSize(new Dimension(patientPanel.getMinimumSize().width + PANE_WIDTH_OFFSET, PANE_HEIGHT));
		patientJSP.setPreferredSize(new Dimension(patientPanel.getMinimumSize().width, patientPanel.getMaximumSize().height));
		patientJSP.setBorder(BorderFactory.createEmptyBorder());
		patientJSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		/* Set up the gene and variant inspectors. */
		ssp = new SplitScreenPanel(variantPane);
        vip = new ComprehensiveInspector();
		vip.addClinvarSubInspector();
		vip.addHGMDSubInspector();
		vip.addOtherIndividualsVariantSubInspector(ssp);
		vip.addSocialSubInspector();
		
		/* Final window layout along with size preferences. */
		workview= new RoundedPanel(10);
		
		workview.setLayout(new MigLayout("", "", "top"));
		workview.add(patientJSP);
		workview.add(ssp);
		workview.add(vip);
		
		/* Set the sizing for a couple panels and let the other panels auto-size. */
		choosePatientButton.setMinimumSize(new Dimension(
			250, choosePatientButton.getHeight()));
		analyzeButton.setMinimumSize(new Dimension(
			200, analyzeButton.getSize().height));
		collapsible.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 0));
		collapsibleSettings.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 0));
		collapsibleSettings.setMaximumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 
			collapsibleSettings.getMaximumSize().height)); // also set the max size for this Pane
		patientPanel.setMinimumSize(new Dimension(PANE_WIDTH, PANE_HEIGHT));
		
		//variantPane.setMinimumSize(new Dimension(variantPane.));
		variantPane.setPreferredSize(variantPane.getMaximumSize()); // Needs changing - try MigLayout features
		
		vip.setMinimumSize(new Dimension(ComprehensiveInspector.INSPECTOR_WIDTH, 700)); // NEEDS TO BE UPDATED
		vip.addSelectionListener(new Listener<Object>() {
			@Override
			public void handleEvent(Object event) {
				stp.getTable().clearSelection();
			}
        });
		
		/* Add the UI to the main app panel. */
		view.add(workview, BorderLayout.CENTER);
    }
	
	
	/**
	 * Update the variantPane with the set of variants.
	 */
	private void updateVariantPane() {
		if (properties.getProperty("sortable_table_panel_columns") == null) {
			stp= discFind.getTableOutput(null);
		} else {
			stp= discFind.getTableOutput(
				getIntArrayFromString(properties.getProperty("sortable_table_panel_columns")));
		}
		stp.getColumnChooser().setProperties(properties, PROPERTIES_FILENAME);
		variantPane.setViewportView(stp);
		
		stp.scrollSafeSelectAction(new Runnable() {
            @Override
            public void run() {
                if (stp.getTable().getSelectedRow() != -1) {
					SortableTable st= stp.getTable();
                    int selectedIndex= st.getSelectedRow();
					String chr= (String) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_CHROM);
					long pos= ((Integer) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_POSITION)).longValue();
					String ref= (String) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_REF);
					String alt= (String) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_ALT);
					String type= (String) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_VARIANT_TYPE);
					
                    SimpleVariant v= new SimpleVariant(chr, pos, ref, alt, type);
                    vip.setSimpleVariant(v);
					
					/* Create custom SubInspectors. */
					Object[] line= new Object[discFind.header.size()];
					for (int index= 0; index != discFind.header.size(); ++index)
						line[index]= st.getModel().getValueAt(selectedIndex, index);
					vip.setVariantLine(line, discFind.header);
               }
            }
        });
	}
	
	
	/**
	 * Create a CollapsiblePane for a custom filter
	 * @param name The name of this filter property/panel
	 * @return A filter panel
	 */
	private JPanel addFilterPanel(final String name) {
		final JPanel j= new JPanel();
		j.setLayout(new MigLayout("insets 0px"));
		
		// CollapsiblePane for the filter
		final CollapsiblePane collapsible= new CollapsiblePane(name);
		collapsible.setLayout(new MigLayout("align center"));
		collapsible.setStyle(CollapsiblePane.PLAIN_STYLE);
		collapsible.setFocusPainted(false);
		
		// The operator selection button
		final JButton operatorButton= new JButton(EXIST_KEYWORD); // defaults to Exists
		final JTextField operatorText= new JTextField(10); // 10 character spaces wide
		
		// Add this FilterDetails object to the list of conditions
		final FilterDetails filterPanelDetails= new FilterDetails();
		filterPanelDetails.setDetails(name, operatorButton.getText(), operatorText); // initialize for the default operator
		conditionList.add(filterPanelDetails);
		
		// The operator selection button's ActionListener
		operatorButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					JPopupMenu jpm= new JPopupMenu();
					JMenu jm= new JMenu(operatorButton.getText());
					for (final String op : OPERATOR_OPTIONS) {
						JMenuItem jmi= new JMenuItem(op);
						jmi.addMouseListener(
							new MouseListener() {
								@Override
								public void mousePressed(MouseEvent me) {
									operatorButton.setText(op);
									if (op.equals(EXIST_KEYWORD))
										collapsible.remove(operatorText);
									else
										collapsible.add(operatorText);
									
									// update fields for the FilterDetails object
									filterPanelDetails.setDetails(name, op, operatorText);
								}
								// remaining methods included but do nothing
								@Override public void mouseExited(MouseEvent me) {}
								@Override public void mouseReleased(MouseEvent me) {}
								@Override public void mouseClicked(MouseEvent me) {}
								@Override public void mouseEntered(MouseEvent me) {}
							}
						);
						
						jm.add(jmi);
					}
					
					jpm.add(jm);
					jpm.show(operatorButton, 0, 0);
				}
			}
		);
		
		collapsible.add(operatorButton);
		
		collapsible.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET - 30, 0)); // 30px extra to accomodate the - button
		
		// Button to remove this filter panel
		JLabel removeButton= ViewUtil.createIconButton(IconFactory.getInstance().getIcon(
			IconFactory.StandardIcon.REMOVE_ON_TOOLBAR));
		removeButton.addMouseListener(
			new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent me) {
					conditionList.remove(filterPanelDetails);
					patientPanel.remove(j); // remove the entire panel when pressed
					patientPanel.updateUI(); // Causes the panel to refresh immediately - was delaying without this and looked sloppy
				}
				// remaining methods included but do nothing
				@Override public void mouseExited(MouseEvent me) {}
				@Override public void mouseReleased(MouseEvent me) {}
				@Override public void mousePressed(MouseEvent me) {}
				@Override public void mouseEntered(MouseEvent me) {}
			}
		);
		
		// Add both components to the panel
		j.add(collapsible);
		j.add(removeButton);
		
		return j;
	}
	
	
	/**
	 * Create a CollapsiblePane of a checkbox panel of mutations
	 * @return A mutation checkbox CollapsiblePane
	 */
	private CollapsiblePane mutationCheckboxPanel() {
		CollapsiblePane collapsibleMutation= new CollapsiblePane("Mutations");
		collapsibleMutation.setLayout(new MigLayout("gapy 0px"));
		
		for (String jm : JANNOVAR_MUTATIONS) {
			final JCheckBox currentCheckBox= new JCheckBox(jm);
			//currentCheckBox.setSelected(true);
			// Allow checkboxes to register themselves as checked or unchecked upon being clicked
			currentCheckBox.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (currentCheckBox.isSelected()) {
							mutationFilterList.add(currentCheckBox.getText());
						} else {
							mutationFilterList.remove(currentCheckBox.getText());
						}
					}
				}
			);
			
			collapsibleMutation.add(currentCheckBox, "wrap");
		}
		
		collapsibleMutation.setStyle(CollapsiblePane.PLAIN_STYLE);
		collapsibleMutation.setFocusPainted(false);
		collapsibleMutation.collapse(true);	
		
		collapsibleMutation.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 0));
		
		return collapsibleMutation;
	}
	
	
	/**
	 * Create a CollapsiblePane for gene or gene panel selection
	 * @return A gene/gene panel selection CollapsiblePane
	 */
	private CollapsiblePane geneSelectionPanel() {
		final CollapsiblePane collapsibleGene= new CollapsiblePane("Genes");
		collapsibleGene.setLayout(new MigLayout("gapy 0px, align center"));
		
		final String GENE_TEXT= "Gene";
		final String GENE_PANEL_TEXT= "Gene panel";
		final String triangleString= " ▾"; // I added the triangles to imply this is a popupmenu button
		
		final JButton geneButton= new JButton(GENE_PANEL_TEXT + triangleString);
		final JTextField geneTextField= new JTextField(10); // 10 character spaces wide
		final JComboBox genePanelComboBox= new JComboBox(genePanelList.toArray());
		genePanelComboBox.setSelectedItem(ALL_GENE_PANEL); // may be set later from properties - remove this
		currentGenePanel= (String) genePanelComboBox.getSelectedItem();
		
		// Add this FilterDetails object to the list of conditions
		final FilterDetails filterPanelDetails= new FilterDetails();
		filterPanelDetails.setDetails(GENE_COLUMN_KEYWORD, LIKE_KEYWORD, geneTextField);
		
		// Detect changes to the panel
		genePanelComboBox.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					currentGenePanel= (String) genePanelComboBox.getSelectedItem();
				}
			}
		);
		
		// UI response to changing the gene or gene panel search option
		geneButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					JPopupMenu jpm= new JPopupMenu();
					// copy over the button text without the triangle
					String jmText= geneButton.getText().substring(0, geneButton.getText().length() - 1 - triangleString.length());
					JMenu jm= new JMenu(jmText);
					for (final String s : (new String[] {GENE_PANEL_TEXT, GENE_TEXT})) {
						JMenuItem jmi= new JMenuItem(s);
						jmi.addMouseListener(
							new MouseListener() {
								@Override
								public void mousePressed(MouseEvent me) {
									geneButton.setText(s + triangleString);
									if (s.equals(GENE_TEXT)) {
										collapsibleGene.remove(genePanelComboBox);
										collapsibleGene.add(geneTextField);
										conditionList.add(filterPanelDetails);
										currentGenePanel= ALL_GENE_PANEL;
									} else if (s.equals(GENE_PANEL_TEXT)) {
										collapsibleGene.remove(geneTextField);
										collapsibleGene.add(genePanelComboBox);
										conditionList.remove(filterPanelDetails);
									}
								}
								// remaining methods included but do nothing
								@Override public void mouseExited(MouseEvent me) {}
								@Override public void mouseReleased(MouseEvent me) {}
								@Override public void mouseClicked(MouseEvent me) {}
								@Override public void mouseEntered(MouseEvent me) {}
							}
						);
						
						jm.add(jmi);
					}
					
					jpm.add(jm);
					jpm.show(geneButton, 0, 0);
				}
			}
		);
		
		collapsibleGene.add(geneButton);
		collapsibleGene.add(genePanelComboBox);
		
		collapsibleGene.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 0));		
		collapsibleGene.setStyle(CollapsiblePane.PLAIN_STYLE);
		collapsibleGene.setFocusPainted(false);
		collapsibleGene.collapse(true);
		
		return collapsibleGene;
	}
	
	/**
	 * Update and set the new ComboCondition based on user selections and the
	 * base ComboCondition returned from the DiscoveryFindings object.
	 */
	private void updateCondition() {
		newComboCondition= new ComboCondition(ComboCondition.Op.AND);
		
		// Start from the original base ComboCondition
		newComboCondition.addCondition(baseComboCondition);
		
		// Iterate through the filters and add those conditions to the new ComboCondition
		for (FilterDetails fd : conditionList) {
			newComboCondition.addCondition(fd.getCurrentCondition());
		}
		
		// Add the mutations to the new ComboCondition
		addMutationCondition(mutationFilterList);
		
		discFind.setComboCondition(newComboCondition);
	}
	
    
	/**
	 * Add a filter condition describing mutations to the ComboCondition of DiscoveryFindings.
	 * @param mutations A list of the mutation Strings, as annotated in Jannovar, to filter the variants
	 */
	private void addMutationCondition(List<String> mutations) {
		Map<String, String> columns= discFind.dbAliasToColumn;
		String JANNOVAR_EFFECT= "jannovar effect";
		
		if (columns.get(JANNOVAR_EFFECT) != null) {
			ComboCondition mutationComboCondition= new ComboCondition(ComboCondition.Op.OR);
			
			for (String m : mutations) {
				mutationComboCondition.addCondition(
					BinaryCondition.like(discFind.ts.getDBColumn(columns.get(JANNOVAR_EFFECT)), m));
			}
			
			newComboCondition.addCondition(mutationComboCondition);
		}
	}
	
	
	/** Set all values from JTextFields. Also set the relevant properties. */
	private void setAllValuesFromFields() throws MalformedURLException {
		coverageThreshold= Integer.parseInt(coverageThresholdText.getText());
		hetRatio= Double.parseDouble(hetRatioText.getText());
		afThreshold= Double.parseDouble(afThresholdText.getText());
		variantFetchLimit= Integer.parseInt(fetchLimitText.getText());
		cgdURL= new URL(cgdText.getText());
		
		/* Set the properties. */
		properties.setProperty("coverage_threshold", Integer.toString(coverageThreshold));
		properties.setProperty("het_ratio", Double.toString(hetRatio));
		properties.setProperty("af_threshold", Double.toString(afThreshold));
		properties.setProperty("CGD_DB_URL", cgdText.getText());
		properties.setProperty("variant_fetch_limit", fetchLimitText.getText());
		
		// quote-enclosed, comma-delimited list as string
		String afChooserStringList= "\"" + StringUtils.join(Arrays.asList(
			chooser.getCheckBoxListSelectedValues()), "\"\t\"") + "\"";
		properties.setProperty("af_chooser_list", afChooserStringList);
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

	
	/**
	 * Load the properties file if it exists.
	 */
	private void loadProperties() throws Exception {
		File propertiesFile= new File(PROPERTIES_FILENAME);
		if (!propertiesFile.exists()) {
			/* Set the defaults. */
			long defaultDate= (new GregorianCalendar(2013, Calendar.NOVEMBER, 
				27)).getTimeInMillis(); // CGD date at time of coding corresponding to the download date of the embedded CGD file
			
			properties.setProperty("CGD_DB_date", Long.toString(defaultDate));
			properties.setProperty("CGD_DB_URL", DEFAULT_CGD_URL.toString());
			properties.setProperty("CGD_DB_filename", DEFAULT_CGD_FILENAME);
			
			properties.setProperty("variant_fetch_limit", Integer.toString(DEFAULT_FETCH_LIMIT));
			properties.setProperty("coverage_threshold", Integer.toString(DEFAULT_COVERAGE_THRESHOLD));
			properties.setProperty("het_ratio", Double.toString(DEFAULT_HET_RATIO));
			properties.setProperty("af_threshold", Double.toString(DEFAULT_AF_THRESHOLD));
			
			String afChooserStringList= "\"" + StringUtils.join(Arrays.asList(DEFAULT_AF_DB_LIST), "\"\t\"") + "\"";
			properties.setProperty("af_chooser_list", afChooserStringList);
						
			saveProperties();
		} else {
			properties.loadFromXML(new FileInputStream(propertiesFile));
		}
		
		/* Set the parameters from properties. */
		cgdURL= new URL(properties.getProperty("CGD_DB_URL"));
		variantFetchLimit= Integer.parseInt(properties.getProperty("variant_fetch_limit"));
		coverageThreshold= Integer.parseInt(properties.getProperty("coverage_threshold"));
		hetRatio= Double.parseDouble(properties.getProperty("het_ratio"));
		afThreshold= Double.parseDouble(properties.getProperty("af_threshold"));

		String s= properties.getProperty("af_chooser_list");
		chooserAFArray= (s.substring(1, s.length() - 1)).split("\"\t\"");
		
		// Update CGD file if necessary
		updateCGD();
		copyCGD();		
	}

	
	/** 
	 * Save the current set of properties to the properties XML file.
	 */
	private void saveProperties() {
		try {
			properties.storeToXML(new FileOutputStream(PROPERTIES_FILENAME), 
				"Configuration options for incidentalome app");
		} catch (Exception e) {
			System.err.println("[IncidentalPanel]: Error saving properties XML file.");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Update the CGD database if a new one exists at the specified URL.
	 */
	private void updateCGD() {
		try {
			HttpURLConnection conn= (HttpURLConnection) cgdURL.openConnection();
			Date urlDate= new Date(conn.getLastModified());
			currentDate= new Date(Long.parseLong((String) properties.getProperty("CGD_DB_date")));

			if (currentDate.before(urlDate)) {
				// notify users
				DateFormat dateFormat= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				System.out.println("[Discovery app]: Existing CGD version from " + 
					dateFormat.format(currentDate) + " to be replaced by newer CGD version from " +
					dateFormat.format(urlDate));

				// download file to cache, uncompress, removed compressed file, set new properties
				File cgdFile= new File(DirectorySettings.getMedSavantDirectory().getPath() +
					File.separator + "cache" + File.separator + 
					FilenameUtils.getName(cgdURL.getFile()));	
				FileUtils.copyURLToFile(cgdURL, cgdFile, TIMEOUT_CONNECTION, TIMEOUT_DATA_READ);
				File newCgdFile= gunzip(cgdFile);
				cgdFile.delete();
				changeCGDHeader(newCgdFile); // should overwrite existing CGD.txt file if exists

				// modify and save properties
				properties.setProperty("CGD_DB_date", Long.toString(urlDate.getTime()));
				properties.setProperty("CGD_DB_filename", newCgdFile.getName());
				currentDate= urlDate;

				saveProperties();
			}
		} catch (IOException e) {
			System.err.println("[Discovery app]: Error when processing CGD URL.");
			e.printStackTrace();
		}
	}
	
	
	/** 
	 * Copy CGD file to cache if it is not already there.
	 */
	private void copyCGD() {
		File f= new File(DirectorySettings.getMedSavantDirectory().getPath() +
				File.separator + "cache" + File.separator + properties.getProperty("CGD_DB_filename"));
		if (!f.exists()) { // copy the default pre-packaged CGD file from Nov. 26, 2013.
			try {
				InputStream in= DiscoveryPanel.class.getResourceAsStream("/db_files/CGD.txt");
				OutputStream out= new FileOutputStream(f);
				IOUtils.copy(in, out);
				in.close();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/** Uncompresses the gzipped File. 
	 *  Code adapted from StackOverFlow example.
	 * @param gzipFile the gzipped file object
	 * @precondition Expecting the gzipFile has a .gz extension
	 * @return the new file object
	 */
	public static File gunzip(File gzipFile) {
		// Get the file name without the .gz extension
		Pattern gunzipFilenamePattern= Pattern.compile("^(.+).gz$", Pattern.CASE_INSENSITIVE);
		Matcher gunzipFilenameMatcher= gunzipFilenamePattern.matcher(gzipFile.getPath());
		String gunzipFilename= null;
		if (gunzipFilenameMatcher.find())
			gunzipFilename= gunzipFilenameMatcher.group(1);
		
		// read the compressed file and output an uncompressed version
		GZIPInputStream in = null;
		OutputStream out = null;
		try {
		   in = new GZIPInputStream(new FileInputStream(gzipFile));
		   out = new FileOutputStream(gunzipFilename);
		   byte[] buf = new byte[1024 * 4];
		   int len;
		   while ((len = in.read(buf)) > 0) {
			   out.write(buf, 0, len); // if you use write(buf), end up writing weird characters if buf not full
		   }
		   in.close();
		   out.close();
		}
		catch (IOException e) {
		   e.printStackTrace();
		}
		
		return new File(gunzipFilename);
	}
	
	
	/** Change CGD header to predefined header. Since the file is small, make 
	 * all changes in memory and output a new file by the same name. */
	public static void changeCGDHeader(File cgdFile) throws FileNotFoundException, IOException {
		List<String> newLines= new LinkedList<String>();
		BufferedReader reader= null;
		
		// Store the custom header - just the first line
		reader= new BufferedReader(
			new InputStreamReader(DiscoveryDB.class.getResourceAsStream("/db_files/CGD_header.txt"))); 
		newLines.add(reader.readLine());
		reader.close();
		
		// Store all the non-header lines from the current CGD file
		reader= new BufferedReader(new FileReader(cgdFile));
		boolean inHeader= true;
		String line= reader.readLine();
		while (line != null) {
			if (inHeader) {
				inHeader= false;
			} else {
				newLines.add(line);
			}
			
			line= reader.readLine();
		}
		reader.close();
		
		// Overwrite all the new lines to the file
		BufferedWriter writer= new BufferedWriter(new FileWriter(cgdFile, false)); // do not append
		for (String l : newLines) {
			writer.write(l);
			writer.newLine();
		}
		writer.close();
	}
	
	/**
	 * Convert string list of integers into an int[].
	 * @param arr String list in the format "[1,2,3,4,5]
	 */
	private int[] getIntArrayFromString(String arr) {
		String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").split("\\s?,\\s?");

		int[] results = new int[items.length];

		for (int i = 0; i < items.length; i++) {
			try {
				results[i] = Integer.parseInt(items[i]);
			} catch (NumberFormatException nfe) {};
		}
		
		return results;
	}
}