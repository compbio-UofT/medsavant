/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.gadget.chart;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.dashboard.AbstractGadget;
import com.jidesoft.dashboard.GadgetComponent;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.ut.biolab.medsavant.model.VariantRecordModel;
import org.ut.biolab.medsavant.view.gadget.DockableFrameGadget;

/**
 *
 * @author mfiume
 */
public class ChartGadget extends AbstractGadget {

    private int currentKeyIndex = VariantRecordModel.INDEX_OF_REF;
    private Map<String,Integer> chartMap;
    private DefaultChartModel chartModel;
    private Chart chart;
    private JPanel panel;
    private JToolBar bar;
    private boolean isPie = false;
    private JCheckBox isPieCB;

    public ChartGadget() {
        super("Chart");
    }

    public GadgetComponent createGadgetComponent() {
        final DockableFrameGadget gadget = new DockableFrameGadget(this);
        gadget.getContentPane().add(new ChartPanel());
        return gadget;
    }

    /*
    private JPanel getContentPanel() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        initDropDown();
        updateChartMap();
        return panel;
    }
     *
     */

    public void disposeGadgetComponent(GadgetComponent gc) {
        return;
    }

    /*
    private void updateChartMap() {
        chart = null;
        chartModel = null;
        chartMap = new HashMap<String,Integer>();

        for (VariantRecord r : ResultController.getVariantRecords()) {
            String key = getKey(r);
            if (chartMap.containsKey(key)) {
                chartMap.put(key, chartMap.get(key)+1);
            } else {
                chartMap.put(key, 1);
            }
        }

        printHist(chartMap);

        chartModel = new DefaultChartModel("Sample Model");
        chart = new Chart(new Dimension(200, 200));

        ChartStyle s = new ChartStyle();
        s.setBarsVisible(true);
        s.setLinesVisible(false);
        
        if (isPie)
            chart.setChartType(ChartType.PIE);

        chart.addModel(chartModel);
        chart.setStyle(chartModel, s);
        chart.setRolloverEnabled(true);

        chart.setSelectionEnabled(true);
        chart.setSelectionShowsOutline(false);
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
            ChartPoint p = new ChartPoint(cat,value);
            p.setHighlight(h);
            chartModel.addPoint(p);
        }

        chart.setXAxis(new CategoryAxis(categories, "Category"));
        chart.setYAxis(new Axis(new NumericRange(0, max), "Frequency"));

        panel.removeAll();
        panel.add(bar, BorderLayout.NORTH);
        panel.add(chart, BorderLayout.CENTER);
    }

    private String getKey(VariantRecord r) {
        switch(currentKeyIndex) {
            case VariantRecordModel.INDEX_OF_ALT:
                return r.getAlt();
            case VariantRecordModel.INDEX_OF_CALLDETAILS:
                return r.getCallDetails();
            case VariantRecordModel.INDEX_OF_CHROM:
                return r.getChrom();
            case VariantRecordModel.INDEX_OF_FILTER:
                return r.getFilter();
            case VariantRecordModel.INDEX_OF_FORMAT:
                return r.getFormat();
            case VariantRecordModel.INDEX_OF_ID:
                return r.getId();
            //case VariantRecordModel.INDEX_OF_POS:
            //    return (String) r.getPos();
            //case VariantRecordModel.INDEX_OF_QUAL:
            //    return r.getQual();
            case VariantRecordModel.INDEX_OF_REF:
                return r.getRef();
            case VariantRecordModel.INDEX_OF_SAMPLEID:
                return r.getSampleID();
        }

        return null;
    }

    private void printHist(Map<String, Integer> chartMap) {
        if (true)
            return;
        System.out.println("Printing hist: ");
        for (String s : chartMap.keySet()) {
            System.out.println(s + ": " + chartMap.get(s));
        }
    }

    private void initDropDown() {
        bar = new JToolBar();
        JComboBox b = new JComboBox();
        
        Vector v = VariantRecordModel.getFieldNames();
        for (int i = 0; i < VariantRecordModel.getNumberOfFields(); i++) {
            b.addItem(v.get(i));
        }

        b.setSelectedIndex(currentKeyIndex);

        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String fieldName = (String)cb.getSelectedItem();
                setCurrentKeyIndex(VariantRecordModel.getIndexOfField(fieldName));
                updateChartMap();
            }

        });

        bar.setFloatable(false);
        bar.add(b);

        bar.add(Box.createHorizontalStrut(5));

        final ChartGadget instance = this;

        isPieCB = new JCheckBox("Display as Pie");
        isPieCB.setSelected(isPie);
        isPieCB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                instance.setIsPie(isPieCB.isSelected());
                instance.updateChartMap();
            }

        });
        bar.add(isPieCB);

        panel.add(bar,BorderLayout.NORTH);
    }

    public void setIsPie(boolean b) {
        this.isPie = b;
    }

    public void setCurrentKeyIndex(int currentKeyIndex) {
        this.currentKeyIndex = currentKeyIndex;
    }
     * 
     */


}
