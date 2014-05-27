package medsavant.discovery;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.swing.ButtonStyle;
import com.jidesoft.swing.JideButton;
import jannovar.common.VariantType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import medsavant.discovery.localDB.DiscoveryDBFunctions;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.ClinvarSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.HGMDSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SimpleVariant;
import org.ut.biolab.medsavant.shared.appdevapi.DBAnnotationColumns;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.serverapi.AnnotationManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;


/**
 * A summary panel containing detailed information for a variant.
 * Information includes gene name, mutation type, allele frequency, 
 * as well as Clinvar and HGMD annotations with URLs to external resources.
 * 
 * @author rammar
 */
public class VariantSummaryPanel extends JScrollPane {
	
	private static final int DB_VARIANT_REQUEST_LIMIT= 5000;
	private static final Log LOG = LogFactory.getLog(MedSavantClient.class);	
	private static final String baseDBSNPUrl= "http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?searchType=adhoc_search&rs=";
	private static final String baseClinvarUrl= "http://www.ncbi.nlm.nih.gov/clinvar/";
	private static final String baseOMIMUrl= "http://www.omim.org/entry/";
	private static final String basePubmedUrl= "http://www.ncbi.nlm.nih.gov/pubmed/";
	private static final Color WARNING_ORANGE= new Color(249, 114, 85); // other option: (242, 103, 34);
	private static final String POLYPHEN2HUMVAR_COLUMN= DBAnnotationColumns.POLYPHEN2HUMVAR;
	private static final String SIFT_COLUMN= DBAnnotationColumns.SIFT;
	private static final String PHYLOP_COLUMN= DBAnnotationColumns.PHYLOP;
	private static final List<String> predictorColumns= Arrays.asList(
		POLYPHEN2HUMVAR_COLUMN, SIFT_COLUMN,PHYLOP_COLUMN
	); // for now these are hard-coded, but this may be updated later
	private static final double POLYPHEN2HUMVAR_PROBABLY_DAMAGING_THRESHOLD= 0.90; // above this
	private static final double POLYPHEN2HUMVAR_POSSIBLY_DAMAGING_THRESHOLD= 0.80; // above this
	private static final double SIFT_PROBABLY_DAMAGING_THRESHOLD= 0.05; // below this
	private static final String PROBABLY_DAMAGING= "probably damaging";
	private static final String POSSIBLY_DAMAGING= "possibly damaging";
	
	/* LOF mutations to colour as red, potentially harmful mutations to colour
	 * as orange. */
	private static final List<String> RED_MUTATIONS= Arrays.asList(
		VariantType.FS_DELETION.toString(), VariantType.FS_DUPLICATION.toString(),
		VariantType.FS_INSERTION.toString(), VariantType.FS_SUBSTITUTION.toString(),
		VariantType.STOPGAIN.toString(), VariantType.SPLICING.toString(),
		VariantType.ERROR.toString() // Errors are red as well, but aren't functional annotations
	);
	private static final List<String> ORANGE_MUTATIONS= Arrays.asList(
		VariantType.MISSENSE.toString(), VariantType.NON_FS_DELETION.toString(),
		VariantType.NON_FS_DUPLICATION.toString(), VariantType.NON_FS_INSERTION.toString(),
		VariantType.NON_FS_SUBSTITUTION.toString(), VariantType.START_LOSS.toString()
	);
	
	private TableSchema ts= ProjectController.getInstance().getCurrentVariantTableSchema();
	private VariantManagerAdapter vma= MedSavantClient.VariantManager;
	public JPanel summaryPanel= new JPanel();
	private int PANE_WIDTH= 380;
	private int PANE_WIDTH_OFFSET= 20;
	private int PANE_HEIGHT= 20; // minimum, but it'll stretch down - may need to change later
	
	private String currentGeneSymbol;
	private JLabel titleLabel;
	private String cgdPaneTitle= "Clinical Genomics Database (CGD) details";
	private String clinvarPaneTitle= "Clinvar details";
	private String hgmdPaneTitle= "HGMD details";
	private String otherIndividualsPaneTitle= "Individuals with this variant";
	private MedSavantWorker otherIndividualsThread;
	private JPanel variantPanel;
	private List<String> alleleFrequencyColumns;
	private String afPaneTitle= "Allele frequency details";
	private String predictorPaneTitle= "Variant harmfulness prediction";
	private List<JComponent> clearList= new LinkedList<JComponent>();
	
