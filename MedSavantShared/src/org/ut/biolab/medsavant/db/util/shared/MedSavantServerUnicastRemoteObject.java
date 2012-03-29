package org.ut.biolab.medsavant.db.util.shared;

import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author mfiume
 */
public class MedSavantServerUnicastRemoteObject extends UnicastRemoteObject {

    /*
    private static final int EXPORT_PORT = 3232;

    public MedSavantServerUnicastRemoteObject() throws java.rmi.RemoteException {
        super(EXPORT_PORT);
    }

    public int getPort() {
        return EXPORT_PORT;
    }
    */

    private static final int CONNECT_PORT = 46802;
    private static final int EXPORT_PORT = 46803;

    public MedSavantServerUnicastRemoteObject() throws java.rmi.RemoteException {
        super(EXPORT_PORT);
    }

    public int getPort() {
        return CONNECT_PORT;
    }
}
