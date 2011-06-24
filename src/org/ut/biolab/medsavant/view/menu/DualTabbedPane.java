/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.menu;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class DualTabbedPane extends JPanel {
    
    
    private JPanel container;
    private CardLayout containerLayout;
    private List<DualTab> dualTabs;
    private DualMenu menu;

    public DualTabbedPane() {
        dualTabs = new ArrayList<DualTab>();
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        
        container = new JPanel();
        container.setBackground(Color.red);
        container.setLayout(new BorderLayout());
        System.out.println("C1:" + container);
        
        JPanel tmpBanner = new JPanel();
        tmpBanner.setLayout(new BoxLayout(tmpBanner,BoxLayout.Y_AXIS));
        
        menu = new DualMenu(container);
        
        this.add(menu,BorderLayout.NORTH);
        this.add(container,BorderLayout.CENTER);
        
    }

    public void addDualTab(DualTab dt) {
        dualTabs.add(dt);
        menu.addDualTab(dt);
        menu.changeSection(dt.getName());
    }
}
