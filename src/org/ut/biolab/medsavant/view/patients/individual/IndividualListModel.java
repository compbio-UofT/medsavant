/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.table.PatientTable;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.olddb.QueryUtil;
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
        
        //List<Vector> table = QueryUtil.getDistinctBasicPatientInfo(limit);
        List<Vector> table = PatientQueryUtil.getBasicPatientInfo(ProjectController.getInstance().getCurrentProjectId(), limit);
        
        return table;
    }

    public List<String> getColumnNames() {
        List<String> result = new ArrayList<String>();
        result.add(PatientTable.ALIAS_ID);
        result.add(PatientTable.ALIAS_FAMILYID);
        result.add(PatientTable.ALIAS_PEDIGREEID);
        result.add(PatientTable.ALIAS_HOSPITALID);
        result.add(PatientTable.ALIAS_DNAIDS);
        return result;
    }

    public List<Class> getColumnClasses() {
        List<Class> result = new ArrayList<Class>();
        result.add(Integer.class);
        result.add(String.class);
        result.add(String.class);
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
