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

/**
 *
 * @author mfiume
 */
public class Patient {

    private String hospitalID;
    private String motherHospitalID;
    private String fatherHospitalID;
    private String familyID;
    private String dnaID;
    private String sex;
    private boolean affected;
    private String bamURL;
    private String phenotypes;
    
    public Patient(String hospitalID) {
        this.hospitalID = hospitalID;
    }

    public String getHospitalID() {
        return hospitalID;
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

    public void setAffected(boolean affected) {
        this.affected = affected;
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
    
}
