/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.model;

import java.io.Serializable;
import java.sql.Timestamp;



/**
 *
 * @author Andrew
 */
public class AnnotationLog implements Serializable {
    
    public static enum Action {ADD_VARIANTS, UPDATE_TABLE};
    public static enum Status {PREPROCESS, PENDING, INPROGRESS, ERROR, COMPLETE};
    
    private String projectName;
    private String referenceName;
    private Action action;
    private Status status;
    private Timestamp timestamp;
    private String user;
    private Integer uploadId;
    
    public AnnotationLog(String projectName, String referenceName, Action action, Status status, Timestamp timestamp, String user, Integer uploadId) {
        this.projectName = projectName;
        this.referenceName = referenceName;
        this.action = action;
        this.status = status;
        this.timestamp = timestamp;
        this.user = user;
        this.uploadId = uploadId;
    }

    public Action getAction() {
        return action;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public Status getStatus() {
        return status;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public Integer getUploadId() {
        return uploadId;
    }

    public String getUser() {
        return user;
    }
    
    public static int actionToInt(Action action){
        switch(action){
            case UPDATE_TABLE:
                return 0;
            case ADD_VARIANTS:
                return 1;
            default:
                return -1;
        }
    }

    public static Action intToAction(int action){
        switch(action){
            case 0:
                return Action.UPDATE_TABLE;
            case 1:
                return Action.ADD_VARIANTS;
            default:
                return null;
        }
    }

    public static int statusToInt(Status status){
        switch(status){
            case PREPROCESS:
                return 0;
            case PENDING:
                return 1;
            case INPROGRESS:
                return 2;
            case ERROR:
                return 3;
            case COMPLETE:
                return 4;
            default:
                return -1;
        }
    }

    public static Status intToStatus(int status){
        switch(status){
            case 0:
                return Status.PREPROCESS;
            case 1:
                return Status.PENDING;
            case 2:
                return Status.INPROGRESS;
            case 3:
                return Status.ERROR;
            case 4:
                return Status.COMPLETE;
            default:
                return null;
        }
    }
}