	//variant properties
	private String chromosome;
	private int start;
	private int end;
	private String reference;
	private String alternate;
	private String zygosity;
	private List<String> mutationEffects;
	private List<String> mutationAnnotations;
			
	
	/**
	 * Create a new scrollable VariantSummaryPanel.
	 * @param title The title of this panel
	 */
	public VariantSummaryPanel(String title) {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setViewportView(summaryPanel);
		this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.variantPanel= new JPanel();
		
		summaryPanel.setLayout(new MigLayout("gapy 0px"));
		variantPanel.setLayout(new MigLayout());
		//summaryPanel.setBackground(ViewUtil.getSidebarColor());
		titleLabel= new JLabel(title);
		titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
		summaryPanel.add(titleLabel, "alignx center, span");
		summaryPanel.add(variantPanel, "alignx center, span");
		summaryPanel.add(new JLabel(" "), "wrap"); // spacer
		
		this.autoSize(PANE_WIDTH, PANE_HEIGHT, PANE_WIDTH_OFFSET);
		
		this.alleleFrequencyColumns= getAlleleFrequencyColumns();
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
	 * Add or update the annotation for this variant.
	 * @param row The entire row entry from the DB for this variant.
	 * @param header The table header row
	 */
	public void updateAnnotation(Object[] row, List<String> header) {
		/* Parse the information. */
		this.chromosome= (String) row[BasicVariantColumns.INDEX_OF_CHROM];
		this.start= ((Integer) row[BasicVariantColumns.INDEX_OF_START_POSITION]).intValue();
		this.end= ((Integer) row[BasicVariantColumns.INDEX_OF_END_POSITION]).intValue();
		this.reference= (String) row[BasicVariantColumns.INDEX_OF_REF];
		this.alternate= (String) row[BasicVariantColumns.INDEX_OF_ALT];
		this.zygosity= (String) row[BasicVariantColumns.INDEX_OF_ZYGOSITY];
		
		// mutation details can be comma-delimited lists
		String effects= (String) row[header.indexOf(
			BasicVariantColumns.JANNOVAR_EFFECT.getAlias())];
		String annotations= (String) row[header.indexOf(
			BasicVariantColumns.JANNOVAR_SYMBOL.getAlias())];
		
		this.mutationEffects= Arrays.asList(effects.split(","));
		this.mutationAnnotations= Arrays.asList(annotations.split(","));
		
		/* Clean up old components then add these details to the variant panel. */
		variantPanel.removeAll();
		variantPanel.add(getBoldLabel(chromosome + ": " + start + "-" + end), "span");
		variantPanel.add(getBoldLabel("Reference"));
		variantPanel.add(new JLabel(reference), "split 2, wrap");
		variantPanel.add(getBoldLabel("Alternate"));
		variantPanel.add(new JLabel(alternate), "split 2, wrap");
		variantPanel.add(getBoldLabel("Zygosity"));
		variantPanel.add(new JLabel(zygosity), "split 2, wrap");
		
		// try-catch block in case mutationEffects and mutationAnnotations are
		// not the same size
		try {
			
			for (int i= 0; i != mutationEffects.size(); ++i) {
				String currentEffect= mutationEffects.get(i);
				String currentAnnotation= mutationAnnotations.get(i);
				Color mutationColor= Color.DARK_GRAY; // default colour
				if (RED_MUTATIONS.contains(currentEffect)) {
					mutationColor= Color.RED;
				} else if (ORANGE_MUTATIONS.contains(currentEffect)) {
					mutationColor= WARNING_ORANGE;
				}
				
				variantPanel.add(getColorTextArea(currentEffect + ": " + 
					currentAnnotation, mutationColor), "span");
			}
			
		} catch (Exception ex) {
			//DialogUtils.displayError(ex.getMessage());
			ex.printStackTrace();
		}
		
		variantPanel.revalidate();
		
		// deal with some weird redrawing error when the collapsiblepane shrinks from previous size
		summaryPanel.updateUI();
	}
	
	
	/**
	 * Add or update the allele frequency pane for this variant.
	 * @param row The entire row entry from the DB for this variant.
	 * @param header The table header row
	 */
	public void updateAlleleFrequencyPane(Object[] row, List<String> header) {		
		CollapsiblePane afPane= getCollapsiblePane(afPaneTitle);
		
		for (String afColumn : alleleFrequencyColumns) {			
			String afValue= "N/A";
			if ((BigDecimal) row[header.indexOf(afColumn)] != null)
				afValue= ((BigDecimal) row[header.indexOf(afColumn)]).toString();
			
			
			afPane.add(getBoldLabel(afColumn));
			afPane.add(new JLabel(afValue), "wrap");
		}
		
		summaryPanel.add(afPane, "wrap");
		
		// add to list of components
		clearList.add(afPane);
		
		// deal with some weird redrawing error when the collapsiblepane shrinks from previous size
		summaryPanel.updateUI();
	}

	
	/**
	 * Add or update the harmfulness predictors pane for this variant.
	 * @param row The entire row entry from the DB for this variant.
	 * @param header The table header row
	 */
	public void updateHarmfulnessPredictorsPane(Object[] row, List<String> header) {		
		CollapsiblePane predictorPane= getCollapsiblePane(predictorPaneTitle);
		
		for (String predictorColumn : predictorColumns) {
			String predictorValue= "N/A";
			JLabel predictorJLabel= getColorLabel(predictorValue, Color.DARK_GRAY);
			
			/* Check if there is a value and then evaluate it before outputting. */
			if ((BigDecimal) row[header.indexOf(predictorColumn)] != null) {
				predictorValue= ((BigDecimal) row[header.indexOf(predictorColumn)]).toString();
				predictorJLabel= getColorLabel(predictorValue, Color.DARK_GRAY);
				
				/* Classify the variants.
				 * SIFT < 0.05 => probably damaging
				 * Polyphen2 >= 0.90 => probably damaging
				 * 0.90 > Polyphen2 >= 0.80 => probably damanging	 */
				if (predictorColumn.equals(SIFT_COLUMN) &&
					Double.parseDouble(predictorValue) < SIFT_PROBABLY_DAMAGING_THRESHOLD) {
					predictorJLabel= getColorLabel(predictorValue + " " + PROBABLY_DAMAGING, Color.RED);
				} else if (predictorColumn.equals(POLYPHEN2HUMVAR_COLUMN) &&
					Double.parseDouble(predictorValue) >= POLYPHEN2HUMVAR_PROBABLY_DAMAGING_THRESHOLD) {
					predictorJLabel= getColorLabel(predictorValue + " " + PROBABLY_DAMAGING, Color.RED);
				} else if (predictorColumn.equals(POLYPHEN2HUMVAR_COLUMN) &&
					Double.parseDouble(predictorValue) >= POLYPHEN2HUMVAR_POSSIBLY_DAMAGING_THRESHOLD) {
					predictorJLabel= getColorLabel(predictorValue + " " + POSSIBLY_DAMAGING, WARNING_ORANGE);
				}
			}
			
			predictorPane.add(getBoldLabel(predictorColumn));
			predictorPane.add(predictorJLabel, "wrap");
		}
		
		
		summaryPanel.add(predictorPane, "wrap");
		
		// add to list of components
		clearList.add(predictorPane);
		
		// deal with some weird redrawing error when the collapsiblepane shrinks from previous size
		summaryPanel.updateUI();
	}
	
	
	/**
	 * Add or update the clinvar pane.
	 * @param csi The ClinvarSubInspector
	 */
	public void updateClinvarPane(ClinvarSubInspector csi) {	
		CollapsiblePane clinvarPane= getCollapsiblePane(clinvarPaneTitle);
		
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
		if (!(csi.getRsID().equals("") && csi.getClinvarAccession().equals(""))) {
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
			
			// add to list of components
			clearList.add(clinvarPane);
		}
		
		// deal with some weird redrawing error when the collapsiblepane shrinks from previous size
		summaryPanel.updateUI();
	}
	
	
	/**
	 * Add or update the HGMD pane.
	 * @param hsi The HGMDSubInspector
	 */
	public void updateHGMDPane(HGMDSubInspector hsi) {
		CollapsiblePane hgmdPane= getCollapsiblePane(hgmdPaneTitle);
		
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
		
		/* Don't add the pane if the HGMD entry is empty. Treat it as empty
		 * if both disease AND omimid are empty. */
		if (!(hsi.getDisease().equals("") && hsi.getOmimID().equals(""))) {
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
			
			// add to list of components
			clearList.add(hgmdPane);
		}
		
		// deal with some weird redrawing error when the collapsiblepane shrinks from previous size
		summaryPanel.updateUI();
	}
	
	
	/**
	 * Add of update the CGD pane.
	 * @param zygosity String for zygosity
	 * @param gender String for gender
	 * @param classification Classification as determined by another program (required to deal with compound hets).
	 */
	public void updateCGDPane(String zygosity, String gender, String classification){
		CollapsiblePane cgdPane= getCollapsiblePane(cgdPaneTitle);
		
		/* Get the CGD annotation. */
		List<String> variantClassification= DiscoveryDBFunctions.getClassification(currentGeneSymbol, zygosity, "", gender);
		String inheritance= variantClassification.get(1);
		String disease= DiscoveryDBFunctions.getDisease(currentGeneSymbol);
		
		/* If there is no CGD annotation, collapse the pane. */
		if (classification != null) {
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
			
			// add to list of components
			clearList.add(cgdPane);
		}
		
		// deal with some weird redrawing error when the collapsiblepane shrinks from previous size
		summaryPanel.updateUI();
	}
	
	
	/**
	 * Add or update the other individuals pane to show all DNA IDs with this variant.
	 * @param simpleVar The SimpleVariant object representing this variant.
	 */
	public void updateOtherIndividualsPane(final SimpleVariant simpleVar) {			
		final CollapsiblePane otherIndividualsPane= getCollapsiblePane(otherIndividualsPaneTitle);
		summaryPanel.add(otherIndividualsPane, "wrap");
		
		// add to list of components
		clearList.add(otherIndividualsPane);
		
		final ProgressWheel pw= new ProgressWheel();
		pw.setIndeterminate(true);
		otherIndividualsPane.add(pw, "alignx center");
		//otherIndividualsPane.updateUI(); // causes random NullPointerExceptions; use revalidate() instead
		otherIndividualsPane.revalidate();
		
		/* Get the other individuals DNA IDs. */		
		// add a random number to differentiate the threads by pagename
		otherIndividualsThread= new MedSavantWorker<Void>("updateOtherIndividualsPane" + Double.toString(Math.random()))
		{
			@Override
			protected void showSuccess(Void result) {
			}

			@Override
			protected Void doInBackground() throws Exception {
				JPanel dnaIDPanel;
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
					"with this variant (" + totalDBPatients + " individuals in database)");
				
				for (String dnaID : dnaIDList) {
					dnaIDPanel.add(new JLabel(dnaID), "wrap");
				}
				
				/* Collapse the pane because it takes up a lot of space. The 
				 * pane title should be informative enough to start. */
				otherIndividualsPane.collapse(true);
				otherIndividualsPane.add(dnaIDPanel);
				otherIndividualsPane.revalidate();
				
				return null;
			}
		};
		otherIndividualsThread.execute();
		
		//summaryPanel.revalidate();
		
		// deal with some weird redrawing error when the collapsiblepane shrinks from previous size
		summaryPanel.updateUI();
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
	 * Create a bold coloured label with the specified text
	 * @param labelText The JLabel text
	 * @param c The text color
	 */
	private JLabel getColorLabel(String labelText, Color c) {
		JLabel colorLabel= getBoldLabel(labelText);
		colorLabel.setForeground(c);
		return colorLabel;
	}
	
	
	/**
	 * Creates a coloured text area (to wrap lines) with the specified text.
	 * @param text The text
	 * @param c The text color
	 */
	private JTextArea getColorTextArea(String text, Color c) {
		JTextArea colorText= new JTextArea(text);
		colorText.setLineWrap(true);
		colorText.setWrapStyleWord(true); // wrap after words, so as not to break words up
		colorText.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH/4, colorText.getPreferredSize().height));
		colorText.setBackground(summaryPanel.getBackground());
		colorText.setForeground(c);
		colorText.setFont(new Font(colorText.getFont().getName(), Font.BOLD,
			colorText.getFont().getSize() + 1)); // add 1 to the font size
		
		return colorText;
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
	
	
	/**
	 * Adds a custom panel to the summary panel.
	 * @param custom Title of custom pane
	 * @return The new custom CollapsiblePane to be modified.
	 */
	public CollapsiblePane addCustomAnnotation(String custom) {
		CollapsiblePane customPane= getCollapsiblePane(custom);
		
		summaryPanel.add(customPane, "wrap");
		
		// add to list of components
		clearList.add(customPane);
		
		return customPane;
	}
	
	
	/**
	 * Get the parent summary panel for the VariantSummaryPanel. 
	 * @return The summary panel
	 */
	public JPanel getSummaryPanel() {
		return summaryPanel;
	}
	
	
	/**
	 * Get the Allele Frequency annotation columns from this database.
	 * @return a list of the allele frequency column names.
	 */
	private List<String> getAlleleFrequencyColumns() {
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
	
		return output;
	}
	
	
	/**
	 * Remove all components from main pane.
	 */
	public void clearSummaryPane() {
		for (JComponent jc : clearList) {
			if (jc != null)
				summaryPanel.remove(jc);
		}
		
		// Ensure the list is empty
		clearList.clear(); // should be switched to removeAll() for Java 1.7
	}
}