/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.patients;

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class PatientsSection extends SectionView {

    
    
    @Override
    public String getName() {
        return "Patients";
    }

    @Override
    public SubSectionView[] getSubSections() {
        SubSectionView[] pages = new SubSectionView[2];
        pages[0] = new IndividualsPage(this);
        pages[1] = new CohortsPage(this);
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return null;
    }

    @Override
    public Component[] getBanner() {
        Component[] cs = new Component[1];
        cs[0] = new JButton("Blah");
        return cs;
    }
    
}
