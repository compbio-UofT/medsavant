/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model;

import com.healthmarketscience.sqlbuilder.Condition;
import org.ut.biolab.medsavant.model.Filter.FilterType;

/**
 *
 * @author mfiume
 */
public abstract class QueryFilter extends Filter {

    public QueryFilter() {
        super(FilterType.QUERY);
    }
    
    public boolean unionOverConditions() { return false; }
    
    public abstract Condition[] getConditions();
}
