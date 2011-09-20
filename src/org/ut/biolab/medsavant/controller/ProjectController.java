package org.ut.biolab.medsavant.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.util.jobject.ProjectQueryUtil;

/**
 *
 * @author mfiume
 */
public class ProjectController {
    
    private final ArrayList<ProjectListener> projectListeners;

    public void removeProject(String projectName) {
        try {
        org.ut.biolab.medsavant.db.Manage.removeProject(projectName);
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
            org.ut.biolab.medsavant.db.Manage.addProject(projectName);
            ProjectController.getInstance().fireProjectAddedEvent(projectName);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int getProjectId(String projectName) throws SQLException {
        return org.ut.biolab.medsavant.db.util.jobject.ProjectQueryUtil.getProjectId(projectName);
    }

    public static interface ProjectListener {
        public void projectAdded(String projectName);
        public void projectRemoved(String projectName);
        public void projectChanged(String projectName);
    }
    
    private int currentProjectId;
    private int currentReferenceId;
    private String currentPatientTable;
    private String currentVariantTable;
    
    private static ProjectController instance;
    
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
    
    public void addProjectListener(ProjectListener l) {
        this.projectListeners.add(l);
    }
    
}
