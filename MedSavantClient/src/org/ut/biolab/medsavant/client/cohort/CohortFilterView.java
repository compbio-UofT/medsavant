/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.client.cohort;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.FilterStateAdapter;
import org.ut.biolab.medsavant.client.filter.Filter;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterState;
import org.ut.biolab.medsavant.client.filter.TabularFilterView;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.client.project.ProjectController;


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
        availableValues = new ArrayList<Cohort>();
        availableValues.addAll(Arrays.asList(MedSavantClient.CohortManager.getCohorts(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID())));
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
                return getDNAIDCondition(MedSavantClient.CohortManager.getDNAIDsForCohorts(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), cohNames));
            }
            return FALSE_CONDITION;
        }
    }
}
