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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Comparator;
import java.util.Map;
import javax.swing.JPanel;

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

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class SummaryChart extends JPanel {
    private boolean isLogscale = false;
    private boolean isPie = false;
    private boolean isSorted = false;
    private static final int DEFAULT_NUM_QUANTITATIVE_CATEGORIES = 15;
    private ChartMapWorker mapWorker;
    private ChartMapGenerator mapGenerator;
    private boolean isSortedKaryotypically;
    private String pageName;

    private final Object updateLock = new Object();
    private boolean updateRequired = false;
    
    public SummaryChart(final String pageName) {
        this.pageName = pageName;
        setLayout(new BorderLayout());
        FilterController.addFilterListener(new FiltersChangedListener() {
            public void filtersChanged() {
                synchronized (updateLock){
                    updateRequired = true;
                }
            }
        });
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
    
    public void updateIfRequired() {
        synchronized (updateLock){
            if(updateRequired){
                updateRequired = false;     
                updateDataAndDrawChart();
            }
        }
    }

    private void updateDataAndDrawChart() {
        
        System.out.println("update");

        this.removeAll();
        this.add(new WaitPanel("Getting chart data"), BorderLayout.CENTER);
        this.updateUI();

        new ChartMapWorker().execute();
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

        this.add(chart, BorderLayout.CENTER);
    }

    void setIsSortedKaryotypically(boolean b) {
        this.isSortedKaryotypically = b;
    }

    public class ChartMapWorker extends MedSavantWorker<ChartFrequencyMap> {

        @SuppressWarnings("LeakingThisInConstructor")
        ChartMapWorker() {
            super(pageName);
            if (mapWorker != null) {
                mapWorker.cancel(true);
            }
            mapWorker = this;
        }

        @Override
        protected ChartFrequencyMap doInBackground() throws Exception {
            if (mapGenerator == null) { return null; }
            if(this.isThreadCancelled()) return null;
            return mapGenerator.generateChartMap();
        }

        public void showSuccess(ChartFrequencyMap result) {
            if (result != null) {
                drawChart(result);
            }
        }

        public void showProgress(double prog) {
            if (prog == 1.0) {
                mapWorker = null;
                removeAll();        // Clear away the WaitPanel.
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
}
