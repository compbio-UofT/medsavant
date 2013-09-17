package org.ut.biolab.medsavant.server.db.variants;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.util.PersistenceUtil;
import org.ut.biolab.medsavant.server.serverapi.AnnotationLogManager;
import org.ut.biolab.medsavant.server.serverapi.AnnotationManager;
import org.ut.biolab.medsavant.server.serverapi.ProjectManager;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;

/**
 *
 * @author mfiume
 */
public class ImportUpdateManager {

    private static final Log LOG = LogFactory.getLog(ImportUpdateManager.class);

    /**
     * IMPORT FILES INTO AN EXISTING TABLE
     */
    public static int doImport(String sessionID, int projectID, int referenceID, boolean publishUponCompletion, File[] vcfFiles, boolean includeHomozygousReferenceCalls, String[][] tags, boolean mergeWithExistingVariants) throws IOException, SQLException, Exception {
        //FIXME This is the separation point
        boolean needsToUnlock = acquireLock(sessionID);

        try {
            LOG.info("Starting import");
            String existingVariantTableName = ProjectManager.getInstance().getVariantTableName(sessionID, projectID, referenceID, true);
            int updateID = AnnotationLogManager.getInstance().addAnnotationLogEntry(sessionID, projectID, referenceID, AnnotationLog.Action.ADD_VARIANTS);
            File workingDirectory = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
            LOG.info("Working directory is " + workingDirectory.getAbsolutePath());

            // prepare for annotation
            File[] allTSVFiles;
            File[] importedTSVFiles = doConvertVCFToTSV(vcfFiles, updateID, includeHomozygousReferenceCalls, createSubdir(workingDirectory, "converted"));

            if (mergeWithExistingVariants) {
                File existingTableAsTSV = doDumpTableAsTSV(sessionID, existingVariantTableName, createSubdir(workingDirectory, "dump"), false);
                allTSVFiles = ArrayUtils.addAll(importedTSVFiles, existingTableAsTSV);
            } else {
                allTSVFiles = importedTSVFiles;
            }

            // annotate
            annotateAndUploadTSVFiles(sessionID, updateID, projectID, referenceID, allTSVFiles, createSubdir(workingDirectory, "annotate_upload"));

            // do some accounting
            addVariantFilesToDatabase(sessionID, updateID, vcfFiles);
            VariantManagerUtils.addTagsToUpload(sessionID, updateID, tags);

            releaseLock(sessionID, needsToUnlock);
            if (publishUponCompletion) {
                publishLatestUpdate(sessionID, projectID);
            }

            /*if (VariantManager.REMOVE_WORKING_DIR) {
                MiscUtils.deleteDirectory(workingDirectory);
            }*/

            LOG.info("Finished import");

            return updateID;
        } catch (Exception e) {
            throw e;
        } finally {
            releaseLock(sessionID, needsToUnlock);
        }
    }
     /**
     * IMPORT FILES INTO AN EXISTING TABLE
     */
    public static int doImport(String sessionID, int projectID, int referenceID, boolean publishUponCompletion, File[] vcfFiles, boolean includeHomozygousReferenceCalls, String[][] tags) throws IOException, SQLException, Exception {
        return PersistenceUtil.doImport(sessionID, projectID, referenceID, publishUponCompletion, vcfFiles, includeHomozygousReferenceCalls, tags);
    }

