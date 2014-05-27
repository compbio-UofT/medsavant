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
import org.ut.biolab.medsavant.shared.util.WebResources;


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
    private final boolean defaultAnnotation;
    
    public AnnotationDownloadInformation(String name, String version, String reference, String description, String url){
        this(name, version, reference, description, url, "N");
    }
    
    public AnnotationDownloadInformation(String name, String version, String reference, String description, String url, String isDefault) {
        this.name = name;
        this.reference = reference;
        this.version = version;
        this.description = description;
        this.url = url;
        if(isDefault != null && (isDefault.equalsIgnoreCase("yes") || isDefault.equalsIgnoreCase("true") || isDefault.equalsIgnoreCase("y") || isDefault.equalsIgnoreCase("yes"))){
            this.defaultAnnotation = true;
        }else{
            this.defaultAnnotation = false;
        }
    }

    public boolean isDefault(){
        return defaultAnnotation;
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
    
    //private static final String databaseURL = "http://compbio.cs.toronto.edu/savant/data/dropbox/newAnnotationDirectory.xml";
    private static File downloadAnnotationDatabase() throws IOException {
        String targetFileName = "AnnotationDatabase.xml";
        File targetDir = DirectorySettings.getTmpDirectory();
        File tgt = new File(targetDir, targetFileName);
        //if(!tgt.exists()){         
        NetworkUtils.downloadFile(WebResources.ANNOTATION_DIRECTORY_URL, targetDir, targetFileName);
        //}
        return tgt;
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
                                    annotation.getAttribute("url"),
                                    annotation.getAttribute("isDefault"));

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
