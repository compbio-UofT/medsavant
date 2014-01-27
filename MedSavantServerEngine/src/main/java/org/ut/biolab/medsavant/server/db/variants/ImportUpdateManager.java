/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.db.variants;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.InCondition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.server.serverapi.AnnotationLogManager;
import org.ut.biolab.medsavant.server.serverapi.AnnotationManager;
import org.ut.biolab.medsavant.server.serverapi.PatientManager;
import org.ut.biolab.medsavant.server.serverapi.ProjectManager;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 *
 * @author mfiume
 */
public class ImportUpdateManager {

    private static final Log LOG = LogFactory.getLog(ImportUpdateManager.class);

    /**
     * IMPORT FILES INTO AN EXISTING TABLE
     */
    public static int doImport(String sessionID, int projectID, int referenceID, boolean publishUponCompletion, File[] vcfFiles, boolean includeHomozygousReferenceCalls, String[][] tags) throws IOException, SQLException, Exception {

        try {
            LOG.info("Starting import");
            String existingVariantTableName = ProjectManager.getInstance().getVariantTableName(sessionID, projectID, referenceID, true);
            int updateID = AnnotationLogManager.getInstance().addAnnotationLogEntry(sessionID, projectID, referenceID, AnnotationLog.Action.ADD_VARIANTS);
            File workingDirectory = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
            LOG.info("Working directory is " + workingDirectory.getAbsolutePath());

            // prepare for annotation
            File[] importedTSVFiles = doConvertVCFToTSV(vcfFiles, updateID, includeHomozygousReferenceCalls, createSubdir(workingDirectory, "converted"));
            File existingTableAsTSV = doDumpTableAsTSV(sessionID, existingVariantTableName, createSubdir(workingDirectory, "dump"), false);
            File[] allTSVFiles = ArrayUtils.addAll(importedTSVFiles, existingTableAsTSV);

            // annotate
            annotateAndUploadTSVFiles(sessionID, updateID, projectID, referenceID, allTSVFiles, createSubdir(workingDirectory, "annotate_upload"));

            // do some accounting
            addVariantFilesToDatabase(sessionID, updateID, projectID, referenceID, vcfFiles);
            VariantManagerUtils.addTagsToUpload(sessionID, updateID, tags);

            // create patients for all DNA ids in this update
            createPatientsForUpdate(sessionID, updateID, projectID, referenceID);

            if (publishUponCompletion) {
                LOG.info("Publishing");
                publishLatestUpdate(sessionID, projectID);
            } else {
                LOG.info("Not publishing");
            }

            if (VariantManager.REMOVE_WORKING_DIR) {
                LOG.info("Deleting workingDirectory " + workingDirectory);
                MiscUtils.deleteDirectory(workingDirectory);
            }

            LOG.info("Finished import");

            return updateID;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * UPDATE AN EXISTING TABLE
     */
    public static int doUpdate(String sessionID, int projectID, int referenceID, int[] annotationIDs, CustomField[] customFields, boolean publishUponCompletion) throws Exception {

        try {

            String existingVariantTableName = ProjectManager.getInstance().getVariantTableName(sessionID, projectID, referenceID, true);
            int updateID = AnnotationLogManager.getInstance().addAnnotationLogEntry(sessionID, projectID, referenceID, AnnotationLog.Action.UPDATE_TABLE);
            File workingDirectory = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
            File existingTableAsTSV = doDumpTableAsTSV(sessionID, existingVariantTableName, createSubdir(workingDirectory, "dump"), false);
            annotateAndUploadTSVFiles(sessionID, updateID, projectID, referenceID, annotationIDs, customFields, new File[]{existingTableAsTSV}, createSubdir(workingDirectory, "annotate_upload"));
            if (publishUponCompletion) {
                publishLatestUpdate(sessionID, projectID);
            }
            if (VariantManager.REMOVE_WORKING_DIR) {
                MiscUtils.deleteDirectory(workingDirectory);
            }

            return updateID;

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Annotate using existing annotation / custom field sets
     */
    private static void annotateAndUploadTSVFiles(String sessionID, int updateID, int projectID, int referenceID, File[] tsvFiles, File workingDir) throws Exception {
        int[] annotationIDs = AnnotationManager.getInstance().getAnnotationIDs(sessionID, projectID, referenceID);
        CustomField[] customFields = ProjectManager.getInstance().getCustomVariantFields(sessionID, projectID, referenceID, ProjectManager.getInstance().getNewestUpdateID(sessionID, projectID, referenceID, false));
        annotateAndUploadTSVFiles(sessionID, updateID, projectID, referenceID, annotationIDs, customFields, tsvFiles, workingDir);
    }

    /**
     * Annotation with the given annotation / custom field sets
     */
    private static void annotateAndUploadTSVFiles(String sessionID, int updateID, int projectID, int referenceID, int[] annotationIDs, CustomField[] customFields, File[] tsvFiles, File workingDir) throws Exception {

        try {
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                    sessionID,
                    LogManagerAdapter.LogType.INFO,
                    "Annotating TSV files, working directory is " + workingDir.getAbsolutePath());
        } catch (RemoteException ex) {
        } catch (SessionExpiredException ex) {
        }

        LOG.info("Annotating TSV files, working directory is " + workingDir.getAbsolutePath());

        ProjectManager.getInstance().setCustomVariantFields(sessionID, projectID, referenceID, updateID, customFields);

        //TODO: shouldn't these (tablename and tablenamesub) functions consider the customfields?
        String tableName = ProjectManager.getInstance().createVariantTable(
                sessionID,
                projectID,
                referenceID,
                updateID,
                annotationIDs,
                true);

        String tableNameSubset = ProjectManager.getInstance().createVariantTable(
                sessionID,
                projectID,
                referenceID,
                updateID,
                annotationIDs,
                false,
                true);

        try {
            //get annotation information
            Annotation[] annotations = getAnnotationsFromIDs(annotationIDs, sessionID);

            File[] splitTSVFiles = splitFilesByDNAAndFileID(tsvFiles, createSubdir(workingDir, "split"));
            File[] annotatedTSVFiles = annotateTSVFiles(sessionID, splitTSVFiles, annotations, customFields, createSubdir(workingDir, "annotate"));
            uploadTSVFiles(sessionID, annotatedTSVFiles, tableName, tableNameSubset, createSubdir(workingDir, "subset"));
            registerTable(sessionID, projectID, referenceID, updateID, tableName, tableNameSubset, annotationIDs);
            dropTablesPriorToUpdateID(sessionID, projectID, referenceID, updateID);
            setAnnotationStatus(sessionID, updateID, AnnotationLog.Status.PENDING);

        } catch (Exception e) {
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessionID, updateID, AnnotationLog.Status.ERROR);
            throw e;
        }
    }

    /**
     * PUBLICATION AND STATUS
     */
    private static void publishLatestUpdate(String sessionID, int projectID) throws RemoteException, Exception {
        VariantManager.getInstance().publishVariants(sessionID, projectID);
    }

    private static void setAnnotationStatus(String sessionID, int updateID, AnnotationLog.Status status) throws SQLException, SessionExpiredException {
        AnnotationLogManager.getInstance().setAnnotationLogStatus(sessionID, updateID, status);
    }

    /**
     * TABLE MANAGEMENT
     */
    private static void registerTable(String sessionID, int projectID, int referenceID, int updateID, String tableName, String tableNameSub, int[] annotationIDs) throws RemoteException, SQLException, SessionExpiredException {
        //add entries to tablemap
        ProjectManager.getInstance().addTableToMap(sessionID, projectID, referenceID, updateID, false, tableName, annotationIDs, tableNameSub);

    }

    private static void dropTablesPriorToUpdateID(String sessionID, int projectID, int referenceID, int updateID) throws RemoteException, SQLException, SessionExpiredException {
        int minId = -1;
        int maxId = updateID - 1;
        ProjectManager.getInstance().removeTables(sessionID, projectID, referenceID, minId, maxId);

    }

    /**
     * PARSING
     */
    public static File[] doConvertVCFToTSV(File[] vcfFiles, int updateID, boolean includeHomozygousReferenceCalls, File outDir) throws Exception {

        LOG.info("Converting VCF files to TSV, working directory is " + outDir.getAbsolutePath());

        // parse each vcf file in a separate thread with a separate file ID
        List<VariantParser> threads = new ArrayList<VariantParser>(vcfFiles.length);
        String stamp = System.nanoTime() + "";
        int fileID = 0;
        for (File vcfFile : vcfFiles) {
            File outFile = new File(outDir, "tmp_" + stamp + "_" + fileID + ".tdf");            
            threads.add(new VariantParser(vcfFile, outFile, updateID, fileID, includeHomozygousReferenceCalls));

            //threads[fileID] = t;
            fileID++;
            LOG.info("Queueing thread to parse " + vcfFile.getAbsolutePath());
        }
        MedSavantServerEngine.getLongExecutorService().invokeAll(threads);

        //VariantManagerUtils.processThreadsWithLimit(threads);
        // tab separated files
        File[] tsvFiles = new File[threads.size()];

        LOG.info("All parsing annotation threads done");

        int i = 0;
        for (VariantParser t : threads) {
            tsvFiles[i++] = new File(t.getOutputFilePath());
            if (!t.didSucceed()) {
                LOG.info("At least one parser thread errored out");
                throw t.getException();
            }
        }

        return tsvFiles;
    }

    private static File doDumpTableAsTSV(String sessionID, String existingVariantTableName, File workingDir, boolean complete) throws SQLException, IOException, InterruptedException, SessionExpiredException {
        LOG.info("Dumping existing table to file, working directory is " + workingDir.getAbsolutePath());

        File outfile = new File(workingDir, existingVariantTableName + ".dump");
        VariantManagerUtils.variantTableToTSVFile(sessionID, existingVariantTableName, outfile, null, complete, 0);
        return outfile;
    }

    private static File createSubdir(File parent, String child) throws IOException, InterruptedException {

        File dir = new File(parent, child);
        dir.mkdirs();

        // TODO: is this necessary?
        Process p = Runtime.getRuntime().exec("chmod -R a+wx " + dir);
        p.waitFor();

        return dir;
    }

    private static File[] splitFilesByDNAAndFileID(File[] tsvFiles, File workingDir) throws FileNotFoundException, IOException {

        LOG.info("Splitting " + tsvFiles.length + " files by DNA and FileID, working directory is " + workingDir.getAbsolutePath());

        File[] splitTSVFiles = new File[0];
        //TODO: thread each of these
        for (File file : tsvFiles) {
            File[] someSplitFiles = VariantManagerUtils.splitTSVFileByFileAndDNAID(workingDir, file);
            splitTSVFiles = ArrayUtils.addAll(splitTSVFiles, someSplitFiles);
        }

        return splitTSVFiles;
    }

    private static Annotation[] getAnnotationsFromIDs(int[] annotIDs, String sessID) throws RemoteException, SQLException, SessionExpiredException {
        int numAnnotations = annotIDs.length;
        Annotation[] annotations = new Annotation[numAnnotations];
        for (int i = 0; i < numAnnotations; i++) {
            annotations[i] = AnnotationManager.getInstance().getAnnotation(sessID, annotIDs[i]);
            LOG.info("\t" + (i + 1) + ". " + annotations[i].getProgram() + " " + annotations[i].getReferenceName() + " " + annotations[i].getVersion());
        }
        return annotations;
    }

    private static File[] annotateTSVFiles(String sessionID, File[] tsvFiles, Annotation[] annotations, CustomField[] customFields, File createSubdir) throws Exception {
        return VariantManagerUtils.annotateTSVFiles(sessionID, tsvFiles, annotations, customFields);
    }

    private static void uploadTSVFiles(String sessionID, File[] annotatedTSVFiles, String tableName, String tableNameSub, File workingDir) throws SQLException, IOException, SessionExpiredException {

        try {
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                    sessionID,
                    LogManagerAdapter.LogType.INFO,
                    "Uploading " + annotatedTSVFiles.length + " TSV files");
        } catch (RemoteException ex) {
        } catch (SessionExpiredException ex) {
        }

        // upload all the TSV files to the table
        for (File f : annotatedTSVFiles) {
            LOG.info("Uploading " + f.getAbsolutePath() + "...");
            VariantManagerUtils.uploadTSVFileToVariantTable(sessionID, f, tableName);
        }

        //create sub table dump from the complete table
        LOG.info("Dumping variants to file for sub table");
        File subDump = new File(workingDir, tableName + "sub.tsv");
        VariantManagerUtils.variantTableToTSVFile(sessionID, tableName, subDump, null, true, VariantManagerUtils.determineStepForSubset(VariantManager.getInstance().getNumFilteredVariantsHelper(sessionID, tableName, new Condition[][]{})));

        //upload to sub table
        LOG.info("Loading into subset table: " + tableNameSub);
        VariantManagerUtils.uploadTSVFileToVariantTable(sessionID, subDump, tableNameSub);
    }

    private static void addVariantFilesToDatabase(String sessionID, int updateID, int projectID, int referenceID, File[] vcfFiles) throws SQLException, SessionExpiredException {
        for (int i = 0; i < vcfFiles.length; i++) {
            VariantManager.addEntryToFileTable(sessionID, updateID, i, projectID, referenceID, vcfFiles[i].getAbsolutePath());
        }
    }

    private static void createPatientsForUpdate(String sessionID, int updateID, int projectID, int referenceID) throws RemoteException, SQLException, SessionExpiredException {

        // get the table schema for the update
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessionID,
                ProjectManager.getInstance().getVariantTableName(sessionID, updateID, projectID, referenceID)
        );

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.setIsDistinct(true);
        query.addColumns(table.getDBColumn(BasicVariantColumns.DNA_ID.getColumnName()));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(BasicVariantColumns.UPLOAD_ID.getColumnName()), updateID));

        List<String> dnaIDs = new ArrayList<String>();

        LOG.info("Creating patient for update " + query.toString());
        
        ResultSet rs = ConnectionController.executeQuery(sessionID, query.toString());
                
        while (rs.next()) {
            String dnaID = rs.getString(1);
            dnaIDs.add(dnaID);
        }

        rs.close();
        
        // get a map of DNA ids to Hospital IDs
        Map<String, String> dnaIDToHospitalIDMap = PatientManager.getInstance().getValuesFromDNAIDs(sessionID, projectID, BasicPatientColumns.HOSPITAL_ID.getColumnName(), dnaIDs);

        LOG.info("Getting orphaned DNA IDs");
        for (String dnaID : dnaIDs) {
            if (dnaIDToHospitalIDMap.containsKey(dnaID)) {
                LOG.info("Already a patient with DNA ID: " + dnaID);
            } else {
                LOG.info("No patient with DNA ID " + dnaID + ", creating one");
                
                // create a patient with Hospital ID equal to the DNA ID
                List<CustomField> patientFields = new ArrayList<CustomField>();
                List<String> fieldValues = new ArrayList<String>();
                patientFields.add(PatientManager.HOSPITAL_ID);
                fieldValues.add(dnaID);
                patientFields.add(PatientManager.DNA_IDS);
                fieldValues.add(dnaID);
                PatientManager.getInstance().addPatient(sessionID, projectID, patientFields, fieldValues);
            }
        }
    }

}
