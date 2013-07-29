package org.ut.biolab.medsavant.shared.model;

import java.util.List;

/**
 * Also called individual in the GUI
 */
public class Patient {

    private String patientId;
    private String familyId;
    private String hospitalId;
    private String motherId;
    private String fatherId;

    private String gender;
    private String affected;
    private List<String> dnaIds;
    private List<String> phenotypes;
    private String bamUrl;

    private List<String> cohortIds;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAffected() {
        return affected;
    }

    public void setAffected(String affected) {
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

    public String getBamUrl() {
        return bamUrl;
    }

    public void setBamUrl(String bamUrl) {
        this.bamUrl = bamUrl;
    }

    public List<String> getCohortIds() {
        return cohortIds;
    }

    public void setCohortIds(List<String> cohortIds) {
        this.cohortIds = cohortIds;
    }
}
