/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public abstract class SubView extends JPanel {
    private JTabbedPane pane;

    public SubView() {
        this.setBackground(ViewUtil.getLightColor());
        initPages();
    }

    public abstract String getName();

    public abstract Page[] getPages();

    public abstract JPanel getPersistentPanel();

    private void initPages() {
        Page[] pages = getPages();
        pane = new JTabbedPane(JTabbedPane.LEFT);
        for (Page p : pages) {
            pane.addTab(p.getName(), p.getView());
        }
        this.setLayout(new BorderLayout());
        this.add(pane, BorderLayout.CENTER);

        JPanel p = getPersistentPanel();
        if (p != null) {
            this.add(p,BorderLayout.EAST);
        }
    }

}
