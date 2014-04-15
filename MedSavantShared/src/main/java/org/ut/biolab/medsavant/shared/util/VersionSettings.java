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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private static final Log LOG = LogFactory.getLog(VersionSettings.class);

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
        //Allow version to be overridden from command line. 
        return System.getProperty("medsavant.version", version);                    
    }

    //The first connection will wait this long.
    private static final int FIRST_TIMEOUT = 10000;
    
    //Second, third, etc. connections will wait this long.  The timeout is 
    //reset to FIRST_TIMEOUT after one successful connection is made.
    private static final int SUBSEQUENT_TIMEOUT = 1000;
    
    private static int timeoutVal = FIRST_TIMEOUT;
    
    public static boolean isCompatible(URL url, String queryVersion, String hostVersion, boolean exactMatchByPass) throws SAXException, SocketTimeoutException, IOException {

        LOG.info("Checking compatibility between " + queryVersion + " and host: " + hostVersion);

        if (exactMatchByPass) {
            if (hostVersion.equals(queryVersion)) {
                LOG.info("Client and server versions match exactly");
                return true;
            }
        }
        
        LOG.info("Client and server versions aren't an exact match, looking up compatibility...");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            return false;
        }   
        Document doc = null;        
        
        try{
            doc = dBuilder.parse(NetworkUtils.openStream(url, timeoutVal, timeoutVal));
        }catch(SocketTimeoutException sto){            
            LOG.info("Timed out after "+timeoutVal+" seconds");
            timeoutVal = SUBSEQUENT_TIMEOUT;
            throw sto;            
        }
        timeoutVal = FIRST_TIMEOUT;
        
        doc.getDocumentElement().normalize();
        System.out.println("Document element normalized");
        Map<String, Set<String>> versionMap = new HashMap<String, Set<String>>();
        String nodeName = "host"; //(isServer ? "server" : "client");
        NodeList nodes = doc.getElementsByTagName(nodeName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                String versionNum = e.getAttribute("name");
                if (versionNum != null && !versionNum.equals("")) {
                    versionMap.put(versionNum, MiscUtils.getTagValues(e, "compatible"));
                }
            }
        }

        return versionMap.containsKey(hostVersion) && versionMap.get(hostVersion).contains(queryVersion);

    }

    public static boolean isClientCompatibleWithServer(String clientVersion, String serverVersion) throws IOException, SAXException, SocketTimeoutException {
        LOG.info("Checking client->server compatibility");
        return isCompatible(WebResources.CLIENT_SERVER_VERSION_COMPATIBILITY_URL,clientVersion,serverVersion,true);
    }

    public static boolean isDatabaseCompatibleWithServer(String dbVersion, String serverVersion) throws IOException, SAXException, SocketTimeoutException {
        LOG.info("Checking database->server compatibility");
        return isCompatible(WebResources.DATABASE_SERVER_VERSION_COMPATIBILITY_URL,dbVersion,serverVersion,true);
    }

    public static boolean isAppSDKCompatibleWithClient(String appSDKVersion, String clientVersion) throws IOException, SAXException, SocketTimeoutException {
        LOG.info("Checking app->client compatibility");
        return isCompatible(WebResources.APPSDK_CLIENT_VERSION_COMPATIBILITY_URL,appSDKVersion,clientVersion,false);
    }
}
