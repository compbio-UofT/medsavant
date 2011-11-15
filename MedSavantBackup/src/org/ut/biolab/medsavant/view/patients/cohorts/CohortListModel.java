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

package org.ut.biolab.medsavant.view.patients.cohorts;

import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.db.util.query.CohortQueryUtil;
import org.ut.biolab.medsavant.view.patients.DetailedListModel;


/**
 *
 * @author mfiume
 */
public class CohortListModel implements DetailedListModel {

    public List<Object[]> getList(int limit) throws Exception {

        List<Object[]> table = new ArrayList<Object[]>();

        List<Cohort> cohorts = CohortQueryUtil.getCohorts(ProjectController.getInstance().getCurrentProjectId());
        for(Cohort c : cohorts){
            table.add(new Object[] { c });
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

    public static String getIndividualID(Object[] r) {
        return (String)r[0];
    }
}
