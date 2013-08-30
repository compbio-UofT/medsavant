package org.ut.biolab.mfiume.app;

import org.ut.biolab.mfiume.app.api.AppInstaller;

/**
 *
 * @author mfiume
 */
public class AppInstallUtils {

    public static AppInfo getAppWithName(AppInstaller installer, String name) {
        for (AppInfo i : installer.getInstallRegistry()) {
            if (i.getName().equals(name)) {
                return i;
            }
        }
        return null;
    }

}
