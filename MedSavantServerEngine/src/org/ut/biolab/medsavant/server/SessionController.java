package org.ut.biolab.medsavant.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.client.api.ClientCallbackAdapter;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.SessionConnection;
import org.ut.biolab.medsavant.server.api.SessionAdapter;
import org.ut.biolab.medsavant.server.mail.CryptoUtils;

/**
 *
 * @author mfiume
 */
public class SessionController extends java.rmi.server.UnicastRemoteObject implements SessionAdapter {

    int lastSessionId = 0;
    private static SessionController instance;

    public static synchronized SessionController getInstance() throws RemoteException {
        if (instance == null) {
            instance = new SessionController();
        }
        return instance;
    }

    public SessionController() throws RemoteException {
    }

    @Override
    public synchronized String registerNewSession(String uname, String pw, String dbname) {

        int newSessionIdNumber = ++lastSessionId;
        String sessionId = CryptoUtils.encrypt(newSessionIdNumber + "");

        //String sessionId = ++lastSessionId + "";

        boolean success = ConnectionController.registerCredentials(sessionId, uname, pw, dbname);

        if (success) {
            System.out.println("Registered session: " + sessionId);
            return sessionId;
        } else {
            return null;
        }
    }

    @Override
    public void unregisterSession(String sessionId) throws RemoteException {
        ConnectionController.removeSession(sessionId);
    }

    @Override
    public boolean testConnection(String sessionId) throws RemoteException {
        return ConnectionController.connectPooled(sessionId) != null;
    }

    public String getUserForSession(String sid) {
        return ConnectionController.getUserForSession(sid);
    }

    @Override
    public void registerCallback(String sessionId, final ClientCallbackAdapter cca) throws RemoteException {
        ConnectionController.addCallback(sessionId, cca);
    }

    public String getDatabaseForSession(String sid) {
        return ConnectionController.getDatabaseForSession(sid);
    }

    public void terminateSessionsForDatabase(String dbname) {
        terminateSessionsForDatabase(dbname, null);
    }

    public void terminateSessionsForDatabase(String dbname, String message) {

        System.out.println("Terminating sessions for database " + dbname);

        List<String> sessionIDsToTerminate = new ArrayList<String>();

        for (String sid : ConnectionController.getSessionIDs()) {
            try {
                if (SessionController.getInstance().getDatabaseForSession(sid).equals(dbname)) {
                    System.out.println("Terminating session " + sid);

                    sessionIDsToTerminate.add(sid);
                    // terminate session for this client
                }
            } catch (Exception ex) {
                Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (String sid : sessionIDsToTerminate) {
            try {
                ClientCallbackAdapter ca = ConnectionController.getCallback(sid);

                if (ca != null) {
                    ca.sessionTerminated(message);
                    SessionController.getInstance().unregisterSession(sid);
                }
            } catch (Exception ex) {
                Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
