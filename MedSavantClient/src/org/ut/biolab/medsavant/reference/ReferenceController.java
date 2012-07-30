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

package org.ut.biolab.medsavant.reference;

import java.rmi.RemoteException;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.serverapi.ReferenceManagerAdapter;
import org.ut.biolab.medsavant.util.Controller;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.util.DialogUtils;


/**
 * @author mfiume
 */
public class ReferenceController extends Controller<ReferenceEvent> {
    private static final Log LOG = LogFactory.getLog(ReferenceController.class);

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

    public String[] getReferenceNames() throws SQLException, RemoteException {
        return manager.getReferenceNames(LoginController.sessionId);
    }

     public boolean setReference(String refName) throws SQLException, RemoteException{
        return setReference(refName, false);
    }

    public boolean setReference(String refName, boolean getConfirmation) throws SQLException, RemoteException {
        if (manager.containsReference(LoginController.sessionId, refName)) {

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
    }

    public int getReferenceID(String refName) throws SQLException, RemoteException {
        return manager.getReferenceID(LoginController.sessionId, refName);
    }

    public int getCurrentReferenceID() {
        return this.currentReferenceID;
    }

    public String getCurrentReferenceName(){
        return this.currentReferenceName;
    }

    public String getCurrentReferenceUrl(){
        try {
            return manager.getReferenceUrl(LoginController.sessionId, currentReferenceID);
        } catch (Exception e){
            return null;
        }
    }

    public Chromosome[] getChromosomes() throws SQLException, RemoteException {
        return manager.getChromosomes(LoginController.sessionId, currentReferenceID);
    }

    public boolean isReferenceSet(){
        return referenceSet;
    }

    public void addReference(String name, Chromosome[] chroms, String url) throws SQLException, RemoteException {
        manager.addReference(LoginController.sessionId, name, chroms, url);
        fireEvent(new ReferenceEvent(ReferenceEvent.Type.ADDED, name));
    }

    public void removeReference(final String refName) {
        new IndeterminateProgressDialog("Removing Reference", "Reference " + refName + " is being removed. Please wait.") {
            @Override
            public void run() {
                try {
                    manager.removeReference(LoginController.sessionId, manager.getReferenceID(LoginController.sessionId, refName));
                    fireEvent(new ReferenceEvent(ReferenceEvent.Type.REMOVED, refName));
                } catch (Throwable ex) {
                    setVisible(false);                    
                    DialogUtils.displayError("Cannot remove this reference because projects\nor annotations still refer to it.");
                }
            }
        }.setVisible(true);
    }
}