    /**
     * UPDATE AN EXISTING TABLE
     */
    public static int doUpdate(String sessionID, int projectID, int referenceID, int[] annotationIDs, CustomField[] customFields, boolean publishUponCompletion) throws Exception {

        boolean needsToUnlock = acquireLock(sessionID);

        try {

            String existingVariantTableName = ProjectManager.getInstance().getVariantTableName(sessionID, projectID, referenceID, true);
            int updateID = AnnotationLogManager.getInstance().addAnnotationLogEntry(sessionID, projectID, referenceID, AnnotationLog.Action.UPDATE_TABLE);
            File workingDirectory = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
            File existingTableAsTSV = doDumpTableAsTSV(sessionID, existingVariantTableName, createSubdir(workingDirectory, "dump"), false);
            annotateAndUploadTSVFiles(sessionID, updateID, projectID, referenceID, annotationIDs, customFields, new File[]{existingTableAsTSV}, createSubdir(workingDirectory, "annotate_upload"));
            releaseLock(sessionID, needsToUnlock);
            if (publishUponCompletion) {
                publishLatestUpdate(sessionID, projectID);
            }
            if (VariantManager.REMOVE_WORKING_DIR) {
                MiscUtils.deleteDirectory(workingDirectory);
            }

            return updateID;

        } catch (Exception e) {
            throw e;
        } finally {
            releaseLock(sessionID, needsToUnlock);
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
        PersistenceUtil.annotateAndUploadTSVFiles(sessionID, updateID, projectID, referenceID, annotationIDs, customFields, tsvFiles, workingDir);
    }

    /**
     * PUBLICATION AND STATUS
     */

    private static void publishLatestUpdate(String sessionID, int projectID) throws RemoteException, Exception {
        VariantManager.getInstance().publishVariants(sessionID, projectID);
    }

    public static void setAnnotationStatus(String sessionID, int updateID, AnnotationLog.Status status) throws SQLException, SessionExpiredException {
        AnnotationLogManager.getInstance().setAnnotationLogStatus(sessionID, updateID, status);
    }


    /**
     * TABLE MANAGEMENT
     */

    //FIXME move logic to SQL implementation backend
    public static void registerTable(String sessionID, int projectID, int referenceID, int updateID, String tableName, String tableNameSub, int[] annotationIDs) throws RemoteException, SQLException, SessionExpiredException {
         //add entries to tablemap
        ProjectManager.getInstance().addTableToMap(sessionID, projectID, referenceID, updateID, false, tableName, annotationIDs, tableNameSub);

    }

    //FIXME move logic to SQL implementation backend
    public static void dropTablesPriorToUpdateID(String sessionID, int projectID, int referenceID, int updateID) throws RemoteException, SQLException, SessionExpiredException {
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
        VariantParser[] threads = new VariantParser[vcfFiles.length];
        String stamp = System.nanoTime() + "";
        int fileID = 0;
        for (File vcfFile : vcfFiles) {
            File outFile = new File(outDir, "tmp_" + stamp + "_" + fileID + ".tdf");
            VariantParser t = new VariantParser(vcfFile, outFile, updateID, fileID, includeHomozygousReferenceCalls);
            threads[fileID] = t;
            fileID++;
            LOG.info("Queueing thread to parse " + vcfFile.getAbsolutePath());
        }

        VariantManagerUtils.processThreadsWithLimit(threads);

        // tab separated files
        File[] tsvFiles = new File[threads.length];

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

    /**
     * LOCK MANAGEMENT
     */
    public static void releaseLock(String sessionID, boolean needsToUnlock) {
        LOG.info("Releasing session lock");
        if (needsToUnlock) {
            ConnectionController.unregisterBackgroundUsageOfSession(sessionID);
        }
    }

    public static boolean acquireLock(String sessionID) {
        LOG.info("Acquiring session lock");
        return ConnectionController.registerBackgroundUsageOfSession(sessionID);
    }

    private static File doDumpTableAsTSV(String sessionID, String existingVariantTableName, File workingDir, boolean complete) throws SQLException, IOException, InterruptedException, SessionExpiredException {
        LOG.info("Dumping existing table to file, working directory is " + workingDir.getAbsolutePath());

        File outfile = new File(workingDir, existingVariantTableName + ".dump");
        VariantManagerUtils.variantTableToTSVFile(sessionID, existingVariantTableName, outfile, null, complete, 0);
        return outfile;
    }

    public static File createSubdir(File parent, String child) throws IOException, InterruptedException {

        File dir = new File(parent, child);
        dir.mkdirs();

        // TODO: is this necessary?
        Process p = Runtime.getRuntime().exec("chmod -R a+wx " + dir);
        p.waitFor();

        return dir;
    }

    public static File[] splitFilesByDNAAndFileID(File[] tsvFiles, File workingDir) throws FileNotFoundException, IOException {

        LOG.info("Splitting " + tsvFiles.length + " files by DNA and FileID, working directory is " + workingDir.getAbsolutePath());

        File[] splitTSVFiles = new File[0];
        //TODO: thread each of these
        for (File file : tsvFiles) {
            File[] someSplitFiles = VariantManagerUtils.splitTSVFileByFileAndDNAID(workingDir, file);
            splitTSVFiles = ArrayUtils.addAll(splitTSVFiles, someSplitFiles);
        }


        return splitTSVFiles;
    }

    public static Annotation[] getAnnotationsFromIDs(int[] annotIDs, String sessID) throws RemoteException, SQLException, SessionExpiredException {
        int numAnnotations = annotIDs.length;
        Annotation[] annotations = new Annotation[numAnnotations];
        for (int i = 0; i < numAnnotations; i++) {
            annotations[i] = AnnotationManager.getInstance().getAnnotation(sessID, annotIDs[i]);
            LOG.info("\t" + (i + 1) + ". " + annotations[i].getProgram() + " " + annotations[i].getReferenceName() + " " + annotations[i].getVersion());
        }
        return annotations;
    }

    public static File[] annotateTSVFiles(String sessionID, File[] tsvFiles, Annotation[] annotations, CustomField[] customFields, File createSubdir) throws Exception {
        return VariantManagerUtils.annotateTSVFiles(sessionID, tsvFiles, annotations, customFields);
    }

    public static void uploadTSVFiles(String sessionID, File[] annotatedTSVFiles, String tableName, String tableNameSub, File workingDir) throws SQLException, IOException, SessionExpiredException {
        // upload all the TSV files to the table
        for (File f : annotatedTSVFiles) {
            LOG.info("Uploading " + f.getAbsolutePath() + "...");
            VariantManagerUtils.uploadTSVFileToVariantTable(sessionID, f, tableName);
        }
    }

    private static void addVariantFilesToDatabase(String sessionID, int updateID, File[] vcfFiles) throws SQLException, SessionExpiredException {
        for (int i = 0; i < vcfFiles.length; i++) {
            VariantManager.addEntryToFileTable(sessionID, updateID, i, vcfFiles[i].getAbsolutePath());
        }
    }



}
