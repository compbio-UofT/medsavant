/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.shared.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;


/**
 *
 * @author mfiume
 */
public class AnnotationDownloadInformation implements Serializable {

    private final String name;
    private final String version;
    private final String reference;
    private final String description;
    private final String url;

    public AnnotationDownloadInformation(String name, String version, String reference, String description, String url) {
        this.name = name;
        this.reference = reference;
        this.version = version;
        this.description = description;
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public String getProgramName() {
        return name;
    }

    public String getURL() {
        return url;
    }

    public String getProgramVersion() {
        return version;
    }

    public String getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return "AnnotationDownloadInformation{" + "name=" + name + ", version=" + version + ", description=" + description + ", url=" + url + ", reference=" + reference + '}';
    }


    /**
     * Static helper variables / methods
     */
//    private static final String databaseURL = "http://genomesavant.com/medsavant/serve/annotation/annotation.xml";
    private static final String databaseURL = "http://compbio.cs.toronto.edu/savant/data/dropbox/annotationDirectory.xml";
    private static File downloadAnnotationDatabase() throws IOException {
        String targetFileName = "AnnotationDatabase.xml";
        File targetDir = DirectorySettings.getTmpDirectory();
        NetworkUtils.downloadFile(new URL(databaseURL), targetDir, targetFileName);
        return new File(targetDir, targetFileName);
    }

    public static List<AnnotationDownloadInformation> getDownloadableAnnotations(String versionName) throws XMLStreamException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        return getDownloadableAnnotations(versionName,null);
    }


    public static List<AnnotationDownloadInformation> getDownloadableAnnotations(String versionName, String referenceName) throws XMLStreamException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        System.out.println("Getting downloadable Annotations for version "+versionName);
        File f = downloadAnnotationDatabase();

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(f);

        // normalize text representation
        doc.getDocumentElement().normalize();

        Element annotations = (Element) doc.getElementsByTagName("annotations").item(0);
        NodeList versionList = annotations.getElementsByTagName("version");
        for (int i = 0; i < versionList.getLength(); i++) {
            Element version = (Element) versionList.item(i);

            if (version.getAttribute("name").equals(versionName)) {
                NodeList referenceList = version.getElementsByTagName("reference");

                List<AnnotationDownloadInformation> adiList = new ArrayList<AnnotationDownloadInformation>();

                for (int j = 0; j < referenceList.getLength(); j++) {
                    Element reference = (Element) referenceList.item(j);

                    if (referenceName == null || reference.getAttribute("name").equals(referenceName)) {

                        NodeList annotationList = reference.getElementsByTagName("annotation");
                        for (int k = 0; k < annotationList.getLength(); k++) {

                            Element annotation = (Element) annotationList.item(k);

                            AnnotationDownloadInformation adi = new AnnotationDownloadInformation(
                                    annotation.getAttribute("name"),
                                    annotation.getAttribute("version"),
                                    reference.getAttribute("name"),
                                    annotation.getAttribute("description"),
                                    annotation.getAttribute("url"));

                            adiList.add(adi);
                        }
                    }
                }
                return adiList;
            }
        }

        return null;
    }
}
