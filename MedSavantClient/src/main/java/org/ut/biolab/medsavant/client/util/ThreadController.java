/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Andrew
 */
public class ThreadController {
    private static final Log LOG = LogFactory.getLog(ThreadController.class);

    private static ThreadController instance;

    private Map<String, List<MedSavantWorker>> workers = new HashMap<String, List<MedSavantWorker>>();

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
}
