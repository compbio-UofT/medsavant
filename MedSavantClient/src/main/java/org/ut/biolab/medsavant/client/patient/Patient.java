/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.patient;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author mfiume
 */
public class Patient {

    private int ID;
    private String hospitalID;
    private String motherHospitalID;
    private String fatherHospitalID;
    private String familyID;
    private String dnaID;
    private String sex;
    private Boolean affected;
    private String bamURL;
    private String phenotypes;

    public Patient(String hospitalID) {
        this.hospitalID = hospitalID;
    }
    

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getHospitalID() {
        return hospitalID;
    }

    
    void setHospitalID(String hospitalID) {
        this.hospitalID = hospitalID;
    }
    
    public String getMotherHospitalID() {
        return motherHospitalID;
    }

    public void setMotherHospitalID(String motherHospitalID) {
        this.motherHospitalID = motherHospitalID;
    }

    public String getFatherHospitalID() {
        return fatherHospitalID;
    }

    public void setFatherHospitalID(String fatherHospitalID) {
        this.fatherHospitalID = fatherHospitalID;
    }

    public String getFamilyID() {
        return familyID;
    }

    public void setFamilyID(String familyID) {
        this.familyID = familyID;
    }

    public String getDnaID() {
        return dnaID;
    }

    public void setDnaID(String dnaID) {
        this.dnaID = dnaID;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public boolean isAffected() {
        return affected;
    }

    public void setAffected(Boolean affected) {
        this.affected = affected == null ? false : affected;
    }

    public String getBamURL() {
        return bamURL;
    }

    public void setBamURL(String bamURL) {
        this.bamURL = bamURL;
    }

    public String getPhenotypes() {
        return phenotypes;
    }

    public void setPhenotypes(String phenotypes) {
        this.phenotypes = phenotypes;
    }

    @Override
    public String toString() {
        return "Patient{" + "hospitalID=" + hospitalID + ", motherHospitalID=" + motherHospitalID + ", fatherHospitalID=" + fatherHospitalID + ", familyID=" + familyID + ", dnaID=" + dnaID + ", sex=" + sex + ", affected=" + affected + ", bamURL=" + bamURL + ", phenotypes=" + phenotypes + '}';
    }

    void saveToDatabase() {
        try {
            CustomField[] fieldArray = MedSavantClient.PatientManager.getPatientFields(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID());

            int idIndex = BasicPatientColumns.INDEX_OF_PATIENT_ID;
            int hospitalIDIndex = BasicPatientColumns.INDEX_OF_HOSPITAL_ID;
            int biomomIndex = BasicPatientColumns.INDEX_OF_IDBIOMOM;
            int biodadIndex = BasicPatientColumns.INDEX_OF_IDBIODAD;
            int familyIDIndex = BasicPatientColumns.INDEX_OF_FAMILY_ID;
            int bamURLIndex = BasicPatientColumns.INDEX_OF_BAM_URL;
            int dnaIDIndex = BasicPatientColumns.INDEX_OF_DNA_IDS;
            int phenotypesIndex = BasicPatientColumns.INDEX_OF_PHENOTYPES;
            int genderIndex = BasicPatientColumns.INDEX_OF_GENDER;
            int affectedIndex = BasicPatientColumns.INDEX_OF_AFFECTED;

            List<CustomField> fields = new ArrayList<CustomField>();
            List<String> values = new ArrayList<String>();

            fields.add(fieldArray[hospitalIDIndex]);
            values.add(this.getHospitalID());

            fields.add(fieldArray[biomomIndex]);
            values.add(this.getMotherHospitalID());

            fields.add(fieldArray[biodadIndex]);
            values.add(this.getFatherHospitalID());

            fields.add(fieldArray[familyIDIndex]);
            values.add(this.getFamilyID());

            fields.add(fieldArray[bamURLIndex]);
            values.add(this.getBamURL());

            fields.add(fieldArray[dnaIDIndex]);
            values.add(this.getDnaID());

            fields.add(fieldArray[phenotypesIndex]);
            values.add(this.getPhenotypes());

            fields.add(fieldArray[genderIndex]);
            values.add(ClientMiscUtils.stringToGender(this.getSex()) + "");
            
            fields.add(fieldArray[affectedIndex]);
            values.add((this.isAffected() ? 1 : 0) + "");

            MedSavantClient.PatientManager.updatePatient(
                    LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    this.getID(), fields, values);

        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
        } catch (Exception ex) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
