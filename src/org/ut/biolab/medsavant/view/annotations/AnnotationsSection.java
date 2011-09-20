/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.annotations;

import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.annotations.interval.IntervalPage;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class AnnotationsSection extends SectionView {

    @Override
    public String getName() {
        return "Annotations";
    }

    @Override
    public SubSectionView[] getSubSections() {
        SubSectionView[] pages = new SubSectionView[3];
        pages[0] = new ReferenceGenomePage(this);
        pages[2] = new IntervalPage(this);
        pages[1] = new AnnotationsPage(this);//new PerPositionPage(this);
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return null;
    }
}
