/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.shared.serverapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Set;
import org.ut.biolab.medsavant.shared.format.UserRole;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

import org.ut.biolab.medsavant.shared.model.UserLevel;

/**
 *
 * @author mfiume
 */
public interface UserManagerAdapter extends Remote {

    public String[] getUserNames(String sessID) throws SQLException, RemoteException, SessionExpiredException;

    public boolean userExists(String sessID, String userName) throws SQLException, RemoteException, SessionExpiredException;

    public void addUser(String sessID, String name, char[] pass, UserLevel level) throws SQLException, RemoteException, SessionExpiredException;

    public void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException, RemoteException, SessionExpiredException;

    public UserLevel getSessionUsersLevel(String sessID) throws SQLException, RemoteException, SessionExpiredException;

    public UserLevel getUserLevel(String sessID, String name) throws SQLException, RemoteException, SessionExpiredException;

    public void removeUser(String sessID, String name) throws SQLException, RemoteException, SessionExpiredException;

    public void changePassword(String sessID, String userName, char[] oldPass, char[] newPass) throws SQLException, RemoteException, SessionExpiredException;
    
    public UserRole getRoleByName(String sessID, String roleName) throws RemoteException, SessionExpiredException, SQLException;
    
    public UserRole addRole(String sessID, String roleName, String roleDescription) throws RemoteException, SessionExpiredException, SQLException, SecurityException;

    public void dropRolesForUser(String sessID, String user, Set<UserRole> roles) throws RemoteException, SessionExpiredException, SQLException, SecurityException;

    public void registerRoleForUser(String sessID, String user, Set<UserRole> roles) throws RemoteException, SessionExpiredException, SQLException, SecurityException;

    public Set<UserRole> getUserRoles(String sessID, String user) throws RemoteException, SessionExpiredException, SQLException, SecurityException;

    public Set<UserRole> getRolesForUser(String sessID) throws SQLException, SessionExpiredException, RemoteException;

    public Set<UserRole> getAllRoles(String sessID) throws RemoteException, SQLException, SecurityException, SessionExpiredException;
}
