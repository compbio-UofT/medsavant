/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import org.ut.biolab.medsavant.view.genetics.*;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class OtherSection extends SectionView {

    private JPanel[] panels;

    public OtherSection() {
    }

    @Override
    public String getName() {
        return "Other";
    }

    @Override
    public Icon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_OTHER);
    }

    @Override
    public SubSectionView[] getSubSections() {
        SubSectionView[] pages;
        pages = new SubSectionView[1];
        pages[0] = new RegionPage(this);
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return panels;
    }

    @Override
    public Component[] getSectionMenuComponents() {
        return null;
    }
}
