package org.ut.biolab.medsavant.client.util;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import savant.api.util.DialogUtils;

/**
 * @author mfiume
 */
public class MedSavantExceptionHandler {

       public static boolean handleSessionExpiredException(SessionExpiredException e) {               
        MedSavantFrame.getInstance().dispose();
        DialogUtils.displayMessage("Your session has expired.  Click OK to close MedSavant.");
        MedSavantClient.quit();
        return false;
    }
}
