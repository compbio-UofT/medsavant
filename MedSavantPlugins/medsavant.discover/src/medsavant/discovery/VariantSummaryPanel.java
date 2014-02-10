package medsavant.discovery;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.swing.JideButton;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.ClinvarSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SimpleVariant;
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
			
	
	/**
	 * Create a new scrollable VariantSummaryPanel.
	 * @param title The title of this panel
	 */
	public VariantSummaryPanel(String title) {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setViewportView(summaryPanel);
		
		summaryPanel.setLayout(new MigLayout("gapy 0px"));
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
		clinvarPane= getCollapsiblePane("Clinvar details");
		
		summaryPanel.add(clinvarPane, "wrap");
	}
	
	
	/**
	 * Update the clinvar pane.
	 * @param csi The ClinvarSubInspector
	 */
	public void updateClinvarPane(ClinvarSubInspector csi) {
		// clearing a collapsible pane leads to weird errors, so I'm removing it and adding it back.
		summaryPanel.remove(clinvarPane);
		
		clinvarPane= getCollapsiblePane("Clinvar details");
		
		JTextArea diseaseText= new JTextArea(csi.getDisease());
		diseaseText.setLineWrap(true);
		diseaseText.setWrapStyleWord(true); // wrap after words, so as not to break words up
		diseaseText.setMinimumSize(new Dimension(PANE_WIDTH / 2, diseaseText.getPreferredSize().height));
		diseaseText.setBackground(summaryPanel.getBackground());
		
		clinvarPane.add(getBoldLabel("Disease"));
		clinvarPane.add(diseaseText, "wrap");
		clinvarPane.add(getBoldLabel("dbSNP ID"));
		clinvarPane.add(new JideButton(csi.getRsID()), "wrap");
		clinvarPane.add(getBoldLabel("OMIM ID"));
		clinvarPane.add(new JideButton(csi.getOmimID()), "wrap");
		clinvarPane.add(getBoldLabel("OMIM Allelic Variant"));
		clinvarPane.add(new JideButton(csi.getOmimAllelicVariantID()), "wrap");
		clinvarPane.add(getBoldLabel("Clinvar accession"));
		clinvarPane.add(new JideButton(csi.getClinvarAccession()), "wrap");
		clinvarPane.add(getBoldLabel("Clinical significance"));
		clinvarPane.add(new JLabel(csi.getClnSig()), "wrap");
		
		summaryPanel.add(clinvarPane, "wrap");
	}
	
	
	/**
	 * Add an HGMD pane to the VariantSummaryPanel.
	 */
	public void addHGMDPane() {
		hgmdPane= getCollapsiblePane("HGMD details");
		
		summaryPanel.add(hgmdPane, "wrap");
	}
	
	
	/**
	 * Add a CGD pane to the VariantSummaryPanel.
	 */
	public void addCGDPane() {
		cgdPane= getCollapsiblePane("Clinical Genomics Database (CGD) details");
		
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

		cgdPane= getCollapsiblePane("Clinical Genomics Database (CGD) details");
		
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

		cc.addCondition(BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.CHROM), simpleVar.chr));
		cc.addCondition(BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.START_POSITION), simpleVar.start_pos));
		cc.addCondition(BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.END_POSITION), simpleVar.end_pos));
		cc.addCondition(BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.REF), simpleVar.ref));
		cc.addCondition(BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.ALT), simpleVar.alt));
		
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
}
