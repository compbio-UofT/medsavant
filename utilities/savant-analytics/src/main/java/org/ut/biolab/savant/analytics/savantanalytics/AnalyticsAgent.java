package org.ut.biolab.savant.analytics.savantanalytics;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author mfiume
 */
public class AnalyticsAgent {

    static boolean sessionActive = false;
    private static String sessionID;
    private static String softwareName;
    private static String softwareVersion;
    private static Locale locale;
    private static Properties p = new Properties(System.getProperties());
    private static NameValuePair[] standardKeyValues;

    public static void onStartSession(String softwareName, String softwareVersion) {

        locale = Locale.getDefault();
        AnalyticsAgent.softwareName = softwareName;
        AnalyticsAgent.softwareVersion = softwareVersion;

        beginSession();
        log("SessionStart",false);
    }

    public static void onEndSession() {
        endSession();
    }

    public static void log(String message, boolean async) {
        log(nvp("msg", message),async);
    }

    public static void log(String message) {
        log(message, true);
    }

    public static void log(NameValuePair kvp, boolean async) {
        log(new NameValuePair[]{kvp},async);
    }

    public static void log(NameValuePair kvp) {
        log(kvp, true);
    }

    public static void log(final NameValuePair[] kvps, boolean async) {

        if (sessionActive) {
            if (async) {
                new Thread(new Runnable() {
                    public void run() {
                        doLog(kvps);
                    }
                }).start();
            } else {
                doLog(kvps);
            }
        }
    }

    private static void doLog(NameValuePair[] kvps) {
        try {
            HttpClient httpClient = new HttpClient();
            PostMethod post = new PostMethod("http://genomesavant.com/u/labs/usage/usage.php");
            NameValuePair[] data = ArrayUtils.addAll(kvps, standardKeyValues);
            data = ArrayUtils.addAll(data, dynamicKeyValues());
            post.setRequestBody(data);
            httpClient.executeMethod(post);
        } catch (Exception ex) {
        }
    }

    public static void log(NameValuePair[] kvps) {
        log(kvps, true);
    }

    private static void endSession() {
        log("SessionEnd",false);
        sessionID = null;
        sessionActive = false;
    }

    private static void beginSession() {
        sessionID = RandomStringUtils.random(20, "0123456789abcdefghijklmnopqrstuvwxyz");
        sessionActive = true;

        standardKeyValues = new NameValuePair[]{
            nvp("software-name", softwareName),
            nvp("session-id", sessionID),
            nvp("agent-version", getAnalyticsAgentVersion())
        };

        String address = "unknown";
        try {
            address = (InetAddress.getLocalHost()).toString();
        } catch (UnknownHostException ex) {
        }

        log(new NameValuePair[]{
                    nvp("system-java-version", System.getProperty("java.version")),
                    nvp("system-java-vendor", System.getProperty("java.vendor")),
                    nvp("system-os-arch", System.getProperty("os.arch")),
                    nvp("system-os-name", System.getProperty("os.name")),
                    nvp("system-os-version", System.getProperty("os.version")),
                    nvp("client-ip", address),
                    nvp("msg", "NewSession")
                });
    }

    private static NameValuePair nvp(String name, String value) {
        return new NameValuePair(name, value);
    }

    private static NameValuePair[] dynamicKeyValues() {
        return new NameValuePair[]{
                    nvp("client-time", (new Date()).toGMTString())
                };
    }
    private static final String UNDEFINED_VERSION = "";

    private static String getAnalyticsAgentVersion() {
        String version = UNDEFINED_VERSION;

        Package aPackage = AnalyticsAgent.class.getPackage();
        if (aPackage != null) {
            version = aPackage.getImplementationVersion();
            if (version == null) {
                version = aPackage.getSpecificationVersion();
            }
        }
        if (version == null) {
            version = UNDEFINED_VERSION;
        }

        return version;
    }
}
