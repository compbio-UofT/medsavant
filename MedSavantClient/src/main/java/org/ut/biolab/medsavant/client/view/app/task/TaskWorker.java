/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.app.task;

import java.util.List;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;

/**
 *
 * @author mfiume
 */
public interface TaskWorker  {

    String getTaskName();
    TaskStatus getCurrentStatus();
    List<String> getLog();
    double getTaskProgress();
    void cancel();
    void addListener(Listener<TaskWorker> w);
    LaunchableApp getOwner();
    
    public enum TaskStatus {
        UNSTARTED {

            @Override
            public String toString() {
                return "Not started";
            }
            
        },
        INPROGRESS {

            @Override
            public String toString() {
                return "Running";
            }
            
        },
        FINISHED {

            @Override
            public String toString() {
                return "Done";
            }
            
        },
        CANCELLED {

            @Override
            public String toString() {
                return "Cancelled";
            }
            
        },
        ERROR {

            @Override
            public String toString() {
                return "Error";
            }
            
        },
    }
}
