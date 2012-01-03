/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.settings;

import org.ut.biolab.medsavant.controller.SettingsController;

/**
 *
 * @author mfiume
 */
public class BrowserSettings {
    
    private static final String CHECKVERSION_KEY = "CHECKVERSION";
    private static final String COLLECTSTATS_KEY = "COLLECTSTATS";
    
    private static SettingsController settings = SettingsController.getInstance();
    
    /**
     * Does Savant need to check it's version number on startup?  Usually controlled by the
     * CHECKVERSION key, except in the case of beta releases, which always check.
     */
    public static boolean getCheckVersionOnStartup() {
        return VersionSettings.isBeta() || settings.getBoolean(CHECKVERSION_KEY, true);
    }

    public static boolean getCollectAnonymousUsage() {
        return settings.getBoolean(COLLECTSTATS_KEY, true);
    }
    
}
