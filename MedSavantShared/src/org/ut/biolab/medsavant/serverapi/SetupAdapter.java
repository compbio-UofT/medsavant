/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.serverapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

/**
 *
 * @author Andrew
 */
public interface SetupAdapter extends Remote {
 
    public void createDatabase(String dbHost, int port, String dbname, String adminName, char[] rootPassword, String versionString) throws SQLException, RemoteException;
    
    public void removeDatabase(String dbHost, int port, String dbname, String adminName, char[] rootPassword) throws SQLException, RemoteException;
}
