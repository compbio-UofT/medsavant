/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.model;

/**
 *
 * @author Andrew
 */
public class SimplePatient {
    
    private int id;
    private String hid;
    
    public SimplePatient(int id, String hid){
        this.id = id;
        this.hid = hid;
    }
    
    public int getId(){
        return this.id;
    }
    
    @Override
    public String toString(){
        return hid;
    }
}
