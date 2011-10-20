/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil.LogType;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;

/**
 *
 * @author mfiume
 */
public class LoginController {

    private static String username;
    private static String password;
    private static boolean isAdmin; 

    public static String getPassword() {
        return password;
    }

    public static String getUsername() {
        return username;
    }
    
    public static boolean isAdmin() {
        return isAdmin;
    }

    private static boolean loggedIn = false;

    private synchronized static void setLoggedIn(boolean loggedIn) {
        if (!loggedIn) {
            ConnectionController.disconnectAll();
            if (!SettingsController.getInstance().getRememberPassword()) {
                password = "";
            }
        }
        LoginController.loggedIn = loggedIn;

        if (loggedIn) {
            ServerLogQueryUtil.addLog(LoginController.username, LogType.INFO, "Logged in");
            fireLoginEvent(new LoginEvent(LoginEvent.EventType.LOGGED_IN));
        } else {
            ServerLogQueryUtil.addLog(LoginController.username, LogType.INFO, "Logged out");
            fireLoginEvent(new LoginEvent(LoginEvent.EventType.LOGGED_OUT));
        }
    }

    private static ArrayList<LoginListener> loginListeners = new ArrayList<LoginListener>();

    public static boolean isLoggedIn() { return loggedIn; }
    
    public static synchronized void login(String un, String pw) {
        
        username = un;
        password = pw;
        
        try {
            isAdmin = username.equals("root") || org.ut.biolab.medsavant.db.util.query.UserQueryUtil.isUserAdmin(username);
        } catch (SQLException ex) {
            setLoggedIn(false);
            setLoginException(ex);
            return;
        }
        
        SettingsController.getInstance().setUsername(un);
        if (SettingsController.getInstance().getRememberPassword()) {
            SettingsController.getInstance().setPassword(pw);
        }

        try {
            setLoggedIn(null != ConnectionController.connect());
        } catch (Exception ex) {
            setLoginException(ex);
        }
    }

    public static void addLoginListener(LoginListener l) {
        loginListeners.add(l);
    }

     public static void removeLoginListener(LoginListener l) {
        loginListeners.remove(l);
    }

    private static void fireLoginEvent(LoginEvent evt) {
        for(int i = loginListeners.size()-1; i >= 0; i--){
            loginListeners.get(i).loginEvent(evt);
        }
    }

    public static void logout() {
        setLoggedIn(false);
    }

    private static void setLoginException(Exception ex) {
        fireLoginEvent(new LoginEvent(LoginEvent.EventType.LOGIN_FAILED,ex));
    }

   
}
