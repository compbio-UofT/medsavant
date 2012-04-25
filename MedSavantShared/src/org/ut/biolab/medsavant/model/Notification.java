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
public class Notification implements Serializable {
    
    public enum Type {PUBLISH};
    
    private Type type;
    private String message;
    private Object data;
    
    public Notification(Type type, String message, Object data){
        this.type = type;
        this.message = message;
        this.data = data;
    }
    
    public Type getType(){
        return type;
    }
    
    public String getMessage(){
        return message;
    }
    
    //TODO: better way to pass this information...
    public Object getData() {
        return data;
    }

    
}
