/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.model;

/**
 *
 * @author Andrew
 */
public class Reference {
    
    private int id;
    private String name;
    
    public Reference(int id, String name){
        this.id = id;
        this.name = name;
    }
    
    public int getId(){
        return id;
    }
    
    public String getName(){
        return name;
    }
    
}
