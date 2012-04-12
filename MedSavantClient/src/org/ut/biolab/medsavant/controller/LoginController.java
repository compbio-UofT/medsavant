/*
 *    Copyright 2010-2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.controller;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.db.util.query.api.ServerLogQueryUtilAdapter.LogType;

import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;
import org.ut.biolab.medsavant.server.api.SessionAdapter;
import org.ut.biolab.medsavant.settings.VersionSettings;

/**
 *
 * @author mfiume
 */
public class LoginController {

    private static String username;
    private static String password;
    private static boolean isAdmin;
    private static boolean loggedIn = false;
    private static ArrayList<LoginListener> loginListeners = new ArrayList<LoginListener>();
    private final static Object eventLock = new Object();
    public static String sessionId;
    public static SessionAdapter SessionAdapter;

    private synchronized static void setLoggedIn(final boolean loggedIn) {
        Thread t = new Thread(){
            @Override
            public void run(){
                synchronized(eventLock){
                    LoginController.loggedIn = loggedIn;

                    if (loggedIn) {
                        addLog("Logged in");
                        fireLoginEvent(new LoginEvent(LoginEvent.EventType.LOGGED_IN));
                    } else {
                        addLog("Logged out");
                        unregister();
                        fireLoginEvent(new LoginEvent(LoginEvent.EventType.LOGGED_OUT));
                    }

                    if (!loggedIn) {
                        if (!SettingsController.getInstance().getRememberPassword()) {
                            password = "";
                        }
                    }
                }
            }
        };
        t.start();
    }

    public static void addLog(String message){
        if(!loggedIn) return;
        try {
            MedSavantClient.ServerLogQueryUtilAdapter.addLog(LoginController.sessionId, LoginController.username, LogType.INFO, message);
        } catch (RemoteException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getPassword() {
        return password;
    }

    public static String getUsername() {
        return username;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }


    public static boolean isLoggedIn() { return loggedIn; }

    public static synchronized void login(String un, String pw, String dbname, String serverAddress, String serverPort) {

        //init registry
        try {
            MedSavantClient.initializeRegistry(serverAddress, serverPort);
        } catch (Exception ex) {
            setLoginException(ex);
        }

        //register session
        username = un;
        password = pw;
        try {
            System.out.print("Starting session...");
            System.out.flush();
            sessionId = SessionAdapter.registerNewSession(un, pw, dbname);
            System.out.println("Done");
            System.out.println("My session ID is: " + sessionId);
        } catch (RemoteException ex) {
            System.out.println("");
            setLoginException(ex);
        }

        if(sessionId == null){
            fireLoginEvent(new LoginEvent(LoginEvent.EventType.LOGIN_FAILED));
            return;
        }

        //determine privileges
        isAdmin = false;
        try {
            isAdmin = username.equals("root") || MedSavantClient.UserQueryUtilAdapter.isUserAdmin(sessionId, username);
        } catch (SQLException ignored) {
            // An SQLException is expected here if the user is not an admin, because they
            // will be unable to read the mysql.users table.
        } catch (RemoteException e){

        }

        SettingsController.getInstance().setUsername(un);
        if (SettingsController.getInstance().getRememberPassword()) {
            SettingsController.getInstance().setPassword(pw);
        }

        //test connection
        try {
            if(!SessionAdapter.testConnection(sessionId)){
                fireLoginEvent(new LoginEvent(LoginEvent.EventType.LOGIN_FAILED));
                return;
            }
        } catch (Exception ex) {
            setLoginException(ex);
            return;
        }

        //check db version
        try {
            String databaseVersion = VersionSettings.getDatabaseVersion();
            if(!VersionSettings.isCompatible(VersionSettings.getVersionString(), databaseVersion, false)){
                JOptionPane.showMessageDialog(
                        null,
                        "<html>Your client version (" + VersionSettings.getVersionString() + ") does not match that of the database (" + databaseVersion + ").<br>Visit " + VersionSettings.URL + " to get the correct version.</html>" ,
                        "Version Out-of-Date",
                        JOptionPane.WARNING_MESSAGE);
                fireLoginEvent(new LoginEvent(LoginEvent.EventType.LOGIN_FAILED));
                return;
            }
        } catch (SQLException ex){
            fireLoginEvent(new LoginEvent(LoginEvent.EventType.LOGIN_FAILED));
            return;
        } catch (Exception ex){
            JOptionPane.showMessageDialog(
                    null,
                    "<html>We could not determine compatibility between MedSavant and your database. <br>Please ensure that your versions are compatible before continuing.</html>" ,
                    "Error Comparing Versions",
                    JOptionPane.WARNING_MESSAGE);
            ex.printStackTrace();
        }

        //register for callback
        /*
        try {
            SessionAdapter.registerCallback(sessionId, CallbackController.getInstance());
        } catch (RemoteException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
         */

        setLoggedIn(true);
    }

    public static void addLoginListener(LoginListener l) {
        loginListeners.add(l);
    }

     public static void removeLoginListener(LoginListener l) {
        loginListeners.remove(l);
    }

    private static void fireLoginEvent(LoginEvent evt) {
        for (int i = loginListeners.size()-1; i >= 0; i--){
            loginListeners.get(i).loginEvent(evt);
        }
    }

    public static void logout() {
        setLoggedIn(false);
    }

    private static void setLoginException(Exception ex) {
        fireLoginEvent(new LoginEvent(LoginEvent.EventType.LOGIN_FAILED,ex));
    }

    public static void unregister(){
        if(!loggedIn) return;
        try {
            SessionAdapter.unregisterSession(LoginController.sessionId);
        } catch (Exception ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
