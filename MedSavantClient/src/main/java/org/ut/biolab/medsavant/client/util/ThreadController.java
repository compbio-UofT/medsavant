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
package org.ut.biolab.medsavant.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.util.notification.VisibleMedSavantWorker;
import org.ut.biolab.medsavant.client.util.notification.VisibleMedSavantWorker.JobStatus;

/**
 * @author Andrew
 */
public class ThreadController {

    private static final Log LOG = LogFactory.getLog(ThreadController.class);
    private static ThreadController instance;
    private Map<String, List<MedSavantWorker>> workers = new HashMap<String, List<MedSavantWorker>>();

    public boolean areJobsRunning(){
        for(List<MedSavantWorker> list : workers.values()){            
            if(list != null){
                for(MedSavantWorker msw : list){
                    if(msw instanceof VisibleMedSavantWorker){
                        VisibleMedSavantWorker vmsw = (VisibleMedSavantWorker)msw;
                        if(vmsw.getStatus() == JobStatus.RUNNING){
                            return true;
                        }
                    }//else job is not visible through the Jobs menu, and hence not considered a 'job'.
                }
            }
        }
        return false;
    }
    
    public static ThreadController getInstance() {
        if (instance == null) {
            instance = new ThreadController();
        }
        return instance;
    }

    public synchronized void addWorker(String page, MedSavantWorker worker) {
        List<MedSavantWorker> list = this.workers.get(page);
        if (list == null) {
            this.workers.put(page, list = new ArrayList<MedSavantWorker>());
        }
        list.add(worker);
        // LOG.info(page + " now has " + list.size() + " workers.");
    }

    public synchronized void removeWorker(String page, MedSavantWorker worker) {
        List<MedSavantWorker> list = this.workers.get(page);
        if (list != null) {
            list.remove(worker);
            // LOG.info(page + " now has " + list.size() + " workers.");
        }
    }

    public synchronized void cancelWorkers(String page) {
        List<MedSavantWorker> list = this.workers.get(page);
        if (list != null) {
            // We null out the map entry because MedSavantWorker.done() calls removeWorker(), which will try to
            // concurrently modify
            // the list we're iterating over.
            this.workers.put(page, null);
            for (MedSavantWorker worker : list) {
                worker.cancel(true);
            }
        }
    }
    
    /**
     * Cancels ALL MedSavantWorker threads, except for the one passed as an
     * argument.  (argument can be null if caller is not a MedSavantWorker)  
     */
    public synchronized void cancelAllWorkers(MedSavantWorker except){        
        for(List<MedSavantWorker> pageWorkers : this.workers.values()){
            if(pageWorkers != null){
                for(MedSavantWorker worker : pageWorkers){
                    if(worker != except){
                        worker.cancel(true);
                    }
                }
            }
        }
    }
}
