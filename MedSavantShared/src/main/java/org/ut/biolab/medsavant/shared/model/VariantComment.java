/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author Andrew
 */
public class VariantComment implements Serializable {

    private int uploadId;
    private int fileId;
    private int variantId;
    private String user;
    private String description;
    private Timestamp timestamp;
    private final int projectId;
    private final int referenceId;

    public VariantComment(int projectId, int refId, int uploadId, int fileId, int variantId, String user, String description, Timestamp timestamp) {
        this.projectId = projectId;
        this.referenceId = refId;
        this.uploadId = uploadId;
        this.fileId = fileId;
        this.variantId = variantId;
        this.user = user;
        this.description = description;
        this.timestamp = timestamp;
    }


    public int getProjectId() {
        return projectId;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public String getDescription() {
        return description;
    }

    public int getFileId() {
        return fileId;
    }

    public int getUploadId() {
        return uploadId;
    }

    public String getUser() {
        return user;
    }

    public int getVariantId() {
        return variantId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        int hash = 23;
        hash = hash*37 + uploadId;
        hash = hash*37 + fileId;
        hash = hash*37 + variantId;
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof VariantComment)) return false;
        VariantComment other = (VariantComment) o;
        return this.uploadId == other.uploadId
                && this.fileId == other.fileId
                && this.variantId == other.variantId
                && (other.user == null || this.user == null || this.user.equals(other.user));
    }

    public Object[] toArray(int projectId, int referenceId){
        return new Object[]{
            projectId,
            referenceId,
            uploadId,
            fileId,
            variantId,
            user,
            description,
            timestamp
        };
    }

}
