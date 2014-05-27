/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import com.jidesoft.chart.ChartType;
import com.jidesoft.chart.Legend;
import com.jidesoft.chart.PointShape;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.CategoryAxis;
import com.jidesoft.chart.model.ChartCategory;
import com.jidesoft.chart.model.ChartPoint;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.model.Highlight;
import com.jidesoft.chart.model.InvertibleTransform;
import com.jidesoft.chart.render.AbstractPieSegmentRenderer;
import com.jidesoft.chart.render.DefaultBarRenderer;
import com.jidesoft.chart.render.DefaultPieSegmentRenderer;
import com.jidesoft.chart.render.LinePieLabelRenderer;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.range.CategoryRange;
import com.jidesoft.range.NumericRange;
import java.text.NumberFormat;
import net.ericaro.surfaceplotter.JSurfacePanel;
import net.ericaro.surfaceplotter.Mapper;
import net.ericaro.surfaceplotter.ProgressiveSurfaceModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.ScatterChartEntry;
import org.ut.biolab.medsavant.shared.model.ScatterChartMap;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.genetics.QueryUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.client.query.QueryViewController;

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
    //Most of the time this is mapGenerator.getName(), except when the field
    //is from a non-standard annotation format (e..g Ensembl)
    private String chartName;

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

    void setChartName(String s) {
        this.chartName = s;
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
    }

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
        
        //Makes a box with fill color 255,255,255,0 and put a label with a black
        //font in that box.  The box is positioned directly over the corresponding pie slice.
        /*
        SimplePieLabelRenderer plr = new SimplePieLabelRenderer();
        plr.setLabelColor(Color.BLACK);
        plr.setBackground(new Color(0,0,0,0));
        rpie.setPieLabelRenderer(plr);
        */
        
        //....alternatively, the below draws a line from the pie wedge to the label.
        //see http://www.jidesoft.com/javadoc/com/jidesoft/chart/render/LinePieLabelRenderer.html
        LinePieLabelRenderer plr = new LinePieLabelRenderer();
        plr.setLabelColor(Color.black);
        plr.setLineColor(Color.black); //see also plr.setLineStroke        
        rpie.setPieLabelRenderer(plr);
        
        
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
            chartCategories = chartMaps[1].getCategories();
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
            System.out.println("Setting chart type to pie");
            chart.setChartType(ChartType.PIE);
            chart.getXAxis().getLabel().setColor(Color.BLUE);
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
            chart.addModel(models[i], style);
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
            Map<Object, List<String>> map;
            try {
                map = MedSavantClient.PatientManager.getDNAIDsForValues(
                        LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        generator.getFilterId());
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return null;
            }
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
                    LoginController.getSessionID(),
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

                boolean isGender = mapGenerator.getName().equalsIgnoreCase(BasicPatientColumns.GENDER.getAlias());
                try {
                    List<String> values = new ArrayList<String>();
                    ListSelectionModel selectionModel = chart.getSelectionsForModel(chart.getModel(0));
                    QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
                    for (int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); i++) {
                        if (selectionModel.isSelectedIndex(i)) {
                            String v = ((ChartPoint) chart.getModel().getPoint(i)).getHighlight().name();
                            values.add(v);
                            if (mapGenerator.isNumeric() && !isGender) {
                                double low = 0;
                                double high = 0;

                                String[] s = v.split(" - ");
                                if (s.length < 2) {
                                    LOG.error("Invalid range detected for numeric condition " + mapGenerator.getName() + " val=" + v);
                                }
                                NumberFormat format = NumberFormat.getInstance();
                                low = format.parse(s[0]).doubleValue();
                                high = format.parse(s[1]).doubleValue();

                                QueryUtils.addNumericQuery(chartName, low, high, false);
                            }
                        }
                    }
                    if (values.isEmpty()) {
                        return;
                    }

                    if (!mapGenerator.isNumeric() || isGender) {
                        QueryUtils.addMultiStringQuery(chartName, values);
                    }

                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error filtering by selection: %s", ex);
                }
            }
        });
        menu.add(filter1Item);

        return menu;
    }
}
