package org.ut.biolab.medsavant.server.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.ut.biolab.medsavant.client.api.ClientCallbackAdapter;

/**
 *
 * @author mfiume
 */
public interface SessionAdapter extends Remote {

    String registerNewSession(String uname, String pw, String dbname) throws RemoteException;
    void unregisterSession(String sessionId) throws RemoteException;
    boolean testConnection(String sessionId) throws RemoteException;    
    void registerCallback(String sessionId, ClientCallbackAdapter cca) throws RemoteException;
    
}
