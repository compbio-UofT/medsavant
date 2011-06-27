/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public abstract class DetailedView extends JPanel {

    private final JLabel title;
    private final JPanel contentPanel;

    public DetailedView() {
        this.setPreferredSize(new Dimension(9999, 310));
        this.setBorder(ViewUtil.getBigBorder());
        
        this.setOpaque(false);
        
        this.setLayout(new BorderLayout());

        JPanel h1 = ViewUtil.getClearPanel();
        h1.setLayout(new BoxLayout(h1,BoxLayout.X_AXIS));
        this.title = ViewUtil.getDetailTitleLabel("");
        h1.add(title);
        h1.add(Box.createHorizontalGlue());
        
        this.add(h1,BorderLayout.NORTH);
        
        contentPanel = ViewUtil.getClearPanel();
        this.add(contentPanel,BorderLayout.CENTER);
    }

    public void paintComponent(Graphics g) {
        PaintUtil.paintDrillDown(g, this);
    }

    public abstract void setSelectedItem(Vector selectedRow);
    
    public abstract void setMultipleSelections(Vector[] selectedRows);
    
    public void setTitle(String str) {
        this.title.setText(str);
    }
    
    public JPanel getContentPanel() {
        return this.contentPanel;
    }
}
