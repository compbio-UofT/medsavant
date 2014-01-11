/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.app.task;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;

/**
 *
 * @author mfiume
 */
interface TaskWorker  {

    String getTaskName();
    String getCurrentStatus();
    double getTaskProgress();
    void cancel();
    void addListener(Listener<TaskWorker> w);
    LaunchableApp getOwner();
}
