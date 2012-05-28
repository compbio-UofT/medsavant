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

package org.ut.biolab.medsavant.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class ProjectDetails implements Serializable {
    
    private final int projectID;
    private final int updateID;
    private final int referenceID;
    private final String projectName;
    private final String referenceName;
    private final String annotationIDs;
    private final boolean published;
    
    public ProjectDetails(int projectId, int referenceId, int updateId, boolean published, String projectName, String referenceName, String annotationIds){
        this.referenceID = referenceId;
        this.referenceName = referenceName;
        this.annotationIDs = annotationIds;
        this.projectID = projectId;
        this.updateID = updateId;
        this.published = published;
        this.projectName = projectName;
    }

    public List<Integer> getAnnotationIds(){
        List<Integer> ids = new ArrayList<Integer>();
        if(annotationIDs != null){
            for(String s : annotationIDs.split(",")){
                if (s.isEmpty()) { continue; }
                ids.add(Integer.parseInt(s));
            }
        }
        return ids;
    }
    
    public int getProjectID() {
        return projectID;
    }

    public int getReferenceID() {
        return referenceID;
    }
    
    public int getUpdateID() {
        return updateID;
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
        if (annotationIDs != null && !annotationIDs.isEmpty()) {
            numAnnotations = annotationIDs.length() - annotationIDs.replaceAll(",", "").length() + 1;
        }
        return numAnnotations;
    }
    
}
