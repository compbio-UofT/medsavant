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
package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.Cohort;

import java.util.List;

/**
 * Adapter class for mapping Solr documents to Cohort objects.
 */
public class SearcheableCohort {

    private Cohort cohort;

    public SearcheableCohort() {
        this.cohort = new Cohort();
    }

    public SearcheableCohort(Cohort cohort) {
        this.cohort = cohort;
    }

    @Field("id")
    public void setId(int id) {
        this.cohort.setId(id);
    }

    @Field("project_id")
    public void setProjectId(int projectId) {
        this.cohort.setProjectId(projectId);
    }

    @Field("name")
    public void setName(String name) {
        this.cohort.setName(name);
    }

    @Field("patient_ids")
    public void setPatientIds(List<Integer> patientIds) {
        this.cohort.setPatientIds(patientIds);
    }

    public int getId() {
        return cohort.getId();
    }

    public String getName() {
        return cohort.getName();
    }

    public List<Integer> getPatientIds() {
        return cohort.getPatientIds();
    }

    public Cohort getCohort() {
        return cohort;
    }

    public void setCohort(Cohort cohort) {
        this.cohort = cohort;
    }

    public int getProjectId() {
        return this.cohort.getProjectId();
    }

}
