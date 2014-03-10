package medsavant.discovery;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.query.SearchConditionItem;
import org.ut.biolab.medsavant.client.query.medsavant.complex.ComprehensiveConditionGenerator;
import org.ut.biolab.medsavant.client.query.medsavant.complex.GenesConditionGenerator;
import org.ut.biolab.medsavant.client.query.medsavant.complex.OntologyConditionGenerator;
import org.ut.biolab.medsavant.client.query.view.GeneSearchConditionEditorView;
import org.ut.biolab.medsavant.client.query.view.SearchConditionEditorView;
import org.ut.biolab.medsavant.client.query.view.SearchConditionPanel;
import org.ut.biolab.medsavant.client.region.RegionController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.importing.BEDFormat;
import org.ut.biolab.medsavant.shared.importing.FileFormat;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.shared.model.RegionSet;


/**
 * Creates a gene panel entry form.
 * 
 * @author rammar
 */
public class GenePanel {
	
	public static final String PANEL= "panel";
	public static final String HPO= "hpo";
	
	private static final String DONE_TEXT= "Apply";
	private static final String CLEAR_TEXT= "Clear";
	private static final String DEFAULT_GENE_PANEL_TITLE_TEXT= "Gene panel name?";
	
	private String panelType;
	private ComprehensiveConditionGenerator ccg;
	private SearchConditionItem sci;
	private SearchConditionEditorView scev;
	private SearchConditionPanel scp;
	private String encodedSearch;
	private ComboCondition genePanelCC= new ComboCondition(ComboCondition.Op.OR); // initialize to something
	private JButton doneButton;
	private RegionController controller= RegionController.getInstance();
	private List<Gene> geneList= new ArrayList<Gene>(); // The list of gene objects created from gene symbols.
	private JTextField genePanelTitle= new JTextField(DEFAULT_GENE_PANEL_TITLE_TEXT);
	private Map<String, String> columns= new DiscoveryFindings(null).getDbToHumanReadableMap();
	private TableSchema ts= ProjectController.getInstance().getCurrentVariantTableSchema();
	private OntologyConditionGenerator ocg;
	private JDialog displayDialog;
	
	
	/**
	 * Create a new GenePanel entry form.
	 * @param type Type of gene panel. Can be GenePanel.PANEL or GenePanel.HPO. If wrong type is specified, defaults to gene panel
	 */
	public GenePanel(String type) {
		panelType= type;
		
		sci= new SearchConditionItem("", null);
		
		if (type.equals(GenePanel.HPO)) {
			ccg= new OntologyConditionGenerator(OntologyType.HPO);
			ocg= new OntologyConditionGenerator(OntologyType.HPO);
		} else { //default to gene panel
			ccg= new GenesConditionGenerator();
		}
		
		scev= ccg.getViewGeneratorForItem(sci);
		createSearchConditionPanel();
	}
	
	
	/**
	 * Create a new GenePanel entry form designed for display in a JDialog.
	 * @param jd The JDialog where it will be displayed
	 * @param type Type of gene panel. Can be GenePanel.PANEL or GenePanel.HPO. If wrong type is specified, defaults to gene panel
	 */
	public GenePanel(String type, JDialog jd) {
		this(type);
		
		this.displayDialog= jd;
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
	 * Return the custom gene panel JPanel using GenesConditionGenerator.
	 * @return the JPanel responsible for gene panel entry and saving.
	 */
	public JPanel getSearchConditionPanel() {
		if (scp == null) {
			createSearchConditionPanel();
		} else {
			// reset doneButton from clear to done
			doneButton.setText(DONE_TEXT);
		}
		
		return scp;
	}
	
	
	/**
	 * Create a custom gene panel JPanel using GenesConditionGenerator.
	 */
	private void createSearchConditionPanel() {
		scp= new SearchConditionPanel(scev, null); // initialize here, not construct
		
		// components of this panel
		doneButton= new JButton(DONE_TEXT);
		genePanelTitle.setForeground(Color.RED);
		
		/* Button to indicate the gene panel selection is ready for variant filtering. */
		doneButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (doneButton.getText().equals(DONE_TEXT)) {
					doneAction();
					
				} else if (doneButton.getText().equals(CLEAR_TEXT)) {
					clearAction();
				}
			}
		});
		
		/* Add the gene panel title text field when hovering over the save button. */
		JButton saveButton= new JButton("Save panel");
		if (panelType.equals(GenePanel.HPO))
			saveButton.setText("Save as gene panel"); // to reduce ambiguity
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
					/* Perform done button actions before saving. */
					doneAction();
					
					/* Save the panel as a region list on the server if the name
					 * has been assigned but doesn't exist already. */
					if (!genePanelTitle.getText().equals(DEFAULT_GENE_PANEL_TITLE_TEXT) &&
						!genePanelTitle.getText().equals("") &&  // can be replaced with regex for whitespace.
						validateGenePanelTitle(genePanelTitle.getText())) {
						saveGenePanel(genePanelTitle.getText(), geneList);
					}
				}
			}
		);
		
		/* Add components to the panel. */
		scp.getButtonPanel().add(saveButton);
		scp.getButtonPanel().add(doneButton);
		
		/* Retrieve the panel. If displaying in a JDialog, pass a non-null
		 * runnable to pack the JDialog. */
		if (displayDialog == null) {
			scp.loadEditorViewInBackground(null);
		} else {
			scp.loadEditorViewInBackground(
				new Runnable() {
                    @Override
                    public void run() {
                        displayDialog.pack();
                        displayDialog.invalidate(); // not sure if this is necessary
                    }
                }
			);
		}
	}
	
	
	/**
	 * Action performed when "Done" button is clicked.
	 */
	private void doneAction() {
		// Clear the current list of Genes
		geneList.clear();
		
		try {
			/* Save changes: this saves the users selections so next time
			 * the dialog pops up, those same selections will be checked.
			 * For most SearchConditionEditorViews, this isn't necessary, 
			 * but it is necessary for some (e.g. GenesConditionGenerator).
			 * Best to always call it. */
			if (scev.saveChanges()) {
				/* Get the gene symbols delimited by ";" */
				encodedSearch= sci.getSearchConditionEncoding();

				/* Only update the button if the string of genes has length > 0. */
				if (encodedSearch.length() != 0)
					doneButton.setText(CLEAR_TEXT);
				
				/* Look up the chromosomal coordinates for each gene before saving */
				String[] genes= null;
				if (panelType.equals(GenePanel.PANEL)) {
					genes= encodedSearch.split(";");
				} else if (panelType.equals(GenePanel.HPO)) {
					// retrieve genes for each ontology term
					List<GenomicRegion> regionList= ocg.getRegionsFromEncoding(encodedSearch);
					genes= new String[regionList.size()];
					for (int i= 0; i != regionList.size(); ++i) {
						genes[i]= regionList.get(i).getName();
					}
				}

				// Use ORs between genes for a gene panel
				genePanelCC= new ComboCondition(ComboCondition.Op.OR);

				/* Add a condition for each gene symbol. */
				for (String geneSymbol : genes) {
					Gene g= GeneSetController.getInstance().getGene(geneSymbol);	
					if (g != null) {
						geneList.add(g);
						
						/* Search for the pattern "GENENAME:" in the jannovar symbol field.
						 * Since there can be multiple genes in a field, I have two 
						 * conditions below. */
						genePanelCC.addCondition(
							BinaryCondition.iLike(ts.getDBColumn(columns.get(
							BasicVariantColumns.JANNOVAR_SYMBOL.getAlias())), geneSymbol + ":%"));
						genePanelCC.addCondition(
							BinaryCondition.iLike(ts.getDBColumn(columns.get(
							BasicVariantColumns.JANNOVAR_SYMBOL.getAlias())), "%:" + geneSymbol + ":%"));
						
					} else { // Gene symbol not found in our DB
						throw new GenePanelException(
							"Could not retrieve details for gene symbol: " + geneSymbol);
					}
				}
			}
		} catch (Exception ex) {
			DialogUtils.displayError(ex.getMessage());
			ex.printStackTrace();
		}
		
		// Hide the JDialog
		if (displayDialog != null)
			displayDialog.setVisible(false);
	}
	
	
	/**
	 * Action performed when "Done" button is clicked.
	 */
	private void clearAction() {
		try {
			doneButton.setText(DONE_TEXT);
			
			if (panelType.equals(GenePanel.PANEL)) {
				GeneSearchConditionEditorView temp= (GeneSearchConditionEditorView) scev;
				temp.clearTextArea();
			}
					
			genePanelTitle.setText(DEFAULT_GENE_PANEL_TITLE_TEXT);
			genePanelTitle.setForeground(Color.RED);
			scp.remove(genePanelTitle);
			scp.revalidate();

			// Clear the current list of Genes
			geneList.clear();

			// Reset the ComboCondition to empty
			genePanelCC= new ComboCondition(ComboCondition.Op.OR);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Get the GenePanel ComboCondition.
	 * @return the ComboCondition corresponding to this gene panel
	 */
	public ComboCondition getComboCondition() {
		return this.genePanelCC;
	}
	
	
	/**
	 * Clear/reset the ComboCondition to empty
	 */
	public void clearCondition() {
		genePanelCC= new ComboCondition(ComboCondition.Op.OR);
	}
	
	
	/**
	 * Set the background color.
	 * @param c The Color
	 */
	public void setBackground(Color c) {
		scev.setBackground(c);
		scp.setBackground(c);
		scp.getButtonPanel().setBackground(c);
	}
	
		
	/**
	 * Saves the gene list as a gene panel for this project in MedSavant.
	 * @param panelTitle The gene panel title
	 * @param geneList The gene panel as a List of Gene objects
	 */
	private void saveGenePanel(String panelTitle, List<Gene> geneList) {
		try {
			/* Create a temp file and send that to the server for import.
			 * NOTE: I'm using the BED order that is used elsewhere in MedSavant,
			 * but I'm not sure why it's designed this way. */
			File tempFile = File.createTempFile("genes", ".bed");
			FileWriter output = new FileWriter(tempFile);
			for (Gene g : geneList) {
				output.write(g.getChrom() + "\t" + g.getStart() + "\t" + 
					g.getEnd() + "\t" + g.getName() + "\n");
			}
			output.close();
			
			char delim = '\t';
			int numHeaderLines = 0;
			FileFormat fileFormat = new BEDFormat();
			String path = tempFile.getAbsolutePath();

			int transferID = ClientNetworkUtils.copyFileToServer(new File(path));

			controller.addRegionSet(panelTitle, delim, fileFormat, numHeaderLines, transferID);
			
			tempFile.delete(); // remove the temporary file
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Check to gene panel title is already in use.
	 * @param panelTitle The gene panel title
	 * @return boolean if name already exists
	 */
	private boolean validateGenePanelTitle(String panelTitle) {
		try {
			for (RegionSet r : controller.getRegionSets()) {
				if (r.getName().equals(panelTitle)) {
					DialogUtils.displayError("Error", "List name already in use.");
					return false;
				}
			}
			return true;
		} catch (Exception ex) {
			ClientMiscUtils.reportError("Error fetching region list: %s", ex);
			return false;
		}
	}
	
	
	/**
	 * Update the gene panel combo condition based on an existing gene panel
	 * stored as a region list on the server.
	 * @param name The name of the gene panel/region list
	 */
	public void addCustomGenePanel(String name) {			
		// Reset the combo condition, use ORs between genes for a gene panel
		genePanelCC= new ComboCondition(ComboCondition.Op.OR);

		// Get the list of region lists (gene panels) and find the one we're
		// interested in.
		try {
			List<RegionSet> allGenePanels= RegionController.getInstance().getRegionSets();
			for (RegionSet rs : allGenePanels) {
				if (rs.getName().equals(name)) {
					List<GenomicRegion> allGenes= RegionController.getInstance().getRegionsInSet(rs);
					
					// iterate through all genes in this panel
					for (GenomicRegion gr : allGenes) {
						// add the gene name/symbol to the combo condition
						genePanelCC.addCondition(BinaryCondition.iLike(
							ts.getDBColumn(columns.get(
								BasicVariantColumns.JANNOVAR_SYMBOL.getAlias())), 
								"%" + gr.getName() + ":%"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
