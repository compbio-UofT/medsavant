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
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.plugin.MedSavantPlugin;
import org.ut.biolab.medsavant.plugin.PluginController;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.view.genetics.GeneticsFilterPage;

/**
 *
 * @author Andrew
 */
public class FilterUtils {

    public enum Table {PATIENT, VARIANT};

    /*
     * This should generally be used for any filter applications external
     * to the TablePanel.
     */
    public static List<FilterPanelSubItem> createAndApplyGenericFixedFilter(String title, String description, Condition c) {

        FilterPanel fp = getFilterPanel();
        FilterController.setAutoCommit(false);

        //create and apply filter to each subquery
        List<FilterPanelSubItem> filterPanels = new ArrayList<FilterPanelSubItem>();
        for (FilterPanelSub fps : fp.getFilterPanelSubs()) {
            String filterId = Long.toString(System.nanoTime());
            FilterView view = GenericFixedFilterView.createGenericFixedView(title, c, description, fps.getId(), filterId);
            filterPanels.add(fps.addNewSubItem(view, filterId));
        }

        fp.refreshSubPanels();
        
        FilterController.commit(title, FilterController.FilterAction.ADDED);
        FilterController.setAutoCommit(true);
        
        return filterPanels;
    }

    public static void createAndApplyNumericFilterView(String column, String alias, Table whichTable, double low, double high) throws SQLException, RemoteException{

        FilterPanel fp = startFilterBy(column);
        FilterController.setAutoCommit(false);

        //create and apply filter to each subquery
        for (FilterPanelSub fps : fp.getFilterPanelSubs()) {
            FilterView view = new NumericFilterView(getTableName(whichTable), column, fps.getId(), alias, false, whichTable);
            fps.addNewSubItem(view, column);
            ((NumericFilterView)view).applyFilter(low, high);
        }

        fp.refreshSubPanels();
        
        FilterController.commit(alias, FilterController.FilterAction.ADDED);
        FilterController.setAutoCommit(true);
    }

    public static void createAndApplyStringListFilterView(String column, String alias, Table whichTable, List<String> values) throws SQLException, RemoteException {

        FilterPanel fp = startFilterBy(column);
        FilterController.setAutoCommit(false);

        //create and apply filter to each subquery
        for (FilterPanelSub fps : fp.getFilterPanelSubs()) {
            FilterView view = new StringListFilterView(getTableName(whichTable), column, fps.getId(), alias, whichTable);
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

    public static void loadFilterView(FilterState state, FilterPanelSub fps) throws SQLException, RemoteException{
        switch(state.getType()) {
            case NUMERIC:
                fps.addNewSubItem(new NumericFilterView(state, fps.getId()), state.getId());
                break;
            case STRING:
                fps.addNewSubItem(new StringListFilterView(state, fps.getId()), state.getId());
                break;
            case BOOLEAN:
                fps.addNewSubItem(new BooleanFilterView(state, fps.getId()), state.getId());
                break;
            case GENELIST:
                fps.addNewSubItem(new GeneListFilterView(state, fps.getId()), state.getId());
                break;
            case COHORT:
                fps.addNewSubItem(new CohortFilterView(state, fps.getId()), state.getId());
                break;
            case GENERIC:
                fps.addNewSubItem(new GenericFixedFilterView(state, fps.getId()), state.getId());
                break;
            case TAG:
                fps.addNewSubItem(new TagFilterView(state, fps.getId()), state.getId());
                break;
            case STARRED:
                fps.addNewSubItem(new StarredFilterView(state, fps.getId()), state.getId());
                break;
            case PLUGIN:
                PluginController pc = PluginController.getInstance();
                for (PluginDescriptor desc: pc.getDescriptors()) {
                    final MedSavantPlugin p = pc.getPlugin(desc.getID());
                    if (p instanceof MedSavantFilterPlugin && ((MedSavantFilterPlugin)p).getTitle().equals(state.getName())) {
                        FilterView view = PluginFilterView.getFilterView((MedSavantFilterPlugin)p, fps.getId());
                        ((MedSavantFilterPlugin)p).loadState(state.getValues(), fps.getId());
                        fps.addNewSubItem(view, state.getId());
                    }
                }
                break;
        }
    }


    /*
     * Common functionality for all created filters
     */
    private static FilterPanel startFilterBy(String column) {

        //get filter panel
        FilterPanel fp = getFilterPanel();

        //remove filters by id
        removeFiltersById(fp, column);

        return fp;
    }

    private static FilterPanel getFilterPanel() {
        /*FilterPanel fp = GeneticsFilterPage.getInstance().getFilterPanel();
        if (fp == null) {
            GeneticsFilterPage.getInstance().getView(true);
            GeneticsFilterPage.getInstance().setUpdateRequired(false);
            fp = GeneticsFilterPage.getInstance().getFilterPanel();
        }*/
        FilterPanel fp = GeneticsFilterPage.getFilterPanel();
        
        //deal with case where no sub panels
        if (fp.getFilterPanelSubs().isEmpty()) {
            fp.createNewSubPanel();
        }

        return fp;
    }

    private static void removeFiltersById(FilterPanel fp, String id) {
        for (FilterPanelSub fps : fp.getFilterPanelSubs()) {
            fps.removeFiltersById(id);
        }
    }

    public static String getTableName(Table whichTable) {
        if (whichTable == Table.VARIANT) {
            return ProjectController.getInstance().getCurrentVariantTableName();
        } else {
            return ProjectController.getInstance().getCurrentPatientTableName();
        }
    }

    public static TableSchema getTableSchema(Table whichTable) {
        if (whichTable == Table.VARIANT) {
            return ProjectController.getInstance().getCurrentVariantTableSchema();
        } else {
            return ProjectController.getInstance().getCurrentPatientTableSchema();
        }
    }

    public static void clearFilterSets() {
        FilterController.removeAllFilters();
        FilterPanel fp = getFilterPanel();
        fp.clearAll();
    }

}
