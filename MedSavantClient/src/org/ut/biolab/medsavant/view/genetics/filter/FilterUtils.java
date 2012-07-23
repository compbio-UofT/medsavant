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

package org.ut.biolab.medsavant.view.genetics.filter;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.api.MedSavantFilterPlugin;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.ontology.OntologyFilterView;
import org.ut.biolab.medsavant.plugin.MedSavantPlugin;
import org.ut.biolab.medsavant.plugin.PluginController;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.region.RegionSetFilterView;
import org.ut.biolab.medsavant.view.genetics.GeneticsFilterPage;

/**
 *
 * @author Andrew
 */
public class FilterUtils {

    public enum WhichTable {
        PATIENT,
        VARIANT;
        
        public String getName() throws RemoteException, SQLException {
            return this == WhichTable.VARIANT ? ProjectController.getInstance().getCurrentVariantTableName() : ProjectController.getInstance().getCurrentPatientTableName();
        }

        public TableSchema getSchema() {
            return this == WhichTable.VARIANT ? ProjectController.getInstance().getCurrentVariantTableSchema() : ProjectController.getInstance().getCurrentPatientTableSchema();
        }
    };

    /*
     * This should generally be used for any filter applications external
     * to the TablePanel.
     */
    public static List<FilterPanelSubItem> createAndApplyGenericFixedFilter(String title, String description, Condition c) {

        SearchBar fp = getFilterPanel();
        FilterController.setAutoCommit(false);

        //create and apply filter to each subquery
        List<FilterPanelSubItem> filterPanels = new ArrayList<FilterPanelSubItem>();
        for (SearchConditionsPanel fps : fp.getFilterPanelSubs()) {
            String filterId = Long.toString(System.nanoTime());
            FilterView view = new GenericFixedFilterView(title, c, description, fps.getID(), filterId);
            filterPanels.add(fps.addNewSubItem(view, filterId));
        }

        fp.refreshSubPanels();

        FilterController.commit(title, FilterController.FilterAction.ADDED);
        FilterController.setAutoCommit(true);

        return filterPanels;
    }

    public static void createAndApplyNumericFilterView(String column, String alias, WhichTable whichTable, double low, double high) throws SQLException, RemoteException{

        SearchBar fp = startFilterBy(column);
        FilterController.setAutoCommit(false);

        //create and apply filter to each subquery
        for (SearchConditionsPanel fps : fp.getFilterPanelSubs()) {
            FilterView view = new NumericFilterView(whichTable, column, fps.getID(), alias, false);
            fps.addNewSubItem(view, column);
            ((NumericFilterView)view).applyFilter(low, high);
        }

        fp.refreshSubPanels();

        FilterController.commit(alias, FilterController.FilterAction.ADDED);
        FilterController.setAutoCommit(true);
    }

    public static void createAndApplyStringListFilterView(String column, String alias, WhichTable whichTable, List<String> values) throws SQLException, RemoteException {

        SearchBar fp = startFilterBy(column);
        FilterController.setAutoCommit(false);

        //create and apply filter to each subquery
        for (SearchConditionsPanel fps : fp.getFilterPanelSubs()) {
            FilterView view = new StringListFilterView(whichTable, column, fps.getID(), alias);
            fps.addNewSubItem(view, column);
            ((StringListFilterView)view).applyFilter(values);
        }

        fp.refreshSubPanels();

        FilterController.commit(alias, FilterController.FilterAction.ADDED);
        FilterController.setAutoCommit(true);
    }

    public static void removeFiltersById(String id) {
        removeFiltersById(getFilterPanel(), id);
    }

    public static void loadFilterView(FilterState state, SearchConditionsPanel fps) throws SQLException, RemoteException {
        switch (state.getType()) {
            case NUMERIC:
                fps.addNewSubItem(new NumericFilterView(state, fps.getID()), state.getID());
                break;
            case STRING:
                fps.addNewSubItem(new StringListFilterView(state, fps.getID()), state.getID());
                break;
            case BOOLEAN:
                fps.addNewSubItem(new BooleanFilterView(state, fps.getID()), state.getID());
                break;
            case REGION_LIST:
                fps.addNewSubItem(new RegionSetFilterView(state, fps.getID()), state.getID());
                break;
            case COHORT:
                fps.addNewSubItem(new CohortFilterView(state, fps.getID()), state.getID());
                break;
            case GENERIC:
                fps.addNewSubItem(new GenericFixedFilterView(state, fps.getID()), state.getID());
                break;
            case TAG:
                fps.addNewSubItem(new TagFilterView(state, fps.getID()), state.getID());
                break;
            case STARRED:
                fps.addNewSubItem(new StarredFilterView(state, fps.getID()), state.getID());
                break;
            case ONTOLOGY:
                fps.addNewSubItem(new OntologyFilterView(state, fps.getID()), state.getID());
                break;
            case PLUGIN:
                PluginController pc = PluginController.getInstance();
                for (PluginDescriptor desc: pc.getDescriptors()) {
                    final MedSavantPlugin p = pc.getPlugin(desc.getID());
                    if (p instanceof MedSavantFilterPlugin && ((MedSavantFilterPlugin)p).getTitle().equals(state.getName())) {
                        FilterView view = PluginFilterView.getFilterView((MedSavantFilterPlugin)p, fps.getID());
                        ((MedSavantFilterPlugin)p).loadState(state.getValues(), fps.getID());
                        fps.addNewSubItem(view, state.getID());
                    }
                }
                break;
        }
    }


    /*
     * Common functionality for all created filters
     */
    private static SearchBar startFilterBy(String column) {

        //get filter panel
        SearchBar fp = getFilterPanel();

        //remove filters by id
        removeFiltersById(fp, column);

        return fp;
    }

    private static SearchBar getFilterPanel() {
        /*FilterPanel fp = GeneticsFilterPage.getInstance().getFilterPanel();
        if (fp == null) {
            GeneticsFilterPage.getInstance().getView(true);
            GeneticsFilterPage.getInstance().setUpdateRequired(false);
            fp = GeneticsFilterPage.getInstance().getFilterPanel();
        }*/
        SearchBar fp = GeneticsFilterPage.getFilterPanel();

        //deal with case where no sub panels
        if (fp.getFilterPanelSubs().isEmpty()) {
            fp.createNewSubPanel();
        }

        return fp;
    }

    private static void removeFiltersById(SearchBar fp, String id) {
        for (SearchConditionsPanel fps : fp.getFilterPanelSubs()) {
            fps.removeFiltersByID(id);
        }
    }

    public static void clearFilterSets() {
        FilterController.removeAllFilters();
        SearchBar fp = getFilterPanel();
        fp.clearAll();
    }
}
