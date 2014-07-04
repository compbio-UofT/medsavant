/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

import java.util.List;
import java.util.Map;

/**
 *
 * @author jim
 */
public interface JobProgressMonitor {           
    public String getUserName();        
    public String getJobName();        
    public String getMessage();        
    public ScheduleStatus getStatus();        
    public Map<String, String> getInfo();        
    public void setInfo(String key, String val);        
    public void setStatus(ScheduleStatus status);     
    public void setMessage(String msg);   
    public List<JobProgressMonitor> getChildJobMonitors();
    public void addChildJobMonitor(JobProgressMonitor childJobProgress);
    public void clearChildJobMonitors();
    public boolean hasChildJobMonitors();
    public boolean removeChildJobMonitor(JobProgressMonitor child);
        
}
