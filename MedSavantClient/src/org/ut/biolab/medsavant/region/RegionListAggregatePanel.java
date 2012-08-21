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
package org.ut.biolab.medsavant.region;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.table.TableModel;

import com.jidesoft.grid.TableModelWrapperUtils;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.aggregate.AggregatePanel;
import org.ut.biolab.medsavant.filter.*;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.DataRetriever;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.genetics.GeneticsFilterPage;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class RegionListAggregatePanel extends AggregatePanel {

    private final JComboBox regionSetCombo;
    private final JPanel mainPanel;
    private MedSavantWorker regionFetcher;
    private final TreeMap<GenomicRegion, Integer> variantCounts;
    private SearchableTablePanel tablePanel;
    private List<GenomicRegion> currentRegions;
    private final TreeMap<GenomicRegion, Integer> patientCounts;
    private final JProgressBar progress;

    private int numbersRetrieved;
    private int limit = 10000;

    public RegionListAggregatePanel(String page) {
        super(page);
        setLayout(new BorderLayout());

        variantCounts = new TreeMap<GenomicRegion, Integer>();
        patientCounts = new TreeMap<GenomicRegion, Integer>();

        regionSetCombo = new JComboBox();

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        progress = new JProgressBar();
        progress.setStringPainted(true);

        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.X_AXIS));

        banner.setBackground(new Color(245,245,245));
        banner.setBorder(BorderFactory.createTitledBorder("Region List"));

        banner.add(regionSetCombo);
        banner.add(ViewUtil.getMediumSeparator());
        banner.add(Box.createHorizontalGlue());
        banner.add(progress);

        add(banner, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        createSearchableTable();

        regionSetCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchRegions((RegionSet)regionSetCombo.getSelectedItem());
            }
        });

        new MedSavantWorker<List<RegionSet>>(pageName) {
            @Override
            protected List<RegionSet> doInBackground() throws Exception {
                return RegionController.getInstance().getRegionSets();
            }

            @Override
            protected void showProgress(double fraction) {}

            @Override
            protected void showSuccess(List<RegionSet> result) {
                updateRegionSetCombo(result);
            }
        }.execute();
    }

    @Override
    public void recalculate() {
        if (currentRegions != null) {
            resetCounts();
            tablePanel.forceRefreshData();
        }
    }

    private void createSearchableTable() {
        tablePanel = new SearchableTablePanel(pageName,
                                        new String[] { "Name", "Chromosome", "Start", "End", "Variants", "Patients" },
                                        new Class[] { String.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class },
                                        new int[0], limit, new AggregationRetriever());

        tablePanel.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    createPopup().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        showShowCard();
    }

    private JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();

        TableModel model = tablePanel.getTable().getModel();
        final int[] selRows = tablePanel.getTable().getSelectedRows();

        JMenuItem posItem = new JMenuItem(String.format("<html>Filter by %s</html>", selRows.length == 1 ? "Region <i>" + model.getValueAt(selRows[0], 0) + "</i>" : "Selected Regions"));
        posItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ThreadController.getInstance().cancelWorkers(pageName);

                List<GenomicRegion> regions = new ArrayList<GenomicRegion>();
                TableModel model = tablePanel.getTable().getModel();
                for (int r: selRows) {
                    regions.add(new GenomicRegion((String)model.getValueAt(r, 0), (String)model.getValueAt(r, 1), (Integer)model.getValueAt(r, 2), (Integer)model.getValueAt(r, 3)));
                }

                RegionSet r = RegionController.getInstance().createAdHocRegionSet("Selected Regions", regions);
                GeneticsFilterPage.getSearchBar().loadFilters(RegionSetFilterView.wrapState(Arrays.asList(r)));
            }

        });
        menu.add(posItem);

        return menu;
    }

    private void showWaitCard() {
        mainPanel.removeAll();
        mainPanel.add(new WaitPanel("Getting aggregate information"), BorderLayout.CENTER);
        mainPanel.updateUI();
    }

    private void showShowCard() {
        mainPanel.removeAll();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.updateUI();
    }

    private void resetCounts() {
        variantCounts.clear();
        patientCounts.clear();
        for (GenomicRegion r: currentRegions) {
            variantCounts.put(r, null);
            patientCounts.put(r, null);
        }
    }

    private void initGeneTable(List<GenomicRegion> genes) {
        currentRegions = genes;
        resetCounts();
        numbersRetrieved = 0;
        updateProgress();

        showShowCard();

        List<Object[]> data = new ArrayList<Object[]>();

        int i = 0;
        for (GenomicRegion r : variantCounts.keySet()) {
            if (i >= limit) break;
            data.add(new Object[] {
                r.getName(), r.getChrom(), r.getStart(), r.getEnd(), variantCounts.get(r), patientCounts.get(r)
            });
            i++;
        }
        tablePanel.applyData(data);
        tablePanel.forceRefreshData();
    }

    private int findRow(GenomicRegion reg) {
        for (int i = 0; i < tablePanel.getTable().getRowCount(); i++) {
            if (reg.getName().equals(tablePanel.getTable().getValueAt(i, 0))) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void updateVariantCount(GenomicRegion reg, int value) {
        variantCounts.put(reg, value);
        int row = findRow(reg);
        tablePanel.getTable().setValueAt(new Integer(value), row, 4);
        numbersRetrieved++;
        updateProgress();
    }

    public synchronized void updatePatientCount(GenomicRegion reg, int value) {
        patientCounts.put(reg, value);
        int row = findRow(reg);
        tablePanel.getTable().setValueAt(new Integer(value), row, 5);
        numbersRetrieved++;
        updateProgress();
    }

    public void updateRegionSetCombo(List<RegionSet> sets) {
        for (RegionSet s: sets) {
            regionSetCombo.addItem(s);
        }
        fetchRegions(sets.get(0));
    }

    private void fetchRegions(final RegionSet regionSet) {

        if (regionFetcher != null && !regionFetcher.isDone() && !regionFetcher.isCancelled()) {
            regionFetcher.cancel(true);
        }

        numbersRetrieved = 0;
        updateProgress();

        showWaitCard();

        progress.setString("Getting regions for " + regionSet);
        regionFetcher = new MedSavantWorker<List<GenomicRegion>>(pageName) {

            @Override
            protected List<GenomicRegion> doInBackground() throws Exception {
                return RegionController.getInstance().getRegionsInSet(regionSet, limit);
            }

            @Override
            protected void showProgress(double fraction) {}

            @Override
            protected void showSuccess(List<GenomicRegion> result) {
                initGeneTable(result);
            }
        };

        regionFetcher.execute();
    }

    private void updateProgress() {
        int value = 0;
        if (variantCounts.size() != 0) {
            value = numbersRetrieved * 50 / Math.min(variantCounts.keySet().size(), limit);
        }
        value = Math.min(value, 100);
        progress.setValue(value);
        progress.setString(value + "% done ");
        tablePanel.setExportButtonEnabled(value == 100);
    }

    private class AggregationRetriever extends DataRetriever<Object[]> {

        @Override
        public List<Object[]> retrieve(int start, int limit) throws Exception {
            for (int i = 0; i < Math.min(currentRegions.size(), limit); i++) {
                GenomicRegion r = currentRegions.get(i);
                int recordsInRegion = MedSavantClient.VariantManager.getVariantCountInRange(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        FilterController.getInstance().getAllFilterConditions(),
                        r.getChrom(),
                        r.getStart(),
                        r.getEnd());
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                updateVariantCount(r, recordsInRegion);
            }

            //compute patient field
            for (int i = 0; i < Math.min(currentRegions.size(), limit); i++) {
                GenomicRegion r = currentRegions.get(i);
                int recordsInRegion = MedSavantClient.VariantManager.getPatientCountWithVariantsInRange(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        FilterController.getInstance().getAllFilterConditions(),
                        r.getChrom(),
                        r.getStart(),
                        r.getEnd());
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                updatePatientCount(r, recordsInRegion);
            }

            // All the data has been applied, so there's no reason to apply it again.
            return null;
        }

        @Override
        public int getTotalNum() {
            return currentRegions.size();
        }

        @Override
        public void retrievalComplete() {
            // 
            //xxx
        }
    }
}
