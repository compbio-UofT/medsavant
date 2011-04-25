/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model;

import java.util.List;

/**
 *
 * @author mfiume
 */
public abstract class Filter {

    public enum FilterType { QUERY, POSTQUERY };

    private FilterType type;

    public Filter(FilterType t) {
        this.type = t;
    }

    public FilterType getType() {
        return type;
    }
    
}
