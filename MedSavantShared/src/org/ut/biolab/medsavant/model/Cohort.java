/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.model;

import java.io.Serializable;

/**
 *
 * @author Andrew
 */
public class Cohort implements Serializable {
    
    private int id;
    private String name;
    
    public Cohort(int id, String name){
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
