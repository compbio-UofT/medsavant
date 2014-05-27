/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.reference;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
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
            return manager.getReferences(LoginController.getSessionID());
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
            if (manager.containsReference(LoginController.getSessionID(), refName)) {

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
            return manager.getReferenceID(LoginController.getSessionID(), refName);
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
            return manager.getReferenceNames(LoginController.getSessionID());
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
            return manager.getReferenceUrl(LoginController.getSessionID(), currentReferenceID);
        } catch (Exception e){
            return null;
        }
    }

    public Chromosome[] getChromosomes() throws SQLException, RemoteException {
        return getChromosomes(currentReferenceID);
    }

    public Chromosome[] getChromosomes(int refID) throws SQLException, RemoteException {
        try {
            return manager.getChromosomes(LoginController.getSessionID(), refID);
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
            manager.addReference(LoginController.getSessionID(), name, chroms, url);
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
                    manager.removeReference(LoginController.getSessionID(), manager.getReferenceID(LoginController.getSessionID(), refName));
                    fireEvent(new ReferenceEvent(ReferenceEvent.Type.REMOVED, refName));
                } catch (Throwable ex) {
                    setVisible(false);
                    DialogUtils.displayError("Cannot remove this reference because projects\nor annotations still refer to it.");
                }
            }
        }.setVisible(true);
    }
}
