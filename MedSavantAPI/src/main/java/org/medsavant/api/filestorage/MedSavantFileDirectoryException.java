/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.filestorage;

/**
 *
 * @author jim
 */
public class MedSavantFileDirectoryException extends Exception{

    public MedSavantFileDirectoryException(String message) {
        super(message);
    }

    public MedSavantFileDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public MedSavantFileDirectoryException(Throwable cause) {
        super(cause);
    }

    public MedSavantFileDirectoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
