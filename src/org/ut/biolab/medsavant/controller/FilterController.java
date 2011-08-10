/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import org.ut.biolab.medsavant.model.PostProcessFilter;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.RangeFilter;

/**
 *
 * @author mfiume
 */
public class FilterController {

    
    private static int filterSetID = 0;
    
    private static Map<Integer,Map<String,Filter>> filterMapHistory = new TreeMap<Integer,Map<String,Filter>>();
    private static Map<String,Filter> filterMap = new TreeMap<String,Filter>();
    private static List<FiltersChangedListener> listeners = new ArrayList<FiltersChangedListener>();
    private static List<FiltersChangedListener> activeListeners = new ArrayList<FiltersChangedListener>();
    
    
    private static Filter lastFilter;
    private static FilterAction lastAction;
    public static enum FilterAction {ADDED, REMOVED, MODIFIED};

    public static void addFilter(Filter filter) {
        Filter prev = filterMap.put(filter.getName(), filter);
        if(prev == null){
            setLastFilter(filter, FilterAction.ADDED);
        } else {
            setLastFilter(filter, FilterAction.MODIFIED);
        }
        fireFiltersChangedEvent();
        //printSQLSelect();
    }

    public static void removeFilter(String filtername) {
        Filter removed = filterMap.remove(filtername);
        setLastFilter(removed, FilterAction.REMOVED);
        fireFiltersChangedEvent();
    }

    /*
    private static void printSQLSelect() {

        SelectQuery q = new SelectQuery();
        q.addAllTableColumns(MedSavantDatabase.getInstance().getVariantTableSchema().getTable());

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
     * 
     */

    public static void addFilterListener(FiltersChangedListener l) {
        listeners.add(l);
    }
    
    public static void addActiveFilterListener(FiltersChangedListener l) {
        activeListeners.add(l);
    }
    
    public static int getCurrentFilterSetID() {
        return filterSetID;
    }
    
    public static Map<String,Filter> getFilterSet(int filterSetID) {
        return filterMapHistory.get(filterSetID);
    }

    public static void fireFiltersChangedEvent() {
        
        filterSetID++;
        filterMapHistory.put(filterSetID,filterMap);
        
        //cancel any running workers from last filter
        for (FiltersChangedListener l : activeListeners) {
            try {
                l.filtersChanged();
            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage());
                e.printStackTrace();
            }
        }
        activeListeners.clear();
        
        //start new filter change
        for (FiltersChangedListener l : listeners) {
            try {
                l.filtersChanged();
            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /*
    private static void printFilters() {
        for (Filter f : filterMap.values()) {
            System.out.println(f);
        }
    }
     *
     */

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

    public static List<QueryFilter> getQueryFilters() {
        List<QueryFilter> qfs = new ArrayList<QueryFilter>();
        RangeFilter rf = new RangeFilter() {
            @Override
            public String getName() {
                return "Range Filters";
            }
        };
        boolean hasRangeFilter = false;
        for (Filter f : filterMap.values()) {
            if (f instanceof RangeFilter) {
                rf.merge(((RangeFilter)f).getRangeSet());
                hasRangeFilter = true;
            } else if (f instanceof QueryFilter) {
                qfs.add((QueryFilter) f);
            }
        }
        if(hasRangeFilter){
            qfs.add((QueryFilter)rf);
        }
        return qfs;
    }
    
    private static void setLastFilter(Filter filter, FilterAction action){
        lastFilter = filter;
        lastAction = action;
    }
    
    public static Filter getLastFilter(){
        return lastFilter;
    }
    
    public static FilterAction getLastAction(){
        return lastAction;
    }
    
    public static String getLastActionString(){
        switch(lastAction){
            case ADDED:
                return "Added";
            case REMOVED:
                return "Removed";
            case MODIFIED:
                return "Modified";
            default:
                return "";
        }
    }

}
