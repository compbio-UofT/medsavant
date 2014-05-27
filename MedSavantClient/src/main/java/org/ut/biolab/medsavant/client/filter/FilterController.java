/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
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

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.view.login.LoginEvent;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.project.ProjectEvent;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.util.Controller;


/**
 *
 * @author mfiume
 */
public class FilterController extends Controller<FilterEvent> {

    private static final Log LOG = LogFactory.getLog(FilterController.class);

    private static FilterController instance;

    private int filterSetID = 0;
    private Map<Integer, Map<String, Filter>> filterMap = new TreeMap<Integer, Map<String, Filter>>();
    private Condition newConditions = BinaryCondition.equalTo(1,1);

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

    @Override
    public void fireEvent(FilterEvent evt) {
        filterSetID++;
        super.fireEvent(evt);
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

    public void removeQuery(int queryID) {
        Map<String, Filter> map = filterMap.remove(queryID);
        if (map == null ||map.isEmpty()) return;
        Filter f = new Filter() {
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

    public Condition[] getFilterConditions(int queryID) throws InterruptedException, SQLException, RemoteException {
        return new Condition[] { this.newConditions };
    }

    public Condition[][] getAllFilterConditions() throws InterruptedException, SQLException, RemoteException {
        return new Condition[][] { new Condition[] { this.newConditions }};
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


    public void setConditions(Condition conds) {
        this.newConditions = conds;
        fireEvent(new FilterEvent(FilterEvent.Type.MODIFIED, new Filter() {

            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getID() {
                return "";
            }

            @Override
            public Condition[] getConditions() throws InterruptedException, SQLException, RemoteException {
                return new Condition[] {};
            }
 }));
    }
}
