/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
public class AnnotationLog implements Serializable {

    public static enum Action {ADD_VARIANTS, UPDATE_TABLE, REMOVE_VARIANTS};
    public static enum Status {STARTED, ERROR, PENDING, PUBLISHED};

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
            case REMOVE_VARIANTS:
                return 2;
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
            case 2:
                return Action.REMOVE_VARIANTS;
            default:
                return null;
        }
    }

    public static int statusToInt(Status status){
        switch(status){
            case STARTED:
                return 0;
            case ERROR:
                return 1;
            case PENDING:
                return 2;
            case PUBLISHED:
                return 3;
            default:
                return -1;
        }
    }

    public static Status intToStatus(int status){
        switch(status){
            case 0:
                return Status.STARTED;
            case 1:
                return Status.ERROR;
            case 2:
                return Status.PENDING;
            case 3:
                return Status.PUBLISHED;
            default:
                return null;
        }
    }
}
