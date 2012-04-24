/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class ProjectDetails implements Serializable {
    
    private int projectId;
    private int updateId;
    private int referenceId;
    private String projectName;
    private String referenceName;
    private String annotationIds;
    private boolean published;
    
    public ProjectDetails(int projectId, int referenceId, int updateId, boolean published, String projectName, String referenceName, String annotationIds){
        this.referenceId = referenceId;
        this.referenceName = referenceName;
        this.annotationIds = annotationIds;
        this.projectId = projectId;
        this.updateId = updateId;
        this.published = published;
        this.projectName = projectName;
    }

    public List<Integer> getAnnotationIds(){
        List<Integer> ids = new ArrayList<Integer>();
        if(annotationIds != null){
            for(String s : annotationIds.split(",")){
                if (s.isEmpty()) { continue; }
                ids.add(Integer.parseInt(s));
            }
        }
        return ids;
    }
    
    public int getProjectId() {
        return projectId;
    }

    public int getReferenceId() {
        return referenceId;
    }
    
    public int getUpdateId() {
        return updateId;
    }
    
    public boolean isPublished(){
        return published;
    }

    public String getReferenceName() {
        return referenceName;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public int getNumAnnotations() {
        int numAnnotations = 0;
        if (annotationIds != null && !annotationIds.isEmpty()) {
            numAnnotations = annotationIds.length() - annotationIds.replaceAll(",", "").length() + 1;
        }
        return numAnnotations;
    }
    
}
