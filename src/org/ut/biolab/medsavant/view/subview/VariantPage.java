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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.gadget.GadgetFactory;

/**
 *
 * @author mfiume
 */
public class VariantPage implements Page {

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

        SingleDashboardHolder sdh = new SingleDashboardHolder(m) {

            @Override
            protected GadgetPalette createGadgetPalette() {
                GadgetPalette palette = new GadgetPalette(getGadgetManager(), this) {

                    @Override
                    protected String getResourceString(String key) {
                        return super.getResourceString(key);
                    }

                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        GradientPaint p = new GradientPaint(0,0,Color.white,0,40,Color.lightGray);
                        ((Graphics2D)g).setPaint(p);
                        g.fillRect(0, 0, this.getWidth(), this.getHeight());
                    }


                    @Override
                    protected JPanel createDescriptionPanel(Component cmpnt) {
                        JPanel p = new JPanel();
                        p.add(createDescriptionLabel());
                        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
                        return p;
                    }

                    @Override
                    protected JComponent createDescriptionLabel() {
                        return new JLabel("Drag items from the dock to the dashboard");
                    }
                };

                palette.setBorder(BorderFactory.createEtchedBorder());
                return palette;
            }
        };
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
        g = GadgetFactory.createChartGadget();
        m.addGadget(g);
        g = GadgetFactory.createResultsGadget();
        m.addGadget(g);
        m.showGadget(g);

        //m.showGadget(g);
    }

    public Component getBanner() {
        JPanel p = ViewUtil.createClearPanel();
        p.add(new JButton("Show in Savant"));
        return p;
    }
}
