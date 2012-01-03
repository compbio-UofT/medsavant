package org.ut.biolab.medsavant.db.util.query.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.db.model.UserLevel;

/**
 *
 * @author mfiume
 */
public interface UserQueryUtilAdapter extends Remote {

    public List<String> getUserNames(String sid) throws SQLException, RemoteException;
    public boolean userExists(String userName) throws SQLException, RemoteException;
    public void addUser(String sid, String name, char[] pass, UserLevel level) throws SQLException, RemoteException;
    public void grantPrivileges(String sid, String name, UserLevel level) throws SQLException, RemoteException;
    public boolean isUserAdmin(String name) throws SQLException, RemoteException;
    public UserLevel getUserLevel(String name) throws SQLException, RemoteException;
    public void removeUser(String name) throws SQLException, RemoteException;

}
