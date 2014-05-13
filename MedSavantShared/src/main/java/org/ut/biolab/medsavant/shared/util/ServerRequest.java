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
package org.ut.biolab.medsavant.shared.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author mfiume
 */
public class ServerRequest {

    //private static final String rootRequest = "http://genomesavant.com/q/medsavant";

    public static final String TRACK_PATH = "browser/track";

    public static JSONObject requestFromServer(String serverPath, Map<String, String> kvps) throws Exception {

        String getString = "";
        boolean started = false;
        for (String key : kvps.keySet()) {
            getString += (started ? "&" : "?") + key + "=" + kvps.get(key);
            started = true;
        }

        String urlString = WebResources.SAVANT_ROOTREQUEST_URL + "/" + serverPath + "/" + getString;//URLEncoder.encode(getString,"UTF-8");
        String data = readUrl(urlString);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(data);
        JSONObject jsonObject = (JSONObject) obj;

        return jsonObject;
    }

    public static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;

        try{
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader (url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[]chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }

    }

    public static void main(String[] args) throws Exception {
        Map<String,String> m = new HashMap<String,String>();
        m.put("reference","hg19");
        m.put("trackname", "gene");
        JSONObject o = ServerRequest.requestFromServer(TRACK_PATH,m);

        System.out.println(o.get("name"));
        System.out.println(o.get("url"));
    }
}
