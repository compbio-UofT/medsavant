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

package org.ut.biolab.medsavant.region;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.filter.*;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.util.ClientMiscUtils;


/**
 *
 * @author mfiume
 */
public class RegionSetFilterView extends TabularFilterView<RegionSet> {

    public static final String FILTER_NAME = "Region List";
    public static final String FILTER_ID = "region_list";

    private final RegionController controller;

    public RegionSetFilterView(FilterState state, int queryID) throws SQLException, RemoteException {
        this(queryID);
        String values = state.getValues().get("values");
        if (values != null) {
            setFilterValues(Arrays.asList(values.split(";;;")));
        }
    }

    public RegionSetFilterView(int queryID) throws SQLException, RemoteException {
        super(FILTER_NAME, queryID);
        controller = RegionController.getInstance();
        availableValues = new ArrayList<RegionSet>();
        availableValues.addAll(Arrays.asList(controller.getRegionSets()));
        initContentPanel();

        RegionController.getInstance().addListener(new Listener<RegionEvent>() {
            @Override
            public void handleEvent(RegionEvent event) {
                availableValues.clear();
                try {
                    availableValues.addAll(Arrays.asList(controller.getRegionSets()));
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Unable to populate region list: %s", ex);
                }
            }
        });
    }

    public static FilterState wrapState(Collection<RegionSet> applied) {
        return new FilterState(Filter.Type.REGION_LIST, FILTER_NAME, FILTER_ID, wrapValues(applied));
    }

    @Override
    public FilterState saveState() {
        return wrapState(appliedValues);
    }

    @Override
    protected void applyFilter() {
        preapplyFilter();

        FilterController.getInstance().addFilter(new RegionSetFilter() {
            @Override
            public Condition[] getConditions() throws SQLException, RemoteException {
                return getConditions(controller.getRegionsInSets(appliedValues));
            }

            @Override
            public String getName() {
                return FILTER_NAME;
            }


            @Override
            public String getID() {
                return FILTER_ID;
            }
        }, queryID);
    }
}
