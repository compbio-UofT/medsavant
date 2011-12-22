/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class ProjectDetails {
    
    private int referenceId;
    private String referenceName;
    private String annotationIds;
    
    public ProjectDetails(int referenceId, String referenceName, String annotationIds){
        this.referenceId = referenceId;
        this.referenceName = referenceName;
        this.annotationIds = annotationIds;
    }

    /*public String getAnnotationIds() {
        return annotationIds;
    }*/
    
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

    public int getReferenceId() {
        return referenceId;
    }

    public String getReferenceName() {
        return referenceName;
    }
    
    public int getNumAnnotations() {
        int numAnnotations = 0;
        if (annotationIds != null && !annotationIds.isEmpty()) {
            numAnnotations = annotationIds.length() - annotationIds.replaceAll(",", "").length() + 1;
        }
        return numAnnotations;
    }
    
}
