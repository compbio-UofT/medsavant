package org.ut.biolab.medsavant.db.variants.update;

import au.com.bytecode.opencsv.CSVReader;
import com.healthmarketscience.rmiio.RemoteInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.ParseException;
import javax.swing.JLabel;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.FileServer;
import org.ut.biolab.medsavant.db.util.ServerDirectorySettings;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.variants.upload.api.UploadVariantsAdapter;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.log.ServerLogger;
import org.ut.biolab.medsavant.vcf.VCFHeader;
import org.ut.biolab.medsavant.vcf.VCFParser;

/**
 *
 * @author Andrew
 */
public class UploadVariants extends java.rmi.server.UnicastRemoteObject implements UploadVariantsAdapter {

    private static UploadVariants instance;

    public static synchronized UploadVariants getInstance() throws RemoteException {

        if (instance == null) {
            instance = new UploadVariants();
        }
        return instance;
    }

    public UploadVariants() throws RemoteException {
    }
    private static final int outputLinesLimit = 1000000;

    public int uploadVariants(String sid, RemoteInputStream[] fileStreams, int projectId, int referenceId) throws RemoteException, IOException, Exception {
        File[] vcfFiles = new File[fileStreams.length];

        int i = 0;
        for (RemoteInputStream s : fileStreams) {
            vcfFiles[i] = FileServer.getInstance().sendFile(s);
            i++;
        }

        return uploadVariants(sid, vcfFiles, projectId, referenceId);
    }

    private static int uploadVariants(String sid, File[] vcfFiles, int projectId, int referenceId) throws Exception {

        String user = SessionController.getInstance().getUserForSession(sid);

        File tmpDir = ServerDirectorySettings.generateDateStampDirectory(ServerDirectorySettings.getTmpDirectory());

        //add log
        int updateId = -1;

        updateId = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sid, projectId, referenceId, org.ut.biolab.medsavant.db.model.AnnotationLog.Action.ADD_VARIANTS, user);

        //create the staging table
        String tableName = DBSettings.createVariantStagingTableName(projectId, referenceId, updateId);
        try {
            tableName = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, null, true);
        } catch (SQLException ex) {
            //table already exists?
        }

        try {
            try {
                return performImportHelper(sid, updateId, tableName, vcfFiles, tmpDir, projectId, referenceId);
            } catch (SQLException e) {
                throw new SQLException("There was an error adding your file to the database. Make sure that all fields are the appropriate type and length.", e);
            } catch (InterruptedException e) {
                throw new InterruptedException("Upload cancelled. ");
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("One or more of your files could not be read. ");
            } catch (IOException e) {
                throw new IOException("There was an error opening one of your files. ", e);
            } catch (ParseException e) {
                throw e;
            } catch (Exception e) {
                throw new Exception("Your import could not be completed. Make sure your files are in the correct format. ");
            }
        } catch (Exception e) {
            VariantQueryUtil.getInstance().cancelUpload(sid, updateId, tableName);
            throw e;
        }
    }

    private static int performImportHelper(String sid, int updateId, String tableName, File[] vcfFiles, File tmpDir, int projectId, int referenceId) throws SQLException, InterruptedException, FileNotFoundException, IOException, ParseException, Exception {

        boolean variantFound = false;

        //add files to staging table
        for (int i = 0; i < vcfFiles.length; i++) {

            checkInterrupt(updateId, tableName);

            //create temp file
            int lastChunkWritten = 0;
            int iteration = 0;

            CSVReader r;
            r = new CSVReader(new FileReader(vcfFiles[i]), VCFParser.defaultDelimiter);
            if (r == null) {
                throw new FileNotFoundException();
            }

            VCFHeader header = null;
            header = VCFParser.parseVCFHeader(r);

            while (iteration == 0 || lastChunkWritten >= outputLinesLimit) {

                File outfile = new File(tmpDir, iteration + "_tmp.tdf");
                iteration++;

                //parse vcf file
                checkInterrupt(updateId, tableName);
                /*
                if(progressLabel != null){
                progressLabel.setText("Parsing part " + iteration + " of file " + (i+1) + " of " + vcfFiles.length + "...");
                progressLabel.updateUI();
                }
                 *
                 */
                lastChunkWritten = VCFParser.parseVariantsFromReader(r, header, outputLinesLimit, outfile, updateId, i);
                if (lastChunkWritten > 0) {
                    variantFound = true;
                }

                //add to staging table
                checkInterrupt(updateId, tableName);
                /*
                if(progressLabel != null){
                if (lastChunkWritten >= outputLinesLimit) {
                progressLabel.setText("Uploading part " + iteration + " of file " + (i+1) + " of " + vcfFiles.length + "...");
                } else {
                progressLabel.setText("Uploading file " + (i+1) + " of " + vcfFiles.length + "...");
                }
                progressLabel.updateUI();
                }
                 *
                 */
                VariantQueryUtil.getInstance().uploadFileToVariantTable(sid, outfile, tableName);

                outfile.delete();
            }

            try {
                r.close();
            } catch (IOException ex) {
            }

            //cleanup
            //outfile.delete();
            System.gc();
        }

        //make sure files weren't all empty
        if (!variantFound) {
            throw new ParseException("No variants were found. Ensure that your files are in the correct format. ", 0);
        }

        //set log as pending
        checkInterrupt(updateId, tableName);
        AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, updateId, org.ut.biolab.medsavant.db.model.AnnotationLog.Status.PENDING);
        return updateId;
    }

    public static void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException, RemoteException {
        try {
            VariantQueryUtil.getInstance().addTagsToUpload(sid, uploadID, variantTags);
        } catch (SQLException e) {
            throw new SQLException("Error adding tags", e);
        }
    }

    private static void checkInterrupt(int updateId, String tableName) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException(Integer.toString(updateId) + ";" + tableName);
        }
    }

    @Override
    public void annotateVariants(String sid, int projectId, int referenceId, int updateId) throws RemoteException {

        try {
            //TODO: users shouldnt see ids
            ServerLogQueryUtil.getInstance().addServerLog(sid, ServerLogQueryUtil.LogType.INFO, "Adding variants to projectid=" + projectId + " referenceid=" + referenceId);
            UpdateVariantTable.performAddVCF(sid,projectId, referenceId, updateId);
            //TODO: users shouldnt see ids
            ServerLogQueryUtil.getInstance().addServerLog(sid, ServerLogQueryUtil.LogType.INFO, "Done adding variants to projectid=" + projectId + " referenceid=" + referenceId);

        } catch (Exception e) {
            ServerLogger.logError(UploadVariants.class, e);
            ServerLogger.logByEmail(UploadVariants.class, "Uh oh...", "There was a problem making update " + updateId + ". Here's the error message:\n\n" + e.getLocalizedMessage());
        }
    }
}
