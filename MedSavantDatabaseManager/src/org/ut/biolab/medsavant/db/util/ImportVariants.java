/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil.Action;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.vcf.VCFParser;

/**
 *
 * @author Andrew
 */
public class ImportVariants {
    
    public static boolean performImport(File[] vcfFiles, int projectId, int referenceId) throws SQLException {
        return performImport(vcfFiles, projectId, referenceId);
    }
    
    public static boolean performImport(File[] vcfFiles, int projectId, int referenceId, JLabel progressLabel) throws SQLException {
        
        //add log
        int updateId = AnnotationLogQueryUtil.addAnnotationLogEntry(projectId, referenceId, Action.ADD_VARIANTS);
        
        //get custom fields for vcf
        /*List<CustomField> customFields = ProjectQueryUtil.getCustomVariantFields(projectId); 
        String[] infoFields = new String[customFields.size()];
        Class[] infoClasses = new Class[customFields.size()];
        for(int i = 0; i < customFields.size(); i++){
            infoFields[i] = customFields.get(i).getColumnName();
            infoClasses[i] = customFields.get(i).getColumnClass();
        }*/
        
        //create the staging table 
        String tableName = DBSettings.createVariantStagingTableName(projectId, referenceId, updateId);
        try {
            tableName = ProjectQueryUtil.createVariantTable(projectId, referenceId, updateId, null, true);
        } catch (SQLException ex) {
            //table already exists?
        }
        
        //add files to staging table
        for(int i = 0; i < vcfFiles.length; i++){

            //update progress
            String progress = "Importing file " + (i+1) + " of " + vcfFiles.length + "...";
            if(progressLabel != null){               
                progressLabel.setText(progress);
            }
            
            //create temp file
            File outfile = new File("temp_tdf"); //TODO: should put this in a temp dir or something
            
            //parse vcf file
            try {
                VCFParser.parseVariants(vcfFiles[i], outfile, updateId, i);
            } catch (Exception ex) {
                Logger.getLogger(ImportVariants.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            
            //add to staging table
            try {
                if(progressLabel != null){               
                    progressLabel.setText("Uploading file " + (i+1) + " of " + vcfFiles.length + "...");
                    progressLabel.updateUI();
                }
                VariantQueryUtil.uploadFileToVariantTable(outfile, tableName); 
            } catch (SQLException ex) {
                Logger.getLogger(ImportVariants.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            
            //cleanup
            //outfile.delete();            
            System.gc();            
        }
        
        //set log as pending
        AnnotationLogQueryUtil.setAnnotationLogStatus(updateId, AnnotationLogQueryUtil.Status.PENDING);
        return true;
    }
 
}
