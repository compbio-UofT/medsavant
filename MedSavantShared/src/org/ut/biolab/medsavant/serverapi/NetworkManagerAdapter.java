package org.ut.biolab.medsavant.serverapi;

import java.io.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

/**
 *
 * @author mfiume
 */
public interface NetworkManagerAdapter extends Remote {

    public int openFileOnServer(String sessID) throws IOException, RemoteException;
    public void sendLineToServer(int fileIdentifier, String line) throws IOException, SQLException, RemoteException;
    public void closeFileOnServer(int fileIdentifier) throws IOException, RemoteException;

}
