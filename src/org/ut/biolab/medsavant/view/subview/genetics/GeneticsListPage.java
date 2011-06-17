/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.subview.genetics;

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
import java.util.ArrayList;
import java.util.List;
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
import org.ut.biolab.medsavant.model.record.Chromosome;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.util.view.PeekingPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.gadget.GadgetFactory;
import org.ut.biolab.medsavant.view.filter.FilterPanel;
import org.ut.biolab.medsavant.view.subview.Page;

/**
 *
 * @author mfiume
 */
public class GeneticsListPage implements Page {

    private JComponent panel;

    public String getName() {
        return "List";
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
        panel.add(new TablePanel(), BorderLayout.CENTER);
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
