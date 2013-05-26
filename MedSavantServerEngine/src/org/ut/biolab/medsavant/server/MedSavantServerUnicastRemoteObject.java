/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.shared.model.ProgressStatus;


/**
 * Base-class which provides some default shared code for our RMI server classes.
 *
 * @author mfiume
 */
public class MedSavantServerUnicastRemoteObject extends UnicastRemoteObject {

    private static final Log LOG = LogFactory.getLog(MedSavantServerUnicastRemoteObject.class);
    private static int CONNECT_PORT = 36800;
    private static int EXPORT_PORT = 36801;

    /**
     * Classes which implement checking for progress during lengthy operations can use this map.  Classes which don't do so can just
     * leave this map empty.
     */
    protected final Map<String, ProgressStatus> sessionProgresses = new HashMap<String, ProgressStatus>();

    public MedSavantServerUnicastRemoteObject() throws RemoteException {
        super(EXPORT_PORT);
    }

    public static int getListenPort() {
        return CONNECT_PORT;
    }

    public static int getExportPort() {
        return EXPORT_PORT;
    }

    public static void setListenPort(int port) {
        CONNECT_PORT = port;
    }

    /*public static void setExportPort(int port) {
        EXPORT_PORT = port;
    }*/

    /**
     * Call which client uses to check the progress of a lengthy process, and optionally to tell the server to cancel.
     *
     * @param sessID uniquely identifies the client session
     * @param userCancelled tells server that client wants to cancel
     */
    public ProgressStatus checkProgress(String sessID, boolean userCancelled) {
        synchronized (sessionProgresses) {
            if (userCancelled) {
                LOG.info("Setting progress-status to CANCELLED.");
                sessionProgresses.put(sessID, ProgressStatus.CANCELLED);
                return ProgressStatus.CANCELLED;
            } else {
                return sessionProgresses.get(sessID);
            }
        }
    }

    /**
     * Called by server-side code to update the progress for the given session, also checking for cancellation.
     * @param frac the progress from 0.0–1.0; 0.0 resets the progress, 1.0 indicates that it is finished, -1.0 indicates an indeterminate task
     */
    protected void makeProgress(String sessID, String msg, double frac) throws InterruptedException {
        synchronized (sessionProgresses) {

            if (frac == 0.0) {
                // As a side-effect, progress 0.0 always clears the cancelled flag.
                sessionProgresses.put(sessID, new ProgressStatus(msg, frac));
            } else {
                ProgressStatus status = sessionProgresses.get(sessID);
                if (status == ProgressStatus.CANCELLED) {
                    LOG.info("Operation cancelled by user.");
                    throw new InterruptedException();
                } else if (status == null || !status.message.equals(msg) || status.fractionCompleted != frac) {
                    sessionProgresses.put(sessID, new ProgressStatus(msg, frac));
                }
            }
        }
    }
}
