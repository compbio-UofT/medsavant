/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model;

import java.util.List;
import java.util.concurrent.locks.Condition;
import org.ut.biolab.medsavant.model.Filter.FilterType;

/**
 *
 * @author mfiume
 */
public abstract class PostProcessFilter extends Filter {

    public PostProcessFilter() {
        super(FilterType.POSTQUERY);
    }
    
    public abstract List<MedSavantRecord> filterResults(List<MedSavantRecord> results);
}
