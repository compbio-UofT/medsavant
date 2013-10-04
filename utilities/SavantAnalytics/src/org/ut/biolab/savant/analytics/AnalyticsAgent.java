package org.ut.biolab.savant.analytics;

import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 *
 * @author mfiume
 */
class AnalyticsAgent {

    static boolean sessionActive = false;
    private static String sessionID;
    private static String softwareName;
    private static String softwareVersion;
    private static Locale locale;

    static void onStartSession(String softwareName, String softwareVersion) {

        locale = Locale.getDefault();
        AnalyticsAgent.softwareName = softwareName;
        AnalyticsAgent.softwareVersion = softwareVersion;


        beginSession();
        log("SessionStart");
    }

    private static void log(String message) {
        if (sessionActive) {
            PostMethod post = new PostMethod("http://jakarata.apache.org/");
            NameValuePair[] data = {
                new NameValuePair("softname", softwareName),
                new NameValuePair("password", "bloggs")
            };
            post.setRequestBody(data);
            // execute method and handle any error responses.
            InputStream in = post.getResponseBodyAsStream();
            // handle response.

        }
    }

    private static void beginSession() {
        sessionID = "fetched-from-server";
        sessionActive = true;
        log("Session started");
    }

    private static void endSession() {
        sessionID = null;
        sessionActive = false;
        log("Session Ended");
    }
}
