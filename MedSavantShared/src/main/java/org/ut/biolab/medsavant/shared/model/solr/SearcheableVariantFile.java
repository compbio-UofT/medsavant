package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;

import java.util.Date;
import java.util.UUID;

/**
 * Adapter class for mapping Solr documents to AnnotationLog objects.
 */
public class SearcheableVariantFile {

    private SimpleVariantFile variantFile;

    private UUID uuid;

    private int uploadId;
    private int fileId;
    private String name;
    private String date;
    private String user;

    public SearcheableVariantFile() {};

    public SearcheableVariantFile(SimpleVariantFile variantFile) {
        this.variantFile = variantFile;
        this.addUUID();
    }

    public SimpleVariantFile getVariantFile() {
        this.variantFile = new SimpleVariantFile(uploadId, fileId, name, date, user);
        return variantFile;
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

    @Field("name")
    public void setName(String name) {
        this.name = name;
    }

    @Field("date")
    public void setDate(String date) {
        this.date = date.toString();
    }

    @Field("user")
    public void setUser(String user) {
        this.user = user;
    }

    public int getUploadId() {
        return variantFile.getUploadId();
    }

    public int getFileId() {
        return variantFile.getFileId();
    }

    public String getName() {
        return variantFile.getName();
    }

    public String getDate() {
        return variantFile.getDate();
    }

    public String getUser() {
        return variantFile.getUser();
    }


    public UUID getUuid() {
        return uuid;
    }

    @Field("uuid")
    public void setUuid(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }
}
