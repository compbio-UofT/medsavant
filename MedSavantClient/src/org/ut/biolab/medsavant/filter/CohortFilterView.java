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

package org.ut.biolab.medsavant.filter;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Cohort;
import org.ut.biolab.medsavant.project.ProjectController;


/**
 * Filter by cohort; a little more complicated than a straight string-list filter.
 *
 * @author tarkvara
 */
public class CohortFilterView extends TabularFilterView<Cohort> {
    public static final String FILTER_NAME = "Cohort";
    public static final String FILTER_ID = "cohort";

    Cohort[] cohorts;

    /**
     * Constructor for loading a saved filter from a file.
     */
    public CohortFilterView(FilterState state, int queryID) throws SQLException, RemoteException {
        this(queryID);
        String values = state.getValues().get("values");
        if (values != null) {
            setFilterValues(Arrays.asList(values.split(";;;")));
        }
    }

    /**
     * Constructor which holders use to create a fresh filter-view.
     */
    public CohortFilterView(int queryID) throws SQLException, RemoteException {
        super(FILTER_NAME, queryID);
        availableValues = new ArrayList<Cohort>();
        availableValues.addAll(Arrays.asList(MedSavantClient.CohortManager.getCohorts(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID())));
        initContentPanel();
    }

    public static FilterState wrapState(Collection<Cohort> applied) {
        Map<String, String> map = new HashMap<String, String>();
        if (applied != null && !applied.isEmpty()) {
            StringBuilder values = new StringBuilder();
            int i = 0;
            for (Cohort val: applied) {
                values.append(val.toString());
                if (i++ != applied.size() - 1) {
                    values.append(";;;");
                }
            }
            map.put("values", values.toString());
        }
        return new FilterState(Filter.Type.COHORT, FILTER_NAME, FILTER_ID, map);
    }

    @Override
    public FilterState saveState() {
        return wrapState(appliedValues);
    }

    @Override
    protected void applyFilter() {
        applyButton.setEnabled(false);

        appliedValues = new ArrayList<Cohort>();

        int[] indices = filterableList.getCheckBoxListSelectedIndices();
        for (int i: indices) {
            appliedValues.add((Cohort)filterableList.getModel().getElementAt(i));
        }

        FilterController fc = FilterController.getInstance();
        if (appliedValues.size() == availableValues.size()) {
            fc.removeFilter(FILTER_ID, queryID);
            return;
        }

        fc.addFilter(new CohortFilter(), queryID);
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
                return getDNAIDCondition(MedSavantClient.CohortManager.getDNAIDsForCohorts(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), cohNames));
            }
            return FALSE_CONDITION;
        }
    }
}
