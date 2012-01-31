package org.ut.biolab.medsavant.db.variants.update;

import com.healthmarketscience.rmiio.RemoteInputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.AnnotationLog.Status;
import org.ut.biolab.medsavant.db.model.SimpleVariantFile;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.FileServer;
import org.ut.biolab.medsavant.db.util.ServerDirectorySettings;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.variants.upload.api.VariantManagerAdapter;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.log.ServerLogger;

/**
 *
 * @author Andrew
 */
public class VariantManager extends java.rmi.server.UnicastRemoteObject implements VariantManagerAdapter {

    private static VariantManager instance;

    public static synchronized VariantManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new VariantManager();
        }
        return instance;
    }

    public VariantManager() throws RemoteException {}    

    
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
        for(Integer refId : refIds){
            ref2Update.put(refId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectID, refId, false));
        }
        
        //update annotation log table
        ServerLogger.log(VariantManager.class, "Setting log status to published");
        for(Integer refId : ref2Update.keySet()){
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, ref2Update.get(refId), Status.PUBLISHED);
        } 
        
        //publish
        ServerLogger.log(VariantManager.class, "Terminating active sessions");
        SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sid), "Administrator (" + SessionController.getInstance().getUserForSession(sid) + ") published new variants");        
        ServerLogger.log(VariantManager.class, "Publishing tables");
        for(Integer refId : ref2Update.keySet()){
            ProjectQueryUtil.getInstance().publishVariantTable(c, projectID, refId, ref2Update.get(refId));
        }   
        
        ServerLogger.log(VariantManager.class, "Publish complete");
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
    public int updateTable(String sid, int projectId, int referenceId, int[] annotationIds, List<CustomField> variantFields) throws RemoteException, IOException, InterruptedException, SQLException, Exception {
        
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
        int updateId = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sid, projectId, referenceId, org.ut.biolab.medsavant.db.model.AnnotationLog.Action.ADD_VARIANTS, user);     
        
        try {
        
            //dump existing table
            ServerLogger.log(VariantManager.class, "Dumping existing variant table");
            String existingTableName = ProjectQueryUtil.getInstance().getVariantTablename(sid, projectId, referenceId, false);
            File variantDumpFile = new File(basedir,"temp_" + existingTableName);
            String variantDump = variantDumpFile.getAbsolutePath();
            ServerLogger.log(VariantManager.class, "Dumping variants to file");
            VariantManagerUtils.variantsToFile(sid,existingTableName, variantDumpFile, null, false);

            //sort variants
            ServerLogger.log(VariantManager.class, "Sorting variants");
            String sortedVariants = variantDump + "_sorted";
            VariantManagerUtils.sortFileByPosition(variantDump, sortedVariants);

            //add custom vcf fields
            ServerLogger.log(VariantManager.class, "Adding custom vcf fields");
            String vcfAnnotatedVariants = sortedVariants + "_vcf";
            VariantManagerUtils.addCustomVcfFields(sortedVariants, vcfAnnotatedVariants, variantFields, DefaultVariantTableSchema.INDEX_OF_FILTER + 1); //last of the default fields

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

            //upload to staging table
            ServerLogger.log(VariantManager.class, "Uploading variants to table: " + tableName);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid, new File(outputFilenameMerged), tableName);

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
            
        } catch (Exception e){
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, updateId, Status.ERROR);
            throw e;
        }
    }
    
    /*
     * Start the upload process for new vcf files. Will result in the creation
     * of a new, unpublished, up-to-date variant table. 
     */
    @Override
    public int uploadVariants(String sid, RemoteInputStream[] fileStreams, String[] fileNames, int projectId, int referenceId, String[][] variantTags) throws RemoteException, IOException, Exception {
        File[] vcfFiles = new File[fileStreams.length];

        int i = 0;
        for (RemoteInputStream s : fileStreams) {
            vcfFiles[i] = FileServer.getInstance().sendFile(s);
            i++;
        }

        return uploadVariants(sid, vcfFiles, fileNames, projectId, referenceId, variantTags);     
    }
    
    /*
     * Helper that does all the actual work of uploading new vcf files. 
     */
    private static int uploadVariants(String sid, File[] vcfFiles, String[] fileNames, int projectId, int referenceId, String[][] variantTags) throws Exception {

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
            File tempFile = VariantManagerUtils.parseVCFs(vcfFiles, baseDir, updateId);
            String tempFilename = tempFile.getAbsolutePath();
            currentFilename = tempFilename;

            //add custom fields
            ServerLogger.log(VariantManager.class, "Adding custom vcf fields");
            List<CustomField> customFields = ProjectQueryUtil.getInstance().getCustomVariantFields(sid, projectId, referenceId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectId, referenceId, false));
            if(!customFields.isEmpty()){
                String customFieldFilename = currentFilename + "_vcf";
                VariantManagerUtils.addCustomVcfFields(currentFilename, customFieldFilename, customFields, DefaultVariantTableSchema.INDEX_OF_FILTER + 1); //last of the default fields
                currentFilename = customFieldFilename;
            }
            
            //get annotation ids
            int[] annotationIds = AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId);
            
            if(annotationIds.length > 0){
                
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
            VariantManagerUtils.variantsToFile(sid,existingTableName, existingVariantsFile, null, true);
            VariantManagerUtils.logFileSize(tempFilename);

            //cat existing table to new annotations
            ServerLogger.log(VariantManager.class, "Concatenating existing variants with new");
            VariantManagerUtils.appendToFile(existingVariantsFile.getAbsolutePath(), currentFilename);
            currentFilename = existingVariantsFile.getAbsolutePath();

            if(annotationIds.length > 0){
            
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

            //upload to staging table
            ServerLogger.log(VariantManager.class, "Uploading variants to table: " + tableName);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid,new File(currentFilename), tableName);
            
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
            
        } catch (Exception e){
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, updateId, Status.ERROR);
            throw e;
        }
    }
    
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
        int updateId = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sid, projectId, referenceId, org.ut.biolab.medsavant.db.model.AnnotationLog.Action.REMOVE_VARIANTS, user);     

        try {

            //dump existing except for files
            String existingTableName = ProjectQueryUtil.getInstance().getVariantTablename(sid, projectId, referenceId, false);
            File existingVariantsFile = new File(baseDir,"temp_proj" + projectId + "_ref" + referenceId + "_update" + updateId);
            ServerLogger.log(VariantManager.class, "Dumping variants to file");
            String conditions = "";
            for(int i = 0; i < files.size(); i++){
                conditions += 
                        "!(" + DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID + "=" + files.get(i).getUploadId() +
                        " AND " + DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID + "=" + files.get(i).getFileId() + ")";
                if(i != files.size()-1){
                    conditions += " AND ";
                }
            }
            VariantManagerUtils.variantsToFile(sid, existingTableName, existingVariantsFile, conditions, true);
            
            //create the staging table
            ServerLogger.log(VariantManager.class, "Creating new variant table for resulting variants");
            ProjectQueryUtil.getInstance().setCustomVariantFields(
                    sid, projectId, referenceId, updateId,
                    ProjectQueryUtil.getInstance().getCustomVariantFields(sid, projectId, referenceId, ProjectQueryUtil.getInstance().getNewestUpdateId(sid, projectId, referenceId, false)));
            String tableName = ProjectQueryUtil.getInstance().createVariantTable(sid, projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), true);

            //upload to staging table
            ServerLogger.log(VariantManager.class, "Uploading variants to table: " + tableName);
            VariantQueryUtil.getInstance().uploadFileToVariantTable(sid, existingVariantsFile, tableName);
            
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
            
        } catch (Exception e){
            AnnotationLogQueryUtil.getInstance().setAnnotationLogStatus(sid, updateId, Status.ERROR);
            throw e;
        }
    }

}
