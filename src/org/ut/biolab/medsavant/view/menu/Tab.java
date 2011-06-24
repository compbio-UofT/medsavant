/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.menu;

import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class Tab {
    
    private String name;
    private final JPanel panel;
    
    public Tab(String name, JPanel panel) {
        this.name = name;
        this.panel = panel;
    }
    
    public String getName() {
        return name;
    }

    public JPanel getPanel() {
        return panel;
    }
    
}
