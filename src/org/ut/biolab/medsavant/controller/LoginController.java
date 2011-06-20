/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import medsavant.db.ConnectionController;
import medsavant.exception.AccessDeniedDatabaseException;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;

/**
 *
 * @author mfiume
 */
public class LoginController {


    private static String username;
    private static String password;

    public static String getPassword() {
        return password;
    }

    public static String getUsername() {
        return username;
    }

    private static boolean loggedIn = false;

    private static void setLoggedIn(boolean loggedIn) {
        LoginController.loggedIn = loggedIn;
        fireLoginEvent(new LoginEvent(loggedIn,username));
    }

    private static ArrayList<LoginListener> loginListeners = new ArrayList<LoginListener>();

    public static boolean isLoggedIn() { return loggedIn; }

    public static boolean login(String un, String pw) {
        
        System.out.println("Logging in with un: " + un + " pass: " + pw);

        username = un;
        password = pw;

        try {
            setLoggedIn(null != ConnectionController.connect());
        } catch (AccessDeniedDatabaseException ex) {
            setLoggedIn(false);
        }

        return loggedIn;
    }

    public static void addLoginListener(LoginListener l) {
        loginListeners.add(l);
    }

     public static void removeLoginListener(LoginListener l) {
        loginListeners.remove(l);
    }

    private static void fireLoginEvent(LoginEvent evt) {
        for (LoginListener l : loginListeners) {
            l.loginEvent(evt);
        }
    }

    public static void logout() {
        setLoggedIn(false);
    }

}
