package org.ut.biolab.medsavant.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.util.jobject.UserQueryUtil;

/**
 *
 * @author mfiume
 */
public class UserController {
    
    private final ArrayList<UserListener> listeners;

    public void removeUser(String name) {
        try {
        org.ut.biolab.medsavant.db.Manage.removeUser(name);
        fireUserRemovedEvent(name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fireUserRemovedEvent(String name) {
        UserController pc = getInstance();
        for (UserListener l : pc.listeners) {
            l.userRemoved(name);
        }
    }

    public boolean addUser(String name, String pass, boolean admin) {
        try {
            org.ut.biolab.medsavant.db.Manage.addUser(name,pass,admin);
            UserController.getInstance().fireUserAddedEvent(name);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static interface UserListener {
        public void userAdded(String name);
        public void userRemoved(String name);
        public void userChanged(String name);
    }
    
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
