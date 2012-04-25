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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

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
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
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

    public void removeVariantTable(int project_id, int ref_id) {
        try {
            MedSavantClient.ProjectQueryUtilAdapter.removeReferenceForProject(LoginController.sessionId, project_id,ref_id);
            fireProjectTableRemovedEvent(project_id,ref_id);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    public String getProjectName(int projectid) throws SQLException, RemoteException {
        return MedSavantClient.ProjectQueryUtilAdapter.getProjectName(LoginController.sessionId, projectid);
    }

    public boolean setProject(String projectName) {
        try {
            if (MedSavantClient.ProjectQueryUtilAdapter.containsProject(LoginController.sessionId, projectName)) {

                if (MedSavantClient.ProjectQueryUtilAdapter.containsProject(LoginController.sessionId, this.currentProjectName) &&
                        FilterController.hasFiltersApplied()) {
                    if (!DialogUtils.confirmChangeReference(true)) {
                        return false;
                    }
                }

                this.currentProjectId = this.getProjectId(projectName);
                this.currentProjectName = projectName;
                this.setCurrentPatientTable();
                ResultController.getInstance().setUpdateTotalNumRequired(true);
                this.fireProjectChangedEvent(projectName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
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

        ReferenceController.getInstance().addReferenceListener(this);
    }

    public static ProjectController getInstance() {
        if (instance == null) {
            instance = new ProjectController();
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
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getCurrentVariantSubTableName() {
        try {
            return MedSavantClient.ProjectQueryUtilAdapter.getVariantTablename(LoginController.sessionId, currentProjectId, ReferenceController.getInstance().getCurrentReferenceId(), true, true);
        } catch (SQLException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
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
            //this.currentTable = MedSavantClient.DBUtilAdapter.importTable(LoginController.sessionId, getCurrentTableName());
            this.currentVariantTableSchema =  MedSavantClient.CustomTablesAdapter.getCustomTableSchema(LoginController.sessionId, getCurrentVariantTableName());
        } catch (SQLException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getCurrentPatientTableName() {
        try {
            return MedSavantClient.PatientQueryUtilAdapter.getPatientTablename(LoginController.sessionId, currentProjectId);
        } catch (SQLException ex) {
            ClientMiscUtils.checkSQLException(ex);
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
            //this.currentPatientTable = MedSavantClient.DBUtilAdapter.importTable(LoginController.sessionId, getCurrentPatientTableName());
            this.currentPatientTableSchema =  MedSavantClient.CustomTablesAdapter.getCustomTableSchema(LoginController.sessionId, getCurrentPatientTableName());
        } catch (SQLException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public AnnotationFormat[] getCurrentAnnotationFormats() {
        if (currentAnnotationFormats == null) {
            try {
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
                for(int i = 0; i < annotationIds.length; i++) {
                    af[i+2] = MedSavantClient.AnnotationQueryUtilAdapter.getAnnotationFormat(LoginController.sessionId, annotationIds[i]);
                }
                currentAnnotationFormats = af;
            } catch (Exception ex) {
                currentAnnotationFormats = new AnnotationFormat[0];
                ex.printStackTrace();
                //Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return currentAnnotationFormats;
    }

    public List<CustomField> getCurrentPatientFormat() {
        if (currentPatientFormat == null) {
            try {
                currentPatientFormat = MedSavantClient.PatientQueryUtilAdapter.getPatientFields(LoginController.sessionId, currentProjectId);
            } catch (SQLException ex) {
                ClientMiscUtils.checkSQLException(ex);
                Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
            }
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

}