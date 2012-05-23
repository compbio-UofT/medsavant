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
package org.ut.biolab.medsavant.project;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.format.VariantFormat;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.reference.ReferenceListener;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public class ProjectController implements ReferenceListener {
    private String currentProjectName;
    private int currentProjectID;

    private AnnotationFormat[] currentAnnotationFormats;
    private List<CustomField> currentPatientFormat;

    private TableSchema currentVariantTableSchema;

    private TableSchema currentPatientTableSchema;

    private static ProjectController instance;

    private final ArrayList<ProjectListener> projectListeners;

    private ProjectController() {
        projectListeners = new ArrayList<ProjectListener>();
    }

    public static ProjectController getInstance() {
        if (instance == null) {
            instance = new ProjectController();
            ReferenceController.getInstance().addReferenceListener(instance);
        }
        return instance;
    }

    public void removeProject(final String projectName) {

        new IndeterminateProgressDialog("Removing Project", projectName + " project is being removed.  Please wait.") {
            @Override
            public void run() {
                try {
                    MedSavantClient.ProjectQueryUtilAdapter.removeProject(LoginController.sessionId, projectName);
                    fireProjectRemovedEvent(projectName);
                } catch (Throwable ex) {
                    ClientMiscUtils.reportError("Error removing project: %s", ex);
                }
            }
        }.setVisible(true);
    }

    public int addProject(String projectName, List<CustomField> fields) throws Exception {
        int projectid = MedSavantClient.ProjectQueryUtilAdapter.addProject(LoginController.sessionId, projectName, fields);
        ProjectController.getInstance().fireProjectAddedEvent(projectName);
        return projectid;
    }

    public int getProjectID(String projectName) throws SQLException, RemoteException {
        return MedSavantClient.ProjectQueryUtilAdapter.getProjectId(LoginController.sessionId, projectName);
    }

    public String getProjectName(int projectid) throws SQLException, RemoteException {
        return MedSavantClient.ProjectQueryUtilAdapter.getProjectName(LoginController.sessionId, projectid);
    }

    public void setProject(String projectName) throws RemoteException, SQLException {
        if (MedSavantClient.ProjectQueryUtilAdapter.containsProject(LoginController.sessionId, projectName)) {

            if (MedSavantClient.ProjectQueryUtilAdapter.containsProject(LoginController.sessionId, this.currentProjectName) &&
                    FilterController.hasFiltersApplied()) {
                if (!DialogUtils.confirmChangeReference(true)) {
                    return;
                }
            }

            currentProjectID = getProjectID(projectName);
            currentProjectName = projectName;
            setCurrentPatientTable();
            ResultController.getInstance().setUpdateTotalNumRequired(true);
            fireProjectChangedEvent(projectName);
        }
    }

    public int getCurrentProjectID() {
        return currentProjectID;
    }

    public String getCurrentProjectName() {
        return currentProjectName;
    }

    public int getNumVariantsInTable(int projectid, int refid) throws SQLException, RemoteException {
        return MedSavantClient.ProjectQueryUtilAdapter.getNumberOfRecordsInVariantTable(LoginController.sessionId, projectid,refid);
    }

    public List<String> getProjectNames() throws SQLException, RemoteException {
        if (MedSavantClient.ProjectQueryUtilAdapter == null) {
            return new ArrayList<String>();
        }
        return MedSavantClient.ProjectQueryUtilAdapter.getProjectNames(LoginController.sessionId);
    }

    void fireProjectAddedEvent(String projectName) {
        for (ProjectListener l : projectListeners) {
            l.projectAdded(projectName);
        }
    }

    void fireProjectChangedEvent(String projectName) {
        for (ProjectListener l : projectListeners) {
            l.projectChanged(projectName);
        }
    }

    void fireProjectRemovedEvent(String projectName) {
        for (ProjectListener l : projectListeners) {
            l.projectRemoved(projectName);
        }
    }

    public void addProjectListener(ProjectListener l) {
        projectListeners.add(l);
    }

    public String getCurrentVariantTableName() throws SQLException, RemoteException {
        return MedSavantClient.ProjectQueryUtilAdapter.getVariantTablename(LoginController.sessionId, currentProjectID, ReferenceController.getInstance().getCurrentReferenceID(), true);
    }

    public String getCurrentVariantSubTableName() throws SQLException, RemoteException {
        return MedSavantClient.ProjectQueryUtilAdapter.getVariantTablename(LoginController.sessionId, currentProjectID, ReferenceController.getInstance().getCurrentReferenceID(), true, true);
    }

    public DbTable getCurrentVariantTable() {
        return currentVariantTableSchema.getTable();
    }

    public TableSchema getCurrentVariantTableSchema() {
        return currentVariantTableSchema;
    }

    private void setCurrentVariantTable() throws SQLException, RemoteException {
        currentVariantTableSchema =  MedSavantClient.CustomTablesAdapter.getCustomTableSchema(LoginController.sessionId, getCurrentVariantTableName());
    }

    public String getCurrentPatientTableName() throws RemoteException, SQLException {
        return MedSavantClient.PatientQueryUtilAdapter.getPatientTablename(LoginController.sessionId, currentProjectID);
    }

    public DbTable getCurrentPatientTable() {
        return currentPatientTableSchema.getTable();
    }

    public TableSchema getCurrentPatientTableSchema() {
        return currentPatientTableSchema;
    }

    private void setCurrentPatientTable() throws SQLException, RemoteException {
        DbColumn dbc = new DbColumn(null, "A", "B", 1);
        currentPatientTableSchema =  MedSavantClient.CustomTablesAdapter.getCustomTableSchema(LoginController.sessionId, getCurrentPatientTableName());
    }

    public AnnotationFormat[] getCurrentAnnotationFormats() throws SQLException, RemoteException {
        if (currentAnnotationFormats == null) {
            int[] annotationIds = MedSavantClient.AnnotationQueryUtilAdapter.getAnnotationIds(LoginController.sessionId, this.currentProjectID, ReferenceController.getInstance().getCurrentReferenceID());
            AnnotationFormat[] af = new AnnotationFormat[annotationIds.length+2];
            af[0] = VariantFormat.getDefaultAnnotationFormat();
            af[1] = VariantFormat.getCustomFieldAnnotationFormat(
                    MedSavantClient.ProjectQueryUtilAdapter.getCustomVariantFields(
                        LoginController.sessionId,
                        currentProjectID,
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        MedSavantClient.ProjectQueryUtilAdapter.getNewestUpdateId(
                            LoginController.sessionId,
                            currentProjectID,
                            ReferenceController.getInstance().getCurrentReferenceID(),
                            true)));
            for (int i = 0; i < annotationIds.length; i++) {
                af[i+2] = MedSavantClient.AnnotationQueryUtilAdapter.getAnnotationFormat(LoginController.sessionId, annotationIds[i]);
            }
            currentAnnotationFormats = af;
        }
        return currentAnnotationFormats;
    }

    public List<CustomField> getCurrentPatientFormat() throws RemoteException, SQLException {
        if (currentPatientFormat == null) {
            currentPatientFormat = MedSavantClient.PatientQueryUtilAdapter.getPatientFields(LoginController.sessionId, currentProjectID);
        }
        return currentPatientFormat;
    }

    public void setCurrentAnnotationFormats(AnnotationFormat[] formats) {
        currentAnnotationFormats = formats;
    }

    @Override
    public void referenceChanged(String referenceName) {
        try {
            setCurrentVariantTable();
            setCurrentAnnotationFormats(null);
        } catch (Throwable ex) {
            ClientMiscUtils.reportError("Error while switching reference: %s", ex);
        }
    }

    @Override
    public void referenceAdded(String name) {
    }

    @Override
    public void referenceRemoved(String name) {
    }

    /**
     * For the current project, set the best reference.
     */
    public void setDefaultReference() throws RemoteException, SQLException {
        List<String> references = MedSavantClient.ReferenceQueryUtilAdapter.getReferencesForProject(LoginController.sessionId, currentProjectID);
        ReferenceController.getInstance().setReference(references.get(references.size()-1));
    }
}
