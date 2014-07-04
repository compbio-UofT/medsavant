/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

/**
 *
 * @author jim
 */
public class MedSavantSecurityException extends Exception {

    public MedSavantSecurityException(String message) {
        super(message);
    }

    public MedSavantSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public MedSavantSecurityException(Throwable cause) {
        super(cause);
    }

    public MedSavantSecurityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
