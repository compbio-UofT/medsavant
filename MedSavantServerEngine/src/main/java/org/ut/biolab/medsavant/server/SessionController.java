/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.server;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.serverapi.SessionManagerAdapter;
import org.ut.biolab.medsavant.server.mail.CryptoUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 *
 * @author mfiume
 */
public class SessionController extends MedSavantServerUnicastRemoteObject implements SessionManagerAdapter {
    private static final Log LOG = LogFactory.getLog(SessionController.class);
    private static SessionController instance;

    int lastSessionId = 0;

    public static synchronized SessionController getInstance() throws RemoteException {
        if (instance == null) {
            instance = new SessionController();
        }
        return instance;
    }

    private SessionController() throws RemoteException {
    }

    @Override
    public synchronized String registerNewSession(String user, String password, String dbName) throws RemoteException, SQLException {
        int newSessionIdNumber = ++lastSessionId;
        String sessionId = CryptoUtils.encrypt(newSessionIdNumber + "");

        ConnectionController.registerCredentials(sessionId, user, password, dbName);
        System.out.println("Registered session " + sessionId + " for " + user);
        LOG.info("Registered session " + sessionId + " for " + user);
        return sessionId;
    }

    @Override
    public void unregisterSession(String sessID) throws RemoteException, SQLException {
        // TODO: fix this, session connection pools are needed by orphaned jobs
        ConnectionController.removeSession(sessID);
        System.out.println("Unregistered session " + sessID);
        LOG.info("Unregistered session " + sessID);
    }

    @Override
    public void testConnection(String sessID) throws RemoteException, SQLException, SessionExpiredException {
        Connection conn = null;
        try {
            conn = ConnectionController.connectPooled(sessID);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public String getUserForSession(String sid) {
        return ConnectionController.getUserForSession(sid);
    }

    public String getDatabaseForSession(String sid) {
        return ConnectionController.getDBName(sid);
    }

    public void terminateSessionsForDatabase(String dbname) {
        terminateSessionsForDatabase(dbname, null);
    }

    public void terminateAllSessions(String message) {
        for(String dbName : ConnectionController.getDBNames()) {
            terminateSessionsForDatabase(dbName, message);
        }
    }

    public void terminateSessionsForDatabase(String dbname, final String message) {

        System.out.println("Terminating sessions for database " + dbname);

        List<String> sessionIDsToTerminate = new ArrayList<String>();

        for (String sid : ConnectionController.getSessionIDs()) {
            try {
                if (SessionController.getInstance().getDatabaseForSession(sid).equals(dbname)) {

                    sessionIDsToTerminate.add(sid);
                    // terminate session for this client
                }
            } catch (Exception ex) {
                LOG.warn("Unable to get session ID for " + dbname + ".", ex);
            }
        }

        for (final String sid : sessionIDsToTerminate) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        System.out.print("Terminating session " + sid + "...");
                        SessionController.getInstance().unregisterSession(sid);
                        System.out.println("Complete");
                    } catch (Exception ex) {
                        System.out.println("Failed");
                        LOG.error("Unable to terminate session for " + sid + ".", ex);
                    }
                }
            };
            t.start();
        }
    }
}
