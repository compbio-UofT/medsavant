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
package org.ut.biolab.medsavant.client.settings;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final String BETA_VERSION = "SNAPSHOT";

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

    public static String getServerVersion() throws RemoteException {
        try {
            return MedSavantClient.SetupManager.getServerVersion();
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
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

        Map<String, Set<String>> versionMap = new HashMap<String, Set<String>>();
        String nodeName = (isServer ? "server" : "client");
        NodeList nodes = doc.getElementsByTagName(nodeName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                String versionNum = e.getAttribute("name");
                if (versionNum != null && !versionNum.equals("")) {
                    versionMap.put(versionNum, ClientMiscUtils.getTagValues(e, "compatible"));
                }
            }
        }

        return versionMap.containsKey(programVersion) && versionMap.get(programVersion).contains(dbVersion);
    }
}
