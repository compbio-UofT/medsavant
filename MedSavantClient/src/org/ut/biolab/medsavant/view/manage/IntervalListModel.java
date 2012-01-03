/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.db.model.RegionSet;
import org.ut.biolab.medsavant.view.list.DetailedListModel;

/**
 *
 * @author mfiume
 */
public class IntervalListModel implements DetailedListModel {

    public List<Object[]> getList(int limit) throws Exception {

        List<Object[]> table = new ArrayList<Object[]>();
        List<RegionSet> regions = MedSavantClient.RegionQueryUtilAdapter.getRegionSets(LoginController.sessionId);
        
        for (RegionSet s : regions) {
            Object[] v = new Object[] { s };
            table.add(v);
        }

        return table;
    }

    public List<String> getColumnNames() {
        List<String> result = new ArrayList<String>();
        result.add("Region List");
        return result;
    }

    public List<Class> getColumnClasses() {
        List<Class> result = new ArrayList<Class>();
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
