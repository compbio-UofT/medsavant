/*
 *    Copyright 2009-2012 University of Toronto
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
package org.ut.biolab.medsavant.client.settings;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.shared.db.Settings;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
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
    public static final URL URL = ClientNetworkUtils.getKnownGoodURL("http://www.genomesavant.com/p/medsavant");
    public static final URL VERSION_URL = ClientNetworkUtils.getKnownGoodURL(URL, "serve/version/version.xml");
    public static final URL PLUGIN_URL = ClientNetworkUtils.getKnownGoodURL(URL, "serve/plugin/plugin.xml");
    public static final URL LOG_USAGE_STATS_URL = ClientNetworkUtils.getKnownGoodURL(URL, "scripts/logUsageStats.cgi");

    // version to user if the version could not be found
    private static final String UNDEFINED_VERSION = "";
    private static final String BETA_VERSION = "b";

    /**
     * Is this version a beta release?
     */
    public static boolean isBeta() {
        // determine if the version is beta based on version substring - is this
        // needed?
        return getVersionString().toLowerCase().contains(BETA_VERSION.toLowerCase());
    }

    public static String getVersionString() {
        String version = UNDEFINED_VERSION;

        Package aPackage = VersionSettings.class.getPackage();
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

    public static String getDatabaseVersion() throws SQLException, RemoteException {
        try {
            return MedSavantClient.SettingsManager.getSetting(LoginController.getInstance().getSessionID(), Settings.KEY_CLIENT_VERSION);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    public static boolean isCompatible(String programVersion, String dbVersion, boolean isServer) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(ClientNetworkUtils.openStream(VERSION_URL, 10000, 10000));

        doc.getDocumentElement().normalize();

        Map<String, List<String>> versionMap = new HashMap<String, List<String>>();
        String nodeName = (isServer ? "server" : "client");
        NodeList nodes = doc.getElementsByTagName(nodeName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                String versionNum = e.getAttribute("name");
                if (versionNum != null && !versionNum.equals("")) {
                    versionMap.put(versionNum, new ArrayList<String>());
                    versionMap.get(versionNum).add(versionNum);
                    versionMap.get(versionNum).addAll(ClientMiscUtils.getTagValues(e, "compatible"));
                }
            }
        }

        return versionMap.containsKey(programVersion) && versionMap.get(programVersion).contains(dbVersion);
    }

}
