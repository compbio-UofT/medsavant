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
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class GeneticsListPage extends SubSectionView {

    private JPanel panel;

    public GeneticsListPage(SectionView parent) { super(parent); }

    
    public String getName() {
        return "List";
    }

    public JPanel getView() {
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

    public Component[] getBanner() {
        Component[] cs = new Component[2];
        cs[0] = new JButton("Save Result Set");
        cs[1] = new JButton("Show in Savant");
        return cs;
    }
}
