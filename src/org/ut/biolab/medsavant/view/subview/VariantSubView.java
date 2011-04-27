/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import com.jidesoft.dashboard.Gadget;
import com.jidesoft.dashboard.GadgetManager;
import com.jidesoft.dashboard.SingleDashboardHolder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.util.ViewUtil;
import org.ut.biolab.medsavant.view.gadget.GadgetFactory;

/**
 *
 * @author mfiume
 */
public class VariantSubView implements SubView {

    private JComponent panel;

    public String getName() {
        return "Variant";
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

        GadgetManager m = new GadgetManager();
        m.setAllowMultipleGadgetInstances(true);

        SingleDashboardHolder sdh = new SingleDashboardHolder(m);
        sdh.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sdh.setColumnCount(2);

        sdh.setBackground(Color.darkGray);
        sdh.setColumnResizable(true);
        sdh.setRowResizable(true);

        panel.add(sdh, BorderLayout.CENTER);

        sdh.showPalette();

        Gadget g;
        g = GadgetFactory.createFilterGadget();
        m.addGadget(g);
        m.showGadget(g);
        g = GadgetFactory.createResultsGadget();
        m.addGadget(g);
        m.showGadget(g);
        g = GadgetFactory.createChartGadget();
        m.addGadget(g);
        //m.showGadget(g);
    }

    public Component getBanner() {
        JPanel p = ViewUtil.createClearPanel();
        p.add(new JButton("Show in Savant"));
        return p;
    }

}
