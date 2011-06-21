/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;
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
