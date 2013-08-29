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
package medsavant.enrichment.app;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import medsavant.enrichment.app.AggregatePanel;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.region.RegionController;
import org.ut.biolab.medsavant.client.util.DataRetriever;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.ViewController;
import org.ut.biolab.medsavant.client.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.genetics.QueryUtils;
import org.ut.biolab.medsavant.client.view.variants.BrowserPage;
import savant.controller.LocationController;
import savant.util.Range;

/**
 *
 * @author mfiume
 */
public class RegionListAggregatePanel extends AggregatePanel {

    private static final Log LOG = LogFactory.getLog(RegionListAggregatePanel.class);

    private static final int VARIANT_COLUMN = 5;
    private static final int VARIANTPERKB_COLUMN = 6;
    private static final int PATIENT_COLUMN = 7;
    private final JComboBox regionSetCombo;
    private final JPanel mainPanel;
    private MedSavantWorker regionFetcher;
    private final TreeMap<GenomicRegion, Integer> variantCounts;
    private SearchableTablePanel tablePanel;
    private List<GenomicRegion> currentRegions;
    private final TreeMap<GenomicRegion, Integer> patientCounts;
    private final JProgressBar progress;

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

        banner.setBackground(new Color(245, 245, 245));
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
                fetchRegions((RegionSet) regionSetCombo.getSelectedItem());
            }
        });

        new MedSavantWorker<List<RegionSet>>(pageName) {
            @Override
            protected List<RegionSet> doInBackground() throws Exception {
                return RegionController.getInstance().getRegionSets();
            }

            @Override
            protected void showProgress(double fraction) {
            }

            @Override
            protected void showSuccess(List<RegionSet> result) {
                if (!result.isEmpty()) {
                    updateRegionSetCombo(result);
                }
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
                new String[]{"Name", "Chromosome", "Start", "End", "Length", "Variants", "Variants / KB", "Individuals"},
                new Class[]{String.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, Double.class, Integer.class},
                new int[0], true, true, Integer.MAX_VALUE, false, SearchableTablePanel.TableSelectionType.ROW, Integer.MAX_VALUE,
                new AggregationRetriever());

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

        if (selRows.length == 1) {
            JMenuItem browseItem = new JMenuItem(String.format("<html>Look at %s in genome browser</html>", "<i>" + model.getValueAt(selRows[0], 0) + "</i>" ));
            browseItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    TableModel model = tablePanel.getTable().getModel();
                    int r = selRows[0];
                    LocationController.getInstance().setLocation((String) model.getValueAt(r, 1), new Range((Integer) model.getValueAt(r, 2), (Integer) model.getValueAt(r, 3)));
                    ViewController.getInstance().getMenu().switchToSubSection(BrowserPage.getInstance());
                }
            });
            menu.add(browseItem);
        }


        JMenuItem posItem = new JMenuItem(String.format("<html>Filter by %s</html>", selRows.length == 1 ? "Region <i>" + model.getValueAt(selRows[0], 0) + "</i>" : "Selected Regions"));
        posItem.addActionListener(new ActionListener() {


            @Override
            public void actionPerformed(ActionEvent ae) {
                ThreadController.getInstance().cancelWorkers(pageName);

                List<GenomicRegion> regions = new ArrayList<GenomicRegion>();
                TableModel model = tablePanel.getTable().getModel();

                for (int r : selRows) {
                    String geneName = (String) model.getValueAt(r, 0);
                    String chrom = (String)model.getValueAt(r,1);
                    Integer start = (Integer) model.getValueAt(r, 2);
                    Integer end = (Integer) model.getValueAt(r, 3);

                    regions.add(new GenomicRegion(geneName, chrom, start, end));
                }

                QueryUtils.addQueryOnRegions(regions, Arrays.asList(new RegionSet[]{(RegionSet) regionSetCombo.getSelectedItem()}));

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
        for (GenomicRegion r : currentRegions) {
            variantCounts.put(r, null);
            patientCounts.put(r, null);
        }
        JTable t = tablePanel.getTable();
        for (int i = 0; i < t.getRowCount(); i++) {
            t.setValueAt("", i, VARIANT_COLUMN);
            t.setValueAt("", i, VARIANTPERKB_COLUMN);
            t.setValueAt("", i, PATIENT_COLUMN);
        }
    }

    private void initGeneTable(List<GenomicRegion> genes) {
        currentRegions = genes;
        resetCounts();
        updateProgress(0.0);

        showShowCard();

        List<Object[]> data = new ArrayList<Object[]>();

        for (GenomicRegion r : variantCounts.keySet()) {
            data.add(new Object[]{
                        r.getName(), r.getChrom(), r.getStart(), r.getEnd(), r.getLength(), variantCounts.get(r), variantCounts.get(r) == null ? null : variantCounts.get(r)/((double)r.getLength()/1000), patientCounts.get(r)
                    });
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

    public void updateVariantCount(GenomicRegion reg, int value) {
        variantCounts.put(reg, value);
        int row = findRow(reg);
        tablePanel.getTable().setValueAt(new Integer(value), row, VARIANT_COLUMN);
        tablePanel.getTable().setValueAt(new Integer(value)/((double)reg.getLength()/1000), row, VARIANTPERKB_COLUMN);
    }

    public void updatePatientCount(GenomicRegion reg, int value) {
        patientCounts.put(reg, value);
        int row = findRow(reg);
        tablePanel.getTable().setValueAt(new Integer(value), row, PATIENT_COLUMN);
    }

    public void updateRegionSetCombo(List<RegionSet> sets) {
        for (RegionSet s : sets) {
            regionSetCombo.addItem(s);
        }
        fetchRegions(sets.get(0));
    }

    private void fetchRegions(final RegionSet regionSet) {

        if (regionFetcher != null && !regionFetcher.isDone() && !regionFetcher.isCancelled()) {
            regionFetcher.cancel(true);
        }

        updateProgress(0.0);

        showWaitCard();

        progress.setString("Getting regions for " + regionSet);
        regionFetcher = new MedSavantWorker<List<GenomicRegion>>(pageName) {
            @Override
            protected List<GenomicRegion> doInBackground() throws Exception {
                return RegionController.getInstance().getRegionsInSet(regionSet);
            }

            @Override
            protected void showProgress(double fraction) {
            }

            @Override
            protected void showSuccess(List<GenomicRegion> result) {
                initGeneTable(result);
            }
        };

        regionFetcher.execute();
    }

    private void updateProgress(double prog) {
        progress.setValue((int) prog);
        progress.setString(String.format("%.1f%%", prog));
        progress.setVisible(true);
        tablePanel.setExportButtonEnabled(prog == 1.0);
    }

    private class AggregationRetriever extends DataRetriever<Object[]> {

        @Override
        public List<Object[]> retrieve(int start, int limit) throws Exception {
            int max = currentRegions.size();
            for (int i = 0; i < max; i++) {
                GenomicRegion reg = currentRegions.get(i);
                //System.out.println("Retrieving " + i + " of " + max + " but there are " + currentRegions.size());
                int recordsInRegion = MedSavantClient.VariantManager.getVariantCountInRange(
                        LoginController.getInstance().getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        FilterController.getInstance().getAllFilterConditions(),
                        reg.getChrom(),
                        reg.getStart(),
                        reg.getEnd());
                updateVariantCount(reg, recordsInRegion);
                updateProgress(100.0 * (i + 0.5) / max);
                recordsInRegion = MedSavantClient.VariantManager.getPatientCountWithVariantsInRange(
                        LoginController.getInstance().getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        FilterController.getInstance().getAllFilterConditions(),
                        reg.getChrom(),
                        (int)reg.getStart(),
                        (int)reg.getEnd());
                updatePatientCount(reg, recordsInRegion);
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                updateProgress(100.0 * (i + 1.0) / max);
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
            progress.setVisible(false);
        }
    }
}
