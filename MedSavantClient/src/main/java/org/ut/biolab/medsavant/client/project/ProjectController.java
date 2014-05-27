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
package org.ut.biolab.medsavant.client.project;

import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.swing.JOptionPane;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.ResultController;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.view.login.LoginEvent;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.shared.serverapi.ProjectManagerAdapter;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.Controller;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 *
 * @author mfiume
 */
public class ProjectController extends Controller<ProjectEvent> {
    private static final Log LOG = LogFactory.getLog(ProjectController.class);
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
                    manager.removeProject(LoginController.getSessionID(), projectName);
                    fireEvent(new ProjectEvent(ProjectEvent.Type.REMOVED, projectName));
                } catch (Throwable ex) {
                    ClientMiscUtils.reportError("Error removing project: %s", ex);
                }
            }
        }.setVisible(true);
    }

    public int addProject(String projName, CustomField[] fields) throws Exception {
        int projectid = manager.addProject(LoginController.getSessionID(), projName, fields);
        fireEvent(new ProjectEvent(ProjectEvent.Type.ADDED, projName));
        return projectid;
    }

    public int getProjectID(String projName) throws SQLException, RemoteException {
        try {
            return manager.getProjectID(LoginController.getSessionID(), projName);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return 0;
        }
    }

    public String getProjectNameFromID(int projID) throws SQLException, RemoteException {
        try {
            return manager.getProjectName(LoginController.getSessionID(), projID);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public void setProject(String projName) throws RemoteException, SQLException {
        try {
            if (manager.containsProject(LoginController.getSessionID(), projName)) {

                if (manager.containsProject(LoginController.getSessionID(), currentProjectName) && FilterController.getInstance().hasFiltersApplied()) {
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
            return manager.getProjectNames(LoginController.getSessionID());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public String getCurrentVariantTableName() throws SQLException, RemoteException {
        try {
            return manager.getVariantTableName(LoginController.getSessionID(), currentProjectID, ReferenceController.getInstance().getCurrentReferenceID(), true);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public String getCurrentVariantSubTableName() throws SQLException, RemoteException {
        try {
            return manager.getVariantTableName(LoginController.getSessionID(), currentProjectID, ReferenceController.getInstance().getCurrentReferenceID(), true, true);
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
            currentVariantTableSchema =  MedSavantClient.CustomTablesManager.getCustomTableSchema(LoginController.getSessionID(), getCurrentVariantTableName());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
    }

    public String getCurrentPatientTableName() throws RemoteException, SQLException {
        try {
            return MedSavantClient.PatientManager.getPatientTableName(LoginController.getSessionID(), currentProjectID);
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
        DbColumn dbc = new DbColumn(null, "A", "B", 1, 0);
        try {
            currentPatientTableSchema =  MedSavantClient.CustomTablesManager.getCustomTableSchema(LoginController.getSessionID(), getCurrentPatientTableName());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
    }

    public int[] getAnnotationIDs(int projectID, int refID) throws SQLException, RemoteException{
        int[] annotIDs;
        try{ 
         annotIDs = MedSavantClient.AnnotationManagerAdapter.getAnnotationIDs(LoginController.getSessionID(), projectID, refID);        
        }catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return null;
        }
        return annotIDs;
    }
    public AnnotationFormat[] getCurrentAnnotationFormats() throws SQLException, RemoteException {
        if (currentAnnotationFormats == null) {
            try {
                int[] annotIDs = getAnnotationIDs(this.currentProjectID, ReferenceController.getInstance().getCurrentReferenceID());
                //int[] annotIDs = MedSavantClient.AnnotationManagerAdapter.getAnnotationIDs(LoginController.getSessionID(), this.currentProjectID, ReferenceController.getInstance().getCurrentReferenceID());
                AnnotationFormat[] af = new AnnotationFormat[annotIDs.length+2];
                af[0] = AnnotationFormat.getDefaultAnnotationFormat();
                af[1] = AnnotationFormat.getCustomFieldAnnotationFormat(
                        manager.getCustomVariantFields(
                            LoginController.getSessionID(),
                            currentProjectID,
                            ReferenceController.getInstance().getCurrentReferenceID(),
                            manager.getNewestUpdateID(
                                LoginController.getSessionID(),
                                currentProjectID,
                                ReferenceController.getInstance().getCurrentReferenceID(),
                                true)));
                for (int i = 0; i < annotIDs.length; i++) {
                    af[i+2] = MedSavantClient.AnnotationManagerAdapter.getAnnotationFormat(LoginController.getSessionID(), annotIDs[i]);
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
                currentPatientFormat = MedSavantClient.PatientManager.getPatientFields(LoginController.getSessionID(), currentProjectID);
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
            references = manager.getReferenceNamesForProject(LoginController.getSessionID(), currentProjectID);
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
            unpublishedTables = manager.getUnpublishedChanges(LoginController.getSessionID());
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
        int option = JOptionPane.showOptionDialog(null, "<HTML>Publishing this table will log all users out of MedSavant, and restart the program. <BR>Are you sure you want to proceed?</HTML>", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
        if (option == JOptionPane.NO_OPTION) {
            try {
                MedSavantClient.VariantManager.cancelPublish(LoginController.getSessionID(), pd.getProjectID(), pd.getReferenceID(), pd.getUpdateID());
                return true;
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error cancelling publication of variants: %s", ex);
            }
        } else if (option == JOptionPane.YES_OPTION) {
            try {
                publishVariants(LoginController.getSessionID(), pd.getProjectID(), pd.getReferenceID(), pd.getUpdateID(), null);
                //MedSavantClient.VariantManager.publishVariants(LoginController.getSessionID(), pd.getProjectID(), pd.getReferenceID(), pd.getUpdateID());
                //MedSavantClient.restart(null);
                //LoginController.getInstance().logout();
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error publishing variants: %s", ex);
            }
        }
        return false;
    }
    
    public void publishVariants(String sessionID, int projectID, int referenceID, int updateID, String msg){
         try {            
            MedSavantFrame.getInstance().dispose();
            if(msg != null){
                DialogUtils.displayMessage(msg);
            }
            if(referenceID > 0 && updateID>0){               
                MedSavantClient.VariantManager.publishVariants(sessionID, projectID, referenceID, updateID);
            }else{
                MedSavantClient.VariantManager.publishVariants(sessionID, projectID);
            }
            MedSavantFrame.getInstance().forceRestart(); 
        } catch (Exception e) {
            LOG.error(e);
        }
    }
    
    public void publishVariants(String sessionID, int projectId, String msg){
        publishVariants(sessionID, projectId, -1, -1, msg);
    }
}
