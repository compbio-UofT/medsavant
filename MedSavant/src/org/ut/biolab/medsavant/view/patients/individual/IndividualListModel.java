/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.view.patients.individual;

import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.view.patients.DetailedListModel;

/**
 *
 * @author mfiume
 */
public class IndividualListModel implements DetailedListModel {

    public List<Object[]> getList(int limit) throws Exception {
        return PatientQueryUtil.getBasicPatientInfo(ProjectController.getInstance().getCurrentProjectId(), limit);
    }

    public List<String> getColumnNames() {
        TableSchema table = MedSavantDatabase.DefaultpatientTableSchema;
        List<String> result = new ArrayList<String>();
        result.add(table.getFieldAlias(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID));
        result.add(table.getFieldAlias(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID));
        result.add(table.getFieldAlias(DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID));
        result.add(table.getFieldAlias(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID));
        result.add(table.getFieldAlias(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS));
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
    
    public static String getIndividualID(Object[] r) {
        return (String)r[0];
    }
}
