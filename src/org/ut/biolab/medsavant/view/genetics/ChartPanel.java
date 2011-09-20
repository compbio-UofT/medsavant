/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

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
import com.jidesoft.utils.SwingWorker;
import fiume.vcf.VariantRecord;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.oldcontroller.FilterController;
import org.ut.biolab.medsavant.oldcontroller.ResultController;
import org.ut.biolab.medsavant.olddb.ConnectionController;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.olddb.table.TableSchema;
import org.ut.biolab.medsavant.olddb.table.TableSchema.ColumnType;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.view.genetics.charts.ChartFrequencyMap;
import org.ut.biolab.medsavant.view.genetics.charts.FrequencyEntry;
import org.ut.biolab.medsavant.view.util.DialogUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class ChartPanel extends JPanel implements FiltersChangedListener {

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
    private ChartMapSW cmsw;
    
    public ChartPanel() {
        this.setLayout(new BorderLayout());
        initToolBar();
        updateDataAndDrawChart();
        FilterController.addFilterListener(this);
    }

    private void updateDataAndDrawChart() {

        this.removeAll();
        this.add(new WaitPanel("Getting chart data"), BorderLayout.CENTER);
        this.updateUI();
        
        // kill existing thread, if any
        if (cmsw != null && !cmsw.isDone()) { cmsw.cancel(true); }
        
        cmsw = new ChartMapSW(currentChart,isSorted);
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

    private static void printHist(Map<String, Integer> chartMap) {
        if (true) {
            return;
        }
        System.out.println("Printing hist: ");
        for (String s : chartMap.keySet()) {
            System.out.println(s + ": " + chartMap.get(s));
        }
    }

    private void initToolBar() {
        bar = new JToolBar();
        bottombar = new JToolBar();
        bottombar.setFloatable(false);
        JComboBox b = new JComboBox();

        
        chartNames = MedSavantDatabase.getInstance().getVariantTableSchema().getFieldAliases();
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

    public class ChartMapSW extends SwingWorker {
        
        private final String fieldName;
        private final boolean isSorted;
        
        public ChartMapSW(String fieldName, boolean isSorted) {
            this.fieldName = fieldName;
            this.isSorted = isSorted;
        }
            
        private ChartFrequencyMap getChartMap(String fieldName, boolean isSorted) throws Exception {

            System.out.println("Threading chart retrieval for " + fieldName);
            
            ChartFrequencyMap chartMap = new ChartFrequencyMap();
            
            TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();
            DbColumn column = table.getDBColumn(fieldName);
            
            ColumnType type = table.getColumnType(column);
            
            if (TableSchema.isNumeric(type)) {
                
                Range r = QueryUtil.getExtremeValuesForColumn(ConnectionController.connect(), table, column);
                System.err.println("Need to do something for range " + r.toString());
                
                int numBins = getNumberOfQuantitativeCategories();
                
                int min = (int) Math.floor(r.getMin());
                int max = (int) Math.ceil(r.getMax());
                
                double step = ((double) (max - min)) / numBins;

                for (int i = 0; i < numBins; i++) {
                    Range binrange = new Range((int) (min + i * step), (int) (min + (i + 1) * step));
                    chartMap.addEntry(
                            binrange.toString(), 
                            QueryUtil.getFilteredFrequencyValuesForColumnInRange(ConnectionController.connect(), column, binrange)
                            );
                }

            } else {
                try {
                    chartMap.addAll(QueryUtil.getFilteredFrequencyValuesForColumn(ConnectionController.connect(), column));
                    Collections.sort(chartMap.getEntries());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            /*
            Class c = VariantRecordModel.getFieldClass(fieldIndex);
            
            
            if (Util.isQuantatitiveClass(c)) {
                int numBins = getNumberOfQuantitativeCategories();
                List<Double> numbers = new ArrayList<Double>();
                Double min = Double.MAX_VALUE;
                Double max = Double.MIN_VALUE;

                // TODO: replace with database call


                DBFilterUtil.get


                for (VariantRecord r : ResultController.getInstance().getFilteredVariantRecords()) {
                    Object numericvalue = VariantRecordModel.getValueOfFieldAtIndex(fieldIndex, r);
                    Double v = Double.parseDouble(numericvalue.toString());
                    min = Math.min(min, v);
                    max = Math.max(max, v);
                    numbers.add(v);
                }

                max = max + 1;
                Double step = (max - min) / numBins;
                int[] bins = new int[numBins];

                for (Double d : numbers) {
                    int binnumber = (int) ((d - min) / step);
                    bins[binnumber]++;
                }

                for (int i = 0; i < numBins; i++) {
                    chartMap.put(((int) (min + i * step)) + " - " + ((int) (min + (i + 1) * step)), bins[i]);
                }

            } else {

                // TODO: replace with database call
                for (VariantRecord r : ResultController.getInstance().getFilteredVariantRecords()) {
                    String key = null;
                    Object value = VariantRecordModel.getValueOfFieldAtIndex(fieldIndex, r);
                    if (value == null) {
                        key = ".";
                    } else {
                        key = value.toString();
                    }
                    if (chartMap.containsKey(key)) {
                        chartMap.put(key, chartMap.get(key) + 1);
                    } else {
                        chartMap.put(key, 1);
                    }
                }
            }

            if (isSorted) {
                ValueComparator bvc = new ValueComparator(chartMap);
                Map m = new TreeMap<String, Integer>(bvc);
                m.putAll(chartMap);
                chartMap = m;
            }
             * 
             */

            return chartMap;
        }

        @Override
        protected Object doInBackground() throws Exception {
            return getChartMap(fieldName,isSorted);
        }
        
        protected void done() {
            try {
                ChartFrequencyMap chartMap = (ChartFrequencyMap) get();
                drawChart(chartMap);
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(ChartPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void filtersChanged() {
        updateDataAndDrawChart();
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
