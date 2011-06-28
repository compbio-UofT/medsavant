/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.cohorts;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.db.table.CohortViewTableSchema;
import org.ut.biolab.medsavant.view.patients.DetailedListModel;

/**
 *
 * @author mfiume
 */
public class CohortListModel implements DetailedListModel {

    public List<Vector> getList() throws Exception {

        List<Vector> table = new ArrayList<Vector>();

        List<String> dbresults = QueryUtil.getDistinctCohortNames();

        for (String s : dbresults) {
            Vector v = new Vector();
            v.add(s);
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
        result.add(String.class);
        return result;
    }

    public List<Boolean> getColumnVisibility() {
        List<Boolean> result = new ArrayList<Boolean>();
        result.add(true);
        return result;
    }

    public static String getIndividualID(Vector r) {
        return (String) r.get(0);
    }
}
