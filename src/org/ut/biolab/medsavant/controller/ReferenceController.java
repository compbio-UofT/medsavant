package org.ut.biolab.medsavant.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil;

/**
 * @author mfiume
 */
public class ReferenceController {
    
    private final ArrayList<ReferenceListener> referenceListeners;

    /*
    public void removeReference(String referenceName) {
        try {
            org.ut.biolab.medsavant.db.Manage.removeReference(referenceName);
            fireReferenceRemovedEvent(referenceName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
     * 
     */

    /*
    private void fireReferenceRemovedEvent(String referenceName) {
        ReferenceController pc = getInstance();
        for (ReferenceListener l : pc.referenceListeners) {
            l.referenceRemoved(referenceName);
        }
    }
     * 
     */

    public void addReference(String name) {
        try {
            org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil.addReference(name);
            fireReferenceAddedEvent(name);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean removeReference(String refName) {
        try {
            boolean success = ReferenceQueryUtil.removeReference(ReferenceQueryUtil.getReferenceId(refName));
            if (success) {
                this.fireReferenceRemovedEvent(refName);
            }
            return success;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static interface ReferenceListener {
        public void referenceAdded(String name);
        public void referenceRemoved(String name);
        public void referenceChanged(String prnameojectName);
    }
    
    private static ReferenceController instance;
    
    private ReferenceController() {
        referenceListeners = new ArrayList<ReferenceListener>();
    }
    
    public static ReferenceController getInstance() {
        if (instance == null) {
            instance = new ReferenceController();
        }
        return instance;
    }
    
    public List<String> getReferenceNames() throws SQLException {
        return ReferenceQueryUtil.getReferenceNames();
    }
    
    public void fireReferenceAddedEvent(String projectName) {
        ReferenceController pc = getInstance();
        for (ReferenceListener l : pc.referenceListeners) {
            l.referenceAdded(projectName);
        }
    }
    
    public void fireReferenceRemovedEvent(String projectName) {
        ReferenceController pc = getInstance();
        for (ReferenceListener l : pc.referenceListeners) {
            l.referenceRemoved(projectName);
        }
    }
    
    public void addReferenceListener(ReferenceListener l) {
        this.referenceListeners.add(l);
    }
    
}
