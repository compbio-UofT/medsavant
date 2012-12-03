/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class SimplePatient implements Serializable {

    private int id;
    private String hid;
    private List<String> dnaIds;

    public SimplePatient(int id, String hid, List<String> dnaIds){
        this.id = id;
        this.hid = hid;
        this.dnaIds = dnaIds;
    }

    public int getId(){
        return this.id;
    }

    public String getHospitalId(){
        return this.hid;
    }

    public List<String> getDnaIds(){
        return this.dnaIds;
    }

    @Override
    public String toString(){
        return hid;
    }
}
