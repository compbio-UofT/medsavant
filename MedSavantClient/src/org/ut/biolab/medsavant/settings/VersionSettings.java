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
import java.sql.SQLException;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.db.settings.Settings;
import org.ut.biolab.medsavant.db.util.shared.MiscUtils;
import org.ut.biolab.medsavant.db.util.shared.NetworkUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author mfiume
 */
public class VersionSettings {

    /*
     * Website URLs
     */
    public static final URL URL = NetworkUtils.getKnownGoodURL("http://www.genomesavant.com/medsavant");
    public static final URL VERSION_URL = NetworkUtils.getKnownGoodURL(URL, "serve/version/version.xml");
    public static final URL PLUGIN_URL = NetworkUtils.getKnownGoodURL(URL, "plugins/plugin.xml");
    public static final URL LOG_USAGE_STATS_URL = NetworkUtils.getKnownGoodURL(URL, "scripts/logUsageStats.cgi");

    public static final String VERSION = "1.0.0";
    public static String BUILD = "beta";

    /**
     * Is this version a beta release?
     */
    public static boolean isBeta() {
        return BUILD.equals("beta");
    }

    public static String getVersionString(){
        return VERSION + " " + BUILD;
    }

    public static String getDatabaseVersion() throws SQLException, RemoteException {
        return MedSavantClient.SettingsQueryUtilAdapter.getSetting(LoginController.sessionId, Settings.KEY_CLIENT_VERSION);
    }

    public static boolean isCompatible(String programVersion, String dbVersion, boolean isServer) throws ParserConfigurationException, IOException, SAXException{

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(NetworkUtils.openStream(VERSION_URL, 10000, 10000));

        doc.getDocumentElement().normalize();

        Map<String, List<String>> versionMap = new HashMap<String, List<String>>();
        String nodeName = (isServer ? "server" : "client");
        NodeList nodes = doc.getElementsByTagName(nodeName);
        for(int i = 0; i < nodes.getLength(); i++){
            Node n = nodes.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element) n;
                String versionNum = e.getAttribute("name");
                if(versionNum != null && !versionNum.equals("")){
                    versionMap.put(versionNum, new ArrayList<String>());
                    versionMap.get(versionNum).add(versionNum);
                    versionMap.get(versionNum).addAll(MiscUtils.getTagValues(e, "compatible"));
                }
            }
        }

        return versionMap.containsKey(programVersion) && versionMap.get(programVersion).contains(dbVersion);
    }

}
