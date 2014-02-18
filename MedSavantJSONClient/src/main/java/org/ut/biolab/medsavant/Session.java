package org.ut.biolab.medsavant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.serverapi.SessionManagerAdapter;

/**
 * Maintains the MedSavant server session for the JSON Client.  getSessionId(true)
 * can be used to renew a session (e.g. in the event of a SessionExpiredException) 
 */
public class Session {
    private static final int RENEW_RETRY_TIME = 10000;
    private static final Log LOG = LogFactory.getLog(Session.class);
    
    private final SessionManagerAdapter sessionManager;
    private final String username;
    private final String password;
    private final String db;        

    private String sessionId;
        
    public Session(SessionManagerAdapter sessionManager, String username, String pw, String db){
        this.sessionManager = sessionManager;
        this.username = username;
        this.password = pw;
        this.db = db;
    }
    
    private synchronized boolean renewSession() {
        try {
            sessionId = null;
            sessionId = this.sessionManager.registerNewSession(this.username, this.password, this.db);
            LOG.info("Renewed new session with id " + sessionId);
        } catch (Exception e) {
            // can't recover from this.
            LOG.error("Exception while registering session, retrying in " + RENEW_RETRY_TIME + " ms: " + e);
            sessionId = null;
            return false;
        }

        return true;
    }
        
    public synchronized String getSessionId(){
        return getSessionId(false);
    }
    
    public synchronized String getSessionId(boolean forceRenew) {
        if(forceRenew){
            sessionId = null;
        }
        
        try {
            if (sessionId == null) {
                while (!renewSession()) {
                    Thread.sleep(RENEW_RETRY_TIME);
                }
            }
            return sessionId;
        } catch (InterruptedException iex) {
            LOG.error("CRITICAL: Thread interrupted while trying to get session id");
        }
        return null;
    }
}
