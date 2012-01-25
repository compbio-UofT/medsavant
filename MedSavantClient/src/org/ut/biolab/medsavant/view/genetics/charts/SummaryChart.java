/*
 *    Copyright 2011 University of Toronto
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
package org.ut.biolab.medsavant.view.genetics.charts;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.ChartType;
import com.jidesoft.chart.Legend;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.CategoryAxis;
import com.jidesoft.chart.model.ChartCategory;
import com.jidesoft.chart.model.ChartPoint;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.model.InvertibleTransform;
import com.jidesoft.chart.render.AbstractPieSegmentRenderer;
import com.jidesoft.chart.render.DefaultBarRenderer;
import com.jidesoft.chart.render.RaisedPieSegmentRenderer;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.range.CategoryRange;
import com.jidesoft.range.NumericRange;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.MedSavantClient;

import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.RangeCondition;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.Table;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class SummaryChart extends JLayeredPane {

    public boolean doesCompareToOriginal() {
        return this.showComparedToOriginal;
    }

    void setDoesCompareToOriginal(boolean b) {
        this.showComparedToOriginal = b;
        updateDataAndDrawChart();
    }

    public static enum ChartAxis {

        X, Y
    };
    private boolean isLogScaleY = false;
    private boolean isLogScaleX = false;
    private boolean showComparedToOriginal = false;
    private boolean isPie = false;
    private boolean isSorted = false;
    private static final int DEFAULT_NUM_QUANTITATIVE_CATEGORIES = 15;
    private ChartMapWorker mapWorker;
    private ChartMapGenerator mapGenerator;
    private boolean isSortedKaryotypically;
    private String pageName;
    private final Object updateLock = new Object();
    private boolean updateRequired = false;
    private GridBagConstraints c;
    private WaitPanel waitPanel = new WaitPanel("Getting chart data");

    public SummaryChart(final String pageName) {
        this.pageName = pageName;
        setLayout(new GridBagLayout());
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        
        this.add(waitPanel, c, JLayeredPane.MODAL_LAYER);
    }

    public void setIsLogScale(boolean isLogScale, ChartAxis axis) {
        if (!isLogScale) {
            isLogScaleY = false;
            isLogScaleX = false;
        } else {
            isLogScaleY = (axis == ChartAxis.Y);
            isLogScaleX = (axis == ChartAxis.X);
        }
        updateDataAndDrawChart();
    }

    /*public void setIsLogScaleY(boolean isLogscale) {
    this.isLogScaleY = isLogscale;
    updateDataAndDrawChart();
    }

    public void setIsLogScaleX(boolean isLogscale) {
    this.isLogScaleX = isLogscale;
    updateDataAndDrawChart();
    }*/
    public void setIsSorted(boolean isSorted) {
        this.isSorted = isSorted;
        updateDataAndDrawChart();
    }

    public void setIsPie(boolean b) {
        this.isPie = b;
        updateDataAndDrawChart();
    }

    public boolean isLogScaleY() {
        return isLogScaleY;
    }

    public boolean isLogScaleX() {
        return isLogScaleX;
    }

    public boolean isPie() {
        return isPie;
    }

    public boolean isSorted() {
        return isSorted;
    }

    public boolean isSortedKaryotypically() {
        return isSortedKaryotypically;
    }

    public void setChartMapGenerator(ChartMapGenerator cmg) {
        this.mapGenerator = cmg;
        updateDataAndDrawChart();
    }

    public void updateIfRequired() {
        synchronized (updateLock) {
            if (updateRequired) {
                updateRequired = false;
                updateDataAndDrawChart();
            }
        }
    }

    public void setUpdateRequired(boolean required) {
        synchronized (updateLock) {
            updateRequired = required;
        }
    }

    private void updateDataAndDrawChart() {

        //begin creating chart
        new ChartMapWorker().execute();

        //show wait panel
        new Thread() {

            @Override
            public void run() {
                waitPanel.setVisible(true);
                setLayer(waitPanel, JLayeredPane.MODAL_LAYER);
            }
        }.start();
        
    }

    private synchronized void drawChart(ChartFrequencyMap[] chartMaps) {

        ChartFrequencyMap filteredChartMap = chartMaps[0];
        ChartFrequencyMap unfilteredChartMap = null;

        DefaultChartModel filteredChartModel = new DefaultChartModel();
        DefaultChartModel unfilteredChartModel = null;

        if (this.showComparedToOriginal) {
            unfilteredChartMap = ChartFrequencyMap.subtract(chartMaps[1],filteredChartMap);
            unfilteredChartModel = new DefaultChartModel();
        }

        final Chart chart = new Chart(new Dimension(200, 200));


        JPanel panel = new JPanel();
        Legend legend = new Legend(chart, 0);
        panel.add(legend);
        legend.addChart(chart);

        chart.addModel(filteredChartModel,new ChartStyle(Color.blue).withBars());

        if (this.showComparedToOriginal) {
            chart.addModel(unfilteredChartModel,new ChartStyle(Color.gray).withBars());
        }

        chart.setRolloverEnabled(true);
        chart.setSelectionEnabled(true);
        chart.setSelectionShowsOutline(true);
        chart.setSelectionShowsExplodedSegments(true);
        chart.setAntiAliasing(true);
        chart.setBarGap(5);
        chart.setBorder(ViewUtil.getBigBorder());
        chart.setLabellingTraces(true);

        chart.setAnimateOnShow(false);

        //chart.setBarsGrouped(true);

        chart.getSelectionsForModel(filteredChartModel).setSelectionMode(
                (mapGenerator.isNumeric() && !mapGenerator.getFilterId().equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER))
                ? ListSelectionModel.SINGLE_SELECTION
                : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        chart.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popup = createPopup(chart);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        AbstractPieSegmentRenderer rpie = new RaisedPieSegmentRenderer();
        chart.setPieSegmentRenderer(rpie);

        DefaultBarRenderer rbar = new DefaultBarRenderer();
        chart.setBarRenderer(rbar);

        rpie.setSelectionColor(Color.gray);
        rbar.setSelectionColor(Color.gray);

        if (this.isSortedKaryotypically()) {
            filteredChartMap.sortKaryotypically();
            if (this.showComparedToOriginal) { unfilteredChartMap.sortKaryotypically(); }
        }

        if (this.isSorted()) {
            filteredChartMap.sortNumerically();
            if (this.showComparedToOriginal) { chartMaps[1].sortNumerically(); }
        }

        long max = filteredChartMap.getMax();
        List<ChartCategory> chartCategories;
        if (this.showComparedToOriginal) {
            chartCategories = chartMaps[1].getCategories();//unfilteredChartMap.getCategories();
            max = Math.max(max,unfilteredChartMap.getMax());
        } else {
            chartCategories = filteredChartMap.getCategories();
        }

        CategoryRange<String> range = new CategoryRange<String>();

        for (ChartCategory category : chartCategories) {
            range.add(category);
        }

        System.out.println("Adding filtered chart...");
        addEntriesToChart(filteredChartModel, filteredChartMap, chartCategories);
        if (this.showComparedToOriginal) {
            System.out.println("Adding unfiltered chart...");
            addEntriesToChart(unfilteredChartModel, unfilteredChartMap, chartCategories);
        }
        System.out.println("Done adding charts");

        CategoryAxis xaxis = new CategoryAxis(range, "Category");
        chart.setXAxis(xaxis);
        if (this.isLogScaleY()) {
            chart.setYAxis(new Axis(new NumericRange(0, Math.log10(max) * 1.1), "log(Frequency)"));
        } else {
            chart.setYAxis(new Axis(new NumericRange(0, max * 1.1), "Frequency"));
        }
        chart.getXAxis().getLabel().setFont(ViewUtil.getMediumTitleFont());
        chart.getYAxis().getLabel().setFont(ViewUtil.getMediumTitleFont());

        // rotate 90 degrees (using radians)
        chart.getXAxis().setTickLabelRotation(1.57079633);

        if (isPie) {
            chart.setChartType(ChartType.PIE);
        }

        for(int i = 1; i < this.getComponentCount(); i++){
            this.remove(i);
        }
        this.add(chart, c, JLayeredPane.DEFAULT_LAYER);
        waitPanel.setVisible(false);
    }

    private void addEntriesToChart(
            DefaultChartModel chartModel,
            ChartFrequencyMap chartMap,
            List<ChartCategory> chartCategories) {

        for (ChartCategory cat : chartCategories) {
            FrequencyEntry fe = chartMap.getEntry(cat.getName());
            long value = 0;
            if (fe != null) {
                value = fe.getFrequency();
            }

            ChartPoint p = new ChartPoint(cat, value);
            ChartPoint logp = new ChartPoint(cat, Math.log10(value));

            System.out.println("key: " + cat.getName() + " value: " + value);

            if (this.isLogScaleY()) {
                chartModel.addPoint(logp);
            } else {
                chartModel.addPoint(p);
            }
        }
    }

    void setIsSortedKaryotypically(boolean b) {
        this.isSortedKaryotypically = b;
    }

    public class ChartMapWorker extends MedSavantWorker<ChartFrequencyMap[]> {

        @SuppressWarnings("LeakingThisInConstructor")
        ChartMapWorker() {
            super(pageName);
            if (mapWorker != null) {
                mapWorker.cancel(true);
            }
            mapWorker = this;
        }

        @Override
        protected ChartFrequencyMap[] doInBackground() throws Exception {
            if (mapGenerator == null) {
                return null;
            }
            if (this.isThreadCancelled()) {
                return null;
            }
            try {

                ChartFrequencyMap[] result;

                if (showComparedToOriginal) {
                    result = new ChartFrequencyMap[2];
                    result[1] = mapGenerator.generateChartMap(false, isLogScaleX && mapGenerator.isNumeric());
                } else {
                    result = new ChartFrequencyMap[1];
                }

                result[0] = mapGenerator.generateChartMap(true, isLogScaleX && mapGenerator.isNumeric());

                return result;
            } catch (SQLException ex) {
                MiscUtils.checkSQLException(ex);
                throw ex;
            }
        }

        public void showSuccess(ChartFrequencyMap[] result) {
            if (result != null) {
                drawChart(result);
            }
        }

        public void showProgress(double prog) {
            if (prog == 1.0) {
                mapWorker = null;
                //removeAll();        // Clear away the WaitPanel.
            }
        }
    }

    static class ValueComparator implements Comparator {

        Map base;

        public ValueComparator(Map base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {

            if ((Integer) base.get(a) < (Integer) base.get(b)) {
                return 1;
            } else if ((Integer) base.get(a) == (Integer) base.get(b)) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    class LogTransform implements InvertibleTransform<Double> {

        public Double transform(Double pos) {
            return Math.log10(pos);
        }

        public Double inverseTransform(Double t) {
            return Math.pow(10, t);
        }
    }

    private JPopupMenu createPopup(final Chart chart) {

        JPopupMenu menu = new JPopupMenu();

        //Filter by selections
        JMenuItem filter1Item = new JMenuItem("Filter by Selection" + (mapGenerator.isNumeric() ? "" : "(s)"));
        filter1Item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                ThreadController.getInstance().cancelWorkers(pageName);

                List<String> values = new ArrayList<String>();
                ListSelectionModel selectionModel = chart.getSelectionsForModel(chart.getModel());
                for (int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); i++) {
                    if (selectionModel.isSelectedIndex(i)) {
                        values.add(((ChartPoint) chart.getModel().getPoint(i)).getHighlight().name());
                    }
                }
                if (values.isEmpty()) {
                    return;
                }

                TableSchema variantTable = ProjectController.getInstance().getCurrentVariantTableSchema();
                TableSchema patientTable = ProjectController.getInstance().getCurrentPatientTableSchema();
                if (mapGenerator.isNumeric() && !mapGenerator.getFilterId().equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)) {

                    Range r = Range.rangeFromString(values.get(0));

                    if (mapGenerator.getTable() == Table.VARIANT) {
                        RangeCondition condition = new RangeCondition(variantTable.getDBColumn(mapGenerator.getFilterId()), r.getMin(), r.getMax());
                        FilterUtils.createAndApplyGenericFixedFilter(
                                "Charts - Filter by Selection",
                                mapGenerator.getName() + ": " + r.getMin() + " - " + r.getMax(),
                                ComboCondition.and(condition));
                    } else {
                        try {
                            List<String> individuals = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsWithValuesInRange(
                                    LoginController.sessionId,
                                    ProjectController.getInstance().getCurrentProjectId(),
                                    mapGenerator.getFilterId(),
                                    r);
                            Condition[] conditions = new Condition[individuals.size()];
                            for (int i = 0; i < individuals.size(); i++) {
                                conditions[i] = BinaryConditionMS.equalTo(variantTable.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), individuals.get(i));
                            }
                            FilterUtils.createAndApplyGenericFixedFilter(
                                    "Charts - Filter by Selection",
                                    mapGenerator.getName() + ": " + r.getMin() + " - " + r.getMax(),
                                    ComboCondition.or(conditions));
                        } catch (SQLException ex) {
                            MiscUtils.checkSQLException(ex);
                        } catch (Exception ex) {
                            Logger.getLogger(SummaryChart.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                } else {

                    if (mapGenerator.getTable() == Table.VARIANT) {
                        Condition[] conditions = new Condition[values.size()];
                        for (int i = 0; i < conditions.length; i++) {
                            conditions[i] = BinaryConditionMS.equalTo(variantTable.getDBColumn(mapGenerator.getFilterId()), values.get(i));
                        }
                        FilterUtils.createAndApplyGenericFixedFilter(
                                "Charts - Filter by Selection",
                                mapGenerator.getName() + ": " + values.size() + " selection(s)",
                                ComboCondition.or(conditions));
                    } else {
                        try {

                            //special case for gender
                            if (mapGenerator.getFilterId().equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)) {
                                List<String> values1 = new ArrayList<String>();
                                for (String s : values) {
                                    values1.add(Integer.toString(MiscUtils.stringToGender(s)));
                                }
                                values = values1;
                            }

                            List<String> individuals = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsForStringList(
                                    LoginController.sessionId,
                                    patientTable,
                                    values,
                                    mapGenerator.getFilterId());
                            Condition[] conditions = new Condition[individuals.size()];
                            for (int i = 0; i < individuals.size(); i++) {
                                conditions[i] = BinaryConditionMS.equalTo(variantTable.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), individuals.get(i));
                            }
                            FilterUtils.createAndApplyGenericFixedFilter(
                                    "Charts - Filter by Selection",
                                    mapGenerator.getName() + ": " + values.size() + " selection(s)",
                                    ComboCondition.or(conditions));
                        } catch (SQLException ex) {
                            MiscUtils.checkSQLException(ex);
                        } catch (Exception ex) {
                            Logger.getLogger(SummaryChart.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                //updateDataAndDrawChart();
            }
        });
        menu.add(filter1Item);

        return menu;
    }
}
