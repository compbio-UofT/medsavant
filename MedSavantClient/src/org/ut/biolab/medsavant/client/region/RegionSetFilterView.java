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

package org.ut.biolab.medsavant.client.region;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.client.api.FilterStateAdapter;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.Filter;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterState;
import org.ut.biolab.medsavant.client.filter.TabularFilterView;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.RegionSet;


/**
 *
 * @author mfiume
 */
public class RegionSetFilterView extends TabularFilterView<RegionSet> {

    public static final String FILTER_NAME = "Region List";
    public static final String FILTER_ID = "region_list";
    private static final String AD_HOC_REGION_SET_NAME = "Selected Regions";

    private static RegionController controller = RegionController.getInstance();

    public RegionSetFilterView(FilterState state, int queryID) throws SQLException, RemoteException {
        this(queryID);
        List<String> names = state.getValues("name");

        // May also need to restore any ad-hoc region filters found in the file.
        List<String> regStrs = state.getValues("region");
        if (regStrs != null && regStrs.size() > 0) {
            List<GenomicRegion> adHocRegions = new ArrayList<GenomicRegion>(regStrs.size());
            for (String str: regStrs) {
                adHocRegions.add(GenomicRegion.fromString(str));
            }
            availableValues.add(controller.createAdHocRegionSet(AD_HOC_REGION_SET_NAME, adHocRegions));
            updateModel();
            if (names == null) {
                names = new ArrayList<String>(1);
            }
            names.add(AD_HOC_REGION_SET_NAME);
        }
        if (names != null) {
            setFilterValues(names);
        }

    }

    public RegionSetFilterView(int queryID) throws SQLException, RemoteException {
        super(FILTER_NAME, queryID);
        availableValues = new ArrayList<RegionSet>();
        availableValues.addAll(controller.getRegionSets());
        initContentPanel();

        // Make sure the filter's check-boxes get updated whenever region sets are added or removed.
        controller.addListener(new Listener<RegionEvent>() {
            @Override
            public void handleEvent(RegionEvent event) {
                updateModel();
            }
        });
    }

    public static FilterState wrapState(Collection<RegionSet> applied) {
        List<String> names = new ArrayList<String>();
        List<GenomicRegion> adHocRegions = new ArrayList<GenomicRegion>();
        if (applied != null && !applied.isEmpty()) {
            names = new ArrayList<String>();
            for (RegionSet regSet: applied) {
                if (regSet instanceof AdHocRegionSet) {
                    try {
                        adHocRegions.addAll(controller.getRegionsInSet(regSet));
                    } catch (Exception ignored) {
                        // The signature for getRegionsInSet throws SQLException and RemoteException, but these don't occur with local regions.
                    }
                } else {
                    names.add(regSet.toString());
                }
            }
        }
        FilterState state = new FilterState(Filter.Type.REGION_LIST, FILTER_NAME, FILTER_ID);
        state.putValues("name", names);
        state.putValues("region", wrapValues(adHocRegions));
        return state;
    }

    @Override
    public FilterStateAdapter saveState() {
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
