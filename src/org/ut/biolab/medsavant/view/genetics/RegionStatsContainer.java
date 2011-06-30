/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Nirvana Nursimulu
 */
public class RegionStatsContainer extends JPanel{
    
    JPanel container;
    
    public void addChart() {
        JPanel p = new RegionStatsPanel();
        p.setPreferredSize(new Dimension(800, 230));
        p.setMaximumSize(new Dimension(800, 230));
        //p.setBorder(ViewUtil.getTinyLineBorder());
        container.add(p);
    }

    public RegionStatsContainer() {
        this.setLayout(new BorderLayout());
        container = ViewUtil.getClearPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        JScrollPane sp = (JScrollPane) ViewUtil.clear(new JScrollPane(container));
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        this.add(sp, BorderLayout.CENTER);
    }
    
}
