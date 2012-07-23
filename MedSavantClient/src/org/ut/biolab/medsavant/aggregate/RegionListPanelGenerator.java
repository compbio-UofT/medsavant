/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.aggregate;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.swing.*;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.grid.SortableTable;
import java.awt.Color;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.region.RegionController;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.DataRetriever;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
class RegionListPanelGenerator extends AggregatePanelGenerator {

    RegionListPanelGenerator(String page) {
        super(page);
    }

    @Override
    public String getName() {
        return "Region List";
    }

    @Override
    AggregatePanel generatePanel() {
        return new RegionListPanel();
    }

    public class RegionListPanel extends AggregatePanel {

        private final JComboBox regionsCombo;
        private final JPanel tablePanel;
        private MedSavantWorker aggregator;
        private final TreeMap<GenomicRegion, Integer> regionToVariantCountMap;
        private SearchableTablePanel stp;
        private GenomicRegion[] currentRegions;
        private final TreeMap<GenomicRegion, Integer> regionToIndividualCountMap;
        private final JProgressBar progress;

        private int numbersRetrieved;
        private int limit = 10000;
        private List<Object[]> currentData = new ArrayList<Object[]>();
        private final Object lock = new Object();

        public RegionListPanel() {
            setLayout(new BorderLayout());

            regionToVariantCountMap = new TreeMap<GenomicRegion, Integer>();
            regionToIndividualCountMap = new TreeMap<GenomicRegion, Integer>();

            regionsCombo = new JComboBox();

            tablePanel = new JPanel();
            tablePanel.setLayout(new BorderLayout());

            progress = new JProgressBar();
            progress.setStringPainted(true);

            JPanel banner = new JPanel();
            banner.setLayout(new BoxLayout(banner, BoxLayout.X_AXIS));
        
            banner.setBackground(new Color(245,245,245));
            banner.setBorder(BorderFactory.createTitledBorder("Region List"));

            banner.add(regionsCombo);
            banner.add(ViewUtil.getMediumSeparator());
            banner.add(Box.createHorizontalGlue());
            banner.add(progress);

            add(banner, BorderLayout.NORTH);
            add(tablePanel, BorderLayout.CENTER);

            createSearchableTable();

            regionsCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    recalculate();
                }
            });

            new MedSavantWorker<RegionSet[]>(pageName) {
                @Override
                protected RegionSet[] doInBackground() throws Exception {
                    return RegionController.getInstance().getRegionSets();
                }

                @Override
                protected void showProgress(double fraction) {}

                @Override
                protected void showSuccess(RegionSet[] result) {
                    updateGeneListDropDown(result);
                }
            }.execute();
        }

        @Override
        public void recalculate() {
            if (regionsCombo != null && regionsCombo.getSelectedItem() != null) {
                showGeneAggregates((RegionSet) regionsCombo.getSelectedItem());
            }
        }

        private void createSearchableTable() {
            DataRetriever<Object[]> retriever = new DataRetriever<Object[]>() {

                @Override
                public List<Object[]> retrieve(int start, int limit) {

                    synchronized(lock) {
                        //Do nothing. This ensures that no data is being written from previous worker.
                    }

                    try {
                        //compute variant field
                        for (int i = 0; i < Math.min(currentRegions.length, limit); i++) {
                            GenomicRegion r = currentRegions[i];
                            int recordsInRegion = MedSavantClient.VariantManager.getVariantCountInRange(
                                    LoginController.sessionId,
                                    ProjectController.getInstance().getCurrentProjectID(),
                                    ReferenceController.getInstance().getCurrentReferenceID(),
                                    FilterController.getQueryFilterConditions(),
                                    r.getChrom(),
                                    r.getStart(),
                                    r.getEnd());
                            if (!Thread.interrupted()) {
                                synchronized(lock) {
                                    updateBEDRecordVariantValue(r, recordsInRegion);
                                }
                            } else {
                                return new ArrayList<Object[]>();
                            }
                        }
                    } catch (Exception ex) {
                        ClientMiscUtils.reportError("Error getting variant count: %s", ex);
                    }

                    //compute patient field
                    try {
                        for (int i = 0; i < Math.min(currentRegions.length, limit); i++) {
                            GenomicRegion r = currentRegions[i];
                            int recordsInRegion = MedSavantClient.VariantManager.getPatientCountWithVariantsInRange(
                                    LoginController.sessionId,
                                    ProjectController.getInstance().getCurrentProjectID(),
                                    ReferenceController.getInstance().getCurrentReferenceID(),
                                    FilterController.getQueryFilterConditions(),
                                    r.getChrom(),
                                    r.getStart(),
                                    r.getEnd());
                            if (!Thread.interrupted()) {
                                synchronized(lock) {
                                    updateBEDRecordPatientValue(r, recordsInRegion);
                                }
                            } else {
                                return new ArrayList<Object[]>();
                            }
                        }
                    } catch (Exception ex) {
                        ClientMiscUtils.reportError("Error getting patient count: %s", ex);
                    }

                    return currentData;
                }

                @Override
                public int getTotalNum() {
                    return currentRegions.length;
                }

                @Override
                public void retrievalComplete() {
                    //xxx
                }
            };

            stp = new SearchableTablePanel(pageName,
                                           new String[] { "Name", "Chromosome", "Start", "End", "Variants", "Patients" },
                                           new Class[] { String.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class },
                                           new int[0], limit, retriever);

            stp.getTable().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {

                    //check for right click
                    if (!SwingUtilities.isRightMouseButton(e)) return;

                    SortableTable table = stp.getTable();
                    int numSelected = table.getSelectedRows().length;
                    if (numSelected == 1) {
                        int r = table.rowAtPoint(e.getPoint());
                        if (r < 0 || r >= table.getRowCount()) return;
                        JPopupMenu popup = createPopupSingle(table, r);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    } else if (numSelected > 1) {
                        //JPopupMenu popup = createPopupMultiple(table);
                        //popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });


            showShowCard();
        }

        private JPopupMenu createPopupSingle(SortableTable table, int r) {

            table.setRowSelectionInterval(r, r);
            //int row = TableModelWrapperUtils.getActualRowAt(table.getModel(), r);

            final String chrom = (String)table.getModel().getValueAt(r, 1);
            final int positionstart = (Integer)table.getModel().getValueAt(r, 2);
            final int positionend = (Integer)table.getModel().getValueAt(r, 3);

            JPopupMenu menu = new JPopupMenu();

            //Filter by position
            JMenuItem filter1Item = new JMenuItem("Filter by Region");
            filter1Item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    ThreadController.getInstance().cancelWorkers(pageName);

                    Condition[] conditions = new Condition[3];
                    conditions[0] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom);
                    conditions[1] = BinaryCondition.greaterThan(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), positionstart, true);
                    conditions[2] = BinaryCondition.lessThan(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), positionend, true);
                    FilterUtils.createAndApplyGenericFixedFilter(
                            "Aggregate - Filter by Region",
                            "Chromosome: " + chrom + ", Position: " + positionstart + " - " + positionend,
                            ComboCondition.and(conditions));
                }
            });
            menu.add(filter1Item);

            return menu;
        }


        private void showWaitCard() {
            tablePanel.removeAll();
            tablePanel.add(new WaitPanel("Getting aggregate information"), BorderLayout.CENTER);
            tablePanel.updateUI();
        }

        private void showShowCard() {
            tablePanel.removeAll();
            tablePanel.add(stp, BorderLayout.CENTER);
            tablePanel.updateUI();
        }

        private synchronized void updateGeneTable() {
            
            regionToVariantCountMap.clear();
            regionToIndividualCountMap.clear();

            numbersRetrieved = 0;
            updateProgess();

            for (GenomicRegion r : this.currentRegions) {
                regionToVariantCountMap.put(r, null);
                regionToIndividualCountMap.put(r, null);
            }

            showShowCard();

            updateData();

            stp.forceRefreshData();

        }

        private void initGeneTable(GenomicRegion[] genes) {
            this.currentRegions = genes;
            updateGeneTable();
        }

        public synchronized void updateData() {
            
            List<Object[]> data = new ArrayList<Object[]>();

            int i = 0;
            for (GenomicRegion r : regionToVariantCountMap.keySet()) {
                if (i >= limit) break;
                data.add(BEDToVector(r, regionToVariantCountMap.get(r), regionToIndividualCountMap.get(r)));
                i++;
            }

            int row = stp.getTable().getSelectedRow();
            stp.applyData(data);
            currentData = data;

            if (row >= 0) {
                stp.getTable().getSelectionModel().setSelectionInterval(row, row);
            }
        }

        public synchronized void updateBEDRecordVariantValue(GenomicRegion br, int value) {
            regionToVariantCountMap.put(br, value);
            updateData();
            incrementProgress();
        }

        public synchronized void updateBEDRecordPatientValue(GenomicRegion br, int value) {
            regionToIndividualCountMap.put(br, value);
            updateData();
            incrementProgress();
        }

        public void updateGeneListDropDown(RegionSet[] geneLists) {
            for (RegionSet genel : geneLists) {
                regionsCombo.addItem(genel);
            }
        }

        private void showGeneAggregates(final RegionSet regionSet) {

            if (aggregator != null && !aggregator.isDone() && !aggregator.isCancelled()) {
                aggregator.cancel(true);
            }

            this.numbersRetrieved = 0;
            this.updateProgess();

            showWaitCard();

            progress.setString("Getting regions for " + regionSet);
            aggregator = new MedSavantWorker<GenomicRegion[]>(pageName) {

                @Override
                protected GenomicRegion[] doInBackground() throws Exception {
                    return MedSavantClient.RegionSetManager.getRegionsInSet(LoginController.sessionId, regionSet, limit);
                }

                @Override
                protected void showProgress(double fraction) {}

                @Override
                protected void showSuccess(GenomicRegion[] result) {
                    initGeneTable(result);
                }
            };

            aggregator.execute();
        }

        private Object[] BEDToVector(GenomicRegion r, Integer numVariants, Integer numIndividuals) {
            return new Object[] { r.getName(), r.getChrom(), r.getStart(), r.getEnd(), numVariants, numIndividuals };
        }

        //public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
            //updateGeneTable();
        //}

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
    }
}
