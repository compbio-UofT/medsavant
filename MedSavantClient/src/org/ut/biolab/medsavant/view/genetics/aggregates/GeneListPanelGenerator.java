/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.sql.SQLException;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.BEDRecord;
import com.jidesoft.utils.SwingWorker;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.model.RegionSet;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.component.Util.DataRetriever;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class GeneListPanelGenerator implements AggregatePanelGenerator {
    private static final Logger LOG = Logger.getLogger(GeneListPanelGenerator.class.getName());

    private GeneListPanel panel;
    private final String pageName;

    public GeneListPanelGenerator(String pageName) {
        this.pageName = pageName;
    }

    public String getName() {
        return "Genomic Region";
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new GeneListPanel();
        } else {
            panel.update();
        }
        return panel;
    }

    /*public void setUpdate(boolean update) {
        
        if (panel == null) { return; }
        
        if (update) {
            
        } else {
            panel.stopThreads();
        }
    }*/
    
    public void run(){
        if(panel != null)
            panel.update();
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
        //private GeneVariantIntersectionWorker gviw;
        private List<BEDRecord> currentGenes;
        private final TreeMap<BEDRecord, Integer> regionToIndividualCountMap;
        //private GenePatientIntersectionWorker gpiw;
        private final JProgressBar progress;
        private int numbersRetrieved;
        private int limit = 10000;
        private List<Object[]> currentData = new ArrayList<Object[]>();
        private final Object lock = new Object();

        public GeneListPanel() {

            this.setLayout(new BorderLayout());
            banner = ViewUtil.getSubBannerPanel("Gene List");

            regionToVariantCountMap = new TreeMap<BEDRecord, Integer>();
            regionToIndividualCountMap = new TreeMap<BEDRecord, Integer>();

            geneLister = new JComboBox();

            goButton = new JButton("Aggregate");

            tablePanel = new JPanel();
            tablePanel.setLayout(new BorderLayout());

            banner.add(geneLister);
            banner.add(ViewUtil.getMediumSeparator());
            
            banner.add(Box.createHorizontalGlue());
            
            progress = new JProgressBar();
            progress.setStringPainted(true);
            
            banner.add(progress);
            
            this.add(banner, BorderLayout.NORTH);
            this.add(tablePanel, BorderLayout.CENTER);
                       
            createSearchableTable();

            geneLister.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    update();
                }
            });
            
            (new GeneListGetter()).execute();

            FilterController.addFilterListener(this);
        }
        
        public void update(){
            if(geneLister != null && geneLister.getSelectedItem() != null)
                showGeneAggregates((RegionSet) geneLister.getSelectedItem());
        }
        
        private void createSearchableTable(){
            DataRetriever retriever = new DataRetriever() {

                public List<Object[]> retrieve(int start, int limit) {
                    
                    synchronized(lock){
                        //Do nothing. This ensures that no data is being written from previous worker. 
                    }
                                        
                    //compute variant field
                    for(int i = 0; i < Math.min(currentGenes.size(), limit); i++){
                        try {
                            BEDRecord r = currentGenes.get(i);
                            int recordsInRegion = MedSavantClient.VariantQueryUtilAdapter.getNumVariantsInRange(
                                    LoginController.sessionId, 
                                    ProjectController.getInstance().getCurrentProjectId(), 
                                    ReferenceController.getInstance().getCurrentReferenceId(), 
                                    FilterController.getQueryFilterConditions(), 
                                    r.getChrom(), 
                                    r.getStart(), 
                                    r.getEnd());
                            if (!Thread.interrupted()) {
                                synchronized(lock){
                                    updateBEDRecordVariantValue(r, recordsInRegion);
                                }                               
                            } else {
                                return new ArrayList<Object[]>();
                            }       
                        } catch (SQLException ex) {
                            MiscUtils.checkSQLException(ex);
                        } catch (Exception e){}
                    }
                    
                    //compute patient field
                    for(int i = 0; i < Math.min(currentGenes.size(), limit); i++){
                        try {
                            BEDRecord r = currentGenes.get(i);
                            int recordsInRegion = MedSavantClient.VariantQueryUtilAdapter.getNumPatientsWithVariantsInRange(
                                    LoginController.sessionId, 
                                    ProjectController.getInstance().getCurrentProjectId(), 
                                    ReferenceController.getInstance().getCurrentReferenceId(), 
                                    FilterController.getQueryFilterConditions(), 
                                    r.getChrom(), 
                                    r.getStart(), 
                                    r.getEnd());
                            if (!Thread.interrupted()) {
                                synchronized(lock){
                                    updateBEDRecordPatientValue(r, recordsInRegion);
                                }
                            } else {
                                return new ArrayList<Object[]>();
                            }
                        } catch (SQLException ex) {
                            MiscUtils.checkSQLException(ex);
                        } catch (Exception e){}
                    }                    
                    
                    return currentData;
                }

                public int getTotalNum() {
                    return currentGenes.size();
                }

                public void retrievalComplete() {
                    //xxx
                }
            };

            List<String> columnNames = Arrays.asList(new String[]{"Name", "Chromosome", "Start", "End", "Variants", "Patients"});
            List<Class> columnClasses = Arrays.asList(new Class[]{String.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class});
            stp = new SearchableTablePanel(pageName, columnNames, columnClasses, new ArrayList<Integer>(), limit, retriever);
            
            showShowCard();
        }
        
        private void showWaitCard(){
            tablePanel.removeAll();
            tablePanel.add(new WaitPanel("Getting aggregate information"), BorderLayout.CENTER);
            tablePanel.updateUI();
        }
        
        private void showShowCard(){
            tablePanel.removeAll();           
            tablePanel.add(stp, BorderLayout.CENTER);
            tablePanel.updateUI();
        }

        private synchronized void updateGeneTable() {            

            regionToVariantCountMap.clear();
            regionToIndividualCountMap.clear();

            numbersRetrieved = 0;
            updateProgess();
            
            for (BEDRecord r : this.currentGenes) {
                regionToVariantCountMap.put(r, null);
                regionToIndividualCountMap.put(r, null);
            }
            
            showShowCard();
                    
            updateData();
            
            stp.forceRefreshData();
                                   
        }

        private void initGeneTable(List<BEDRecord> genes) {
            this.currentGenes = genes;
            updateGeneTable();
        }

        public synchronized void updateData() {
            
            List<Object[]> data = new ArrayList<Object[]>();

            int i = 0;
            for (BEDRecord r : regionToVariantCountMap.keySet()) {
                if(i >= limit) break;
                data.add(BEDToVector(r, regionToVariantCountMap.get(r), regionToIndividualCountMap.get(r)));
                i++;
            }
            
            stp.applyData(data);
            currentData = data;
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

        public void updateGeneListDropDown(List<RegionSet> geneLists) {
            for (RegionSet genel : geneLists) {
                geneLister.addItem(genel);
            }
        }

        private void showGeneAggregates(RegionSet geneList) {

            if (aggregator != null && !aggregator.isDone() && !aggregator.isCancelled()) {
                aggregator.cancel(true);
            }

            this.numbersRetrieved = 0;
            this.updateProgess();
            
            showWaitCard();

            progress.setString("Getting " + geneList + " gene list");
            aggregator = new GeneAggregator(geneList);
            aggregator.execute();
        }

        private Object[] BEDToVector(BEDRecord r, Integer numVariants, Integer numIndividuals) {
            return new Object[] { r.getName(), r.getChrom(), r.getStart(), r.getEnd(), numVariants, numIndividuals };
        }

        public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
            updateGeneTable();
        }
        
        private void incrementProgress() {
            numbersRetrieved++;
            updateProgess();
        }

        private void updateProgess() {
            int value = 0;
            if (regionToVariantCountMap.size() != 0) { 
                value = numbersRetrieved*100/2/Math.min(regionToVariantCountMap.keySet().size(), limit); 
            }
            value = Math.min(value, 100);
            progress.setValue(value);
            progress.setString(value + "% done ");    
            stp.setExportButtonEnabled(value == 100);
        }

        private void stopThreads() {
            progress.setString("stopped");
        }

        private class GeneAggregator extends SwingWorker<List<BEDRecord>, BEDRecord> {

            private final RegionSet geneList;

            private GeneAggregator(RegionSet geneList) {
                this.geneList = geneList;
            }

            @Override
            protected List<BEDRecord> doInBackground() throws Exception {
                return MedSavantClient.RegionQueryUtilAdapter.getBedRegionsInRegionSet(LoginController.sessionId, geneList.getId(), limit);
            }

            @Override
            protected void done() {
                try {
                    initGeneTable(get());
                } catch (Exception x) {
                    // TODO: #90
                    LOG.log(Level.SEVERE, null, x);
                }
            }
        }

        private class GeneListGetter extends SwingWorker<List<RegionSet>, RegionSet> {

            @Override
            protected List<RegionSet> doInBackground() throws Exception {
                return MedSavantClient.RegionQueryUtilAdapter.getRegionSets(LoginController.sessionId);
            }

            @Override
            protected void done() {
                try {
                    updateGeneListDropDown(get());
                } catch (Exception x) {
                    // TODO: #90
                    LOG.log(Level.SEVERE, null, x);
                }
            }
        }

    }
}
