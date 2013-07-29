package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;

/**
 *   Adapter class for mapping Solr documents to ProjectDetails objects.
 */
public class SearcheableProjectDetails {

    private ProjectDetails projectDetails;

    private int projectID;
    private int referenceID;
    private int updateID;
    private boolean published;
    private String projectName;
    private String referenceName;
    private int[] annotationIDs;

    /**
     * Default constructor. Called by the Document Object Binder
     */
    public SearcheableProjectDetails() {}

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
        this.projectDetails = new ProjectDetails(projectID, referenceID, updateID, published, projectName, referenceName, annotationIDs);
        return projectDetails;
    }

    @Field("project_id")
    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    @Field("reference_id")
    public void setReferenceID(int referenceID) {
        this.referenceID = referenceID;
    }

    @Field("update_id")
    public void setUpdateID(int updateID) {
        this.updateID = updateID;
    }

    @Field("published")
    public void setPublished(boolean published) {
        this.published = published;
    }

    @Field("name")
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Field("reference_name")
    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    @Field("annotation_ids")
    public void setAnnotationIDs(int[] annotationIDs) {
        this.annotationIDs = annotationIDs;
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

    public boolean isPublished() {
        return published;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public int[] getAnnotationIDs() {
        return annotationIDs;
    }
}
