/*
 *    Copyright 2009-2011 University of Toronto
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
package org.ut.biolab.medsavant.settings;

import java.net.URL;
import org.ut.biolab.medsavant.controller.SettingsController;


/**
 *
 * @author mfiume
 */
public class BrowserSettings {
    private static final String CHECKVERSION_KEY = "CHECKVERSION";
    private static final String COLLECTSTATS_KEY = "COLLECTSTATS";

    /*
     * Website URLs
     */
    public static final URL URL = NetworkUtils.getKnownGoodURL("http://www.genomesavant.com/medsavant");
    public static final URL VERSION_URL = NetworkUtils.getKnownGoodURL(URL, "serve/version/version.xml");
    public static final URL PLUGIN_URL = NetworkUtils.getKnownGoodURL(URL, "serve/plugin/plugin.xml");
    public static final URL LOG_USAGE_STATS_URL = NetworkUtils.getKnownGoodURL(URL, "scripts/logUsageStats.cgi");

    public static final String VERSION = "1.0.0";
    public static String BUILD = "beta";

    private static SettingsController settings = SettingsController.getInstance();

    /**
     * Is this version a beta release?
     */
    public static boolean isBeta() {
        return BUILD.equals("beta");
    }

    /**
     * Does Savant need to check it's version number on startup?  Usually controlled by the
     * CHECKVERSION key, except in the case of beta releases, which always check.
     */
    public static boolean getCheckVersionOnStartup() {
        return isBeta() || settings.getBoolean(CHECKVERSION_KEY, true);
    }

    public static boolean getCollectAnonymousUsage() {
        return settings.getBoolean(COLLECTSTATS_KEY, true);
    }
}
