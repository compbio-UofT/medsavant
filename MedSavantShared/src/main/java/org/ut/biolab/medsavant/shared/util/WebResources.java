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
    private static final String BASE_DOMAIN = "genomesavant.com";    
    private static final String BASE_WWW = "http://"+BASE_DOMAIN;
    public static final URL FEEDBACK_URL = NetworkUtils.getKnownGoodURL("mailto:feedback@"+BASE_DOMAIN+"?subject=MedSavant%20Feedback");
    public static final URL URL = NetworkUtils.getKnownGoodURL(BASE_WWW+"/p/medsavant");
    public static final URL USERGUIDE_URL = NetworkUtils.getKnownGoodURL(URL, "learn");

    public static final URL GENEMANIA_DATA_URL = NetworkUtils.getKnownGoodURL(BASE_WWW+"/serve/data/genemania/gmdata.zip");
    public static final URL CLIENT_SERVER_VERSION_COMPATIBILITY_URL = NetworkUtils.getKnownGoodURL(URL, "serve/version/compatibility/client_server_compatibility.xml");
    public static final URL DATABASE_SERVER_VERSION_COMPATIBILITY_URL = NetworkUtils.getKnownGoodURL(URL, "serve/version/compatibility/db_server_compatibility.xml");
    public static final URL APPSDK_CLIENT_VERSION_COMPATIBILITY_URL = NetworkUtils.getKnownGoodURL(URL, "serve/version/compatibility/appsdk_client_compatibility.xml");
    public static final URL LOG_USAGE_STATS_URL = NetworkUtils.getKnownGoodURL(URL, "scripts/logUsageStats.cgi");

    public static final URL BUGREPORT_URL = NetworkUtils.getKnownGoodURL(BASE_WWW+"/p/assets/include/form/both/bugreport-post.php");
    public static final URL FEEDBACK_FORM_URL = NetworkUtils.getKnownGoodURL(BASE_WWW+"/p/assets/include/form/both/feedbackreport-post.php");

    public static final URL REFGENE_HG18_URL = NetworkUtils.getKnownGoodURL(BASE_WWW+"/data/hg18/hg18.refGene.gz");
    public static final URL REFGENE_HG19_URL = NetworkUtils.getKnownGoodURL(BASE_WWW+"/data/medsavant/hg19/refGene.txt.gz");
    public static final String[] PLUGIN_REPOSITORY_URLS
            = new String[]{BASE_WWW+"/p/medsavant/serve/plugin/pluginDirectory.xml"};

    public static final URL JANNOVAR_HG19_SERFILE_URL = NetworkUtils.getKnownGoodURL(BASE_WWW+"/p/medsavant/serve/annotation/jannovar/refseq_hg19.ser");
    public static final URL SAVANT_ROOTREQUEST_URL = NetworkUtils.getKnownGoodURL(BASE_WWW+"/q/medsavant");

    //public_base: genomesavant.com/medsavant
    //server_base: genomesvant.com/serve    
    //userguide: public_base, learn
    //genemania: server_base, genemania
    //compatibility: server_base, compatibility
    //logusage: server_base, logging
    //bugreport: public_base, bugreport
    //feedbackform: public_base, feedback
    //refgene: server_base, refgene/hg18
    //jannovar: server_base, jannovar/hg19
    //savant: savant_base, medsavant
    
}
