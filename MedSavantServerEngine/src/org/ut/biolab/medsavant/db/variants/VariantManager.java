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
package org.ut.biolab.medsavant.db.variants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.VariantFileTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.VariantPendingUpdateTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.VariantStarredTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.VarianttagTableSchema;
import org.ut.biolab.medsavant.db.Settings;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.db.util.CustomTables;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.DistinctValuesCache;
import org.ut.biolab.medsavant.db.util.FileServer;
import org.ut.biolab.medsavant.db.util.ServerDirectorySettings;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.model.AnnotationLog;
import org.ut.biolab.medsavant.model.AnnotationLog.Status;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.ScatterChartEntry;
import org.ut.biolab.medsavant.model.ScatterChartMap;
import org.ut.biolab.medsavant.model.SimplePatient;
import org.ut.biolab.medsavant.model.SimpleVariantFile;
import org.ut.biolab.medsavant.model.StarredVariant;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.serverapi.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.serverapi.AnnotationQueryUtil;
import org.ut.biolab.medsavant.serverapi.PatientQueryUtil;
import org.ut.biolab.medsavant.serverapi.ProjectQueryUtil;
import org.ut.biolab.medsavant.serverapi.ReferenceQueryUtil;
import org.ut.biolab.medsavant.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.ChromosomeComparator;
import org.ut.biolab.medsavant.util.IOUtils;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.util.MiscUtils;


/**
 *
 * @author Andrew
 */
public class VariantManager extends MedSavantServerUnicastRemoteObject implements VariantManagerAdapter {
    
    private static final Log LOG = LogFactory.getLog(VariantManager.class);
    private static final int COUNT_ESTIMATE_THRESHOLD = 1000;
    private static final int BIN_TOTAL_THRESHOLD = 10000;
    private static final int PATIENT_HEATMAP_THRESHOLD = 1000;

    private static VariantManager instance;

    private VariantManager() throws RemoteException {
    }

