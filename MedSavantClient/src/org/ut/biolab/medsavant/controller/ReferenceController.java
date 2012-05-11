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
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.util.DialogUtils;


/**
 * @author mfiume
 */
public class ReferenceController {
    private static final Log LOG = LogFactory.getLog(ReferenceController.class);
    private int currentReferenceId;
    private String currentReferenceName;
    private boolean referenceSet = false;

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
    public void addReference(String name, List<Chromosome> contigs, String url) {
        try {
            MedSavantClient.ReferenceQueryUtilAdapter.addReference(LoginController.sessionId, name, contigs, url);
            fireReferenceAddedEvent(name);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (RemoteException ex) {
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
                    MedSavantClient.ReferenceQueryUtilAdapter.removeReference(LoginController.sessionId, MedSavantClient.ReferenceQueryUtilAdapter.getReferenceId(LoginController.sessionId, refName));
                    fireReferenceRemovedEvent(refName);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MedSavantFrame.getInstance(), "Cannot remove this reference because projects\nor annotations still refer to it.", "", JOptionPane.ERROR_MESSAGE);
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

    public List<String> getReferenceNames() throws SQLException, RemoteException {
        return MedSavantClient.ReferenceQueryUtilAdapter.getReferenceNames(LoginController.sessionId);
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
            if (MedSavantClient.ReferenceQueryUtilAdapter.containsReference(LoginController.sessionId, refName)) {

                if(getConfirmation && FilterController.hasFiltersApplied()){
                    if(!DialogUtils.confirmChangeReference(false)){
                        return false;
                    }
                }

                this.currentReferenceId = this.getReferenceId(refName);
                this.currentReferenceName = refName;

                this.fireReferenceChangedEvent(refName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Error setting reference.", ex);
            return false;
        }
        referenceSet = true;
        return true;
    }

    public void fireReferenceChangedEvent(String referenceName){
        for (ReferenceListener l : referenceListeners){
            l.referenceChanged(referenceName);
        }
    }

    public int getReferenceId(String refName) throws SQLException, RemoteException {
        return MedSavantClient.ReferenceQueryUtilAdapter.getReferenceId(LoginController.sessionId, refName);
    }

    public int getCurrentReferenceId() {
        return this.currentReferenceId;
    }

    public String getCurrentReferenceName(){
        return this.currentReferenceName;
    }

    public String getCurrentReferenceUrl(){
        try {
            return MedSavantClient.ReferenceQueryUtilAdapter.getReferenceUrl(LoginController.sessionId, currentReferenceId);
        } catch (Exception e){
            return null;
        }
    }

    public List<Chromosome> getChromosomes() throws SQLException, RemoteException {
        return MedSavantClient.ReferenceQueryUtilAdapter.getChromosomes(LoginController.sessionId, currentReferenceId);
    }

    public boolean isReferenceSet(){
        return referenceSet;
    }

}
