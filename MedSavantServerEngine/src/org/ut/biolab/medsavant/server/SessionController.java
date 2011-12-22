package org.ut.biolab.medsavant.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.server.api.SessionAdapter;

/**
 *
 * @author mfiume
 */
public class SessionController implements SessionAdapter, Serializable {

    int lastSessionId = 0;

    private static SessionController instance;

    public static SessionController getInstance() {
        if (instance == null) {
            instance = new SessionController();
        }
        return instance;
    }

    @Override
    public String registerNewSession(String uname, String pw, String dbname) {

        String sessionId = ++lastSessionId + "";

        boolean success = ConnectionController.registerCredentials(sessionId,uname,pw,dbname);

        if (success) {
            return sessionId;
        } else {
            return null;
        }
    }

}
