/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.view.genetics.GeneticsFilterPage;

/**
 *
 * @author Andrew
 */
public class FilterUtils {
           
    public enum Table {PATIENT, VARIANT};
    
    public static void createAndApplyNumericFilter(String column, String alias, Table whichTable, double low, double high) throws SQLException{
        
        FilterPanel fp = startFilterBy(column);

        //create and apply filter to each subquery
        for(FilterPanelSub fps : fp.getFilterPanelSubs()){
            FilterView view = new NumericFilterView(getTableName(whichTable), column, fps.getId(), alias, false, whichTable);
            fps.addNewSubItem(view, column);
            ((NumericFilterView)view).applyFilter(low, high);
        }

        fp.refreshSubPanels();
    }
    
    public static void createAndApplyStringListFilter(String column, String alias, Table whichTable, List<String> values) throws SQLException {
        
        FilterPanel fp = startFilterBy(column);

        //create and apply filter to each subquery
        for(FilterPanelSub fps : fp.getFilterPanelSubs()){
            FilterView view = new StringListFilterView(getTableName(whichTable), column, fps.getId(), alias, whichTable);
            fps.addNewSubItem(view, column);
            ((StringListFilterView)view).applyFilter(values);
        }

        fp.refreshSubPanels();
    }
    
    public static void removeFiltersById(String id){
        removeFiltersById(getFilterPanel(), id);
    }
    
    
    /*
     * Common functionality for all created filters
     */
    private static FilterPanel startFilterBy(String column){
        
        //get filter panel
        FilterPanel fp = getFilterPanel();
              
        //remove filters by id
        removeFiltersById(fp, column);
           
        //deal with case where no sub panels
        if(fp.getFilterPanelSubs().isEmpty()){
            fp.createNewSubPanel();
        }
        
        return fp;
    }
    
    private static FilterPanel getFilterPanel(){
        FilterPanel fp = GeneticsFilterPage.getInstance().getFilterPanel();
        if(fp == null){
            GeneticsFilterPage.getInstance().getView(true);
            GeneticsFilterPage.getInstance().setUpdateRequired(false);
            fp = GeneticsFilterPage.getInstance().getFilterPanel();;
        }
        return fp;
    }
    
    private static void removeFiltersById(FilterPanel fp, String id){
        for(FilterPanelSub fps : fp.getFilterPanelSubs()){
            fps.removeFiltersById(id);
        }
    }
    
    private static String getTableName(Table whichTable){
        if(whichTable == Table.VARIANT){
            return ProjectController.getInstance().getCurrentTableName();
        } else {
            return ProjectController.getInstance().getCurrentPatientTableName();
        }
    }
    
}
