package medsavant.discovery;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.swing.ButtonStyle;
import com.jidesoft.swing.JideButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import medsavant.discovery.localDB.DiscoveryDBFunctions;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.ClinvarSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.HGMDSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SimpleVariant;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;


/**
 * A summary panel containing detailed information for a variant.
 * Information includes gene name, mutation type, allele frequency, 
 * as well as Clinvar and HGMD annotations with URLs to external resources.
 * 
 * @author rammar
 */
public class VariantSummaryPanel extends JScrollPane {
	
	private final static int DB_VARIANT_REQUEST_LIMIT= 5000;
	private static final Log LOG = LogFactory.getLog(MedSavantClient.class);
	private final String baseDBSNPUrl= "http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?searchType=adhoc_search&rs=";
	private final String baseClinvarUrl= "http://www.ncbi.nlm.nih.gov/clinvar/";
	private final String baseOMIMUrl= "http://www.omim.org/entry/";
	private final String basePubmedUrl= "http://www.ncbi.nlm.nih.gov/pubmed/";
	
	private TableSchema ts= ProjectController.getInstance().getCurrentVariantTableSchema();
	private VariantManagerAdapter vma= MedSavantClient.VariantManager;
	public JPanel summaryPanel= new JPanel();
	private int PANE_WIDTH= 380;
	private int PANE_WIDTH_OFFSET= 20;
	private int PANE_HEIGHT= 20; // minimum, but it'll stretch down - may need to change later
	
