/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.view.patients.DetailedListModel;

/**
 *
 * @author mfiume
 */
public class IndividualListModel implements DetailedListModel {

    public List<Vector> getList(int limit) throws Exception {

        /*List<Vector> table = new ArrayList<Vector>();

        List<String> dbresults = QueryUtil.getDistinctPatientIDs();

        for (String s : dbresults) {
            Vector v = new Vector();
            v.add(s);
            table.add(v);
        }*/
        
        List<Vector> table = QueryUtil.getDistinctBasicPatientInfo(limit);

        return table;
    }

    public List<String> getColumnNames() {
        List<String> result = new ArrayList<String>();
        result.add("Patient ID");
        result.add("DNA ID");
        result.add("Family ID");
        return result;
    }

    public List<Class> getColumnClasses() {
        List<Class> result = new ArrayList<Class>();
        result.add(String.class);
        result.add(String.class);
        result.add(String.class);
        return result;
    }

    public List<Integer> getHiddenColumns() {
        return new ArrayList<Integer>();
    }
    
    public static String getIndividualID(Vector r) {
        return (String) r.get(0);
    }
}
