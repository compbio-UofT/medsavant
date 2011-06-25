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
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class SectionNavigator extends JPanel {
    
    private JPanel container;
    private HierarchicalMenu menu;

    public SectionNavigator() {
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        
        container = new JPanel();
        container.setBackground(Color.red);
        container.setLayout(new BorderLayout());
        
        JPanel tmpBanner = new JPanel();
        tmpBanner.setLayout(new BoxLayout(tmpBanner,BoxLayout.Y_AXIS));
        
        menu = new HierarchicalMenu(container);
        
        this.add(menu,BorderLayout.NORTH);
        this.add(container,BorderLayout.CENTER);
        
    }

    public void addSection(SectionView section) {
        menu.addSection(section);
    }
}
