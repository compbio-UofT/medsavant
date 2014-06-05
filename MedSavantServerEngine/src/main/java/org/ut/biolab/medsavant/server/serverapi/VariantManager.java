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
package org.ut.biolab.medsavant.server.serverapi;

import org.ut.biolab.medsavant.shared.model.ScatterChartEntry;
import org.ut.biolab.medsavant.shared.model.SimplePatient;
import org.ut.biolab.medsavant.shared.model.VariantComment;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.ScatterChartMap;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import jannovar.exception.JannovarException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;
import org.ut.biolab.medsavant.server.MedSavantServerJob;

import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantFileTableSchema;
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
import org.ut.biolab.medsavant.server.db.LockController;
import static org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantFileIBTableSchema;
import org.ut.biolab.medsavant.server.db.util.DBSettings;
import org.ut.biolab.medsavant.server.db.variants.ImportUpdateManager;
import org.ut.biolab.medsavant.server.db.variants.VariantManagerUtils;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.server.ontology.OntologyManager;
import org.ut.biolab.medsavant.shared.model.UserCommentGroup;
import org.ut.biolab.medsavant.shared.model.UserComment;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.exception.LockException;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.IOUtils;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

/**
 *
 * @author Andrew
 */
public class VariantManager extends MedSavantServerUnicastRemoteObject implements VariantManagerAdapter, BasicVariantColumns {

    private static final Log LOG = LogFactory.getLog(VariantManager.class);
    //approximate maximium size of the subset table, in # of variants.    
    private static final int APPROX_MAX_SUBSET_SIZE = 1000000;
    // thresholds for querying and drawing
    private static final int COUNT_ESTIMATE_THRESHOLD = 1000;
    private static final int BIN_TOTAL_THRESHOLD = 1000000;
    private static final int PATIENT_HEATMAP_THRESHOLD = 1000;
    private static VariantManager instance;
    public static boolean REMOVE_WORKING_DIR = true;

    private VariantManager() throws RemoteException, SessionExpiredException {
    }

