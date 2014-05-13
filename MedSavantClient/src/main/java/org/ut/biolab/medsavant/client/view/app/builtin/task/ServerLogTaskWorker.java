/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.app.builtin.task;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.splash.LoginController;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.shared.model.GeneralLog;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author mfiume
 */
class ServerLogTaskWorker implements TaskWorker {

    public ServerLogTaskWorker() {
    }

    @Override
    public String getTaskName() {
        return "Server Log";
    }

    @Override
    public TaskStatus getCurrentStatus() {
        return TaskStatus.PERSISTENT;
    }

    @Override
    public List<GeneralLog> getLog() {
        List<GeneralLog> results;
        try {
            results = MedSavantClient.LogManager.getServerLogForUserWithSessionID(LoginController.getSessionID(), 0, 500);

        } catch (Exception ex) {
            results = new ArrayList<GeneralLog>();
            results.add(new GeneralLog("Error retrieving logs"));
            Logger.getLogger(ServerLogTaskWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
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
