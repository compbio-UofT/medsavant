/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

/**
 * Indicates an error in processing a variant window.
 * @see VariantWindow
 * @see VariantDispatcher 
 */
public class VariantWindowException extends Exception{
    public VariantWindowException(String message) {
        super(message);
    }

    public VariantWindowException(String message, Throwable cause) {
        super(message, cause);
    }

    public VariantWindowException(Throwable cause) {
        super(cause);
    }

    public VariantWindowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
