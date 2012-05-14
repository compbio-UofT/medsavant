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
package org.ut.biolab.medsavant.db.variants.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Column;

import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.model.AnnotationLog.Status;
import org.ut.biolab.medsavant.model.SimpleVariantFile;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.db.util.CustomTables;
import org.ut.biolab.medsavant.db.util.FileServer;
import org.ut.biolab.medsavant.db.util.ServerDirectorySettings;
import org.ut.biolab.medsavant.serverapi.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.serverapi.AnnotationQueryUtil;
import org.ut.biolab.medsavant.serverapi.ProjectQueryUtil;
import org.ut.biolab.medsavant.serverapi.ReferenceQueryUtil;
import org.ut.biolab.medsavant.serverapi.VariantQueryUtil;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.log.ServerLogger;

/**
 *
 * @author Andrew
 */
public class VariantManager extends MedSavantServerUnicastRemoteObject implements VariantManagerAdapter {

    private static VariantManager instance;

    public static synchronized VariantManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new VariantManager();
        }
        return instance;
    }

    public VariantManager() throws RemoteException {
    }


    /*
     * Make all variant tables live for a project. All users accessing this db will be logged out.
     */
    @Override
    public void publishVariants(String sid, int projectID) throws Exception {

        ServerLogger.log(VariantManager.class, "Beginning publish of all tables for project " + projectID);

        Connection c = (ConnectionController.connectPooled(sid));

        //get update ids and references
        ServerLogger.log(VariantManager.class, "Getting map of update ids");
        List<Integer> refIds = ReferenceQueryUtil.getInstance().getReferenceIdsForProject(sid, projectID);
        Map<Integer, Integer> ref2Update = new HashMap<Integer, Integer>();
        for (Integer refId : refIds) {
            ref2Update.put(refId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectID, refId, false));
        }

        //update annotation log table
        ServerLogger.log(VariantManager.class, "Setting log status to published");
        for (Integer refId : ref2Update.keySet()) {
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, ref2Update.get(refId), Status.PUBLISHED);
        }

        //publish
        ServerLogger.log(VariantManager.class, "Terminating active sessions");
        SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sid), "Administrator (" + SessionController.getInstance().getUserForSession(sid) + ") published new variants");
        ServerLogger.log(VariantManager.class, "Publishing tables");
        for (Integer refId : ref2Update.keySet()) {
            ProjectQueryUtil.getInstance().publishVariantTable(c, projectID, refId, ref2Update.get(refId));
        }

        ServerLogger.log(VariantManager.class, "Publish complete");
        c.close();
    }


    /*
     * Make a variant table live. All users accessing this db will be logged out.
     */
    @Override
    public void publishVariants(String sid, int projectID, int referenceID, int updateID) throws Exception {
        ServerLogger.log(VariantManager.class, "Publishing table. pid:" + projectID + " refid:" + referenceID + " upid:" + updateID);
        Connection c = (ConnectionController.connectPooled(sid));
        ServerLogger.log(VariantManager.class, "Setting log status to published");
        AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, updateID, Status.PUBLISHED);
        ServerLogger.log(VariantManager.class, "Terminating active sessions");
        SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sid), "Administrator (" + SessionController.getInstance().getUserForSession(sid) + ") published new variants");
        ServerLogger.log(VariantManager.class, "Publishing table");
        ProjectQueryUtil.getInstance().publishVariantTable(c, projectID, referenceID, updateID);
        ServerLogger.log(VariantManager.class, "Publish complete");
        c.close();
    }


    /*
     * Remove an unpublished variant table.
     */
    @Override
    public void cancelPublish(String sid, int projectID, int referenceID, int updateID) throws Exception {
        ServerLogger.log(VariantManager.class, "Cancelling publish. pid:" + projectID + " refid:" + referenceID + " upid:" + updateID);
        ProjectQueryUtil.getInstance().removeTables(sid, projectID, referenceID, updateID, updateID);
        ServerLogger.log(VariantManager.class, "Cancel complete");
    }

    /*
     * Perform updates to custom vcf fields and other annotations. Will result
     * in the creation of a new, unpublished, up-to-date variant table.
     */
    @Override
    public int updateTable(String sid, int projectId, int referenceId, int[] annotationIds, List<CustomField> variantFields) throws Exception {

        ServerLogger.log(VariantManager.class, "Beginning new variant update");

        String user = SessionController.getInstance().getUserForSession(sid);

        //generate directory
        ServerLogger.log(VariantManager.class, "Generating base directory");
        File basedir = ServerDirectorySettings.generateDateStampDirectory(new File("./"));
        ServerLogger.log(VariantManager.class, "Base directory: " + basedir.getAbsolutePath());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + basedir);
        p.waitFor();

        //add log
        ServerLogger.log(VariantManager.class, "Adding log and generating update id");
        int updateId = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sid, projectId, referenceId, org.ut.biolab.medsavant.model.AnnotationLog.Action.ADD_VARIANTS, user);

        try {

            //dump existing table
            ServerLogger.log(VariantManager.class, "Dumping existing variant table");
            String existingTableName = ProjectQueryUtil.getInstance().getVariantTablename(sid, projectId, referenceId, false);
            File variantDumpFile = new File(basedir,"temp_" + existingTableName);
            String variantDump = variantDumpFile.getAbsolutePath();
            ServerLogger.log(VariantManager.class, "Dumping variants to file");
            VariantManagerUtils.variantsToFile(sid,existingTableName, variantDumpFile, null, false, 0);

            //sort variants
            ServerLogger.log(VariantManager.class, "Sorting variants");
            String sortedVariants = variantDump + "_sorted";
            VariantManagerUtils.sortFileByPosition(variantDump, sortedVariants);

            //add custom vcf fields
            ServerLogger.log(VariantManager.class, "Adding custom vcf fields");
            String vcfAnnotatedVariants = sortedVariants + "_vcf";
            VariantManagerUtils.addCustomVcfFields(sortedVariants, vcfAnnotatedVariants, variantFields, DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO); //last of the default fields

            //annotate
            String outputFilename = vcfAnnotatedVariants + "_annotated";
            ServerLogger.log(VariantManager.class, "File containing annotated variants, sorted by position: " + outputFilename);
            VariantManagerUtils.annotateTDF(sid,vcfAnnotatedVariants, outputFilename, annotationIds);

            //split
            File splitDir = new File(basedir,"splitDir");
            splitDir.mkdir();
            ServerLogger.log(VariantManager.class, "Splitting annotation file into multiple files by file ID");
            for (File f : splitDir.listFiles()) {
                VariantManagerUtils.removeTemp(f);
            }
            VariantManagerUtils.splitFileOnColumn(splitDir, outputFilename, 1);
            String outputFilenameMerged = outputFilename + "_merged";

            //merge
            ServerLogger.log(VariantManager.class, "File containing annotated variants, sorted by file: " + outputFilenameMerged);
            ServerLogger.log(VariantManager.class, "Merging files");
            VariantManagerUtils.concatenateFilesInDir(splitDir, outputFilenameMerged);

            //create the staging table
            ServerLogger.log(VariantManager.class, "Creating new variant table for resulting variants");
            ProjectQueryUtil.getInstance().setCustomVariantFields(sid, projectId, referenceId, updateId, variantFields);
            String tableName = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, annotationIds, true);
            String tableNameSub = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), false, true);

            //upload to staging table
            ServerLogger.log(VariantManager.class, "Uploading variants to table: " + tableName);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid, new File(outputFilenameMerged), tableName);

            //upload to sub table
            File subFile = new File(outputFilenameMerged + "_sub");
            ServerLogger.log(VariantManager.class, "Generating sub file: " + subFile.getAbsolutePath());
            VariantManagerUtils.generateSubset(new File(outputFilenameMerged), subFile);
            ServerLogger.log(VariantManager.class, "Importing to: " + tableNameSub);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid, subFile, tableNameSub);
            //ProjectQueryUtil.getInstance().addSubsetInfoToMap(sid, projectId, referenceId, updateId, tableNameSub, ProjectQueryUtil.getInstance().getMultiplier(sid, tableName, tableNameSub));
            
            //add entries to tablemap
            ProjectQueryUtil.getInstance().addTableToMap(sid, projectId, referenceId, updateId, false, tableName, tableNameSub);
            
            //cleanup
            ServerLogger.log(VariantManager.class, "Dropping old table(s)");
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

    /*
     * Start the upload process for new vcf files. Will result in the creation
     * of a new, unpublished, up-to-date variant table.
     */
    @Override
    public int uploadVariants(String sid, RemoteInputStream[] fileStreams, String[] fileNames, int projectId, int referenceId, String[][] variantTags, boolean includeHomoRef) throws RemoteException, IOException, Exception {
        File[] vcfFiles = new File[fileStreams.length];

        int i = 0;
        for (RemoteInputStream s : fileStreams) {
            vcfFiles[i] = FileServer.getInstance().sendFile(s, fileNames[i]);
            i++;
        }

        return uploadVariants(sid, vcfFiles, fileNames, projectId, referenceId, variantTags, includeHomoRef);
    }
    
    /*
     * Helper that does all the actual work of uploading new vcf files.
     */
    private static int uploadVariants(String sid, File[] vcfFiles, String[] fileNames, int projectId, int referenceId, String[][] variantTags, boolean includeHomoRef) throws Exception {
        
        ServerLogger.log(VariantManager.class, "Beginning new vcf upload");

        String user = SessionController.getInstance().getUserForSession(sid);

        //generate directory
        ServerLogger.log(VariantManager.class, "Generating base directory");
        File baseDir = ServerDirectorySettings.generateDateStampDirectory(new File("./"));
        ServerLogger.log(VariantManager.class, "Base directory: " + baseDir.getAbsolutePath());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir);
        p.waitFor();

        //add log
        ServerLogger.log(VariantManager.class, "Adding log and generating update id");
        int updateId = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sid, projectId, referenceId, org.ut.biolab.medsavant.model.AnnotationLog.Action.ADD_VARIANTS, user);
        
        try {

            //dump existing table
            String existingTableName = ProjectQueryUtil.getInstance().getVariantTablename(sid, projectId, referenceId, false);
            File existingVariantsFile = new File(baseDir,"temp_proj" + projectId + "_ref" + referenceId + "_update" + updateId);
            ServerLogger.log(VariantManager.class, "Dumping variants to file");
            VariantManagerUtils.variantsToFile(sid,existingTableName, existingVariantsFile, null, true, 0);

            //create the staging table
            ServerLogger.log(VariantManager.class, "Creating new variant table for resulting variants");
            ProjectQueryUtil.getInstance().setCustomVariantFields(
                    sid, projectId, referenceId, updateId,
                    ProjectQueryUtil.getInstance().getCustomVariantFields(sid, projectId, referenceId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectId, referenceId, false)));
            String tableName = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), true);
            String tableNameSub = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), false, true);

            //upload dump to staging table
            ServerLogger.log(VariantManager.class, "Uploading variants to table: " + tableName);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid, existingVariantsFile, tableName);

            VCFIterator parser = new VCFIterator(vcfFiles, baseDir, updateId, includeHomoRef);
            File f;
            while ((f = parser.next()) != null) {
                
                List<String> filesUsed = new ArrayList<String>();

                String tempFilename = f.getAbsolutePath();
                filesUsed.add(tempFilename);
                String currentFilename = tempFilename;

                //add custom fields
                ServerLogger.log(VariantManager.class, "Adding custom vcf fields");
                List<CustomField> customFields = ProjectQueryUtil.getInstance().getCustomVariantFields(sid, projectId, referenceId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectId, referenceId, false));
                if (!customFields.isEmpty()) {
                    String customFieldFilename = currentFilename + "_vcf";
                    filesUsed.add(customFieldFilename);
                    VariantManagerUtils.addCustomVcfFields(currentFilename, customFieldFilename, customFields, DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO); //last of the default fields
                    currentFilename = customFieldFilename;
                }

                //get annotation ids
                int[] annotationIds = AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId);

                if (annotationIds.length > 0) {

                    //sort variants
                    ServerLogger.log(VariantManager.class, "Sorting variants");
                    String sortedVariants = currentFilename + "_sorted";
                    filesUsed.add(sortedVariants);
                    VariantManagerUtils.sortFileByPosition(currentFilename, sortedVariants);
                    VariantManagerUtils.logFileSize(sortedVariants);
                    currentFilename = sortedVariants;

                    //annotate
                    String annotatedFilename = currentFilename + "_annotated";
                    filesUsed.add(annotatedFilename);
                    ServerLogger.log(VariantManager.class, "File containing annotated variants, sorted by position: " + annotatedFilename);
                    VariantManagerUtils.annotateTDF(sid, currentFilename, annotatedFilename, annotationIds);
                    VariantManagerUtils.logFileSize(annotatedFilename);
                    currentFilename = annotatedFilename;
                }

                if (annotationIds.length > 0) {

                    //split
                    File splitDir = new File(baseDir,"splitDir");
                    splitDir.mkdir();
                    ServerLogger.log(VariantManager.class, "Splitting annotation file into multiple files by file ID");
                    VariantManagerUtils.splitFileOnColumn(splitDir, currentFilename, 1);

                    //merge
                    ServerLogger.log(VariantManager.class, "Merging files");
                    String outputFilenameMerged = tempFilename + "_annotated_merged";
                    filesUsed.add(outputFilenameMerged);
                    ServerLogger.log(VariantManager.class, "File containing annotated variants, sorted by file: " + outputFilenameMerged);
                    VariantManagerUtils.concatenateFilesInDir(splitDir, outputFilenameMerged);
                    VariantManagerUtils.logFileSize(outputFilenameMerged);
                    currentFilename = outputFilenameMerged;
                }

                //upload to staging table
                ServerLogger.log(VariantManager.class, "Uploading variants to table: " + tableName);
                VariantQueryUtil.getInstance().uploadFileToVariantTable(sid,new File(currentFilename), tableName);
                
                //cleanup
                System.gc();
                for (String filename : filesUsed) {
                    boolean deleted = (new File(filename)).delete();
                    ServerLogger.log(VariantManager.class, "Deleting " + filename + " - " + (deleted ? "successful" : "failed"));
                }

            }
            
            //create sub table dump
            File subDump = new File(baseDir,"temp_proj" + projectId + "_ref" + referenceId + "_update" + updateId + "_sub");
            ServerLogger.log(VariantManager.class, "Dumping variants to file for sub table");
            VariantManagerUtils.variantsToFile(sid,tableName, subDump, null, true, VariantManagerUtils.determineStepForSubset(VariantQueryUtil.getInstance().getNumFilteredVariantsHelper(sid, tableName, new Condition[][]{})));
            
            //upload to sub table
            ServerLogger.log(VariantManager.class, "Uploading variants to table: " + tableNameSub);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid, subDump, tableNameSub);
            //ProjectQueryUtil.getInstance().addSubsetInfoToMap(sid, projectId, referenceId, updateId, tableNameSub, ProjectQueryUtil.getInstance().getMultiplier(sid, tableName, tableNameSub)); 

            //add entries to tablemap
            ProjectQueryUtil.getInstance().addTableToMap(sid, projectId, referenceId, updateId, false, tableName, tableNameSub);
            
            //add tags to upload
            ServerLogger.log(VariantManager.class, "Adding upload tags");
            VariantManagerUtils.addTagsToUpload(sid, updateId, variantTags);

            //add entries to file table
            ServerLogger.log(VariantManager.class, "Adding entries to file table");
            for (int i = 0; i < fileNames.length; i++) {
                VariantQueryUtil.getInstance().addEntryToFileTable(sid, updateId, i, fileNames[i]);
            }

            //cleanup
            ServerLogger.log(VariantManager.class, "Dropping old table(s)");
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

    /*
     * Helper that does all the actual work of uploading new vcf files.
     */
    /*private static int uploadVariants(String sid, File[] vcfFiles, String[] fileNames, int projectId, int referenceId, String[][] variantTags, boolean includeHomoRef) throws Exception {

        ServerLogger.log(VariantManager.class, "Beginning new vcf upload");

        String user = SessionController.getInstance().getUserForSession(sid);

        //generate directory
        ServerLogger.log(VariantManager.class, "Generating base directory");
        File baseDir = ServerDirectorySettings.generateDateStampDirectory(new File("./"));
        ServerLogger.log(VariantManager.class, "Base directory: " + baseDir.getAbsolutePath());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir);
        p.waitFor();

        //add log
        ServerLogger.log(VariantManager.class, "Adding log and generating update id");
        int updateId = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sid, projectId, referenceId, org.ut.biolab.medsavant.db.model.AnnotationLog.Action.ADD_VARIANTS, user);

        try {

            String currentFilename;

            //parse and cat new files
            ServerLogger.log(VariantManager.class, "Parsing vcf files and concatenating");
            File tempFile = VariantManagerUtils.parseVCFs(vcfFiles, baseDir, updateId, includeHomoRef);
            String tempFilename = tempFile.getAbsolutePath();
            currentFilename = tempFilename;

            //add custom fields
            ServerLogger.log(VariantManager.class, "Adding custom vcf fields");
            List<CustomField> customFields = ProjectQueryUtil.getInstance().getCustomVariantFields(sid, projectId, referenceId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectId, referenceId, false));
            if (!customFields.isEmpty()) {
                String customFieldFilename = currentFilename + "_vcf";
                VariantManagerUtils.addCustomVcfFields(currentFilename, customFieldFilename, customFields, DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO); //last of the default fields
                currentFilename = customFieldFilename;
            }

            //get annotation ids
            int[] annotationIds = AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId);

            if (annotationIds.length > 0) {

                //sort variants
                ServerLogger.log(VariantManager.class, "Sorting variants");
                String sortedVariants = currentFilename + "_sorted";
                VariantManagerUtils.sortFileByPosition(currentFilename, sortedVariants);
                VariantManagerUtils.logFileSize(sortedVariants);
                currentFilename = sortedVariants;

                //annotate
                String annotatedFilename = currentFilename + "_annotated";
                ServerLogger.log(VariantManager.class, "File containing annotated variants, sorted by position: " + annotatedFilename);
                VariantManagerUtils.annotateTDF(sid, currentFilename, annotatedFilename, annotationIds);
                VariantManagerUtils.logFileSize(annotatedFilename);
                currentFilename = annotatedFilename;
            }

            //dump existing table
            String existingTableName = ProjectQueryUtil.getInstance().getVariantTablename(sid, projectId, referenceId, false);
            File existingVariantsFile = new File(baseDir,"temp_proj" + projectId + "_ref" + referenceId + "_update" + updateId);
            ServerLogger.log(VariantManager.class, "Dumping variants to file");
            VariantManagerUtils.variantsToFile(sid,existingTableName, existingVariantsFile, null, true, 0);
            VariantManagerUtils.logFileSize(tempFilename);

            //cat existing table to new annotations
            ServerLogger.log(VariantManager.class, "Concatenating existing variants with new");
            VariantManagerUtils.appendToFile(existingVariantsFile.getAbsolutePath(), currentFilename);
            currentFilename = existingVariantsFile.getAbsolutePath();

            if (annotationIds.length > 0) {

                //split
                File splitDir = new File(baseDir,"splitDir");
                splitDir.mkdir();
                ServerLogger.log(VariantManager.class, "Splitting annotation file into multiple files by file ID");
                VariantManagerUtils.splitFileOnColumn(splitDir, currentFilename, 1);

                //merge
                ServerLogger.log(VariantManager.class, "Merging files");
                String outputFilenameMerged = tempFilename + "_annotated_merged";
                ServerLogger.log(VariantManager.class, "File containing annotated variants, sorted by file: " + outputFilenameMerged);
                VariantManagerUtils.concatenateFilesInDir(splitDir, outputFilenameMerged);
                VariantManagerUtils.logFileSize(outputFilenameMerged);
                currentFilename = outputFilenameMerged;
            }

            //create the staging table
            ServerLogger.log(VariantManager.class, "Creating new variant table for resulting variants");
            ProjectQueryUtil.getInstance().setCustomVariantFields(
                    sid, projectId, referenceId, updateId,
                    ProjectQueryUtil.getInstance().getCustomVariantFields(sid, projectId, referenceId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectId, referenceId, false)));
            String tableName = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), true);
            String tableNameSub = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), false, true);

            //upload to staging table
            ServerLogger.log(VariantManager.class, "Uploading variants to table: " + tableName);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid,new File(currentFilename), tableName);

            //upload to sub table
            File subFile = new File(currentFilename + "_sub");
            ServerLogger.log(VariantManager.class, "Generating sub file: " + subFile.getAbsolutePath());
            VariantManagerUtils.generateSubset(new File(currentFilename), subFile);
            ServerLogger.log(VariantManager.class, "Importing to: " + tableNameSub);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid, subFile, tableNameSub);
            ProjectQueryUtil.getInstance().addSubsetInfoToMap(sid, projectId, referenceId, updateId, tableNameSub, ProjectQueryUtil.getInstance().getMultiplier(sid, tableName, tableNameSub));

            //add tags to upload
            ServerLogger.log(VariantManager.class, "Adding upload tags");
            VariantManagerUtils.addTagsToUpload(sid, updateId, variantTags);

            //add entries to file table
            ServerLogger.log(VariantManager.class, "Adding entries to file table");
            for (int i = 0; i < fileNames.length; i++) {
                VariantQueryUtil.getInstance().addEntryToFileTable(sid, updateId, i, fileNames[i]);
            }

            //cleanup
            ServerLogger.log(VariantManager.class, "Dropping old table(s)");
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
    }*/

    @Override
    public int removeVariants(String sid, int projectId, int referenceId, List<SimpleVariantFile> files) throws Exception {
        ServerLogger.log(VariantManager.class, "Beginning removal of variants");

        String user = SessionController.getInstance().getUserForSession(sid);

        //generate directory
        ServerLogger.log(VariantManager.class, "Generating base directory");
        File baseDir = ServerDirectorySettings.generateDateStampDirectory(new File("./"));
        ServerLogger.log(VariantManager.class, "Base directory: " + baseDir.getAbsolutePath());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir);
        p.waitFor();

        //add log
        ServerLogger.log(VariantManager.class, "Adding log and generating update id");
        int updateId = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sid, projectId, referenceId, org.ut.biolab.medsavant.model.AnnotationLog.Action.REMOVE_VARIANTS, user);

        try {

            //dump existing except for files
            String existingTableName = ProjectQueryUtil.getInstance().getVariantTablename(sid, projectId, referenceId, false);
            File existingVariantsFile = new File(baseDir,"temp_proj" + projectId + "_ref" + referenceId + "_update" + updateId);
            ServerLogger.log(VariantManager.class, "Dumping variants to file");
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
            ServerLogger.log(VariantManager.class, "Creating new variant table for resulting variants");
            ProjectQueryUtil.getInstance().setCustomVariantFields(
                    sid, projectId, referenceId, updateId,
                    ProjectQueryUtil.getInstance().getCustomVariantFields(sid, projectId, referenceId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectId, referenceId, false)));
            String tableName = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), true);
            String tableNameSub = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), false, true);

            //upload to staging table
            ServerLogger.log(VariantManager.class, "Uploading variants to table: " + tableName);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid, existingVariantsFile, tableName);

            //upload to sub table
            File subFile = new File(existingVariantsFile.getAbsolutePath() + "_sub");
            ServerLogger.log(VariantManager.class, "Generating sub file: " + subFile.getAbsolutePath());
            VariantManagerUtils.generateSubset(existingVariantsFile, subFile);
            ServerLogger.log(VariantManager.class, "Importing to: " + tableNameSub);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid, subFile, tableNameSub);
            //ProjectQueryUtil.getInstance().addSubsetInfoToMap(sid, projectId, referenceId, updateId, tableNameSub, ProjectQueryUtil.getInstance().getMultiplier(sid, tableName, tableNameSub));

            //add entries to tablemap
            ProjectQueryUtil.getInstance().addTableToMap(sid, projectId, referenceId, updateId, false, tableName, tableNameSub);
            
            //cleanup
            ServerLogger.log(VariantManager.class, "Dropping old table(s)");
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
        File baseDir = ServerDirectorySettings.generateDateStampDirectory(new File("./"));
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir);
        p.waitFor();
        File file = new File(baseDir, "export.tdf");
        System.out.println(baseDir.getAbsolutePath());

        //select into
        System.out.println("starting select into");
        long start = System.nanoTime();
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessionId,ProjectQueryUtil.getInstance().getVariantTablename(sessionId,projectId, referenceId, true));
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        VariantQueryUtil.getInstance().addConditionsToQuery(query, conditions);
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
}
