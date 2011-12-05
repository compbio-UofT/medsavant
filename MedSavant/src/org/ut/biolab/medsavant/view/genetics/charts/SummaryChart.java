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
import java.awt.BorderLayout;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.RangeCondition;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.Table;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class SummaryChart extends JPanel implements FiltersChangedListener {
    
    public static enum ChartAxis {X, Y};
    
    private boolean isLogScaleY = false;
    private boolean isLogScaleX = false;
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
        FilterController.addFilterListener(this);
    }
    
    public void setIsLogScale(boolean isLogScale, ChartAxis axis){
        if(!isLogScale){
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
        synchronized (updateLock){
            if(updateRequired){
                updateRequired = false;     
                updateDataAndDrawChart();
            }
        }
    }

    private void updateDataAndDrawChart() {
        
        //begin creating chart
        new ChartMapWorker().execute();
        
        //show wait panel
        final JPanel instance = this;
        new Thread() {
            @Override
            public void run(){
                instance.removeAll();
                instance.add(new WaitPanel("Getting chart data"), BorderLayout.CENTER);
                instance.updateUI();
            }
        }.start();
    }

    private synchronized void drawChart(ChartFrequencyMap chartMap) {
        DefaultChartModel chartModel = new DefaultChartModel();

        final Chart chart = new Chart(new Dimension(200, 200));
        chart.addModel(chartModel);
        
        chart.setRolloverEnabled(true);
        chart.setSelectionEnabled(true);
        chart.setSelectionShowsOutline(true);
        chart.setSelectionShowsExplodedSegments(true);
        chart.setAntiAliasing(true);
        chart.setBarGap(5);
        chart.setBorder(ViewUtil.getBigBorder());
        chart.setLabellingTraces(true);
        chart.getSelectionsForModel(chartModel).setSelectionMode(
                mapGenerator.isNumeric() ? 
                ListSelectionModel.SINGLE_SELECTION : 
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        chart.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)){
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
            if (this.isLogScaleY()) {
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
        if (this.isLogScaleY()) {
            chart.setYAxis(new Axis(new NumericRange(0, Math.log10(max) * 1.1), "log(Frequency)"));
        } else {
            chart.setYAxis(new Axis(new NumericRange(0, max * 1.1), "Frequency"));
        }
        chart.getXAxis().getLabel().setFont(ViewUtil.getMediumTitleFont());
        chart.getYAxis().getLabel().setFont(ViewUtil.getMediumTitleFont());

        // rotate 90 degrees (using radians)
        chart.getXAxis().setTickLabelRotation(1.57079633);

        
        chart.setStyle(chartModel, s);

        if (isPie) {
            chart.setChartType(ChartType.PIE);
        }

        this.add(chart, BorderLayout.CENTER);
    }

    void setIsSortedKaryotypically(boolean b) {
        this.isSortedKaryotypically = b;
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        synchronized (updateLock){
            updateRequired = true;
        }
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
            return mapGenerator.generateChartMap(isLogScaleX && mapGenerator.isNumeric());
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
    
    private JPopupMenu createPopup(final Chart chart){

        JPopupMenu menu = new JPopupMenu();
          
        //Filter by selections
        JMenuItem filter1Item = new JMenuItem("Filter by Selection" + (mapGenerator.isNumeric() ? "" : "(s)"));
        filter1Item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                
                ThreadController.getInstance().cancelWorkers(pageName);
                
                List<String> values = new ArrayList<String>();
                ListSelectionModel selectionModel = chart.getSelectionsForModel(chart.getModel());
                for(int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); i++){
                    if (selectionModel.isSelectedIndex(i)){
                        values.add(((ChartPoint)chart.getModel().getPoint(i)).getHighlight().name());
                    }
                }                
                if(values.isEmpty()) return;

                TableSchema table = FilterUtils.getTableSchema(mapGenerator.getTable());
                if(mapGenerator.isNumeric()){
                    Range r = Range.rangeFromString(values.get(0));
                    RangeCondition condition = new RangeCondition(table.getDBColumn(mapGenerator.getFilterId()), r.getMin(), r.getMax());
                    FilterUtils.createAndApplyGenericFixedFilter(
                            "Charts - Filter by Selection", 
                            mapGenerator.getName() + ": " + r.getMin() + " - " + r.getMax(), 
                            ComboCondition.and(condition));

                    //Range r = Range.rangeFromString(values.get(0)); //there should only be one item here
                    //FilterUtils.createAndApplyNumericFilterView(mapGenerator.getFilterId(), mapGenerator.getName(), mapGenerator.getTable(), r.getMin(), r.getMax());
                } else {
                    Condition[] conditions = new Condition[values.size()];
                    for(int i = 0; i < conditions.length; i++){
                        conditions[i] = BinaryConditionMS.equalTo(table.getDBColumn(mapGenerator.getFilterId()), values.get(i));
                    }
                    FilterUtils.createAndApplyGenericFixedFilter("Charts - Filter by Selection", mapGenerator.getName() + ": " + values.size() + " selection(s)", ComboCondition.or(conditions));
                    //FilterUtils.createAndApplyStringListFilterView(mapGenerator.getFilterId(), mapGenerator.getName(), mapGenerator.getTable(), values);
                }

                updateDataAndDrawChart();
            }
        });
        menu.add(filter1Item);
        
        return menu;
    }
}
