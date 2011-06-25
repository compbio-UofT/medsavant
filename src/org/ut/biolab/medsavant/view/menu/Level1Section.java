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
public class Level1Section extends JPanel {
    
    private List<Level2Section> tabs;
    private String name;

    public Level1Section(String name) {
        tabs = new ArrayList<Level2Section>();
        this.name = name;
    }
    
    public List<Level2Section> getTabs() {
        return tabs;
    }
    
    public String getName() {
        return name;
    }
    
    public void addTab(Level2Section t) {
        this.tabs.add(t);
    }
}
