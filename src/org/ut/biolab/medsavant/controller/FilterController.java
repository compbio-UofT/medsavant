/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.util.ArrayList;
import org.ut.biolab.medsavant.db.DB;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.Filter.FilterType;
import org.ut.biolab.medsavant.model.QueryFilter;

/**
 *
 * @author mfiume
 */
public class FilterController {

    private static final ArrayList<Filter> filters = new ArrayList<Filter>();

    public static void addFilter(Filter filter) {
        filters.add(filter);
        printSQLSelect();
    }

    private static void printSQLSelect() {

        SelectQuery q = new SelectQuery();
        q.addAllTableColumns(DB.getInstance().patientTable.getTable());

        for (Filter f : filters) {
            if (f.getType() == FilterType.QUERY) {
                QueryFilter qf = (QueryFilter) f;
                for (Condition c : qf.getConditions()) {
                    q.addCondition(c);
                }
            }
        }

        System.out.println(q);
    }

}
