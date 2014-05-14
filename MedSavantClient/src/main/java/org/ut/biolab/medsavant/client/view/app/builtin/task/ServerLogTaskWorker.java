/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
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
import org.ut.biolab.medsavant.client.view.login.LoginController;
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
