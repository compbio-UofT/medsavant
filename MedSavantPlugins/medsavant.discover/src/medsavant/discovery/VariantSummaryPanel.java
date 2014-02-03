package medsavant.discovery;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.pane.CollapsiblePane;
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
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
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
	
	
	private CollapsiblePane otherIndividualsPane;
	private JPanel dnaIDPanel= new JPanel();
	
	/**
	 * Create a new scrollable VariantSummaryPanel.
	 * @param title The title of this panel
	 */
	public VariantSummaryPanel(String title) {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setViewportView(summaryPanel);
		
		summaryPanel.setLayout(new MigLayout("gapy 10"));
		JLabel t= new JLabel(title);
		t.setFont(new Font(t.getFont().getName(), Font.BOLD, 25));
		summaryPanel.add(t, "alignx center, span");
		summaryPanel.add(new JLabel(" "), "wrap");
		
		this.autoSize(PANE_WIDTH, PANE_HEIGHT, PANE_WIDTH_OFFSET);
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
	}
	
	
	/**
	 * Add an HGMD pane to the VariantSummaryPanel.
	 */
	public void addHGMDPane() {
	}
	
	
	/**
	 * Add a pane to the VariantSummaryPanel showing other individuals in this
	 * DB who have this variant.
	 */
	public void addOtherIndividualsPane() {
		otherIndividualsPane= new CollapsiblePane("Individuals with this variant");
		otherIndividualsPane.setLayout(new MigLayout("alignx center"));
		otherIndividualsPane.setStyle(CollapsiblePane.PLAIN_STYLE);
		otherIndividualsPane.setFocusPainted(false);
		otherIndividualsPane.collapse(true);
		otherIndividualsPane.setMinimumSize(new Dimension(PANE_WIDTH - PANE_WIDTH_OFFSET, 0));
		
		summaryPanel.add(otherIndividualsPane, "wrap");
	}
	
	
	/**
	 * Update the other individuals pane to show all DNA IDs with this variant.
	 * @param simpleVar The SimpleVariant object representing this variant.
	 */
	public void updateOtherIndividualsPane(SimpleVariant simpleVar) {
		/* Clear the existing other individuals pane and put status bar. */
		otherIndividualsPane.remove(dnaIDPanel);
		ProgressWheel pw= new ProgressWheel();
		pw.setIndeterminate(true);
		otherIndividualsPane.add(pw, "alignx center");
		
		/* Get the other individuals DNA IDs. */
		List<String> dnaIDList= this.getAllDNAIDsForVariant(simpleVar);
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
		otherIndividualsPane.collapse(false); // expand the collapsible pane
		String individuals= " individual ";
		if (dnaIDList.size() > 1) individuals= " individuals ";
		otherIndividualsPane.setTitle(dnaIDList.size() + individuals +"with this variant");
		for (String dnaID : dnaIDList) {
			dnaIDPanel.add(new JLabel(dnaID), "wrap");
		}
		otherIndividualsPane.add(dnaIDPanel);
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
	
}
