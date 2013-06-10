package org.ut.biolab.medsavant.shared.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author mfiume
 */
public class ServerRequest {

    private static final String rootRequest = "http://genomesavant.com/q/medsavant";

    public static final String TRACK_PATH = "browser/track";

    public static JSONObject requestFromServer(String serverPath, Map<String, String> kvps) throws Exception {

        String getString = "";
        boolean started = false;
        for (String key : kvps.keySet()) {
            getString += (started ? "&" : "?") + key + "=" + kvps.get(key);
            started = true;
        }

        String urlString = rootRequest + "/" + serverPath + "/" + getString;//URLEncoder.encode(getString,"UTF-8");
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
