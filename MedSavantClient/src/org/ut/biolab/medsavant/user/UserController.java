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

package org.ut.biolab.medsavant.user;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.UserLevel;


/**
 *
 * @author mfiume
 */
public class UserController {
    private final ArrayList<UserListener> listeners;

    private static UserController instance;
    
    private UserController() {
        listeners = new ArrayList<UserListener>();
    }
    
    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }
    
    public void addUser(String name, char[] pass, UserLevel level) throws SQLException, RemoteException {
        MedSavantClient.UserQueryUtilAdapter.addUser(LoginController.sessionId, name, pass, level);
        UserController.getInstance().fireUserAddedEvent(name);
    }

    public void removeUser(String name) throws SQLException, RemoteException {
        MedSavantClient.UserQueryUtilAdapter.removeUser(LoginController.sessionId, name);
        fireUserRemovedEvent(name);
    }

    public static interface UserListener {
        public void userAdded(String name);
        public void userRemoved(String name);
        public void userChanged(String name);
    }
    
    public List<String> getUserNames() throws SQLException, RemoteException {
        return MedSavantClient.UserQueryUtilAdapter.getUserNames(LoginController.sessionId);
    }
    
    private void fireUserRemovedEvent(String name) {
        for (UserListener l: listeners) {
            l.userRemoved(name);
        }
    }

    private void fireUserAddedEvent(String name) {
        for (UserListener l: listeners) {
            l.userAdded(name);
        }
    }
    
    public void addUserListener(UserListener l) {
        listeners.add(l);
    }
}
