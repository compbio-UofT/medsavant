/*
 *    Copyright 2011 University of Toronto
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
package org.ut.biolab.medsavant.plugin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.ut.biolab.medsavant.db.util.NetworkUtils;

/**
 * Given a file (typically the plugin.xml file from our web-site), create an index of plugin versions.
 *
 * @author tarkvara
 */
public class PluginIndex {
    private static final Logger LOG = Logger.getLogger(PluginIndex.class.getName());
    private Map<String, URL> urls;

    public PluginIndex(URL url) throws IOException {
        urls = new HashMap<String, URL>();
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(NetworkUtils.openStream(url));
            boolean done = false;
            String id = null;
            do {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        String elemName = reader.getLocalName();
                        if (elemName.equals("leaf")) {
                            id = reader.getAttributeValue(null, "id");
                        } else if (elemName.equals("url")) {
                            if (id != null) {
                                try {
                                    urls.put(id, new URL(reader.getElementText()));
                                } catch (MalformedURLException x) {
                                    LOG.log(Level.INFO, "Unable to parse \"%s\" as a plugin URL.", reader.getElementText());
                                }
                                id = null;
                            }
                        }
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        reader.close();
                        done = true;
                        break;
                }
            } while (!done);
        } catch (XMLStreamException x) {
            throw new IOException("Unable to get version number from web-site.", x);
        }
    }

    public URL getPluginURL(String id) {
        return urls.get(id);
    }
}

