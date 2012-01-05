/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.controller;

import java.io.Serializable;
import java.rmi.RemoteException;
import org.ut.biolab.medsavant.client.api.ClientCallbackAdapter;

/**
 *
 * @author Andrew
 */
public class CallbackController extends java.rmi.server.UnicastRemoteObject implements ClientCallbackAdapter, Serializable {
    
    private static CallbackController instance;
    
    public static CallbackController getInstance() throws RemoteException {
        if(instance == null) {
            instance = new CallbackController();                    
        }
        return instance;
    }
    
    public CallbackController() throws RemoteException {}

    @Override
    public void sessionTerminated(String message) throws RemoteException {
        //TODO
        System.out.println("Session Terminated");
    }
    
}
