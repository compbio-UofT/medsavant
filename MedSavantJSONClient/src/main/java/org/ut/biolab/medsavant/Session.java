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
