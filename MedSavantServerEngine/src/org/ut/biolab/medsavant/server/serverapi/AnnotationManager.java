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
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.AnnotationColumns;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.AnnotationFormatColumns;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat.AnnotationType;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.AnnotationDownloadInformation;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.AnnotationManagerAdapter;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.IOUtils;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;

/**
 *
 * @author mfiume
 */
public class AnnotationManager extends MedSavantServerUnicastRemoteObject implements AnnotationManagerAdapter, AnnotationColumns {

    private static final Log LOG = LogFactory.getLog(AnnotationManager.class);
    private static AnnotationManager instance;

    private AnnotationManager() throws RemoteException, SessionExpiredException {
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

    /*
     private static void installZipForProject(String sessionID, int projectID, File zip) throws IOException, ParserConfigurationException, SAXException, SQLException, SessionExpiredException {
     LOG.info("Installing zip...");
     File dir = unpackAnnotationZip(zip);
     LOG.info("... DONE");


     }
     */
    public static void addAnnotationFormat(String sessID, int annotID, int pos, String colName, String colType, boolean filterable, String alias, String desc) throws SQLException, SessionExpiredException {

        LOG.debug("Adding annotation format for " + colName);

        // remove non-alphanumeric characters from the proposed column name
        colName = colName.replaceAll("[^A-Za-z0-9]", "");

        InsertQuery query = MedSavantDatabase.AnnotationFormatTableSchema.insert(ANNOTATION_ID, annotID,
                                                                                 AnnotationFormatColumns.POSITION, pos,
                                                                                 AnnotationFormatColumns.COLUMN_NAME, colName,
                                                                                 AnnotationFormatColumns.COLUMN_TYPE, colType,
                                                                                 AnnotationFormatColumns.FILTERABLE, filterable,
                                                                                 AnnotationFormatColumns.ALIAS, alias,
                                                                                 AnnotationFormatColumns.DESCRIPTION, desc);

        Connection c = ConnectionController.connectPooled(sessID);
        c.createStatement().executeUpdate(query.toString());
        c.close();
    }

    public static int addAnnotation(String sessID, String prog, String vers, int refID, String path, boolean hasRef, boolean hasAlt, int type, boolean endInclusive) throws SQLException, SessionExpiredException {

        LOG.debug("Adding annotation...");

        TableSchema table = MedSavantDatabase.AnnotationTableSchema;
        InsertQuery query = MedSavantDatabase.AnnotationTableSchema.insert(PROGRAM, prog, VERSION, vers, REFERENCE_ID, refID,
                                                                           PATH, path, HAS_REF, hasRef, HAS_ALT, hasAlt, TYPE, type,
                                                                           IS_END_INCLUSIVE, endInclusive);

        PooledConnection c = ConnectionController.connectPooled(sessID);
        PreparedStatement stmt = c.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
        stmt.execute();
        ResultSet res = stmt.getGeneratedKeys();
        res.next();

        int annotid = res.getInt(1);

        c.close();

        return annotid;
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

        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema annTable = MedSavantDatabase.AnnotationTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(annTable.getTable());
        query.addAllColumns();
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER,
                annTable.getTable(),
                refTable.getTable(),
                BinaryConditionMS.equalTo(
                annTable.getDBColumn(REFERENCE_ID),
                refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));
        query.addCondition(BinaryConditionMS.equalTo(annTable.getDBColumn(ANNOTATION_ID), annotation_id));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        rs.next();
        Annotation result = new Annotation(
                rs.getInt(ANNOTATION_ID.getColumnName()),
                rs.getString(PROGRAM.getColumnName()),
                rs.getString(VERSION.getColumnName()),
                rs.getInt(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                rs.getString(PATH.getColumnName()),
                AnnotationType.fromInt(rs.getInt(TYPE.getColumnName())),
                rs.getBoolean(IS_END_INCLUSIVE.getColumnName()));

        return result;
    }

    @Override
    public Annotation[] getAnnotations(String sid) throws SQLException, SessionExpiredException {

        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema annTable = MedSavantDatabase.AnnotationTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(annTable.getTable());
        query.addAllColumns();
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER,
                annTable.getTable(),
                refTable.getTable(),
                BinaryConditionMS.equalTo(
                annTable.getDBColumn(REFERENCE_ID),
                refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Annotation> result = new ArrayList<Annotation>();

        while (rs.next()) {
            result.add(new Annotation(
                    rs.getInt(ANNOTATION_ID.getColumnName()),
                    rs.getString(PROGRAM.getColumnName()),
                    rs.getString(VERSION.getColumnName()),
                    rs.getInt(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                    rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                    rs.getString(PATH.getColumnName()),
                    AnnotationType.fromInt(rs.getInt(TYPE.getColumnName())),
                    rs.getBoolean(IS_END_INCLUSIVE.getColumnName())));
        }

        return result.toArray(new Annotation[0]);
    }

    /*
     * Get the annotation ids associated with the latest published table.
     */
    @Override
    public int[] getAnnotationIDs(String sessID, int projID, int refID) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS));
        query.addCondition(ComboCondition.and(
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID),
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID),
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true)));
        query.addOrdering(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);


        String a = query.toString();
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        rs.next();
        String annotationString = rs.getString(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS);

        if (annotationString == null || annotationString.isEmpty()) {
            return new int[0];
        }

        String[] split = annotationString.split(",");
        int[] result = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }

        return result;
    }

    @Override
    public AnnotationFormat getAnnotationFormat(String sessID, int annotID) throws SQLException, RemoteException, SessionExpiredException {

        TableSchema annTable = MedSavantDatabase.AnnotationTableSchema;
        SelectQuery query1 = new SelectQuery();
        query1.addFromTable(annTable.getTable());
        query1.addAllColumns();
        query1.addCondition(BinaryConditionMS.equalTo(annTable.getDBColumn(ANNOTATION_ID), annotID));

        ResultSet rs1 = ConnectionController.executeQuery(sessID, query1.toString());

        rs1.next();

        String program = rs1.getString(PROGRAM.getColumnName());
        String version = rs1.getString(VERSION.getColumnName());
        String referenceName = ReferenceManager.getInstance().getReferenceName(sessID, rs1.getInt(REFERENCE_ID.getColumnName()));
        String path = rs1.getString(PATH.getColumnName());
        boolean hasRef = rs1.getBoolean(HAS_REF.getColumnName());
        boolean hasAlt = rs1.getBoolean(HAS_ALT.getColumnName());
        boolean isEndInclusive = rs1.getBoolean(IS_END_INCLUSIVE.getColumnName());
        AnnotationType type = AnnotationType.fromInt(rs1.getInt(TYPE.getColumnName()));

        TableSchema annFormatTable = MedSavantDatabase.AnnotationFormatTableSchema;
        SelectQuery query2 = new SelectQuery();
        query2.addFromTable(annFormatTable.getTable());
        query2.addAllColumns();
        query2.addCondition(BinaryConditionMS.equalTo(annFormatTable.getDBColumn(AnnotationFormatColumns.ANNOTATION_ID), annotID));
        query2.addOrdering(annFormatTable.getDBColumn(AnnotationFormatColumns.POSITION), Dir.ASCENDING);

        ResultSet rs2 = ConnectionController.executeQuery(sessID, query2.toString());

        List<CustomField> fields = new ArrayList<CustomField>();
        while (rs2.next()) {
            fields.add(new CustomField(
                    rs2.getString(AnnotationFormatColumns.COLUMN_NAME.getColumnName()),
                    rs2.getString(AnnotationFormatColumns.COLUMN_TYPE.getColumnName()),
                    rs2.getBoolean(AnnotationFormatColumns.FILTERABLE.getColumnName()),
                    rs2.getString(AnnotationFormatColumns.ALIAS.getColumnName()),
                    rs2.getString(AnnotationFormatColumns.DESCRIPTION.getColumnName())));
        }

        return new AnnotationFormat(program, version, referenceName, path, hasRef, hasAlt, type, isEndInclusive, fields.toArray(new CustomField[0]));
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

        TableSchema table = MedSavantDatabase.AnnotationTableSchema;
        SelectQuery query1 = new SelectQuery();
        query1.addFromTable(table.getTable());
        query1.addAllColumns();
        query1.addCondition(ComboCondition.and(
                BinaryConditionMS.equalTo(table.getDBColumn(PROGRAM), info.getProgramName()),
                BinaryConditionMS.equalTo(table.getDBColumn(VERSION), info.getProgramVersion()),
                BinaryConditionMS.equalTo(
                table.getDBColumn(REFERENCE_ID),
                ReferenceManager.getInstance().getReferenceID(sessID, info.getReference()))));

        ResultSet rs1 = ConnectionController.executeQuery(sessID, query1.toString());

        // true if there is a match and false otherwise
        return rs1.next();
    }

    @Override
    public void uninstallAnnotation(String sessionID, Annotation an) throws RemoteException, SQLException, SessionExpiredException {
        int annotationID = an.getID();

        TableSchema table = MedSavantDatabase.AnnotationTableSchema;

        DeleteQuery query1 = new DeleteQuery(table.getTable());
        query1.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ANNOTATION_ID), annotationID));
        ConnectionController.executeUpdate(sessionID, query1.toString());


        TableSchema table2 = MedSavantDatabase.AnnotationFormatTableSchema;

        DeleteQuery query2 = new DeleteQuery(table2.getTable());
        query2.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ANNOTATION_ID), annotationID));
        ConnectionController.executeUpdate(sessionID, query2.toString());

        System.out.println(query1);
        System.out.println(query2);

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

        //populate
        Connection conn = ConnectionController.connectPooled(sessionID);
        conn.setAutoCommit(false);

        int i = 0;
        for (CustomField a : format.getCustomFields()) {
            addAnnotationFormat(sessionID, id, i++, id + "_" + a.getColumnName(), a.getTypeString(), a.isFilterable(), a.getAlias(), a.getDescription());
        }
        conn.commit();
        conn.setAutoCommit(true);
        conn.close();

        LOG.info("Installed to " + dir.getAbsolutePath());
    }
}
