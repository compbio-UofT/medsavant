/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model.event;

/**
 *
 * @author mfiume
 */
public class SectionChangedEvent {

    private final String section;

    public SectionChangedEvent(String section) {
        this.section = section;
    }

    public String getSection() {
        return section;
    }

}
