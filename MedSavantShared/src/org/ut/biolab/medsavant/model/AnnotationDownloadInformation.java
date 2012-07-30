package org.ut.biolab.medsavant.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.ut.biolab.medsavant.util.DirectorySettings;
import org.ut.biolab.medsavant.util.NetworkUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author mfiume
 */
public class AnnotationDownloadInformation {

    String name;
    String version;
    String description;
    String url;
    private final String reference;

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

    public String getUrl() {
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
    private static final String databaseURL = "http://genomesavant.com/medsavant/serve/annotation/annotation.xml";

    private static File downloadAnnotationDatabase() {

        String targetFileName = "AnnotationDatabase.xml";
        File targetDir = DirectorySettings.getTmpDirectory();
        try {
            NetworkUtils.downloadFile(new URL(databaseURL), targetDir, targetFileName);
            File f = new File(targetDir, targetFileName);
            return f;
        } catch (IOException ex) {
            Logger.getLogger(AnnotationDownloadInformation.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static List<AnnotationDownloadInformation> getDownloadAbleAnnotations(String versionName, String referenceName) throws XMLStreamException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {

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
                for (int j = 0; j < referenceList.getLength(); j++) {
                    Element reference = (Element) referenceList.item(j);


                    if (reference.getAttribute("name").equals(referenceName)) {

                        List<AnnotationDownloadInformation> adiList = new ArrayList<AnnotationDownloadInformation>();

                        NodeList annotationList = reference.getElementsByTagName("annotation");
                        for (int k = 0; k < annotationList.getLength(); k++) {

                            Element annotation = (Element) annotationList.item(k);

                            AnnotationDownloadInformation adi = new AnnotationDownloadInformation(
                                    annotation.getAttribute("name"),
                                    annotation.getAttribute("version"),
                                    referenceName,
                                    annotation.getAttribute("description"),
                                    annotation.getAttribute("url"));
                            System.out.println(adi);

                            adiList.add(adi);
                        }

                        return adiList;
                    }


                }
            }
        }

        return null;
    }
}
