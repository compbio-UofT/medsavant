/*
 *    Copyright 2010-2011 University of Toronto
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

package org.ut.biolab.medsavant.db.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Some useful methods for performing network-related functions.
 *
 * @author vwilliams, tarkvara
 */
public class NetworkUtils {

    private static final int CONNECT_TIMEOUT = 30000; // 30s timeout for making connection
    private static final int READ_TIMEOUT = 30000;    // 30s timeout for reading data
    public static final int BUF_SIZE = 8192;         // 8kB buffer

    static {
        // Create a trust manager that does not validate certificate chains.
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception x) {
        }
    }

    /**
     * Open a stream for the given URL with the CONNECT_TIMEOUT and READ_TIMEOUT.
     * @throws IOException
     */
    public static InputStream openStream(URL url) throws IOException {
        return openStream(url, CONNECT_TIMEOUT, READ_TIMEOUT);
    }
    
    /**
     * Open a stream for the given URL with custom timeouts
     * @throws IOException
     */
    public static InputStream openStream(URL url, int connectTimeout, int readTimeout) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        return conn.getInputStream();
    }

    /**
     * Create a URL object from a string which we know to be a valid URL.  Avoids having
     * to catch a MalformedURLException which we know will never be thrown.  Intended
     * as the URL equivalent to <code>URL.create()</code>.
     */
    public static URL getKnownGoodURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException ignored) {
            throw new IllegalArgumentException();
        }
    }


    /**
     * Create a URL object from an existing URL and a string which we know to be a valid path.  Avoids having
     * to catch a MalformedURLException which we know will never be thrown.  Intended
     * as the URL equivalent to <code>URL.create()</code>.
     */
    public static URL getKnownGoodURL(URL base, String spec) {
        try {
            String baseStr = base.toString();
            if (!baseStr.endsWith("/")) {
                baseStr += "/";
            }
            return new URL(baseStr + spec);
        } catch (MalformedURLException ignored) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Synchronously download the given URL to the given destination directory.
     *
     * @param u the URL to be downloaded
     * @param destDir the destination directory
     * @param fileName the destination file within <code>destDir</code>; use <code>null</code> to infer the name from the URL
     * @return the downloaded file
     */
    public static File downloadFile(URL u, File destDir, String fileName) throws IOException {
        File f = new File(destDir, fileName != null ? fileName : MiscUtils.getFilenameFromPath(u.getPath()));

        InputStream in = NetworkUtils.openStream(u);
        OutputStream out = new FileOutputStream(f);
        byte[] buf = new byte[BUF_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
        }

        return f;
    }

    /**
     * Synchronously download a (small) file and read its contents to a String.
     *
     * @param u the URL to be downloaded
     * @return a string containing the contents of the URL
     */
    public static String downloadFile(URL u) throws IOException {

        StringBuilder result = new StringBuilder();

        InputStream in = NetworkUtils.openStream(u);
        byte[] buf = new byte[BUF_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buf)) != -1) {
            char [] r = (new String(buf)).toCharArray();
            result.append(r, 0, bytesRead);
        }

        return result.toString();
    }

}
