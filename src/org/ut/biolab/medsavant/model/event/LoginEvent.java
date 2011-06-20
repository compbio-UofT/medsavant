/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model.event;

/**
 *
 * @author mfiume
 */
public class LoginEvent {
    
    private final boolean isLoggedIn;
    private final String username;

    public LoginEvent(boolean isLoggedIn) {
        this(isLoggedIn,null);
    }

    public LoginEvent(boolean isLoggedIn, String username) {
        System.out.println("Creating " + isLoggedIn + " login event");
        this.isLoggedIn = isLoggedIn;
        this.username = username;
    }

    public boolean getLoggedIn() {
        return isLoggedIn;
    }

    public String getUserName() {
        return username;
    }
}
