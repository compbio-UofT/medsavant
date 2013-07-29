package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.Cohort;

import java.util.List;

/**
 * Adapter class for mapping Solr documents to Cohort objects.
 */
public class SearcheableCohort {

    private Cohort cohort;

    private int id;
    private String name;
    private List<Integer> patientIds;


    @Field("id")
    public void setId(int id) {
        this.cohort.setId(id);
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
}
