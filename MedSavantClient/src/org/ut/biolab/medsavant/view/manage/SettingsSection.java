/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.manage;
import org.ut.biolab.medsavant.view.genetics.*;

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
public class SettingsSection extends SectionView {
    private JPanel[] panels;

    public SettingsSection() {
    }
    
    @Override
    public String getName() {
        return "Settings";
    }
    
    @Override
    public Icon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CHART);
    }

    @Override
    public SubSectionView[] getSubSections() {
        SubSectionView[] pages = new SubSectionView[1];
        pages[0] = new DatabaseManagementPage(this);
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
