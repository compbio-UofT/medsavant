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

package org.ut.biolab.medsavant.controller;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.model.UserLevel;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;


/**
 *
 * @author mfiume
 */
public class UserController {
    private static final Log LOG = LogFactory.getLog(UserController.class);
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
    
    public void removeUser(final String name) {
        new IndeterminateProgressDialog("Removing User", name + " is being removed. Please wait.") {
            @Override
            public void run() {
                try {
                    MedSavantClient.UserQueryUtilAdapter.removeUser(LoginController.sessionId, name);
                    fireUserRemovedEvent(name);
                } catch (Throwable ex) {
                    ClientMiscUtils.reportError("Error removing user: " + MiscUtils.getMessage(ex), ex);
                }
            }
        }.setVisible(true);
    }

    private void fireUserRemovedEvent(String name) {
        UserController pc = getInstance();
        for (UserListener l : pc.listeners) {
            l.userRemoved(name);
        }
    }

    public boolean addUser(String name, char[] pass, UserLevel level) {
        try {
            MedSavantClient.UserQueryUtilAdapter.addUser(LoginController.sessionId, name, pass, level);
            UserController.getInstance().fireUserAddedEvent(name);
            return true;
        } catch (SQLException ex) {
            LOG.error("Error adding user.", ex);
        } catch (RemoteException ex) {
            LOG.error("Error adding user.", ex);
        }
        return false;
    }

    public static interface UserListener {
        public void userAdded(String name);
        public void userRemoved(String name);
        public void userChanged(String name);
    }
    
    public List<String> getUserNames() throws SQLException, RemoteException {
        return MedSavantClient.UserQueryUtilAdapter.getUserNames(LoginController.sessionId);
    }
    
    public void fireUserAddedEvent(String name) {
        UserController pc = getInstance();
        for (UserListener l : pc.listeners) {
            l.userAdded(name);
        }
    }
    
    public void addUserListener(UserListener l) {
        this.listeners.add(l);
    }
}
