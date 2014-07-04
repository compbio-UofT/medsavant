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
public class InvalidAnnotationPipelineException extends Exception{

    public InvalidAnnotationPipelineException(String message) {
        super(message);
    }

    public InvalidAnnotationPipelineException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAnnotationPipelineException(Throwable cause) {
        super(cause);
    }

    public InvalidAnnotationPipelineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
