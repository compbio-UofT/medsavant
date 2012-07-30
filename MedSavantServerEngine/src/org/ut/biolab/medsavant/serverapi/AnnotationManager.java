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
package org.ut.biolab.medsavant.serverapi;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.AnnotationColumns;
import org.ut.biolab.medsavant.db.MedSavantDatabase.AnnotationFormatColumns;
import org.ut.biolab.medsavant.db.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.format.AnnotationFormat.AnnotationType;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.logging.DBLogger;
import org.ut.biolab.medsavant.model.Annotation;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.model.AnnotationDownloadInformation;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.DirectorySettings;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.util.NetworkUtils;

/**
 *
 * @author mfiume
 */
public class AnnotationManager extends MedSavantServerUnicastRemoteObject implements AnnotationManagerAdapter, AnnotationColumns {

    private static AnnotationManager instance;

    private AnnotationManager() throws RemoteException {
    }

    public static synchronized AnnotationManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new AnnotationManager();
        }
        return instance;
    }

    @Override
    public void installAnnotationForProject(String sessID, int projectID, AnnotationDownloadInformation info) {
        if (!checkIfAnnotationIsInstalled(info)) {
            downloadAnnotation(info);
        }
    }

    @Override
    public Annotation getAnnotation(String sid,int annotation_id) throws SQLException {

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
                    annTable.getDBColumn(REFERENCE_ID.name),
                    refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));
        query.addCondition(BinaryConditionMS.equalTo(annTable.getDBColumn(ANNOTATION_ID.name), annotation_id));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        rs.next();
        Annotation result = new Annotation(
                    rs.getInt(ANNOTATION_ID.name),
                    rs.getString(PROGRAM.name),
                    rs.getString(VERSION.name),
                    rs.getInt(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                    rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                    rs.getString(PATH.name),
                    AnnotationType.fromInt(rs.getInt(TYPE.name)));

        return result;
    }

    @Override
    public Annotation[] getAnnotations(String sid) throws SQLException {

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
                    annTable.getDBColumn(REFERENCE_ID.name),
                    refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Annotation> result = new ArrayList<Annotation>();

        while (rs.next()) {
            result.add(new Annotation(
                    rs.getInt(ANNOTATION_ID.name),
                    rs.getString(PROGRAM.name),
                    rs.getString(VERSION.name),
                    rs.getInt(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                    rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                    rs.getString(PATH.name),
                    AnnotationType.fromInt(rs.getInt(TYPE.name))));
        }

        return result.toArray(new Annotation[0]);
    }

    /*
     * Get the annotation ids associated with the latest published table.
     */
    @Override
    public int[] getAnnotationIDs(String sessID, int projID, int refID) throws SQLException {

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
    public AnnotationFormat getAnnotationFormat(String sessID, int annotID) throws SQLException {

        TableSchema annTable = MedSavantDatabase.AnnotationTableSchema;
        SelectQuery query1 = new SelectQuery();
        query1.addFromTable(annTable.getTable());
        query1.addAllColumns();
        query1.addCondition(BinaryConditionMS.equalTo(annTable.getDBColumn(ANNOTATION_ID.name), annotID));

        ResultSet rs1 = ConnectionController.executeQuery(sessID, query1.toString());

        rs1.next();

        String program = rs1.getString(PROGRAM.name);
        String version = rs1.getString(VERSION.name);
        int referenceId = rs1.getInt(REFERENCE_ID.name);
        String path = rs1.getString(PATH.name);
        boolean hasRef = rs1.getBoolean(HAS_REF.name);
        boolean hasAlt = rs1.getBoolean(HAS_ALT.name);
        AnnotationType type = AnnotationType.fromInt(rs1.getInt(TYPE.name));

        TableSchema annFormatTable = MedSavantDatabase.AnnotationFormatTableSchema;
        SelectQuery query2 = new SelectQuery();
        query2.addFromTable(annFormatTable.getTable());
        query2.addAllColumns();
        query2.addCondition(BinaryConditionMS.equalTo(annFormatTable.getDBColumn(AnnotationFormatColumns.ANNOTATION_ID.name), annotID));
        query2.addOrdering(annFormatTable.getDBColumn(AnnotationFormatColumns.POSITION.name), Dir.ASCENDING);

        ResultSet rs2 = ConnectionController.executeQuery(sessID, query2.toString());

        List<CustomField> fields = new ArrayList<CustomField>();
        while (rs2.next()) {
            fields.add(new CustomField(
                    rs2.getString(AnnotationFormatColumns.COLUMN_NAME.name),
                    rs2.getString(AnnotationFormatColumns.COLUMN_TYPE.name),
                    rs2.getBoolean(AnnotationFormatColumns.FILTERABLE.name),
                    rs2.getString(AnnotationFormatColumns.ALIAS.name),
                    rs2.getString(AnnotationFormatColumns.DESCRIPTION.name)));
        }

        return new AnnotationFormat(program, version, referenceId, path, hasRef, hasAlt, type, fields.toArray(new CustomField[0]));
    }



    /**
     * HELPER FUNCTIONS *
     */
    private static final File localDirectory = new File(DirectorySettings.getMedSavantDirectory() + "/annotation");

    public static void printFile(File f) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = "";
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }

    public static void downloadAnnotation(AnnotationDownloadInformation adi) {

        String programName = adi.getProgramName();
        String version = adi.getProgramVersion();
        String reference = adi.getReference();
        URL u = null;
        try {
            u = new URL(adi.getUrl());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        File targetDir = new File(localDirectory.getAbsolutePath() + "/" + programName + "/" + version + "/" + reference);
        targetDir.mkdirs();
        try {
            String targetFileName = "tmp.zip";
            NetworkUtils.downloadFile(u, targetDir, targetFileName);
            File zip = new File(targetDir, targetFileName);
            extractFolder(zip.getAbsolutePath(), new File(zip.getAbsolutePath()).getParent());
            zip.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Unzip a zip file
     *
     * @param zipFile Path to the zip file
     * @param toPath Destination path
     * @throws ZipException
     * @throws IOException
     */
    private static void extractFolder(String zipFile, String toPath) throws ZipException, IOException {
        int BUFFER = 2048;
        File file = new File(zipFile);

        ZipFile zip = new ZipFile(file);

        //new File(newPath).mkdir();
        Enumeration zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(toPath, currentEntry);
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos,
                        BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }

            if (currentEntry.endsWith(".zip")) {
                // found a zip file, try to open
                extractFolder(destFile.getAbsolutePath(), new File(destFile.getAbsolutePath()).getParent());
            }
        }
    }

    /**
     * Get the prescribed directory for the given annotation
     * @param info The annotation whose directory is to be returned
     * @return  The directory
     */
    private static File getDirectoryForAnnotation(AnnotationDownloadInformation info) {
        return getDirectoryForAnnotation(info.getProgramName(), info.getProgramVersion(), info.getReference());
    }


    /**
     * Get the prescribed directory for the given annotation
     * @param programName The program name for this annotation
     * @param programVersion The program version for this annotation
     * @param reference The genome reference that this annotation applies to
     * @return
     */
    private static File getDirectoryForAnnotation(String programName, String programVersion, String reference) {
        return new File(localDirectory.getAbsolutePath() + "/" + reference + "/" + programName + "/" + programVersion);
    }

    /**
     * Checks that the annotation is installed
     * @param info The Annotation information (version,reference,etc) for the annotation to check
     * @return Whether or not this annotation is currently installed
     */
    private boolean checkIfAnnotationIsInstalled(AnnotationDownloadInformation info) {
        File dir = getDirectoryForAnnotation(info);
        if (dir.exists()) {
            File annotationZip = new File (dir + "/annotation.gz");
            File annotationIndex = new File (dir + "/annotation.tbi");
            File annotationDescription = new File (dir + "/annotation.xml");

            if (annotationZip.exists() && annotationIndex.exists() && annotationDescription.exists()) {
                return true;
            }
        }

        return false;
    }
}
