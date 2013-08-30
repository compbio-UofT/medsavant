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
package org.ut.biolab.medsavant.client.plugin;

import java.io.File;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Plugin description read from the plugin.xml file.
 *
 * @author tarkvara
 */
public class PluginDescriptor implements Comparable<PluginDescriptor> {
    private static Log LOG = LogFactory.getLog(PluginDescriptor.class);
    /**
     * Bare-bones set of tags we need to recognise in plugin.xml in order to
     * identify plugins.
     */
    private enum PluginXMLElement {

        PLUGIN,
        ATTRIBUTE,
        PARAMETER,
        PROPERTY,
        IGNORED
    };

    /**
     * Bare-bones set of attributes we need to recognise in plugin.xml in order
     * to identify plugins.
     */
    private enum PluginXMLAttribute {

        ID,
        NAME,
        VALUE,
        VERSION,
        CLASS,
        TYPE,
        IGNORED
    };

    public enum Type {

        FILTER {
            @Override
            public String toString() {
                return "Search Condition";
            }
        },
        VARIANT {
            @Override
            public String toString() {
                return "Variant Addition";
            }
        },
        UNKNOWN {
            @Override
            public String toString() {
                return "Unknown";
            }
        },
        SECTION {
            @Override
            public String toString(){
                return "Section";
            }
        }
    }
    final String className;
    final String id;
    final String version;
    final String name;
    final String sdkVersion;
    final File file;
    final Type type;
    private static XMLStreamReader reader;

    private PluginDescriptor(String className, String id, String version, String name, String sdkVersion, String type, File file) {
        this.className = className;
        this.id = id;
        this.version = version;
        this.name = name;
        this.sdkVersion = sdkVersion;
        this.type = Type.valueOf(type.toUpperCase());
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

    public Type getType() {
        return type;
    }

    public File getFile() {
        return file;
    }

    @Override
    public int compareTo(PluginDescriptor t) {
        return (id + version).compareTo(t.id + t.version);
    }

    /**
     * Here's where we do our SDK compatibility check. Update this code whenever
     * the API changes.
     */
    public boolean isCompatible() {
        return sdkVersion.equals("1.0.0");
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
                String type = "FILTER";
                String currentElement = null;
                String currentText = "";
                do {
                    switch (reader.next()) {
                        case XMLStreamConstants.START_ELEMENT:
                            switch (readElement()) {
                                case PLUGIN:                                    
                                    className = readAttribute(PluginXMLAttribute.CLASS);
                                    id = readAttribute(PluginXMLAttribute.ID);
                                    version = readAttribute(PluginXMLAttribute.VERSION);
                                    type = readAttribute(PluginXMLAttribute.TYPE);
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
                                case PROPERTY:
                                    if ("name".equals(readAttribute(PluginXMLAttribute.NAME))) {
                                        name = readAttribute(PluginXMLAttribute.VALUE);
                                        if (name == null) {
                                            currentElement = "name";
                                        }
                                    }

                                    if ("sdk-version".equals(readAttribute(PluginXMLAttribute.NAME))) {
                                        sdkVersion = readAttribute(PluginXMLAttribute.VALUE);
                                        if (sdkVersion == null) {
                                            currentElement = "sdk-version";
                                        }
                                    }
                                    break;
                            }
                            break;
                        case XMLStreamConstants.CHARACTERS:
                            if (reader.isWhiteSpace()) {
                                break;
                            } else if (currentElement != null) {
                                currentText += reader.getText().trim().replace("\t", "");
                                //System.out.println("currentText="+currentText);
                            }
                            break;

                        case XMLStreamConstants.END_ELEMENT:
                            if (readElement() == PluginXMLElement.PROPERTY) {
                                if (currentElement != null && currentText.length() > 0) {
                                    if (currentElement.equals("name")) {
                                        name = currentText;
                                    } else if (currentElement.equals("sdk-version")) {
                                        sdkVersion = currentText;
                                    }
                                }
                                currentText = "";
                                currentElement = null;
                            }
                            break;

                        case XMLStreamConstants.END_DOCUMENT:
                            reader.close();
                            reader = null;
                            break;
                    }
                } while (reader != null);

                //System.out.println("className="+className+" id="+id+" name="+name);
                if (className != null && id != null && name != null) {
                    return new PluginDescriptor(className, id, version, name, sdkVersion, type, f);
                }
            }
        } catch (Exception x) {
            LOG.error("Error parsing plugin.xml from "+f.getAbsolutePath()+": "+x);
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
    
    //For testing, delete once finalized.
    public static void test(){
        File f = new File("/tmp/medsavant.demo-1.0.0.jar");
        try{
            PluginDescriptor pd = PluginDescriptor.fromFile(f);
            
            System.out.println("ID=["+pd.getID()+"]");
            System.out.println("Name=["+pd.getName()+"]");
            System.out.println("SDKVersion=["+pd.getSDKVersion()+"]");
            System.out.println("Version=["+pd.getVersion()+"]");
            System.out.println("ClassName=["+pd.getClassName()+"]");
            System.out.println("Type=["+pd.getType()+"]");
            System.out.println("File=["+pd.getFile().getAbsolutePath()+"]"); 
        }catch(Exception ex){
            System.err.println("Error reading file: "+ex);
        }
    }
}
