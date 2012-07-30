/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.controller;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.event.LoginEvent;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.RangeFilter;
import org.ut.biolab.medsavant.model.event.FilterEvent;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.project.ProjectEvent;
import org.ut.biolab.medsavant.event.ReferenceEvent;
import org.ut.biolab.medsavant.util.Controller;


/**
 *
 * @author mfiume
 */
public class FilterController extends Controller<FilterEvent> {

    private static final Log LOG = LogFactory.getLog(FilterController.class);

    private static FilterController instance;

    private int filterSetID = 0;
    private Map<Integer, Map<String, Filter>> filterMap = new TreeMap<Integer, Map<String, Filter>>();

    private FilterController() {
        ProjectController.getInstance().addListener(new Listener<ProjectEvent>() {
            @Override
            public void handleEvent(ProjectEvent evt) {
                if (evt.getType() == ProjectEvent.Type.CHANGED) {
                    removeAllFilters();
                }
            }
        });

        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    removeAllFilters();
                }
            }
        });

        LoginController.getInstance().addListener(new Listener<LoginEvent>() {
            @Override
            public void handleEvent(LoginEvent evt) {
                if (evt.getType() == LoginEvent.Type.LOGGED_OUT) {
                    removeAllFilters();
                }
            }

        });
    }

    public static FilterController getInstance() {
        if (instance == null) {
            instance = new FilterController();
        }
        return instance;
    }

    public void addFilter(Filter filter, int queryID) {
        if (filterMap.get(queryID) == null) {
            filterMap.put(queryID, new TreeMap<String, Filter>());
        }
        Filter prev = filterMap.get(queryID).put(filter.getID(), filter);
        fireEvent(new FilterEvent(prev == null ? FilterEvent.Type.ADDED : FilterEvent.Type.MODIFIED, filter));
    }

    public void removeFilter(String filtID, int queryID) {

        if (filterMap.get(queryID) != null) {

            Filter removed = filterMap.get(queryID).remove(filtID);
            if (filterMap.get(queryID).isEmpty()) {
                filterMap.remove(queryID);
            }

            if (removed != null) {
                fireEvent(new FilterEvent(FilterEvent.Type.REMOVED, removed));
            }
        }
    }

    public void removeFilterSet(int queryID) {
        Map<String, Filter> map = filterMap.remove(queryID);
        if (map == null ||map.isEmpty()) return;
        Filter f = new QueryFilter() {
            @Override
            public String getName() {
                return "Filter Set";
            }

            @Override
            public String getID() {
                return null;
            }

            @Override
            public Condition[] getConditions() {
                return null;
            }
        };
        fireEvent(new FilterEvent(FilterEvent.Type.REMOVED, f));
    }

    /**
     * Called when the project or reference has changed, or when the user logs out.
     */
    public void removeAllFilters() {
        filterMap.clear();
    }

    public int getCurrentFilterSetID() {
        return filterSetID;
    }

    public Filter getFilter(String title, int queryID) {
        return filterMap.get(queryID).get(title);
    }

    public List<QueryFilter> getQueryFilters(int queryID) {
        List<QueryFilter> qfs = new ArrayList<QueryFilter>();
        RangeFilter rf = new RangeFilter() {
            @Override
            public String getName() {
                return "Range Filters";
            }
            @Override
            public String getID() {
                return "range_filters";
            }
        };
        boolean hasRangeFilter = false;
        for (Filter f : filterMap.get(queryID).values()) {
            if (f instanceof RangeFilter) {
                rf.merge(((RangeFilter)f).getRangeSet());
                hasRangeFilter = true;
            } else if (f instanceof QueryFilter) {
                qfs.add((QueryFilter) f);
            }
        }
        if (hasRangeFilter) {
            qfs.add((QueryFilter)rf);
        }
        return qfs;
    }

    public List<List<QueryFilter>> getQueryFilters() {
        List<List<QueryFilter>> qfs = new ArrayList<List<QueryFilter>>();
        for (Object key : filterMap.keySet().toArray()) {
            qfs.add(getQueryFilters((Integer)key));
        }
        return qfs;
    }

    public Condition[] getQueryFilterConditions(int queryID) throws SQLException, RemoteException {
        List<QueryFilter> filters = prioritizeFilters(getQueryFilters(queryID));
        Condition[] conditions = new Condition[filters.size()];
        for (int i = 0; i < filters.size(); i++) {
            conditions[i] = ComboCondition.or(filters.get(i).getConditions());
        }
        return conditions;
    }

    private List<QueryFilter> prioritizeFilters(List<QueryFilter> filters) {

        List<QueryFilter> result = new ArrayList<QueryFilter>();
        addFiltersToList(filters, result, DefaultVariantTableSchema.COLUMNNAME_OF_CHROM);
        addFiltersToList(filters, result, DefaultVariantTableSchema.COLUMNNAME_OF_POSITION);
        for (QueryFilter f : filters) result.add(f); //remaining

        return result;
    }

    //add anything from filters with filterId to list
    private void addFiltersToList(List<QueryFilter> filters, List<QueryFilter> list, String filterId) {
        for (int i = filters.size()-1; i >= 0; i--) {
            if (filters.get(i).getID().equals(filterId)) {
                list.add(filters.remove(i));
            }
        }
    }

    public Condition[][] getQueryFilterConditions() throws SQLException, RemoteException {
        Object[] keys = filterMap.keySet().toArray();
        Condition[][] conditions = new Condition[keys.length][];
        for (int i = 0; i < keys.length; i++) {
            conditions[i] = getQueryFilterConditions((Integer)keys[i]);
        }
        return conditions;
    }

    public boolean hasFiltersApplied() {
        for (Integer key : filterMap.keySet()) {
            Map<String, Filter> current = filterMap.get(key);
            if (current != null && !current.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFilterActive(int queryId, String filterId) {
        return filterMap.containsKey(queryId) && filterMap.get(queryId).containsKey(filterId);
    }
}