	private String currentGeneSymbol;
	private JLabel titleLabel;
	private CollapsiblePane otherIndividualsPane;
	private JPanel dnaIDPanel= new JPanel();
	private CollapsiblePane clinvarPane;
	private CollapsiblePane cgdPane;
	private CollapsiblePane hgmdPane;
	private String CGDPaneTitle= "Clinical Genomics Database (CGD) details";
	private String clinvarPaneTitle= "Clinvar details";
	private String HGMDPaneTitle= "HGMD details";
			
	
	/**
	 * Create a new scrollable VariantSummaryPanel.
	 * @param title The title of this panel
	 */
	public VariantSummaryPanel(String title) {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setViewportView(summaryPanel);
		
		summaryPanel.setLayout(new MigLayout("gapy 0px"));
		//summaryPanel.setBackground(ViewUtil.getSidebarColor());
		titleLabel= new JLabel(title);
		titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
		summaryPanel.add(titleLabel, "alignx center, span");
		summaryPanel.add(new JLabel(" "), "wrap");
		
		this.autoSize(PANE_WIDTH, PANE_HEIGHT, PANE_WIDTH_OFFSET);
	}
	
	
	/**
	 * Update the title for the VariantSummaryPanel.
	 * @param geneSymbol The new title string
	 */
	public void updateGeneSymbol(String geneSymbol) {
		titleLabel.setText(geneSymbol);
		currentGeneSymbol= geneSymbol;
		summaryPanel.revalidate();
	}
	
	
	/**
	 * Adds a custom annotation to the summary panel.
	 * @param annotation
	 * @param value
	 * @param optionalURL 
	 */
	public void addAnnotation(String annotation, String value, URL optionalURL) {
		JLabel annoLabel= new JLabel(annotation);
		JLabel valueLabel= new JLabel(value);
		summaryPanel.add(annoLabel, "alignx left");
		summaryPanel.add(valueLabel, "wrap");
	}
	
	
	/**
	 * Add a clinvar pane to the VariantSummaryPanel.
	 */
	public void addClinvarPane() {
		clinvarPane= getCollapsiblePane(clinvarPaneTitle);
		
		summaryPanel.add(clinvarPane, "wrap");
	}
	
	
	/**
	 * Update the clinvar pane.
	 * @param csi The ClinvarSubInspector
	 */
	public void updateClinvarPane(ClinvarSubInspector csi) {
		// clearing a collapsible pane leads to weird errors, so I'm removing it and adding it back.
		summaryPanel.remove(clinvarPane);
		
		clinvarPane= getCollapsiblePane(clinvarPaneTitle);
		
		String disease= csi.getDisease();
		// Convert all "_" to spaces, "|" to "; " and "\x2c" to ","
		disease= disease.replaceAll("_", " ");
		disease= disease.replaceAll("\\|", "; ");
		disease= disease.replaceAll("\\\\x2c", ",");
		
		JTextArea diseaseText= new JTextArea(disease);
		diseaseText.setLineWrap(true);
		diseaseText.setWrapStyleWord(true); // wrap after words, so as not to break words up
		diseaseText.setMinimumSize(new Dimension(PANE_WIDTH / 2, diseaseText.getPreferredSize().height));
		diseaseText.setBackground(summaryPanel.getBackground());
		
		// Process the omim allelic variant text for URL
		String omimAllelicVariantID_url= csi.getOmimAllelicVariantID().replaceAll("\\.", "#");		
		
		// Process the Clinvar accession text (get the root) for URL
		Matcher m= Pattern.compile("([^\\.]+)\\.").matcher(csi.getClinvarAccession());
		String accessionRoot= "";
		if (m.find())
			accessionRoot= m.group(1);
		
		/* Collapse the pane if the Clinvar entry is empty. Treat it as empty
		 * if both dbSNP AND clinvar accession are empty. */
		if (csi.getRsID().equals("") && csi.getClinvarAccession().equals(""))
			clinvarPane.collapse(true);
		
		// Add labels and buttons to the pane
		clinvarPane.add(getBoldLabel("Disease"));
		clinvarPane.add(diseaseText, "wrap");
		clinvarPane.add(getBoldLabel("dbSNP ID"));
		clinvarPane.add(getURLButton(csi.getRsID(), baseDBSNPUrl, csi.getRsID(), true), "wrap");
		clinvarPane.add(getBoldLabel("OMIM"));
		clinvarPane.add(getURLButton(csi.getOmimID(), baseOMIMUrl, csi.getOmimID(), true), "wrap");
		clinvarPane.add(getBoldLabel("OMIM Allelic Variant"));
		clinvarPane.add(getURLButton(csi.getOmimAllelicVariantID(), baseOMIMUrl, omimAllelicVariantID_url, false), "wrap");
		clinvarPane.add(getBoldLabel("Clinvar accession"));
		clinvarPane.add(getURLButton(csi.getClinvarAccession(), baseClinvarUrl, accessionRoot, true), "wrap");
		clinvarPane.add(getBoldLabel("Clinical significance"));
		clinvarPane.add(new JLabel(csi.getClnSig()), "wrap");
		
		summaryPanel.add(clinvarPane, "wrap");
	}
	
	
	/**
	 * Add an HGMD pane to the VariantSummaryPanel.
	 */
	public void addHGMDPane() {
		hgmdPane= getCollapsiblePane(HGMDPaneTitle);
		
		summaryPanel.add(hgmdPane, "wrap");
	}
	
	
	/**
	 * Update the HGMD pane.
	 * @param hsi The HGMDSubInspector
	 */
	public void updateHGMDPane(HGMDSubInspector hsi) {
		// clearing a collapsible pane leads to weird errors, so I'm removing it and adding it back.
		summaryPanel.remove(hgmdPane);
		
		hgmdPane= getCollapsiblePane(HGMDPaneTitle);
		
		// use JTextAreas when text runs over one line
		JTextArea diseaseText= new JTextArea(hsi.getDisease());
		diseaseText.setLineWrap(true);
		diseaseText.setWrapStyleWord(true); // wrap after words, so as not to break words up
		diseaseText.setMinimumSize(new Dimension(PANE_WIDTH / 2, diseaseText.getPreferredSize().height));
		diseaseText.setBackground(summaryPanel.getBackground());
		
		JTextArea commentsText= new JTextArea(hsi.getHGMDComments());
		commentsText.setLineWrap(true);
		commentsText.setWrapStyleWord(true); // wrap after words, so as not to break words up
		commentsText.setMinimumSize(new Dimension(PANE_WIDTH / 2, commentsText.getPreferredSize().height));
		commentsText.setBackground(summaryPanel.getBackground());
		
		/* Collapse the pane if the HGMD entry is empty. Treat it as empty
		 * if both disease AND omimid are empty. */
		if (hsi.getDisease().equals("") && hsi.getOmimID().equals(""))
			hgmdPane.collapse(true);
		
		// Add labels and buttons to the pane
		hgmdPane.add(getBoldLabel("Disease"));
		hgmdPane.add(diseaseText, "wrap");
		hgmdPane.add(getBoldLabel("OMIM"));
		hgmdPane.add(getURLButton(hsi.getOmimID(), baseOMIMUrl, hsi.getOmimID(), true), "wrap");
		hgmdPane.add(getBoldLabel("Pubmed"));
		hgmdPane.add(getURLButton(hsi.getPubmedID(), basePubmedUrl, hsi.getPubmedID(), true), "wrap");
		hgmdPane.add(getBoldLabel("HGMD comments"));
		hgmdPane.add(commentsText);
		
		summaryPanel.add(hgmdPane, "wrap");
	}
	
	
	/**
	 * Add a CGD pane to the VariantSummaryPanel.
	 */
	public void addCGDPane() {
		cgdPane= getCollapsiblePane(CGDPaneTitle);
		
		summaryPanel.add(cgdPane, "wrap");
	}
	
	
	/**
	 * Update the CGD pane.
	 * @param zygosity String for zygosity
	 * @param gender String for gender
	 * @param classification Classification as determined by another program (required to deal with compound hets).
	 */
	public void updateCGDPane(String zygosity, String gender, String classification){
		// clearing a collapsible pane leads to weird errors, so I'm removing it and adding it back.
		summaryPanel.remove(cgdPane);

		cgdPane= getCollapsiblePane(CGDPaneTitle);
		
		/* Get the CGD annotation. */
		List<String> variantClassification= DiscoveryDBFunctions.getClassification(currentGeneSymbol, zygosity, "", gender);
		String inheritance= variantClassification.get(1);
		String disease= DiscoveryDBFunctions.getDisease(currentGeneSymbol);
		
		/* If there is no CGD annotation, collapse the pane. */
		if (classification == null) cgdPane.collapse(true);
						
		cgdPane.add(getBoldLabel("Classification"));
		cgdPane.add(new JLabel(classification), "wrap");
		cgdPane.add(getBoldLabel("Inheritance"));
		cgdPane.add(new JLabel(inheritance), "wrap");
		cgdPane.add(getBoldLabel("Disease"));
		
		JTextArea diseaseText= new JTextArea(disease);
		diseaseText.setLineWrap(true);
		diseaseText.setWrapStyleWord(true); // wrap after words, so as not to break words up
		diseaseText.setMinimumSize(new Dimension(PANE_WIDTH / 2, diseaseText.getPreferredSize().height));
		diseaseText.setBackground(summaryPanel.getBackground());
		cgdPane.add(diseaseText, "wrap");
		
		summaryPanel.add(cgdPane, "wrap");
		
		// deal with some weird redrawing error when the collapsiblepane shrinks from previous size
		summaryPanel.updateUI();
	}
	
	
	/**
	 * Add a pane to the VariantSummaryPanel showing other individuals in this
	 * DB who have this variant.
	 */
	public void addOtherIndividualsPane() {
		otherIndividualsPane= getCollapsiblePane("Individuals with this variant");
		
		summaryPanel.add(otherIndividualsPane, "wrap");
	}
	
	
	/**
	 * Update the other individuals pane to show all DNA IDs with this variant.
	 * @param simpleVar The SimpleVariant object representing this variant.
	 */
	public void updateOtherIndividualsPane(final SimpleVariant simpleVar) {
		/* Clear the existing other individuals pane and put status bar. */
		otherIndividualsPane.remove(dnaIDPanel);
		final ProgressWheel pw= new ProgressWheel();
		pw.setIndeterminate(true);
		otherIndividualsPane.add(pw, "alignx center");
		//otherIndividualsPane.updateUI(); // causes random NullPointerExceptions; use revalidate() instead
		otherIndividualsPane.revalidate();
		
		
		/* Get the other individuals DNA IDs. */		
		MedSavantWorker otherIndividualsThread= new MedSavantWorker<Void>("updateOtherIndividualsPane")
		{
			@Override
			protected void showSuccess(Void result) {
			}

			@Override
			protected Void doInBackground() throws Exception {
				List<String> dnaIDList= getAllDNAIDsForVariant(simpleVar);
				Collections.sort(dnaIDList); // sort the DNA IDs so that a user can scroll through quickly
				
				int totalDBPatients= 0;
				try {
					totalDBPatients= MedSavantClient.PatientManager.getPatients(
						LoginController.getInstance().getSessionID(),
						ProjectController.getInstance().getCurrentProjectID()).size();
				} catch (Exception e) {
					LOG.error("Error processing total patient counter. " + e.toString());
					e.printStackTrace();
				}
				
				/* Update the other individuals pane. */
				dnaIDPanel= new JPanel(new MigLayout("alignx center, insets 0px"));
				otherIndividualsPane.remove(pw);
				
				String individuals= " individual ";
				if (dnaIDList.size() > 1) individuals= " individuals ";
				otherIndividualsPane.setTitle(dnaIDList.size() + individuals +
					"with this variant (" + totalDBPatients + " total)");
				for (String dnaID : dnaIDList) {
					dnaIDPanel.add(new JLabel(dnaID), "wrap");
				}
				otherIndividualsPane.add(dnaIDPanel);
				//otherIndividualsPane.updateUI(); // causes random NullPointerExceptions; use revalidate() instead
				otherIndividualsPane.revalidate();
				
				return null;
			}
		};
		
		otherIndividualsThread.execute();
	}
	
	
	/**
	 * Get a list of DNA IDs that have this variant.
	 * @param simpleVar The SimpleVariant object representing this variant.
	 * @return A list of DNA IDs (as strings) that have this variant.
	 */
	private List<String> getAllDNAIDsForVariant(SimpleVariant simpleVar) {
		List<String> allDNAIDs= new LinkedList<String>();
		
		/* Create the ComboCondition to identify all individuals with this variant. */
		ComboCondition cc= new ComboCondition(ComboCondition.Op.AND);

		cc.addCondition(BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.CHROM), simpleVar.getChromosome()));
		cc.addCondition(BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.START_POSITION), simpleVar.getStartPosition()));
		cc.addCondition(BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.END_POSITION), simpleVar.getEndPosition()));
		cc.addCondition(BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.REF), simpleVar.getReference()));
		cc.addCondition(BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.ALT), simpleVar.getAlternate()));
		
		Condition[][] conditionMatrix= new Condition[1][1];
		conditionMatrix[0][0]= cc;
		
		/* Get variants in chunks based on a request limit offset to save memory. */
		List<Object[]> allVariants= new ArrayList<Object[]>(DB_VARIANT_REQUEST_LIMIT);
		try {
			int fetchPosition= 0;
			List<Object[]> currentVariants= null;
			while (currentVariants == null || currentVariants.size() != 0 ){			
				currentVariants= vma.getVariants(LoginController.getInstance().getSessionID(),
					ProjectController.getInstance().getCurrentProjectID(),
					ReferenceController.getInstance().getCurrentReferenceID(),
					conditionMatrix,
					fetchPosition, DB_VARIANT_REQUEST_LIMIT);
				fetchPosition += DB_VARIANT_REQUEST_LIMIT;
				
				allVariants.addAll(currentVariants);
			}
		} catch (Exception e) {
			LOG.error("Error processing query. " + e.toString());
			e.printStackTrace();
		}
		
		for (Object[] row : allVariants) {
			allDNAIDs.add((String) row[BasicVariantColumns.INDEX_OF_DNA_ID]);
		}
		
		return allDNAIDs;
	}	
	
	
	/**
	 * Lets the scrollable component autosize itself optimally for the screen.
	 * @param width
	 * @param height
	 * @param offset 
	 */
	public void autoSize(int width, int height, int offset) {
		PANE_WIDTH= width;
		PANE_HEIGHT= height;
		PANE_WIDTH_OFFSET= offset;
		
		summaryPanel.setMinimumSize(new Dimension(width, height));
		this.setMinimumSize(new Dimension(summaryPanel.getMinimumSize().width + offset, height));
		this.setPreferredSize(new Dimension(summaryPanel.getMinimumSize().width, summaryPanel.getMaximumSize().height));
	}
	
	
	/**
	 * Get a new CollapsiblePane with the preferred presets.
	 * @param title Title of the pane
	 */
	private CollapsiblePane getCollapsiblePane(String title) {
		CollapsiblePane p= new CollapsiblePane(title);
		p.setLayout(new MigLayout("alignx center"));
		p.setStyle(CollapsiblePane.PLAIN_STYLE);
		p.setFocusPainted(false);
		p.collapse(false); // expand the collapsible pane
		p.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 0));
		
		return p;
	}
	
	
	/**
	 * Create a bold label with the specified text
	 * @param labelText The JLabel text
	 */
	private JLabel getBoldLabel(String labelText) {
		JLabel boldLabel= new JLabel(labelText);
		boldLabel.setFont(new Font(boldLabel.getFont().getName(), Font.BOLD, boldLabel.getFont().getSize()));
		return boldLabel;				
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
