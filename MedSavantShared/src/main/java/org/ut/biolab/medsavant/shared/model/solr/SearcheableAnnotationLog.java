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

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Adapter class for mapping Solr documents to AnnotationLog objects.
 */
public class SearcheableAnnotationLog {

    private AnnotationLog log;

    private String projectName;
    private String referenceName;
    private AnnotationLog.Action action;
    private AnnotationLog.Status status;
    private Timestamp timestamp;
    private String user;
    private Integer uploadId;

    private UUID uuid;

    public SearcheableAnnotationLog() {};

    public SearcheableAnnotationLog(AnnotationLog log) {
        this.log = log;
        this.addUUID();
    }

    private void addUUID() {
        this.uuid = UUID.randomUUID();
    }

    @Field("project_name")
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Field("reference_name")
    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    @Field("action")
    public void setAction(String action) {
        this.action = AnnotationLog.Action.valueOf(action);
    }

    @Field("status")
    public void setStatus(String status) {
        this.status = AnnotationLog.Status.valueOf(status);
    }

    @Field("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = new Timestamp(timestamp.getTime());
    }

    @Field("user")
    public void setUser(String user) {
        this.user = user;
    }

    @Field("upload_id")
    public void setUploadId(Integer uploadId) {
        this.uploadId = uploadId;
    }

    public AnnotationLog getLog() {
        this.log = new AnnotationLog(projectName, referenceName, action, status, timestamp, user, uploadId);

        return log;
    }

    public String getProjectName() {
        return log.getProjectName();
    }

    public String getReferenceName() {
        return log.getReferenceName();
    }

    public AnnotationLog.Action getAction() {
        return log.getAction();
    }

    public AnnotationLog.Status getStatus() {
        return log.getStatus();
    }

    public Timestamp getTimestamp() {
        return log.getTimestamp();
    }

    public String getUser() {
        return log.getUser();
    }

    public Integer getUploadId() {
        return log.getUploadId();
    }

    public UUID getUuid() {
        return uuid;
    }
}
