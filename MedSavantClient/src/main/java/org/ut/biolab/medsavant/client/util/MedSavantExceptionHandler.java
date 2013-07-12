package org.ut.biolab.medsavant.client.util;

import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 * @author mfiume
 */
public class MedSavantExceptionHandler {

    public static boolean handleSessionExpiredException(SessionExpiredException e) {               
        MedSavantClient.restart("Your session has expired.  Click OK to restart MedSavant.");
        return false;
    }
}
