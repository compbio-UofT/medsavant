/*
 *    Copyright 2010-2012 University of Toronto
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
package org.ut.biolab.medsavant.client.login;

import java.rmi.RemoteException;
import java.sql.SQLException;
import org.apache.commons.httpclient.NameValuePair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.client.project.ProjectChooser;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.project.ProjectWizard;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter.LogType;
import org.ut.biolab.medsavant.client.settings.VersionSettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.Controller;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.savant.analytics.savantanalytics.AnalyticsAgent;

/**
 *
 * @author mfiume
 */
public class LoginController extends Controller<LoginEvent> {

    private static final Log LOG = LogFactory.getLog(LoginController.class);
    private final static Object EVENT_LOCK = new Object();
    private static LoginController instance;
    private String userName;
    private String password;
    private UserLevel level;
    private boolean loggedIn = false;
    private static String sessionId;

    public static String getSessionID() {
        return sessionId;
    }

    public static LoginController getInstance() {
        if (instance == null) {
            instance = new LoginController();
        }
        return instance;
    }
    private String dbname;
    private String serverAddress;

    private synchronized void setLoggedIn(final boolean loggedIn) {

        try {
            AnalyticsAgent.log(
                    new NameValuePair("login-event", loggedIn ? "LoggedIn" : "LoggedOut")
                    );
        } catch (Exception e) {
        }

        Thread t = new Thread() {
            @Override
            public void run() {
                synchronized (EVENT_LOCK) {
                    LoginController.this.loggedIn = loggedIn;

                    if (loggedIn) {
                        addLog("Logged in");
                        fireEvent(new LoginEvent(LoginEvent.Type.LOGGED_IN));
                    } else {
                        addLog("Logged out");
                        unregister();
                        fireEvent(new LoginEvent(LoginEvent.Type.LOGGED_OUT));
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

    public void addLog(String message) {
        if (loggedIn) {
            try {
                MedSavantClient.LogManager.addLog(sessionId, userName, LogType.INFO, message);
            } catch (Exception ex) {
                LOG.error("Error adding server log entry.", ex);
            }
        }
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public UserLevel getUserLevel() {
        return level;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public synchronized void login(String un, String pw, String dbname, String serverAddress, String serverPort) {

        //init registry
        try {
            MedSavantClient.initializeRegistry(serverAddress, serverPort);

            this.serverAddress = serverAddress;
            this.dbname = dbname;

            //register session
            userName = un;
            password = pw;
            LOG.info("Starting session...");
            sessionId = MedSavantClient.SessionManager.registerNewSession(un, pw, dbname);
            LOG.info("... done.  My session ID is: " + sessionId);
        } catch (Exception ex) {
            fireEvent(new LoginEvent(ex));
            return;
        }

        //determine privileges
        level = UserLevel.NONE;
        try {
            if (userName.equals("root")) {
                level = UserLevel.ADMIN;
            } else {
                level = MedSavantClient.UserManager.getUserLevel(sessionId, userName);
            }
        } catch (Exception ex) {
            fireEvent(new LoginEvent(ex));
            return;
        }

        SettingsController settings = SettingsController.getInstance();
        settings.setUsername(un);
        if (settings.getRememberPassword()) {
            settings.setPassword(pw);
        }

        //test connection
        try {
            MedSavantClient.SessionManager.testConnection(sessionId);
        } catch (Exception ex) {
            fireEvent(new LoginEvent(ex));
            return;
        }

        //check db version
        try {
            String databaseVersion = VersionSettings.getDatabaseVersion(); // TODO: implement database version check
            if (!VersionSettings.isCompatible(VersionSettings.getVersionString(), databaseVersion, false)) {
                DialogUtils.displayMessage("Version Mismatch", "<html>Your client version (" + VersionSettings.getVersionString() + ") is not compatible database (" + databaseVersion + ").<br>Visit " + VersionSettings.URL + " to get the correct version.</html>");
                fireEvent(new LoginEvent(LoginEvent.Type.LOGIN_FAILED));
                return;
            }
        } catch (SQLException ex) {
            fireEvent(new LoginEvent(ex));
            return;
        } catch (Exception ex) {
            LOG.error("Error comparing versions.", ex);
            ex.printStackTrace();
            DialogUtils.displayError("Error Comparing Versions", "<html>We could not determine compatibility between MedSavant and your database.<br>Please ensure that your versions are compatible before continuing.</html>");
        }
        try {
            if (setProject()) {
                setLoggedIn(true);
            } else {
                fireEvent(new LoginEvent(LoginEvent.Type.LOGIN_FAILED));
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error logging in: %s", ex);
            fireEvent(new LoginEvent(LoginEvent.Type.LOGIN_FAILED));
        }
    }

    public void logout() {
        MedSavantFrame.getInstance().setTitle("MedSavant");
        setLoggedIn(false);
        AnalyticsAgent.onEndSession(true);
        this.unregister();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        } finally {
            System.exit(0);
        }
    }

    public void unregister() {

        // queue session for unregistration
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    MedSavantClient.SessionManager.unregisterSession(sessionId);
                } catch (RemoteException ex) {
                    LOG.info("Error while logging out: " + ClientMiscUtils.getMessage(ex));
                } catch (Exception ex) {
                    LOG.info("Error while logging out: " + ClientMiscUtils.getMessage(ex));
                }
            }
        };
        new Thread(r).start();
    }

    /**
     * Set the initial project. If there are none, prompt to create one. If
     * there is one, select it. If there is more than one, prompt to select one.
     */
    private boolean setProject() throws SQLException, RemoteException {
        ProjectController pc = ProjectController.getInstance();
        String[] projNames = pc.getProjectNames();

        String proj = null;
        if (projNames.length == 0) {
            if (level == UserLevel.ADMIN) {
                DialogUtils.displayMessage("Welcome to MedSavant", "To begin using MedSavant, you will need to create a project.");
                //if (result == DialogUtils.OK) {
                new ProjectWizard().setVisible(true);
                projNames = pc.getProjectNames();
                if (projNames.length > 0) {
                    proj = projNames[0];
                }
                //} else {
                //    MedSavantFrame.getInstance().requestClose();
                //}

            } else {
                DialogUtils.displayMessage("Welcome to MedSavant", "No projects have been started. Please contact your administrator.");
            }
        } else if (projNames.length == 1) {
            proj = projNames[0];
        } else {
            ProjectChooser d = new ProjectChooser(projNames);
            d.setVisible(true);
            proj = d.getSelected();
        }
        if (proj != null) {
            pc.setProject(proj);
            pc.setDefaultReference();
        }
        return proj != null;
    }

    public String getServerAddress() {
        return this.serverAddress;
    }

    public String getDatabaseName() {
        return this.dbname;
    }
}
