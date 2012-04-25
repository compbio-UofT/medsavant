/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db;

/**
 *
 * @author mfiume
 */
public class NonFatalDatabaseException extends Exception {
    
    private final ExceptionType exceptionType;

    public ExceptionType getExceptionType() {
        return exceptionType;
    }
    private final String username;


    public NonFatalDatabaseException(ExceptionType exceptionType, String username) {
        this.exceptionType = exceptionType;
        this.username = username;
    }

    public enum ExceptionType { TYPE_ACCESS_DENIED, TYPE_DB_CONNECTION_FAILURE, TYPE_UNKNOWN};

    public String getUsername() {
        return username;
    }


}
