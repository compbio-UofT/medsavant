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

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.format.VariantFormat;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public class ProjectController implements ReferenceListener {
    private static final Log LOG = LogFactory.getLog(ProjectController.class);
    private String currentProjectName;
    private int currentProjectId;

    private AnnotationFormat[] currentAnnotationFormats;
    private List<CustomField> currentPatientFormat;

    private TableSchema currentVariantTableSchema;

    private TableSchema currentPatientTableSchema;

    private static ProjectController instance;


    private final ArrayList<ProjectListener> projectListeners;

    public void removeProject(final String projectName) {

        final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
                "Removing Project",
                projectName + " project is being removed. Please wait.",
                true);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    MedSavantClient.ProjectQueryUtilAdapter.removeProject(LoginController.sessionId, projectName);
                    fireProjectRemovedEvent(projectName);
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error removing project.", ex);
                }
                dialog.close();
            }
        };
        thread.start();
        dialog.setVisible(true);
    }

    public void fireProjectRemovedEvent(String projectName) {
        ProjectController pc = getInstance();
        for (ProjectListener l : pc.projectListeners) {
            l.projectRemoved(projectName);
        }
    }

    public int addProject(String projectName, List<CustomField> fields) {
        int projectid = -1;
        try {
            projectid = MedSavantClient.ProjectQueryUtilAdapter.addProject(LoginController.sessionId, projectName, fields);
            ProjectController.getInstance().fireProjectAddedEvent(projectName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return projectid;
    }

    public int getProjectId(String projectName) throws SQLException, RemoteException {
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

            this.currentProjectId = this.getProjectId(projectName);
            this.currentProjectName = projectName;
            this.setCurrentPatientTable();
            ResultController.getInstance().setUpdateTotalNumRequired(true);
            this.fireProjectChangedEvent(projectName);
        }
    }

    public int getCurrentProjectId() {
        return this.currentProjectId;
    }

    public String getCurrentProjectName() {
        return this.currentProjectName;
    }

    public int getNumVariantsInTable(int projectid, int refid) throws SQLException, RemoteException {
        return MedSavantClient.ProjectQueryUtilAdapter.getNumberOfRecordsInVariantTable(LoginController.sessionId, projectid,refid);
    }

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

    public List<String> getProjectNames() throws SQLException, RemoteException {
        if (MedSavantClient.ProjectQueryUtilAdapter == null) { return new ArrayList<String>(); }
        return MedSavantClient.ProjectQueryUtilAdapter.getProjectNames(LoginController.sessionId);
    }

    public void fireProjectAddedEvent(String projectName) {
        ProjectController pc = getInstance();
        for (ProjectListener l : pc.projectListeners) {
            l.projectAdded(projectName);
        }
    }

    public void fireProjectChangedEvent(String projectName) {
        ProjectController pc = getInstance();
        for (ProjectListener l : pc.projectListeners) {
            l.projectChanged(projectName);
        }
    }

    public void fireProjectTableRemovedEvent(int projid, int refid) {
        ProjectController pc = getInstance();
        for (ProjectListener l : pc.projectListeners) {
            l.projectTableRemoved(projid, refid);
        }
    }



    public void addProjectListener(ProjectListener l) {
        this.projectListeners.add(l);
    }

    public String getCurrentVariantTableName() {
        try {
            return MedSavantClient.ProjectQueryUtilAdapter.getVariantTablename(LoginController.sessionId, currentProjectId, ReferenceController.getInstance().getCurrentReferenceId(), true);
        } catch (SQLException ex) {
            LOG.error("Error getting variant table name.", ex);
        } catch (RemoteException ex) {
            LOG.error("Error getting variant table name.", ex);
        }
        return null;
    }

    public String getCurrentVariantSubTableName() {
        try {
            return MedSavantClient.ProjectQueryUtilAdapter.getVariantTablename(LoginController.sessionId, currentProjectId, ReferenceController.getInstance().getCurrentReferenceId(), true, true);
        } catch (SQLException ex) {
            LOG.error("Error getting current variant sub-table name.", ex);
        } catch (RemoteException ex) {
            LOG.error("Error getting current variant sub-table name.", ex);
        }
        return null;
    }

    public DbTable getCurrentVariantTable() {
        return currentVariantTableSchema.getTable();
    }

    public TableSchema getCurrentVariantTableSchema() {
        return currentVariantTableSchema;
    }

    private void setCurrentVariantTable() {
        try {
            currentVariantTableSchema =  MedSavantClient.CustomTablesAdapter.getCustomTableSchema(LoginController.sessionId, getCurrentVariantTableName());
        } catch (SQLException ex) {
            LOG.error("Error setting current variant table.", ex);
        } catch (RemoteException ex) {
            LOG.error("Error setting current variant table.", ex);
        }
    }

    public String getCurrentPatientTableName() throws RemoteException, SQLException {
        return MedSavantClient.PatientQueryUtilAdapter.getPatientTablename(LoginController.sessionId, currentProjectId);
    }

    public DbTable getCurrentPatientTable() {
        return currentPatientTableSchema.getTable();
    }

    public TableSchema getCurrentPatientTableSchema() {
        return currentPatientTableSchema;
    }

    private void setCurrentPatientTable() {
        try {

            DbColumn dbc = new DbColumn(null, "A", "B", 1);
            currentPatientTableSchema =  MedSavantClient.CustomTablesAdapter.getCustomTableSchema(LoginController.sessionId, getCurrentPatientTableName());
        } catch (SQLException ex) {
            LOG.error("Error setting current patient table.", ex);
        } catch (RemoteException ex) {
            LOG.error("Error setting current patient table.", ex);
        }
    }

    public AnnotationFormat[] getCurrentAnnotationFormats() throws RemoteException, SQLException {
        if (currentAnnotationFormats == null) {
            int[] annotationIds = MedSavantClient.AnnotationQueryUtilAdapter.getAnnotationIds(LoginController.sessionId, this.currentProjectId, ReferenceController.getInstance().getCurrentReferenceId());
            AnnotationFormat[] af = new AnnotationFormat[annotationIds.length+2];
            af[0] = VariantFormat.getDefaultAnnotationFormat();
            af[1] = VariantFormat.getCustomFieldAnnotationFormat(
                    MedSavantClient.ProjectQueryUtilAdapter.getCustomVariantFields(
                        LoginController.sessionId,
                        currentProjectId,
                        ReferenceController.getInstance().getCurrentReferenceId(),
                        MedSavantClient.ProjectQueryUtilAdapter.getNewestUpdateId(
                            LoginController.sessionId,
                            currentProjectId,
                            ReferenceController.getInstance().getCurrentReferenceId(),
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
            currentPatientFormat = MedSavantClient.PatientQueryUtilAdapter.getPatientFields(LoginController.sessionId, currentProjectId);
        }
        return currentPatientFormat;
    }

    public void setCurrentAnnotationFormats(AnnotationFormat[] formats) {
        currentAnnotationFormats = formats;
    }

    @Override
    public void referenceChanged(String referenceName) {
        setCurrentVariantTable();
        setCurrentAnnotationFormats(null);
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
        List<String> references = MedSavantClient.ReferenceQueryUtilAdapter.getReferencesForProject(LoginController.sessionId, currentProjectId);
        ReferenceController.getInstance().setReference(references.get(references.size()-1));
    }
}
