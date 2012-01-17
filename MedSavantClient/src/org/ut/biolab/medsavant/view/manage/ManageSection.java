/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.manage;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.images.IconFactory;
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
        return "Administration";
    }

    @Override
    public Icon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CHART);
    }

    @Override
    public SubSectionView[] getSubSections() {
        SubSectionView[] pages;
        
            pages = new SubSectionView[5];
            pages[0] = new UserManagementPage(this);
            pages[1] = new ProjectManagementPage(this);
            pages[2] = new AnnotationsPage(this);
            pages[3] = new ReferenceGenomePage(this);
            pages[4] = new ServerLogPage(this);
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
