/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class DatabaseManagementPage extends SubSectionView {

    private JPanel panel;

    public DatabaseManagementPage(SectionView parent) { super(parent); }

    public String getName() {
        return "Database";
    }

    public JPanel getView(boolean update) {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }

    private void setPanel() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
  
        
        //panel.setBackground(Color.red);
        //panel.add(new TablePanel(), BorderLayout.CENTER);
    }

    public Component[] getBanner() {
        return null;
    }
    
    
    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }
    
}
