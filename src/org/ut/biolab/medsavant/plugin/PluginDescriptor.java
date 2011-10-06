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

import java.io.File;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;


/**
 * Plugin description read from the plugin.xml file.
 *
 * @author tarkvara
 */
public class PluginDescriptor implements Comparable<PluginDescriptor> {

    /**
     * Bare-bones set of tags we need to recognise in plugin.xml in order to identify plugins.
     */
    private enum PluginXMLElement {
        PLUGIN,
        ATTRIBUTE,
        PARAMETER,
        IGNORED
    };

    /**
     * Bare-bones set of attributes we need to recognise in plugin.xml in order to identify plugins.
     */
    private enum PluginXMLAttribute {
        ID,
        VALUE,
        VERSION,
        CLASS,
        IGNORED
    };


    final String className;
    final String id;
    final String version;
    final String name;
    final String sdkVersion;
    final File file;

    private static XMLStreamReader reader;

    private PluginDescriptor(String className, String id, String version, String name, String sdkVersion, File file) {
        this.className = className;
        this.id = id;
        this.version = version;
        this.name = name;
        this.sdkVersion = sdkVersion != null ? sdkVersion : "1.4.2 or earlier";
        this.file = file;
    }

    @Override
    public String toString() {
        return id + "-" + version;
    }

    public String getClassName() {
        return className;
    }

    public String getID() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getSDKVersion() {
        return sdkVersion;
    }

    public File getFile() {
        return file;
    }

    @Override
    public int compareTo(PluginDescriptor t) {
        return (id + version).compareTo(t.id + t.version);
    }


    /**
     * Here's where we do our SDK compatibility check.  Update this code whenever the API changes.
     */
    public boolean isCompatible() {
        return sdkVersion.equals("1.5.1") || sdkVersion.equals("1.6.0");
    }

    public static PluginDescriptor fromFile(File f) throws PluginVersionException {
        try {
            JarFile jar = new JarFile(f);
            ZipEntry entry = jar.getEntry("plugin.xml");
            if (entry != null) {
                InputStream entryStream = jar.getInputStream(entry);
                reader = XMLInputFactory.newInstance().createXMLStreamReader(entryStream);
                String className = null;
                String id = null;
                String version = null;
                String sdkVersion = null;
                String name = null;
                do {
                    switch (reader.next()) {
                        case XMLStreamConstants.START_ELEMENT:
                            switch (readElement()) {
                                case PLUGIN:
                                    className = readAttribute(PluginXMLAttribute.CLASS);
                                    id = readAttribute(PluginXMLAttribute.ID);
                                    version = readAttribute(PluginXMLAttribute.VERSION);
                                    break;
                                case ATTRIBUTE:
                                    if ("sdk-version".equals(readAttribute(PluginXMLAttribute.ID))) {
                                        sdkVersion = readAttribute(PluginXMLAttribute.VALUE);
                                    }
                                    break;
                                case PARAMETER:
                                    if ("name".equals(readAttribute(PluginXMLAttribute.ID))) {
                                        name = readAttribute(PluginXMLAttribute.VALUE);
                                    }
                                    break;
                            }
                            break;
                        case XMLStreamConstants.END_DOCUMENT:
                            reader.close();
                            reader = null;
                            break;
                    }
                } while (reader != null);

                if (className != null && id != null && name != null) {
                    return new PluginDescriptor(className, id, version, name, sdkVersion, f);
                }
            }
        } catch (Exception x) {
        }
        throw new PluginVersionException(f.getName() + " did not contain a valid plugin");
    }

    private static PluginXMLElement readElement() {
        try {
            String elemName = reader.getLocalName().toUpperCase();
            return Enum.valueOf(PluginXMLElement.class, elemName);
        } catch (IllegalArgumentException ignored) {
            // Any elements not in our enum will just be ignored.
            return PluginXMLElement.IGNORED;
        }
    }

    private static String readAttribute(PluginXMLAttribute attr) {
        return reader.getAttributeValue(null, attr.toString().toLowerCase());
    }


}
