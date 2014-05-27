/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.query.medsavant.complex;

import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.list.FilterableListModelEvent;
import com.jidesoft.list.FilterableListModelListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.client.query.SearchConditionItem;
import org.ut.biolab.medsavant.client.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.medsavant.client.query.value.StringConditionValueGenerator;
import org.ut.biolab.medsavant.client.query.value.encode.StringConditionEncoder;
import org.ut.biolab.medsavant.client.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume, rammar
 */
public class OntologyConditionGenerator implements ComprehensiveConditionGenerator {

    private static final Log LOG = LogFactory.getLog(OntologyConditionGenerator.class);
    private static final int MAX_GENES_IN_POPUP = 6; //List at most 6 genes in the popup before displaying 'More...'         
    private boolean alreadyInitialized;
    private HashMap<String, OntologyTerm> termNameToTermObjectMap;   
    private List<String> acceptableValues;
    private final OntologyType ontology;
    
    private static final Dimension DEFAULT_DIMENSIONS = new Dimension(600,384);

    private class OntologySearchConditionEditorView extends StringSearchConditionEditorView {

        private Map<String, JPopupMenu> popupMap;
        private Map<String, JLabel> countMap;
        private boolean mapsReady = false;
        private Semaphore mapLock = new Semaphore(1);
        private MedSavantWorker<Void> refresher;

        public OntologySearchConditionEditorView(SearchConditionItem i, StringConditionValueGenerator vg) {
            super(i, vg);
            setPreferredSize(DEFAULT_DIMENSIONS);            
        }

        @Override
        public void loadViewFromSearchConditionParameters(String encoding) throws ConditionRestorationException {
            super.loadViewFromSearchConditionParameters(encoding);                                       
        }
        
        
        
        private synchronized void refreshMaps() {
            if (refresher != null) {
                return;
            }
            try {                
                mapLock.acquire();
                final OntologySearchConditionEditorView instance = this;
                this.refresher = new MedSavantWorker<Void>("Ontology") {
                    @Override
                    protected Void doInBackground() throws Exception {
                        
                        OntologyTerm[] terms = MedSavantClient.OntologyManager.getAllTerms(LoginController.getSessionID(), ontology);
                        popupMap = new HashMap<String, JPopupMenu>();
                        countMap = new HashMap<String, JLabel>();
                        for (OntologyTerm term : terms) {
                            popupMap.put(getTermName(term), new JPopupMenu());
                            countMap.put(getTermName(term), new JLabel());                           
                        }
                        
                        mapLock.release();
                        mapsReady = true;

                        String session = LoginController.getSessionID();
                        String refName = ReferenceController.getInstance().getCurrentReferenceName();
                        for (final OntologyTerm term : terms) {
                            final String[] genes =
                                    MedSavantClient.OntologyManager.getGenesForTerm(session, term, refName);
                            if (genes != null) {
                                final JPopupMenu menu = popupMap.get(getTermName(term));
                                final JLabel lbl = countMap.get(getTermName(term));
                                if(lbl == null){
                                    System.out.println("Null label for "+getTermName(term));
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        lbl.setText("(" + genes.length + ")");                                        
                                        menu.removeAll();
                                        menu.add(getPopupItem(term, genes));                                        
                                        lbl.addMouseListener(new MouseAdapter(){

                                            @Override
                                            public void mouseClicked(MouseEvent me) {                                                
                                                super.mouseClicked(me); 
                                            }

                                            @Override
                                            public void mouseEntered(MouseEvent me) {                                                
                                                super.mouseEntered(me); 
                                            }

                                            @Override
                                            public void mouseExited(MouseEvent me) {
                                                
                                                super.mouseExited(me);
                                            }
                                            
                                        });
                                        lbl.add(menu);
                                        instance.revalidate();
                                        instance.repaint();
                                    }
                                });
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void showSuccess(Void result) {
                       //do nothing.
                    }
                };
                refresher.execute();
               
                //wait for maps to be ready
                mapLock.acquire();                 
                mapLock.release();
            } catch (Exception ex) {
                LOG.error(ex);
            }
        }

        @Override
        protected JPopupMenu getPopupMenu(String itemHoveredOver) {                                   
            if (!mapsReady) {                
                refreshMaps();
            }            
            return popupMap.get(itemHoveredOver);
        }

        @Override
        protected JLabel getNumberInCategory(String category) {
            if (!mapsReady) {             
                refreshMaps();
            }            
            return countMap.get(category);          
        }

        private JMenuItem getPopupItem(final OntologyTerm term, final String[] genes) {

            String s = "";
            int j = 0;
            for (String gene : genes) {
                s += gene.trim() + " ";
                j++;
                if (j > MAX_GENES_IN_POPUP) {
                    s += "(" + (genes.length - MAX_GENES_IN_POPUP) + " More...)";
                    break;
                }
            }

            final JMenuItem ji = new JMenuItem(s);
            ji.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JDialog dialog = new JDialog(MedSavantFrame.getInstance());
                    dialog.setModal(true);
                    JPanel p = new JPanel();
                    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

                    JPanel lblPanel = new JPanel();
                    lblPanel.setLayout(new BoxLayout(lblPanel, BoxLayout.X_AXIS));
                    lblPanel.add(Box.createHorizontalGlue());
                    lblPanel.add(new JLabel(" Genes corresponding to annotation " + getTermName(term) + " "));
                    lblPanel.add(Box.createHorizontalGlue());

                    Arrays.sort(genes);
                    JList jl = new JList(genes);
                    JScrollPane jsp = new JScrollPane(jl);
                    p.add(lblPanel);
                    p.add(jsp);
                    dialog.setContentPane(p);
                    dialog.pack();
                    dialog.setLocationRelativeTo(MedSavantFrame.getInstance());
                    dialog.setVisible(true);
                }
            });

