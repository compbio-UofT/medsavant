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
package org.ut.biolab.medsavant.client.cohort;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.FilterStateAdapter;
import org.ut.biolab.medsavant.client.filter.Filter;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterState;
import org.ut.biolab.medsavant.client.filter.TabularFilterView;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 * Filter by cohort; a little more complicated than a straight string-list filter.
 *
 * @author tarkvara
 */
public class CohortFilterView extends TabularFilterView<Cohort> {
    public static final String FILTER_NAME = "Cohort";
    public static final String FILTER_ID = "cohort";

    /**
     * Constructor for loading a saved filter from a file.
     */
    public CohortFilterView(FilterState state, int queryID) throws SQLException, RemoteException {
        this(queryID);
        List<String> values = state.getValues("value");
        if (values != null) {
            setFilterValues(values);
        }
    }

    /**
     * Constructor which holders use to create a fresh filter-view.
     */
    public CohortFilterView(int queryID) throws SQLException, RemoteException {
        super(FILTER_NAME, queryID);
        List vals = new ArrayList<Cohort>();
        try {
            vals.addAll(Arrays.asList(MedSavantClient.CohortManager.getCohorts(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID())));
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
        }
        setAvailableValues(vals);
        initContentPanel();
    }

    public static FilterState wrapState(Collection<Cohort> applied) {
        FilterState state = new FilterState(Filter.Type.COHORT, FILTER_NAME, FILTER_ID);
        state.putValues(FilterState.VALUE_ELEMENT, wrapValues(applied));
        return state;
    }

    @Override
    public FilterStateAdapter saveState() {
        return wrapState(appliedValues);
    }

    @Override
    protected void applyFilter() {
        preapplyFilter();
        FilterController.getInstance().addFilter(new CohortFilter(), queryID);
    }

    private class CohortFilter extends Filter {
        @Override
        public String getName() {
            return FILTER_NAME;
        }

        @Override
        public String getID() {
            return FILTER_ID;
        }

        @Override
        public Condition[] getConditions() throws SQLException, RemoteException {
            if (appliedValues.size() > 0) {
                List<String> cohNames = new ArrayList<String>();
                for (Cohort coh: appliedValues) {
                    cohNames.add(coh.getName());
                }
                try {
                    return getDNAIDCondition(MedSavantClient.CohortManager.getDNAIDsForCohorts(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), cohNames));
                } catch (SessionExpiredException ex) {
                    MedSavantExceptionHandler.handleSessionExpiredException(ex);
                }
            }
            return FALSE_CONDITION;
        }
    }
}
