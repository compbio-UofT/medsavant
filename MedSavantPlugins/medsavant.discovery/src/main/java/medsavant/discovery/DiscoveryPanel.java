package medsavant.discovery;

import org.ut.biolab.medsavant.shared.appdevapi.GenePanel;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.swing.ButtonStyle;
import com.jidesoft.swing.CheckBoxList;
import com.jidesoft.swing.JideButton;
import jannovar.common.VariantType;
import java.util.Calendar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
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
import javax.swing.BoxLayout;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.query.view.JScrollMenu;
import org.ut.biolab.medsavant.client.region.RegionController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.client.view.genetics.charts.Ring;
import org.ut.biolab.medsavant.client.view.genetics.charts.RingChart;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.ClinvarSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.HGMDSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SimpleVariant;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.shared.appdevapi.AppColors;
import org.ut.biolab.medsavant.shared.appdevapi.DBAnnotationColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.shared.serverapi.AnnotationManagerAdapter;


/**
 * Default panel for Discovery app
 * 
 * @author rammar
 */
public class DiscoveryPanel extends JPanel {
	private static final Log LOG = LogFactory.getLog(MedSavantClient.class);
	private static final Properties properties= new Properties();
	private static final String PROPERTIES_FILENAME= DirectorySettings.getMedSavantDirectory().getPath() +
				File.separator + "cache" + File.separator + "discovery_app_settings.xml";
	private static final int[] DEFAULT_COLUMNS= new int[] {
		BasicVariantColumns.INDEX_OF_CHROM, BasicVariantColumns.INDEX_OF_START_POSITION,
		BasicVariantColumns.INDEX_OF_END_POSITION, BasicVariantColumns.INDEX_OF_REF,
		BasicVariantColumns.INDEX_OF_ALT, BasicVariantColumns.INDEX_OF_ZYGOSITY
	};
	private static final int DEFAULT_FETCH_LIMIT= 2000;
	private static final String DEFAULT_CGD_URL= "http://research.nhgri.nih.gov/CGD/download/txt/CGD.txt.gz";
	private static final String DEFAULT_CGD_FILENAME= "CGD.txt";
	private static final int DEFAULT_COVERAGE_THRESHOLD= 10;
	private static final double	DEFAULT_HET_RATIO= 0.3;
	private static final double DEFAULT_AF_THRESHOLD= 0.05;
	private static final String[] DEFAULT_AF_DB_LIST= new String[] {
		DBAnnotationColumns.AF1000g, DBAnnotationColumns.AF6500ex
	};
	private static final String DISCOVERY_DB_USER= "incidental_user";
	private static final String DISCOVERY_DB_PASSWORD= "$hazam!2734"; // random password		
	private static final List<String> JANNOVAR_MUTATIONS= Arrays.asList(
		VariantType.MISSENSE.toString(), VariantType.SYNONYMOUS.toString(),
		VariantType.FS_DELETION.toString(), VariantType.FS_INSERTION.toString(),
		VariantType.FS_SUBSTITUTION.toString(), VariantType.FS_DUPLICATION.toString(),
		VariantType.NON_FS_DELETION .toString(), VariantType.NON_FS_INSERTION.toString(),
		VariantType.NON_FS_SUBSTITUTION.toString(), VariantType.NON_FS_DUPLICATION.toString(),
		VariantType.SPLICING.toString(), VariantType.STOPGAIN.toString(),
		VariantType.STOPLOSS.toString(), 
		VariantType.START_LOSS.toString(), VariantType.UTR3.toString(),
		VariantType.UTR5.toString(), VariantType.INTRONIC.toString(),
		VariantType.UPSTREAM.toString(), VariantType.DOWNSTREAM.toString(),
		VariantType.INTERGENIC.toString(), VariantType.ncRNA_EXONIC.toString(),
		VariantType.ncRNA_INTRONIC.toString(), VariantType.ncRNA_SPLICING.toString(),
		VariantType.ERROR.toString()
	);
	private static final String[] DEFAULT_MUTATIONS= new String[] {
		VariantType.MISSENSE.toString(), VariantType.FS_DELETION.toString(),
		VariantType.FS_INSERTION.toString(), VariantType.FS_SUBSTITUTION.toString(),
		VariantType.FS_DUPLICATION.toString(), VariantType.NON_FS_DELETION .toString(),
		VariantType.NON_FS_INSERTION.toString(), VariantType.NON_FS_SUBSTITUTION.toString(),
		VariantType.NON_FS_DUPLICATION.toString(), VariantType.SPLICING.toString(),
		VariantType.STOPGAIN.toString(), VariantType.START_LOSS.toString()
	};
	public static final String EXIST_KEYWORD= "Exists";
	public static final String EQUALS_KEYWORD= "=";
	public static final String LESS_KEYWORD= "<";
	public static final String GREATER_KEYWORD= ">";
	public static final String LIKE_KEYWORD= "Like";
	private static final List<String> OPERATOR_OPTIONS= Arrays.asList(
		EXIST_KEYWORD, EQUALS_KEYWORD, LESS_KEYWORD, GREATER_KEYWORD, LIKE_KEYWORD);
	private static final int TIMEOUT_CONNECTION= 10000; // 10 seconds (10000 milliseconds)
	private static final int TIMEOUT_DATA_READ= 15000; // 15 seconds (15000 milliseconds)	
	private static final String LEFT_HIDE_STRING= "<<";
	private static final String RIGHT_HIDE_STRING= ">>";
	private static final String CUSTOM_GENE_PANEL_TEXT= "Custom...";
	private static final Color GREYISH_BLUE= new Color(27, 106, 198);
	private static final String HPO_GENE_PANEL_TEXT= "Human Phenotypes...";
	
