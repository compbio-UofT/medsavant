/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.shared.serverapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
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
    public UserLevel getUserLevel(String sessID, String name) throws SQLException, RemoteException, SessionExpiredException;
    public void removeUser(String sessID, String name) throws SQLException, RemoteException, SessionExpiredException;

}
