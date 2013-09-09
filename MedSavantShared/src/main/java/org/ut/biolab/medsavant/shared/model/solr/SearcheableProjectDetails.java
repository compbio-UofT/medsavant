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
package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.commons.lang.ArrayUtils;
import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;

/**
 *   Adapter class for mapping Solr documents to ProjectDetails objects.
 */
public class SearcheableProjectDetails {

    private ProjectDetails projectDetails;

    /**
     * Default constructor. Called by the Document Object Binder
     */
    public SearcheableProjectDetails() {
        projectDetails = new ProjectDetails();
    }

    /**
     * Initialize the class and set the ProjectDetails instance.
     * @param projectDetails            Inner ProjectDetails instance.
     */
    public SearcheableProjectDetails(ProjectDetails projectDetails) {
        this.projectDetails = projectDetails;
    }

    /**
     * Return the ProjectDetails instance.
      * @return                         The ProjectDetails instance.
     */
    public ProjectDetails getProjectDetails() {
        return projectDetails;
    }

    @Field("project_id")
    public void setProjectID(int projectID) {
        projectDetails.setProjectID(projectID);
    }

    @Field("reference_id")
    public void setReferenceID(int referenceID) {
        projectDetails.setReferenceID(referenceID);
    }

    @Field("update_id")
    public void setUpdateID(int updateID) {
        projectDetails.setUpdateID(updateID);
    }

    @Field("published")
    public void setPublished(boolean published) {
        projectDetails.setPublished(published);
    }

    @Field("name")
    public void setProjectName(String projectName) {
        projectDetails.setProjectName(projectName);
    }

    @Field("reference_name")
    public void setReferenceName(String referenceName) {
        projectDetails.setReferenceName(referenceName);
    }

    @Field("annotation_ids")
    public void setAnnotationIDs(Integer[] annotationIDs) {
        projectDetails.setAnnotationIDs(ArrayUtils.toPrimitive(annotationIDs));
    }

    public int getProjectID() {
        return projectDetails.getProjectID();
    }

    public int getReferenceID() {
        return projectDetails.getReferenceID();
    }

    public int getUpdateID() {
        return projectDetails.getUpdateID();
    }

    public boolean getPublished() {
        return projectDetails.isPublished();
    }

    public String getProjectName() {
        return projectDetails.getProjectName();
    }

    public String getReferenceName() {
        return projectDetails.getReferenceName();
    }

    public Integer[] getAnnotationIDs() {
        return ArrayUtils.toObject(projectDetails.getAnnotationIDs());
    }
}
