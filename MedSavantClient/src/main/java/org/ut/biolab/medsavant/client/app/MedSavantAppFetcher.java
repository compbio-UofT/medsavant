/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.app;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
import org.ut.biolab.medsavant.shared.util.VersionSettings;
import org.ut.biolab.medsavant.shared.util.WebResources;
import org.ut.biolab.medsavant.client.app.api.AppInfoFetcher;

/**
 *
 * @author mfiume
 */
public class MedSavantAppFetcher implements AppInfoFetcher {
    //This could also be dynamic, and editable by the user via some kind of settings dialog.

    private static Log LOG = LogFactory.getLog(MedSavantAppFetcher.class);

    private static final String[] REQUIRED_PAIRS = new String[]{
        "url",
        "name",
        "version",
        "sdk-version",
        "category",
        "author",
        "description"
    };

    private List<AppInfo> appInfo;

    private enum XMLElement {

        PLUGINS, PLUGIN, AUTHOR, WEBPAGE, DESCRIPTION, SHORTDESCRIPTION, NEWINVERSION, ERROR;
    }

    private enum XMLPluginAttribute {

        URL, NAME, VERSION, SDK_VERSION {
            @Override
            public String toString() {
                return "sdk-version";
            }
        }, CATEGORY;
    }

    private static MedSavantAppFetcher.XMLElement readElement(XMLStreamReader reader) {
        try {
            String elemName = reader.getLocalName().toUpperCase();
            return Enum.valueOf(XMLElement.class, elemName);
        } catch (IllegalArgumentException ignored) {
            // Any elements not in our enum will just be ignored.
            return XMLElement.ERROR;
        }
    }

    private AppInfo getAppInfo(Map<String, String> pluginMap, String repositoryURL) throws MalformedURLException, IOException {
        //check that all required name/value pairs are set:
        for (String key : REQUIRED_PAIRS) {
            if (pluginMap.get(key) == null) {
                String n = pluginMap.get("name");
                if (n == null) {
                    n = "<Unknown Plugin>";
                }
                throw new IOException("Invalid plugin repository xml at " + repositoryURL + ", plugin " + n + " is missing value for '" + key + "'");
            }
        }

        String name = pluginMap.get("name");
        String version = pluginMap.get("version");
        String compatibleWith = pluginMap.get("sdk-version");
        String category = pluginMap.get("category");
        if (category != null) {
            category = Enum.valueOf(AppDescriptor.Category.class, category.toUpperCase()).toString();
        }

        String description = pluginMap.get("description");
        String shortdesc = pluginMap.get("shortdescription");
        String newinversion = pluginMap.get("newinversion");
        String author = pluginMap.get("author");
        String web = pluginMap.get("webpage");
        URL downloadURL = new URL(pluginMap.get("url"));
        LOG.info("Located plugin " + name + " in App repository");
        return new AppInfo(name, version, category, compatibleWith, shortdesc, newinversion, description, author, web, downloadURL);
    }

    private void refreshAppInfo(String[] pluginRepositoryUrls) throws Exception {
        appInfo = new LinkedList<AppInfo>();
        //query the server for XML file describing the apps.
        for (String repositoryURL : pluginRepositoryUrls) {
            XMLStreamReader reader =
                    XMLInputFactory.newInstance().createXMLStreamReader(new URL(repositoryURL).openConnection().getInputStream());
            Map<String, String> pluginMap = new HashMap<String, String>();
            XMLElement currentElement = null;
            String currentText = "";
            do {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        currentElement = readElement(reader);
                        currentText = "";
                        switch (currentElement) {
                            case PLUGINS:

                            case PLUGIN:
                                pluginMap.clear();
                                for (XMLPluginAttribute a : XMLPluginAttribute.values()) {
                                    String attrName = a.toString().toLowerCase();
                                    String attrVal = reader.getAttributeValue(null, attrName);
                                    pluginMap.put(attrName, attrVal);
                                }
                                break;

                            case AUTHOR:
                            case WEBPAGE:
                            case DESCRIPTION:
                            case SHORTDESCRIPTION:
                            case NEWINVERSION:
                                String val = reader.getAttributeValue(null, "value");
                                if (val != null) {
                                    pluginMap.put(currentElement.toString().toLowerCase(), val);
                                }
                                break;
                            default:
                                throw new IOException("Invalid plugin repository xml at " + repositoryURL);
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (reader.getName().toString().equalsIgnoreCase(XMLElement.PLUGIN.toString())) {
                            AppInfo ai = getAppInfo(pluginMap, repositoryURL);
                            appInfo.add(ai);
                        } else if (currentElement != null && currentText.length() > 0) {
                            pluginMap.put(currentElement.toString().toLowerCase(), currentText);
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        currentText += reader.getText().trim().replace("\t", "");
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        reader.close();
                        reader = null;
                        break;
                }
            } while (reader != null);
        }
        LOG.info("Found " + appInfo.size() + " App descriptions");
    }

    @Override
    public List<AppInfo> fetchApplicationInformation(String search) throws Exception {

        if (appInfo == null) {
            refreshAppInfo(WebResources.PLUGIN_REPOSITORY_URLS);
        }


        if (search == null || search.length() < 1) {
            return appInfo;
        }

        List<AppInfo> results = new LinkedList<AppInfo>();

        //search names first - those hits will be listed first.
        for (AppInfo ai : appInfo) {
            if (ai.getName().contains(search)) {
                if (VersionSettings.isAppSDKCompatibleWithClient(ai.getSDKVersion(),VersionSettings.getVersionString())) {
                    results.add(ai);
                }
            }
        }

        //Search description, shortdescription, and newinversion next.
        for (AppInfo ai : appInfo) {
            if (ai.getDescription().contains(search) || ai.getShortDescription().contains(search)
                    || ai.getNewInVersion().contains(search)) {
                if (VersionSettings.isAppSDKCompatibleWithClient(ai.getSDKVersion(),VersionSettings.getVersionString())) { results.add(ai); }
            }
        }


        return results;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("USAGE: MedSavantAppFetcher <URLs to test>");
            System.err.println("Local files can be specified using file:// (e.g. file:///tmp/pluginDirectory.xml)");
        }
        try {
            MedSavantAppFetcher m = new MedSavantAppFetcher();
            m.refreshAppInfo(args);
        } catch (Exception ex) {
            System.err.println("Exception caught " + ex);
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}