/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.charts;

import org.ut.biolab.medsavant.view.genetics.charts.ChartMapGenerator;
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
import com.jidesoft.chart.render.DefaultBarRenderer;
import com.jidesoft.chart.render.RaisedPieSegmentRenderer;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.range.CategoryRange;
import com.jidesoft.range.NumericRange;
import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.oldcontroller.FilterController;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class SummaryChart extends JPanel implements FiltersChangedListener {

    private boolean isLogscale = false;
    private boolean isPie = false;
    private boolean isSorted = false;
    private static final int DEFAULT_NUM_QUANTITATIVE_CATEGORIES = 15;
    //private String currentChart;
    private ChartMapSW cmsw;
    private ChartMapGenerator mapGenerator;
    private boolean isSortedKaryotypically;

    public SummaryChart() {
        this.setLayout(new BorderLayout());
        //updateDataAndDrawChart();
        FilterController.addFilterListener(this);
    }

    public void setIsLogscale(boolean isLogscale) {
        this.isLogscale = isLogscale;
        updateDataAndDrawChart();
    }

    public void setIsSorted(boolean isSorted) {
        this.isSorted = isSorted;
        updateDataAndDrawChart();
    }

    public void setIsPie(boolean b) {
        this.isPie = b;
        updateDataAndDrawChart();
    }

    public boolean isLogscale() {
        return isLogscale;
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

    private void updateDataAndDrawChart() {

        this.removeAll();
        this.add(new WaitPanel("Getting chart data"), BorderLayout.CENTER);
        this.updateUI();

        // kill existing thread, if any
        if (cmsw != null && !cmsw.isDone()) {
            try {
                cmsw.cancel(true);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        cmsw = new ChartMapSW();
        cmsw.execute();
    }

    private synchronized void drawChart(ChartFrequencyMap chartMap) {
        DefaultChartModel chartModel = new DefaultChartModel();

        Chart chart = new Chart(new Dimension(200, 200));
        chart.setRolloverEnabled(true);
        chart.setSelectionEnabled(true);
        chart.setSelectionShowsOutline(true);
        chart.setSelectionShowsExplodedSegments(true);
        chart.setAntiAliasing(true);
        chart.setBarGap(5);
        chart.setBorder(ViewUtil.getBigBorder());
        chart.setLabellingTraces(true);

        AbstractPieSegmentRenderer rpie = new RaisedPieSegmentRenderer();
        chart.setPieSegmentRenderer(rpie);


        DefaultBarRenderer rbar = new DefaultBarRenderer();
        chart.setBarRenderer(rbar);

        rpie.setSelectionColor(Color.gray);
        rbar.setSelectionColor(Color.gray);

        CategoryRange<String> categories = new CategoryRange<String>();
        int max = Integer.MIN_VALUE;

        boolean isnumeric = mapGenerator.isNumeric();

        Color c = ViewUtil.getColor(1);
        int entry = 0;
        
        if (this.isSortedKaryotypically()) {
            chartMap.sortKaryotypically();
        } else
        if (this.isSorted()) {
            chartMap.sortNumerically();
        }

        for (FrequencyEntry fe : chartMap.getEntries()) {

            if (!isnumeric || isPie) {
                c = ViewUtil.getColor(entry++, chartMap.getEntries().size());
            }

            String key = fe.getKey();
            int value = fe.getFrequency();
            ChartCategory cat = new ChartCategory<String>(key);
            categories.add(cat);
            Highlight h = new Highlight(key);
            chart.setHighlightStyle(h, new ChartStyle(c));
            max = Math.max(max, value);
            ChartPoint p = new ChartPoint(cat, value);
            ChartPoint logp = new ChartPoint(cat, Math.log10(value));

            p.setHighlight(h);
            logp.setHighlight(h);
            if (this.isLogscale()) {
                chartModel.addPoint(logp);
            } else {
                chartModel.addPoint(p);
            }
        }

        ChartStyle s = new ChartStyle();
        s.setBarsVisible(true);
        s.setLinesVisible(false);

        CategoryAxis xaxis = new CategoryAxis(categories, "Category");
        chart.setXAxis(xaxis);
        if (this.isLogscale()) {
            chart.setYAxis(new Axis(new NumericRange(0, Math.log10(max)), "log(Frequency)"));
        } else {
            chart.setYAxis(new Axis(new NumericRange(0, max), "Frequency"));
        }
        chart.getXAxis().getLabel().setFont(ViewUtil.getMediumTitleFont());
        chart.getYAxis().getLabel().setFont(ViewUtil.getMediumTitleFont());

        // rotate 90 degrees (using radians)
        chart.getXAxis().setTickLabelRotation(1.57079633);

        chart.addModel(chartModel);
        chart.setStyle(chartModel, s);

        if (isPie) {
            chart.setChartType(ChartType.PIE);
        }

        this.removeAll();
        //this.add(bar, BorderLayout.NORTH);
        this.add(chart, BorderLayout.CENTER);
        //this.add(bottombar, BorderLayout.SOUTH);
    }

    void setIsSortedKaryotypically(boolean b) {
        this.isSortedKaryotypically = b;
    }

    public class ChartMapSW extends SwingWorker {

        @Override
        protected Object doInBackground() throws Exception {
            try {
                if (mapGenerator == null) { return null; }
                return mapGenerator.generateChartMap();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        protected void done() {
            try {
                ChartFrequencyMap chartMap = (ChartFrequencyMap) get();
                if (chartMap == null) { return; }
                drawChart(chartMap);
            } catch (Exception ex) {
                //ex.printStackTrace();
                //Logger.getLogger(SummaryChart.class.getName()).log(Level.SEVERE, null, ex);
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

    class LogTransform implements InvertibleTransform<Double> {

        public Double transform(Double pos) {
            return Math.log10(pos);
        }

        public Double inverseTransform(Double t) {
            return Math.pow(10, t);
        }
    }
}
