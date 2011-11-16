/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model.event;

import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class SectionChangedEvent {

    private final SectionView section;

    public SectionChangedEvent(SectionView section) {
        this.section = section;
    }

    public SectionView getSection() {
        return section;
    }

}
