package org.ut.biolab.medsavant.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 * @author mfiume
 */
public class ReferenceController {

    private int currentReferenceId;
    private String currentReferenceName;
    
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
    public void addReference(String name, List<Chromosome> contigs) {
        try {
            org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil.addReference(name, contigs);
            fireReferenceAddedEvent(name);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void removeReference(final String refName) {
        final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
                "Removing Reference", 
                "Reference " + refName + " is being removed. Please wait.", 
                true);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    ReferenceQueryUtil.removeReference(ReferenceQueryUtil.getReferenceId(refName));
                    fireReferenceRemovedEvent(refName);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Cannot remove this reference because projects\nor annotations still refer to it.", "", JOptionPane.ERROR_MESSAGE);
                }
                dialog.close();  
            }
        };
        thread.start(); 
        dialog.setVisible(true);
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
    
     public boolean setReference(String refName){
        return setReference(refName, false);
    }
    
     

    

    public boolean setReference(String refName, boolean getConfirmation) {
        try {
            if (ReferenceQueryUtil.containsReference(refName)) {
                
                if(getConfirmation && FilterController.hasFiltersApplied()){
                    if(!DialogUtils.confirmChangeReference(false)){
                        return false;
                    }
                }
                
                this.currentReferenceId = this.getReferenceId(refName);
                this.currentReferenceName = refName;
                
                this.fireReferenceChangedEvent(refName);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    public void fireReferenceChangedEvent(String referenceName){
        for (ReferenceListener l : referenceListeners){
            l.referenceChanged(referenceName);
        }
    }

    public int getReferenceId(String refName) throws SQLException {
        return org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil.getReferenceId(refName);
    }

    public int getCurrentReferenceId() {
        return this.currentReferenceId;
    }
    
    public String getCurrentReferenceName(){
        return this.currentReferenceName;
    }

}
