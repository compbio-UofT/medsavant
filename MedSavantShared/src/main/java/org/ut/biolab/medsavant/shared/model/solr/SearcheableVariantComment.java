package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.VariantComment;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Comments about a certain variant.
 */
public class SearcheableVariantComment {

    private UUID uuid;
    private int uploadId;
    private int fileId;
    private int variantId;
    private String user;
    private String description;
    private Timestamp timestamp;
    private int projectId;
    private int referenceId;

    private VariantComment variantComment;

    public SearcheableVariantComment() {

    }
    public SearcheableVariantComment(VariantComment variantComment) {
        this.variantComment = variantComment;
        this.addUUID();
    }

    public VariantComment getVariantComment() {
        variantComment =  new VariantComment(projectId, referenceId, uploadId, fileId, variantId, user, description, timestamp);
        return variantComment;
    }

    private void addUUID() {
        this.uuid = UUID.randomUUID();
    }

    @Field("upload_id")
    public void setUploadId(int uploadId) {
        this.uploadId = uploadId;
    }

    @Field("file_id")
    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    @Field("variant_id")
    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    @Field("user")
    public void setUser(String user) {
        this.user = user;
    }

    @Field("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @Field("timestamp")
    public void setTimestamp(Date timestamp) {
        //Solr does not support java.sql.Timestamp
        this.timestamp =  new Timestamp(timestamp.getTime());
    }

    @Field("project_id")
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    @Field("reference_id")
    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    @Field("uuid")
    public void setUuid(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public int getUploadId() {
        return variantComment.getUploadId();
    }

    public int getFileId() {
        return variantComment.getFileId();
    }

    public int getVariantId() {
        return variantComment.getVariantId();
    }

    public String getUser() {
        return variantComment.getUser();
    }

    public String getDescription() {
        return variantComment.getDescription();
    }

    public Timestamp getTimestamp() {
        return variantComment.getTimestamp();
    }

    public int getProjectId() {
        return variantComment.getProjectId();
    }

    public int getReferenceId() {
        return variantComment.getReferenceId();
    }

    public UUID getUuid() {
        return uuid;
    }

}
