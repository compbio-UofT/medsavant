/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static org.ut.biolab.medsavant.shared.model.MedSavantServerJobProgress.ScheduleStatus.FINISHED;
import static org.ut.biolab.medsavant.shared.model.MedSavantServerJobProgress.ScheduleStatus.NOT_STARTED;

public class MedSavantServerJobProgress implements Serializable{

    public enum ScheduleStatus {

        RUNNING_AS_LONGJOB {
                    @Override
                    public String toString() {
                        return "RUNNING_IN_QUEUE";
                    }
                },
        RUNNING_AS_SHORTJOB {
                    @Override
                    public String toString() {
                        return "RUNNING";
                    }
                },
        SCHEDULED_AS_SHORTJOB {
                    @Override
                    public String toString() {
                        return "ABOUT_TO_RUN";
                    }
                },
        SCHEDULED_AS_LONGJOB {
                    @Override
                    public String toString() {
                        return "QUEUED";
                    }
                },
        NOT_STARTED,
        FINISHED,
        CANCELLED
    };
    private final String userId;
    private final String jobName;
    private String message;
    private ScheduleStatus status;
    private Map<String, String> info;

    public List<MedSavantServerJobProgress> childJobProgresses;

    public MedSavantServerJobProgress(String userId, String jobName) {
        this.userId = userId;
        this.jobName = jobName;
        this.status = NOT_STARTED;
    }

    public String getUserId() {
        return userId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getMessage() {
        return message;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public Map<String, String> getInfo() {
        return info;
    }
    
    public void setInfo(String key, String val){
        if(info == null){
            info = new HashMap<String, String>();
        }
        info.put(key, val);
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
        if(this.status == FINISHED){
            setMessage("Done.");
        }
    }
    
    public void setMessage(String msg){
        this.message = msg;
    }

    public void addChildJobProgress(MedSavantServerJobProgress childJobProgress) {
        if (childJobProgresses == null) {
            childJobProgresses = new LinkedList<MedSavantServerJobProgress>();
        }
        childJobProgresses.add(childJobProgress);
    }
}
