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

package org.ut.biolab.medsavant.client.reference;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.model.Reference;
import org.ut.biolab.medsavant.shared.serverapi.ReferenceManagerAdapter;
import org.ut.biolab.medsavant.client.util.Controller;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 * @author mfiume
 */
public class ReferenceController extends Controller<ReferenceEvent> {

    private static ReferenceController instance;

    private final ReferenceManagerAdapter manager;
    private int currentReferenceID;
    private String currentReferenceName;
    private boolean referenceSet = false;

    private ReferenceController() {
        manager = MedSavantClient.ReferenceManager;
    }

    public static ReferenceController getInstance() {
        if (instance == null) {
            instance = new ReferenceController();
        }
        return instance;
    }

    public Reference[] getReferences() throws SQLException, RemoteException {
        try {
            return manager.getReferences(LoginController.getInstance().getSessionID());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public boolean setReference(String refName) throws SQLException, RemoteException{
        return setReference(refName, false);
    }

    public boolean setReference(String refName, boolean getConfirmation) throws SQLException, RemoteException {
        try {
            if (manager.containsReference(LoginController.getInstance().getSessionID(), refName)) {

                if (getConfirmation && FilterController.getInstance().hasFiltersApplied()){
                    if(!DialogUtils.confirmChangeReference(false)){
                        return false;
                    }
                }

                currentReferenceID = getReferenceID(refName);
                currentReferenceName = refName;

                fireEvent(new ReferenceEvent(ReferenceEvent.Type.CHANGED, refName));
            }
            referenceSet = true;
            return true;
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return false;
        }
    }

    public int getReferenceID(String refName) throws SQLException, RemoteException {
        try {
            return manager.getReferenceID(LoginController.getInstance().getSessionID(), refName);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return 0;
        }
    }

    public int getCurrentReferenceID() {
        return this.currentReferenceID;
    }

    public String[] getReferenceNames() throws SQLException, RemoteException {
        try {
            return manager.getReferenceNames(LoginController.getInstance().getSessionID());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public String getCurrentReferenceName(){
        return this.currentReferenceName;
    }

    public String getCurrentReferenceUrl(){
        try {
            return manager.getReferenceUrl(LoginController.getInstance().getSessionID(), currentReferenceID);
        } catch (Exception e){
            return null;
        }
    }

    public Chromosome[] getChromosomes() throws SQLException, RemoteException {
        return getChromosomes(currentReferenceID);
    }

    public Chromosome[] getChromosomes(int refID) throws SQLException, RemoteException {
        try {
            return manager.getChromosomes(LoginController.getInstance().getSessionID(), refID);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public boolean isReferenceSet(){
        return referenceSet;
    }

    public void addReference(String name, Chromosome[] chroms, String url) throws SQLException, RemoteException {
        try {
            manager.addReference(LoginController.getInstance().getSessionID(), name, chroms, url);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
        fireEvent(new ReferenceEvent(ReferenceEvent.Type.ADDED, name));
    }

    public void removeReference(final String refName) {
        new ProgressDialog("Removing Reference", "Reference " + refName + " is being removed. Please wait.") {
            @Override
            public void run() {
                try {
                    manager.removeReference(LoginController.getInstance().getSessionID(), manager.getReferenceID(LoginController.getInstance().getSessionID(), refName));
                    fireEvent(new ReferenceEvent(ReferenceEvent.Type.REMOVED, refName));
                } catch (Throwable ex) {
                    setVisible(false);
                    DialogUtils.displayError("Cannot remove this reference because projects\nor annotations still refer to it.");
                }
            }
        }.setVisible(true);
    }
}
