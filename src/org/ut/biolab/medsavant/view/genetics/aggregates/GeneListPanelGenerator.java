/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import org.ut.biolab.medsavant.model.record.BEDRecord;
import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.view.genetics.aggregates.AggregatePanelGenerator;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class GeneListPanelGenerator implements AggregatePanelGenerator {

    private GeneListPanel panel;

    public GeneListPanelGenerator() {
    }

    public String getName() {
        return "Genomic Region";
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new GeneListPanel();
        }
        return panel;
    }

    public void setUpdate(boolean updatePanelUponFilterChanges) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public class GeneListPanel extends JPanel {

        private final JPanel banner;
        private final JComboBox geneLister;
        private final JButton goButton;
        private final JPanel tablePanel;
        private GeneAggregator aggregator;

        public GeneListPanel() {
            this.setLayout(new BorderLayout());
            banner = ViewUtil.getSubBannerPanel("Gene List");
            //banner.setLayout(new BoxLayout(banner,BoxLayout.X_AXIS));
            //banner.setBorder(ViewUtil.getMediumBorder());

            geneLister = new JComboBox();
            
            goButton = new JButton("Aggregate");
            
            tablePanel = new JPanel();
            tablePanel.setBackground(Color.white);
            tablePanel.setLayout(new BorderLayout());

            //banner.add(Box.createHorizontalGlue());
            //banner.add(new JLabel("> Gene list: "));
            banner.add(geneLister);
            banner.add(ViewUtil.getMediumSeparator());
            banner.add(goButton);
            banner.add(Box.createHorizontalGlue());
            
            this.add(banner,BorderLayout.NORTH);
            this.add(tablePanel,BorderLayout.CENTER);
            
            
            geneLister.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    showGeneAggregates((String) geneLister.getSelectedItem());
                }
                
            });
            
            (new GeneListGetter()).execute();
        }

        public void updateGeneListDropDown(List<String> geneLists) {
            for (String genel : geneLists) {
                geneLister.addItem(genel);
            }
        }
        
        
        private void showGeneAggregates(String geneList) {
            
            if (aggregator != null && !aggregator.isDone()) {
                aggregator.cancel(true);
            }
            
            this.tablePanel.removeAll();
            this.tablePanel.add(new WaitPanel("Getting aggregate information"),BorderLayout.CENTER);
            
            aggregator = new GeneAggregator(geneList);
            aggregator.execute();
        }
        
        private class GeneAggregator extends SwingWorker {
            private final String geneListName;

            private GeneAggregator(String geneList) {
                this.geneListName = geneList;
            }

            @Override
            protected Object doInBackground() throws Exception {
                List<BEDRecord> genes = QueryUtil.getRegionsInRegionList(geneListName);
                
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        //initGeneTable(genes);
                    }
                });
                
                return null;
            }
        }

        private class GeneListGetter extends SwingWorker {

            @Override
            protected Object doInBackground() throws Exception {
                List<String> geneLists = QueryUtil.getDistinctRegionLists();
                return geneLists;
            }

            protected void done() {
                try {
                    List<String> geneLists = (List<String>) get();
                    updateGeneListDropDown(geneLists);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GeneListPanelGenerator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(GeneListPanelGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
