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
package org.ut.biolab.medsavant.server.db.variants;

import org.ut.biolab.medsavant.shared.model.VariantTag;
import org.ut.biolab.medsavant.shared.model.ScatterChartEntry;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.model.SimplePatient;
import org.ut.biolab.medsavant.shared.model.VariantComment;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.ScatterChartMap;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.shared.model.Annotation;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import net.sf.samtools.util.BlockCompressedInputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantFileTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantPendingUpdateTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantStarredTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantTagColumns;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.AnnotationLog.Status;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.server.serverapi.AnnotationLogManager;
import org.ut.biolab.medsavant.server.serverapi.AnnotationManager;
import org.ut.biolab.medsavant.server.serverapi.NetworkManager;
import org.ut.biolab.medsavant.server.serverapi.PatientManager;
import org.ut.biolab.medsavant.server.serverapi.ProjectManager;
import org.ut.biolab.medsavant.server.serverapi.SettingsManager;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.IOUtils;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.server.vcf.VCFIterator;
import org.ut.biolab.medsavant.server.vcf.VCFParser;

/**
 *
 * @author Andrew
 */
public class VariantManager extends MedSavantServerUnicastRemoteObject implements VariantManagerAdapter, BasicVariantColumns {

    private static final Log LOG = LogFactory.getLog(VariantManager.class);
    // thresholds for querying and drawing
    private static final int COUNT_ESTIMATE_THRESHOLD = 1000;
    private static final int BIN_TOTAL_THRESHOLD = 1000000;
    private static final int PATIENT_HEATMAP_THRESHOLD = 1000;
    // Stages within the upload process.
    private static final double LOG_FRACTION = 0.05;
    private static final double DUMP_FRACTION = 0.1;
    private static final double SORTING_FRACTION = 0.1;
    private static final double CUSTOM_FIELD_FRACTION = 0.05;
    private static final double ANNOTATING_FRACTION = 0.15;
    private static final double SPLITTING_FRACTION = 0.05;
    private static final double MERGING_FRACTION = 0.05;
    private static final double CREATING_TABLES_FRACTION = 0.05;
    private static final double SUBSET_FRACTION = 0.05;
    private static final double LOAD_TABLE_FRACTION = 0.15;             // Happens twice
    private static VariantManager instance;
    private static final String varchar = DbColumn.getTypeName(java.sql.Types.VARCHAR);

    private static boolean isVarchar(String typeNameSQL) {
        return typeNameSQL.equals(varchar);
    }

    private VariantManager() throws RemoteException {
    }

