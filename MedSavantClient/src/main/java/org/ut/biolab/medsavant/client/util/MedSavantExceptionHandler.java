package org.ut.biolab.medsavant.client.util;

import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author mfiume
 */
public class MedSavantExceptionHandler {

    public static boolean handleSessionExpiredException(SessionExpiredException e) {
        DialogUtils.displayMessage("Session expired");
        LoginController.getInstance().logout();
        return false;
    }
}
