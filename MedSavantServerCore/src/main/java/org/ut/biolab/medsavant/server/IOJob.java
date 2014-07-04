/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.server;

import java.io.IOException;
/**
 *
 * @author jim
 */
public abstract class IOJob {
    private boolean x = true;
    private final String name;
    
    public IOJob(String name){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
    
    protected void finish() throws IOException{
        
    }
    
    protected boolean continueIO() throws IOException{        
        if(x){
            x = false;
            return true;
        }
        return false;
    }    
    
    protected abstract void doIO() throws IOException; 
    
    
}
