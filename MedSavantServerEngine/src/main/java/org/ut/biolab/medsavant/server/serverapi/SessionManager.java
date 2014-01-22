/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.serverapi;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;

import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.serverapi.SessionManagerAdapter;
import org.ut.biolab.medsavant.server.mail.CryptoUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;

/**
 *
 * @author mfiume
 */
public class SessionManager extends MedSavantServerUnicastRemoteObject implements SessionManagerAdapter {

    private static final Log LOG = LogFactory.getLog(SessionManager.class);
    private static SessionManager instance;

    int lastSessionId = 0;

    public static synchronized SessionManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    private SessionManager() throws RemoteException {
    }

    @Override
    public synchronized String registerNewSession(String user, String password, String dbName) throws RemoteException, SQLException, Exception {
        String sessionID = nextSession();
        ConnectionController.registerCredentials(sessionID, user, password, dbName);
        LOG.info("Registered session for " + user);
        org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(sessionID, LogManagerAdapter.LogType.INFO, "Registered session for " + user);
        return sessionID;
    }

    @Override
    public void unregisterSession(String sessID) throws RemoteException, SQLException, SessionExpiredException {
        if(ConnectionController.sessionExists(sessID)){
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(sessID, LogManagerAdapter.LogType.INFO, "Unregistered session for " + SessionManager.getInstance().getUserForSession(sessID));        
            ConnectionController.removeSession(sessID);
        }
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
        for (String dbName : ConnectionController.getDBNames()) {
            terminateSessionsForDatabase(dbName, message);
        }
    }

    public void terminateSessionsForDatabase(String dbname, final String message) {

        LOG.info("Terminating sessions for database " + dbname);

        List<String> sessionIDsToTerminate = new ArrayList<String>();

        for (String sid : ConnectionController.getSessionIDs()) {
            try {
                if (SessionManager.getInstance().getDatabaseForSession(sid).equals(dbname)) {

                    sessionIDsToTerminate.add(sid);
                    // terminate session for this client
                }
            } catch (Exception ex) {
                LOG.warn("Unable to get session ID for " + dbname + ".", ex);
            }
        }

        for (final String sid : sessionIDsToTerminate) {
            MedSavantServerEngine.submitShortJob(new Runnable() {
                @Override
                public void run() {
                    try {
                        LOG.info("Terminating session " + sid + "...");
                        SessionManager.getInstance().unregisterSession(sid);
                    } catch (Exception ex) {
                        LOG.error("Unable to terminate session for " + sid + ".", ex);
                    }
                }
            });
        }
    }

    /**
     * Creates a new session key associated with the given session, to be used
     * by a background task.
     *
     * @param sessID The session ID whose credentials will be used
     * @return A new session ID
     */
    public String createBackgroundSessionFromSession(String sessID) {
        String sessionID = nextSession();
        LOG.info("Registered background session " + sessionID + " from " + sessID);
        ConnectionController.registerAdditionalSessionForSession(sessID, sessionID);
        return sessionID;
    }

    private synchronized String nextSession() {
        int newSessionIdNumber = ++lastSessionId;
        String sessionId = CryptoUtils.encrypt(newSessionIdNumber + "");
        return sessionId;
    }
}