    public static synchronized VariantManager getInstance() throws RemoteException, SessionExpiredException {
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
    public void publishVariants(String sessID, int projectID) throws Exception, LockException {

        LockController.getInstance().requestLock(SessionManager.getInstance().getDatabaseForSession(sessID), projectID);
        final String database = SessionManager.getInstance().getDatabaseForSession(sessID);
        LOG.info("Beginning publish of all tables for project " + projectID);

        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            LOG.info("Getting map of update ids");
            int[] refIDs = ProjectManager.getInstance().getReferenceIDsForProject(sessID, projectID);
            Map<Integer, Integer> ref2Update = new HashMap<Integer, Integer>();
            for (int refID : refIDs) {
                ref2Update.put(refID, ProjectManager.getInstance().getNewestUpdateID(sessID, projectID, refID, false));
                ProjectManager.getInstance().publishVariantTable(sessID, conn, projectID, refID);
            }

            //update annotation log table
            LOG.info("Setting log status to published");
            for (Integer refId : ref2Update.keySet()) {
                AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, ref2Update.get(refId), Status.PUBLISHED);
            }

            //publish            
            LOG.info("Publishing tables");
            ProjectManager.getInstance().publishVariantTable(sessID, conn, projectID, refIDs);

            LOG.info("Terminating active sessions");
            SessionManager.getInstance().terminateSessionsForDatabase(SessionManager.getInstance().getDatabaseForSession(sessID), "Administrator (" + SessionManager.getInstance().getUserForSession(sessID) + ") published new variants");

            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(sessID, LogManagerAdapter.LogType.INFO, "Published " + ProjectManager.getInstance().getProjectName(sessID, projectID));

            LOG.info("Publish complete");
        } finally {
            conn.close();
            LockController.getInstance().releaseLock(database, projectID);
        }
    }

    /**
     * Publishes the variant table corresponding to the given project and
     * reference ID, then logs all users out.
     */
    public void publishVariants(String sessID, int projID, int refID) throws Exception, LockException {

        LockController.getInstance().requestLock(SessionManager.getInstance().getDatabaseForSession(sessID), projID);
        final String database = SessionManager.getInstance().getDatabaseForSession(sessID);
        //LOG.info("Publishing table. pid:" + projID + " refid:" + refID + " upid:" + updID);
        LOG.info("Publishing table. pid:" + projID + " refid:" + refID);
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            LOG.info("Setting log status to published");
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, refID, Status.PUBLISHED);
            LOG.info("Terminating active sessions");
            ProjectManager.getInstance().publishVariantTable(sessID, conn, projID, refID);
            LOG.info("Publish complete");
            SessionManager.getInstance().terminateSessionsForDatabase(SessionManager.getInstance().getDatabaseForSession(sessID), "Administrator (" + SessionManager.getInstance().getUserForSession(sessID) + ") published new variants");
            LOG.info("Publishing table");
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(sessID, LogManagerAdapter.LogType.INFO, "Published " + ProjectManager.getInstance().getProjectName(sessID, projID));
        } catch (Exception ex) {
            LOG.error(ex);
            ex.printStackTrace();
        } finally {
            conn.close();
            LockController.getInstance().releaseLock(database, projID);
        }
    }

    /**
     * Make a variant table live. All users accessing this database will be
     * logged out.
     *
     * @param updID - this argument is IGNORED and will be removed in the near
     * future.
     */
    @Deprecated
    @Override
    public void publishVariants(String sessID, int projID, int refID, int updID) throws Exception, LockException {
        //The updID argument is deprecated and should be removed at a later date.
        publishVariants(sessID, projID, refID);
    }

    /**
     * Cancel publishing. Note updateID is ignored and will be removed in a
     * future version.
     */
    @Override
    @Deprecated
    public void cancelPublish(String sid, int projectID, int referenceID, int updateID) throws Exception, LockException {
        final String database = SessionManager.getInstance().getDatabaseForSession(sid);
        LockController.getInstance().requestLock(SessionManager.getInstance().getDatabaseForSession(sid), projectID);

        LOG.info("Cancelling publish. pid:" + projectID + " refid:" + referenceID);

        ProjectManager.getInstance().cancelPublish(sid, projectID, referenceID);
        //ProjectManager.getInstance().removeTables(sid, projectID, referenceID, updateID, updateID);
        //this.publishVariants(sid, projectID, referenceID);
        LOG.info("Cancel complete");

        LockController.getInstance().releaseLock(database, projectID);

        org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(sid, LogManagerAdapter.LogType.INFO, "Cancelled publish of " + ProjectManager.getInstance().getProjectName(sid, projectID));
    }

    /**
     * Perform updates to custom vcf fields and other annotations. Will result
     * in the creation of a new, unpublished, up-to-date variant table. This
     * method is used only by ProjectWizard.modifyProject().
     */
    @Override
    public int updateTable(String userSessionID, int projID, int refID, int[] annotIDs, CustomField[] customFields, boolean autoPublish, String email) throws Exception, LockException {

        LockController.getInstance().requestLock(SessionManager.getInstance().getDatabaseForSession(userSessionID), projID);
        final String database = SessionManager.getInstance().getDatabaseForSession(userSessionID);
        String backgroundSessionID = SessionManager.getInstance().createBackgroundSessionFromSession(userSessionID);

        try {
            EmailLogger.logByEmail("Update started", "Update started. " + annotIDs.length + " annotation(s) will be performed. You will be notified again upon completion.", email);

            int updateID = ImportUpdateManager.doUpdate(backgroundSessionID, projID, refID, annotIDs, customFields, autoPublish);

            EmailLogger.logByEmail("Update finished", "Update completed. " + annotIDs.length + " annotation(s) were performed.", email);

            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(backgroundSessionID, LogManagerAdapter.LogType.INFO, "Done updating " + ProjectManager.getInstance().getProjectName(backgroundSessionID, projID));

            return updateID;
        } catch (Exception e) {

            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(backgroundSessionID, LogManagerAdapter.LogType.ERROR, "Update failed for " + ProjectManager.getInstance().getProjectName(backgroundSessionID, projID) + ". " + e.getLocalizedMessage());

            EmailLogger.logByEmail("Update failed", "Update failed with error: " + MiscUtils.getStackTrace(e), email);
            LOG.error(e);
            throw e;
        } finally {
            LockController.getInstance().releaseLock(database, projID);
            SessionManager.getInstance().unregisterSession(backgroundSessionID);
        }

    }

    /**
     * Adds variants that have been transferred to the server. Variants are
     * appended to the existing table. The variant view will only be updated to
     * include these new variants upon publication, or if autopublish is true.
     */
    @Override
    public int uploadTransferredVariants(String userSessionID, int[] transferIDs, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish, boolean preAnnotateWithAnnovar, boolean doPhasing) throws Exception, LockException {
        return uploadVariants(userSessionID, transferIDs, projID, refID, tags, includeHomoRef, email, autoPublish, preAnnotateWithAnnovar, doPhasing);
    }

    /**
     * Import variant files which have been transferred from a client. Variants
     * are appended to the existing table. The variant view will only be updated
     * to include these new variants upon publication, or if autopublish is
     * true.
     */
    @Override
    public int uploadVariants(String userSessionID, int[] transferIDs, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish, boolean preAnnotateWithAnnovar, boolean doPhasing) throws Exception, LockException {
        if (ProjectManager.getInstance().hasUnpublishedChanges(userSessionID, projID, refID)) {
            throw new IllegalArgumentException("Can't import variants for this project and reference until unpublished changes are published.");
        }
        LockController.getInstance().requestLock(SessionManager.getInstance().getDatabaseForSession(userSessionID), projID);
        final String database = SessionManager.getInstance().getDatabaseForSession(userSessionID);
        String backgroundSessionID = SessionManager.getInstance().createBackgroundSessionFromSession(userSessionID);

        try {
            LOG.info("Importing variants by transferring from client");
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(backgroundSessionID, LogManagerAdapter.LogType.INFO, "Started upload of variants for " + ProjectManager.getInstance().getProjectName(backgroundSessionID, projID));

            NetworkManager netMgr = NetworkManager.getInstance();
            File[] vcfFiles = new File[transferIDs.length];
            String[] sourceNames = new String[transferIDs.length];

            int i = 0;
            for (int id : transferIDs) {
                vcfFiles[i] = netMgr.getFileByTransferID(userSessionID, id);
                sourceNames[i] = netMgr.getSourceNameByTransferID(userSessionID, id);
                i++;
            }

            return uploadVariants(backgroundSessionID, vcfFiles, sourceNames, projID, refID, tags, includeHomoRef, email, autoPublish, preAnnotateWithAnnovar, doPhasing);
        } finally {
            LockController.getInstance().releaseLock(database, projID);
            SessionManager.getInstance().unregisterSession(backgroundSessionID);
        }
    }

    /**
     * Use when variant files are already on the server. Performs variant import
     * of an entire directory. Variants are appended to the existing table. The
     * variant view will only be updated to include these new variants upon
     * publication, or if autopublish is true.
     */
    @Override
    public int uploadVariants(String userSessionID, File dirContainingVCFs, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish, boolean preAnnotateWithAnnovar, boolean doPhasing) throws RemoteException, SessionExpiredException, IOException, Exception, LockException {
        if (ProjectManager.getInstance().hasUnpublishedChanges(userSessionID, projID, refID)) {
            throw new IllegalArgumentException("Can't import variants for this project and reference until unpublished changes are published.");
        }
        LockController.getInstance().requestLock(SessionManager.getInstance().getDatabaseForSession(userSessionID), projID);
        final String database = SessionManager.getInstance().getDatabaseForSession(userSessionID);
        String backgroundSessionID = SessionManager.getInstance().createBackgroundSessionFromSession(userSessionID);

        try {
            LOG.info("Importing variants already stored on server in dir " + dirContainingVCFs.getAbsolutePath());
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(backgroundSessionID, LogManagerAdapter.LogType.INFO, "Started upload of variants for " + ProjectManager.getInstance().getProjectName(backgroundSessionID, projID));

            if (!dirContainingVCFs.exists()) {
                LOG.info("Directory from which to load variants does not exist, bailing out.");
                return -1;
            }

            File[] vcfFiles = dirContainingVCFs.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    String name = file.getName();
                    return name.endsWith(".vcf") || name.endsWith(".vcf.gz") || name.endsWith(".vcf.bz2") || name.endsWith(".tgz");
                }
            });

            if (vcfFiles.length == 0) {
                LOG.info("Directory exists but contains no .vcf or .vcf.gz files.");
                throw new IllegalArgumentException("Directory on server exists, but does not contain any recognized file types");
                //return -1;
            }

            return uploadVariants(backgroundSessionID, vcfFiles, null, projID, refID, tags, includeHomoRef, email, autoPublish, preAnnotateWithAnnovar, doPhasing);
        } finally {
            LockController.getInstance().releaseLock(database, projID);
            SessionManager.getInstance().unregisterSession(backgroundSessionID);
        }
    }

    private boolean isVCF41File(File f) {
        //For now, check is simple.
        return f.getName().toLowerCase().endsWith(".vcf");
    }

    public static File getVCFDestinationDir(String database, int projectID) {
        //Get destination file
        File gd = DirectorySettings.getGenoTypeDirectory(database, projectID);
        return gd;
    }

    public static File getVCFDestination(final File inputFile, String database, int projectID) {
        File gd = getVCFDestinationDir(database, projectID);
        File dest = new File(gd, inputFile.getName());

        int prefix = 0;
        while (dest.exists()) {
            dest = new File(gd, prefix + "_" + inputFile.getName());
            prefix++;
        }

        return dest;
    }

    /**
     * Start the upload process for new vcf files. Will result in the creation
     * of a new, unpublished, up-to-date variant table.
     *
     * @param sessID uniquely identifies the client
     * @param vcfFiles local VCF files on the server's file-system
     * @param sourceNames if non-null, client-side names of uploaded files
     */
    private int uploadVariants(String userSessionID, File[] inputFiles, String[] sourceNames, int projectID, int referenceID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish, boolean preAnnotateWithJannovar, boolean doPhasing) throws Exception, LockException {

        LockController.getInstance().requestLock(SessionManager.getInstance().getDatabaseForSession(userSessionID), projectID);
        final String database = SessionManager.getInstance().getDatabaseForSession(userSessionID);
        String backgroundSessionID = SessionManager.getInstance().createBackgroundSessionFromSession(userSessionID);

        try {
            List<File> vcfFileList = new ArrayList(inputFiles.length);

            for (File inputFile : inputFiles) {
                //If input file is a vcf file, MOVE it to the destination unless it is outside the
                //tmp directory, then just keep it where it is.
                if (isVCF41File(inputFile)) {
                    if (IOUtils.isInDirectory(inputFile, DirectorySettings.getTmpDirectory())) {
                        File dest = getVCFDestination(inputFile, database, projectID);
                        if (!IOUtils.moveFile(inputFile, dest)) {
                            throw new IOException("Cannot move VCF file to genotype storage.");
                        }
                        LOG.info("Moved input VCF " + inputFile + " to " + dest);
                        inputFile = dest;
                    } else {
                        LOG.info("VCF file " + inputFile + " was not found in server tmp directory -- keeping it where it is.");
                    }
                    vcfFileList.add(inputFile);
                    continue;
                }

                //Otherwise if it's a zip file, extract it to a temporary '_extracted_' folder.
                int i = 0;
                File outputDir = new File(inputFile.getParent() + File.separator + inputFile.getName() + "_extracted_" + i);
                while (outputDir.exists()) {
                    //Note the outputDir should not normally exist, because the filename should be unique.  
                    //(this would have been enforced by NetworkManager.openWriterOnServer)                     
                    LOG.error("Unzip destination directory " + outputDir + " already exists, trying new directory name...");
                    i++;
                    outputDir = new File(inputFile.getParent() + File.separator + inputFile.getName() + "_extracted_" + i);
                }

                if (!outputDir.mkdirs()) {
                    throw new IOException("Cannot make target directory " + outputDir);
                }
                List<File> files = IOUtils.decompressAndDelete(inputFile, outputDir);
                for (File putativeVCF : files) {
                    if (isVCF41File(putativeVCF)) {
                        File dest = getVCFDestination(putativeVCF, database, projectID);
                        if (!IOUtils.moveFile(putativeVCF, dest)) {
                            throw new IOException("Cannot move VCF file in compressed file to genotype storage.");
                        }
                        LOG.info("Moved input VCF " + putativeVCF + " contained in compressed file to " + dest);
                        putativeVCF = dest;
                        vcfFileList.add(putativeVCF);
                    } else {
                        LOG.error("Unrecognized file " + putativeVCF + " -- skipping");
                        putativeVCF.delete();
                    }
                }
                outputDir.delete();
            }

            File[] vcfFiles = vcfFileList.toArray(new File[vcfFileList.size()]);

            EmailLogger.logByEmail("Upload started", "Upload started. " + vcfFiles.length + " file(s) will be imported. You will be notified again upon completion.", email);
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(userSessionID, LogManagerAdapter.LogType.INFO, "Upload started. " + vcfFiles.length + " file(s) will be imported. You will be notified again upon completion.");
            int updateID = -1;
            try {
                updateID = ImportUpdateManager.doImport(backgroundSessionID, projectID, referenceID, vcfFiles, includeHomoRef, preAnnotateWithJannovar, doPhasing, tags);
                // addVariantFilesToDatabase(userSessionID, updateID, projectID, referenceID, vcfFiles);
                EmailLogger.logByEmail("Upload finished", "Upload completed. " + vcfFiles.length + " file(s) were imported.", email);
                org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(backgroundSessionID, LogManagerAdapter.LogType.INFO, "Done uploading variants for " + ProjectManager.getInstance().getProjectName(backgroundSessionID, projectID));
            } finally {

            }
            //clean up.
            //never delete vcf files
            /*
             for (File f : vcfFiles) {
             f = new File(f.getAbsolutePath());
             File d = new File(f.getParent());
             if (f.exists()) {
             f.delete();
             }
             if (d.exists() && d.isDirectory() && d.getName().contains("_extracted_")) {
             d.delete();
             }
             }*/
            if (autoPublish) {
                LOG.info("Publishing");
                VariantManager.getInstance().publishVariants(backgroundSessionID, projectID);
            } else {
                LOG.info("Not publishing");
            }
            return updateID;
        } catch (JannovarException je) {
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(backgroundSessionID, LogManagerAdapter.LogType.ERROR, "Error uploading variants for " + ProjectManager.getInstance().getProjectName(backgroundSessionID, projectID) + ". " + je.getLocalizedMessage());
            EmailLogger.logByEmail("Upload failed", "Upload failed with error: " + MiscUtils.getStackTrace(je), email);
            LOG.error(je);
            throw new IllegalArgumentException("Could not annotate variant file: " + je.getLocalizedMessage());
        } catch (Exception e) {
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(backgroundSessionID, LogManagerAdapter.LogType.ERROR, "Error uploading variants for " + ProjectManager.getInstance().getProjectName(backgroundSessionID, projectID) + ". " + e.getLocalizedMessage());
            EmailLogger.logByEmail("Upload failed", "Upload failed with error: " + MiscUtils.getStackTrace(e), email);
            LOG.error("Could not annotate variant file: ", e);
            throw e;
        } finally {
            LockController.getInstance().releaseLock(database, projectID);
            SessionManager.getInstance().unregisterSession(backgroundSessionID);
        }
    }

    /**
     * Returns the size of the subset table as a fraction of the main variant
     * view, (@see DBSettings.getVariantViewName), where the maintable contains
     * 'nv' rows.
     *
     * @param nv The number of variants in the main variant view.
     */
    public static double getSubsetFraction(int nv) {
        if (nv > APPROX_MAX_SUBSET_SIZE) {
            double fractionOfOriginalTable = APPROX_MAX_SUBSET_SIZE / (double) nv;
            return fractionOfOriginalTable;
        } else {
            return 1;
        }
    }

    /**
     *
     * @param nv The number of variants in the main variant view.
     * @return a query condition to randomly sample approximately
     * nv*getSubsetFraction(nv) variants from the main variant view (@see
     * DBSettings.getVariantViewName).
     */
    public static Condition getSubsetRestrictionCondition(int nv) {
        LOG.info("getting subset restriction condition for " + nv + " variants");
        return BinaryCondition.lessThan(new FunctionCall(new CustomSql("RAND")), getSubsetFraction(nv), true);
    }

    /**
     * Removes variants associated with the given list of files. These files are
     * deleted from the file (MyISAM) table. Upon publication, this table is
     * copied over the file "join table" (Infobright). If publication is
     * cancelled, the file "join table" is used to restore the MyISAM table to
     * it's pre-removal state.
     *
     * File Table: @see MedSavantDatabase.VariantFileTableSchema File "join
     * table": @see MedSavantDatabase.VariantFileIBTableSchema
     *
     * @see ProjectController.publishVariants
     * @throws Exception
     * @throws LockException
     */
    @Override
    public int removeVariants(final String userSessionID, final int projID, final int refID, final List<SimpleVariantFile> files, final boolean autoPublish, final String email) throws Exception, LockException {
        LOG.info("Beginning removal of variants");
        if (ProjectManager.getInstance().hasUnpublishedChanges(userSessionID, projID, refID)) {
            throw new IllegalArgumentException("Can't remove variants for this project and reference until unpublished changes are published.");
        }

        final int[] returnVal = new int[]{1};
        MedSavantServerJob msj
                = new MedSavantServerJob(SessionManager.getInstance().getUserForSession(userSessionID),
                        "Removing variants", null) {
                    @Override
                    public boolean run() throws Exception {
                        final String database = SessionManager.getInstance().getDatabaseForSession(userSessionID);
                        LockController.getInstance().requestLock(database, projID);

                        org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(userSessionID, LogManagerAdapter.LogType.INFO, "Removing " + files.size() + " files");

                        int oldUpdateId = ProjectManager.getInstance().getNewestUpdateID(userSessionID, projID, refID, true);
                        int updateId = AnnotationLogManager.getInstance().addAnnotationLogEntry(userSessionID, projID, refID, org.ut.biolab.medsavant.shared.model.AnnotationLog.Action.REMOVE_VARIANTS);

                        try {

                            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(userSessionID, LogManagerAdapter.LogType.INFO, "Removing variants from " + ProjectManager.getInstance().getProjectName(userSessionID, projID));

                            //guarantee the file table reflects the published files.
                            ProjectManager.getInstance().restorePublishedFileTable(userSessionID);
                            removeEntriesFromFileTable(userSessionID, files);

                            //Calculate the total number of variants that will remain in the main variant view after deletion and publication.
                            String viewName = DBSettings.getVariantViewName(projID, refID);
                            int numVariantsInFiles = countPublishedVariantsInFiles(userSessionID, projID, refID, files);
                            int numTotalVariants = getNumFilteredVariantsHelper(userSessionID, viewName, new Condition[0][]);
                            int numVariantsAfterDeletion = numTotalVariants - numVariantsInFiles;
                            //LOG.info("Number variants before deletion: " + numTotalVariants);
                            //LOG.info("Number varaints after deletion: " + numVariantsAfterDeletion);

                            //Create a new subset table, using the configuration of the last published table.                      
                            int[] annIDs = AnnotationManager.getInstance().getAnnotationIDs(userSessionID, projID, refID);
                            CustomField[] customFields = ProjectManager.getInstance().getCustomVariantFields(userSessionID, projID, refID, oldUpdateId);
                            String newSubsetTableName = ProjectManager.getInstance().addVariantTableToDatabase(userSessionID, projID, refID, updateId, annIDs, customFields, true);
                            TableSchema viewSchema = CustomTables.getInstance().getCustomTableSchema(userSessionID, viewName);
                            Condition[] conditions = new Condition[files.size() + 1];
                            int i = 0;
                            for (SimpleVariantFile svf : files) {
                                conditions[i++] = BinaryCondition.notEqualTo(viewSchema.getDBColumn(BasicVariantColumns.FILE_ID), svf.getFileId());
                            }

                            //Fill the subset table by sampling from the (main view excluding rows with file_ids that are to be deleted).
                            conditions[i] = getSubsetRestrictionCondition(numVariantsAfterDeletion);

                            LOG.info("Creating new subset table called " + newSubsetTableName);
                            getJobProgress().setMessage("Creating new subset table...");
                            SelectQuery sq = new SelectQuery();
                            sq.addAllColumns();
                            sq.addFromTable(viewSchema.getTable());
                            sq.addCondition(ComboCondition.and(conditions));

                            DBUtils.copyQueryResultToNewTable(userSessionID, sq, newSubsetTableName);

                            //set as pending
                            AnnotationLogManager.getInstance().setAnnotationLogStatus(userSessionID, updateId, Status.PENDING);

                            //Reconfigure the variant tablemap (MedSavantDatabase.VarianttablemapTableSchema)
                            ProjectManager.getInstance().setupTablesForVariantRemoval(userSessionID, projID, refID, updateId, newSubsetTableName);

                            //Set the custom fields for this update to be those of the previously published update.
                            CustomField[] cf = ProjectManager.getInstance().getCustomVariantFields(userSessionID, projID, refID, oldUpdateId);
                            if (cf != null) {
                                ProjectManager.getInstance().setCustomVariantFields(userSessionID, projID, refID, updateId, cf);
                            }

                            if (autoPublish) {
                                publishVariants(userSessionID, projID);
                                SessionManager.getInstance().unregisterSession(userSessionID);
                            }

                            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(userSessionID, LogManagerAdapter.LogType.INFO, "Done removing variants from " + ProjectManager.getInstance().getProjectName(userSessionID, projID));
                            returnVal[0] = updateId;
                            //return updateId;
                            return true;
                        } catch (Exception e) {

                            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(userSessionID, LogManagerAdapter.LogType.ERROR, "Error removing variants from " + ProjectManager.getInstance().getProjectName(userSessionID, projID));

                            AnnotationLogManager.getInstance().setAnnotationLogStatus(userSessionID, updateId, Status.ERROR);
                            EmailLogger.logByEmail("Removal failed", "Removal failed with error: " + MiscUtils.getStackTrace(e), email);
                            throw e;
                        } finally {
                            //remove VCF files.
                            for (SimpleVariantFile svf : files) {
                                File f = new File(svf.getPath());
                                if (f.exists()) {
                                    if (IOUtils.isInDirectory(f, DirectorySettings.getTmpDirectory())) {
                                        File p = f.getParentFile();
                                        f.delete();
                                        IOUtils.deleteEmptyParents(p, DirectorySettings.getTmpDirectory());
                                    } else if (IOUtils.isInDirectory(f, DirectorySettings.getGenoTypeDirectory())) {
                                        File p = f.getParentFile();
                                        f.delete();
                                        IOUtils.deleteEmptyParents(p, DirectorySettings.getGenoTypeDirectory());
                                    } else {
                                        LOG.info("Not removing .vcf file " + svf.getPath() + " -- not in a MedSavant directory.");
                                    }
                                } else {
                                    LOG.info("Not removing .vcf file " + svf.getPath() + " -- does not exist on file system.");
                                }
                            }

                            LockController.getInstance().releaseLock(database, projID);
                            //SessionManager.getInstance().unregisterSesion(backgroundSessionID);
                        }
                    }//end run
                };//end MedSavantServerJob

        MedSavantServerEngine.runJobInCurrentThread(msj);
        return returnVal[0]; //delete me
    }

    //Returns the number of variants that were taken from the given list of files AND that were published (i.e. exist in the main variant view)
    private int countPublishedVariantsInFiles(String sid, int projID, int refID, Collection<SimpleVariantFile> files) throws RemoteException, SQLException, SessionExpiredException {
        String viewName = DBSettings.getVariantViewName(projID, refID);
        TableSchema vtable = CustomTables.getInstance().getCustomTableSchema(sid, viewName);

        int i = 0;
        Condition[] conditions = new Condition[files.size()];
        for (SimpleVariantFile f : files) {
            conditions[i++] = BinaryConditionMS.equalTo(vtable.getDBColumn(BasicVariantColumns.FILE_ID), f.getFileId());
        }

        SelectQuery query = new SelectQuery();
        query.addFromTable(vtable.getTable());
        query.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(query, new Condition[][]{{ComboCondition.or(conditions)}});
        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            LOG.error("Couldn't count published variants, query: " + query.toString());
            throw new SQLException("Couldn't count published variants");
        }
    }

    @Override
    public int exportVariants(String userSessionID, int projID, int refID, Condition[][] conditions, boolean orderedByPosition, boolean zipOutputFile) throws SQLException, RemoteException, SessionExpiredException, IOException, InterruptedException {

        String backgroundSessionID = SessionManager.getInstance().createBackgroundSessionFromSession(userSessionID);

        //generate directory
        File baseDir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir.getCanonicalPath());
        p.waitFor();
        String filename = ProjectManager.getInstance().getProjectName(backgroundSessionID, projID).replace(" ", "") + "-varexport-" + System.currentTimeMillis() + ".tdf";
        File file = new File(baseDir, filename);
        File zipFile = null;
        try {
            LOG.info("Exporting variants to " + file.getAbsolutePath());

            long start = System.currentTimeMillis();
            TableSchema table = CustomTables.getInstance().getCustomTableSchema(backgroundSessionID, ProjectManager.getInstance().getVariantTableName(backgroundSessionID, projID, refID, true));
            SelectQuery query = new SelectQuery();
            query.addFromTable(table.getTable());
            query.addAllColumns();
            addConditionsToQuery(query, conditions);
            if (orderedByPosition) {
                query.addOrderings(table.getDBColumn(START_POSITION), table.getDBColumn(END_POSITION));
            }
            String intoString
                    = "INTO OUTFILE \"" + file.getAbsolutePath().replaceAll("\\\\", "/") + "\" "
                    + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.FIELD_DELIMITER) + "' "
                    + "ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' "
                    + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "' " //+ " LINES TERMINATED BY '\\r\\n' ";
                    ;
            String queryString = query.toString().replace("FROM", intoString + "FROM");

            LOG.info(queryString);
            ConnectionController.executeQuery(backgroundSessionID, queryString);

            if (zipOutputFile) {
                LOG.info("Zipping export...");
                zipFile = new File(file.getAbsoluteFile() + ".zip");
                IOUtils.zipFile(file, zipFile);
            }
            LOG.info("Done exporting variants to " + file.getAbsolutePath());
            LOG.info("Export took " + ((System.currentTimeMillis() - start) / 1000) + " seconds");

        } finally {
            if (zipOutputFile) {
                boolean deleted = file.delete();
                LOG.info("Deleting " + file.getAbsolutePath() + " - " + (deleted ? "successful" : "failed"));
                file = zipFile;
                LOG.info("Done zipping");
            }
        }

        // add file to map and send the id back 
        //int fileID = NetworkManager.getInstance().openReaderOnServer(backgroundSessionID, file);
        int fileID = NetworkManager.getInstance().openReaderOnServer(userSessionID, file);
        return fileID;
    }

    @Override
    public TableSchema getCustomTableSchema(String sessionId, int projectId, int referenceId) throws SQLException, RemoteException, SessionExpiredException {
        return CustomTables.getInstance().getCustomTableSchema(sessionId, ProjectManager.getInstance().getVariantTableName(sessionId, projectId, referenceId, true));
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, int start, int limit) throws SQLException, RemoteException, SessionExpiredException {
        return getVariants(sessionId, projectId, referenceId, new Condition[1][], start, limit);
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException, SessionExpiredException {
        return getVariants(sessionId, projectId, referenceId, conditions, start, limit, null);
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions, int start, int limit, String[] orderByCols) throws SQLException, RemoteException, SessionExpiredException {
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

        LOG.info(queryString);

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
    public int getVariantCount(String sid, int projectId, int referenceId) throws SQLException, RemoteException, SessionExpiredException {
        return getFilteredVariantCount(sid, projectId, referenceId, new Condition[0][], true);
    }

    @Override
    public int getFilteredVariantCount(String sid, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException {
        return getFilteredVariantCount(sid, projectId, referenceId, conditions, false);
    }

    private int getFilteredVariantCount(String sid, int projectId, int referenceId, Condition[][] conditions, boolean forceExact) throws SQLException, RemoteException, SessionExpiredException {

        //String name = ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId, true);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableViewInfo(sid, projectId, referenceId);
        String tableViewName = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float subMultiplier = (Float) variantTableInfo[2];

        if (tableViewName == null) {
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
        return getNumFilteredVariantsHelper(sid, tableViewName, conditions);
    }

    public int getNumFilteredVariantsHelper(String sessID, String tableViewName, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException {
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tableViewName);

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
    public int getVariantCountForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException, SessionExpiredException {

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
    public boolean willApproximateCountsForConditions(String sid, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException {
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        return total >= BIN_TOTAL_THRESHOLD;
    }

    @Override
    public Map<Range, Long> getFilteredFrequencyValuesForNumericColumn(String sid, int projectId, int referenceId, Condition[][] conditions, CustomField column, boolean logBins) throws InterruptedException, SQLException, RemoteException, SessionExpiredException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableViewInfo(sid, projectId, referenceId);
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
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sessID, int projID, int refID, Condition[][] conditions, String colName) throws SQLException, RemoteException, SessionExpiredException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sessID, projID, refID, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableViewInfo(sessID, projID, refID);
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
    public ScatterChartMap getFilteredFrequencyValuesForScatter(String sid, int projectId, int referenceId, Condition[][] conditions, String columnnameX, String columnnameY, boolean columnXCategorical, boolean columnYCategorical, boolean sortKaryotypically) throws InterruptedException, SQLException, RemoteException, SessionExpiredException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableViewInfo(sid, projectId, referenceId);
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
     * Convenience method.  Returns all variants whose START position lies within the given range.
     * 
     */
    @Override
    public int getVariantCountInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, long start, long end) throws SQLException, RemoteException, SessionExpiredException {

        String name = ProjectManager.getInstance().getVariantTableName(sid, projectId, referenceId, true);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid, name);

        Condition[] rangeConditions = new Condition[]{
            BinaryCondition.equalTo(table.getDBColumn(CHROM), chrom),
            BinaryCondition.greaterThan(table.getDBColumn(START_POSITION), start, true),
            BinaryCondition.lessThan(table.getDBColumn(START_POSITION), end, false)
        };

        Condition[] c1 = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c1[i] = ComboCondition.and(conditions[i]);
        }

        Condition[] finalCondition = new Condition[]{ComboCondition.and(ComboCondition.and(rangeConditions), ComboCondition.or(c1))};

        return getFilteredVariantCount(sid, projectId, referenceId, new Condition[][]{finalCondition});
    }

    @Override
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, int binsize) throws SQLException, RemoteException, SessionExpiredException {

        //pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableViewInfo(sid, projectId, referenceId);
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

        String roundFunction = "ROUND(" + START_POSITION.getColumnName() + "/" + binsize + ",0)";

        queryBase.addCustomColumns(FunctionCall.countAll());
        queryBase.addGroupings(table.getDBColumn(CHROM));

        addConditionsToQuery(queryBase, conditions);

        String query = queryBase.toString().replace("COUNT(*)", "COUNT(*)," + roundFunction) + "," + roundFunction;

        ResultSet rs = ConnectionController.executeQuery(sid, query);

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

    /**
     *
     * Returns a count of all patients that have a variant within the given
     * range.
     */
    @Override
    public int getPatientCountWithVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int start, int end) throws SQLException, RemoteException, SessionExpiredException {

        //TODO: approximate counts??
        //might not be a good idea... don't want to miss a dna id
        TableSchema table = getCustomTableSchema(sid, projectId, referenceId);
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns("COUNT(DISTINCT " + DNA_ID.getColumnName() + ")");
        addConditionsToQuery(q, conditions);

        Condition[] cond = new Condition[3];
        //cond[0] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, table.getDBColumn(CHROM), chrom);
        //cond[1] = new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, table.getDBColumn(POSITION), start);
        //cond[2] = new BinaryCondition(BinaryCondition.Op.LESS_THAN, table.getDBColumn(POSITION), end);
        //q.addCondition(ComboCondition.and(cond));
        q.addCondition(
                ComboCondition.and(
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, table.getDBColumn(CHROM), chrom),
                        MiscUtils.getIntersectCondition(start, end, table.getDBColumn(START_POSITION), table.getDBColumn(END_POSITION))));

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
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, List<String> dnaIds, int limit) throws SQLException, RemoteException, SessionExpiredException {

        Map<String, List<String>> results = new HashMap<String, List<String>>();

        TableSchema table = getCustomTableSchema(sessID, projID, refID);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(DNA_ID), table.getDBColumn(CHROM), table.getDBColumn(START_POSITION));
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
    public Map<String, Integer> getNumVariantsInFamily(String sessID, int projID, int refID, String famID, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException {

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
    public void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException, SessionExpiredException {

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

    public void removeTags(String sessID, int uploadID) throws SQLException, SessionExpiredException {
        ConnectionController.executeUpdate(sessID, MedSavantDatabase.VariantTagTableSchema.delete(VariantTagColumns.UPLOAD_ID, uploadID).toString());
    }

    @Override
    public List<String> getDistinctTagNames(String sessID) throws SQLException, SessionExpiredException {

        ResultSet rs = ConnectionController.executeQuery(sessID, MedSavantDatabase.VariantTagTableSchema.distinct().select(VariantTagColumns.TAGKEY).toString());

        List<String> tagNames = new ArrayList<String>();
        while (rs.next()) {
            tagNames.add(rs.getString(1));
        }

        return tagNames;
    }

    @Override
    public List<String> getValuesForTagName(String sessID, String tagName) throws SQLException, SessionExpiredException {

        ResultSet rs = ConnectionController.executeQuery(sessID, MedSavantDatabase.VariantTagTableSchema.distinct().where(VariantTagColumns.TAGKEY, tagName).select(VariantTagColumns.TAGVALUE).toString());

        List<String> tagValues = new ArrayList<String>();
        while (rs.next()) {
            tagValues.add(rs.getString(1));
        }

        return tagValues;

    }

    @Override
    public List<Integer> getUploadIDsMatchingVariantTags(String sessID, String[][] variantTags) throws SQLException, SessionExpiredException {
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
    public SimpleVariantFile[] getUploadedFiles(String sessID, int projID, int refID) throws SQLException, RemoteException, SessionExpiredException {

        TableSchema fileTable = MedSavantDatabase.VariantFileIBTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(fileTable.getTable());
        query.setIsDistinct(true);
        query.addColumns(fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), fileTable.getDBColumn(VariantFileIBTableSchema.COLUMNNAME_OF_FILE_ID), fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_NAME));

        query.addCondition(BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileIBTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        query.addCondition(BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileIBTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        LOG.info("Getting variant tables: " + query.toString());

        List<SimpleVariantFile> result = new ArrayList<SimpleVariantFile>();
        while (rs.next()) {
            // upload, file, path
            result.add(new SimpleVariantFile(rs.getInt(1), rs.getInt(2), rs.getString(3)));
        }
        return result.toArray(new SimpleVariantFile[0]);
    }

    @Override
    public List<String[]> getTagsForUpload(String sessID, int uplID) throws SQLException, RemoteException, SessionExpiredException {

        ResultSet rs = ConnectionController.executeQuery(sessID, MedSavantDatabase.VariantTagTableSchema.where(VariantTagColumns.UPLOAD_ID, uplID).select(VariantTagColumns.TAGKEY, VariantTagColumns.TAGVALUE).toString());

        List<String[]> result = new ArrayList<String[]>();
        while (rs.next()) {
            result.add(new String[]{rs.getString(1), rs.getString(2)});
        }
        return result;
    }

    /*
     * @Override public Set<StarredVariant> getStarredVariants(String sid, int
     * projectId, int referenceId) throws SQLException, RemoteException, SessionExpiredException {
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
    public List<VariantComment> getVariantComments(String sid, int projectId, int referenceId, int uploadId, int fileID, int variantID) throws SQLException, RemoteException, SessionExpiredException {

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
    public void addVariantComments(String sid, List<VariantComment> variants) throws SQLException, RemoteException, SessionExpiredException {

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

    @Override
    public void removeVariantComments(String sessID, List<VariantComment> comments) throws SQLException, SessionExpiredException {

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

    private int getTotalNumStarred(String sid, int projectId, int referenceId) throws SQLException, SessionExpiredException {

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

    /**
     * Adds a new entry to the file table, returning a unique file_id.
     *
     * @param sid
     * @param uploadId
     * @param projectID
     * @param referenceID
     * @param file
     * @return
     * @throws SQLException
     * @throws SessionExpiredException
     */
    public static synchronized int addEntryToFileTable(String sid, int uploadId, int projectID, int referenceID, File file) throws SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.VariantFileTableSchema;

        InsertQuery q = new InsertQuery(table.getTable());
        q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadId);
        //q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileId);
        q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_PROJECT_ID), projectID);
        q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceID);
        q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_NAME), file.getAbsolutePath());
        ConnectionController.executeUpdate(sid, q.toString());

        String query = "SELECT last_insert_id() AS last_id from " + table.getTableName();
        ResultSet rs = ConnectionController.executeQuery(sid, query);
        if (!rs.first()) {
            throw new SQLException("Couldn't fetch file_id for file " + file.getAbsolutePath() + " on project " + projectID + ", ref " + referenceID);
        }

        int file_id = rs.getInt(1);
        if (file_id < 1) {//assert
            throw new SQLException("Invalid file_id for file " + file.getAbsolutePath() + " on project " + projectID + ", ref " + referenceID);
        }

        return file_id;
    }

    //public void removeEntriesFromFileTable(String sessID, int uploadID, int fileID) throws SQLException, SessionExpiredException {
    private void removeEntriesFromFileTable(String sessID, Collection<SimpleVariantFile> files) throws SQLException, SessionExpiredException {
        int i = 0;
        for (SimpleVariantFile svf : files) {
            //removeEntryFromFileTable(sessID, svf.getUploadId(), svf.getFileId());
            removeEntryFromFileTable(sessID, svf.getFileId());

        }
    }

    private void removeEntryFromFileTable(String sessID, int fileID) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VariantFileTableSchema;

        DeleteQuery q = new DeleteQuery(table.getTable());
        q.addCondition(ComboCondition.and(new Condition[]{
            //BinaryCondition.equalTo(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadID),
            BinaryCondition.equalTo(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileID)
        }));

        ConnectionController.executeUpdate(sessID, q.toString());
    }

    @Override
    public Map<SimplePatient, Integer> getPatientHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<SimplePatient> patients) throws SQLException, RemoteException, SessionExpiredException {

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
    public Map<String, Integer> getDNAIDHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException, SessionExpiredException {

        Map<String, Integer> dnaIDMap = new HashMap<String, Integer>();

        if (!dnaIDs.isEmpty()) {
            Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableViewInfo(sessID, projID, refID);
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

    private boolean isAuthorizedForUserComments(String sessID) throws SecurityException, SessionExpiredException, SQLException, RemoteException {        
        return true;
        //Temporary authorization: require admin access.        
        /*
        UserLevel ul = UserManager.getInstance().getSessionUsersLevel(sessID);
        if (ul.ADMIN != ul || UserManager.getInstance().isUserOfThisDatabase(sessID)) {
            return false;

        }
        return true;*/
        
    }

    public void deleteComment(String sessID, int userCommentId) throws SessionExpiredException, SQLException, RemoteException, SecurityException {
        if (!isAuthorizedForUserComments(sessID)) {
            throw new SecurityException("This user does not have access to view comments");
        }

        TableSchema lcTable = MedSavantDatabase.UserCommentTableSchema;
        UpdateQuery uq = new UpdateQuery(lcTable.getTable());
        uq.addSetClause(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_DELETED), true);
        uq.addCondition(BinaryCondition.equalTo(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER_COMMENT_ID), userCommentId));
        ConnectionController.executeUpdate(sessID, uq.toString());
    }

    @Override
    public UserCommentGroup createUserCommentGroup(String sessID, int projectId, int refId, VariantRecord vr) throws RemoteException, SQLException, SessionExpiredException, IllegalArgumentException {
        return createUserCommentGroup(sessID, projectId, refId, vr.getChrom(), vr.getStartPosition(), vr.getEndPosition(), vr.getRef(), vr.getAlt());
    }

    @Override
    public UserCommentGroup createUserCommentGroup(String sessID, int projectId, int refId, String chrom, long start_position, long end_position, String ref, String alt) throws RemoteException, SQLException, SessionExpiredException, IllegalArgumentException {
        UserCommentGroup lcg = getUserCommentGroup(sessID, projectId, refId, chrom, start_position, end_position, ref, alt, true);
        if (lcg != null) {
            throw new IllegalArgumentException("A comment group already exists at chrom=" + chrom + ", start=" + start_position + " end=" + end_position + " ref=" + ref + " alt=" + alt);
        }

        //insert, get groupId and modDate
        TableSchema lcgTable = MedSavantDatabase.UserCommentGroupTableSchema;
        InsertQuery iq = new InsertQuery(lcgTable.getTable());
        iq.addColumn(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId);
        iq.addColumn(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_REFERENCE_ID), refId);
        iq.addColumn(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_CHROMOSOME), chrom);
        iq.addColumn(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_START_POSITION), start_position);
        iq.addColumn(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_END_POSITION), end_position);
        iq.addColumn(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_REF), ref);
        iq.addColumn(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_ALT), alt);
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        PreparedStatement stmt = null;
        ResultSet res = null;
        int groupId = -1;
        try {
            LOG.info(iq.toString());
            stmt = conn.prepareStatement(iq.toString(), Statement.RETURN_GENERATED_KEYS);
            stmt.execute();
            res = stmt.getGeneratedKeys();
            res.next();
            groupId = res.getInt(1);
        } catch (SQLException sqe) {
            LOG.error("SQL Error ", sqe);
            throw sqe;
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }

        if (groupId < 0) {
            throw new SQLException("Unable to create new group - invalid insertion id");
        }

        //The modification time is the time when a change was made to the last comment.  Since there are 
        //no comments yet, modstamp is null.
        //Date modStamp = DBUtils.getCurrentDatabaseTime(sessID);
        Date modStamp = null;
        lcg = new UserCommentGroup(groupId, projectId, refId, chrom, start_position, end_position, ref, alt, modStamp, null);
        return lcg;
    }

    public void setUserCommentStatus(String sessID, int userCommentGroupId, UserComment statusChangeComment) throws SessionExpiredException, SQLException, RemoteException, SecurityException, IllegalArgumentException {
        if (!statusChangeComment.statusChanged()) {
            throw new IllegalArgumentException("Can't update the comment's status because it hasn't changed.");
        }

        Integer parentCommentId = statusChangeComment.getOriginalComment().getCommentID();
        if (parentCommentId == null) {
            throw new IllegalArgumentException("Can't update the comment's status because comment could not be located in the database");
        }

        int statusChangeCommentId = replyToUserCommentGroup(sessID, userCommentGroupId, statusChangeComment);

        //Overwrite the original status of the comment with the new status.        
        updateCommentStatus(sessID,
                parentCommentId,
                statusChangeComment.getOriginalComment().isApproved(),
                statusChangeComment.getOriginalComment().isIncluded(),                
                statusChangeComment.getOriginalComment().isDeleted());

        //Associate the original status of the comment with the status change comment.
        updateCommentStatus(sessID,
                statusChangeCommentId,
                statusChangeComment.isApproved(),
                statusChangeComment.isIncluded(),                
                statusChangeComment.isDeleted());

    }

    private void updateCommentStatus(String sessID, int commentId, boolean isApproved, boolean isIncluded, boolean isDeleted) throws SQLException, SessionExpiredException {
        TableSchema lcTable = MedSavantDatabase.UserCommentTableSchema;
        UpdateQuery uq = new UpdateQuery(lcTable.getTable());
        uq.addCondition(BinaryCondition.equalTo(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER_COMMENT_ID), commentId));
        uq.addSetClause(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_APPROVED), isApproved);
        uq.addSetClause(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_INCLUDE), isIncluded);        
        uq.addSetClause(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_DELETED), isDeleted);
        ConnectionController.executeUpdate(sessID, uq.toString());
    }

    @Override
    public int replyToUserCommentGroup(String sessID, int userCommentGroupId, UserComment userComment) throws SessionExpiredException, SQLException, RemoteException, SecurityException {
        if (!isAuthorizedForUserComments(sessID)) {
            throw new SecurityException("This user does not have access to view comments");
        }

        String username = SessionManager.getInstance().getUserForSession(sessID);
        String ontologyId = userComment.getOntologyTerm().getID();

        //TODO: Insert checks here to make sure user has permissions to change flags.      
        //If they don't, use the flags in 'lastComment' commented out above.       
        Boolean isApproved = userComment.isApproved();
        Boolean isIncluded = userComment.isIncluded();        
        Boolean isDeleted = userComment.isDeleted();
        String commentText = userComment.getCommentText();
        String ontology = userComment.getOntologyTerm().getOntology().name();
        TableSchema lcTable = MedSavantDatabase.UserCommentTableSchema;
        InsertQuery iq = new InsertQuery(lcTable.getTable());

        iq.addColumn(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER_COMMENT_GROUP_ID), userCommentGroupId);
        iq.addColumn(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_ONTOLOGY_ID), ontologyId);
        iq.addColumn(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_ONTOLOGY), ontology);
        iq.addColumn(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER), username);
        iq.addColumn(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_APPROVED), isApproved);
        iq.addColumn(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_INCLUDE), isIncluded);        
        iq.addColumn(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_DELETED), isDeleted);
        iq.addColumn(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_CREATION_DATE), (new FunctionCall(new CustomSql("NOW"))).addCustomParams());
        iq.addColumn(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_COMMENT), commentText);
        if (userComment.getOriginalComment() != null) {
            Integer commentId = userComment.getOriginalComment().getCommentID();
            if (commentId == null) {
                throw new IllegalArgumentException("Cannot post this comment as it refers to a comment with a null identifier");
            }
            if (commentId < 1) {
                throw new IllegalArgumentException("Cannot post this comment as it refers to a comment with a non-positive identifier: " + commentId);
            }
            iq.addColumn(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_PARENT_USER_COMMENT_ID), commentId);
        }

        PreparedStatement stmt = null;
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        ResultSet res = null;
        int commentId = -1; //Not returned for now.
        try {
            stmt = conn.prepareStatement(iq.toString(), Statement.RETURN_GENERATED_KEYS);
            stmt.execute();
            res = stmt.getGeneratedKeys();
            res.next();
            commentId = res.getInt(1);
            //LOG.info("Inserted new comment with id " + commentId + " for comment group with id " + userCommentGroupId);

            //update original comment's status if necessary.
            if (userComment.getOriginalComment() != null) {
                UpdateQuery uq = new UpdateQuery(lcTable.getTable());
                uq.addCondition(BinaryCondition.equalTo(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER_COMMENT_ID), userComment.getOriginalComment().getCommentID()));
                uq.addSetClause(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_APPROVED), userComment.getOriginalComment().isApproved());
                uq.addSetClause(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_INCLUDE), userComment.getOriginalComment().isIncluded());                
                uq.addSetClause(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_DELETED), userComment.getOriginalComment().isDeleted());
                stmt = conn.prepareStatement(uq.toString());
                stmt.execute();
            }
            return commentId;
        } catch (SQLException sqe) {
            LOG.error("SQL Error", sqe);
            throw sqe;
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (res != null) {
                res.close();
            }
        }
    }

    @Override
    public UserCommentGroup getUserCommentGroup(String sessID, int projectId, int refId, VariantRecord vr) throws RemoteException, SessionExpiredException, SQLException, SecurityException {
        return getUserCommentGroup(sessID, projectId, refId, vr.getChrom(), vr.getStartPosition(), vr.getEndPosition(), vr.getRef(), vr.getAlt());
    }

    @Override
    public UserCommentGroup getUserCommentGroup(String sessID, int projectId, int refId, String chrom, long start_position, long end_position, String ref, String alt) throws RemoteException, SessionExpiredException, SQLException, SecurityException {
        return getUserCommentGroup(sessID, projectId, refId, chrom, start_position, end_position, ref, alt, false);
    }

    private List<UserComment> getUserCommentsForGroup(String sessID, int groupId, boolean loadOnlyMostRecentComment) throws SQLException, SessionExpiredException, RemoteException {

        ResultSet rs = null;
        try {
            TableSchema lcgTable = MedSavantDatabase.UserCommentGroupTableSchema;
            SelectQuery sq = new SelectQuery();//lcgTable.getTable());
            sq.addFromTable(lcgTable.getTable());
            sq.addAllColumns();
            sq.addCondition(BinaryCondition.equalTo(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_USER_COMMENT_GROUP_ID), groupId));
            rs = ConnectionController.executeQuery(sessID, sq.toString());
            if (rs == null || !rs.next()) {
                throw new IllegalArgumentException("There is no comment group with the given identifier " + groupId);
            }
        } catch (SQLException sqe) {
            LOG.error("SQL Error", sqe);
            throw sqe;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        rs = null;
        try {
            TableSchema lcTable = MedSavantDatabase.UserCommentTableSchema;
            SelectQuery sq = new SelectQuery();
            sq.addFromTable(lcTable.getTable());
            sq.addAllColumns();
            sq.addCondition(BinaryCondition.equalTo(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER_COMMENT_GROUP_ID), groupId));
            String querySuffix = "";
            if (loadOnlyMostRecentComment) {
                sq.addCondition(UnaryCondition.isNotNull(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_PARENT_USER_COMMENT_ID)));
                sq.addOrdering(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER_COMMENT_ID), Dir.DESCENDING);
                querySuffix = " LIMIT 1";
            } else {
                sq.addOrdering(MedSavantDatabase.UserCommentTableSchema.getDBColumn(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER_COMMENT_ID), Dir.ASCENDING);
            }
            LOG.info(sq.toString() + querySuffix);

            rs = ConnectionController.executeQuery(sessID, sq.toString() + querySuffix);
            Map<Integer, UserComment> parentCommentMap = new TreeMap<Integer, UserComment>();
            Map<Integer, Set<UserComment>> childCommentMap = new HashMap<Integer, Set<UserComment>>();

            while (rs.next()) {
                int commentId = rs.getInt(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER_COMMENT_ID);
                int parentCommentId = rs.getInt(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_PARENT_USER_COMMENT_ID);
                String ontologyId = rs.getString(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_ONTOLOGY_ID);
                String user = rs.getString(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER);
                Boolean isApproved = rs.getBoolean(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_APPROVED);
                Boolean isIncluded = rs.getBoolean(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_INCLUDE);                
                Boolean isDeleted = rs.getBoolean(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_DELETED);
                Date creationDate = rs.getDate(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_CREATION_DATE);
                Timestamp ts = rs.getTimestamp(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_MODIFICATION_DATE);
                Date modDate = new Date(ts.getTime());
                String commentText = rs.getString(MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_COMMENT);
                OntologyTerm ot = OntologyManager.getInstance().getOntologyTerm(sessID, UserComment.ONTOLOGY_TYPE, ontologyId);

                UserComment parentComment = null;
                if (parentCommentId > 0) {
                    parentComment = parentCommentMap.get(parentCommentId);
                    if (parentComment == null) {
                        LOG.error("Comment with id " + commentId + " refers to a comment with an unknown identifier (" + parentCommentId + ")");
                        throw new SQLException("Invalid comment detected in database with id " + commentId + " (Refers to non-existant comment with id " + parentCommentId + ")");
                    } else {
                        //this is a child comment
                        UserComment lc = new UserComment(commentId, user, isApproved, isIncluded, isDeleted, creationDate, modDate, commentText, ot, parentComment);
                        Set<UserComment> lcSet = childCommentMap.get(parentCommentId);
                        if (lcSet == null) {
                            lcSet = new TreeSet<UserComment>(new Comparator<UserComment>() {
                                @Override
                                public int compare(UserComment o1, UserComment o2) {
                                    return o1.getModificationDate().compareTo(o2.getModificationDate());
                                }
                            });
                        }
                        
                        lcSet.add(lc);                        
                        childCommentMap.put(parentCommentId, lcSet);
                    }
                } else {
                    //this is a parent comment.
                    UserComment lc = new UserComment(commentId, user, isApproved, isIncluded, isDeleted, creationDate, modDate, commentText, ot, null);
                    parentCommentMap.put(commentId, lc);
                }
            }

            List<UserComment> comments = new ArrayList<UserComment>();

            for (Map.Entry<Integer, UserComment> e : parentCommentMap.entrySet()) {
                Integer parentId = e.getKey();
                UserComment parentComment = e.getValue();
                comments.add(parentComment);

                Set<UserComment> childComments = childCommentMap.get(parentId);
                if (childComments != null) {
                    comments.addAll(childComments);
                }
            }

            return comments;
        } catch (SQLException sqe) {
            LOG.error("SQL Error", sqe);
            throw sqe;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    private UserCommentGroup getUserCommentGroup(String sessID, int projectId, int refId, String chrom, long start_position, long end_position, String ref, String alt, boolean loadOnlyMostRecentComment) throws RemoteException, SessionExpiredException, SQLException, SecurityException {
        if (!isAuthorizedForUserComments(sessID)) {
            throw new SecurityException("This user does not have access to view comments");
        }

        //Select VariantCommentGroup
        TableSchema lcgTable = MedSavantDatabase.UserCommentGroupTableSchema;
        SelectQuery sq = new SelectQuery();//lcgTable.getTable());
        sq.addFromTable(lcgTable.getTable());
        ComboCondition cc
                = ComboCondition.and(
                        BinaryCondition.equalTo(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId),
                        BinaryCondition.equalTo(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_REFERENCE_ID), refId),
                        BinaryCondition.equalTo(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_CHROMOSOME), chrom),
                        BinaryCondition.equalTo(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_START_POSITION), start_position),
                        BinaryCondition.equalTo(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_END_POSITION), end_position),
                        BinaryCondition.equalTo(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_REF), ref),
                        BinaryCondition.equalTo(MedSavantDatabase.UserCommentGroupTableSchema.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_ALT), alt)
                );
        sq.addCondition(cc);
        sq.addColumns(lcgTable.getDBColumn(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_USER_COMMENT_GROUP_ID));
        ResultSet rs = null;
        int groupId = 0;
        try {
            rs = ConnectionController.executeQuery(sessID, sq.toString());

            if (rs.next()) {
                groupId = rs.getInt(MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_USER_COMMENT_GROUP_ID);
            } else {
                //No comments started for this variant! 
                return null;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        Date modDate = null;
        List<UserComment> comments = getUserCommentsForGroup(sessID, groupId, loadOnlyMostRecentComment);
        if (comments.size() > 0) {
            UserComment lastComment = comments.get(comments.size() - 1);
            modDate = lastComment.getModificationDate();
        }

        return new UserCommentGroup(groupId, projectId, refId, chrom, start_position, end_position, ref, alt, modDate, comments);
    }

    private void getDNAIDHeatMapHelper(String sessID, TableSchema table, float multiplier, Collection<String> dnaIDs, Condition c, boolean useThreshold, Map<String, Integer> map) throws SQLException, SessionExpiredException {

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

}
