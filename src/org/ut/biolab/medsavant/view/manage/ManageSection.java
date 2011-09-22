/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.manage;
import org.ut.biolab.medsavant.view.genetics.*;

import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class ManageSection extends SectionView {
    private JPanel[] panels;

    public ManageSection() {
    }
    
    @Override
    public String getName() {
        return "Management";
    }

    @Override
    public SubSectionView[] getSubSections() {
        SubSectionView[] pages;
        if (LoginController.isAdmin()) {
            pages = new SubSectionView[5];
            pages[0] = new UserManagementPage(this);
            pages[1] = new ProjectManagementPage(this);
            pages[2] = new AnnotationsPage(this);
            pages[3] = new ReferenceGenomePage(this);
            pages[4] = new IntervalPage(this);
        } else {
            pages = new SubSectionView[1];
            pages[0] = new IntervalPage(this);
        }
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return panels;
    }

    @Override
    public Component[] getBanner() {
        return null;
    }

}
