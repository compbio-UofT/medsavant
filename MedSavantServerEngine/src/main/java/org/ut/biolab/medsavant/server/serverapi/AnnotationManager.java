/*
 *    Copyright 2011-2012 University of Toronto
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
package org.ut.biolab.medsavant.server.serverapi;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.shared.model.AnnotatedColumn;
import org.ut.biolab.medsavant.shared.persistence.CustomFieldManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.persistence.solr.SolrCustomFieldManager;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase.AnnotationColumns;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat.AnnotationType;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.AnnotationDownloadInformation;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.AnnotationManagerAdapter;

/**
 *
 * @author mfiume
 */
public class AnnotationManager extends MedSavantServerUnicastRemoteObject implements AnnotationManagerAdapter, AnnotationColumns {

    private static final Log LOG = LogFactory.getLog(AnnotationManager.class);
    private static AnnotationManager instance;
    private static QueryManager queryManager;
    private static EntityManager entityManager;
    private static CustomFieldManager customFieldManager;

    private AnnotationManager() throws RemoteException, SessionExpiredException {
        entityManager = EntityManagerFactory.getEntityManager();
        queryManager = QueryManagerFactory.getQueryManager();
        customFieldManager = new SolrCustomFieldManager();
    }

    public static synchronized AnnotationManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new AnnotationManager();
        }
        return instance;
    }

    @Override
    public boolean installAnnotationForProject(String sessID, int projectID, AnnotationDownloadInformation info) {

        LOG.info("Installing annotation " + info);

        try {

            // if it's not installed
            if (!checkIfAnnotationIsInstalled(sessID, info)) {

                // is it already downloaded?
                File installPath = getInstallationDirectory(info.getProgramName(), info.getProgramVersion(), info.getReference());
                File doneFile = new File(installPath, "installed.touch");

                LOG.info("Checking for successful installation at " + installPath.getAbsolutePath());

                if (!doneFile.exists()) {

                    File downloadPath = new File(installPath, "tmp.zip");
                    LOG.info("Downloading annotation, be patient...");
                    downloadAnnotation(info, downloadPath);
                    LOG.info("Registering annotation to project");
                    unpackAnnotationZip(downloadPath);
                    doneFile.createNewFile();

                } else {
                    LOG.info("Annotation files already on disk");
                }

                registerAnnotationWithProject(installPath, sessID);

                LOG.info("Done installing annotation");
                return true;

                // annotation already installed
            } else {
                LOG.info("This annotation is already installed");
                return false;
            }
        } catch (Exception ex) {
            LOG.error("Problem installing annotation", ex);
        }
        return false;
    }


    public static void addAnnotationFormat(String sessID, int annotID, int pos, String colName, String colType, boolean filterable, String alias, String desc) throws SQLException, SessionExpiredException {

        AnnotatedColumn annotatedColumn = new AnnotatedColumn(annotID,pos,colName,colType,filterable,alias,desc);
        try {
            entityManager.persist(annotatedColumn);
            customFieldManager.addCustomField(annotatedColumn);
        } catch (InitializationException e) {
            LOG.error("Error persisting annotated column");
        } catch (IOException e) {
            LOG.error("Error persisting annotated column");
        } catch (URISyntaxException e) {
            LOG.error("Error persisting annotated column");
        }
    }

    public static int addAnnotation(String sessID, String prog, String vers, int refID, String path, boolean hasRef, boolean hasAlt, int type, boolean endInclusive) throws SQLException, SessionExpiredException {

        LOG.debug("Adding annotation...");

        int annotationId = DBUtils.generateId("id", Entity.ANNOTATION);
        Annotation annotation = new Annotation(annotationId,prog,vers,refID,null,path,AnnotationType.fromInt(type),endInclusive, hasRef, hasAlt);
        try {
            entityManager.persist(annotation);
        } catch (InitializationException e) {
            LOG.error("Error persisting annotation");
        }
        return annotationId;
    }

    private static AnnotationFormat parseFormat(File tabixFile, File xmlFormatFile) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xmlFormatFile);
        doc.getDocumentElement().normalize();


        boolean hasRef = doc.getDocumentElement().getAttribute("hasref").equals("true");
        boolean hasAlt = doc.getDocumentElement().getAttribute("hasalt").equals("true");
        boolean isEndInclusive = doc.getDocumentElement().getAttribute("isEndInclusive").isEmpty() ? false : true;
        String version = doc.getDocumentElement().getAttribute("version");
        String program = doc.getDocumentElement().getAttribute("program");
        String referenceName = doc.getDocumentElement().getAttribute("reference");
        AnnotationType annotationType = AnnotationFormat.AnnotationType.fromString(doc.getDocumentElement().getAttribute("type"));

        //String prefix = program + "_" + version.replaceAll("\\.", "_") + "_";


        //get custom columns
        NodeList fields = doc.getElementsByTagName("field");
        CustomField[] annotationFields = new CustomField[fields.getLength()];

        for (int i = 0; i < fields.getLength(); i++) {
            Element field = (Element) (fields.item(i));
            annotationFields[i] = new CustomField(
                    field.getAttribute("name"),
                    field.getAttribute("type"),
                    field.getAttribute("filterable").equals("true"),
                    field.getAttribute("alias"),
                    field.getAttribute("description"));
        }

        return new AnnotationFormat(
                program,
                version,
                referenceName,
                tabixFile.getAbsolutePath(),
                hasRef,
                hasAlt,
                annotationType,
                isEndInclusive,
                annotationFields);
    }

    private static File unpackAnnotationZip(File zip) throws ZipException, IOException {
        IOUtils.unzipFile(zip, new File(zip.getAbsolutePath()).getParent());
        zip.delete();
        return new File(new File(zip.getAbsolutePath()).getParent());
    }

    @Override
    public Annotation getAnnotation(String sid, int annotation_id) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select a from Annotation a where a.id= :annotationId");
        query.setParameter("annotationId", annotation_id);
        List<Annotation> annotationList = query.execute();

        Annotation result = (annotationList.size() > 0) ? annotationList.get(0) : null;
        return result;
    }

    @Override
    public Annotation[] getAnnotations(String sid) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select a from Annotation a");
        List<Annotation> result = query.execute();
        return result.toArray(new Annotation[0]);
    }

    /*
     * Get the annotation ids associated with the latest published table.
     */
    @Override
    public int[] getAnnotationIDs(String sessID, int projID, int refID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select a.id from Annotation a where " +
                "a.project_id = :projectId AND a.reference_id = :referenceId AND a.published = :published");
        query.setParameter("projectId", projID);
        query.setParameter("referenceId", refID);
        query.setParameter("published", true);

        List<ResultRow> resultRowList = query.executeForRows();

        int annotations = resultRowList.size();
        int[] annotationIds = new int[annotations];
        for (int i = 0 ; i < annotations; i++) {
            annotationIds[i] = (Integer) resultRowList.get(i).getObject("id");
        }

        return annotationIds;
    }

    @Override
    public AnnotationFormat getAnnotationFormat(String sessID, int annotID) throws SQLException, RemoteException, SessionExpiredException {

        Query query = queryManager.createQuery("Select a from Annotation a where a.id = :annotationId");
        query.setParameter("annotationId", annotID);

        Annotation annotation = query.getFirst();

        query = queryManager.createQuery("Select a from AnnotatedColumn a where a.annotationd_id = :annotationId");
        List<AnnotatedColumn> annotatedColumnList = query.execute();

        return new AnnotationFormat(annotation.getProgram(),
                annotation.getVersion(),
                annotation.getReferenceName(),
                annotation.getDataPath(),
                true,
                true,
                annotation.getAnnotationType(),
                annotation.isEndInclusive(),
                annotatedColumnList.toArray(new AnnotatedColumn[0]));

    }
    /**
     * HELPER FUNCTIONS
     */
    private static final File localDirectory = new File(DirectorySettings.getMedSavantDirectory() + "/annotation");

    public static void printFile(File f) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }


    /*
     private static File getZipLocation(AnnotationDownloadInformation adi) {

     File targetDir = getInstallationDirectory(adi.getProgramName(), adi.getProgramVersion(), adi.getReference());
     String targetFileName = "tmp.zip";

     return new File(targetDir, targetFileName);
     }
     */
    public static void downloadAnnotation(AnnotationDownloadInformation adi, File location) throws MalformedURLException, IOException {

        URL u = new URL(adi.getURL());
        File targetDir = location.getParentFile();
        targetDir.mkdirs();
        String targetFilename = location.getName();

        LOG.debug("Downloading " + u.toString() + " to " + ((new File(targetDir, targetFilename).getAbsolutePath())));

        NetworkUtils.downloadFile(u, targetDir, targetFilename);

    }



    /**
     * Get the prescribed directory for the given annotation
     *
     * @param info The annotation whose directory is to be returned
     * @return The directory
     */
    private static File getDirectoryForAnnotation(AnnotationDownloadInformation info) {
        return getDirectoryForAnnotation(info.getProgramName(), info.getProgramVersion(), info.getReference());
    }

    /**
     * Get the prescribed directory for the given annotation
     *
     * @param programName The program name for this annotation
     * @param programVersion The program version for this annotation
     * @param reference The genome reference that this annotation applies to
     * @return
     */
    private static File getDirectoryForAnnotation(String programName, String programVersion, String reference) {
        return new File(localDirectory.getAbsolutePath() + "/" + reference + "/" + programName + "/" + programVersion);
    }

    /**
     * Get the expected location of the Tabix file
     *
     * @param dir The directory
     * @return The expected location of the Tabix file
     */
    private static File getTabixFile(File dir) {
        //return new File(dir + "/annotation.gz");
        return getFileWithExtentionInDir(dir,"gz");
    }

    /**
     * Get the expected location of the Tabix index file
     *
     * @param dir The directory
     * @return The expected location of the Tabix index file
     */
    private static File getTabixIndexFile(File dir) {
        //return new File(dir + "/annotation.tbi");
        return getFileWithExtentionInDir(dir,"tbi");
    }

    /**
     * Get the expected location of the format file
     *
     * @param dir The directory
     * @return The expected location of the format file
     */
    private static File getFormatFile(File dir) {
        return getFileWithExtentionInDir(dir,"xml");
    }

    private static File getFileWithExtentionInDir(File dir, final String ext) {
        return dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith("." + ext)) {
                    return true;
                }
                return false;
            }

        })[0];
    }


    /**
     * Checks that the annotation is installed
     *
     * @param info The Annotation information (version,reference,etc) for the
     * annotation to check
     * @return Whether or not this annotation is currently installed
     */
    private boolean checkIfAnnotationIsInstalled(String sessID, AnnotationDownloadInformation info) throws RemoteException, SQLException, SessionExpiredException {

        int referenceId = ReferenceManager.getInstance().getReferenceID(sessID, info.getReference());
        Query query = queryManager.createQuery("Select a from Annotation a where a.reference_id= :referenceId" +
                "and a.program= :program and a.version= :version");
        query.setParameter("referenceId", referenceId);
        query.setParameter("program", info.getProgramName());
        query.setParameter("version", info.getProgramVersion());
        query.setLimit(1);

        List<Annotation> annotationList = query.execute();
        return (annotationList.size() > 0 ? true : false);
    }

    @Override
    public void uninstallAnnotation(String sessionID, Annotation an) throws RemoteException, SQLException, SessionExpiredException {
        int annotationID = an.getID();

        Query query = queryManager.createQuery("Delete from Annotation a where a.id= :annotationId");
        query.setParameter("annotationId", annotationID);
        query.executeDelete();

        query = queryManager.createQuery("Delete from AnnotationColumn a where a.annotation_id= :annotationId");
        query.setParameter("annotationId", annotationID);
        query.executeDelete();

        File installationPath = getInstallationDirectory(an.getProgram(), an.getVersion(), an.getReferenceName());
        System.out.println("Deleting path: " + installationPath.getAbsolutePath());
        try {
            Process p = Runtime.getRuntime().exec("chmod -R o+w " + installationPath.getAbsolutePath());
            p.waitFor();
        } catch (Exception e) {
        }
        deleteDirectory(installationPath);
    }

    /**
     * Force deletion of directory
     *
     * @param path
     * @return
     */
    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    private static File getInstallationDirectory(String programName, String version, String reference) {
        return new File(localDirectory.getAbsolutePath() + "/" + programName + "_" + version + "_" + reference);
    }

    private static void registerAnnotationWithProject(File dir, String sessionID) throws RemoteException, SAXException, SQLException, IOException, ParserConfigurationException, SessionExpiredException {
        LOG.info("Parsing format...");
        AnnotationFormat format = parseFormat(getTabixFile(dir), getFormatFile(dir));
        LOG.info("... DONE");

        LOG.info("FORMAT: " + format);

        int id = addAnnotation(
                sessionID,
                format.getProgram(),
                format.getVersion(),
                ReferenceManager.getInstance().getReferenceID(sessionID, format.getReferenceName()),
                getTabixFile(dir).getAbsolutePath(),
                format.hasRef(),
                format.hasAlt(),
                AnnotationType.toInt(format.getType()),
                format.isEndInclusive());

        int i = 0;
        for (CustomField a : format.getCustomFields()) {
            addAnnotationFormat(sessionID, id, i++, id + "_" + a.getColumnName(), a.getTypeString(), a.isFilterable(), a.getAlias(), a.getDescription());
        }

        LOG.info("Installed to " + dir.getAbsolutePath());
    }
}
