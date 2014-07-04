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
package org.medsavant.api.common.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.ScheduleStatus;
import static org.medsavant.api.common.ScheduleStatus.FINISHED;
import static org.medsavant.api.common.ScheduleStatus.NOT_STARTED;

public class JobProgressMonitorImpl implements Serializable, JobProgressMonitor{

    
    private final String userId;
    private final String jobName;
    private String message;
    private ScheduleStatus status;
    private Map<String, String> info;

    private List<JobProgressMonitor> childJobProgresses;

    public JobProgressMonitorImpl(String userId, String jobName) {
        this.userId = userId;
        this.jobName = jobName;
        this.status = NOT_STARTED;
    }

    @Override
    public String getUserName() {
        return userId;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ScheduleStatus getStatus() {
        return status;
    }

    @Override
    public Map<String, String> getInfo() {
        return info;
    }
    
    @Override
    public void setInfo(String key, String val){
        if(info == null){
            info = new HashMap<String, String>();
        }
        info.put(key, val);
    }

    @Override
    public void setStatus(ScheduleStatus status) {
        this.status = status;
        if(this.status == FINISHED){
            setMessage("Done.");
        }
    }
    
    @Override
    public void setMessage(String msg){
        this.message = msg;
    }

    @Override
    public void addChildJobMonitor(JobProgressMonitor childJobProgress) {
        if (childJobProgresses == null) {
            childJobProgresses = new LinkedList<JobProgressMonitor>();
        }
        childJobProgresses.add(childJobProgress);
    }
    
    @Override
    public List<JobProgressMonitor> getChildJobMonitors(){
        return this.childJobProgresses;
    }

    @Override
    public void clearChildJobMonitors() {
        childJobProgresses.clear();
        childJobProgresses = null;
    }

    @Override
    public boolean hasChildJobMonitors() {
        return (childJobProgresses != null && !childJobProgresses.isEmpty());
        
    }

    @Override
    public boolean removeChildJobMonitor(JobProgressMonitor child) {
        if(childJobProgresses != null){
            return childJobProgresses.remove(child);
        }        
        return false;
    }
     
    
}
