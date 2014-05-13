/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.ut.biolab.medsavant.shared.util.VersionSettings;
import org.ut.biolab.medsavant.shared.util.WebResources;

/**
 *
 * @author mfiume
 */
public class BugReport {

    //public static String bugreportURL = "http://www.genomesavant.com/p/assets/include/form/both/bugreport-post.php";
    //public static String feedbackreportURL = "http://www.genomesavant.com/p/assets/include/form/both/feedbackreport-post.php";

    public static boolean reportBug(String tool, String version, String name, String email, String institute, String problem, Throwable t) throws UnsupportedEncodingException {

        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair("tool", tool));
            params.add(new NameValuePair("name", name));
            params.add(new NameValuePair("email", email));
            params.add(new NameValuePair("institution", institute));
            params.add(new NameValuePair("problem", problem));
            params.add(new NameValuePair("exception", getStackTrace(t)));
            params.add(new NameValuePair("clientinfo",
                    kvp("program-version",version) +
                    ", " +
                    kvp("java-version", getJDKVersion()) +
                    ", " +
                    kvp("os", getOS()) +
                    ", " +
                    kvp("time",(new Date()).toLocaleString())));

            /*String params =
                      "tool=" + tool
                    + "&name=" + name
                    + "&email=" + email
                    + "&institution=" + institute
                    + "&problem=" + problem
                    + "&exception=" + getStackTrace(t)
                    + "&clientinfo=" + kvp("program-version",version) + ", " + kvp("java-version", getJDKVersion()) + ", " + kvp("os", getOS()) + ", " + kvp("time",(new Date()).toLocaleString());
*/
            postRequest(WebResources.BUGREPORT_URL, params);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String getStackTrace(Throwable t) {
        if (t == null) {
            return "";
        }
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        t.printStackTrace(printWriter);
        return result.toString();
    }

    private static String kvp(String k, String v) {
        return k + "=" + v;
    }

    private static String getJDKVersion() {
        return getProperty("java.version");
    }

    private static String getOS() {
        return getProperty("os.name") + " " + getProperty("os.arch") + " " + getProperty("os.version");
    }

    private static String getProperty(String propertyName) {
        try {
            String value = System.getProperty(propertyName);
            if (value == null) {
                return "unknown";
            } else {
                return value;
            }
        } catch (SecurityException e) {
            return "unknown";
        }
    }

    /*private static void postRequest(URL url, String params) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(params);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(
                                    connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);
        in.close();

        connection.disconnect();
    }*/

    private static void postRequest(URL url, List<NameValuePair> params) throws IOException{
        HttpClient hc = new HttpClient();
        NameValuePair[] data = new NameValuePair[params.size()];
        PostMethod post = new PostMethod(url.toString());
        post.setRequestBody(params.toArray(data));
        hc.executeMethod(post);
        BufferedReader in =  new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            System.out.println(inputLine);
        }
        in.close();
        post.releaseConnection();
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        BugReport.reportBug("MedSavant",VersionSettings.getVersionString(), "Marc Fiume", "mfiume@cs.toronto.edu", "UofT", "Testing bug reporting", new Exception("msg"));
        System.out.println("Bug reported");
    }

    static boolean reportFeedback(String tool, String version, String name, String email, String feedbackStr) {
         try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair("tool", tool));
            params.add(new NameValuePair("name", name));
            params.add(new NameValuePair("email", email));
            params.add(new NameValuePair("feedback", feedbackStr));
            params.add(new NameValuePair("clientinfo",
                    kvp("program-version",version) +
                    ", " +
                    kvp("java-version", getJDKVersion()) +
                    ", " +
                    kvp("os", getOS()) +
                    ", " +
                    kvp("time",(new Date()).toLocaleString())));

/*            String params =
                      "tool=" + tool
                    + "&name=" + name
                    + "&email=" + email
                    + "&feedback=" + feedbackStr
                    + "&clientinfo=" + kvp("program-version",version) + ", " + kvp("java-version", getJDKVersion()) + ", " + kvp("os", getOS()) + ", " + kvp("time",(new Date()).toLocaleString());
*/
            postRequest(WebResources.FEEDBACK_FORM_URL, params);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