	private final int TOP_MARGIN= 0;
	private final int SIDE_MARGIN= 0;
	private final int BOTTOM_MARGIN= 0;
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
	private List<String> mutationFilterList= new LinkedList<String>();
	private List<String> genePanelList;
	private String currentGenePanel;
	private String[] mutationArray;
	
	private boolean analysisRunning= false;
	private DiscoveryHSQLServer server;
	private boolean dbLoaded= false;
	private Date currentDate= null;
	
	private JPanel view;
	private RoundedPanel workview;
	private JideButton choosePatientButton;
	private JideButton analyzeButton;
	private String analyzeButtonDefaultText= "Refresh";
	private IndividualSelector customSelector= new IndividualSelector(true); // to choose the patient sample
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
	private CheckBoxList afChooser;
	private URL cgdURL;
	private CollapsiblePane collapsible;
	private CollapsiblePane collapsibleSettings;
	private JLabel cgdURLLabel= new JLabel("Clinical Genomics Database (CGD) URL");
	private JTextField cgdText;
	private JButton cgdHelp;
	private JLabel cgdDateLabel;
	private JButton addFilterButton;
	private JPanel patientPanel;
	private JLabel fetchLimitLabel;
	private JTextField fetchLimitText;
	private JButton fetchLimitHelp;
	private JPanel progressPanel;
	private RingChart ringChart;
	private Ring ring;
	private JScrollPane patientJSP;
	private JRootPane rootPane;
	private JideButton leftHideButton= new JideButton(LEFT_HIDE_STRING);
	private JideButton rightHideButton= new JideButton(RIGHT_HIDE_STRING);
	private VariantSummaryPanel vsp;
	private int patientPanelInsertPosition= 2;
	private JPanel errorMessage;
	private GenePanel customGenePanel;
	private GenePanel hpoPanel;
	private JDialog hpoDialog;
	private JPanel hpoStatus;
    
	
	public DiscoveryPanel() {
		/* Set up the properties based on stored user preference. */
		loadProperties();
	
		/* Set up the initial app view. */
		setupView();
		
		// the local server
		server= new DiscoveryHSQLServer(DISCOVERY_DB_USER, DISCOVERY_DB_PASSWORD);
	}	
	
