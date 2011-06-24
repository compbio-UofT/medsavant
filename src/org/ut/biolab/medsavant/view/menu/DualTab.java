/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.menu;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class DualTab extends JPanel {
    
    private List<Tab> tabs;
    private String name;

    public DualTab(String name) {
        tabs = new ArrayList<Tab>();
        this.name = name;
    }
    
    public List<Tab> getTabs() {
        return tabs;
    }
    
    public String getName() {
        return name;
    }
    
    public void addTab(Tab t) {
        this.tabs.add(t);
    }
}
