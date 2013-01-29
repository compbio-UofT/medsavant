/*
 *    Copyright 2012 University of Toronto
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
package org.ut.biolab.medsavant.client.view.genetics.charts;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import javax.swing.*;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.ChartColor;
import com.jidesoft.chart.ChartType;
import com.jidesoft.chart.Legend;
import com.jidesoft.chart.PointShape;
import com.jidesoft.chart.annotation.AutoPositionedLabel;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.CategoryAxis;
import com.jidesoft.chart.model.ChartCategory;
import com.jidesoft.chart.model.ChartPoint;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.model.Highlight;
import com.jidesoft.chart.model.InvertibleTransform;
import com.jidesoft.chart.render.AbstractPieSegmentRenderer;
import com.jidesoft.chart.render.BarRenderer;
import com.jidesoft.chart.render.DefaultBarRenderer;
import com.jidesoft.chart.render.DefaultPieSegmentRenderer;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.range.CategoryRange;
import com.jidesoft.range.NumericRange;
import javax.swing.border.EmptyBorder;
import net.ericaro.surfaceplotter.JSurfacePanel;
import net.ericaro.surfaceplotter.Mapper;
import net.ericaro.surfaceplotter.ProgressiveSurfaceModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.ScatterChartEntry;
import org.ut.biolab.medsavant.shared.model.ScatterChartMap;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class SummaryChart extends JLayeredPane implements BasicPatientColumns, BasicVariantColumns {

    private static final Log LOG = LogFactory.getLog(SummaryChart.class);

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
    private MedSavantWorker mapWorker;
    private ChartMapGenerator mapGenerator;
    private ChartMapGenerator mapGeneratorScatter;
    private boolean isSortedKaryotypically;
    private String pageName;
    private final Object updateLock = new Object();
    private boolean updateRequired = false;
    private GridBagConstraints c;
    private WaitPanel waitPanel = new WaitPanel("Getting chart data");
    private boolean isScatter = false;

    //private Stack<ZoomFrame> zoomStack = new Stack<ZoomFrame>();
    //private String scatterAliasX;
    //private String scatterAliasY;
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
        //updateDataAndDrawChart();
    }

    /*public void setScatterChart(String aliasX, String aliasY) {
     this.scatterAliasX = aliasX;
     this.scatterAliasY = aliasY;
     }*/
    public void setScatterChartMapGenerator(ChartMapGenerator cmg) {
        mapGeneratorScatter = cmg;
    }

    public void setIsScatterChart(boolean scatter) {
        isScatter = scatter;
    }

    public boolean isScatterChart() {
        return isScatter;
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

        removeAll();
        add(waitPanel, c, JLayeredPane.MODAL_LAYER);
        waitPanel.setVisible(true);
        setLayer(waitPanel, JLayeredPane.MODAL_LAYER);

        //begin creating chart
        mapWorker = isScatter ? new ScatterChartMapWorker() : new ChartMapWorker();
        mapWorker.execute();
    }

    private ChartStyle barStyle(Paint fill) {
        ChartStyle style = new ChartStyle();
        style.setBarsVisible(true);
        style.setBarPaint(fill);
        return style;
    }

    private synchronized Chart drawChart(ChartFrequencyMap[] chartMaps) {

        ChartFrequencyMap filteredChartMap = chartMaps[0];
        ChartFrequencyMap unfilteredChartMap = null;

        DefaultChartModel filteredChartModel = new DefaultChartModel();
        DefaultChartModel unfilteredChartModel = null;

        if (this.showComparedToOriginal) {
            unfilteredChartMap = ChartFrequencyMap.subtract(chartMaps[1], filteredChartMap, isLogScaleY());
            unfilteredChartModel = new DefaultChartModel();
        }

        final Chart chart = new Chart();

        JPanel panel = new JPanel();
        Legend legend = new Legend(chart, 0);
        panel.add(legend);
        legend.addChart(chart);

        boolean multiColor = !mapGenerator.isNumeric() || isPie;

        chart.setRolloverEnabled(true);
        chart.setSelectionEnabled(true);
        chart.setSelectionShowsOutline(true);
        chart.setSelectionShowsExplodedSegments(true);
        chart.setAntiAliasing(true);
        chart.setBarGap(5);
        chart.setBorder(ViewUtil.getBigBorder());
        chart.setLabellingTraces(true);

        chart.setAnimateOnShow(false);

        chart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popup = createPopup(chart);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        AbstractPieSegmentRenderer rpie = new DefaultPieSegmentRenderer();
        chart.setPieSegmentRenderer(rpie);

        DefaultBarRenderer rbar = new DefaultBarRenderer();
        chart.setBarRenderer(rbar);

        rpie.setSelectionColor(Color.gray);
        rbar.setSelectionColor(Color.gray);

        if (isSortedKaryotypically()) {
            filteredChartMap.sortKaryotypically();
            if (this.showComparedToOriginal) {
                unfilteredChartMap.sortKaryotypically();
            }
        }

        if (isSorted() && !mapGenerator.isNumeric() && !isSortedKaryotypically()) {
            filteredChartMap.sortNumerically();
            if (this.showComparedToOriginal) {
                chartMaps[1].sortNumerically();
            }
        } else {
            filteredChartMap.undoSortNumerically();
            if (this.showComparedToOriginal) {
                chartMaps[1].undoSortNumerically();
            }
        }

        long max = filteredChartMap.getMax();
        List<ChartCategory> chartCategories;
        if (this.showComparedToOriginal) {
            chartCategories = chartMaps[1].getCategories();//unfilteredChartMap.getCategories();
            max = chartMaps[1].getMax();
        } else {
            chartCategories = filteredChartMap.getCategories();
        }

        CategoryRange<String> range = new CategoryRange<String>();
        List<Highlight> highlights = new ArrayList<Highlight>();

        Color color = new Color(72, 181, 249);
        Highlight h;
        int catNum = 0;
        int totalCats = filteredChartMap.getEntries().size();

        for (ChartCategory category : chartCategories) {
            range.add(category);
            if (multiColor) {
                color = ViewUtil.getColor(catNum++, totalCats);
            }
            h = new Highlight(category.getName());
            highlights.add(h);
            chart.setHighlightStyle(h, barStyle(color));
        }

        final CategoryAxis xaxis = new CategoryAxis(range, "Category");
        chart.setXAxis(xaxis);
        if (this.isLogScaleY()) {
            chart.setYAxis(new Axis(new NumericRange(0, Math.log10(max) * 1.1), "log(Frequency)"));
        } else {
            chart.setYAxis(new Axis(new NumericRange(0, max * 1.1), "Frequency"));
        }

        addEntriesToChart(filteredChartModel, filteredChartMap, chartCategories, highlights);
        if (this.showComparedToOriginal) {
            addEntriesToChart(unfilteredChartModel, unfilteredChartMap, chartCategories, null);
        }

        chart.getXAxis().getLabel().setFont(ViewUtil.getMediumTitleFont());
        chart.getYAxis().getLabel().setFont(ViewUtil.getMediumTitleFont());

        // rotate 90 degrees (using radians)
        chart.getXAxis().setTickLabelRotation(1.57079633);

        if (isPie) {
            chart.setChartType(ChartType.PIE);
        }

        // This adds zooming cababilities to bar charts, not great though
        /*else {
         RubberBandZoomer rubberBand = new RubberBandZoomer(chart);
         chart.addDrawable(rubberBand);
         chart.addMouseListener(rubberBand);
         chart.addMouseMotionListener(rubberBand);

         rubberBand.addZoomListener(new ZoomListener() {
         public void zoomChanged(ChartSelectionEvent event) {
         if (event instanceof RectangleSelectionEvent) {
         Range<?> currentXRange = chart.getXAxis().getOutputRange();
         Range<?> currentYRange = chart.getYAxis().getOutputRange();
         ZoomFrame frame = new ZoomFrame(currentXRange, currentYRange);
         zoomStack.push(frame);
         Rectangle selection = (Rectangle) event.getLocation();
         Point topLeft = selection.getLocation();
         topLeft.x = (int) Math.floor(frame.getXRange().minimum());
         Point bottomRight = new Point(topLeft.x + selection.width, topLeft.y + selection.height);
         bottomRight.x = (int) Math.ceil(frame.getXRange().maximum());
         assert bottomRight.x >= topLeft.x;
         Point2D rp1 = chart.calculateUserPoint(topLeft);
         Point2D rp2 = chart.calculateUserPoint(bottomRight);
         if (rp1 != null && rp2 != null) {
         assert rp2.getX() >= rp1.getX();
         Range<?> xRange = new NumericRange(rp1.getX(), rp2.getX());
         assert rp1.getY() >= rp2.getY();
         Range<?> yRange = new NumericRange(rp2.getY(), rp1.getY());
         //chart.getXAxis().setRange(xRange);
         chart.getYAxis().setRange(yRange);
         }
         } else if (event instanceof PointSelectionEvent) {
         if (zoomStack.size() > 0) {
         ZoomFrame frame = zoomStack.pop();
         Range<?> xRange = frame.getXRange();
         Range<?> yRange = frame.getYRange();
         //chart.getXAxis().setRange(xRange);
         chart.getYAxis().setRange(yRange);
         }
         }
         }
         });
         }
         *
         */

        for (int i = 1; i < this.getComponentCount(); i++) {
            this.remove(i);
        }

        chart.addModel(filteredChartModel, new ChartStyle().withBars());
        if (this.showComparedToOriginal) {
            chart.addModel(unfilteredChartModel, new ChartStyle(new Color(10, 10, 10, 100)).withBars());
        }

        return chart;
    }

    private void addEntriesToChart(
            DefaultChartModel chartModel,
            ChartFrequencyMap chartMap,
            List<ChartCategory> chartCategories,
            List<Highlight> highlights) {

        boolean addHighlights = highlights != null;

        int index = 0;
        for (ChartCategory cat : chartCategories) {

            FrequencyEntry fe = chartMap.getEntry(cat.getName());
            long value = 0;
            if (fe != null) {
                value = fe.getFrequency();
            }

            ChartPoint p = new ChartPoint(cat, value);
            ChartPoint logp = new ChartPoint(cat, Math.log10(value));

            if (addHighlights) {
                p.setHighlight(highlights.get(index));
                logp.setHighlight(highlights.get(index));
            }

            if (this.isLogScaleY()) {
                chartModel.addPoint(logp);
            } else {
                chartModel.addPoint(p);
            }

            index++;
        }
    }

    private synchronized void drawScatterChart(ScatterChartMap entries) {

        Chart chart = new Chart(new Dimension(200, 200));
        Legend legend = new Legend(chart);

        //Create x axis
        int max = 0;
        CategoryRange<String> range = new CategoryRange<String>();
        for (int i = 0; i < entries.getNumX(); i++) {
            range.add(new ChartCategory(entries.getXValueAt(i)));
        }

        //create models
        DefaultChartModel[] models = new DefaultChartModel[entries.getNumY()];
        for (int i = 0; i < entries.getNumY(); i++) {
            models[i] = new DefaultChartModel();
        }

        for (int i = 0; i < entries.getNumX(); i++) {
            //ScatterChartEntry[] x = entries[i];
            for (int j = 0; j < entries.getNumY(); j++) {
                ScatterChartEntry entry = entries.getValueAt(i, j);
                if (entry != null) {
                    max = Math.max(max, entry.getFrequency());
                    models[j].addPoint(new ChartPoint(range.getCategoryValues().get(i), entry.getFrequency()));
                    models[j].setName(entry.getYRange());
                } else {
                    models[j].addPoint(new ChartPoint(range.getCategoryValues().get(i), 0));
                }
            }
        }

        //add models
        for (int i = 0; i < models.length; i++) {
            Color translucentGreen = ViewUtil.getColor(i, models.length);
            ChartStyle style = new ChartStyle(translucentGreen, PointShape.DISC);
            style.setPointSize(10);
            chart.addModel(models[i], style);//new ChartStyle(ViewUtil.getColor(i, models.length), true, mapGenerator.isNumeric()));
        }

        //add axes
        CategoryAxis xaxis = new CategoryAxis(range, mapGenerator.getName());
        chart.setXAxis(xaxis);
        chart.setYAxis(new Axis(new NumericRange(0, max * 1.1), "Frequency"));

        //add chart
        add(chart, c, JLayeredPane.DEFAULT_LAYER);

        //add legend in scrollpane
        JScrollPane scroll = new JScrollPane(legend);
        scroll.setPreferredSize(new Dimension(150, 100));
        c.gridx = 1;
        c.weightx = 0;
        add(scroll, c, JLayeredPane.DEFAULT_LAYER);
        c.weightx = 1;
        c.gridx = 0;

    }

    private synchronized void drawSurface(final ScatterChartMap entries) {

        String[] firstX = entries.getXValueAt(0).split("[^(\\d|\\.|E)]");
        String[] firstY = entries.getYValueAt(0).split("[^(\\d|\\.|E)]");
        String[] lastX = entries.getXValueAt(entries.getNumX() - 1).split("[^(\\d|\\.|E)]");
        String[] lastY = entries.getYValueAt(entries.getNumY() - 1).split("[^(\\d|\\.|E)]");

        float startX = Float.parseFloat(firstX[0]);
        float endX = Float.parseFloat(lastX[lastX.length - 1]);
        final float binSizeX = Float.parseFloat(firstX[firstX.length - 1]) - startX;

        float startY = Float.parseFloat(firstY[0]);
        float endY = Float.parseFloat(lastY[lastY.length - 1]);
        final float binSizeY = Float.parseFloat(firstY[firstY.length - 1]) - startY;

        JSurfacePanel panel = new JSurfacePanel();
        panel.setTitleText("");

        ProgressiveSurfaceModel model = new ProgressiveSurfaceModel();
        panel.setModel(model);
        model.setXMin(startX);
        model.setXMax(endX);
        model.setYMin(startY);
        model.setYMax(endY);
        model.setZMin(0);
        model.setZMax(entries.getMaxFrequency() + 1);
        model.setDisplayXY(true);
        model.setDisplayZ(true);

        model.setMapper(new Mapper() {
            @Override
            public float f1(float x, float y) {

                try {
                    int binX = (int) (x / binSizeX);
                    int binY = (int) (y / binSizeY);
                    ScatterChartEntry entry = entries.getValueAt(binX, binY);
                    if (entry == null) {
                        return 0;
                    } else {
                        return entry.getFrequency();
                    }
                } catch (Exception ex) {
                    LOG.error("Exception thrown by mapper.", ex);
                }
                return x;
            }

            @Override
            public float f2(float x, float y) {
                return 0;
            }
        });
        model.plot().execute();

        add(panel, c, JLayeredPane.DEFAULT_LAYER);
    }

    void setIsSortedKaryotypically(boolean b) {
        this.isSortedKaryotypically = b;
    }

    public class ChartMapWorker extends MedSavantWorker<ChartFrequencyMap[]> {

        ChartMapWorker() {
            super(pageName);
            if (mapWorker != null) {
                mapWorker.cancel(true);
            }
        }

        @Override
        protected ChartFrequencyMap[] doInBackground() throws Exception {
            if (mapGenerator == null) {
                return null;
            }
            ChartFrequencyMap[] result;

            if (showComparedToOriginal) {
                result = new ChartFrequencyMap[2];
                result[1] = mapGenerator.generateChartMap(false, isLogScaleX && mapGenerator.isNumeric());
            } else {
                result = new ChartFrequencyMap[1];
            }

            result[0] = mapGenerator.generateChartMap(true, isLogScaleX && mapGenerator.isNumeric());
            return result;
        }

        @Override
        public void showSuccess(ChartFrequencyMap[] result) {
            if (result != null && result[0] != null && result[0].getEntries().size() < 200 && !result[0].getEntries().isEmpty()) {
                Chart chart = drawChart(result);
                add(chart, c, JLayeredPane.DEFAULT_LAYER);
            } else if (result != null && result[0] != null && result[0].getEntries().isEmpty()) {
                add(ViewUtil.getMessagePanelBig("No variants pass query"), c);
            } else if (result != null && result[0] != null && result[0].getEntries().size() >= 200) {
                add(ViewUtil.getMessagePanelBig("Too many values to display chart"), c);
            } else {
                add(ViewUtil.getMessagePanelBig("Error creating chart"), c);
            }

            waitPanel.setVisible(false);
            revalidate();
        }

        @Override
        public void showProgress(double prog) {
            if (prog == 1.0) {
                mapWorker = null;
                //removeAll();        // Clear away the WaitPanel.
            }
        }
    }

    public class ScatterChartMapWorker extends MedSavantWorker<ScatterChartMap> {

        ScatterChartMapWorker() {
            super(pageName);
            if (mapWorker != null) {
                mapWorker.cancel(true);
            }
        }

        private ScatterChartMap mapPatientField(ScatterChartMap scatterMap, ChartMapGenerator generator, boolean isX) throws SQLException, RemoteException {
            Map<Object, List<String>> map = MedSavantClient.PatientManager.getDNAIDsForValues(
                    LoginController.getInstance().getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    generator.getFilterId());
            if (generator.getFilterId().equals(GENDER.getColumnName())) {
                map = ClientMiscUtils.modifyGenderMap(map);
            }

            List<String> rangesA = new ArrayList<String>();
            for (Object o : map.keySet()) {
                rangesA.add(o.toString());
            }
            List<String> rangesB = (isX ? scatterMap.getYRanges() : scatterMap.getXRanges());

            List<ScatterChartEntry> entries = new ArrayList<ScatterChartEntry>();
            for (Object a : map.keySet()) {
                List<Integer> indices = new ArrayList<Integer>();
                for (String dnaId : map.get(a)) {
                    int index = (isX ? scatterMap.getIndexOnX(dnaId) : scatterMap.getIndexOnY(dnaId));
                    if (index != -1) {
                        indices.add(index);
                    }
                }
                for (int b = 0; b < rangesB.size(); b++) {
                    int sum = 0;
                    for (Integer index : indices) {
                        if ((isX ? scatterMap.getValueAt(index, b) : scatterMap.getValueAt(b, index)) != null) {
                            sum += (isX ? scatterMap.getValueAt(index, b) : scatterMap.getValueAt(b, index)).getFrequency();
                        }
                    }
                    entries.add((isX ? new ScatterChartEntry(a.toString(), rangesB.get(b), sum) : new ScatterChartEntry(rangesB.get(b), a.toString(), sum)));
                }
            }
            return (isX ? new ScatterChartMap(rangesA, rangesB, entries) : new ScatterChartMap(rangesB, rangesA, entries));
        }

        @Override
        protected ScatterChartMap doInBackground() throws Exception {
            if (mapGenerator == null) {
                return null;
            }

            //get column names
            String columnX = mapGenerator.getFilterId();
            String columnY = mapGeneratorScatter.getFilterId();
            if (mapGenerator.getTable() == WhichTable.PATIENT) {
                columnX = DNA_ID.getColumnName();
            }
            if (mapGeneratorScatter.getTable() == WhichTable.PATIENT) {
                columnY = DNA_ID.getColumnName();
            }

            ScatterChartMap scatterMap = MedSavantClient.VariantManager.getFilteredFrequencyValuesForScatter(
                    LoginController.getInstance().getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    ReferenceController.getInstance().getCurrentReferenceID(),
                    FilterController.getInstance().getAllFilterConditions(),
                    columnX,
                    columnY,
                    !mapGenerator.isNumeric() || mapGenerator.getTable() == WhichTable.PATIENT,
                    !mapGeneratorScatter.isNumeric() || mapGeneratorScatter.getTable() == WhichTable.PATIENT,
                    isSortedKaryotypically());

            //TODO: re-mapping below works only for categorical patient fields. Generalize for numeric/categorical.

            //map for patient field
            if (mapGenerator.getTable() == WhichTable.PATIENT) {
                scatterMap = mapPatientField(scatterMap, mapGenerator, true);
            }

            if (mapGeneratorScatter.getTable() == WhichTable.PATIENT) {
                scatterMap = mapPatientField(scatterMap, mapGeneratorScatter, false);
            }

            return scatterMap;
        }

        @Override
        public void showSuccess(ScatterChartMap result) {
            if (mapGenerator.isNumeric()
                    && mapGeneratorScatter.isNumeric()
                    && !mapGenerator.getFilterId().equals(GENDER.getColumnName())
                    && !mapGeneratorScatter.getFilterId().equals(GENDER.getColumnName())) {
                drawSurface(result);
            } else {
                drawScatterChart(result);
            }
            waitPanel.setVisible(false);
            revalidate();
        }

        @Override
        public void showProgress(double prog) {
            if (prog == 1.0) {
                mapWorker = null;
            }
        }
    }

    static class ValueComparator implements Comparator {

        Map base;

        public ValueComparator(Map base) {
            this.base = base;
        }

        @Override
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

        @Override
        public Double transform(Double pos) {
            return Math.log10(pos);
        }

        @Override
        public Double inverseTransform(Double t) {
            return Math.pow(10, t);
        }
    }

    private JPopupMenu createPopup(final Chart chart) {

        JPopupMenu menu = new JPopupMenu();

        //Filter by selections
        JMenuItem filter1Item = new JMenuItem("Filter by Selection" + (mapGenerator.isNumeric() ? "" : "(s)"));
        filter1Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ThreadController.getInstance().cancelWorkers(pageName);

                try {
                    List<String> values = new ArrayList<String>();
                    ListSelectionModel selectionModel = chart.getSelectionsForModel(chart.getModel(0));

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
                    /*                    if (mapGenerator.isNumeric() && !mapGenerator.getFilterId().equals(DefaultPatientTableSchema.COLUMNNAME_OF_GENDER)) {

                     Range r = Range.rangeFromString(values.get(0));

                     if (mapGenerator.getTable() == WhichTable.VARIANT) {
                     RangeCondition condition = new RangeCondition(variantTable.getDBColumn(mapGenerator.getFilterId()), r.getMin(), r.getMax());
                     FilterUtils.createAndApplyGenericFixedFilter(
                     "Charts - Filter by Selection",
                     mapGenerator.getName() + ": " + r.getMin() + " - " + r.getMax(),
                     ComboCondition.and(condition));
                     } else {
                     List<String> individuals = MedSavantClient.PatientManager.getDNAIDsWithValuesInRange(
                     LoginController.getInstance().getSessionID(),
                     ProjectController.getInstance().getCurrentProjectID(),
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
                     }

                     } else {

                     if (mapGenerator.getTable() == WhichTable.VARIANT) {
                     Condition[] conditions = new Condition[values.size()];
                     for (int i = 0; i < conditions.length; i++) {
                     conditions[i] = BinaryConditionMS.equalTo(variantTable.getDBColumn(mapGenerator.getFilterId()), values.get(i));
                     }
                     FilterUtils.createAndApplyGenericFixedFilter(
                     "Charts - Filter by Selection",
                     mapGenerator.getName() + ": " + values.size() + " selection(s)",
                     ComboCondition.or(conditions));
                     } else {
                     //special case for gender
                     if (mapGenerator.getFilterId().equals(DefaultPatientTableSchema.COLUMNNAME_OF_GENDER)) {
                     List<String> values1 = new ArrayList<String>();
                     for (String s : values) {
                     values1.add(Integer.toString(ClientMiscUtils.stringToGender(s)));
                     }
                     values = values1;
                     }

                     List<String> individuals = MedSavantClient.PatientManager.getDNAIDsForStringList(
                     LoginController.getInstance().getSessionID(),
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
                     }
                     }*/
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error filtering by selection: %s", ex);
                }
            }
        });
        menu.add(filter1Item);

        return menu;
    }
}
