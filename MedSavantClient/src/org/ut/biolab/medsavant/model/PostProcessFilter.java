/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model;

import java.util.List;
import org.ut.biolab.medsavant.model.Filter.FilterType;
import org.ut.biolab.medsavant.vcf.VariantRecord;

/**
 *
 * @author mfiume
 */
public abstract class PostProcessFilter extends Filter {

    public PostProcessFilter() {
        super(FilterType.POSTQUERY);
    }
    
    public abstract List<VariantRecord> filterResults(List<VariantRecord> results);

    public abstract String toString();
}
