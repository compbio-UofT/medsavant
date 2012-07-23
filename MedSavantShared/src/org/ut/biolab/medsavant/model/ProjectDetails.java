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


/**
 *
 * @author Andrew
 */
public class ProjectDetails implements Serializable {
    
    private final int projectID;
    private final int referenceID;
    private final int updateID;
    private final boolean published;
    private final String projectName;
    private final String referenceName;
    private final int[] annotationIDs;
    
    public ProjectDetails(int projID, int refID, int updID, boolean published, String projName, String refNam, int[] annIDs) {
        this.projectID = projID;
        this.referenceID = refID;
        this.updateID = updID;
        this.published = published;
        this.referenceName = refNam;
        this.annotationIDs = annIDs;
        this.projectName = projName;
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
    
    public int[] getAnnotationIDs(){
        return annotationIDs;
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
        return annotationIDs.length;
    }    
}
