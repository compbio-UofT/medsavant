package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.Patient;

import java.util.List;

/**
 * Adapter class for mapping Solr documents to Patient objects.
 */
public class SearcheablePatient {

    private Patient patient;

    public SearcheablePatient() {};

    public SearcheablePatient(Patient patient) {
        this.patient = patient;
    }

    @Field("patient_id")
    public void setPatientId(String patientId) {
        this.patient.setPatientId(patientId);
    }

    @Field("family_id")
    public void setFamilyId(String familyId) {
        this.patient.setFamilyId(familyId);
    }

    @Field("hospital_id")
    public void setHospitalId(String hospitalId) {
        this.patient.setHospitalId(hospitalId);
    }

    @Field("mother_id")
    public void setMotherId(String motherId) {
        this.patient.setMotherId(motherId);
    }

    @Field("father_id")
    public void setFatherId(String fatherId) {
        this.patient.setFamilyId(fatherId);
    }

    @Field("gender")
    public void setGender(String gender) {
        this.patient.setGender(gender);
    }

    @Field("affected")
    public void setAffected(String affected) {
        this.patient.setAffected(affected);
    }

    @Field("dna_ids")
    public void setDnaIds(List<String> dnaIds) {
        this.patient.setDnaIds(dnaIds);
    }

    @Field("phenotypes")
    public void setPhenotypes(List<String> phenotypes) {
        this.patient.setPhenotypes(phenotypes);
    }

    @Field("bam_url")
    public void setBamUrl(String bamUrl) {
        this.patient.setBamUrl(bamUrl);
    }

    @Field("cohort_ids")
    public void setCohortIds(List<String> cohortIds) {
        this.patient.setCohortIds(cohortIds);
    }

    public Patient getPatient() {
        return patient;
    }

    public String getPatientId() {
        return patient.getPatientId();
    }

    public String getFamilyId() {
        return patient.getFamilyId();
    }

    public String getHospitalId() {
        return patient.getHospitalId();
    }

    public String getMotherId() {
        return patient.getMotherId();
    }

    public String getFatherId() {
        return patient.getFatherId();
    }

    public String getGender() {
        return patient.getGender();
    }

    public String getAffected() {
        return patient.getAffected();
    }

    public List<String> getDnaIds() {
        return patient.getDnaIds();
    }

    public List<String> getPhenotypes() {
        return patient.getPhenotypes();
    }

    public String getBamUrl() {
        return patient.getBamUrl();
    }

    public List<String> getCohortIds() {
        return patient.getCohortIds();
    }

}
