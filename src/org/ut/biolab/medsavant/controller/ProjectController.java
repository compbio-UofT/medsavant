package org.ut.biolab.medsavant.controller;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationFormat;
import org.ut.biolab.medsavant.db.util.query.AnnotationQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil;

/**
 *
 * @author mfiume
 */
public class ProjectController {
    
    private String currentProjectName;
    private int currentProjectId;
    private String currentReferenceName;

    private int currentReferenceId;
    private String currentPatientTable;
    private String currentVariantTable;
    private AnnotationFormat[] currentAnnotationFormats;
    
    private DbTable currentTable;
    
    private static ProjectController instance;
    
    
    private final ArrayList<ProjectListener> projectListeners;

    public void removeProject(String projectName) {
        try {
        ProjectQueryUtil.removeProject(projectName);
        fireProjectRemovedEvent(projectName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fireProjectRemovedEvent(String projectName) {
        ProjectController pc = getInstance();
        for (ProjectListener l : pc.projectListeners) {
            l.projectRemoved(projectName);
        }
    }

    public void addProject(String projectName) {
        try {
            ProjectQueryUtil.addProject(projectName);
            ProjectController.getInstance().fireProjectAddedEvent(projectName);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int getProjectId(String projectName) throws SQLException {
        return org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil.getProjectId(projectName);
    }

    public void removeVariantTable(int project_id, int ref_id) {
        try {
            org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil.removeReferenceForProject(project_id,ref_id);
            fireProjectTableRemovedEvent(project_id,ref_id);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getProjectName(int projectid) throws SQLException {
        return org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil.getProjectName(projectid);
    }

    public boolean setProject(String projectName) {
        try {
            if (ProjectQueryUtil.containsProject(projectName)) {
                
                if(FilterController.hasFiltersApplied()){
                    if(!confirmChangeReference(true)){
                        return false;
                    }
                }
                                
                this.currentProjectId = this.getProjectId(projectName);
                this.currentProjectName = projectName;                
                this.fireProjectChangedEvent(projectName);              
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    public boolean setReference(String refName){
        return setReference(refName, false);
    }
    
    public boolean setReference(String refName, boolean getConfirmation) {
        try {
            if (ReferenceQueryUtil.containsReference(refName)) {
                
                if(getConfirmation && FilterController.hasFiltersApplied()){
                    if(!confirmChangeReference(false)){
                        return false;
                    }
                }
                
                this.currentReferenceId = this.getReferenceId(refName);
                this.currentReferenceName = refName;
                setCurrentTable();
                setCurrentAnnotationFormats(null);
                this.fireReferenceChangedEvent(refName);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    private boolean confirmChangeReference(boolean isChangingProject){
        int result = JOptionPane.showConfirmDialog(
                null, 
                "<HTML>Changing the " + (isChangingProject ? "project" : "reference") + " will remove current filters.<BR>Are you sure you want to do this?</HTML>", 
                "Confirm", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    public int getCurrentReferenceId(){
        return this.currentReferenceId;
    }
    
    public String getCurrentReferenceName(){
        return this.currentReferenceName;
    }

    public int getReferenceId(String refName) throws SQLException {
        return org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil.getReferenceId(refName);
    }

    public int getCurrentProjectId() {
        return this.currentProjectId;
    }
    
    public String getCurrentProjectName() {
        return this.currentProjectName;
    }

    public int getNumVariantsInTable(int projectid, int refid) throws SQLException {
        return ProjectQueryUtil.getNumberOfRecordsInVariantTable(projectid,refid);
    }

    public static interface ProjectListener {
        public void projectAdded(String projectName);
        public void projectRemoved(String projectName);
        public void projectChanged(String projectName);

        public void projectTableRemoved(int projid, int refid);
        
        public void referenceChanged(String referenceName);
    }
    
   
    private ProjectController() {
        projectListeners = new ArrayList<ProjectListener>();
    }
    
    public static ProjectController getInstance() {
        if (instance == null) {
            instance = new ProjectController();
        }
        return instance;
    }
    
    public List<String> getProjectNames() throws SQLException {
        return ProjectQueryUtil.getProjectNames();
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
    
    public void fireReferenceChangedEvent(String referenceName){
        ProjectController pc = getInstance();
        for (ProjectListener l : pc.projectListeners){
            l.referenceChanged(referenceName);
        }
    }
    
    public void addProjectListener(ProjectListener l) {
        this.projectListeners.add(l);
    }
    
    public String getCurrentTableName(){
        return DBSettings.getVariantTableName(currentProjectId, currentReferenceId);
    }
    
    public DbTable getCurrentTable(){
        return currentTable;
    }
    
    private void setCurrentTable(){
        try {
            this.currentTable = DBUtil.importTable(getCurrentTableName());
        } catch (SQLException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public AnnotationFormat[] getCurrentAnnotationFormats(){
        if(currentAnnotationFormats == null){
            try {
                int[] annotationIds = AnnotationQueryUtil.getAnnotationIds(this.currentProjectId, this.currentReferenceId);
                AnnotationFormat[] af = new AnnotationFormat[annotationIds.length+1];
                af[0] = AnnotationFormat.getDefaultAnnotationFormat();
                for(int i = 0; i < annotationIds.length; i++){
                    af[i+1] = AnnotationQueryUtil.getAnnotationFormat(annotationIds[i]);
                }
                currentAnnotationFormats = af;
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return currentAnnotationFormats;
    }
    
    public void setCurrentAnnotationFormats(AnnotationFormat[] formats){
        this.currentAnnotationFormats = formats;
    }
    
}
