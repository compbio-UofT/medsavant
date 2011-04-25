/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model;

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.List;
import org.ut.biolab.medsavant.model.Filter.FilterType;

/**
 *
 * @author mfiume
 */
public abstract class QueryFilter extends Filter {

    public QueryFilter() {
        super(FilterType.QUERY);
    }
    
    public abstract List<Condition> getConditions();
}
