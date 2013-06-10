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
package org.ut.biolab.medsavant.client.project;

import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.swing.JOptionPane;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.ResultController;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.login.LoginEvent;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.shared.serverapi.ProjectManagerAdapter;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.Controller;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 *
 * @author mfiume
 */
public class ProjectController extends Controller<ProjectEvent> {

    private static ProjectController instance;

    private ProjectManagerAdapter manager;

    private String currentProjectName;
    private int currentProjectID;

    private AnnotationFormat[] currentAnnotationFormats;
    private CustomField[] currentPatientFormat;
    private TableSchema currentVariantTableSchema;
    private TableSchema currentPatientTableSchema;

    private ProjectController() {
        manager = MedSavantClient.ProjectManager;
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    try {
                        setCurrentVariantTable();
                        setCurrentAnnotationFormats(null);
                    } catch (Throwable ex) {
                        ClientMiscUtils.reportError("Error while switching reference: %s", ex);
                    }
                }
            }
        });
    }

    public static ProjectController getInstance() {
        if (instance == null) {
            instance = new ProjectController();
        }
        return instance;
    }

    public void removeProject(final String projectName) {

        new ProgressDialog("Removing Project", projectName + " project is being removed.  Please wait.") {
            @Override
            public void run() {
                try {
                    manager.removeProject(LoginController.getInstance().getSessionID(), projectName);
                    fireEvent(new ProjectEvent(ProjectEvent.Type.REMOVED, projectName));
                } catch (Throwable ex) {
                    ClientMiscUtils.reportError("Error removing project: %s", ex);
                }
            }
        }.setVisible(true);
    }

    public int addProject(String projName, CustomField[] fields) throws Exception {
        int projectid = manager.addProject(LoginController.getInstance().getSessionID(), projName, fields);
        fireEvent(new ProjectEvent(ProjectEvent.Type.ADDED, projName));
        return projectid;
    }

    public int getProjectID(String projName) throws SQLException, RemoteException {
        try {
            return manager.getProjectID(LoginController.getInstance().getSessionID(), projName);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return 0;
        }
    }

    public String getProjectNameFromID(int projID) throws SQLException, RemoteException {
        try {
            return manager.getProjectName(LoginController.getInstance().getSessionID(), projID);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public void setProject(String projName) throws RemoteException, SQLException {
        try {
            if (manager.containsProject(LoginController.getInstance().getSessionID(), projName)) {

                if (manager.containsProject(LoginController.getInstance().getSessionID(), currentProjectName) && FilterController.getInstance().hasFiltersApplied()) {
                    if (!DialogUtils.confirmChangeReference(true)) {
                        return;
                    }
                }

                currentProjectID = getProjectID(projName);
                currentProjectName = projName;
                setCurrentPatientTable();
                ResultController.getInstance().refreshCounts();
                fireEvent(new ProjectEvent(ProjectEvent.Type.CHANGED, projName));
            }
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
    }

    public int getCurrentProjectID() {
        return currentProjectID;
    }

    public String getCurrentProjectName() {
        return currentProjectName;
    }

    public String[] getProjectNames() throws SQLException, RemoteException {
        if (manager == null) {
            return new String[0];
        }
        try {
            return manager.getProjectNames(LoginController.getInstance().getSessionID());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public String getCurrentVariantTableName() throws SQLException, RemoteException {
        try {
            return manager.getVariantTableName(LoginController.getInstance().getSessionID(), currentProjectID, ReferenceController.getInstance().getCurrentReferenceID(), true);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public String getCurrentVariantSubTableName() throws SQLException, RemoteException {
        try {
            return manager.getVariantTableName(LoginController.getInstance().getSessionID(), currentProjectID, ReferenceController.getInstance().getCurrentReferenceID(), true, true);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public DbTable getCurrentVariantTable() {
        return currentVariantTableSchema.getTable();
    }

    public TableSchema getCurrentVariantTableSchema() {
        return currentVariantTableSchema;
    }

    private void setCurrentVariantTable() throws SQLException, RemoteException {
        try {
            currentVariantTableSchema =  MedSavantClient.CustomTablesManager.getCustomTableSchema(LoginController.getInstance().getSessionID(), getCurrentVariantTableName());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
    }

    public String getCurrentPatientTableName() throws RemoteException, SQLException {
        try {
            return MedSavantClient.PatientManager.getPatientTableName(LoginController.getInstance().getSessionID(), currentProjectID);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public DbTable getCurrentPatientTable() {
        return currentPatientTableSchema.getTable();
    }

    public TableSchema getCurrentPatientTableSchema() {
        return currentPatientTableSchema;
    }

    private void setCurrentPatientTable() throws SQLException, RemoteException {
        DbColumn dbc = new DbColumn(null, "A", "B", 1);
        try {
            currentPatientTableSchema =  MedSavantClient.CustomTablesManager.getCustomTableSchema(LoginController.getInstance().getSessionID(), getCurrentPatientTableName());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
    }

    public AnnotationFormat[] getCurrentAnnotationFormats() throws SQLException, RemoteException {
        if (currentAnnotationFormats == null) {
            try {
                int[] annotIDs = MedSavantClient.AnnotationManagerAdapter.getAnnotationIDs(LoginController.getInstance().getSessionID(), this.currentProjectID, ReferenceController.getInstance().getCurrentReferenceID());
                AnnotationFormat[] af = new AnnotationFormat[annotIDs.length+2];
                af[0] = AnnotationFormat.getDefaultAnnotationFormat();
                af[1] = AnnotationFormat.getCustomFieldAnnotationFormat(
                        manager.getCustomVariantFields(
                            LoginController.getInstance().getSessionID(),
                            currentProjectID,
                            ReferenceController.getInstance().getCurrentReferenceID(),
                            manager.getNewestUpdateID(
                                LoginController.getInstance().getSessionID(),
                                currentProjectID,
                                ReferenceController.getInstance().getCurrentReferenceID(),
                                true)));
                for (int i = 0; i < annotIDs.length; i++) {
                    af[i+2] = MedSavantClient.AnnotationManagerAdapter.getAnnotationFormat(LoginController.getInstance().getSessionID(), annotIDs[i]);
                }
                currentAnnotationFormats = af;
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return null;
            }
        }
        return currentAnnotationFormats;
    }

    public CustomField[] getCurrentPatientFormat() throws RemoteException, SQLException {
        if (currentPatientFormat == null) {
            try {
                currentPatientFormat = MedSavantClient.PatientManager.getPatientFields(LoginController.getInstance().getSessionID(), currentProjectID);
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return null;
            }
        }
        return currentPatientFormat;
    }

    public void setCurrentAnnotationFormats(AnnotationFormat[] formats) {
        currentAnnotationFormats = formats;
    }

    /**
     * For the current project, set the best reference.
     */
    public void setDefaultReference() throws RemoteException, SQLException {
        String[] references;
        try {
            references = manager.getReferenceNamesForProject(LoginController.getInstance().getSessionID(), currentProjectID);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
        ReferenceController.getInstance().setReference(references[references.length - 1]);
    }

    /**
     * Give user the option to publish unpublished changes or cancel them.
     */
    public boolean promptForUnpublished() throws SQLException, RemoteException {
        ProjectDetails[] unpublishedTables;
        try {
            unpublishedTables = manager.getUnpublishedChanges(LoginController.getInstance().getSessionID());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return false;
        }
        int refID = ReferenceController.getInstance().getCurrentReferenceID();
        for (ProjectDetails pd: unpublishedTables) {
            if (pd.getProjectID() == currentProjectID && pd.getReferenceID() == refID) {
                if (!promptToPublish(pd)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Give user the option to publish unpublished changes or cancel them.
     * @return <code>true</code> if the user is able to proceed (i.e. they haven't cancelled or logged out)
     */
    public boolean promptToPublish(ProjectDetails pd) {
        Object[] options = new Object[]{"Publish", "Delete (Undo Changes)", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, "<HTML>Publishing this table will log all users out of MedSavant.<BR>Are you sure you want to proceed?</HTML>", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

        if (option == JOptionPane.NO_OPTION) {
            try {
                MedSavantClient.VariantManager.cancelPublish(LoginController.getInstance().getSessionID(), pd.getProjectID(), pd.getReferenceID(), pd.getUpdateID());
                return true;
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error cancelling publication of variants: %s", ex);
            }
        } else if (option == JOptionPane.YES_OPTION) {
            try {
                MedSavantClient.VariantManager.publishVariants(LoginController.getInstance().getSessionID(), pd.getProjectID(), pd.getReferenceID(), pd.getUpdateID());
                LoginController.getInstance().logout();
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error publishing variants: %s", ex);
            }
        }
        return false;
    }
}