    public static synchronized VariantManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new VariantManager();
        }
        return instance;
    }

    /*
     * Make all variant tables live for a project. All users accessing this db will be logged out.
     */
    @Override
    public void publishVariants(String sid, int projectID) throws Exception {

        LOG.info("Beginning publish of all tables for project " + projectID);

        Connection c = ConnectionController.connectPooled(sid);

        //get update ids and references
        LOG.info("Getting map of update ids");
        List<Integer> refIds = ReferenceQueryUtil.getInstance().getReferenceIdsForProject(sid, projectID);
        Map<Integer, Integer> ref2Update = new HashMap<Integer, Integer>();
        for (Integer refId : refIds) {
            ref2Update.put(refId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectID, refId, false));
        }

        //update annotation log table
        LOG.info("Setting log status to published");
        for (Integer refId : ref2Update.keySet()) {
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, ref2Update.get(refId), Status.PUBLISHED);
        }

        //publish
        LOG.info("Terminating active sessions");
        SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sid), "Administrator (" + SessionController.getInstance().getUserForSession(sid) + ") published new variants");
        LOG.info("Publishing tables");
        for (Integer refId : ref2Update.keySet()) {
            ProjectQueryUtil.getInstance().publishVariantTable(c, projectID, refId, ref2Update.get(refId));
        }

        LOG.info("Publish complete");
        c.close();
    }


    /*
     * Make a variant table live. All users accessing this db will be logged out.
     */
    @Override
    public void publishVariants(String sid, int projectID, int referenceID, int updateID) throws Exception {
        LOG.info("Publishing table. pid:" + projectID + " refid:" + referenceID + " upid:" + updateID);
        Connection c = (ConnectionController.connectPooled(sid));
        LOG.info("Setting log status to published");
        AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, updateID, Status.PUBLISHED);
        LOG.info("Terminating active sessions");
        SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sid), "Administrator (" + SessionController.getInstance().getUserForSession(sid) + ") published new variants");
        LOG.info("Publishing table");
        ProjectQueryUtil.getInstance().publishVariantTable(c, projectID, referenceID, updateID);
        LOG.info("Publish complete");
        c.close();
    }


    /*
     * Remove an unpublished variant table.
     */
    @Override
    public void cancelPublish(String sid, int projectID, int referenceID, int updateID) throws Exception {
        LOG.info("Cancelling publish. pid:" + projectID + " refid:" + referenceID + " upid:" + updateID);
        ProjectQueryUtil.getInstance().removeTables(sid, projectID, referenceID, updateID, updateID);
        LOG.info("Cancel complete");
    }

    /*
     * Perform updates to custom vcf fields and other annotations. Will result
     * in the creation of a new, unpublished, up-to-date variant table.
     */
    @Override
    public int updateTable(String sessID, int projID, int refID, int[] annotIDs, List<CustomField> variantFields) throws Exception {

        LOG.info("Beginning new variant update");

        String user = SessionController.getInstance().getUserForSession(sessID);

        //generate directory
        LOG.info("Generating base directory");
        File basedir = ServerDirectorySettings.generateDateStampDirectory(new File("."));
        LOG.info("Base directory: " + basedir.getCanonicalPath());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + basedir);
        p.waitFor();

        //add log
        LOG.info("Adding log and generating update id");
        int updateId = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sessID, projID, refID, org.ut.biolab.medsavant.model.AnnotationLog.Action.ADD_VARIANTS, user);

        try {

            //dump existing table
            LOG.info("Dumping existing variant table");
            String existingTableName = ProjectQueryUtil.getInstance().getVariantTablename(sessID, projID, refID, false);
            File variantDumpFile = new File(basedir,"temp_" + existingTableName);
            String variantDump = variantDumpFile.getAbsolutePath();
            LOG.info("Dumping variants to file");
            VariantManagerUtils.variantsToFile(sessID, existingTableName, variantDumpFile, null, false, 0);

            //sort variants
            LOG.info("Sorting variants");
            String sortedVariants = variantDump + "_sorted";
            VariantManagerUtils.sortFileByPosition(variantDump, sortedVariants);

            //add custom vcf fields
            LOG.info("Adding custom vcf fields");
            String vcfAnnotatedVariants = sortedVariants + "_vcf";
            VariantManagerUtils.addCustomVcfFields(sortedVariants, vcfAnnotatedVariants, variantFields, DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO); //last of the default fields

            //annotate
            String outputFilename = vcfAnnotatedVariants + "_annotated";
            LOG.info("File containing annotated variants, sorted by position: " + outputFilename);
            VariantManagerUtils.annotateTDF(sessID,vcfAnnotatedVariants, outputFilename, annotIDs);

            //split
            File splitDir = new File(basedir,"splitDir");
            splitDir.mkdir();
            LOG.info("Splitting annotation file into multiple files by file ID");
            for (File f : splitDir.listFiles()) {
                VariantManagerUtils.removeTemp(f);
            }
            VariantManagerUtils.splitFileOnColumn(splitDir, outputFilename, 1);
            String outputFilenameMerged = outputFilename + "_merged";

            //merge
            LOG.info("File containing annotated variants, sorted by file: " + outputFilenameMerged);
            LOG.info("Merging files");
            VariantManagerUtils.concatenateFilesInDir(splitDir, outputFilenameMerged);

            //create the staging table
            LOG.info("Creating new variant table for resulting variants");
            ProjectQueryUtil.getInstance().setCustomVariantFields(sessID, projID, refID, updateId, variantFields);
            String tableName = ProjectQueryUtil.getInstance().createVariantTable(sessID, projID, refID, updateId, annotIDs, true);
            String tableNameSub = ProjectQueryUtil.getInstance().createVariantTable(sessID, projID, refID, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sessID,projID, refID), false, true);

            //upload to staging table
            LOG.info("Uploading variants to table: " + tableName);
            uploadFileToVariantTable(sessID, new File(outputFilenameMerged), tableName);

            //upload to sub table
            File subFile = new File(outputFilenameMerged + "_sub");
            LOG.info("Generating sub file: " + subFile.getAbsolutePath());
            VariantManagerUtils.generateSubset(new File(outputFilenameMerged), subFile);
            LOG.info("Importing to: " + tableNameSub);
            uploadFileToVariantTable(sessID, subFile, tableNameSub);
            //ProjectQueryUtil.getInstance().addSubsetInfoToMap(sid, projectId, referenceId, updateId, tableNameSub, ProjectQueryUtil.getInstance().getMultiplier(sid, tableName, tableNameSub));
            
            //add entries to tablemap
            ProjectQueryUtil.getInstance().addTableToMap(sessID, projID, refID, updateId, false, tableName, tableNameSub);
            
            //cleanup
            LOG.info("Dropping old table(s)");
            int newestId = ProjectQueryUtil.getInstance().getNewestUpdateId(sessID, projID, refID, true);
            int minId = -1;
            int maxId = newestId-1;
            ProjectQueryUtil.getInstance().removeTables(sessID, projID, refID, minId, maxId);

            //TODO: remove files

            //TODO: server logs

            //set as pending
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sessID, updateId, Status.PENDING);

            return updateId;

        } catch (Exception e) {
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sessID, updateId, Status.ERROR);
            throw e;
        }
    }

    /*
     * Start the upload process for new vcf files. Will result in the creation
     * of a new, unpublished, up-to-date variant table.
     */
    @Override
    public int uploadVariants(String sid, RemoteInputStream[] fileStreams, String[] fileNames, int projectId, int referenceId, String[][] variantTags, boolean includeHomoRef) throws RemoteException, IOException, Exception {
        File[] vcfFiles = new File[fileStreams.length];

        for (int i = 0; i < fileStreams.length; i++) {
            LOG.info("Sending " + fileNames[i]);
            vcfFiles[i] = FileServer.getInstance().sendFile(fileStreams[i], fileNames[i]);
        }

        return uploadVariants(sid, vcfFiles, fileNames, projectId, referenceId, variantTags, includeHomoRef);
    }
    
    /*
     * Helper that does all the actual work of uploading new vcf files.
     */
    private int uploadVariants(String sessID, File[] vcfFiles, String[] fileNames, int projID, int refID, String[][] variantTags, boolean includeHomoRef) throws Exception {
        
        LOG.info("Beginning new vcf upload");

        String user = SessionController.getInstance().getUserForSession(sessID);

        //generate directory
        LOG.info("Generating base directory");
        File baseDir = ServerDirectorySettings.generateDateStampDirectory(new File("."));
        LOG.info("Base directory: " + baseDir.getAbsolutePath());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir);
        p.waitFor();

        //add log
        LOG.info("Adding log and generating update id");
        int updateID = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sessID, projID, refID, AnnotationLog.Action.ADD_VARIANTS, user);
        
        try {
            ProjectQueryUtil projMgr = ProjectQueryUtil.getInstance();
            AnnotationQueryUtil annotMgr = AnnotationQueryUtil.getInstance();

            //dump existing table
            String existingTableName = projMgr.getVariantTablename(sessID, projID, refID, false);
            IOUtils.checkForWorldExecute(baseDir);
            File existingVariantsFile = new File(baseDir, "temp_proj" + projID + "_ref" + refID + "_update" + updateID);
            
            LOG.info("Dumping variants to file");
            VariantManagerUtils.variantsToFile(sessID, existingTableName, existingVariantsFile, null, true, 0);

            //create the staging table
            LOG.info("Creating new variant table for resulting variants");
            projMgr.setCustomVariantFields(
                    sessID, projID, refID, updateID,
                    projMgr.getCustomVariantFields(sessID, projID, refID, projMgr.getNewestUpdateId(sessID, projID, refID, false)));
            String tableName = projMgr.createVariantTable(sessID, projID, refID, updateID, annotMgr.getAnnotationIds(sessID, projID, refID), true);
            String tableNameSub = projMgr.createVariantTable(sessID, projID, refID, updateID, annotMgr.getAnnotationIds(sessID, projID, refID), false, true);

            //upload dump to staging table
            LOG.info("Uploading variants to table: " + tableName);
            uploadFileToVariantTable(sessID, existingVariantsFile, tableName);

            VCFIterator parser = new VCFIterator(vcfFiles, baseDir, updateID, includeHomoRef);
            File f;
            while ((f = parser.next()) != null) {
                
                List<String> filesUsed = new ArrayList<String>();

                String tempFilename = f.getAbsolutePath();
                filesUsed.add(tempFilename);
                String currentFilename = tempFilename;

                //add custom fields
                LOG.info("Adding custom vcf fields");
                List<CustomField> customFields = projMgr.getCustomVariantFields(sessID, projID, refID, projMgr.getNewestUpdateId(sessID, projID, refID, false));
                if (!customFields.isEmpty()) {
                    String customFieldFilename = currentFilename + "_vcf";
                    filesUsed.add(customFieldFilename);
                    VariantManagerUtils.addCustomVcfFields(currentFilename, customFieldFilename, customFields, DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO); //last of the default fields
                    currentFilename = customFieldFilename;
                }

                //get annotation ids
                int[] annotIDs = annotMgr.getAnnotationIds(sessID,projID, refID);

                if (annotIDs.length > 0) {

                    //sort variants
                    LOG.info("Sorting variants");
                    String sortedVariants = currentFilename + "_sorted";
                    filesUsed.add(sortedVariants);
                    VariantManagerUtils.sortFileByPosition(currentFilename, sortedVariants);
                    VariantManagerUtils.logFileSize(sortedVariants);
                    currentFilename = sortedVariants;

                    //annotate
                    String annotatedFilename = currentFilename + "_annotated";
                    filesUsed.add(annotatedFilename);
                    LOG.info("File containing annotated variants, sorted by position: " + annotatedFilename);
                    VariantManagerUtils.annotateTDF(sessID, currentFilename, annotatedFilename, annotIDs);
                    VariantManagerUtils.logFileSize(annotatedFilename);
                    currentFilename = annotatedFilename;
                }

                if (annotIDs.length > 0) {

                    //split
                    File splitDir = new File(baseDir,"splitDir");
                    splitDir.mkdir();
                    LOG.info("Splitting annotation file into multiple files by file ID");
                    VariantManagerUtils.splitFileOnColumn(splitDir, currentFilename, 1);

                    //merge
                    LOG.info("Merging files");
                    String outputFilenameMerged = tempFilename + "_annotated_merged";
                    filesUsed.add(outputFilenameMerged);
                    LOG.info("File containing annotated variants, sorted by file: " + outputFilenameMerged);
                    VariantManagerUtils.concatenateFilesInDir(splitDir, outputFilenameMerged);
                    VariantManagerUtils.logFileSize(outputFilenameMerged);
                    currentFilename = outputFilenameMerged;
                }

                //upload to staging table
                LOG.info("Uploading variants to table: " + tableName);
                uploadFileToVariantTable(sessID,new File(currentFilename), tableName);
                
                //cleanup
                System.gc();
                for (String filename : filesUsed) {
                    boolean deleted = (new File(filename)).delete();
                    LOG.info("Deleting " + filename + " - " + (deleted ? "successful" : "failed"));
                }

            }
            
            //create sub table dump
            File subDump = new File(baseDir, "temp_proj" + projID + "_ref" + refID + "_update" + updateID + "_sub");
            LOG.info("Dumping variants to file for sub table");
            VariantManagerUtils.variantsToFile(sessID,tableName, subDump, null, true, VariantManagerUtils.determineStepForSubset(getNumFilteredVariantsHelper(sessID, tableName, new Condition[][]{})));
            
            //upload to sub table
            LOG.info("Uploading variants to table: " + tableNameSub);
            uploadFileToVariantTable(sessID, subDump, tableNameSub);
            //ProjectQueryUtil.getInstance().addSubsetInfoToMap(sid, projectId, referenceId, updateId, tableNameSub, ProjectQueryUtil.getInstance().getMultiplier(sid, tableName, tableNameSub)); 

            //add entries to tablemap
            projMgr.addTableToMap(sessID, projID, refID, updateID, false, tableName, tableNameSub);
            
            //add tags to upload
            LOG.info("Adding upload tags");
            VariantManagerUtils.addTagsToUpload(sessID, updateID, variantTags);

            //add entries to file table
            LOG.info("Adding entries to file table");
            for (int i = 0; i < fileNames.length; i++) {
                addEntryToFileTable(sessID, updateID, i, fileNames[i]);
            }

            //cleanup
            LOG.info("Dropping old table(s)");
            int newestId = projMgr.getNewestUpdateId(sessID, projID, refID, true);
            int minId = -1;
            int maxId = newestId-1;
            projMgr.removeTables(sessID, projID, refID, minId, maxId);

            //TODO: remove files

            //TODO: server logs

            //set as pending
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sessID, updateID, Status.PENDING);

            return updateID;
            
        } catch (Exception e) {
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sessID, updateID, Status.ERROR);
            throw e;
        }
    }

    @Override
    public int removeVariants(String sid, int projectId, int referenceId, List<SimpleVariantFile> files) throws Exception {
        LOG.info("Beginning removal of variants");

        String user = SessionController.getInstance().getUserForSession(sid);

        //generate directory
        LOG.info("Generating base directory");
        File baseDir = ServerDirectorySettings.generateDateStampDirectory(new File("."));
        LOG.info("Base directory: " + baseDir.getCanonicalPath());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir);
        p.waitFor();

        //add log
        LOG.info("Adding log and generating update id");
        int updateId = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sid, projectId, referenceId, org.ut.biolab.medsavant.model.AnnotationLog.Action.REMOVE_VARIANTS, user);

        try {

            //dump existing except for files
            String existingTableName = ProjectQueryUtil.getInstance().getVariantTablename(sid, projectId, referenceId, false);
            File existingVariantsFile = new File(baseDir,"temp_proj" + projectId + "_ref" + referenceId + "_update" + updateId);
            LOG.info("Dumping variants to file");
            String conditions = "";
            for (int i = 0; i < files.size(); i++) {
                conditions +=
                        "!(" + DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID + "=" + files.get(i).getUploadId() +
                        " AND " + DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID + "=" + files.get(i).getFileId() + ")";
                if (i != files.size()-1) {
                    conditions += " AND ";
                }
            }
            VariantManagerUtils.variantsToFile(sid, existingTableName, existingVariantsFile, conditions, true, 0);

            //create the staging table
            LOG.info("Creating new variant table for resulting variants");
            ProjectQueryUtil.getInstance().setCustomVariantFields(
                    sid, projectId, referenceId, updateId,
                    ProjectQueryUtil.getInstance().getCustomVariantFields(sid, projectId, referenceId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectId, referenceId, false)));
            String tableName = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), true);
            String tableNameSub = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), false, true);

            //upload to staging table
            LOG.info("Uploading variants to table: " + tableName);
            uploadFileToVariantTable(sid, existingVariantsFile, tableName);

            //upload to sub table
            File subFile = new File(existingVariantsFile.getAbsolutePath() + "_sub");
            LOG.info("Generating sub file: " + subFile.getAbsolutePath());
            VariantManagerUtils.generateSubset(existingVariantsFile, subFile);
            LOG.info("Importing to: " + tableNameSub);
            uploadFileToVariantTable(sid, subFile, tableNameSub);
            //ProjectQueryUtil.getInstance().addSubsetInfoToMap(sid, projectId, referenceId, updateId, tableNameSub, ProjectQueryUtil.getInstance().getMultiplier(sid, tableName, tableNameSub));

            //add entries to tablemap
            ProjectQueryUtil.getInstance().addTableToMap(sid, projectId, referenceId, updateId, false, tableName, tableNameSub);
            
            //cleanup
            LOG.info("Dropping old table(s)");
            int newestId = ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectId, referenceId, true);
            int minId = -1;
            int maxId = newestId-1;
            ProjectQueryUtil.getInstance().removeTables(sid, projectId, referenceId, minId, maxId);

            //TODO: remove files

            //TODO: server logs

            //set as pending
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, updateId, Status.PENDING);

            return updateId;

        } catch (Exception e) {
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, updateId, Status.ERROR);
            throw e;
        }
    }

    @Override
    public RemoteInputStream exportVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException, IOException, InterruptedException {

        //generate directory
        File baseDir = ServerDirectorySettings.generateDateStampDirectory(new File("."));
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir.getCanonicalPath());
        p.waitFor();
        File file = new File(baseDir, "export.tdf");
        System.out.println(baseDir.getCanonicalPath());

        //select into
        System.out.println("starting select into");
        long start = System.nanoTime();
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessionId,ProjectQueryUtil.getInstance().getVariantTablename(sessionId,projectId, referenceId, true));
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        addConditionsToQuery(query, conditions);
        query.addOrderings(new Column[]{table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION)});
        String intoString =
                "INTO OUTFILE \"" + file.getAbsolutePath().replaceAll("\\\\", "/") + "\""
                + " FIELDS TERMINATED BY ',' ENCLOSED BY '\"'"
                + " LINES TERMINATED BY '\\r\\n' ";
        String queryString = query.toString().replace("FROM", intoString + "FROM");
        ConnectionController.execute(sessionId, queryString);
        System.out.println("done: " + (System.nanoTime()-start));


        /*//zip
        int BUFFER = 2048;
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("c:\\zip\\myfigs.zip")));
        byte data[] = new byte[BUFFER];

        File f = new File(".");
        String files[] = f.list();

        for (int i=0; i<files.length; i++) {
            System.out.println("Adding: "+files[i]);
            FileInputStream fi = new
            FileInputStream(files[i]);
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(files[i]);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
        out.close();*/

        return (new SimpleRemoteInputStream(new FileInputStream(file.getAbsolutePath()))).export();
    }
    @Override
    public TableSchema getCustomTableSchema(String sessionId, int projectId, int referenceId) throws SQLException, RemoteException {
        return CustomTables.getInstance().getCustomTableSchema(sessionId,ProjectQueryUtil.getInstance().getVariantTablename(sessionId,projectId, referenceId, true));
    }


    @Override
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, int start, int limit) throws SQLException, RemoteException {
        return getVariants(sessionId,projectId, referenceId, new Condition[1][], start, limit);
    }

    @Override
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException {
        return getVariants(sessionId, projectId, referenceId, conditions, start, limit, null);
    }

    @Override
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, Condition[][] conditions, int start, int limit, Column[] order) throws SQLException, RemoteException {
        return getVariants(sessionId, projectId, referenceId, conditions, start, limit, order, null);
    }

    @Override
    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, Condition[][] conditions, int start, int limit, Column[] order, Column[] columns) throws SQLException, RemoteException {

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessionId,ProjectQueryUtil.getInstance().getVariantTablename(sessionId,projectId, referenceId, true));
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        if (columns == null) {
            query.addAllColumns();
        } else {
            query.addColumns(columns);
        }
        addConditionsToQuery(query, conditions);
        if (order != null) {
            query.addOrderings(order);
        }

        String queryString = query.toString();
        if (limit != -1) {
            if (start != -1) {
                queryString += " LIMIT " + start + ", " + limit;
            } else {
                queryString += " LIMIT " + limit;
            }
        }
        System.out.println(queryString);
        ResultSet rs = ConnectionController.executeQuery(sessionId, queryString);

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberColumns = rsMetaData.getColumnCount();

        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()) {
            Object[] v = new Object[numberColumns];
            for (int i = 1; i <= numberColumns; i++) {
                v[i - 1] = rs.getObject(i);
            }
            result.add(v);
        }

        return result;
    }

    @Override
    public double[] getExtremeValuesForColumn(String sid, String tablename, String columnname) throws SQLException, RemoteException {

        String dbName = SessionController.getInstance().getDatabaseForSession(sid);
        if (DistinctValuesCache.isCached(dbName, tablename, columnname)) {
            try {
                double[] result = DistinctValuesCache.getCachedRange(dbName, tablename, columnname);
                if (result != null) return result;
            } catch (Exception ex) {
                LOG.warn("Unable to get cached distinct values for " + dbName + "/" + tablename + "/" + columnname, ex);
            }
        }

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addCustomColumns(FunctionCall.min().addColumnParams(table.getDBColumn(columnname)));
        query.addCustomColumns(FunctionCall.max().addColumnParams(table.getDBColumn(columnname)));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());
        rs.next();

        double[] result = new double[]{rs.getDouble(1), rs.getDouble(2)};
        List<Double> list = new ArrayList<Double>();
        list.add(result[0]);
        list.add(result[1]);
        DistinctValuesCache.cacheResults(dbName, tablename, columnname, (List)list);

        return result;
    }

    /**
     * A return value of null indicates too many values.
     */
    @Override
    public List<String> getDistinctValuesForColumn(String sessID, String tableName, String columnName, boolean useCache) throws SQLException, RemoteException {

        String dbName = SessionController.getInstance().getDatabaseForSession(sessID);
        if (useCache && DistinctValuesCache.isCached(dbName, tableName, columnName)) {
            try {
                List<String> result = DistinctValuesCache.getCachedStringList(dbName, tableName, columnName);
                return result;
            } catch (Exception ex) {
                LOG.warn("Unable to get cached distinct values for " + dbName + "/" + tableName + "/" + columnName, ex);
            }
        }

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tableName);

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.setIsDistinct(true);
        query.addColumns(table.getDBColumn(columnName));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString() + (useCache ? " LIMIT " + DistinctValuesCache.CACHE_LIMIT : ""));

        List<String> result = new ArrayList<String>();
        while (rs.next()) {
            String val = rs.getString(1);
            if (val == null) {
                result.add("");
            } else {
                result.add(val);
            }
        }

        if (useCache) {
            if (result.size() == DistinctValuesCache.CACHE_LIMIT) {
                DistinctValuesCache.cacheResults(dbName, tableName, columnName, null);
                return null;
            } else {
                Collections.sort(result);
                DistinctValuesCache.cacheResults(dbName, tableName, columnName, (List)result);
            }
        }

        return result;
    }

    @Override
    public int getNumFilteredVariants(String sid, int projectId, int referenceId) throws SQLException, RemoteException {
        return getNumFilteredVariants(sid,projectId, referenceId, new Condition[0][], true);
    }

    @Override
    public int getNumFilteredVariants(String sid,int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException {
        return getNumFilteredVariants(sid,projectId, referenceId, conditions, false);
    }

    public int getNumFilteredVariants(String sid,int projectId, int referenceId, Condition[][] conditions, boolean forceExact) throws SQLException, RemoteException {

        //String name = ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId, true);
        Object[] variantTableInfo = ProjectQueryUtil.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String)variantTableInfo[0];
        String tablenameSub = (String)variantTableInfo[1];
        float subMultiplier = (Float)variantTableInfo[2];

        if (tablename == null) {
            return -1;
        }

        //try to get a reasonable approximation
        if (tablenameSub != null && !forceExact) {
            int estimate = (int)(getNumFilteredVariantsHelper(sid, tablenameSub, conditions) * subMultiplier);
            if (estimate >= COUNT_ESTIMATE_THRESHOLD) {
                return estimate;
            }
        }

        //approximation not good enough. use actual data
        return getNumFilteredVariantsHelper(sid, tablename, conditions);
    }

    public int getNumFilteredVariantsHelper(String sid, String tablename, Condition[][] conditions) throws SQLException, RemoteException {
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        rs.next();
        return rs.getInt(1);
    }

    /*
     * Convenience method
     */
    @Override
    public int getNumVariantsForDnaIds(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds) throws SQLException, RemoteException {

        if (dnaIds.isEmpty()) return 0;

        String name = ProjectQueryUtil.getInstance().getVariantTablename(sid, projectId, referenceId, true);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid, name);

        Condition[] dnaConditions = new Condition[dnaIds.size()];
        for (int i = 0; i < dnaIds.size(); i++) {
            dnaConditions[i] = BinaryConditionMS.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaIds.get(i));
        }

        Condition[] c1 = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c1[i] = ComboCondition.and(conditions[i]);
        }

        Condition[] finalCondition = new Condition[]{ComboCondition.and(ComboCondition.or(dnaConditions), ComboCondition.or(c1))};

        return getNumFilteredVariants(sid, projectId, referenceId, new Condition[][]{finalCondition});
    }

    @Override
    public Map<Range, Long> getFilteredFrequencyValuesForNumericColumn(String sid, int projectId, int referenceId, Condition[][] conditions, CustomField column, boolean logBins) throws SQLException, RemoteException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getNumFilteredVariants(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectQueryUtil.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String)variantTableInfo[0];
        String tablenameSub = (String)variantTableInfo[1];
        float multiplier = (Float)variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }
        
        Range range = new Range(getExtremeValuesForColumn(sid, table.getTableName(), column.getColumnName()));
        double binSize = MiscUtils.generateBins(column, range, logBins);
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);

        String round;
        if (logBins) {
            round = "floor(log10(" + column.getColumnName() + ")) as m";
        } else {
            round = "floor(" + column.getColumnName() + " / " + binSize + ") as m";
        }

        String query = q.toString().replace("COUNT(*)", "COUNT(*), " + round);
        query += " GROUP BY m ORDER BY m ASC";

        ResultSet rs = ConnectionController.executeQuery(sid, query);

        Map<Range, Long> results = new TreeMap<Range, Long>();
        while (rs.next()) {
            int binNo = rs.getInt(2);
            Range r;
            if (logBins) {
                r = new Range(Math.pow(10, binNo), Math.pow(10, binNo+1));
            } else {
                r = new Range(binNo*binSize, (binNo+1)*binSize);
            }
            long count = (long)(rs.getLong(1) * multiplier);
            results.put(r, count);
        }

        return results;
    }

    @Override
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sid, int projectId, int referenceId, Condition[][] conditions, String columnAlias) throws SQLException, RemoteException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getNumFilteredVariants(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectQueryUtil.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String)variantTableInfo[0];
        String tablenameSub = (String)variantTableInfo[1];
        float multiplier = (Float)variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }

        DbColumn column = table.getDBColumnByAlias(columnAlias);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(column);
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);
        q.addGroupings(column);
        
        if (column.getColumnNameSQL().equals(DefaultVariantTableSchema.COLUMNNAME_OF_ALT) || column.getColumnNameSQL().equals(DefaultVariantTableSchema.COLUMNNAME_OF_REF)) {
            q.addCondition(createNucleotideCondition(column));
        }

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        Map<String, Integer> map = new HashMap<String, Integer>();

        while (rs.next()) {
            String key = rs.getString(1);
            if (key == null) {
                key = "";
            }
            map.put(key, (int)(rs.getInt(2) * multiplier));
        }

        return map;
    }

    @Override
    public ScatterChartMap getFilteredFrequencyValuesForScatter(String sid, int projectId, int referenceId, Condition[][] conditions, String columnnameX, String columnnameY, boolean columnXCategorical, boolean columnYCategorical, boolean sortKaryotypically) throws SQLException, RemoteException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getNumFilteredVariants(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectQueryUtil.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String)variantTableInfo[0];
        String tablenameSub = (String)variantTableInfo[1];
        float multiplier = (Float)variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }
        
        DbColumn columnX = table.getDBColumn(columnnameX);   
        DbColumn columnY = table.getDBColumn(columnnameY);     

        double binSizeX = 0;
        if (!columnXCategorical) {
            Range rangeX = new Range(getExtremeValuesForColumn(sid, table.getTableName(), columnnameX));
            binSizeX = MiscUtils.generateBins(new CustomField(columnnameX, columnX.getTypeNameSQL() + "(" + columnX.getTypeLength() + ")", false, "", ""), rangeX, false);
        }
        
        double binSizeY = 0;
        if (!columnYCategorical) {
            Range rangeY = new Range(getExtremeValuesForColumn(sid, table.getTableName(), columnnameY));
            binSizeY = MiscUtils.generateBins(new CustomField(columnnameY, columnY.getTypeNameSQL() + "(" + columnY.getTypeLength() + ")", false, "", ""), rangeY, false);
        }

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        //q.addColumns(columnX, columnY);
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);
        
        if (columnnameX.equals(DefaultVariantTableSchema.COLUMNNAME_OF_ALT) || columnnameX.equals(DefaultVariantTableSchema.COLUMNNAME_OF_REF)) {
            q.addCondition(createNucleotideCondition(columnX));
        }
        
        if (columnnameY.equals(DefaultVariantTableSchema.COLUMNNAME_OF_ALT) || columnnameY.equals(DefaultVariantTableSchema.COLUMNNAME_OF_REF)) {
            q.addCondition(createNucleotideCondition(columnY));
        }

        String m = columnnameX + " as m";
        if (!columnXCategorical) {
            m = "floor(" + columnnameX + " / " + binSizeX + ") as m";
        }
        String n = columnnameY + " as n";
        if (!columnYCategorical) {
            n = "floor(" + columnnameY + " / " + binSizeY + ") as n";
        }
        String query = q.toString().replace("COUNT(*)", "COUNT(*), " + m + ", " + n);
        query += " GROUP BY m, n ORDER BY m, n ASC";


        //String round = "floor(" + columnname + " / " + binSize + ") as m";

        ResultSet rs = ConnectionController.executeQuery(sid, query);

        List<ScatterChartEntry> entries = new ArrayList<ScatterChartEntry>();
        List<String> xRanges = new ArrayList<String>();
        List<String> yRanges = new ArrayList<String>();

        while (rs.next()) {
            String x = rs.getString(2);
            String y = rs.getString(3);
            
            if (x == null) x = "null"; //prevents NPE when sorting below
            if (y == null) y = "null";
            
            if (!columnXCategorical) {
                x = MiscUtils.doubleToString(Integer.parseInt(x) * binSizeX, 2) + " - " + MiscUtils.doubleToString(Integer.parseInt(x) * binSizeX + binSizeX, 2);
            }
            if (!columnYCategorical) {
                y = MiscUtils.doubleToString(Integer.parseInt(y) * binSizeY, 2) + " - " + MiscUtils.doubleToString(Integer.parseInt(y) * binSizeY + binSizeY, 2);
            }
            ScatterChartEntry entry = new ScatterChartEntry(x, y, (int)(rs.getInt(1)*multiplier));
            entries.add(entry);
            if (!xRanges.contains(entry.getXRange())) {
                xRanges.add(entry.getXRange());
            }
            if (!yRanges.contains(entry.getYRange())) {
                yRanges.add(entry.getYRange());
            }
        }

        if (sortKaryotypically) {
            Collections.sort(xRanges, new ChromosomeComparator());
        } else if (columnXCategorical) {
            Collections.sort(xRanges);
        }
        if (columnYCategorical) {
            Collections.sort(yRanges);
        }

        return new ScatterChartMap(xRanges, yRanges, entries);
    }

    /*
     * Convenience method
     */
    @Override
    public int getNumVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, long start, long end) throws SQLException, RemoteException {

        String name = ProjectQueryUtil.getInstance().getVariantTablename(sid, projectId, referenceId, true);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid, name);

        Condition[] rangeConditions = new Condition[] {
            BinaryCondition.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom),
            BinaryCondition.greaterThan(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), start, true),
            BinaryCondition.lessThan(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), end, false)
        };

        Condition[] c1 = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c1[i] = ComboCondition.and(conditions[i]);
        }

        Condition[] finalCondition = new Condition[]{ComboCondition.and(ComboCondition.and(rangeConditions), ComboCondition.or(c1))};

        return getNumFilteredVariants(sid, projectId, referenceId, new Condition[][]{finalCondition});
    }

    @Override
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, int binsize) throws SQLException, RemoteException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getNumFilteredVariants(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectQueryUtil.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String)variantTableInfo[0];
        String tablenameSub = (String)variantTableInfo[1];
        float multiplier = (Float)variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }

        //TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId, true));

        SelectQuery queryBase = new SelectQuery();
        queryBase.addFromTable(table.getTable());

        queryBase.addColumns(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM));

        String roundFunction = "ROUND(" + DefaultVariantTableSchema.COLUMNNAME_OF_POSITION + "/" + binsize + ",0)";

        queryBase.addCustomColumns(FunctionCall.countAll());
        queryBase.addGroupings(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM));


        addConditionsToQuery(queryBase, conditions);

        String query = queryBase.toString().replace("COUNT(*)", "COUNT(*)," + roundFunction) + "," + roundFunction;

        long start = System.nanoTime();
        ResultSet rs = ConnectionController.executeQuery(sid, query);
        System.out.println(query);
        System.out.println("  time:" + (System.nanoTime() - start)/1000000000);

        Map<String, Map<Range, Integer>> results = new HashMap<String, Map<Range, Integer>>();
        while (rs.next()) {

            String chrom = rs.getString(1);

            Map<Range, Integer> chromMap;
            if (!results.containsKey(chrom)) {
                chromMap = new HashMap<Range, Integer>();
            } else {
                chromMap = results.get(chrom);
            }

            int binNo = rs.getInt(3);
            Range binRange = new Range(binNo * binsize, (binNo + 1) * binsize);

            int count = (int)(rs.getInt(2) * multiplier);

            chromMap.put(binRange, count);
            results.put(chrom, chromMap);
        }

        return results;
    }

    @Override
    public void uploadFileToVariantTable(String sid, File file, String tableName) throws SQLException {

        // TODO: for some reason the connection is closed going into this function
        Connection c = ConnectionController.connectPooled(sid);

        Statement s = c.createStatement();
        s.setQueryTimeout(60 * 60); // 1 hour
        s.execute(
                "LOAD DATA LOCAL INFILE '" + file.getAbsolutePath().replaceAll("\\\\", "/") + "' "
                + "INTO TABLE " + tableName + " "
                + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' "
                + "LINES TERMINATED BY '\\r\\n';");
        c.close();
    }

    @Override
    public int getNumPatientsWithVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int start, int end) throws SQLException, RemoteException {

        //TODO: approximate counts??
        //might not be a good idea... don't want to miss a dna id

        TableSchema table = getCustomTableSchema(sid,projectId, referenceId);
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns("COUNT(DISTINCT " + DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID + ")");
        addConditionsToQuery(q, conditions);

        Condition[] cond = new Condition[3];
        cond[0] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom);
        cond[1] = new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), start);
        cond[2] = new BinaryCondition(BinaryCondition.Op.LESS_THAN, table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), end);
        q.addCondition(ComboCondition.and(cond));

        String query = q.toString();
        query = query.replaceFirst("'", "").replaceFirst("'", "");

        ResultSet rs = ConnectionController.executeQuery(sid, query);
        rs.next();

        int numrows = rs.getInt(1);

        return numrows;
    }

    @Override
    public void addConditionsToQuery(SelectQuery query, Condition[][] conditions) {
        Condition[] c = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c[i] = ComboCondition.and(conditions[i]);
        }
        query.addCondition(ComboCondition.or(c));
    }

    @Override
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIds(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds, int limit) throws SQLException, RemoteException {

        Map<String, List<String>> results = new HashMap<String, List<String>>();

        TableSchema table = getCustomTableSchema(sid,projectId, referenceId);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID),
                table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM),
                table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION));
        addConditionsToQuery(query, conditions);
        Condition[] dnaIdConditions = new Condition[dnaIds.size()];
        for (int i = 0; i < dnaIds.size(); i++) {
            dnaIdConditions[i] = BinaryConditionMS.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaIds.get(i));
            results.put(dnaIds.get(i), new ArrayList<String>());
        }
        query.addCondition(ComboCondition.or(dnaIdConditions));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString() + ((limit == -1) ? "" : (" LIMIT " + limit)));

        while (rs.next()) {
            results.get(rs.getString(1)).add(rs.getString(2) + ":" + (rs.getLong(3) - 100) + "-" + (rs.getLong(3) + 100));
        }

        return results;
    }

    @Override
    public Map<String, Integer> getNumVariantsInFamily(String sid, int projectId, int referenceId, String familyId, Condition[][] conditions) throws SQLException, RemoteException {

        //TODO: approximate counts

        String name = ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId, true);

        if (name == null) {
            return null;
        }

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,name);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID));
        q.addCustomColumns(FunctionCall.countAll());
        q.addGroupings(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID));
        addConditionsToQuery(q, conditions);

        Map<String, String> patientToDNAIDMap = PatientQueryUtil.getInstance().getDNAIdsForFamily(sid,projectId, familyId);
        Map<String, List<String>> betterPatientToDNAIDMap = new HashMap<String, List<String>>();

        List<String> dnaIDs = new ArrayList<String>();
        for (String patientID : patientToDNAIDMap.keySet()) {
            String dnaIDString = patientToDNAIDMap.get(patientID);
            List<String> idList = new ArrayList<String>();
            for (String dnaID : dnaIDString.split(",")) {
                if (dnaID != null && !dnaID.isEmpty()) {
                    dnaIDs.add(dnaID);
                    idList.add(dnaID);
                }
            }
            betterPatientToDNAIDMap.put(patientID, idList);
        }
        patientToDNAIDMap = null; // we don't need it anymore; use betterPatientToDNAIDMap instead

        Map<String, Integer> dnaIDsToCountMap = new HashMap<String, Integer>();

        if (!dnaIDs.isEmpty()) {

            Condition[] dnaIDConditions = new Condition[dnaIDs.size()];

            int i = 0;
            for (String dnaID : dnaIDs) {
                dnaIDConditions[i] = BinaryCondition.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaID);
                i++;
            }

            q.addCondition(ComboCondition.or(dnaIDConditions));

            ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

            while (rs.next()) {
                dnaIDsToCountMap.put(rs.getString(1), rs.getInt(2));
            }
        }

        Map<String, Integer> patientIDTOCount = new HashMap<String, Integer>();
        for (String patientID : betterPatientToDNAIDMap.keySet()) {
            int count = 0;
            for (String dnaID : betterPatientToDNAIDMap.get(patientID)) {
                if (dnaIDsToCountMap.containsKey(dnaID)) {
                    count += dnaIDsToCountMap.get(dnaID);
                }
            }
            patientIDTOCount.put(patientID, count);
        }

        return patientIDTOCount;
    }

    @Override
    public void cancelUpload(String sid,int uploadId, String tableName) {
        try {

            //remove log entry
            AnnotationLogQueryUtil.getInstance().removeAnnotationLogEntry(sid,uploadId);

            //drop staging table
            DBUtil.dropTable(sid,tableName);

        } catch (Exception ex) {
            LOG.warn("Error cancelling upload " + uploadId + " for " + tableName, ex);
        }
    }

    @Override
    public void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException {

        Connection conn = ConnectionController.connectPooled(sid);
        TableSchema variantTagTable = MedSavantDatabase.VarianttagTableSchema;

        conn.setAutoCommit(false);

        //add tags
        for (int i = 0; i < variantTags.length && !Thread.currentThread().isInterrupted(); i++) {
            InsertQuery query = new InsertQuery(variantTagTable.getTable());
            query.addColumn(variantTagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadID);
            query.addColumn(variantTagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGKEY), variantTags[i][0]);
            query.addColumn(variantTagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGVALUE), variantTags[i][1]);

            conn.createStatement().executeUpdate(query.toString());
        }
        if (Thread.currentThread().isInterrupted()) {
            conn.rollback();
        } else {
            conn.commit();
        }
        conn.setAutoCommit(true);
    }

    public void removeTags(String sid, int uploadId) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttagTableSchema;

        DeleteQuery q = new DeleteQuery(table.getTable());
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadId));

        ConnectionController.execute(sid, q.toString());
    }

    @Override
    public List<String> getDistinctTagNames(String sid) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttagTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGKEY));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        List<String> tagNames = new ArrayList<String>();
        while (rs.next()) {
            tagNames.add(rs.getString(1));
        }

        return tagNames;
    }

    @Override
    public List<String> getValuesForTagName(String sid, String tagName) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttagTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGVALUE));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGKEY), tagName));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        List<String> tagValues = new ArrayList<String>();
        while (rs.next()) {
            tagValues.add(rs.getString(1));
        }

        return tagValues;

    }

    @Override
    public List<Integer> getUploadIDsMatchingVariantTags(String sid, String[][] variantTags) throws SQLException {
        TableSchema table = MedSavantDatabase.VarianttagTableSchema;

        /*
         * SELECT upload_id FROM (SELECT t17.upload_id, COUNT(*) AS count FROM variant_tag t17 WHERE (((t17.tagkey = 'Sequencer') AND (t17.tagvalue = 'SOLID')) OR ((t17.tagkey = 'Sequencer Version') AND (t17.tagvalue = '5500'))) GROUP BY t17.upload_id) as tbl WHERE count = 2;
         */


        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_UPLOAD_ID));

        Condition[] orConditions = new Condition[variantTags.length];

        Set<String> seenConditions = new HashSet<String>();
        int duplicates = 0;

        for (int i = 0; i < variantTags.length; i++) {

            String strRepresentation = variantTags[i][0] + ":" + variantTags[i][1];

            if (seenConditions.contains(strRepresentation)) {
                duplicates++;
            } else {

                orConditions[i] = ComboCondition.and(new Condition[]{
                            BinaryCondition.equalTo(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGKEY), variantTags[i][0]),
                            BinaryCondition.equalTo(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGVALUE), variantTags[i][1])});
                seenConditions.add(strRepresentation);
            }
        }

        q.addCondition(ComboCondition.or(orConditions));
        q.addGroupings(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_UPLOAD_ID));
        q.addHaving(BinaryCondition.equalTo(FunctionCall.countAll(), variantTags.length-duplicates));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        List<Integer> results = new ArrayList<Integer>();
        while (rs.next()) {
            results.add(rs.getInt(1));
        }

        return results;
    }

    @Override
    public List<SimpleVariantFile> getUploadedFiles(String sid, int projectId, int referenceId) throws SQLException, RemoteException {

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid, ProjectQueryUtil.getInstance().getVariantTablename(sid, projectId, referenceId, true));
        TableSchema tagTable = MedSavantDatabase.VarianttagTableSchema;
        TableSchema pendingTable = MedSavantDatabase.VariantpendingupdateTableSchema;
        TableSchema fileTable = MedSavantDatabase.VariantFileTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.setIsDistinct(true);
        query.addColumns(
                table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID),
                table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID));

        ResultSet idRs = ConnectionController.executeQuery(sid, query.toString());

        List<Integer> uploadIds = new ArrayList<Integer>();
        List<Integer> fileIds = new ArrayList<Integer>();
        while(idRs.next()) {
            uploadIds.add(idRs.getInt(1));
            fileIds.add(idRs.getInt(2));
        }

        if (uploadIds.isEmpty()) {
            return new ArrayList<SimpleVariantFile>();
        }

        Condition[] idConditions = new Condition[uploadIds.size()];
        for (int i = 0; i < uploadIds.size(); i++) {
            idConditions[i] = ComboCondition.and(
                    BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadIds.get(i)),
                    BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileIds.get(i)));
        }

        SelectQuery q = new SelectQuery();
        q.addFromTable(tagTable.getTable());
        q.addFromTable(pendingTable.getTable());
        q.addFromTable(fileTable.getTable());
        q.addColumns(
                tagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_UPLOAD_ID),
                tagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGVALUE),
                fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID),
                fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_NAME),
                pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_USER));
        q.addCondition(BinaryCondition.equalTo(tagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGKEY), "Upload Date"));
        q.addCondition(BinaryCondition.equalTo(tagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_UPLOAD_ID), pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID)));
        q.addCondition(BinaryCondition.equalTo(pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        q.addCondition(BinaryCondition.equalTo(pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        q.addCondition(BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID)));
        q.addCondition(ComboCondition.or(idConditions));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        List<SimpleVariantFile> result = new ArrayList<SimpleVariantFile>();
        while(rs.next()) {
            result.add(new SimpleVariantFile(
                    rs.getInt(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID),
                    rs.getInt(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID),
                    rs.getString(VariantFileTableSchema.COLUMNNAME_OF_FILE_NAME),
                    rs.getString(VarianttagTableSchema.COLUMNNAME_OF_TAGVALUE),
                    rs.getString(VariantPendingUpdateTableSchema.COLUMNNAME_OF_USER)));
        }
        return result;
    }

    @Override
    public List<String[]> getTagsForUpload(String sid, int uploadId) throws SQLException, RemoteException {

        TableSchema tagTable = MedSavantDatabase.VarianttagTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(tagTable.getTable());
        q.addColumns(
                tagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGKEY),
                tagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGVALUE));
        q.addCondition(BinaryCondition.equalTo(tagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadId));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        List<String[]> result = new ArrayList<String[]>();
        while(rs.next()) {
            result.add(new String[]{rs.getString(1), rs.getString(2)});
        }
        return result;
    }

    @Override
    public Set<StarredVariant> getStarredVariants(String sid, int projectId, int referenceId) throws SQLException, RemoteException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        Set<StarredVariant> result = new HashSet<StarredVariant>();
        while(rs.next()) {
            result.add(new StarredVariant(
                    rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID),
                    rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
                    rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID),
                    rs.getString(VariantStarredTableSchema.COLUMNNAME_OF_USER),
                    rs.getString(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION),
                    rs.getTimestamp(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP)));
        }
        return result;
    }

    @Override
    public int addStarredVariants(String sid, int projectId, int referenceId, List<StarredVariant> variants) throws SQLException, RemoteException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        int totalNumStarred = getTotalNumStarred(sid, projectId, referenceId);
        int numStarred = 0;

        Connection c = ConnectionController.connectPooled(sid);
        c.setAutoCommit(false);

        for (StarredVariant variant : variants) {

            if (totalNumStarred == Settings.NUM_STARRED_ALLOWED) {
                return numStarred;
            }

            InsertQuery q = new InsertQuery(table.getTable());
            List<DbColumn> columnsList = table.getColumns();
            Column[] columnsArray = new Column[columnsList.size()];
            columnsArray = columnsList.toArray(columnsArray);
            q.addColumns(columnsArray, variant.toArray(projectId, referenceId));

            try {
                c.createStatement().executeUpdate(q.toString());
                totalNumStarred++;
            } catch (MySQLIntegrityConstraintViolationException e) {
                //duplicate entry, update
                updateStarredVariant(c, projectId, referenceId, variant);
            }
            numStarred++;
        }

        c.commit();
        c.setAutoCommit(true);
        c.close();

        return numStarred;
    }

    private void updateStarredVariant(Connection c, int projectId, int referenceId, StarredVariant variant) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        UpdateQuery q = new UpdateQuery(table.getTable());
        //q.addSetClause(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER), variant.getUser());
        q.addSetClause(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION), variant.getDescription());
        q.addSetClause(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP), variant.getTimestamp());
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID), variant.getUploadId()));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID), variant.getFileId()));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID), variant.getVariantId()));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER), variant.getUser()));

        c.createStatement().executeUpdate(q.toString());
    }

    @Override
    public void unstarVariant(String sid, int projectId, int referenceId, int uploadId, int fileId, int variantId, String user) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        DeleteQuery q = new DeleteQuery(table.getTable());
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID), fileId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID), variantId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER), user));

        ConnectionController.execute(sid, q.toString());
    }

    private int getTotalNumStarred(String sid, int projectId, int referenceId) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        rs.next();
        return rs.getInt(1);
    }

    public void addEntryToFileTable(String sid, int uploadId, int fileId, String fileName) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantFileTableSchema;

        InsertQuery q = new InsertQuery(table.getTable());
        q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadId);
        q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileId);
        q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_NAME), fileName);

        ConnectionController.executeUpdate(sid,  q.toString());
    }

    public void removeEntryFromFileTable(String sid, int uploadId, int fileId) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantFileTableSchema;

        DeleteQuery q = new DeleteQuery(table.getTable());
        q.addCondition(ComboCondition.and(new Condition[]{
            BinaryCondition.equalTo(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadId),
            BinaryCondition.equalTo(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileId)
        }));

        ConnectionController.execute(sid, q.toString());
    }

    @Override
    public Map<SimplePatient, Integer> getPatientHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, List<SimplePatient> patients) throws SQLException, RemoteException{

        //get dna ids
        List<String> dnaIds = new ArrayList<String>();
        for (SimplePatient sp : patients) {
            for (String id : sp.getDnaIds()) {
                if (!dnaIds.contains(id)) {
                    dnaIds.add(id);
                }
            }
        }

        Map<String, Integer> dnaIdMap = getDnaIdHeatMap(sid, projectId, referenceId, conditions, dnaIds);

        //map back to simple patients;
        Map<SimplePatient, Integer> result = new HashMap<SimplePatient, Integer>();
        for (SimplePatient p : patients) {
            Integer count = 0;
            for (String dnaId : p.getDnaIds()) {
                Integer i = dnaIdMap.get(dnaId);
                if (i!=null) {
                    count += i;
                }
            }
            result.put(p, count);
        }

        return result;
    }

    @Override
    public Map<String, Integer> getDnaIdHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds) throws SQLException, RemoteException{

        Object[] variantTableInfo = ProjectQueryUtil.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String)variantTableInfo[0];
        String tablenameSub = (String)variantTableInfo[1];
        float multiplier = (Float)variantTableInfo[2];

        TableSchema subTable = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);

        Map<String, Integer> dnaIdMap = new HashMap<String, Integer>();

        //combine conditions
        Condition[] c1 = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c1[i] = ComboCondition.and(conditions[i]);
        }
        Condition c2 = ComboCondition.or(c1);

        // Try sub table first.
        getDNAIDHeatMapHelper(sid, subTable, multiplier, dnaIds, c2, true, dnaIdMap);

        // Determine dnaIDs with no value yet.
        List<String> dnaIds2 = new ArrayList<String>();
        for (String dnaId : dnaIds) {
            if (!dnaIdMap.containsKey(dnaId)) {
                dnaIds2.add(dnaId);
            }
        }

        //get remaining dna ids from actual table
        if (!dnaIds2.isEmpty()) {
            getDNAIDHeatMapHelper(sid, table, 1, dnaIds2, c2, false, dnaIdMap);
        }

        return dnaIdMap;
    }

    private void getDNAIDHeatMapHelper(String sid, TableSchema table, float multiplier, List<String> dnaIds, Condition c, boolean useThreshold, Map<String, Integer> map) throws SQLException {

        //generate conditions from dna ids
        Condition[] dnaIdConditions = new Condition[dnaIds.size()];
        for (int i = 0; i < dnaIds.size(); i++) {
            dnaIdConditions[i] = BinaryCondition.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaIds.get(i));
        }
        Condition dnaCondition = ComboCondition.or(dnaIdConditions);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addColumns(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID));
        q.addCondition(ComboCondition.and(new Condition[]{dnaCondition, c}));
        q.addGroupings(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        while(rs.next()) {
            int value = (int)(rs.getInt(1) * multiplier);
            if (!useThreshold || value >= PATIENT_HEATMAP_THRESHOLD) {
                map.put(rs.getString(2), value);
            }
        }
    }

    private Condition createNucleotideCondition(DbColumn column) {
        return ComboCondition.or(
                BinaryCondition.equalTo(column, "A"),
                BinaryCondition.equalTo(column, "C"),
                BinaryCondition.equalTo(column, "G"),
                BinaryCondition.equalTo(column, "T"));
    }
}
