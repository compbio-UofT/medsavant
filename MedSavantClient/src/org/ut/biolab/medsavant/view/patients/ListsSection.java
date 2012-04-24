/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.patients;

import javax.swing.Icon;
import org.ut.biolab.medsavant.view.patients.individual.IndividualsPage;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.manage.IntervalPage;
import org.ut.biolab.medsavant.view.manage.VariantFilesPage;
import org.ut.biolab.medsavant.view.patients.cohorts.CohortsPage;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class ListsSection extends SectionView {

    private SubSectionView[] pages;
    {
        pages = new SubSectionView[4];
        pages[0] = new IndividualsPage(this);
        pages[1] = new CohortsPage(this);
        pages[2] = new IntervalPage(this);
        pages[3] = new VariantFilesPage(this);

     }

    @Override
    public String getName() {
        return "Tables";
    }

    @Override
    public Icon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_PATIENTS);
    }

    @Override
    public SubSectionView[] getSubSections() {
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return null;
    }

}
