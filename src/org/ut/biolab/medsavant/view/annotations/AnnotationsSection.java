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
        SubSectionView[] pages = new SubSectionView[2];
        pages[0] = new IntervalPage(this);
        pages[1] = new SNPPage(this);
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return null;
    }
}
