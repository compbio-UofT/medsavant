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
package org.ut.biolab.medsavant.client.view.app.builtin.task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.shared.model.GeneralLog;
import org.ut.biolab.medsavant.shared.model.MedSavantServerJobProgress;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author jim
 */
public class ServerJobMonitorTaskWorker implements TaskWorker {

    private boolean stop = false;
    
    public ServerJobMonitorTaskWorker() {
    }

    @Override
    public String getTaskName() {
        return "Server Job Monitor";
    }

    @Override
    public TaskStatus getCurrentStatus() {
        if(stop){
            return TaskStatus.FINISHED;
        }else{
            return TaskStatus.PERSISTENT_AUTOREFRESH;
        }        
    }

    /*
     //works for children up to 16 deep, after that it flattens.
     private void updateMap(Map<Integer, Map<Integer, String>> km, List<MedSavantServerJobProgress> mjps, int level) {
     if(level > 16){
     level = 16;
     }
     int jid = 0;
     for (MedSavantServerJobProgress mjp : mjps) {
     ScheduleStatus status = mjp.getStatus();
     rank += status.ordinal()+1<<(16-level);
            
     //int rank = status.ordinal()<<(16-level);
     Map<Integer, String> jm = km.get(rank);
     if (jm == null) {
     jm = new TreeMap<Integer, String>();
     }

            
     String tabStr = StringUtils.repeat("    ", level);
     jm.put(jid, tabStr + "(" + status + ") " + jid + ". " + mjp.getJobName() + " - " + mjp.getMessage());
     km.put(rank, jm);
     if(mjp.childJobProgresses != null && !mjp.childJobProgresses.isEmpty()){
     updateMap(km, mjp.childJobProgresses, level+1);
     }
     jid++;
     }
     }*/
    private List<GeneralLog> getSortedChildren(List<MedSavantServerJobProgress> mjps, int level) {
        List<GeneralLog> output = new LinkedList<GeneralLog>();
        Map<Integer, List<MedSavantServerJobProgress>> m = new TreeMap<Integer, List<MedSavantServerJobProgress>>();
        for (MedSavantServerJobProgress p : mjps) {
            List<MedSavantServerJobProgress> l = m.get(p.getStatus().ordinal());
            if (l == null) {
                l = new LinkedList<MedSavantServerJobProgress>();
            }
            l.add(p);
            m.put(p.getStatus().ordinal(), l);
        }
        final String tabStr = StringUtils.repeat("     ", level);
        int jid = 0;
        for (List<MedSavantServerJobProgress> mjpList : m.values()) {
            for (MedSavantServerJobProgress mjp : mjpList) {
                String msg = mjp.getMessage();
                if (msg == null) {
                    msg = "";
                } else {
                    msg = " - " + msg;
                }
                output.add(new GeneralLog(null, tabStr + "(" + mjp.getStatus() + ") " + jid + ". " + mjp.getJobName() + msg, null));
                if (mjp.childJobProgresses != null && !mjp.childJobProgresses.isEmpty()) {
                    output.addAll(getSortedChildren(mjp.childJobProgresses, level + 1));
                }
                jid++;
            }
        }
        return output;
    }

    private List<GeneralLog> logout(SessionExpiredException see) {
        List<GeneralLog> nl = new ArrayList<GeneralLog>(1);
        nl.add(new GeneralLog("Session Expired - please quit and login again"));
        MedSavantExceptionHandler.handleSessionExpiredException(see);
        return nl; //maybe unreachable, depending on implementation of above.
    }

    @Override
    public List<GeneralLog> getLog() {
        if(stop){
            return new ArrayList<GeneralLog>();
        }
        
        String sessionId = LoginController.getSessionID();
        try {
            List<MedSavantServerJobProgress> mjps = MedSavantClient.LogManager.getJobProgressForUserWithSessionID(sessionId);
            if (mjps == null) {
                return new ArrayList<GeneralLog>(1);
            } else {
                return getSortedChildren(mjps, 0);
            }
        } catch (SessionExpiredException see) {
            stop = true;
            return logout(see);
        } catch (Exception ex) {
            stop = true;
            //check if we're still logged in.
            try {
                MedSavantClient.SessionManager.testConnection(sessionId);
            } catch (SessionExpiredException see) {
                return logout(see);
            } catch (Exception ex2) {

            }          
            String s = "Error retrieving task information";
            List<GeneralLog> r = new ArrayList<GeneralLog>(1);
            r.add(new GeneralLog(null, s, null));
            Logger.getLogger(ServerJobMonitorTaskWorker.class.getName()).log(Level.SEVERE, null, ex);
            return r;
        }
    }

    @Override
    public double getTaskProgress() {
        return 0.0;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addListener(Listener<TaskWorker> w) {
    }

    @Override
    public LaunchableApp getOwner() {
        return null;
    }
}
