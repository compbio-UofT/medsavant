package org.ut.biolab.medsavant.shared.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Also called individual in the GUI
 */
public class Patient extends MedsavantEntity {

    private int patientId;
    private String familyId;
    private String hospitalId;
    private String motherId;
    private String fatherId;
    private int projectId;

    private int gender;
    private int affected;
    private List<String> dnaIds;
    private List<String> phenotypes;
    private List<String> bamUrl;

    private List<Integer> cohortIds;

    public Patient() {
        this.cohortIds = new ArrayList<Integer>();
    }

    public SimplePatient getSimplePatient() {
        return new SimplePatient(patientId, hospitalId, dnaIds);
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getMotherId() {
        return motherId;
    }

    public void setMotherId(String motherId) {
        this.motherId = motherId;
    }

    public String getFatherId() {
        return fatherId;
    }

    public void setFatherId(String fatherId) {
        this.fatherId = fatherId;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getAffected() {
        return affected;
    }

    public void setAffected(int affected) {
        this.affected = affected;
    }

    public List<String> getDnaIds() {
        return dnaIds;
    }

    public void setDnaIds(List<String> dnaIds) {
        this.dnaIds = dnaIds;
    }

    public List<String> getPhenotypes() {
        return phenotypes;
    }

    public void setPhenotypes(List<String> phenotypes) {
        this.phenotypes = phenotypes;
    }

    public List<String> getBamUrl() {
        return bamUrl;
    }

    public void setBamUrl(List<String> bamUrl) {
        this.bamUrl = bamUrl;
    }

    public List<Integer> getCohortIds() {
        return cohortIds;
    }

    public void setCohortIds(List<Integer> cohortIds) {
        this.cohortIds = cohortIds;
    }

    public Integer removeCohortId(int cohortId) {
        return this.cohortIds.remove(cohortId);
    }
}
