/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

/**
 * Exception for reporting database errors, without necessarily exposing implementation
 * detail. 
 */
public class MedSavantDatabaseException extends Exception{

    public MedSavantDatabaseException() {
    }

    public MedSavantDatabaseException(String message) {
        super(message);
    }

    public MedSavantDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public MedSavantDatabaseException(Throwable cause) {
        super(cause);
    }

    public MedSavantDatabaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
