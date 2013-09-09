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

package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;


/**
 *
 * @author Andrew
 */
public class ProjectDetails implements Serializable {

    private int projectID;
    private int referenceID;
    private int updateID;
    private boolean published;
    private String projectName;
    private String referenceName;
    private int[] annotationIDs;

    public ProjectDetails() {};

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
        return (annotationIDs != null) ? annotationIDs.length : 0;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public void setReferenceID(int referenceID) {
        this.referenceID = referenceID;
    }

    public void setUpdateID(int updateID) {
        this.updateID = updateID;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public void setAnnotationIDs(int[] annotationIDs) {
        this.annotationIDs = annotationIDs;
    }


}
