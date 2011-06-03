/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.subview;

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
import fiume.vcf.VariantRecord;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.view.gadget.CollapsibleFrameGadget;
import org.ut.biolab.medsavant.view.util.DialogUtil;

/**
 *
 * @author mfiume
 */
public class ChartPanel extends JPanel implements FiltersChangedListener {

    private int currentKeyIndex = VariantRecordModel.INDEX_OF_REF;
    private JToolBar bar;
    private boolean isLogscale = false;
    private boolean isPie = false;
    private boolean isSorted = false;
    private JCheckBox isPieCB;
    private JCheckBox isSortedCB;
    private JCheckBox isLogarithmicCB;
    private SpinnerNumberModel numberModel;
    private static final int DEFAULT_NUM_QUANTITATIVE_CATEGORIES = 5;
    private JToolBar bottombar;

    public ChartPanel() {
        this.setLayout(new BorderLayout());
        initToolBar();
        updateChartMap();
        FilterController.addFilterListener(this);
    }

    private void updateChartMap() {

        Map<String, Integer> chartMap;
        try {
            chartMap = getChartMap(currentKeyIndex, isSorted);
        } catch (Exception ex) {
            Logger.getLogger(ChartPanel.class.getName()).log(Level.SEVERE, null, ex);
            DialogUtil.displayErrorMessage("Problem getting data.", ex);
            return;
        }

        printHist(chartMap);

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

        for (String key : chartMap.keySet()) {
            ChartCategory cat = new ChartCategory<String>(key);
            categories.add(cat);
            Highlight h = new Highlight(key);
            chart.setHighlightStyle(h, new ChartStyle(Util.getRandomColor()));
            int value = chartMap.get(key);
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
        JComboBox b = new JComboBox();

        Vector v = VariantRecordModel.getFieldNames();
        for (int i = 0; i < VariantRecordModel.getNumberOfFields(); i++) {
            b.addItem(v.get(i));
        }

        b.setSelectedIndex(currentKeyIndex);

        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String fieldName = (String) cb.getSelectedItem();
                setCurrentKeyIndex(VariantRecordModel.getIndexOfField(fieldName));
                updateChartMap();
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
                updateChartMap();
            }
        });
        bottombar.add(isPieCB);

        bottombar.add(Box.createHorizontalStrut(5));

        isSortedCB = new JCheckBox("Sort");
        isSortedCB.setSelected(isSorted);
        isSortedCB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setSortByFrequency(isSortedCB.isSelected());
                updateChartMap();
            }
        });
        bottombar.add(isSortedCB);

        bottombar.add(Box.createHorizontalStrut(5));

        isLogarithmicCB = new JCheckBox("Log");
        isLogarithmicCB.setSelected(isLogscale);
        isLogarithmicCB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setIsLogarithmic(isLogarithmicCB.isSelected());
                updateChartMap();
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
                updateChartMap();
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

    public void setCurrentKeyIndex(int currentKeyIndex) {
        this.currentKeyIndex = currentKeyIndex;
    }

    private int getNumberOfQuantitativeCategories() {
        return (Integer) numberModel.getValue();
    }

    private Map<String, Integer> getChartMap(int fieldIndex, boolean isSorted) throws Exception {

        Map<String, Integer> chartMap = new TreeMap<String, Integer>();

        Class c = VariantRecordModel.getFieldClass(fieldIndex);
        if (Util.isQuantatitiveClass(c)) {
            int numBins = getNumberOfQuantitativeCategories();
            List<Double> numbers = new ArrayList<Double>();
            Double min = Double.MAX_VALUE;
            Double max = Double.MIN_VALUE;
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
            for (VariantRecord r : ResultController.getInstance().getFilteredVariantRecords()) {
                String key = (String) VariantRecordModel.getValueOfFieldAtIndex(fieldIndex, r);
                if (key == null) {
                    key = ".";
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

        return chartMap;
    }

    public void filtersChanged() {
        updateChartMap();
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
