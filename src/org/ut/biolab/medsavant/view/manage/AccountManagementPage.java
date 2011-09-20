/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class AccountManagementPage extends SubSectionView {

    private JPanel panel;

    public AccountManagementPage(SectionView parent) { super(parent); }

    public String getName() {
        return "Account";
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
    }

    public Component[] getBanner() {
        return null;
    }
    
    
    @Override
    public void viewLoading() {
    }

    @Override
    public void viewDidUnload() {
    }
    
    
    
}
