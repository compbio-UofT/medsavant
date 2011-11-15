/*
 *    Copyright 2011 University of Toronto
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.db.model.UserLevel;
import org.ut.biolab.medsavant.db.util.query.UserQueryUtil;

/**
 *
 * @author mfiume
 */
public class UserController {
    private static final Logger LOG = Logger.getLogger(UserController.class.getName());
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
    
    public void removeUser(String name) {
        try {
            UserQueryUtil.removeUser(name);
            fireUserRemovedEvent(name);
        } catch (SQLException x) {
            LOG.log(Level.SEVERE, null, x);
        }
    }

    private void fireUserRemovedEvent(String name) {
        UserController pc = getInstance();
        for (UserListener l : pc.listeners) {
            l.userRemoved(name);
        }
    }

    public boolean addUser(String name, char[] pass, UserLevel level) {
        try {
            UserQueryUtil.addUser(name, pass, level);
            UserController.getInstance().fireUserAddedEvent(name);
            return true;
        } catch (SQLException x) {
            LOG.log(Level.SEVERE, null, x);
        }
        return false;
    }

    public static interface UserListener {
        public void userAdded(String name);
        public void userRemoved(String name);
        public void userChanged(String name);
    }
    
    public List<String> getUserNames() throws SQLException {
        return UserQueryUtil.getUserNames();
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
