/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

/**
 *
 * @author jim
 */
public class VCFPreProcessorException extends Exception {
    public VCFPreProcessorException(String message) {
        super(message);
    }

    public VCFPreProcessorException(String message, Throwable cause) {
        super(message, cause);
    }

    public VCFPreProcessorException(Throwable cause) {
        super(cause);
    }

    public VCFPreProcessorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