            return ji;
        }
    
    }

    public OntologyConditionGenerator(OntologyType ont) {
        this.ontology = ont;
    }

    @Override
    public String getName() {
        return ontology.name();
    }

    @Override
    public String category() {
        return MedSavantConditionViewGenerator.REGIONBASED_CONDITIONS;
    }

	
	
	/**
	 * Fetch the region list for an ontology encoding string.
	 * @param encoding the ontology encoding string
	 * @return a list of GenomicRegion regions
	 */
    public List<GenomicRegion> getRegionsFromEncoding(String encoding) throws Exception {
        init();
        List<String> termNames = StringConditionEncoder.unencodeConditions(encoding);
        List<OntologyTerm> appliedTerms = new ArrayList<OntologyTerm>(termNames.size());
        for (String termName : termNames) {
            appliedTerms.add(termNameToTermObjectMap.get(termName));
        }

        Set<Gene> genes = new HashSet<Gene>();
        Map<OntologyTerm, String[]> allTermsGenes = MedSavantClient.OntologyManager.getGenesForTerms(LoginController.getSessionID(), appliedTerms.toArray(new OntologyTerm[0]), ReferenceController.getInstance().getCurrentReferenceName());
        for (String[] termGenes : allTermsGenes.values()) {
            for (String geneName : termGenes) {
                Gene g = GeneSetController.getInstance().getGene(geneName);
                if (g != null) {
                    genes.add(g);
                } else {
                    LOG.info("Non-existent gene " + geneName + " referenced by " + ontology);
                }
            }
        }
        List<GenomicRegion> regions = new ArrayList<GenomicRegion>(genes.size());
        int i = 0;
        for (Gene g : genes) {
            regions.add(new GenomicRegion(g.getName(), g.getChrom(), g.getStart(), g.getEnd()));
        }
		
		return regions;
	}
	
	
    @Override
    public Condition getConditionsFromEncoding(String encoding) throws Exception {
		List<GenomicRegion> regions= getRegionsFromEncoding(encoding);
		
        return ConditionUtils.getConditionsMatchingGenomicRegions(regions);
    }
    
    @Override
    public StringSearchConditionEditorView getViewGeneratorForItem(SearchConditionItem item) {
        OntologySearchConditionEditorView editor = new OntologySearchConditionEditorView(item, new StringConditionValueGenerator() {            
            @Override
            public List<String> getStringValues() {
                init();
                return acceptableValues;
            }
        });        
        editor.refreshMaps();
        return editor;
    }
    
    private String getTermName(OntologyTerm t){
        return t.getOntology() + ":" + t.getName();
    }

    private void init() {
        if (alreadyInitialized) {
            return;
        }

        List<String> vals = new ArrayList<String>();

        try {

            OntologyTerm[] terms = MedSavantClient.OntologyManager.getAllTerms(LoginController.getSessionID(), ontology);         

            vals = new ArrayList<String>(terms.length);
            termNameToTermObjectMap = new HashMap<String, OntologyTerm>();
            for (OntologyTerm t : terms) {
                String termName = getTermName(t);
                termNameToTermObjectMap.put(termName, t);               
                vals.add(termName);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
        }

        acceptableValues = vals;
        alreadyInitialized = true;
    }
}
