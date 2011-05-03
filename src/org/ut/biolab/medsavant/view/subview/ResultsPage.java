/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.subview;

import com.jidesoft.dashboard.Dashboard;
import com.jidesoft.dashboard.Gadget;
import com.jidesoft.dashboard.GadgetManager;
import com.jidesoft.dashboard.GadgetPalette;
import com.jidesoft.dashboard.SingleDashboardHolder;
import com.jidesoft.swing.JideSwingUtilities;
import fiume.table.SearchableTablePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.util.view.PeekingPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.gadget.GadgetFactory;
import org.ut.biolab.medsavant.view.gadget.chart.ChartPanel;
import org.ut.biolab.medsavant.view.gadget.filter.FilterPanel;

/**
 *
 * @author mfiume
 */
public class ResultsPage implements Page {

    private static class ChartContainer extends JPanel {

        private JPanel container;

        public ChartContainer() {
            this.setLayout(new BorderLayout());
            JPanel title = ViewUtil.getBannerPanel();
            title.add(Box.createHorizontalGlue());
            JButton addButton = new JButton("Add chart");
            addButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    JPanel p = new ChartPanel();
                    p.setPreferredSize(new Dimension(300,230));
                    p.setMaximumSize(new Dimension(300,230));
                    p.setBorder(ViewUtil.getTinyLineBorder());
                    addChart(p);
                }

            });
            title.add(addButton);
            this.add(title,BorderLayout.NORTH);
            container = new JPanel();
            //container.setBackground(ViewUtil.get);
            container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
            container.add(ViewUtil.getMediumSeparator());
            container.add(Box.createHorizontalGlue());
            this.add(new JScrollPane(container),BorderLayout.CENTER);
        }

        private void addChart(Component p) {
            container.add(p, container.getComponentCount()-1);
            container.add(ViewUtil.getMediumSeparator(), container.getComponentCount()-1);
        }
    }

    private JComponent panel;

    public String getName() {
        return "Results";
    }

    public JComponent getView() {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }

    private void setPanel() {

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        PeekingPanel detailView = new PeekingPanel("Filters", BorderLayout.EAST, new FilterPanel(), true);
        detailView.setTabColor(ViewUtil.getMidColor());//new Color(70,136,165));
        panel.add(detailView, BorderLayout.WEST);

        PeekingPanel genomeView = new PeekingPanel("Genome", BorderLayout.SOUTH, new JPanel(), false);
        genomeView.setTabColor(ViewUtil.getMidColor());//new Color(255,166,0));
        panel.add(genomeView, BorderLayout.NORTH);

        PeekingPanel chartView = new PeekingPanel("Charts", BorderLayout.NORTH, new ChartContainer(), true);
        chartView.setTabColor(ViewUtil.getMidColor());//new Color(233,74,64));
        panel.add(chartView, BorderLayout.SOUTH);

        Vector records = Util.getVariantRecordsVector(ResultController.getAllVariantRecords());
        JPanel p = new SearchableTablePanel(records, VariantRecordModel.getFieldNames(), VariantRecordModel.getFieldClasses());
        panel.add(p, BorderLayout.CENTER);

        //m.showGadget(g);
    }

    

    public Component getBanner() {
        JPanel p = ViewUtil.createClearPanel();
        p.add(Box.createHorizontalGlue());
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
        p.add(new JButton("Save Result Set"));
        p.add(new JButton("Show in Savant"));
        p.add(Box.createHorizontalStrut(10));
        //JTextField jtf = new JTextField("Search library");
        //jtf.setMaximumSize(new Dimension(200,999));
        //jtf.setColumns(30);
        //p.add(jtf);
        return p;
    }
}
