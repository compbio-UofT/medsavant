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

    public int openFileWriterOnServer(String sessID) throws IOException, RemoteException;
    public void writeLineToServer(String sessID, int fileID, String line) throws IOException, SQLException, RemoteException;
    public void closeFileWriterOnServer(String sessID, int fileID) throws IOException, RemoteException;

    public void openFileReaderOnServer(String sessID, int fileID) throws IOException, RemoteException;
    public String readLineFromServer(String sessID, int fileID) throws IOException, SQLException, RemoteException;
    public void closeFileReaderOnServer(String sessID, int fileIdentifier) throws IOException, RemoteException;

}
