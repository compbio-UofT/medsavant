/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.RangeFilter;

/**
 *
 * @author mfiume
 */
public class FilterController{

    
    private static int filterSetID = 0;
    
    //private static Map<Integer,Map<String,Filter>> filterMapHistory = new TreeMap<Integer,Map<String,Filter>>();
    private static Map<Integer, Map<Integer, Map<String, Filter>>> filterMapHistory = new TreeMap<Integer, Map<Integer, Map<String, Filter>>>();
    //private static Map<String,Filter> filterMap = new TreeMap<String,Filter>();
    private static Map<Integer, Map<String, Filter>> filterMap = new TreeMap<Integer, Map<String, Filter>>();   
    private static List<FiltersChangedListener> listeners = new ArrayList<FiltersChangedListener>();
    private static List<FiltersChangedListener> activeListeners = new ArrayList<FiltersChangedListener>();
    
    private static Filter lastFilter;
    private static FilterAction lastAction;

    private static ProjectListener projectListener;
    private static ReferenceListener referenceListener;
    
    
    public static void init(){
        if(projectListener == null){
            projectListener = new ProjectListener() {
                public void projectAdded(String projectName) {}
                public void projectRemoved(String projectName) {}
                public void projectChanged(String projectName) {
                    removeAllFilters();
                }
                public void projectTableRemoved(int projid, int refid) {}
            };           
            ProjectController.getInstance().addProjectListener(projectListener);
        }
        if(referenceListener == null){
            referenceListener = new ReferenceListener() {
                public void referenceAdded(String name) {}
                public void referenceRemoved(String name) {}
                public void referenceChanged(String prnameojectName) {
                    removeAllFilters();
                }              
            };
            ReferenceController.getInstance().addReferenceListener(referenceListener);
        }
    }
    
    public static enum FilterAction {ADDED, REMOVED, MODIFIED};

    public static void addFilter(Filter filter, int queryId) {

        if(filterMap.get(queryId) == null){
            filterMap.put(queryId, new TreeMap<String, Filter>());
        }
        Filter prev = filterMap.get(queryId).put(filter.getId(), filter);
        
        if(prev == null){
            setLastFilter(filter, FilterAction.ADDED);
        } else {
            setLastFilter(filter, FilterAction.MODIFIED);
        }
        fireFiltersChangedEvent();
        //printSQLSelect();
    }

    public static void removeFilter(String filterId, int queryId) {

        if(filterMap.get(queryId) == null) return; //filter was never actually added
        
        Filter removed = filterMap.get(queryId).remove(filterId);
        if(filterMap.get(queryId).isEmpty()){
            filterMap.remove(queryId);
        }
        
        if(removed == null) return; //something went wrong, but ignore it
        setLastFilter(removed, FilterAction.REMOVED);
        fireFiltersChangedEvent();
    }
    
    public static void removeAllFilters(){
        filterMap.clear();
        filterMapHistory.clear();
    }

    public static void addFilterListener(FiltersChangedListener l) {
        listeners.add(l);
    }
    
    public static void addActiveFilterListener(FiltersChangedListener l) {
        activeListeners.add(l);
    }
    
    public static int getCurrentFilterSetID() {
        return filterSetID;
    }
    
    //public static Map<String,Filter> getFilterSet(int filterSetID) {
    public static Map<Integer, Map<String, Filter>> getFilterSet(int filterSetID) {
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

    public static Filter getFilter(String title, int queryId) {
        //return filterMap.get(title);
        return filterMap.get(queryId).get(title);
    }

   /* static List<PostProcessFilter> getPostProcessFilters() {
        List<PostProcessFilter> ppfs = new ArrayList<PostProcessFilter>();
        for (Filter f : filterMap.values()) {
            if (f instanceof PostProcessFilter) {
                ppfs.add((PostProcessFilter) f);
            }
        }  
        return ppfs;
    }*/

    public static List<QueryFilter> getQueryFilters(int queryId) {
        List<QueryFilter> qfs = new ArrayList<QueryFilter>();
        RangeFilter rf = new RangeFilter() {
            @Override
            public String getName() {
                return "Range Filters";
            }
            @Override
            public String getId() {
                return "range_filters";
            }
        };
        boolean hasRangeFilter = false;
        for (Filter f : filterMap.get(queryId).values()) {
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
    
    public static List<List<QueryFilter>> getQueryFilters(){
        List<List<QueryFilter>> qfs = new ArrayList<List<QueryFilter>>();
        for(Object key : filterMap.keySet().toArray()){
            qfs.add(getQueryFilters((Integer)key));
        }
        return qfs;
    }
    
    /*public static List<Condition> getQueryFilterConditions(int queryId) {
        List<Condition> conditions = new ArrayList<Condition>();
        for(QueryFilter f : FilterController.getQueryFilters(queryId)){
            conditions.add(ComboCondition.or(f.getConditions()));
        }
        return conditions;
    }*/
    
    public static Condition[] getQueryFilterConditions(int queryId) {
        List<QueryFilter> filters = getQueryFilters(queryId);
        Condition[] conditions = new Condition[filters.size()];
        for(int i = 0; i < filters.size(); i++){
            conditions[i] = ComboCondition.or(filters.get(i).getConditions());
        }
        return conditions;
    }
    
    /*public static List<List<Condition>> getQueryFilterConditions() {
        List<List<Condition>> conditions = new ArrayList<List<Condition>>();
        for(Object key : filterMap.keySet().toArray()){
            conditions.add(getQueryFilterConditions((Integer)key));
        }
        return conditions;
    }*/
    
    public static Condition[][] getQueryFilterConditions() {
        Object[] keys = filterMap.keySet().toArray();
        Condition[][] conditions = new Condition[keys.length][];
        for(int i = 0; i < keys.length; i++){
            conditions[i] = getQueryFilterConditions((Integer)keys[i]);
        }
        return conditions;
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
    
    public static boolean hasFiltersApplied(){
        for(Integer key : filterMap.keySet()){
            Map<String, Filter> current = filterMap.get(key);
            if(current != null && !current.isEmpty()){
                return true;
            }
        }
        return false;
    }

    public static boolean isFilterActive(int queryId, String filterId){
        return filterMap.containsKey(queryId) && filterMap.get(queryId).containsKey(filterId);
    }
    
}
