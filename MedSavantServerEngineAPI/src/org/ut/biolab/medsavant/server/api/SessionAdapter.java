package org.ut.biolab.medsavant.server.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author mfiume
 */
public interface SessionAdapter extends Remote {

    String registerNewSession(String uname, String pw, String dbname) throws RemoteException;
    void unregisterSession(String sessionId);
    
}
