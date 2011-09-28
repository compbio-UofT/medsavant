/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model.event;

import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;

/**
 *
 * @author mfiume
 */
public class LoginEvent {
    
    public enum EventType { LOGGED_IN, LOGGED_OUT, LOGIN_FAILED };
    
    private final boolean isLoggedIn;
    private final EventType type;
    private Exception exception;

    public LoginEvent(EventType type) {
        this.isLoggedIn = type == EventType.LOGGED_IN;
        this.type = type;
        this.exception = null;
    }
    
    public LoginEvent(EventType type, Exception e) {
        this.isLoggedIn = type == EventType.LOGGED_IN;
        this.type = type;
        this.exception = e;
    }

    public Exception getException() {
        return exception;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public EventType getType() {
        return type;
    }
    
    
}
