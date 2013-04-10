/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.util;

/**
 *
 * @author jim
 */
public class CustomFieldConstraint {
    
    private boolean required;     
    private int maxlength;
    private String errorMessage;
    
    /**
     * 
     * @param required  Whether a missing value is acceptable (true) or not (false).
     * @param maxlength The maximum number of characters for the input.
     * @param errorMessage An optional error message that can be stored for later retrieval with getErrorMessage()
     */
    public CustomFieldConstraint(boolean required, int maxlength, String errorMessage){
        this(required, maxlength);
        this.errorMessage = errorMessage;
    }
    
    /**
     * 
     * @param required  Whether a missing value is acceptable (true) or not (false).
     * @param maxlength The maximum number of characters for the input.
     */
    public CustomFieldConstraint(boolean required, int maxlength){
        this.required = required;
        this.maxlength = maxlength;
    }
       
    /**
     * 
     * @return The errorMessage that was supplied when the constraint was constructed, 
     * or null if no message was given.
     */
    public String getErrorMessage(){
        return errorMessage;
    }
    
    
    /**
     * 
     * @return Whether or not a missing value is acceptable input.
     */
    public boolean isRequired(){
        return required;
    }
      
    /**     
     * @param required true if a missing value is acceptable, false otherwise.
     */
    public void setRequired(boolean required){
        this.required = required;
    }                    
       
    /**
     * Validates the given input by checking if it is missing or if it contains
     * too many characters.
     * 
     * @param val The input string to validate.  Null, zero-length, or white-space only strings are considered
     * missing.
     * @return true if valid, false otherwise.
     */
    public boolean isValid(String val){     
        
        if((val == null || val.trim().isEmpty())){            
            return !required;            
        }
        
        if(val.length() > maxlength){
            //val is too long            
            return false;
        }    
        
        return true;
    }
    
}
