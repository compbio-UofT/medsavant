package org.ut.biolab.medsavant.util;

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

    private static int CONNECT_PORT = 36800;
    private static int EXPORT_PORT = 36801;

    public MedSavantServerUnicastRemoteObject() throws java.rmi.RemoteException {
        super(EXPORT_PORT);
    }

    public static int getListenPort() {
        return CONNECT_PORT;
    }

    public static int getExportPort() {
        return EXPORT_PORT;
    }

    public static void setListenPort(int port) {
        CONNECT_PORT = port;
    }

    public static void setExportPort(int port) {
        EXPORT_PORT = port;
    }

}

