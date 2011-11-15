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

package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.ChartType;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.CategoryAxis;
import com.jidesoft.chart.model.ChartCategory;
import com.jidesoft.chart.model.ChartPoint;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.model.Highlight;
import com.jidesoft.chart.model.InvertibleTransform;
import com.jidesoft.chart.render.AbstractPieSegmentRenderer;
import com.jidesoft.chart.render.RaisedPieSegmentRenderer;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.range.CategoryRange;
import com.jidesoft.range.NumericRange;

import java.util.concurrent.ExecutionException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.CustomTables;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.view.genetics.charts.ChartFrequencyMap;
import org.ut.biolab.medsavant.view.genetics.charts.FrequencyEntry;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class ChartPanel extends JPanel {
    private static final Logger LOG = Logger.getLogger(ChartPanel.class.getName());

    //private int currentKeyIndex = VariantRecordModel.INDEX_OF_REF;
    private JToolBar bar;
    private boolean isLogscale = false;
    private boolean isPie = false;
    private boolean isSorted = false;
    private JCheckBox isPieCB;
    private JCheckBox isSortedCB;
    private JCheckBox isLogarithmicCB;
    private SpinnerNumberModel numberModel;
    private static final int DEFAULT_NUM_QUANTITATIVE_CATEGORIES = 10;
    private JToolBar bottombar;
    
    private List<String> chartNames;
    private String currentChart;
    private ChartMapWorker cmsw;
    
    public ChartPanel() {
        this.setLayout(new BorderLayout());
        initToolBar();
        updateDataAndDrawChart();
        FilterController.addFilterListener(new FiltersChangedListener() {
            public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
                updateDataAndDrawChart();
            }    
        });
    }

    private void updateDataAndDrawChart() {

        this.removeAll();
        this.add(new WaitPanel("Getting chart data"), BorderLayout.CENTER);
        this.updateUI();
        
        // kill existing thread, if any
        if (cmsw != null && !cmsw.isDone()) { cmsw.cancel(true); }
        
        cmsw = new ChartMapWorker(currentChart,isSorted);
        cmsw.execute();
        
    }
    
    private synchronized void drawChart(ChartFrequencyMap chartMap) {
        DefaultChartModel chartModel = new DefaultChartModel();

        Chart chart = new Chart(new Dimension(200, 200));
        chart.setRolloverEnabled(true);
        chart.setSelectionEnabled(true);
        chart.setSelectionShowsOutline(true);
        chart.setSelectionShowsExplodedSegments(true);

        AbstractPieSegmentRenderer r = new RaisedPieSegmentRenderer();
        chart.setPieSegmentRenderer(r);

        CategoryRange<String> categories = new CategoryRange<String>();
        int max = Integer.MIN_VALUE;

        for (FrequencyEntry fe : chartMap.getEntries()) {
            String key = fe.getKey();
            int value = fe.getFrequency();
            ChartCategory cat = new ChartCategory<String>(key);
            categories.add(cat);
            Highlight h = new Highlight(key);
            chart.setHighlightStyle(h, new ChartStyle(Util.getRandomColor()));
            max = Math.max(max, value);
            ChartPoint p = new ChartPoint(cat, value);
            ChartPoint logp = new ChartPoint(cat, Math.log10(value));
            p.setHighlight(h);
            logp.setHighlight(h);
            if (this.isLogScale()) {
                chartModel.addPoint(logp);
            } else {
                chartModel.addPoint(p);
            }
        }

        ChartStyle s = new ChartStyle();
        s.setBarsVisible(true);
        s.setLinesVisible(false);

        chart.setXAxis(new CategoryAxis(categories, "Category"));
        if (this.isLogScale()) {
             chart.setYAxis(new Axis(new NumericRange(0, Math.log10(max)), "log(Frequency)"));
        } else {
            chart.setYAxis(new Axis(new NumericRange(0, max), "Frequency"));
        }
        chart.addModel(chartModel);
        chart.setStyle(chartModel, s);

        if (isPie) {
            chart.setChartType(ChartType.PIE);
        }

        this.removeAll();
        this.add(bar, BorderLayout.NORTH);
        this.add(chart, BorderLayout.CENTER);
        this.add(bottombar, BorderLayout.SOUTH);
    }

    private void initToolBar() {
        bar = new JToolBar();
        bottombar = new JToolBar();
        bottombar.setFloatable(false);
        JComboBox b = new JComboBox();

        TableSchema table = null;
        try {
            table = CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(ProjectController.getInstance().getCurrentProjectId(), ReferenceController.getInstance().getCurrentReferenceId()));
        } catch (SQLException ex) {
            Logger.getLogger(ChartPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        chartNames = table.getFieldAliases();
        for (String chartName : chartNames) {
            b.addItem(chartName);
        }
        
        setCurrentChart(chartNames.get(0));
        
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String fieldName = (String) cb.getSelectedItem();
                setCurrentChart(fieldName);
                updateDataAndDrawChart();
            }
        });

        bar.setFloatable(false);
        bar.add(b);

        bar.add(Box.createHorizontalStrut(5));

        isPieCB = new JCheckBox("Pie");
        isPieCB.setSelected(isPie);
        isPieCB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setIsPie(isPieCB.isSelected());
                updateDataAndDrawChart();
            }
        });
        bottombar.add(isPieCB);

        bottombar.add(Box.createHorizontalStrut(5));

        isSortedCB = new JCheckBox("Sort");
        isSortedCB.setSelected(isSorted);
        isSortedCB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setSortByFrequency(isSortedCB.isSelected());
                updateDataAndDrawChart();
            }
        });
        bottombar.add(isSortedCB);

        bottombar.add(Box.createHorizontalStrut(5));

        isLogarithmicCB = new JCheckBox("Log");
        isLogarithmicCB.setSelected(isLogscale);
        isLogarithmicCB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setIsLogarithmic(isLogarithmicCB.isSelected());
                updateDataAndDrawChart();
            }
        });
        bottombar.add(isLogarithmicCB);

        bottombar.add(Box.createHorizontalStrut(5));

        bottombar.add(new JLabel("Bins:"));

        bottombar.add(Box.createHorizontalGlue());

        numberModel = new SpinnerNumberModel();
        numberModel.setStepSize(2);
        numberModel.setMinimum(1);
        numberModel.setMaximum(100);
        numberModel.setValue(DEFAULT_NUM_QUANTITATIVE_CATEGORIES);
        JSpinner numberSpinner = new JSpinner(numberModel);
        numberModel.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                updateDataAndDrawChart();
            }
        });
        bottombar.add(numberSpinner);

        this.add(bar, BorderLayout.NORTH);
        this.add(bottombar, BorderLayout.SOUTH);
    }

    public void setIsPie(boolean b) {
        this.isPie = b;
    }

    private void setIsLogarithmic(boolean selected) {
        this.isLogscale = selected;
    }

    public void setSortByFrequency(boolean b) {
        this.isSorted = b;
    }

    public void setCurrentChart(String chartName) {
        this.currentChart = chartName;
    }

    private int getNumberOfQuantitativeCategories() {
        return (Integer) numberModel.getValue();
    }

    public class ChartMapWorker extends SwingWorker<ChartFrequencyMap, Object> {
        
        private final String fieldName;
        private final boolean isSorted;
        
        public ChartMapWorker(String fieldName, boolean isSorted) {
            this.fieldName = fieldName;
            this.isSorted = isSorted;
        }
            
        protected ChartFrequencyMap doInBackground() throws Exception {

            ChartFrequencyMap chartMap = new ChartFrequencyMap();
            
            TableSchema table = CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(ProjectController.getInstance().getCurrentProjectId(), ReferenceController.getInstance().getCurrentReferenceId()));
            DbColumn column = table.getDBColumnByAlias(fieldName);
            
            ColumnType type = table.getColumnType(column);
            
            if (TableSchema.isNumeric(type)) {
                
                Range r = org.ut.biolab.medsavant.db.util.query.QueryUtil.getExtremeValuesForColumn(table, column);
                
                int numBins = getNumberOfQuantitativeCategories();
                
                int min = (r.getMin() > 0) ? 0 : (int) Math.floor(r.getMin());
                int max = (int) Math.ceil(r.getMax());
                
                double step = ((double) (max - min)) / numBins;

                for (int i = 0; i < numBins; i++) {
                    Range binrange = new Range((int) (min + i * step), (int) (min + (i + 1) * step));
                    chartMap.addEntry(
                            binrange.toString(), 
                            VariantQueryUtil.getFilteredFrequencyValuesForColumnInRange(
                                    ProjectController.getInstance().getCurrentProjectId(), 
                                    ReferenceController.getInstance().getCurrentReferenceId(), 
                                    FilterController.getQueryFilterConditions(), 
                                    fieldName, 
                                    binrange.getMin(), 
                                    binrange.getMax())
                            );
                }

            } else {
                chartMap.addAll(VariantQueryUtil.getFilteredFrequencyValuesForColumn(table.getTable(), FilterController.getQueryFilterConditions(), column));
                Collections.sort(chartMap.getEntries());
            }
            
            return chartMap;
        }

        @Override
        protected void done() {
            try {
                drawChart(get());
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
                DialogUtils.displayException("MedSavant", "Unable to get chart data.", ex);
                removeAll();
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

    public boolean isLogScale() {
        return isLogscale;
    }

    class LogTransform implements InvertibleTransform<Double> {

        public Double transform(Double pos) {
            return Math.log10(pos);
        }

        public Double inverseTransform(Double t) {
            return Math.pow(10, t);
        }
    }
}
