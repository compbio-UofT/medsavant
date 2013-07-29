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
public class Cohort implements Serializable, Comparable {

    private int id;
    private String name;
    private List<Integer> patientIds;

    public Cohort(int id, String name){
        this.id = id;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getPatientIds() {
        return patientIds;
    }

    public void setPatientIds(List<Integer> patientIds) {
        this.patientIds = patientIds;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Object t) {
        return this.getName().compareTo(t.toString());
    }

}
