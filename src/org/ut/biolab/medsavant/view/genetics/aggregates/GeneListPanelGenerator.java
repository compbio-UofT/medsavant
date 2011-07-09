/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.sql.SQLException;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.record.BEDRecord;
import com.jidesoft.utils.SwingWorker;
import fiume.table.SearchableTablePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.ConnectionController;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
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

    public void setUpdate(boolean update) {
        
        System.out.println("Received message update " + update);
        
        if (panel == null) { return; }
        
        if (update) {
            
        } else {
            panel.stopThreads();

        }
    }

    public class GeneListPanel extends JPanel implements FiltersChangedListener {

        private final JPanel banner;
        private final JComboBox geneLister;
        private final JButton goButton;
        private final JPanel tablePanel;
        private GeneAggregator aggregator;
        //private final JPanel content;
        private final TreeMap<BEDRecord, Integer> regionToVariantCountMap;
        private SearchableTablePanel stp;
        private GeneVariantIntersectionWorker gviw;
        private List<BEDRecord> currentGenes;
        private final TreeMap<BEDRecord, Integer> regionToIndividualCountMap;
        private GenePatientIntersectionWorker gpiw;
        private final JProgressBar progress;
        private int numbersRetrieved;

        public GeneListPanel() {

            this.setLayout(new BorderLayout());
            banner = ViewUtil.getSubBannerPanel("Gene List");
            //banner.setLayout(new BoxLayout(banner,BoxLayout.X_AXIS));
            //banner.setBorder(ViewUtil.getMediumBorder());

            regionToVariantCountMap = new TreeMap<BEDRecord, Integer>();
            regionToIndividualCountMap = new TreeMap<BEDRecord, Integer>();

            geneLister = new JComboBox();

            goButton = new JButton("Aggregate");

            tablePanel = new JPanel();
            //tablePanel.setBackground(Color.white);
            tablePanel.setLayout(new BorderLayout());

            //banner.add(Box.createHorizontalGlue());
            //banner.add(new JLabel("> Gene list: "));
            banner.add(geneLister);
            banner.add(ViewUtil.getMediumSeparator());
            
            //banner.add(goButton);
            banner.add(Box.createHorizontalGlue());
            
            progress = new JProgressBar();
            progress.setStringPainted(true);
            
            banner.add(progress);
            

            this.add(banner, BorderLayout.NORTH);
            this.add(tablePanel, BorderLayout.CENTER);

            geneLister.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("=== " + (String) geneLister.getSelectedItem() + "===");
                    showGeneAggregates((String) geneLister.getSelectedItem());
                }
            });

            //content = new JPanel();
            //content.setLayout(new BorderLayout());

            (new GeneListGetter()).execute();

            FilterController.addFilterListener(this);
        }

        private synchronized void updateGeneTable() {

            //this.add(content,BorderLayout.CENTER);

            List<String> columnNames = Arrays.asList(new String[]{"Name", "Chromosome", "Start", "End", "Variants", "Patients"});
            List<Class> columnClasses = Arrays.asList(new Class[]{String.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class});
            stp = new SearchableTablePanel(new ArrayList<Vector>(), columnNames, columnClasses, new ArrayList<Integer>());

            regionToVariantCountMap.clear();
            regionToIndividualCountMap.clear();

            numbersRetrieved = 0;
            updateProgess();
            
            for (BEDRecord r : this.currentGenes) {
                regionToVariantCountMap.put(r, null);
                regionToIndividualCountMap.put(r, null);
            }
            
            tablePanel.removeAll();
            
            tablePanel.add(stp, BorderLayout.CENTER);

            System.out.println("showing table");
            tablePanel.updateUI();

            updateData();

            tablePanel.updateUI();

            if (gviw != null && !gviw.isDone()) {
                gviw.cancel(true);
            }
            
            if (gpiw != null && !gpiw.isDone()) {
                gpiw.cancel(true);
            }

            
            gviw = new GeneVariantIntersectionWorker(this.currentGenes);
            gviw.execute();
            
            gpiw = new GenePatientIntersectionWorker(this.currentGenes);
            gpiw.execute();

        }

        private void initGeneTable(List<BEDRecord> genes) {
            this.currentGenes = genes;
            updateGeneTable();
        }

        public synchronized void updateData() {
            
            Vector data = new Vector();
            
            for (BEDRecord r : regionToVariantCountMap.keySet()) {
                 data.add(BEDToVector(r, regionToVariantCountMap.get(r), regionToIndividualCountMap.get(r)));
            }
            

            stp.updateData(data);
            stp.updateUI();
            stp.updateView();
        }

        public synchronized void updateBEDRecordVariantValue(BEDRecord br, int value) {
            regionToVariantCountMap.put(br, value);
            updateData();
                        incrementProgress();
        }
        
        public synchronized void updateBEDRecordPatientValue(BEDRecord br, int value) {
            regionToIndividualCountMap.put(br, value);
            updateData();
                        incrementProgress();
        }

        public void updateGeneListDropDown(List<String> geneLists) {
            for (String genel : geneLists) {
                geneLister.addItem(genel);
            }
        }

        private void showGeneAggregates(String geneList) {

            if (aggregator != null && !aggregator.isDone() && !aggregator.isCancelled()) {
                aggregator.cancel(true);
            }

            this.numbersRetrieved = 0;
            this.updateProgess();
            
            this.tablePanel.removeAll();
            this.tablePanel.add(new WaitPanel("Getting aggregate information"), BorderLayout.CENTER);
            tablePanel.updateUI();

            progress.setString("Getting " + geneList + " gene list");
            aggregator = new GeneAggregator(geneList);
            aggregator.execute();
        }

        private Vector BEDToVector(BEDRecord r, Integer numVariants, Integer numIndividuals) {
            Vector v = new Vector();
            v.add(r.getName());
            v.add(r.getChrom());
            v.add(r.getStart());
            v.add(r.getEnd());
            v.add(numVariants);
            v.add(numIndividuals);
            return v;
        }

        public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {

            if (this.gviw != null && !this.gviw.isDone()) {
                this.gviw.cancel(true);
            }
            
            if (this.gpiw != null && !this.gpiw.isDone()) {
                this.gpiw.cancel(true);
            }

            updateGeneTable();
        }
        
        private void incrementProgress() {
            numbersRetrieved++;
            updateProgess();
        }

        private void updateProgess() {
            int value = 0;
            if (regionToVariantCountMap.size() != 0) { 
                value = numbersRetrieved*100/2/regionToVariantCountMap.keySet().size(); 
            }
            progress.setValue(value);
            progress.setString(value + "% done ");
            
        }

        private void stopThreads() {
            
            try {
                this.gpiw.cancel(true);
            } catch (Exception e) {}
            
            try {
                this.gviw.cancel(true);
            } catch (Exception e) {}
            
            progress.setString("stopped");

        }

        private class GeneAggregator extends SwingWorker {

            private final String geneListName;

            private GeneAggregator(String geneList) {
                this.geneListName = geneList;
            }

            @Override
            protected Object doInBackground() throws Exception {
                final List<BEDRecord> genes = QueryUtil.getRegionsInRegionList(geneListName);
                return genes;
            }

            protected void done() {
                try {
                    List<BEDRecord> genes = (List<BEDRecord>) get();
                    initGeneTable(genes);
                } catch (java.util.concurrent.CancellationException ex) {
                    System.err.println("Gene list " + geneListName + " cancelled");
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                } catch (Exception e) {
                    System.err.println("Error initializing table: " + e.getMessage());
                }
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
                } catch (java.util.concurrent.CancellationException ex) {
                } catch (InterruptedException ex) {
                    Logger.getLogger(GeneListPanelGenerator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(GeneListPanelGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        private class GeneVariantIntersectionWorker extends SwingWorker {

            private final List<BEDRecord> records;

            public GeneVariantIntersectionWorker(List<BEDRecord> records) {
                this.records = records;
            }

            @Override
            protected Object doInBackground() throws Exception {
                for (BEDRecord r : records) {
                    int recordsInRegion = QueryUtil.getNumVariantsInRange(
                            ConnectionController.connect(),
                            r.getChrom(),
                            r.getStart(),
                            r.getEnd());
                    if (!Thread.interrupted()) {
                        updateBEDRecordVariantValue(r, recordsInRegion);
                    } else {
                        break;
                    }
                }
                return null;
            }
        }
        
        private class GenePatientIntersectionWorker extends SwingWorker {

            private final List<BEDRecord> records;

            public GenePatientIntersectionWorker(List<BEDRecord> records) {
                this.records = records;
            }

            @Override
            protected Object doInBackground() throws Exception {
                for (BEDRecord r : records) {
                    int recordsInRegion = QueryUtil.getNumPatientsWithVariantsInRange(
                            ConnectionController.connect(),
                            r.getChrom(),
                            r.getStart(),
                            r.getEnd());
                    if (!Thread.interrupted()) {
                        updateBEDRecordPatientValue(r, recordsInRegion);
                    } else {
                        break;
                    }
                }
                return null;
            }
        }
    }
}
