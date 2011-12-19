/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import javax.swing.JLabel;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil.Action;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.vcf.VCFHeader;
import org.ut.biolab.medsavant.vcf.VCFParser;

/**
 *
 * @author Andrew
 */
public class ImportVariants {

    private static final int outputLinesLimit = 1000000;

    public static int performImport(File[] vcfFiles, File tmpDir, int projectId, int referenceId, JLabel progressLabel) throws Exception {
        
        //add log
        int updateId = -1;
        try {
            updateId = AnnotationLogQueryUtil.addAnnotationLogEntry(projectId, referenceId, Action.ADD_VARIANTS);
        } catch (SQLException e){
            throw new SQLException("Could not start update. ", e);
        }

        //create the staging table
        String tableName = DBSettings.createVariantStagingTableName(projectId, referenceId, updateId);
        try {
            tableName = ProjectQueryUtil.createVariantTable(projectId, referenceId, updateId, null, true);
        } catch (SQLException ex) {
            //table already exists?
        }
        
        try {
            try {
                return performImportHelper(updateId, tableName, vcfFiles, tmpDir, projectId, referenceId, progressLabel);
            } catch (SQLException e){   
                throw new SQLException("There was an error adding your file to the database. Make sure that all fields are the appropriate type and length.", e);           
            } catch (InterruptedException e){
                throw new InterruptedException("Upload cancelled. ");
            } catch (FileNotFoundException e){
                throw new FileNotFoundException("One or more of your files could not be read. ");
            } catch (IOException e) {
                throw new IOException("There was an error opening one of your files. ", e);
            } catch (ParseException e) {
                throw e;
            } catch (Exception e) {
                throw new Exception("Your import could not be completed. Make sure your files are in the correct format. ");
            }
        } catch (Exception e){
            VariantQueryUtil.cancelUpload(updateId, tableName);
            throw e;
        }
    }
    
    private static int performImportHelper(int updateId, String tableName, File[] vcfFiles, File tmpDir, int projectId, int referenceId, JLabel progressLabel) throws SQLException, InterruptedException, FileNotFoundException, IOException, ParseException, Exception {

        boolean variantFound = false;
        
        //add files to staging table
        for(int i = 0; i < vcfFiles.length; i++){

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

                File outfile = new File(tmpDir,iteration + "_tmp.tdf");
                iteration++;

                //parse vcf file
                checkInterrupt(updateId, tableName);
                if(progressLabel != null){
                    progressLabel.setText("Parsing part " + iteration + " of file " + (i+1) + " of " + vcfFiles.length + "...");
                    progressLabel.updateUI();
                }
                lastChunkWritten = VCFParser.parseVariantsFromReader(r, header, outputLinesLimit, outfile, updateId, i);
                if(lastChunkWritten > 0){
                    variantFound = true;
                }

                //add to staging table
                checkInterrupt(updateId, tableName);
                if(progressLabel != null){
                    if (lastChunkWritten >= outputLinesLimit) {
                        progressLabel.setText("Uploading part " + iteration + " of file " + (i+1) + " of " + vcfFiles.length + "...");
                    } else {
                        progressLabel.setText("Uploading file " + (i+1) + " of " + vcfFiles.length + "...");
                    }
                    progressLabel.updateUI();
                }
                VariantQueryUtil.uploadFileToVariantTable(outfile, tableName);

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
        if(!variantFound){
            throw new ParseException("No variants were found. Ensure that your files are in the correct format. ", 0);
        }

        //set log as pending
        checkInterrupt(updateId, tableName);
        AnnotationLogQueryUtil.setAnnotationLogStatus(updateId, AnnotationLogQueryUtil.Status.PENDING);
        return updateId;
    }

    public static void addTagsToUpload(int uploadID, String[][] variantTags) throws SQLException {
        try {
            VariantQueryUtil.addTagsToUpload(uploadID,variantTags);
        } catch (SQLException e) {
            throw new SQLException("Error adding tags", e);
        }
    }

    private static void checkInterrupt(int updateId, String tableName) throws InterruptedException{
        if(Thread.interrupted()){
            throw new InterruptedException(Integer.toString(updateId) + ";" + tableName);
        }
    }
}
