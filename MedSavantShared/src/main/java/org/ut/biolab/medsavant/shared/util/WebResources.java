package org.ut.biolab.medsavant.shared.util;

import java.net.URL;

/**
 *
 * @author mfiume
 */
public class WebResources {

    /*
     * Website URLs
     */
    public static final URL URL = NetworkUtils.getKnownGoodURL("http://www.genomesavant.com/p/medsavant");
    public static final URL CLIENT_SERVER_VERSION_COMPATIBILITY_URL = NetworkUtils.getKnownGoodURL(URL, "serve/version/compatibility/client_server_compatibility.xml");
    public static final URL DATABASE_SERVER_VERSION_COMPATIBILITY_URL = NetworkUtils.getKnownGoodURL(URL, "serve/version/compatibility/db_server_compatibility.xml");
    public static final URL APPSDK_CLIENT_VERSION_COMPATIBILITY_URL = NetworkUtils.getKnownGoodURL(URL, "serve/version/compatibility/appsdk_client_compatibility.xml");
    public static final URL LOG_USAGE_STATS_URL = NetworkUtils.getKnownGoodURL(URL, "scripts/logUsageStats.cgi");

    
    public static final String[] PLUGIN_REPOSITORY_URLS =
            new String[]{"http://www.genomesavant.com/p/medsavant/serve/plugin/pluginDirectory.xml"};

}
