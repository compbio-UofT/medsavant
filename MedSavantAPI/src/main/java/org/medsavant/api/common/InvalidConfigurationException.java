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
public class InvalidConfigurationException extends Exception {

    /**
     * Creates a new instance of <code>InvalidConfigurationException</code>
     * without detail message.
     */
    public InvalidConfigurationException() {
    }

    /**
     * Constructs an instance of <code>InvalidConfigurationException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidConfigurationException(String msg) {
        super(msg);
    }
}
