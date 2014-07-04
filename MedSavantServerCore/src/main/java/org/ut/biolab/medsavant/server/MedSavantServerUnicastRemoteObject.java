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
package org.ut.biolab.medsavant.server;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
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
    
    
    /**
     * Classes which implement checking for progress during lengthy operations can use this map.  Classes which don't do so can just
     * leave this map empty.
     */
    protected final Map<String, ProgressStatus> sessionProgresses = new HashMap<String, ProgressStatus>();
   
    public MedSavantServerUnicastRemoteObject() throws RemoteException{
        super(MedSavantServerEngine.getInstance().getExportPort(), MedSavantServerEngine.getInstance().getDefaultClientSocketFactory(), MedSavantServerEngine.getInstance().getDefaultServerSocketFactory());
    }
    /*
    public MedSavantServerUnicastRemoteObject() throws RemoteException {
        super(EXPORT_PORT, MedSavantServerEngine.getDefaultClientSocketFactory(), MedSavantServerEngine.getDefaultServerSocketFactory());
        
        //super(EXPORT_PORT, new SslRMIClientSocketFactory(), MedSavantServerEngine.getServerSocketFactory());
        //new SslRMIServerSocketFactory(null, null, MedSavantServerEngine.REQUIRE_CLIENT_AUTH));
    }
    */
    
    

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
