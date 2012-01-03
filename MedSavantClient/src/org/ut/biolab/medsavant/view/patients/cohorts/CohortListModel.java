/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.cohorts;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.view.list.DetailedListModel;

/**
 *
 * @author mfiume
 */
public class CohortListModel implements DetailedListModel {

    public List<Object[]> getList(int limit) throws Exception {

        List<Object[]> table = new ArrayList<Object[]>();

        /*List<String> dbresults = new ArrayList<String>();//TODO QueryUtil.getDistinctCohortNames(limit);        
        for (String s : dbresults) {
            Vector v = new Vector();
            v.add(s);
            table.add(v);
        }*/
        
        List<Cohort> cohorts = MedSavantClient.CohortQueryUtilAdapter.getCohorts(
                LoginController.sessionId, 
                ProjectController.getInstance().getCurrentProjectId());
        for(Cohort c : cohorts){
            Object[] v = new Object[] { c };
            table.add(v);
        }

        return table;
    }

    public List<String> getColumnNames() {
        List<String> result = new ArrayList<String>();
        result.add("Cohort");
        return result;
    }

    public List<Class> getColumnClasses() {
        List<Class> result = new ArrayList<Class>();
        result.add(Cohort.class);
        return result;
    }
    
    public List<Integer> getHiddenColumns() {
        return new ArrayList<Integer>();
    }

    public static String getIndividualID(Vector r) {
        return (String) r.get(0);
    }
}