	public JPanel getView() {
		return view;
	}
	
	
	/**
	 * Set up the initial view of the DiscoveryPanel.
	 */
	private void setupView() {
		view= ViewUtil.getClearPanel();
		view.setLayout(new BorderLayout());
		//view.setBorder(BorderFactory.createLineBorder(Color.RED));
		view.setBorder(BorderFactory.createEmptyBorder(TOP_MARGIN, SIDE_MARGIN, BOTTOM_MARGIN, SIDE_MARGIN));
		
		// main view
		workview= new RoundedPanel(10);
		workview.setBackground(AppColors.iCloudBlue);
		workview.setLayout(new MigLayout("insets 0px, gapx 0px", "", "top"));
		
		choosePatientButton= new JideButton("Choose Patient");
		choosePatientButton.setButtonStyle(JideButton.TOOLBAR_STYLE);
		choosePatientButton.setOpaque(true);
		choosePatientButton.setFont(new Font(choosePatientButton.getFont().getName(),
			Font.PLAIN, 18));
		choosePatientButton.addActionListener(getChoosePatientButtonAL());
				
		analyzeButton= new JideButton(analyzeButtonDefaultText);
		analyzeButton.setButtonStyle(JideButton.TOOLBAR_STYLE);
		analyzeButton.setFont(new Font(analyzeButton.getFont().getName(),
			Font.BOLD, 14));
		analyzeButton.setEnabled(false); // cannot click until valid DNA ID is selected
		analyzeButton.setVisible(false);
		analyzeButton.setOpaque(true);
		analyzeButton.addActionListener(getAnalyzeButtonAL()); // to run the analysis
		
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
		
		/* Error messages. */
		errorMessage= new JPanel();
		errorMessage.setLayout(new MigLayout("", "center", ""));		
		errorMessage.setBackground(workview.getBackground());
		
		variantPane= new JScrollPane();
		variantPane.setBorder(BorderFactory.createEmptyBorder());
		JPanel initVariantPane= new JPanel();
		JLabel initVariantPaneLabel= new JLabel("Choose patient to see genomic variants.");
		initVariantPane.setLayout(new MigLayout("align 50% 50%"));
		initVariantPane.add(initVariantPaneLabel);
		initVariantPaneLabel.setFont(new Font(initVariantPaneLabel.getFont().getName(), Font.PLAIN, 14));
		initVariantPaneLabel.setForeground(Color.DARK_GRAY);
		variantPane.setViewportView(initVariantPane);
		
		/* Custom allele freq selection. */
		afChooser= new CheckBoxList(getAFColumnArray());
		afChooser.addCheckBoxListSelectedValues(chooserAFArray);
		chooseAFColumns= new JButton("Choose Allelle Frequency DBs");
		chooseAFColumns.setFocusPainted(false);
		chooseAFColumns.addActionListener(getChooseAFColumnsAL());
		chooseAFColumnsHelp= ViewUtil.getHelpButton("Allele Frequency Database selector", 
				"Choose the databases to use when filtering for allele frequency.");
		
		/* Custome variant filter selection. */
		addFilterButton= new JButton("Add variant filter");
		addFilterButton.setFocusPainted(false);
		addFilterButton.addActionListener(getAddFilterButtonAL());
		
		
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
		JPanel resetPanel= new JPanel();
		resetPanel.setLayout(new MigLayout("insets 2 6 2 6")); // top left bottom right
		resetPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		resetPanel.setBackground(workview.getBackground());
		JButton reset= new JButton("Restore defaults");
		reset.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				resetProperties();
				MedSavantFrame.getInstance().forceRestart();
			}
		}
		);
		resetPanel.add(reset);
		resetPanel.add(new JLabel("(restart required)"));
		
		collapsibleSettings= new CollapsiblePane("Advanced Settings");
		collapsibleSettings.setLayout(new MigLayout());
		//collapsibleSettings.add(fetchLimitLabel, "split"); // split this cell, makes the panel less wide (see MigLayout Quickstart on splitting)
		//collapsibleSettings.add(fetchLimitText);
		//collapsibleSettings.add(fetchLimitHelp, "wrap");
		collapsibleSettings.add(cgdURLLabel); // split this cell, makes the panel less wide (see MigLayout Quickstart on splitting)
		collapsibleSettings.add(cgdHelp, "wrap");
		collapsibleSettings.add(cgdText, "span");
		collapsibleSettings.add(cgdDateLabel, "wrap");
		collapsibleSettings.add(new JLabel(" "), "wrap"); // use as a spacer
		collapsibleSettings.add(resetPanel);
		collapsibleSettings.setStyle(CollapsiblePane.PLAIN_STYLE);
		collapsibleSettings.setFocusPainted(false);
		collapsibleSettings.collapse(true);
		
		/* Progress bar panel. */
		progressPanel= new JPanel(new MigLayout("", "center", ""));
		progressPanel.setBackground(workview.getBackground());
		progressPanel.add(ringChart, "wrap");
		progressPanel.add(progressLabel, "gapy 25, wrap");
		progressPanel.add(pw);
		
		/* Patient selection panel. */
		patientPanel= new JPanel();
		patientPanel.setBackground(workview.getBackground());
		patientPanel.setLayout(new MigLayout("insets 10 10 0 0, gapy 0px")); // create a bit of inset spacing top and left
		patientPanel.add(choosePatientButton, "alignx center, wrap");
		patientPanel.add(addFilterButton, "alignx center, wrap, gapy 20px");
		patientPanel.add(collapsible, "wrap, gapy 20px");
		patientPanel.add(geneSelectionPanel(), "wrap");
		patientPanel.add(mutationCheckboxPanel(), "wrap");
		patientPanel.add(collapsibleSettings, "wrap");
		patientPanel.add(progressPanel, "alignx center, wrap");
		patientPanel.add(analyzeButton, "alignx center, wrap"); // need wrap for spacer below
		patientPanel.add(new JLabel(" "), "gapy 20px"); // use as a spacer at the bottom
		// Put the patient panel in a JScrollPane
		patientJSP= new JScrollPane(patientPanel);
		patientJSP.setMinimumSize(new Dimension(patientPanel.getMinimumSize().width + PANE_WIDTH_OFFSET, PANE_HEIGHT));
		patientJSP.setPreferredSize(new Dimension(patientPanel.getMinimumSize().width, patientPanel.getMaximumSize().height));
		patientJSP.setBorder(BorderFactory.createEmptyBorder());
		patientJSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);		
		
		/* Set up the variant summary panel. */
		vsp= new VariantSummaryPanel("Variant Summary");
		
		/* Final window layout along with size preferences. */
		rootPane= new JRootPane();
		Container contentPane= rootPane.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		workview.add(patientJSP);
		workview.add(variantPane);
		contentPane.add(workview);
		
		
		/* Add the UI to the main app panel. */
		view.add(rootPane, BorderLayout.CENTER);
		this.setLayout(new BorderLayout());
		this.add(view, BorderLayout.CENTER);
		
		/* Set the sizing for a couple panels and let the other panels auto-size. 
		 * Do sizing after all the components have been added to their parent panels
		 * otherwise the size will be Dimension(0,0). */
		choosePatientButton.setMinimumSize(new Dimension(
			250, choosePatientButton.getHeight()));
		analyzeButton.setMinimumSize(new Dimension(
			200, analyzeButton.getSize().height));
		collapsible.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 0));
		collapsibleSettings.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 0));
		collapsibleSettings.setMaximumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 
			collapsibleSettings.getMaximumSize().height)); // also set the max size for this Pane
		patientPanel.setMinimumSize(new Dimension(PANE_WIDTH, PANE_HEIGHT));
		variantPane.setPreferredSize(variantPane.getMaximumSize()); // Needs changing - try MigLayout features
    }
	
	
	/**
	 * Creates an ActionListener for specific use with the choosePatientButton.
	 * @return the choosePatientButton ActionListener
	 */
	private ActionListener getChoosePatientButtonAL() {
		ActionListener outputAL= new ActionListener() 
		{
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
		};
		
		return outputAL;
	}
	
	
	/**
	 * Creates an ActionListener for specific use with the analyzeButton.
	 * @return the analyzeButton ActionListener
	 */
	private ActionListener getAnalyzeButtonAL() {
		ActionListener outputAL= new ActionListener()
		{	
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
									DiscoveryDB.populateDB(server.getURL(), DISCOVERY_DB_USER, DISCOVERY_DB_PASSWORD, properties);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}

							/* Get all the user settings. */
							setAllValuesFromFields();

							/* Every time an analysis is run, parameters/settings are saved. */
							saveProperties();

							/*  Get discovery findings. Initialize or update for current DNA ID. */
							if (discFind == null || !currentIndividualDNA.equals(discFind.dnaID)) {
								discFind= new DiscoveryFindings(currentIndividualDNA);
							}
							
							int total= discFind.getMaximumVariantCount(); // total variants for this DNA ID
							
							progressLabel.setText(total + " total variants found. Applying filters.");
							
							baseComboCondition= discFind.getComboCondition(
								Arrays.asList(afChooser.getCheckBoxListSelectedValues()),
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
								int filtered= discFind.getFilteredVariantCount();

								ring= new Ring();
								ring.addItem("Pass all filters", filtered, Color.LIGHT_GRAY);
								ring.addItem("Don't pass filters", total - filtered, Color.RED);
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
							errorStatusReport();
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
					discFind.setCancelled();
				}
			}
		};
		
		return outputAL;
	}
	
	
	/**
	 * Creates an ActionListener for specific use with the addFilterButton.
	 * @return the addFilterButton ActionListener
	 */
	private ActionListener getAddFilterButtonAL() {
		ActionListener outputAL= new ActionListener()
		{		
			@Override
			public void actionPerformed (ActionEvent e) {
				JPopupMenu popupMenu= new JPopupMenu();
				JScrollMenu filterMenu= new JScrollMenu(addFilterButton.getText());

				for (Object columnName : getDbColumnList()) {
					final JMenuItem filter= new JMenuItem((String) columnName);
					filter.addMouseListener(
						new MouseListener() {
							@Override
							public void mousePressed(MouseEvent me) {
								patientPanel.add(addFilterPanel(filter.getText()), "wrap", patientPanelInsertPosition);
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
		};
		
		return outputAL;
	}
	
	
	/**
	 * Creates an ActionListener for specific use with chooseAFColumns
	 * @return the chooseAFColumns ActionListener
	 */
	public ActionListener getChooseAFColumnsAL() {
		ActionListener outputAL= new ActionListener()
		{		
			@Override
			public void actionPerformed (ActionEvent e) {
				JPopupMenu popupMenu= new JPopupMenu();
				JMenu afMenu= new JMenu(chooseAFColumns.getText());
				afMenu.add(afChooser);
				popupMenu.add(afMenu);
				popupMenu.show(chooseAFColumns, 0, 0);
			}
		};		
		
		return outputAL;
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
				/* Add the variant summary panel and hide buttons once the table
				 * is presented to the user and a variant has been selected. Also
				 * check that the user hasn't already hidden the summary panel. */
				if (vsp.getParent() != workview && !rightHideButton.getText().equals(LEFT_HIDE_STRING)) {
					workview.add(vsp);
					workview.revalidate();

					drawHideButtons();
					
					/* Redraw hide buttons once the component has been shown or 
					 * resized because the JLayeredPane from the JRootPane doesn't
					 * have a layout to dynamically resize itself.
					 * Add this listener down here, so buttons aren't drawn at startup. */
					rootPane.addComponentListener(
						new ComponentListener() {
							@Override
							public void componentShown(ComponentEvent ce) {
								//drawHideButtons(); // commented out - no longer draw upon loading of rootPane
							}

							@Override
							public void componentResized(ComponentEvent ce) {
								drawHideButtons();
							}

							@Override public void componentMoved(ComponentEvent ce) {}
							@Override public void componentHidden(ComponentEvent ce) {}
						}
					);
				}
				
                if (stp.getTable().getSelectedRow() != -1) {
					SortableTable st= stp.getTable();
                    int selectedIndex= st.getSelectedRow();
					String chr= (String) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_CHROM);
					long start= ((Integer) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_START_POSITION)).longValue();
					long end= ((Integer) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_END_POSITION)).longValue();
					String ref= (String) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_REF);
					String alt= (String) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_ALT);
					String type= (String) st.getModel().getValueAt(selectedIndex, BasicVariantColumns.INDEX_OF_VARIANT_TYPE);
					
					
					Object[] line= new Object[discFind.header.size()];
					for (int index= 0; index != discFind.header.size(); ++index)
						line[index]= st.getModel().getValueAt(selectedIndex, index);
					
					/* Set up/update the Variant Summary Panel. */
					// clearing a collapsible pane leads to weird errors, so I'm removing it and adding it back.
					vsp.clearSummaryPane();
					vsp.updateGeneSymbol(discFind.getGeneSymbol(line));
					vsp.updateAlleleFrequencyPane(line, discFind.header);
					vsp.updateAnnotation(line, discFind.header);
					vsp.updateHarmfulnessPredictorsPane(line, discFind.header);
					vsp.updateOtherIndividualsPane(new SimpleVariant(chr, start, end, ref, alt, type));
					vsp.updateCGDPane(discFind.getZygosity(line), discFind.getGender(), discFind.getClassification(line));
					ClinvarSubInspector csi= new ClinvarSubInspector();
					csi.getInfoPanel(); // set this to avoid null values, but we're not using it.
					csi.setVariantLine(line, discFind.header);
					vsp.updateClinvarPane(csi);
					HGMDSubInspector hsi= new HGMDSubInspector();
					hsi.getInfoPanel(); // set this to avoid null values, but we're not using it.
					hsi.setVariantLine(line, discFind.header);
					vsp.updateHGMDPane(hsi);
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
		j.setBackground(workview.getBackground());
		
		// CollapsiblePane for the filter
		final CollapsiblePane collapsible= new CollapsiblePane(name);
		collapsible.setLayout(new MigLayout("align center"));
		collapsible.setStyle(CollapsiblePane.PLAIN_STYLE);
		collapsible.setFocusPainted(false);
		
		// The operator selection button
		final JButton operatorButton= new JButton(EXIST_KEYWORD); // defaults to Exists
		final JTextField operatorText= new JTextField(10); // 10 character spaces wide
		
		// Add this FilterDetails object to the list of conditions
		final FilterDetails filterPanelDetails= new FilterDetails(this);
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
			
			// Set the defaults
			if (Arrays.asList(mutationArray).contains(jm)) {
				currentCheckBox.setSelected(true);
				mutationFilterList.add(currentCheckBox.getText());
			}
			
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
		final CollapsiblePane collapsibleGene= new CollapsiblePane("Genes, Panels and Phenotypes");
		collapsibleGene.setLayout(new MigLayout("gapy 0px, align center"));
		
		final String GENE_TEXT= "Gene";
		final String GENE_PANEL_TEXT= "Gene panel";
		final String HUMAN_PHENOTYPES_TEXT= "Human phenotypes";
		final String triangleString= " ▾"; // I added the triangles to imply this is a popupmenu button
		
		final JButton geneButton= new JButton(GENE_PANEL_TEXT + triangleString);
		final JTextField geneTextField= new JTextField(10); // 10 character spaces wide
		final JComboBox genePanelComboBox= new JComboBox();
		final JButton hpoButton= new JButton("Launch HPO chooser...");
		populateGenePanels(genePanelComboBox);
		currentGenePanel= (String) genePanelComboBox.getSelectedItem(); // initialize
		hpoStatus= new JPanel(); //initialize
		hpoStatus.setLayout(new MigLayout());
		hpoStatus.setBackground(workview.getBackground());
		
		final JButton genePanelHelp= ViewUtil.getHelpButton("Gene Panel selection", 
				"Choose the gene panel appropriate for your workflow. Default " +
				"panels include the American College of Medical Genetics recommendations " +
				"for reporting incidental findings (ACMG), and all genes with " +
				"manually curated modes of inheritance from the Clinical Genomics " +
				"Database (CGD).");
		
		// Add this FilterDetails object to the list of conditions
		final FilterDetails filterPanelDetails= new FilterDetails(this);
		filterPanelDetails.setDetails(BasicVariantColumns.JANNOVAR_SYMBOL.getAlias(),
			LIKE_KEYWORD, geneTextField);
		
		// Update the gene panels dropdown when the popup menu is made visible
		genePanelComboBox.addPopupMenuListener(
			new PopupMenuListener() {
				@Override public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
					populateGenePanels(genePanelComboBox);
				}
				
				@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {}
				@Override public void popupMenuCanceled(PopupMenuEvent pme) {}
			}
		);
		
		// Respond to gene panel selection when a selection has been made (clicked)
		// This includes HPO selection
		genePanelComboBox.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					// If nothing has been selected yet, don't assign null
					if (genePanelComboBox.getSelectedItem() != null)
						currentGenePanel= (String) genePanelComboBox.getSelectedItem();				
					
					if (currentGenePanel.equals(CUSTOM_GENE_PANEL_TEXT)) { // custom gene panel
						// newline before adding, like adding wrap to previous element
						collapsibleGene.add(customGenePanel.getSearchConditionPanel(), "newline, span");
					} else {
						collapsibleGene.remove(customGenePanel.getSearchConditionPanel());
						
						// clear the custom panel and HPO ComboConditions and select an existing panel
						clearPanelConditions();
						
						customGenePanel.addCustomGenePanel(currentGenePanel);
					}
					
					collapsibleGene.revalidate(); // refresh
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
					for (final String s : (new String[] {GENE_PANEL_TEXT, HUMAN_PHENOTYPES_TEXT, GENE_TEXT})) {
						JMenuItem jmi= new JMenuItem(s);
						jmi.addMouseListener(
							new MouseListener() {
								@Override
								public void mousePressed(MouseEvent me) {
									geneButton.setText(s + triangleString);
									
									// Clear the existing pane. Note: using removeAll()
									// doesn't work with CollapsiblePanes
									collapsibleGene.remove(genePanelComboBox);
									collapsibleGene.remove(genePanelHelp);
									collapsibleGene.remove(customGenePanel.getSearchConditionPanel());
									collapsibleGene.remove(geneTextField);
									collapsibleGene.remove(hpoButton);
									collapsibleGene.remove(hpoStatus);
									
									if (s.equals(GENE_TEXT)) {
										collapsibleGene.add(geneTextField);
										conditionList.add(filterPanelDetails);
										currentGenePanel= DiscoveryFindings.ALL_GENE_PANEL;
									} else if (s.equals(GENE_PANEL_TEXT)) {
										collapsibleGene.add(genePanelComboBox);
										collapsibleGene.add(genePanelHelp);
										conditionList.remove(filterPanelDetails);
									} else if (s.equals(HUMAN_PHENOTYPES_TEXT)) {
										collapsibleGene.add(hpoButton, "wrap");
										collapsibleGene.add(hpoStatus);
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
		
		// HPO Launcher
		hpoButton.addActionListener(
			new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent ae) {
					// clear the custom panel and HPO ComboConditions and select an existing panel
					clearPanelConditions();
					
					hpoStatus.add(new JLabel("Loading ontology..."), "alignx center");
					hpoStatus.add(new ProgressWheel(), "alignx center");
					hpoStatus.revalidate();
					
					hpoSelectionDialog(); // creates AND displays the dialog and updates the status
				}
			}
		);
		
		// Create a custom gene panel JPanel to be added to the pane later
		customGenePanel= new GenePanel(GenePanel.PANEL);
		customGenePanel.setBackground(workview.getBackground());
		
		// Add the components to the collapsible pane
		collapsibleGene.add(geneButton, "wrap");
		collapsibleGene.add(genePanelComboBox);
		collapsibleGene.add(genePanelHelp);		
		
		collapsibleGene.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 0));		
		collapsibleGene.setStyle(CollapsiblePane.PLAIN_STYLE);
		collapsibleGene.setFocusPainted(false);
		collapsibleGene.collapse(true);
		
		return collapsibleGene;
	}
	
	
	/**
	 * Create and show a JDialog for HPO selection.
	 */
	private void hpoSelectionDialog() {
		if (hpoDialog != null) {
			hpoDialog.setVisible(true);
		} else {
			hpoDialog= new JDialog(MedSavantFrame.getInstance(), "Human Phenotype Ontology (HPO) Chooser");

			// Loading the HPO panel is quite laggy and makes the client freeze
			// doing this loading in another thread.
			MedSavantWorker hpoLoadingThread=
				new MedSavantWorker<Object> (DiscoveryPanel.class.getCanonicalName()) {
					@Override
					protected Object doInBackground() throws Exception {
						hpoPanel= new GenePanel(GenePanel.HPO, hpoDialog);
						hpoPanel.setBackground(workview.getBackground());
						hpoDialog.setContentPane(hpoPanel.getSearchConditionPanel());
						
						hpoDialog.setSize(600,400); // FIX - find out how it's implemented elsewhere without being fixed size
						
						return null;
					}

					@Override protected void showSuccess(Object t) {
						hpoDialog.setVisible(true);
					}
				};

			hpoLoadingThread.execute();
			
			hpoDialog.setLocationRelativeTo(this.choosePatientButton);
			
			hpoDialog.addWindowFocusListener(
				new WindowFocusListener() {
					@Override
					public void windowGainedFocus(WindowEvent we) {
						// Clear the loading message
						hpoStatus.removeAll();
						hpoStatus.revalidate();
					}
					
					@Override
					public void windowLostFocus(WindowEvent we) {
						if (hpoPanel != null && !hpoPanel.getComboCondition().isEmpty()) {
							hpoStatus.add(new JLabel("HPO conditions added."));
							hpoStatus.revalidate();
						}
					}
				}
			);
		}
	}
	
	
	/**
	 * Clears the conditions from the GenePanel objects.
	 */
	private void clearPanelConditions() {
		if (customGenePanel != null)
			customGenePanel.clearCondition();
		
		if (hpoPanel != null)
			hpoPanel.clearCondition();
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
		
		// Add the gene panel genes, if a panel has been entered
		if (customGenePanel != null && !customGenePanel.getComboCondition().isEmpty()) {
			newComboCondition.addCondition(customGenePanel.getComboCondition());
		}
		
		// Add the hpo genes, if a panel has been entered
		if (hpoPanel != null && !hpoPanel.getComboCondition().isEmpty()) {
			newComboCondition.addCondition(hpoPanel.getComboCondition());
		}
		
		discFind.setComboCondition(newComboCondition);
	}
	
    
	/**
	 * Add a filter condition describing mutations to the ComboCondition of DiscoveryFindings.
	 * @param mutations A list of the mutation Strings, as annotated in Jannovar, to filter the variants
	 */
	private void addMutationCondition(List<String> mutations) {
		Map<String, String> columns= discFind.dbAliasToColumn;
		String JANNOVAR_EFFECT= BasicVariantColumns.JANNOVAR_EFFECT.getAlias();
		
		if (columns.get(JANNOVAR_EFFECT) != null) {
			ComboCondition mutationComboCondition= new ComboCondition(ComboCondition.Op.OR);
			
			for (String m : mutations) {
					/* In the interest of efficiency, use Like instead of iLike 
					 * here, just make sure the letter case of the Jannovar annotations
					 * and the mutations stored here match.
					 * NOTE: need the wildcards because some mutations may look like
					 * this - INTRON,STOPGAIN. However, also need to ensure that
					 * %FS_INSERTION% doesn't match "NON_FS_INSERTION", so condition
					 * will be specified as below matching either m% or %,m% - the
					 * delimiter is ":". */
				
				// First mutation in the string
				mutationComboCondition.addCondition(
					BinaryCondition.like(discFind.ts.getDBColumn(columns.get(JANNOVAR_EFFECT)), m + "%"));
				
				// OR: Multi-annotation mutation midstring
				mutationComboCondition.addCondition(
					BinaryCondition.like(discFind.ts.getDBColumn(columns.get(JANNOVAR_EFFECT)), "%," + m + "%"));
			}
			
			newComboCondition.addCondition(mutationComboCondition);
		}
	}
	
	
	/** 
	 * Create hide buttons for the patient and inspector panels.
	 * This method draws the hide buttons in a JLayeredPane from the JRootPane
	 * instance once the JRootPane has been made visible in parent containers.
	 * This is required because the JLayeredPane has to have dimensions related
	 * to the content pane of the JRootPane, and the dimensions are non-zero
	 * once the JRootPane has been made visible. Also this is critical when the
	 * window or JRootPane component has been resized.
	 */
	private void drawHideButtons() {
		JLayeredPane layeredPane= rootPane.getLayeredPane();
		layeredPane.setSize(rootPane.getSize());
		
		leftHideButton.setButtonStyle(ButtonStyle.TOOLBAR_STYLE);
		leftHideButton.setFont(new Font(leftHideButton.getFont().getName(), Font.BOLD, 20));
		leftHideButton.setForeground(GREYISH_BLUE);
		leftHideButton.setBackground(workview.getBackground());
		leftHideButton.setSize(leftHideButton.getMinimumSize());
		leftHideButton.setLocation(0, 0);
		
		rightHideButton.setButtonStyle(ButtonStyle.TOOLBAR_STYLE);
		rightHideButton.setFont(new Font(rightHideButton.getFont().getName(), Font.BOLD, 20));
		rightHideButton.setForeground(GREYISH_BLUE);
		rightHideButton.setBackground(workview.getBackground());
		rightHideButton.setSize(rightHideButton.getMinimumSize());
		rightHideButton.setLocation(layeredPane.getSize().width - rightHideButton.getSize().width, 0);
		
		/* Hide actions. */
		leftHideButton.addMouseListener(
			new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent me) {
					if (leftHideButton.getText().equals(LEFT_HIDE_STRING)) {
						leftHideButton.setText(RIGHT_HIDE_STRING);
						workview.remove(patientJSP); // remove the patient panel when pressed
					} else if (leftHideButton.getText().equals(RIGHT_HIDE_STRING)) {
						leftHideButton.setText(LEFT_HIDE_STRING);
						workview.add(patientJSP, 0); // add the patient panel when pressed
					}
					workview.updateUI();
				}
				// remaining methods included but do nothing
				@Override public void mouseExited(MouseEvent me) {}
				@Override public void mouseReleased(MouseEvent me) {}
				@Override public void mousePressed(MouseEvent me) {}
				@Override public void mouseEntered(MouseEvent me) {}
			}
		);
		
		rightHideButton.addMouseListener(
			new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent me) {
					if (rightHideButton.getText().equals(RIGHT_HIDE_STRING)) {
						rightHideButton.setText(LEFT_HIDE_STRING);
						workview.remove(vsp); // remove the patient panel when pressed
					} else if (rightHideButton.getText().equals(LEFT_HIDE_STRING)) {
						rightHideButton.setText(RIGHT_HIDE_STRING);
						workview.add(vsp, -1); // add the patient panel to the end when pressed
					}
					workview.updateUI();
				}
				// remaining methods included but do nothing
				@Override public void mouseExited(MouseEvent me) {}
				@Override public void mouseReleased(MouseEvent me) {}
				@Override public void mousePressed(MouseEvent me) {}
				@Override public void mouseEntered(MouseEvent me) {}
			}
		);
		
		
		/* Add the buttons in a layer above the content pane layer.
		 * Only add the buttons if they haven't already been added.*/
		if (leftHideButton.getParent() != layeredPane) { // only check left since left and right are added together
			layeredPane.add(leftHideButton, JLayeredPane.PALETTE_LAYER);
			layeredPane.add(rightHideButton, JLayeredPane.PALETTE_LAYER);
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
			afChooser.getCheckBoxListSelectedValues()), "\"\t\"") + "\"";
		properties.setProperty("af_chooser_list", afChooserStringList);
		
		// quote-enclosed, comma-delimited list as string
		String mutationStringList= "\"" + StringUtils.join(mutationFilterList, "\"\t\"") + "\"";
		properties.setProperty("mutation_list", mutationStringList);
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
	 * Get the allele frequency columns from the table.
	 * @return String list of allele frequency column names.
	 */
	private Object[] getAFColumnArray() {
		List<String> output= new ArrayList<String>();
		
		try {
			AnnotationManagerAdapter am= MedSavantClient.AnnotationManagerAdapter;
			Map<String, Set<CustomField>> fieldMap= 
				am.getAnnotationFieldsByTag(LoginController.getInstance().getSessionID(), true);
			
			Set<CustomField> columnNames= fieldMap.get(CustomField.ALLELE_FREQUENCY_TAG);
			for (CustomField cf : columnNames) {
				output.add(cf.getAlias());
			}
			
		} catch (Exception e) {
			LOG.error("[" + this.getClass().getSimpleName() + 
				"]: Error retrieving allele frequency columns.");
			e.printStackTrace();
		}
		
		return output.toArray();
	}
	
	
	/**
	 * Populate a JComboBox with gene panels from the server.
	 * @param jcb the JComboBox with all gene panel names
	 */
	private void populateGenePanels(JComboBox jcb) {
		// Store the current selection before repopulating
		String originalSelection= (String) jcb.getSelectedItem();
		
		// clear the combobox of current items
		jcb.removeAllItems();
		
		// initialize the gene panel list, convert to list
		genePanelList= new ArrayList<String>(Arrays.asList(DiscoveryFindings.ALL_GENE_PANEL, 
			CUSTOM_GENE_PANEL_TEXT, DiscoveryFindings.ACMG_GENE_PANEL,
			DiscoveryFindings.CGD_GENE_PANEL));
		
		// Populate with the existing region lists
		try {
			RegionController controller= RegionController.getInstance();
			for (RegionSet r : controller.getRegionSets()) {
				genePanelList.add(r.getName());
			}
		} catch (Exception e) {
			LOG.error("[" + this.getClass().getSimpleName() + 
				"]: Error retrieving gene panels from server.");
			e.printStackTrace();
		}
		
		// Add the list of all gene panels to the combobox
		int maximumPanelNameLength= 30;
		for (String name : genePanelList) {
			if (name.length() <= maximumPanelNameLength) {
				jcb.addItem(name);
			} else {
				jcb.addItem(name.substring(0, maximumPanelNameLength) + "...");
			}
		}
		
		// set the default if nothing was selected before repopulating and set 
		// the previous selection otherwise
		if (originalSelection == null) {
			jcb.setSelectedItem(DiscoveryFindings.ALL_GENE_PANEL);
		} else {
			jcb.setSelectedItem(originalSelection);
		}
	}	
	
	
	/**
	 * Status bar to report potential errors in the processing of the current 
	 * patient. If there are no errors, no status is output. Unlike the progress
	 * status, this one is reported at the top of the patient panel, to increase
	 * the likelihood that it is seen by the user.
	 */
	private void errorStatusReport() {
		List<String> errorList= new ArrayList<String>();
		
		/* Check gender. */
		if (discFind.getGender().equals(ClientMiscUtils.GENDER_UNKNOWN)) {
			errorList.add("Patient gender is " + ClientMiscUtils.GENDER_UNKNOWN);
		}
		
		/* Check if allelic depth information is present. */
		if (!discFind.hasAllelicCoverage()) {
			errorList.add("Sample missing allelic coverage");
		}
		
		/* Add/remove the errorMessage text. */
		if (errorList.size() > 0) {
			errorMessage.removeAll(); // clear existing errors
			
			// Add a warnings title
			JLabel errorTitle= new JLabel("WARNING"); // note: doesn't wrap text if too long
			errorTitle.setForeground(Color.RED);
			errorTitle.setFont(new Font(errorTitle.getFont().getName(), Font.BOLD, 16));
			errorMessage.add(errorTitle, "wrap");
			
			patientPanelInsertPosition= 3; // push any upcoming new buttons down
			
			for (String e : errorList) {
				JLabel errorText= new JLabel(e); // note: doesn't wrap text if too long
				//errorText.setBackground(workview.getBackground());
				errorText.setForeground(Color.RED);
				errorText.setFont(new Font(errorText.getFont().getName(), Font.PLAIN, 15));
				
				errorMessage.add(errorText, "wrap");
			}
					
			// Add a little help message
			JButton errorMessageHelp= ViewUtil.getHelpButton("WARNING help", 
				"Warnings presented above may affect the quality of the variants " +
				"output by this workflow. When warnings are present, please note " +
				"that some results of this workflow may be incorrect.");
			errorMessage.add(errorMessageHelp);
			
			if (errorMessage.getParent() != patientPanel) { // if not already added
				patientPanel.add(errorMessage, "alignx center, wrap, gapy 20px", patientPanelInsertPosition - 2);
			}
		} else {
			patientPanelInsertPosition= 2; // back to original value
			if (errorMessage != null)
				patientPanel.remove(errorMessage);
		}
	}

	
	/**
	 * Get the DiscoveryFindings object from this Discovery app.
	 * @return The DiscoveryFindings object.
	 */
	public DiscoveryFindings getDiscoveryFindings() {
		return this.discFind;
	}


	/**
	 * Load the properties file if it exists.
	 */
	private void loadProperties() {
		try {
			File propertiesFile= new File(PROPERTIES_FILENAME);
			if (!propertiesFile.exists()) {
				/* Set the defaults. */
				// CGD date at time of coding corresponding to the download date of the embedded CGD file
				long defaultDate= (new GregorianCalendar(2014, Calendar.MARCH, 25)).getTimeInMillis();

				properties.setProperty("CGD_DB_date", Long.toString(defaultDate));
				properties.setProperty("CGD_DB_URL", DEFAULT_CGD_URL.toString());
				properties.setProperty("CGD_DB_filename", DEFAULT_CGD_FILENAME);

				String columnStringList= Arrays.toString(DEFAULT_COLUMNS);
				properties.setProperty("sortable_table_panel_columns" , columnStringList);
						
				properties.setProperty("variant_fetch_limit", Integer.toString(DEFAULT_FETCH_LIMIT));
				properties.setProperty("coverage_threshold", Integer.toString(DEFAULT_COVERAGE_THRESHOLD));
				properties.setProperty("het_ratio", Double.toString(DEFAULT_HET_RATIO));
				properties.setProperty("af_threshold", Double.toString(DEFAULT_AF_THRESHOLD));

				String afChooserStringList= "\"" + StringUtils.join(Arrays.asList(DEFAULT_AF_DB_LIST), "\"\t\"") + "\"";
				properties.setProperty("af_chooser_list", afChooserStringList);

				String mutationStringList= "\"" + StringUtils.join(Arrays.asList(DEFAULT_MUTATIONS), "\"\t\"") + "\"";
				properties.setProperty("mutation_list", mutationStringList);

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

			String s2= properties.getProperty("mutation_list");
			mutationArray= (s2.substring(1, s2.length() - 1)).split("\"\t\"");

			// Update CGD file if necessary
			updateCGD();
			copyCGD();	
		} catch (Exception e) {
			System.err.println("[" + this.getClass().getSimpleName() + "]: Error loading properties.");
			e.printStackTrace();
		}
	}

	
	/** 
	 * Save the current set of properties to the properties XML file.
	 */
	private void saveProperties() {
		try {
			properties.storeToXML(new FileOutputStream(PROPERTIES_FILENAME), 
				"Configuration options for Discovery app");
		} catch (Exception e) {
			System.err.println("[" + this.getClass().getSimpleName() + "]: Error saving properties XML file.");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Resets the entire set of properties to defaults.
	 */
	private void resetProperties() {
		File propertiesFile= new File(PROPERTIES_FILENAME);
		propertiesFile.delete(); // delete existing properties, switches to defaults
		loadProperties();
		
		workview.updateUI(); // May need to update the UI based on these properties.
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
				System.out.println("[" + this.getClass().getSimpleName() + "]: Existing CGD version from " + 
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
			System.err.println("[" + this.getClass().getSimpleName() + "]: Error when processing CGD URL.");
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
}
