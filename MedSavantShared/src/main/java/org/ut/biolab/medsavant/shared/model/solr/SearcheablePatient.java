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
import org.ut.biolab.medsavant.shared.model.Patient;

import java.util.List;
import java.util.UUID;

/**
 * Adapter class for mapping Solr documents to Patient objects.
 */
public class SearcheablePatient extends SearcheableMedsavantEntity {

    private Patient patient;

    public SearcheablePatient() {
        this.patient = new Patient();
    };

    public SearcheablePatient(Patient patient) {
        this.patient = patient;
    }

    @Field("project_id")
    public void setProjectId(int projectId) {
        this.patient.setProjectId(projectId);
    }

    @Field("patient_id")
    public void setPatientId(int patientId) {
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

    @Field("idbiomom")
    public void setMotherId(String motherId) {
        this.patient.setMotherId(motherId);
    }

    @Field("idbiodad")
    public void setFatherId(String fatherId) {
        this.patient.setFatherId(fatherId);
    }

    @Field("gender")
    public void setGender(int gender) {
        this.patient.setGender(gender);
    }

    @Field("affected")
    public void setAffected(int affected) {
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
    public void setBamUrl(List<String> bamUrl) {
        this.patient.setBamUrl(bamUrl);
    }

    @Field("cohort_ids")
    public void setCohortIds(List<Integer> cohortIds) {
        this.patient.setCohortIds(cohortIds);
    }

    public Patient getPatient() {
        return patient;
    }

    public int getPatientId() {
        return patient.getPatientId();
    }

    public int getProjectId() {
        return patient.getProjectId();
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

    public int getGender() {
        return patient.getGender();
    }

    public int getAffected() {
        return patient.getAffected();
    }

    public List<String> getDnaIds() {
        return patient.getDnaIds();
    }

    public List<String> getPhenotypes() {
        return patient.getPhenotypes();
    }

    public List<String> getBamUrl() {
        return patient.getBamUrl();
    }

    public List<Integer> getCohortIds() {
        return patient.getCohortIds();
    }

    @Override
    public void setUUID(String uuid) {
        patient.setUuid(UUID.fromString(uuid));
    }

    @Override
    public UUID getUUID() {
        return patient.getUuid();
    }
}
