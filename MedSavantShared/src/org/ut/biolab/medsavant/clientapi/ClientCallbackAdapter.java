/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.clientapi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Andrew
 */
public interface ClientCallbackAdapter extends Remote {
    
    public void sessionTerminated(String message) throws RemoteException;
    
}
