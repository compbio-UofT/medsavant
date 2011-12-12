/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.format.PatientFormat;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.view.list.DetailedListModel;

/**
 *
 * @author mfiume
 */
public class IndividualListModel implements DetailedListModel {

    public List<Object[]> getList(int limit) throws Exception {
        List<Object[]> table = PatientQueryUtil.getBasicPatientInfo(ProjectController.getInstance().getCurrentProjectId(), limit);
        return table;
    }

    public List<String> getColumnNames() {
        //TableSchema table = MedSavantDatabase.DefaultpatientTableSchema;
        List<String> result = new ArrayList<String>();
        result.add(PatientFormat.ALIAS_OF_PATIENT_ID);
        result.add(PatientFormat.ALIAS_OF_FAMILY_ID);
        result.add(PatientFormat.ALIAS_OF_HOSPITAL_ID);
        result.add(PatientFormat.ALIAS_OF_IDBIOMOM);
        result.add(PatientFormat.ALIAS_OF_IDBIODAD);
        result.add(PatientFormat.ALIAS_OF_GENDER);
        result.add(PatientFormat.ALIAS_OF_DNA_IDS);
        return result;
    }

    public List<Class> getColumnClasses() {
        List<Class> result = new ArrayList<Class>();
        result.add(Integer.class);
        result.add(String.class);
        result.add(String.class);
        result.add(String.class);
        result.add(String.class);
        result.add(Integer.class);
        result.add(String.class);
        return result;
    }

    public List<Integer> getHiddenColumns() {
        List<Integer> hidden = new ArrayList<Integer>();
        hidden.add(0);
        hidden.add(1);
        hidden.add(3);
        hidden.add(4);
        hidden.add(5);
        hidden.add(6);
        return hidden;
    }
    
    public static String getIndividualID(Vector r) {
        return (String) r.get(0);
    }
}
