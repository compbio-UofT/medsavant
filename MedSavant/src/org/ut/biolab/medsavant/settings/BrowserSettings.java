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

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.ut.biolab.medsavant.util.NetworkUtils;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


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
    public static final URL PLUGIN_URL = NetworkUtils.getKnownGoodURL(URL, "plugins/plugin.xml");
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
    
    public static String getVersionString(){
        return VERSION + " " + BUILD;
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
    
    public static boolean isCompatible(String clientVersion, String dbVersion) throws ParserConfigurationException, IOException, SAXException{
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(NetworkUtils.openStream(BrowserSettings.VERSION_URL, 10000, 10000));
        
        doc.getDocumentElement().normalize();
        
        Map<String, List<String>> versionMap = new HashMap<String, List<String>>();
        NodeList nodes = doc.getElementsByTagName("version");
        for(int i = 0; i < nodes.getLength(); i++){
            Node n = nodes.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element) n;
                String versionNum = getTagValue(e, "name");
                if(versionNum != null && !versionNum.equals("")){
                    versionMap.put(versionNum, new ArrayList<String>());
                    versionMap.get(versionNum).add(versionNum);
                    versionMap.get(versionNum).addAll(getTagValues(e, "compatible"));
                }
            }
        }
        
        return versionMap.containsKey(clientVersion) && versionMap.get(clientVersion).contains(dbVersion);
    }
    
    private static String getTagValue(Element e, String tag) {
	NodeList nlList = e.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
	return nValue.getNodeValue();
    }
    
    private static List<String> getTagValues(Element e, String tag) {
        NodeList nlList = e.getElementsByTagName(tag);
        List<String> result = new ArrayList<String>();
        for(int i = 0; i < nlList.getLength(); i++){
            Node nValue = (Node) nlList.item(i).getChildNodes().item(0);
            result.add(nValue.getNodeValue());
        }
        return result;
    }
    
}
