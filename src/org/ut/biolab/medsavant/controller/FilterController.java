/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import org.ut.biolab.medsavant.model.PostProcessFilter;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.DB;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.Filter.FilterType;
import org.ut.biolab.medsavant.model.QueryFilter;

/**
 *
 * @author mfiume
 */
public class FilterController {

    private static Map<String,Filter> filterMap = new TreeMap<String,Filter>();
    private static List<FiltersChangedListener> listeners = new ArrayList<FiltersChangedListener>();

    public static void addFilter(Filter filter) {
        filterMap.put(filter.getName(), filter);
        fireFiltersChangedEvent();
        //printSQLSelect();

    }

    public static void removeFilter(String filtername) {
        filterMap.remove(filtername);
        fireFiltersChangedEvent();
    }

    private static void printSQLSelect() {

        SelectQuery q = new SelectQuery();
        q.addAllTableColumns(DB.getInstance().patientTable.getTable());

        for (Filter f : filterMap.values()) {
            if (f.getType() == FilterType.QUERY) {
                QueryFilter qf = (QueryFilter) f;
                for (Condition c : qf.getConditions()) {
                    q.addCondition(c);
                }
            }
        }

        System.out.println(q);
    }

    public static void addFilterListener(FiltersChangedListener l) {
        listeners.add(l);
    }

    private static void fireFiltersChangedEvent() {
        printFilters();
        for (FiltersChangedListener l : listeners) {
            try {
                l.filtersChanged();
            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage());
            }
        }
    }

    private static void printFilters() {
        for (Filter f : filterMap.values()) {
            System.out.println(f);
        }
    }

    public static Filter getFilter(String title) {
        return filterMap.get(title);
    }

    static List<PostProcessFilter> getPostProcessFilters() {
        List<PostProcessFilter> ppfs = new ArrayList<PostProcessFilter>();
        for (Filter f : filterMap.values()) {
            if (f instanceof PostProcessFilter) {
                ppfs.add((PostProcessFilter) f);
            }
        }
        return ppfs;
    }

    static List<QueryFilter> getQueryFilters() {
        List<QueryFilter> qfs = new ArrayList<QueryFilter>();
        for (Filter f : filterMap.values()) {
            if (f instanceof PostProcessFilter) {
                qfs.add((QueryFilter) f);
            }
        }
        return qfs;
    }

}
