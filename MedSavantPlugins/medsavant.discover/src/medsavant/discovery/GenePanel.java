package medsavant.discovery;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.medsavant.complex.GenesConditionGenerator;
import org.ut.biolab.mfiume.query.view.SearchConditionEditorView;
import org.ut.biolab.mfiume.query.view.SearchConditionPanel;

/**
 * Creates a gene panel entry form.
 * 
 * @author rammar
 */
public class GenePanel {
	
	private static final String DONE_TEXT= "Done";
	private static final String CLEAR_TEXT= "Clear";
	
	private SearchConditionItem sci;
	/* NOTE:
	 * Replace 'OntologyConditionGenerator' with 'GenesConditionGenerator' to try out the gene query pane. */
	//private ComprehensiveConditionGenerator ccg = new OntologyConditionGenerator(OntologyType.HPO);
	private GenesConditionGenerator ccg;
	private SearchConditionEditorView scev;
	private SearchConditionPanel scp;
	private String encodedSearch;
	private DiscoveryPanel dp;
	private ComboCondition genePanelCC= new ComboCondition(ComboCondition.Op.OR); // initialize to something
	
	
	/**
	 * Create a new GenePanel entry form.
	 * @param dp The DiscoveryPanel that is using this GenePanel
	 */
	public GenePanel(DiscoveryPanel dp) {
		this.dp= dp;
		
		sci= new SearchConditionItem("", null);
		ccg= new GenesConditionGenerator();
		scev= ccg.getViewGeneratorForItem(sci);
		scp= new SearchConditionPanel(scev, null);
	}
	
		
	/**
	 * Define exception for GenePanel.
	 */
	private class GenePanelException extends Exception {
		public GenePanelException(String message) {
			super(message);
		}
	}
	
	
	/**
	 * Create a custom gene panel JPanel using GenesConditionGenerator.
	 * @return the JPanel responsible for gene panel entry and saving.
	 */
	public JPanel getPanel() {	
		/* The list of gene objects created from gene symbols. */
		final List<Gene> geneList= new ArrayList<Gene>();
		
		/* Button to indicate the gene panel selection is ready for variant filtering. */
		final JButton doneButton = new JButton(DONE_TEXT);
		doneButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (doneButton.getText().equals(DONE_TEXT)) {
					doneButton.setText(CLEAR_TEXT);
					
					try {
						/* Save changes: this saves the users selections so next time
						 * the dialog pops up, those same selections will be checked.
						 * For most SearchConditionEditorViews, this isn't necessary, 
						 * but it is necessary for some (e.g. GenesConditionGenerator).
						 * Best to always call it. */
						if (scev.saveChanges()) {
							/* Get the gene symbols delimited by ";" */
							encodedSearch= sci.getSearchConditionEncoding();

							/* Look up the chromosomal coordinates for each gene before saving */
							String[] genes= encodedSearch.split(";");

							DiscoveryFindings discFind= dp.getDiscoveryFindings();
							Map<String, String> columns= discFind.dbAliasToColumn;

							// Use ORs between genes for a gene panel
							genePanelCC= new ComboCondition(ComboCondition.Op.OR);

							/* Add a condition for each gene symbol. */
							for (String geneSymbol : genes) {
								Gene g= GeneSetController.getInstance().getGene(geneSymbol);	
								if (g != null) {
									geneList.add(g);

									genePanelCC.addCondition(BinaryCondition.iLike(
										discFind.ts.getDBColumn(columns.get(
											BasicVariantColumns.JANNOVAR_SYMBOL.getAlias())),
										"%" + geneSymbol + "%"));

								} else { // Gene symbol not found in our DB
									throw new GenePanelException(
										"Could not retrieve details for gene symbol: " + geneSymbol);
								}
							}
						}
					} catch (Exception ex) {
						DialogUtils.displayError(ex.getMessage());
					}
					
				} else if (doneButton.getText().equals(CLEAR_TEXT)) {
					doneButton.setText(DONE_TEXT);
					scp.revalidate();
					
					// Reset the ComboCondition to empty
					genePanelCC= new ComboCondition(ComboCondition.Op.OR);
				}
			}
		});
		
		/* Add the gene panel title text field when hovering over the save button. */
		JButton saveButton= new JButton("Save panel");
		final String defaultGenePanelTitleText= "Gene panel name?";
		final JTextField genePanelTitle= new JTextField(defaultGenePanelTitleText);
		genePanelTitle.setForeground(Color.RED);
		saveButton.addMouseMotionListener(
			new MouseMotionListener() {
				@Override
				public void mouseMoved(MouseEvent me) {
					genePanelTitle.addMouseListener(
						new MouseListener() {
							@Override
							public void mouseClicked(MouseEvent me) {
								genePanelTitle.setText("");
								genePanelTitle.setForeground(Color.BLACK);
							}
							// remaining methods included but do nothing
							@Override public void mouseExited(MouseEvent me) {}
							@Override public void mouseReleased(MouseEvent me) {}
							@Override public void mousePressed(MouseEvent me) {}
							@Override public void mouseEntered(MouseEvent me) {}
						}
					);
					
					
					// Add the title bar on top of the buttons if it hasn't
					// already been added
					if (genePanelTitle.getParent() != scp) {
						scp.add(genePanelTitle, 2);
						scp.revalidate();
					}
				}
				
				@Override public void mouseDragged(MouseEvent me) {}
			}
		);
		
		/* Save the gene panel as a region list on the server, as long as there
		 * is a specified panel title. */
		saveButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {				
					/* Save the list of gene objects as a region list. */
					for (Gene g : geneList) {
						/////////////////////////////////// FILL IN HERE
						/// Save as a region list
					}
				}
			}
		);
		
		/* Add components to the panel. */
		scp.getButtonPanel().add(doneButton);
		scp.getButtonPanel().add(saveButton);
		scp.loadEditorViewInBackground(null);
		
		return scp;
	}
	
	
	/**
	 * Get the GenePanel ComboCondition.
	 * @return the ComboCondition corresponding to this gene panel
	 */
	public ComboCondition getComboCondition() {
		return this.genePanelCC;
	}
	
	
	/**
	 * Set the background color for this panel.
	 * @param c The background color.
	 */
	public void setBackground(Color c) {
		/* Change the background colour to the workview one. */
		scev.setBackground(c);
		scp.setBackground(c);
		scp.getButtonPanel().setBackground(c);
	}
}