    public static synchronized VariantManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new VariantManager();
        }
        return instance;
    }

    /**
     * Make all variant tables live for a project. All users accessing this
     * database will be logged out.
     */
    @Override
    public void publishVariants(String sessID, int projectID) throws Exception {

        LOG.info("Beginning publish of all tables for project " + projectID);

        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            //get update ids and references
            LOG.info("Getting map of update ids");
            int[] refIDs = ProjectManager.getInstance().getReferenceIDsForProject(sessID, projectID);
            Map<Integer, Integer> ref2Update = new HashMap<Integer, Integer>();
            for (int refID : refIDs) {
                ref2Update.put(refID, ProjectManager.getInstance().getNewestUpdateID(sessID, projectID, refID, false));
            }

            //update annotation log table
            LOG.info("Setting log status to published");
            for (Integer refId : ref2Update.keySet()) {
                AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, ref2Update.get(refId), Status.PUBLISHED);
            }

            //publish
            LOG.info("Releasing database lock.");
            SettingsManager.getInstance().releaseDBLock(conn);
            LOG.info("Terminating active sessions");
            SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sessID), "Administrator (" + SessionController.getInstance().getUserForSession(sessID) + ") published new variants");
            LOG.info("Publishing tables");
            for (Integer refId : ref2Update.keySet()) {
                ProjectManager.getInstance().publishVariantTable(conn, projectID, refId, ref2Update.get(refId));
            }

            LOG.info("Publish complete");
        } finally {
            conn.close();
        }
    }

    /**
     * Make a variant table live. All users accessing this database will be
     * logged out.
     */
    @Override
    public void publishVariants(String sessID, int projID, int refID, int updID) throws Exception {
        LOG.info("Publishing table. pid:" + projID + " refid:" + refID + " upid:" + updID);
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            LOG.info("Setting log status to published");
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updID, Status.PUBLISHED);
            LOG.info("Releasing database lock.");
            SettingsManager.getInstance().releaseDBLock(conn);
            LOG.info("Terminating active sessions");
            SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sessID), "Administrator (" + SessionController.getInstance().getUserForSession(sessID) + ") published new variants");
            LOG.info("Publishing table");
            ProjectManager.getInstance().publishVariantTable(conn, projID, refID, updID);
            LOG.info("Publish complete");
        } finally {
            conn.close();
        }
    }


    /*
     * Remove an unpublished variant table.
     */
    @Override
    public void cancelPublish(String sid, int projectID, int referenceID, int updateID) throws Exception {
        LOG.info("Cancelling publish. pid:" + projectID + " refid:" + referenceID + " upid:" + updateID);
        ProjectManager.getInstance().removeTables(sid, projectID, referenceID, updateID, updateID);
        LOG.info("Cancel complete");
    }

    /**
     * Perform updates to custom vcf fields and other annotations. Will result
     * in the creation of a new, unpublished, up-to-date variant table. This
     * method is used only by ProjectWizard.modifyProject().
     */
    @Override
    public int updateTable(String sessID, int projID, int refID, int[] annotIDs, CustomField[] variantFields, String email) throws Exception {

        String projectName = ProjectManager.getInstance().getProjectName(sessID, projID);
        EmailLogger.logByEmail("Updating " + projectName + " MedSavant Project - STARTED", "Updating of " + projectName + " project started at " + (new Date()).toString() + ". You will be notified again upon completion.", email);

        LOG.info("Beginning new variant update");
        double fract = 0.0;
        makeProgress(sessID, "Preparing...", fract);

        String user = SessionController.getInstance().getUserForSession(sessID);

        //generate directory
        LOG.info("Generating base directory");
        File basedir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
        LOG.info("Base directory: " + basedir.getCanonicalPath());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + basedir);
        p.waitFor();

        //add log
        LOG.info("Adding log and generating update id");
        int updateId = AnnotationLogManager.getInstance().addAnnotationLogEntry(sessID, projID, refID, org.ut.biolab.medsavant.shared.model.AnnotationLog.Action.ADD_VARIANTS, user);
        fract += LOG_FRACTION;

        try {
            ProjectManager projMgr = ProjectManager.getInstance();

            //dump existing table
            LOG.info("Dumping existing variant table");
            makeProgress(sessID, "Dumping existing variants...", fract);

            String existingTableName = projMgr.getVariantTableName(sessID, projID, refID, false);
            File variantDumpFile = new File(basedir, existingTableName);
            String variantDump = variantDumpFile.getAbsolutePath();
            LOG.info("Dumping variants to file");
            VariantManagerUtils.variantsToFile(sessID, existingTableName, variantDumpFile, null, false, 0);
            fract += DUMP_FRACTION;

            //sort variants
            LOG.info("Sorting variants");
            makeProgress(sessID, "Sorting variants...", fract);
            String sortedVariants = variantDump + "_sorted";
            VariantManagerUtils.sortFileByPosition(variantDump, sortedVariants);
            fract += SORTING_FRACTION;

            //add custom vcf fields
            LOG.info("Adding custom vcf fields");
            makeProgress(sessID, "Adding custom VCF fields...", fract);
            String vcfAnnotatedVariants = sortedVariants + "_vcf";
            VariantManagerUtils.addCustomVCFFields(sortedVariants, vcfAnnotatedVariants, variantFields, INDEX_OF_CUSTOM_INFO); //last of the default fields
            fract += CUSTOM_FIELD_FRACTION;

            //annotate
            makeProgress(sessID, "Annotating...", fract);
            String outputFilename = vcfAnnotatedVariants + "_annotated";
            LOG.info("File containing annotated variants, sorted by position: " + outputFilename);

            LOG.info("Annotating variants in " + vcfAnnotatedVariants + ", destination " + outputFilename);

            long startTime = System.currentTimeMillis();

            Annotation[] annotations = getAnnotationsFromIDs(annotIDs, sessID);
            BatchVariantAnnotator bva = new BatchVariantAnnotator(new File(vcfAnnotatedVariants), new File(outputFilename), annotations, sessID);
            bva.performBatchAnnotationInParallel();

            long endTime = System.currentTimeMillis();

            long duration = (endTime - startTime) / 1000 / 60;

            LOG.info("Completed annotation, taking " + duration + " minutes");

            fract += ANNOTATING_FRACTION;

            //split
            File splitDir = new File(basedir, "split-by-id");
            splitDir.mkdir();
            LOG.info("Splitting annotation file into multiple files by file ID");
            makeProgress(sessID, "Splitting files...", fract);
            for (File f : splitDir.listFiles()) {
                VariantManagerUtils.removeTemp(f);
            }
            VariantManagerUtils.splitFileOnColumn(splitDir, outputFilename, 1);
            fract += SPLITTING_FRACTION;

            //merge
            String outputFilenameMerged = outputFilename + "_merged";
            LOG.info("File containing annotated variants, sorted by file: " + outputFilenameMerged);
            LOG.info("Merging files");
            makeProgress(sessID, "Merging files...", fract);
            VariantManagerUtils.concatenateFilesInDir(splitDir, outputFilenameMerged);
            fract += MERGING_FRACTION;

            //create the staging table
            LOG.info("Creating new variant table for resulting variants");
            makeProgress(sessID, "Creating table...", fract);
            projMgr.setCustomVariantFields(sessID, projID, refID, updateId, variantFields);
            String tableName = projMgr.createVariantTable(sessID, projID, refID, updateId, annotIDs, true);
            String tableNameSub = projMgr.createVariantTable(sessID, projID, refID, updateId, annotIDs, false, true);
            fract += CREATING_TABLES_FRACTION;

            //upload to staging table
            LOG.info("Uploading variants to table: " + tableName);
            makeProgress(sessID, "Loading data into table...", fract);
            uploadFileToVariantTable(sessID, new File(outputFilenameMerged), tableName);
            fract += LOAD_TABLE_FRACTION;

            //upload to sub table
            File subFile = new File(outputFilenameMerged + "_sub");
            LOG.info("Generating sub file: " + subFile.getAbsolutePath());
            makeProgress(sessID, "Generating subset...", fract);
            VariantManagerUtils.generateSubset(new File(outputFilenameMerged), subFile);
            fract += SUBSET_FRACTION;

            LOG.info("Importing to: " + tableNameSub);
            makeProgress(sessID, "Loading data into subset table...", fract);
            uploadFileToVariantTable(sessID, subFile, tableNameSub);
            fract += LOAD_TABLE_FRACTION;

            if (cleanUp) {
                boolean deleted = new File(outputFilenameMerged).delete();
                LOG.info("Deleting " + outputFilenameMerged + " - " + (deleted ? "successful" : "failed"));
                deleted = subFile.delete();
                LOG.info("Deleting " + outputFilenameMerged + " - " + (deleted ? "successful" : "failed"));
            }

            //add entries to tablemap
            projMgr.addTableToMap(sessID, projID, refID, updateId, false, tableName, annotIDs, tableNameSub);

            //cleanup
            LOG.info("Dropping old table(s)");
            makeProgress(sessID, "Cleaning up...", fract);
            int newestId = projMgr.getNewestUpdateID(sessID, projID, refID, true);
            int minId = -1;
            int maxId = newestId - 1;
            projMgr.removeTables(sessID, projID, refID, minId, maxId);

            //TODO: remove files

            //TODO: server logs

            //set as pending
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updateId, Status.PENDING);
            makeProgress(sessID, "Variants updated.", 1.0);

            LOG.info("Done");

            EmailLogger.logByEmail("Updating " + projectName + " MedSavant Project - COMPLETED", "Updating of " + projectName + " project finished at " + (new Date()).toString() + ".", email);


            return updateId;

        } catch (Exception e) {
            LOG.error(e);
            e.printStackTrace();
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updateId, Status.ERROR);
            EmailLogger.logByEmail("Updating " + projectName + " MedSavant Project - FAILED", "There was a problem updating project " + projectName + ". Here is the message: " + MiscUtils.getMessage(e), email);
            throw e;
        }
    }

    /**
     * Import variant files which have been transferred from a client.
     */
    @Override
    public int uploadVariants(String sessID, int[] transferIDs, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish) throws InterruptedException, IOException, SQLException {

        LOG.info("Importing variants by transferring from client");

        NetworkManager netMgr = NetworkManager.getInstance();
        File[] vcfFiles = new File[transferIDs.length];
        String[] sourceNames = new String[transferIDs.length];


        int i = 0;
        for (int id : transferIDs) {
            vcfFiles[i] = netMgr.getFileByTransferID(sessID, id);
            sourceNames[i] = netMgr.getSourceNameByTransferID(sessID, id);
            i++;
        }

        return uploadVariants(sessID, vcfFiles, sourceNames, projID, refID, tags, includeHomoRef, email, autoPublish);
    }

    /**
     * Use when variant files are already on the server. Performs variant import
     * of an entire directory.
     */
    @Override
    public int uploadVariants(String sessID, File dirContainingVCFs, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish) throws RemoteException, IOException, Exception {

        LOG.info("Importing variants already stored on server in dir " + dirContainingVCFs.getAbsolutePath());

        if (!dirContainingVCFs.exists()) {
            LOG.info("Directory from which to load variants does not exist, bailing out.");
            return -1;
        }

        File[] vcfFiles = dirContainingVCFs.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName();
                return name.endsWith(".vcf") || name.endsWith(".vcf.gz");
            }
        });

        if (vcfFiles.length == 0) {
            LOG.info("Directory exists but contains no .vcf or .vcf.gz files.");
            return -1;
        }

        return uploadVariants(sessID, vcfFiles, null, projID, refID, tags, includeHomoRef, email, autoPublish);
    }

    /**
     * Start the upload process for new vcf files. Will result in the creation
     * of a new, unpublished, up-to-date variant table.
     *
     * @param sessID uniquely identifies the client
     * @param vcfFiles local VCF files on the server's file-system
     * @param sourceNames if non-null, client-side names of uploaded files
     */
    public int uploadVariants(String sessID, File[] vcfFiles, String[] sourceNames, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish) throws IOException, InterruptedException, SQLException {

        LOG.info("Acquiring session lock");
        boolean needsToUnlock = ConnectionController.registerBackgroundUsageOfSession(sessID);

        String projectName = ProjectManager.getInstance().getProjectName(sessID, projID);
        EmailLogger.logByEmail("Importing variants to " + projectName + " MedSavant Project - STARTED", "Importing " + vcfFiles.length + " file(s) started at " + (new Date()).toString() + ". You will be notified again upon completion.", email);

        LOG.info("Beginning variant import");

        double fract = 0.0;
        makeProgress(sessID, "Preparing...", fract);

        String user = SessionController.getInstance().getUserForSession(sessID);

        //generate directory
        LOG.info("Generating base directory");
        File baseDir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());

        LOG.info("Base directory: " + baseDir.getAbsolutePath());
        Process p = Runtime.getRuntime().exec("chmod -R a+wx " + baseDir);
        p.waitFor();

        //add log
        LOG.info("Adding log and generating update id");
        makeProgress(sessID, "Generating update ID...", fract);
        int updateID = AnnotationLogManager.getInstance().addAnnotationLogEntry(sessID, projID, refID, AnnotationLog.Action.ADD_VARIANTS, user);
        fract += LOG_FRACTION;

        try {
            ProjectManager projMgr = ProjectManager.getInstance();
            AnnotationManager annotMgr = AnnotationManager.getInstance();

            //dump existing table
            String existingTableName = projMgr.getVariantTableName(sessID, projID, refID, false);
            LOG.info("Checking that " + baseDir + " is executable");
            IOUtils.checkForWorldExecute(baseDir);
            File existingVariantsFile = new File(baseDir, "proj" + projID + "_ref" + refID + "_update" + updateID);

            LOG.info("Dumping variants to file");
            makeProgress(sessID, "Dumping variants to file...", fract);

            VariantManagerUtils.variantsToFile(sessID, existingTableName, existingVariantsFile, null, true, 0);
            fract += DUMP_FRACTION;

            //create the staging table
            LOG.info("Creating new variant table for resulting variants");

            projMgr.setCustomVariantFields(
                    sessID, projID, refID, updateID,
                    projMgr.getCustomVariantFields(sessID, projID, refID, projMgr.getNewestUpdateID(sessID, projID, refID, false)));
            String tableName = projMgr.createVariantTable(sessID, projID, refID, updateID, annotMgr.getAnnotationIDs(sessID, projID, refID), true);
            String tableNameSub = projMgr.createVariantTable(sessID, projID, refID, updateID, annotMgr.getAnnotationIDs(sessID, projID, refID), false, true);
            fract += CUSTOM_FIELD_FRACTION;

            //upload dump to staging table
            LOG.info("Loading data into table: " + tableName);
            makeProgress(sessID, "Loading data into table...", fract);
            uploadFileToVariantTable(sessID, existingVariantsFile, tableName);
            fract += LOAD_TABLE_FRACTION;

            //get annotation ids
            int[] annotIDs = annotMgr.getAnnotationIDs(sessID, projID, refID);
            CustomField[] customFields = projMgr.getCustomVariantFields(sessID, projID, refID, projMgr.getNewestUpdateID(sessID, projID, refID, false));
            Annotation[] annotations = getAnnotationsFromIDs(annotIDs, sessID);

            // parse each vcf file in a separate thread with a separate file ID
            ParserThread[] threads = new ParserThread[vcfFiles.length];
            String stamp = System.nanoTime() + "";
            int fileID = 0;
            for (File vcfFile : vcfFiles) {
                File outFile = new File(baseDir, "tmp_" + stamp + "_" + fileID + ".tdf");
                ParserThread t = new ParserThread(sessID, vcfFile, outFile, updateID, fileID, includeHomoRef, annotations, customFields, tableName);
                threads[fileID] = t;
                fileID++;
                LOG.info("Queueing thread to parse " + vcfFile.getAbsolutePath());
                t.start();
            }

            // wait for all the threads to finish
            LOG.info("Waiting for all threads to finish...");
            for (Thread t : threads) {
                t.join();
            }
            LOG.info("All threads done");

            //create sub table dump
            LOG.info("Dumping variants to file for sub table");
            makeProgress(sessID, "Generating subset...", fract);
            File subDump = new File(baseDir, "proj" + projID + "_ref" + refID + "_update" + updateID + "_sub");
            VariantManagerUtils.variantsToFile(sessID, tableName, subDump, null, true, VariantManagerUtils.determineStepForSubset(getNumFilteredVariantsHelper(sessID, tableName, new Condition[][]{})));
            fract += SUBSET_FRACTION;

            //upload to sub table
            LOG.info("Loading into subset table: " + tableNameSub);
            makeProgress(sessID, "Loading data into subset table...", fract);
            uploadFileToVariantTable(sessID, subDump, tableNameSub);
            fract += LOAD_TABLE_FRACTION;

            //add entries to tablemap
            makeProgress(sessID, "Cleaning up...", fract);
            projMgr.addTableToMap(sessID, projID, refID, updateID, false, tableName, annotIDs, tableNameSub);

            //add tags to upload
            LOG.info("Adding upload tags");
            VariantManagerUtils.addTagsToUpload(sessID, updateID, tags);

            //add entries to file table
            LOG.info("Adding entries to file table");
            for (int i = 0; i < vcfFiles.length; i++) {
                // Name added to table will be name of client-side file, if supplied.
                String name = sourceNames != null ? sourceNames[i] : vcfFiles[i].getPath();
                addEntryToFileTable(sessID, updateID, i, name);
            }

            //cleanup
            LOG.info("Dropping old table(s)");
            int newestId = projMgr.getNewestUpdateID(sessID, projID, refID, true);
            int minId = -1;
            int maxId = newestId - 1;
            projMgr.removeTables(sessID, projID, refID, minId, maxId);

            //TODO: remove files
            //TODO: server logs

            //set as pending
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updateID, Status.PENDING);

            LOG.info("Assigning update ID of " + updateID);
            makeProgress(sessID, "Variants uploaded.", 1.0);

            if (autoPublish) {
                publishVariants(sessID, projID);
            }

            EmailLogger.logByEmail("Importing variants to " + projectName + " MedSavant Project - COMPLETED", "Importing " + vcfFiles.length + " file(s) finished at " + (new Date()).toString() + ".");

            return updateID;

        } catch (IOException ex) {
            ex.printStackTrace();
            EmailLogger.logByEmail("Importing variants to " + projectName + " MedSavant Project - FAILED", "There was a problem importing variants into " + projectName + ". Here is the message: " + ExceptionUtils.getStackTrace(ex));
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updateID, Status.ERROR);
            throw ex;
        } catch (SQLException ex) {
            ex.printStackTrace();
            EmailLogger.logByEmail("Importing variants to " + projectName + " MedSavant Project - FAILED", "There was a problem importing variants into " + projectName + ". Here is the message: " + ExceptionUtils.getStackTrace(ex));
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updateID, Status.ERROR);
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            EmailLogger.logByEmail("Importing variants to " + projectName + " MedSavant Project - FAILED", "There was a problem importing variants into " + projectName + ". Here is the message: " + ExceptionUtils.getStackTrace(ex));
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updateID, Status.ERROR);
            throw new IOException(ex);
        } finally {
            LOG.info("Releasing session lock");
            if (needsToUnlock) {
                ConnectionController.unregisterBackgroundUsageOfSession(sessID);
            }
        }
    }

    @Override
    public int removeVariants(String sessID, int projID, int refID, List<SimpleVariantFile> files) throws Exception {
        LOG.info("Beginning removal of variants");

        String user = SessionController.getInstance().getUserForSession(sessID);

        //generate directory
        LOG.info("Generating base directory");
        File baseDir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
        LOG.info("Base directory: " + baseDir.getCanonicalPath());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir);
        p.waitFor();

        //add log
        LOG.info("Adding log and generating update id");
        int updateId = AnnotationLogManager.getInstance().addAnnotationLogEntry(sessID, projID, refID, org.ut.biolab.medsavant.shared.model.AnnotationLog.Action.REMOVE_VARIANTS, user);

        try {

            //dump existing except for files
            ProjectManager projMgr = ProjectManager.getInstance();
            String existingTableName = projMgr.getVariantTableName(sessID, projID, refID, false);
            File existingVariantsFile = new File(baseDir, "proj" + projID + "_ref" + refID + "_update" + updateId);
            LOG.info("Dumping variants to file");
            String conditions = "";
            for (int i = 0; i < files.size(); i++) {
                conditions +=
                        "!(" + UPLOAD_ID.getColumnName() + "=" + files.get(i).getUploadId()
                        + " AND " + FILE_ID.getColumnName() + "=" + files.get(i).getFileId() + ")";
                if (i != files.size() - 1) {
                    conditions += " AND ";
                }
            }
            VariantManagerUtils.variantsToFile(sessID, existingTableName, existingVariantsFile, conditions, true, 0);

            //create the staging table
            LOG.info("Creating new variant table for resulting variants");
            projMgr.setCustomVariantFields(
                    sessID, projID, refID, updateId,
                    projMgr.getCustomVariantFields(sessID, projID, refID, projMgr.getNewestUpdateID(sessID, projID, refID, false)));
            String tableName = projMgr.createVariantTable(sessID, projID, refID, updateId, AnnotationManager.getInstance().getAnnotationIDs(sessID, projID, refID), true);
            String tableNameSub = projMgr.createVariantTable(sessID, projID, refID, updateId, AnnotationManager.getInstance().getAnnotationIDs(sessID, projID, refID), false, true);

            //upload to staging table
            LOG.info("Uploading variants to table: " + tableName);
            uploadFileToVariantTable(sessID, existingVariantsFile, tableName);

            //upload to sub table
            File subFile = new File(existingVariantsFile.getAbsolutePath() + "_sub");
            LOG.info("Generating sub file: " + subFile.getAbsolutePath());
            VariantManagerUtils.generateSubset(existingVariantsFile, subFile);
            LOG.info("Importing to: " + tableNameSub);
            uploadFileToVariantTable(sessID, subFile, tableNameSub);


            if (cleanUp) {
                boolean deleted = existingVariantsFile.delete();
                LOG.info("Deleting " + existingVariantsFile.getAbsolutePath() + " - " + (deleted ? "successful" : "failed"));
                deleted = subFile.delete();
                LOG.info("Deleting " + subFile.getAbsolutePath() + " - " + (deleted ? "successful" : "failed"));
            }

            //get annotation ids
            AnnotationManager annotMgr = AnnotationManager.getInstance();
            int[] annotIDs = annotMgr.getAnnotationIDs(sessID, projID, refID);

            //add entries to tablemap
            projMgr.addTableToMap(sessID, projID, refID, updateId, false, tableName, annotIDs, tableNameSub);

            //cleanup
            LOG.info("Dropping old table(s)");
            int newestId = projMgr.getNewestUpdateID(sessID, projID, refID, true);
            int minId = -1;
            int maxId = newestId - 1;
            projMgr.removeTables(sessID, projID, refID, minId, maxId);

            //TODO: remove files

            //TODO: server logs

            //set as pending
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updateId, Status.PENDING);

            return updateId;

        } catch (Exception e) {
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updateId, Status.ERROR);
            throw e;
        }
    }

    @Override
    public int exportVariants(String sessID, int projID, int refID, Condition[][] conditions, boolean orderedByPosition, boolean zipOutputFile) throws SQLException, RemoteException, IOException, InterruptedException {

        //generate directory
        File baseDir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir.getCanonicalPath());
        p.waitFor();

        String filename = ProjectManager.getInstance().getProjectName(sessID, projID).replace(" ", "") + "-varexport-" + System.currentTimeMillis() + ".tdf";
        File file = new File(baseDir, filename);

        LOG.info("Exporting variants to " + file.getAbsolutePath());

        long start = System.currentTimeMillis();
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, ProjectManager.getInstance().getVariantTableName(sessID, projID, refID, true));
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        addConditionsToQuery(query, conditions);
        if (orderedByPosition) {
            query.addOrderings(table.getDBColumn(POSITION));
        }
        String intoString =
                "INTO OUTFILE \"" + file.getAbsolutePath().replaceAll("\\\\", "/") + "\" "
                + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.FIELD_DELIMITER) + "' "
                + "ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' "
                + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "'" //+ " LINES TERMINATED BY '\\r\\n' ";
                ;
        String queryString = query.toString().replace("FROM", intoString + "FROM");

        LOG.info(queryString);
        ConnectionController.executeQuery(sessID, queryString);

        LOG.info("Done exporting variants to " + file.getAbsolutePath());
        LOG.info("Export took " + ((System.currentTimeMillis() - start) / 1000) + " seconds");

        if (zipOutputFile) {
            LOG.info("Zipping export...");
            File zipFile = new File(file.getAbsoluteFile() + ".zip");
            IOUtils.zipFile(file, zipFile);
            boolean deleted = file.delete();
            LOG.info("Deleting " + file.getAbsolutePath() + " - " + (deleted ? "successful" : "failed"));
            file = zipFile;
            LOG.info("Done zipping");
        }

        // add file to map and send the id back
        int fileID = NetworkManager.getInstance().openReaderOnServer(sessID, file);
        return fileID;
    }

    @Override
    public TableSchema getCustomTableSchema(String sessionId, int projectId, int referenceId) throws SQLException, RemoteException {
        return CustomTables.getInstance().getCustomTableSchema(sessionId, ProjectManager.getInstance().getVariantTableName(sessionId, projectId, referenceId, true));
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, int start, int limit) throws SQLException, RemoteException {
        return getVariants(sessionId, projectId, referenceId, new Condition[1][], start, limit);
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException {
        return getVariants(sessionId, projectId, referenceId, conditions, start, limit, null);
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions, int start, int limit, String[] orderByCols) throws SQLException, RemoteException {
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessionId, ProjectManager.getInstance().getVariantTableName(sessionId, projectId, referenceId, true));
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        addConditionsToQuery(query, conditions);
        if (orderByCols != null) {
            query.addCustomOrderings((Object[]) orderByCols);
        }

        String queryString = query.toString();
        if (limit != -1) {
            if (start != -1) {
                queryString += " LIMIT " + start + ", " + limit;
            } else {
                queryString += " LIMIT " + limit;
            }
        }

        LOG.debug(queryString);

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
    public int getVariantCount(String sid, int projectId, int referenceId) throws SQLException, RemoteException {
        return getFilteredVariantCount(sid, projectId, referenceId, new Condition[0][], true);
    }

    @Override
    public int getFilteredVariantCount(String sid, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException {
        return getFilteredVariantCount(sid, projectId, referenceId, conditions, false);
    }

    private int getFilteredVariantCount(String sid, int projectId, int referenceId, Condition[][] conditions, boolean forceExact) throws SQLException, RemoteException {

        //String name = ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId, true);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float subMultiplier = (Float) variantTableInfo[2];

        if (tablename == null) {
            return -1;
        }

        //try to get a reasonable approximation
        if (tablenameSub != null && !forceExact && conditions.length > 0) {
            int estimate = (int) (getNumFilteredVariantsHelper(sid, tablenameSub, conditions) * subMultiplier);
            if (estimate >= COUNT_ESTIMATE_THRESHOLD) {
                return estimate; // TODO: this should be rounded instead of a precise one
            }
        }

        // Approximation not good enough; use actual data.
        return getNumFilteredVariantsHelper(sid, tablename, conditions);
    }

    public int getNumFilteredVariantsHelper(String sessID, String tablename, Condition[][] conditions) throws SQLException, RemoteException {
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);

        LOG.info(q);

        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        rs.next();

        LOG.info("Number of variants remaining: " + rs.getInt(1));

        return rs.getInt(1);
    }

    /*
     * Convenience method
     */
    @Override
    public int getVariantCountForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException {

        if (dnaIDs.isEmpty()) {
            return 0;
        }

        String name = ProjectManager.getInstance().getVariantTableName(sessID, projID, refID, true);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, name);
        Condition dnaCondition = new InCondition(table.getDBColumn(DNA_ID.getColumnName()), dnaIDs);

        Condition[] c1 = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c1[i] = ComboCondition.and(conditions[i]);
        }

        Condition[] finalCondition = new Condition[]{ComboCondition.and(dnaCondition, ComboCondition.or(c1))};

        return getFilteredVariantCount(sessID, projID, refID, new Condition[][]{finalCondition});
    }

    @Override
    public boolean willApproximateCountsForConditions(String sid, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException {
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        return total >= BIN_TOTAL_THRESHOLD;
    }

    @Override
    public Map<Range, Long> getFilteredFrequencyValuesForNumericColumn(String sid, int projectId, int referenceId, Condition[][] conditions, CustomField column, boolean logBins) throws InterruptedException, SQLException, RemoteException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }

        Range range = DBUtils.getInstance().getExtremeValuesForColumn(sid, table.getTableName(), column.getColumnName());
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
                r = new Range(Math.pow(10, binNo), Math.pow(10, binNo + 1));
            } else {
                r = new Range(binNo * binSize, (binNo + 1) * binSize);
            }
            long count = (long) (rs.getLong(1) * multiplier);
            results.put(r, count);
        }

        return results;
    }

    @Override
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sessID, int projID, int refID, Condition[][] conditions, String colName) throws SQLException, RemoteException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sessID, projID, refID, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sessID, projID, refID, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sessID, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);
            multiplier = 1;
        }

        DbColumn column = table.getDBColumn(colName);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(column);
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);
        q.addGroupings(column);

        if (column.getColumnNameSQL().equals(ALT.getColumnName()) || column.getColumnNameSQL().equals(REF.getColumnName())) {
            q.addCondition(createNucleotideCondition(column));
        }

        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        Map<String, Integer> map = new HashMap<String, Integer>();

        while (rs.next()) {
            String key = rs.getString(1);
            if (key == null) {
                key = "";
            }
            map.put(key, (int) (rs.getInt(2) * multiplier));
        }

        return map;
    }

    @Override
    public ScatterChartMap getFilteredFrequencyValuesForScatter(String sid, int projectId, int referenceId, Condition[][] conditions, String columnnameX, String columnnameY, boolean columnXCategorical, boolean columnYCategorical, boolean sortKaryotypically) throws InterruptedException, SQLException, RemoteException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
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
            Range rangeX = DBUtils.getInstance().getExtremeValuesForColumn(sid, table.getTableName(), columnnameX);
            binSizeX = MiscUtils.generateBins(new CustomField(columnnameX, columnX.getTypeNameSQL() + "(" + columnX.getTypeLength() + ")", false, "", ""), rangeX, false);
        }

        double binSizeY = 0;
        if (!columnYCategorical) {
            Range rangeY = DBUtils.getInstance().getExtremeValuesForColumn(sid, table.getTableName(), columnnameY);
            binSizeY = MiscUtils.generateBins(new CustomField(columnnameY, columnY.getTypeNameSQL() + "(" + columnY.getTypeLength() + ")", false, "", ""), rangeY, false);
        }

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        //q.addColumns(columnX, columnY);
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);

        if (columnnameX.equals(ALT.getColumnName()) || columnnameX.equals(REF.getColumnName())) {
            q.addCondition(createNucleotideCondition(columnX));
        }

        if (columnnameY.equals(ALT.getColumnName()) || columnnameY.equals(REF.getColumnName())) {
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

            if (x == null) {
                x = "null"; //prevents NPE when sorting below
            }
            if (y == null) {
                y = "null";
            }

            if (!columnXCategorical) {
                x = MiscUtils.doubleToString(Integer.parseInt(x) * binSizeX, 2) + " - " + MiscUtils.doubleToString(Integer.parseInt(x) * binSizeX + binSizeX, 2);
            }
            if (!columnYCategorical) {
                y = MiscUtils.doubleToString(Integer.parseInt(y) * binSizeY, 2) + " - " + MiscUtils.doubleToString(Integer.parseInt(y) * binSizeY + binSizeY, 2);
            }
            ScatterChartEntry entry = new ScatterChartEntry(x, y, (int) (rs.getInt(1) * multiplier));
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
    public int getVariantCountInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, long start, long end) throws SQLException, RemoteException {

        String name = ProjectManager.getInstance().getVariantTableName(sid, projectId, referenceId, true);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid, name);

        Condition[] rangeConditions = new Condition[]{
            BinaryCondition.equalTo(table.getDBColumn(CHROM), chrom),
            BinaryCondition.greaterThan(table.getDBColumn(POSITION), start, true),
            BinaryCondition.lessThan(table.getDBColumn(POSITION), end, false)
        };

        Condition[] c1 = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c1[i] = ComboCondition.and(conditions[i]);
        }

        Condition[] finalCondition = new Condition[]{ComboCondition.and(ComboCondition.and(rangeConditions), ComboCondition.or(c1))};

        return getFilteredVariantCount(sid, projectId, referenceId, new Condition[][]{finalCondition});
    }

    @Override
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, int binsize) throws SQLException, RemoteException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }

        //TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId, true));

        SelectQuery queryBase = new SelectQuery();
        queryBase.addFromTable(table.getTable());

        queryBase.addColumns(table.getDBColumn(CHROM));

        String roundFunction = "ROUND(" + POSITION.getColumnName() + "/" + binsize + ",0)";

        queryBase.addCustomColumns(FunctionCall.countAll());
        queryBase.addGroupings(table.getDBColumn(CHROM));


        addConditionsToQuery(queryBase, conditions);

        String query = queryBase.toString().replace("COUNT(*)", "COUNT(*)," + roundFunction) + "," + roundFunction;

        //long start = System.nanoTime();
        ResultSet rs = ConnectionController.executeQuery(sid, query);
        //System.out.println(query);
        //System.out.println("  time:" + (System.nanoTime() - start) / 1000000000);

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

            int count = (int) (rs.getInt(2) * multiplier);

            chromMap.put(binRange, count);
            results.put(chrom, chromMap);
        }

        return results;
    }

    private static String extractFileName(String fullName) {
        Pattern p = Pattern.compile(".*?([^\\\\/]+)$");
        Matcher m = p.matcher(fullName);

        return (m.find()) ? m.group(1) : "";
    }

    private static File cleanVariantFile(File inputFile, String tableName, String sessionId) throws SQLException, RemoteException, IOException {
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessionId, tableName);
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        File outputFile = new File(inputFile.getCanonicalPath() + "_clean");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line;
        int expectedColumnCount = table.getColumns().size();
        long row = 0;
        int[] expectedColumnLengths = new int[table.getColumns().size()];
        for (int i = 0; i < expectedColumnCount; ++i) {
            if (isVarchar(table.getColumns().get(i).getTypeNameSQL())) {
                expectedColumnLengths[i] = table.getColumns().get(i).getTypeLength();
            } else {
                expectedColumnLengths[i] = Integer.MAX_VALUE;
            }
        }

        int announceEvery = 10000;

        LOG.info("Cleaning " + inputFile.getAbsolutePath());

        while ((line = br.readLine()) != null) {
            ++row;

            if (row % announceEvery == 0) {
                LOG.info("Cleaned " + row + " lines of " + inputFile.getAbsolutePath());
            }

            //int numFields =  StringUtils.countMatches(line,VariantManagerUtils.FIELD_DELIMITER) + 1;
            String[] values = line.split(VariantManagerUtils.FIELD_DELIMITER, -1);
            if (values.length != expectedColumnCount) {
                LOG.warn("Unexpected number of columns: expected [" + expectedColumnCount + "] found [" + values.length + "] at line [" + values.length + "] in file [" + inputFile.getAbsolutePath() + "]");
                continue;
            }

            for (int i = 0; i < expectedColumnCount; ++i) {

                // truncate fields that are too long
                int lengthOfField = values[i].length() - 2;
                if (lengthOfField >= expectedColumnLengths[i]) {
                    LOG.warn("Value too long: [" + values[i] + "]; trimmed to [" + expectedColumnLengths[i] + "] characters");
                    String unenclosed = values[i].replace(VariantManagerUtils.ENCLOSED_BY, "");
                    values[i] = VariantManagerUtils.ENCLOSED_BY + unenclosed.substring(0, expectedColumnLengths[i]) + VariantManagerUtils.ENCLOSED_BY;
                }
            }

            bw.append(StringUtils.join(values, VariantManagerUtils.FIELD_DELIMITER));
            bw.append("\n");
        }

        LOG.info("Done cleaning " + inputFile.getAbsolutePath() + " output to " + outputFile.getAbsolutePath());

        bw.close();
        br.close();
        return outputFile;
    }

    public static void uploadFileToVariantTable(String sid, File file, String tableName) throws SQLException, IOException {
        file = cleanVariantFile(file, tableName, sid);

        BufferedReader br = new BufferedReader(new FileReader(file));

        // TODO: for some reason the connection is closed going into this function
        Connection c = ConnectionController.connectPooled(sid);

        c.setAutoCommit(false);

        int chunkSize = 100000; // number of lines per chunk (100K lines = ~50MB for a standard VCF file)
        int lineNumber = 0;

        BufferedWriter bw = null;
        String currentOutputPath = null;

        boolean stateOpen = false;

        String parentDirectory = file.getParentFile().getAbsolutePath();

        String line;
        while ((line = br.readLine()) != null) {
            lineNumber++;

            // start a new output file
            if (lineNumber % chunkSize == 1) {
                currentOutputPath = parentDirectory + "/" + extractFileName(file.getAbsolutePath()) + "_" + (lineNumber / chunkSize);
                LOG.info("Opening new partial file " + currentOutputPath);
                bw = new BufferedWriter(new FileWriter(currentOutputPath));
                stateOpen = true;
            }

            // write line to chunk file
            bw.write(line + "\n");

            // close and upload this output file
            if (lineNumber % chunkSize == 0) {
                bw.close();

                LOG.info("Closing and uploading partial file " + currentOutputPath);

                String query = "LOAD DATA LOCAL INFILE '" + currentOutputPath.replaceAll("\\\\", "/") + "' "
                        + "INTO TABLE " + tableName + " "
                        + "FIELDS TERMINATED BY '" + VariantManagerUtils.FIELD_DELIMITER + "' ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' "
                        + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "' "
                        //+ " LINES TERMINATED BY '\\r\\n'";
                        + ";";

                //LOG.info(query);
                Statement s = c.createStatement();
                s.setQueryTimeout(30 * 60); // 30 minutes
                s.execute(query);

                if (cleanUp) {
                    boolean deleted = new File(currentOutputPath).delete();
                    LOG.info("Deleting " + currentOutputPath + " - " + (deleted ? "successful" : "failed"));
                }
                stateOpen = false;
            }
        }

        // write the remaining open file
        if (bw != null && stateOpen) {
            bw.close();
            String query = "LOAD DATA LOCAL INFILE '" + currentOutputPath.replaceAll("\\\\", "/") + "' "
                    + "INTO TABLE " + tableName + " "
                    + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.FIELD_DELIMITER) + "' ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' "
                    + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "'"
                    //+ " LINES TERMINATED BY '\\r\\n'"
                    + ";";

            LOG.info("Closing and uploading last partial file " + currentOutputPath);

            //LOG.info(query);
            Statement s = c.createStatement();
            s.setQueryTimeout(60 * 60); // 1 hour
            s.execute(query);

            if (cleanUp) {
                boolean deleted = new File(currentOutputPath).delete();
                LOG.info("Deleting " + currentOutputPath + " - " + (deleted ? "successful" : "failed"));
            }
        }

        LOG.info("Imported " + lineNumber + " lines of variants in total");

        c.commit();
        c.setAutoCommit(true);

        c.close();

        if (cleanUp) {
            boolean deleted = file.delete();
            LOG.info("Deleting " + file.getAbsolutePath() + " - " + (deleted ? "successful" : "failed"));
        }
    }

    @Override
    public int getPatientCountWithVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int start, int end) throws SQLException, RemoteException {

        //TODO: approximate counts??
        //might not be a good idea... don't want to miss a dna id

        TableSchema table = getCustomTableSchema(sid, projectId, referenceId);
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns("COUNT(DISTINCT " + DNA_ID.getColumnName() + ")");
        addConditionsToQuery(q, conditions);

        Condition[] cond = new Condition[3];
        cond[0] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, table.getDBColumn(CHROM), chrom);
        cond[1] = new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, table.getDBColumn(POSITION), start);
        cond[2] = new BinaryCondition(BinaryCondition.Op.LESS_THAN, table.getDBColumn(POSITION), end);
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
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, List<String> dnaIds, int limit) throws SQLException, RemoteException {

        Map<String, List<String>> results = new HashMap<String, List<String>>();

        TableSchema table = getCustomTableSchema(sessID, projID, refID);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(DNA_ID), table.getDBColumn(CHROM), table.getDBColumn(POSITION));
        addConditionsToQuery(query, conditions);
        Condition[] dnaIdConditions = new Condition[dnaIds.size()];
        for (int i = 0; i < dnaIds.size(); i++) {
            dnaIdConditions[i] = BinaryConditionMS.equalTo(table.getDBColumn(DNA_ID), dnaIds.get(i));
            results.put(dnaIds.get(i), new ArrayList<String>());
        }
        query.addCondition(ComboCondition.or(dnaIdConditions));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString() + ((limit == -1) ? "" : (" LIMIT " + limit)));

        while (rs.next()) {
            results.get(rs.getString(1)).add(rs.getString(2) + ":" + (rs.getLong(3) - 100) + "-" + (rs.getLong(3) + 100));
        }

        return results;
    }

    @Override
    public Map<String, Integer> getNumVariantsInFamily(String sessID, int projID, int refID, String famID, Condition[][] conditions) throws SQLException, RemoteException {

        //TODO: approximate counts

        String name = ProjectManager.getInstance().getVariantTableName(sessID, projID, refID, true);

        if (name == null) {
            return null;
        }

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, name);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(DNA_ID));
        q.addCustomColumns(FunctionCall.countAll());
        q.addGroupings(table.getDBColumn(DNA_ID));
        addConditionsToQuery(q, conditions);

        Map<String, String> patientToDNAIDMap = PatientManager.getInstance().getDNAIDsForFamily(sessID, projID, famID);
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

        Map<String, Integer> dnaIDsToCountMap = new HashMap<String, Integer>();

        if (!dnaIDs.isEmpty()) {

            Condition[] dnaIDConditions = new Condition[dnaIDs.size()];

            int i = 0;
            for (String dnaID : dnaIDs) {
                dnaIDConditions[i] = BinaryCondition.equalTo(table.getDBColumn(DNA_ID), dnaID);
                i++;
            }

            q.addCondition(ComboCondition.or(dnaIDConditions));

            ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

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
    public void cancelUpload(String sid, int uploadId, String tableName) {
        try {

            //remove log entry
            AnnotationLogManager.getInstance().removeAnnotationLogEntry(sid, uploadId);

            //drop staging table
            DBUtils.dropTable(sid, tableName);

        } catch (Exception ex) {
            LOG.warn("Error cancelling upload " + uploadId + " for " + tableName, ex);
        }
    }

    @Override
    public void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException {

        PooledConnection conn = ConnectionController.connectPooled(sid);
        try {
            TableSchema variantTagTable = MedSavantDatabase.VariantTagTableSchema;

            conn.setAutoCommit(false);

            //add tags
            for (int i = 0; i < variantTags.length && !Thread.currentThread().isInterrupted(); i++) {
                InsertQuery query = variantTagTable.insert(VariantTagColumns.UPLOAD_ID, uploadID,
                        VariantTagColumns.TAGKEY, variantTags[i][0],
                        VariantTagColumns.TAGVALUE, variantTags[i][1]);
                conn.createStatement().executeUpdate(query.toString());
            }
            if (Thread.currentThread().isInterrupted()) {
                conn.rollback();
            } else {
                conn.commit();
            }
        } finally {
            conn.close();
        }
    }

    public void removeTags(String sessID, int uploadID) throws SQLException {
        ConnectionController.executeUpdate(sessID, MedSavantDatabase.VariantTagTableSchema.delete(VariantTagColumns.UPLOAD_ID, uploadID).toString());
    }

    @Override
    public List<String> getDistinctTagNames(String sessID) throws SQLException {

        ResultSet rs = ConnectionController.executeQuery(sessID, MedSavantDatabase.VariantTagTableSchema.distinct().select(VariantTagColumns.TAGKEY).toString());

        List<String> tagNames = new ArrayList<String>();
        while (rs.next()) {
            tagNames.add(rs.getString(1));
        }

        return tagNames;
    }

    @Override
    public List<String> getValuesForTagName(String sessID, String tagName) throws SQLException {

        ResultSet rs = ConnectionController.executeQuery(sessID, MedSavantDatabase.VariantTagTableSchema.distinct().where(VariantTagColumns.TAGKEY, tagName).select(VariantTagColumns.TAGVALUE).toString());

        List<String> tagValues = new ArrayList<String>();
        while (rs.next()) {
            tagValues.add(rs.getString(1));
        }

        return tagValues;

    }

    @Override
    public List<Integer> getUploadIDsMatchingVariantTags(String sessID, String[][] variantTags) throws SQLException {
        TableSchema table = MedSavantDatabase.VariantTagTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(VariantTagColumns.UPLOAD_ID));

        Condition[] orConditions = new Condition[variantTags.length];

        Set<String> seenConditions = new HashSet<String>();
        int duplicates = 0;

        for (int i = 0; i < variantTags.length; i++) {

            String strRepresentation = variantTags[i][0] + ":" + variantTags[i][1];

            if (seenConditions.contains(strRepresentation)) {
                duplicates++;
            } else {

                orConditions[i] = ComboCondition.and(new Condition[]{
                            BinaryCondition.equalTo(table.getDBColumn(VariantTagColumns.TAGKEY), variantTags[i][0]),
                            BinaryCondition.equalTo(table.getDBColumn(VariantTagColumns.TAGVALUE), variantTags[i][1])});
                seenConditions.add(strRepresentation);
            }
        }

        q.addCondition(ComboCondition.or(orConditions));
        q.addGroupings(table.getDBColumn(VariantTagColumns.UPLOAD_ID));
        q.addHaving(BinaryCondition.equalTo(FunctionCall.countAll(), variantTags.length - duplicates));

        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        List<Integer> results = new ArrayList<Integer>();
        while (rs.next()) {
            results.add(rs.getInt(1));
        }

        return results;
    }

    @Override
    public SimpleVariantFile[] getUploadedFiles(String sessID, int projID, int refID) throws SQLException, RemoteException {

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, ProjectManager.getInstance().getVariantTableName(sessID, projID, refID, true));
        TableSchema tagTable = MedSavantDatabase.VariantTagTableSchema;
        TableSchema pendingTable = MedSavantDatabase.VariantpendingupdateTableSchema;
        TableSchema fileTable = MedSavantDatabase.VariantFileTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.setIsDistinct(true);
        query.addColumns(table.getDBColumn(UPLOAD_ID), table.getDBColumn(FILE_ID));

        ResultSet idRs = ConnectionController.executeQuery(sessID, query.toString());

        List<Integer> uplIDs = new ArrayList<Integer>();
        List<Integer> fileIDs = new ArrayList<Integer>();
        while (idRs.next()) {
            uplIDs.add(idRs.getInt(1));
            fileIDs.add(idRs.getInt(2));
        }

        if (uplIDs.isEmpty()) {
            return new SimpleVariantFile[0];
        }

        Condition[] idConditions = new Condition[uplIDs.size()];
        for (int i = 0; i < uplIDs.size(); i++) {
            idConditions[i] = ComboCondition.and(
                    BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uplIDs.get(i)),
                    BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileIDs.get(i)));
        }

        SelectQuery q = new SelectQuery();
        q.addFromTable(tagTable.getTable());
        q.addFromTable(pendingTable.getTable());
        q.addFromTable(fileTable.getTable());
        q.addColumns(
                tagTable.getDBColumn(VariantTagColumns.UPLOAD_ID),
                tagTable.getDBColumn(VariantTagColumns.TAGVALUE),
                fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID),
                fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_NAME),
                pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_USER));
        q.addCondition(BinaryCondition.equalTo(tagTable.getDBColumn(VariantTagColumns.TAGKEY), VariantTag.UPLOAD_DATE));
        q.addCondition(BinaryCondition.equalTo(tagTable.getDBColumn(VariantTagColumns.UPLOAD_ID), pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID)));
        q.addCondition(BinaryCondition.equalTo(pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        q.addCondition(BinaryCondition.equalTo(pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
        q.addCondition(BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID)));
        q.addCondition(ComboCondition.or(idConditions));

        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        List<SimpleVariantFile> result = new ArrayList<SimpleVariantFile>();
        while (rs.next()) {
            result.add(new SimpleVariantFile(rs.getInt(1), rs.getInt(3), rs.getString(4), rs.getString(1), rs.getString(5)));
        }
        return result.toArray(new SimpleVariantFile[0]);
    }

    @Override
    public List<String[]> getTagsForUpload(String sessID, int uplID) throws SQLException, RemoteException {

        ResultSet rs = ConnectionController.executeQuery(sessID, MedSavantDatabase.VariantTagTableSchema.where(VariantTagColumns.UPLOAD_ID, uplID).select(VariantTagColumns.TAGKEY, VariantTagColumns.TAGVALUE).toString());

        List<String[]> result = new ArrayList<String[]>();
        while (rs.next()) {
            result.add(new String[]{rs.getString(1), rs.getString(2)});
        }
        return result;
    }

    /*
     * @Override public Set<StarredVariant> getStarredVariants(String sid, int
     * projectId, int referenceId) throws SQLException, RemoteException {
     *
     * TableSchema table = MedSavantDatabase.VariantStarredTableSchema;
     *
     * SelectQuery q = new SelectQuery(); q.addFromTable(table.getTable());
     * q.addColumns(
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP));
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID),
     * projectId));
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID),
     * referenceId));
     *
     * ResultSet rs = ConnectionController.executeQuery(sid, q.toString());
     *
     * Set<StarredVariant> result = new HashSet<StarredVariant>();
     * while(rs.next()) { result.add(new StarredVariant(
     * rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID),
     * rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
     * rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID),
     * rs.getString(VariantStarredTableSchema.COLUMNNAME_OF_USER),
     * rs.getString(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION),
     * rs.getTimestamp(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP))); }
     * return result; }
     */
    @Override
    public List<VariantComment> getVariantComments(String sid, int projectId, int referenceId, int uploadId, int fileID, int variantID) throws SQLException, RemoteException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID), fileID));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID), variantID));

        //System.out.println(q.toString());

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        List<VariantComment> result = new ArrayList<VariantComment>();
        while (rs.next()) {
            result.add(new VariantComment(
                    rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID),
                    rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID),
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
    public void addVariantComments(String sid, List<VariantComment> variants) throws SQLException, RemoteException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        Connection c = ConnectionController.connectPooled(sid);
        c.setAutoCommit(false);

        for (VariantComment variant : variants) {
            InsertQuery q = new InsertQuery(table.getTable());
            List<DbColumn> columnsList = table.getColumns();
            Column[] columnsArray = new Column[columnsList.size()];
            columnsArray = columnsList.toArray(columnsArray);
            q.addColumns(columnsArray, variant.toArray(variant.getProjectId(), variant.getReferenceId()));
            c.createStatement().executeUpdate(q.toString());
        }

        c.commit();
        c.setAutoCommit(true);
        c.close();
    }


    /*
     * private void updateStarredVariant(Connection c, int projectId, int
     * referenceId, VariantComment variant) throws SQLException {
     *
     * TableSchema table = MedSavantDatabase.VariantStarredTableSchema;
     *
     * UpdateQuery q = new UpdateQuery(table.getTable());
     * //q.addSetClause(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER),
     * variant.getUser());
     * q.addSetClause(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION),
     * variant.getDescription());
     * q.addSetClause(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP),
     * variant.getTimestamp());
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID),
     * projectId));
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID),
     * referenceId));
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID),
     * variant.getUploadId()));
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
     * variant.getFileId()));
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID),
     * variant.getVariantId()));
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER),
     * variant.getUser()));
     *
     * c.createStatement().executeUpdate(q.toString()); }
     */
    @Override
    public void removeVariantComments(String sessID, List<VariantComment> comments) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        Connection c = ConnectionController.connectPooled(sessID);
        c.setAutoCommit(false);

        for (VariantComment vc : comments) {
            DeleteQuery q = new DeleteQuery(table.getTable());
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), vc.getProjectId()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), vc.getReferenceId()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID), vc.getUploadId()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID), vc.getFileId()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID), vc.getVariantId()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER), vc.getUser()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP), vc.getTimestamp()));

            ConnectionController.executeUpdate(sessID, q.toString());
        }

        c.commit();
        c.setAutoCommit(true);
        c.close();
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

        ConnectionController.executeUpdate(sid, q.toString());
    }

    public void removeEntryFromFileTable(String sessID, int uploadID, int fileID) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantFileTableSchema;

        DeleteQuery q = new DeleteQuery(table.getTable());
        q.addCondition(ComboCondition.and(new Condition[]{
                    BinaryCondition.equalTo(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadID),
                    BinaryCondition.equalTo(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileID)
                }));

        ConnectionController.executeUpdate(sessID, q.toString());
    }

    @Override
    public Map<SimplePatient, Integer> getPatientHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<SimplePatient> patients) throws SQLException, RemoteException {

        //get dna ids
        List<String> dnaIds = new ArrayList<String>();
        for (SimplePatient sp : patients) {
            for (String id : sp.getDnaIds()) {
                if (!dnaIds.contains(id)) {
                    dnaIds.add(id);
                }
            }
        }

        Map<String, Integer> dnaIdMap = getDNAIDHeatMap(sessID, projID, refID, conditions, dnaIds);

        //map back to simple patients;
        Map<SimplePatient, Integer> result = new HashMap<SimplePatient, Integer>();
        for (SimplePatient p : patients) {
            Integer count = 0;
            for (String dnaId : p.getDnaIds()) {
                Integer i = dnaIdMap.get(dnaId);
                if (i != null) {
                    count += i;
                }
            }
            result.put(p, count);
        }

        return result;
    }

    @Override
    public Map<String, Integer> getDNAIDHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException {

        Map<String, Integer> dnaIDMap = new HashMap<String, Integer>();

        if (!dnaIDs.isEmpty()) {
            Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sessID, projID, refID, true);
            String tablename = (String) variantTableInfo[0];
            String tablenameSub = (String) variantTableInfo[1];
            float multiplier = (Float) variantTableInfo[2];

            TableSchema subTable = CustomTables.getInstance().getCustomTableSchema(sessID, tablenameSub);
            TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

            //combine conditions
            Condition[] c1 = new Condition[conditions.length];
            for (int i = 0; i < conditions.length; i++) {
                c1[i] = ComboCondition.and(conditions[i]);
            }
            Condition c2 = ComboCondition.or(c1);

            // Try sub table first.
            getDNAIDHeatMapHelper(sessID, subTable, multiplier, dnaIDs, c2, true, dnaIDMap);

            // Determine dnaIDs with no value yet.
            List<String> dnaIDs2 = new ArrayList<String>();
            for (String id : dnaIDs) {
                if (!dnaIDMap.containsKey(id)) {
                    dnaIDs2.add(id);
                }
            }

            //get remaining dna ids from actual table
            if (!dnaIDs2.isEmpty()) {
                getDNAIDHeatMapHelper(sessID, table, 1, dnaIDs2, c2, false, dnaIDMap);
            }
        }

        return dnaIDMap;
    }

    private void getDNAIDHeatMapHelper(String sessID, TableSchema table, float multiplier, Collection<String> dnaIDs, Condition c, boolean useThreshold, Map<String, Integer> map) throws SQLException {

        //generate conditions from dna ids
        Condition dnaCondition = new InCondition(table.getDBColumn(DNA_ID), dnaIDs);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addColumns(table.getDBColumn(DNA_ID));
        q.addCondition(ComboCondition.and(new Condition[]{dnaCondition, c}));
        q.addGroupings(table.getDBColumn(DNA_ID));

        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        while (rs.next()) {
            int value = (int) (rs.getInt(1) * multiplier);
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

    private Annotation[] getAnnotationsFromIDs(int[] annotIDs, String sessID) throws RemoteException, SQLException {
        int numAnnotations = annotIDs.length;
        Annotation[] annotations = new Annotation[numAnnotations];
        for (int i = 0; i < numAnnotations; i++) {
            annotations[i] = AnnotationManager.getInstance().getAnnotation(sessID, annotIDs[i]);
            LOG.info("\t" + (i + 1) + ". " + annotations[i].getProgram() + " " + annotations[i].getReferenceName() + " " + annotations[i].getVersion());
        }
        return annotations;
    }
    private static boolean cleanUp = true;

    private static class ParserThread extends Thread {

        private final boolean includeHomoRef;
        private final int updateID;
        private final File vcfFile;
        private final BufferedReader reader;
        private final File outFile;
        private final int fileID;
        private final CustomField[] customFields;
        private final Annotation[] annotations;
        private final String sessID;
        private final String tableName;
        private final String baseDir;

        private ParserThread(String sessID, File vcfFile, File outFile, int updateID, int fileID, boolean includeHomoRef, Annotation[] annotations, CustomField[] customFields, String tableName) throws FileNotFoundException, IOException {
            this.vcfFile = vcfFile;
            this.outFile = outFile;
            this.fileID = fileID;
            this.updateID = updateID;
            this.includeHomoRef = includeHomoRef;
            this.customFields = customFields;
            this.annotations = annotations;
            this.sessID = sessID;
            this.tableName = tableName;
            this.baseDir = outFile.getParentFile().getAbsolutePath();

            if (IOUtils.isGZipped(vcfFile)) {
                reader = new BufferedReader(new InputStreamReader(new BlockCompressedInputStream(vcfFile)));
            } else {
                reader = new BufferedReader(new FileReader(vcfFile));
            }
        }

        @Override
        public String toString() {
            return "ParserThread{" + "includeHomoRef=" + includeHomoRef + ", updateID=" + updateID + ", vcfFile=" + vcfFile + ", reader=" + reader + ", outFile=" + outFile + ", fileID=" + fileID + ", customFields=" + customFields + ", annotations=" + annotations + ", sessID=" + sessID + ", tableName=" + tableName + ", baseDir=" + baseDir + '}';
        }

        @Override
        public void run() {

            List<String> filesUsed = new ArrayList<String>();

            try {
                VCFParser.parseVariantsFromReader(reader, outFile, updateID, fileID, includeHomoRef);

                String tempFilename = outFile.getAbsolutePath();
                filesUsed.add(tempFilename);
                String currentFilename = tempFilename;

                //add custom fields
                LOG.info("Adding custom VCF fields");
                if (customFields.length > 0) {
                    String customFieldFilename = currentFilename + "_vcf";
                    filesUsed.add(customFieldFilename);
                    VariantManagerUtils.addCustomVCFFields(currentFilename, customFieldFilename, customFields, INDEX_OF_CUSTOM_INFO); //last of the default fields
                    currentFilename = customFieldFilename;
                }

                if (annotations.length > 0) {

                    //sort variants
                    LOG.info("Sorting variants");
                    //makeProgress(sessID, "Sorting variants...", fract);
                    String sortedVariants = currentFilename + "_sorted";
                    filesUsed.add(sortedVariants);
                    VariantManagerUtils.sortFileByPosition(currentFilename, sortedVariants);
                    VariantManagerUtils.logFileSize(sortedVariants);
                    currentFilename = sortedVariants;
                    //fract += SORTING_FRACTION / vcfFiles.length;

                    //annotate
                    String annotatedFilename = currentFilename + "_annotated";
                    filesUsed.add(annotatedFilename);
                    LOG.info("File containing annotated variants, sorted by position: " + annotatedFilename);
                    //makeProgress(sessID, "Annotating...", fract);

                    LOG.info("Annotating variants in " + currentFilename + ", destination " + annotatedFilename);

                    long startTime = System.currentTimeMillis();


                    BatchVariantAnnotator bva = new BatchVariantAnnotator(new File(currentFilename), new File(annotatedFilename), annotations, sessID);
                    bva.performBatchAnnotationInParallel();

                    long endTime = System.currentTimeMillis();

                    long duration = (endTime - startTime) / 1000 / 60;

                    LOG.info("Completed annotation, taking " + duration + " minutes");

                    VariantManagerUtils.logFileSize(annotatedFilename);
                    currentFilename = annotatedFilename;
                    //fract += ANNOTATING_FRACTION / vcfFiles.length;

                    //split
                    LOG.info("Splitting annotation file into multiple files by file ID");
                    //makeProgress(sessID, "Splitting files...", fract);
                    File splitDir = new File(baseDir, "split-by-id");
                    splitDir.mkdir();
                    VariantManagerUtils.splitFileOnColumn(splitDir, currentFilename, 1);
                    //fract += SPLITTING_FRACTION / vcfFiles.length;

                    //merge
                    LOG.info("Merging files");
                    //makeProgress(sessID, "Merging files...", fract);
                    String outputFilenameMerged = tempFilename + "_annotated_merged";
                    filesUsed.add(outputFilenameMerged);
                    LOG.info("File containing annotated variants, sorted by file: " + outputFilenameMerged);
                    VariantManagerUtils.concatenateFilesInDir(splitDir, outputFilenameMerged);
                    VariantManagerUtils.logFileSize(outputFilenameMerged);
                    currentFilename = outputFilenameMerged;
                }

                //upload to staging table
                LOG.info("Loading data into table: " + tableName + " from " + currentFilename);
                //makeProgress(sessID, "Loading data into table...", fract);
                uploadFileToVariantTable(sessID, new File(currentFilename), tableName);
                //fract += LOAD_TABLE_FRACTION;

            } catch (Exception e) {
                EmailLogger.logByEmail("Error running parser on " + vcfFile.getAbsolutePath(), "Here is the object: " + toString() + ". Here is the message: " + ExceptionUtils.getStackTrace(e));
                LOG.error(e);
            }

            //cleanup
            System.gc();
            if (cleanUp) {
                for (String filename : filesUsed) {
                    boolean deleted = (new File(filename)).delete();
                    LOG.info("Deleting " + filename + " - " + (deleted ? "successful" : "failed"));
                }
            }

        }
    }
}
