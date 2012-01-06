package org.ut.biolab.medsavant.db.variants.update;

import au.com.bytecode.opencsv.CSVReader;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.FileServer;
import org.ut.biolab.medsavant.db.util.ServerDirectorySettings;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.variants.upload.api.VariantManagerAdapter;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.log.ServerLogger;
import org.ut.biolab.medsavant.vcf.VCFHeader;
import org.ut.biolab.medsavant.vcf.VCFParser;
import org.ut.biolab.medsavant.vcf.VariantRecord;

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

    public VariantManager() throws RemoteException {
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

        /**
         * TEMPORARY!
         *
        SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sid), "Administrator (" + SessionController.getInstance().getUserForSession(sid) + ") published new variants");
        */


        String user = SessionController.getInstance().getUserForSession(sid);

        File tmpDir = ServerDirectorySettings.generateDateStampDirectory(ServerDirectorySettings.getTmpDirectory());

        //add log
        int updateId = -1;

        updateId = AnnotationLogQueryUtil.getInstance().addAnnotationLogEntry(sid, projectId, referenceId, org.ut.biolab.medsavant.db.model.AnnotationLog.Action.ADD_VARIANTS, user);

        //create the staging table
        String tableName = DBSettings.getVariantStagingTableName(projectId, referenceId, updateId);
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
            performAddVCF(sid,projectId, referenceId, updateId);
            //TODO: users shouldnt see ids
            ServerLogQueryUtil.getInstance().addServerLog(sid, ServerLogQueryUtil.LogType.INFO, "Done adding variants to projectid=" + projectId + " referenceid=" + referenceId);

        } catch (Exception e) {
            ServerLogger.logError(VariantManager.class, e);
            ServerLogger.logByEmail(VariantManager.class, "Uh oh...", "There was a problem making update " + updateId + ". Here's the error message:\n\n" + e.getLocalizedMessage());
        }
    }

    @Override
    public void publishVariants(String sid, int projectID, int referenceID, int updateID) throws RemoteException, SQLException {

        SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sid), "Administrator (" + SessionController.getInstance().getUserForSession(sid) + ") published new variants");

        String stagingTableName = DBSettings.getVariantStagingTableName(projectID, referenceID, updateID);
        String newTableName = DBSettings.getVariantTableName(projectID, referenceID, updateID);

        Connection c = (ConnectionController.connectPooled(sid));

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;

        SelectQuery s = new SelectQuery();
        s.addFromTable(table.getTable());
        s.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
        s.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectID));
        s.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceID));
        ResultSet rs = c.createStatement().executeQuery(s.toString());
        String oldTableName = null;
        if (rs.next()) {
            oldTableName = rs.getString(1);
        }

        //remove existing entries
        DeleteQuery d = new DeleteQuery(table.getTable());
        d.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectID));
        d.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceID));
        c.createStatement().execute(d.toString());

        //create new entry
        InsertQuery query1 = new InsertQuery(table.getTable());
        query1.addColumn(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectID);
        query1.addColumn(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceID);
        query1.addColumn(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), newTableName);
        c.createStatement().execute(query1.toString());

         //remove staging tables and old table
        ServerLogger.log(VariantManager.class, "Removing staging table and old variant table");

        dropTableIfExists(sid,stagingTableName);
        if (oldTableName != null) {
            dropTableIfExists(sid,oldTableName);
        }

    }

    public static Random rand = new Random();

    public static void performUpdateAnnotations(String sid, int projectId, int referenceId, int updateId) throws SQLException, Exception {

        Date now = new Date();
        String basedir = (now.getYear()+1900) + "_" + now.getMonth() + "_" + now.getDay() + "_" + now.getHours() + "_" + now.getMinutes() + "_project_" + projectId + "_reference_" + referenceId + "_" + (rand).nextInt();
        (new File(basedir)).mkdir();

        ServerLogger.log(VariantManager.class, "Creating writable directory " + basedir);

        Process p = Runtime.getRuntime().exec("chmod -R o+w " + basedir);
        p.waitFor();

        ServerLogger.log(VariantManager.class, "Updating project=" + projectId + " reference=" + referenceId);

        String tableName = ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId);

        //dump existing variants
        File variantDumpFile = new File(basedir,"temp_proj" + projectId + "_ref" + referenceId);
        String variantDump = variantDumpFile.getAbsolutePath();
        ServerLogger.log(VariantManager.class, "Dumping variants to file: " + variantDump);
        variantsToFile(sid,tableName, new File(variantDump));
        //variantsToFile(tableName, new File(variantDump), ProjectQueryUtil.getActualCustomVariantFields(projectId), new ArrayList<CustomField>());

        //sort variants
        ServerLogger.log(VariantManager.class, "Sorting variants");
        String sortedVariants = variantDump + "_sorted";
        sortFileByPosition(variantDump, sortedVariants);

        //add custom vcf fields
        ServerLogger.log(VariantManager.class, "Adding custom vcf fields");
        List<CustomField> customFields = ProjectQueryUtil.getInstance().getCustomVariantFields(sid,projectId);
        String vcfAnnotatedVariants = sortedVariants + "_vcf";
        addCustomVcfFields(sortedVariants, vcfAnnotatedVariants, customFields, DefaultVariantTableSchema.INDEX_OF_FILTER + 1); //last of the default fields

        //annotate
        String outputFilename = vcfAnnotatedVariants + "_annotated";
        ServerLogger.log(VariantManager.class, "File containing annotated variants, sorted by position: " + outputFilename);
        int[] annotationIds = AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId);
        annotateTDF(sid,vcfAnnotatedVariants, outputFilename, annotationIds);

        //split
        File splitDir = new File(basedir,"splitDir");
        splitDir.mkdir();
        ServerLogger.log(VariantManager.class, "Splitting annotation file into multiple files by file ID");
        for (File f : splitDir.listFiles()) {
            removeTemp(f);
        }
        splitFileOnColumn(splitDir, outputFilename, 1);
        String outputFilenameMerged = outputFilename + "_merged";

        //merge
        ServerLogger.log(VariantManager.class, "File containing annotated variants, sorted by file: " + outputFilenameMerged);
        ServerLogger.log(VariantManager.class, "Merging files");
        concatenateFilesInDir(splitDir, outputFilenameMerged);

        //create new table from file
        ServerLogger.log(VariantManager.class, "Creating new table");
        String newTableName = ProjectQueryUtil.getInstance().createVariantTable(sid,projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), false); //recreate with annotations

        //upload variants
        ServerLogger.log(VariantManager.class, "Uploading variants to table: " + newTableName);
        VariantQueryUtil.getInstance().uploadFileToVariantTable(sid,new File(outputFilenameMerged), newTableName);

        //remove temporary files
        ServerLogger.log(VariantManager.class, "Removing temp files");
        /*removeTemp(variantDump);
        removeTemp(outputFilename);
        removeTemp(outputFilenameMerged);

        for (File f : splitDir.listFiles()) {
            removeTemp(f);
        }
        splitDir.delete();*/

    }

    public static void performAddVCF(String sid, int projectId, int referenceId, int updateId) throws SQLException, IOException, Exception {

        Date now = new Date();
        String basedir = (now.getYear()+1900) + "_" + now.getMonth() + "_" + now.getDay() + "_" + now.getHours() + "_" + now.getMinutes() + "_project_" + projectId + "_reference_" + referenceId + "_" + (rand).nextInt();
        (new File(basedir)).mkdir();

        ServerLogger.log(VariantManager.class, "Creating writable directory " + basedir);

        Process p = Runtime.getRuntime().exec("chmod -R o+w " + basedir);
        p.waitFor();

        ServerLogger.log(VariantManager.class, "Adding VCFs to project=" + projectId + " reference=" + referenceId);

        String tableName = ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId);

        //dump variants from staging table
        String stagingTableName = DBSettings.getVariantStagingTableName(projectId, referenceId, updateId);
        File tempFile = new File(basedir,"temp_proj" + projectId + "_ref" + referenceId + "_update" + updateId);
        String tempFilename = tempFile.getAbsolutePath();
        ServerLogger.log(VariantManager.class, "Dumping variants to file");
        variantsToFile(sid,stagingTableName, new File(tempFilename));
        logFileSize(tempFilename);

        //sort variants
        ServerLogger.log(VariantManager.class, "Sorting variants");
        String sortedVariants = tempFilename + "_sorted";
        sortFileByPosition(tempFilename, sortedVariants);
        logFileSize(sortedVariants);

        //add custom fields
        ServerLogger.log(VariantManager.class, "Adding custom vcf fields");
        List<CustomField> customFields = ProjectQueryUtil.getInstance().getCustomVariantFields(sid,projectId);
        String vcfAnnotatedVariants = sortedVariants + "_vcf";
        addCustomVcfFields(sortedVariants, vcfAnnotatedVariants, customFields, DefaultVariantTableSchema.INDEX_OF_FILTER + 1); //last of the default fields

        //annotate
        String annotatedFilename = sortedVariants + "_annotated";
        ServerLogger.log(VariantManager.class, "File containing annotated variants, sorted by position: " + annotatedFilename);
        int[] annotationIds = AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId);
        annotateTDF(sid,vcfAnnotatedVariants, annotatedFilename, annotationIds);
        logFileSize(annotatedFilename);

        //split
        File splitDir = new File(basedir,"splitDir");
        splitDir.mkdir();
        ServerLogger.log(VariantManager.class, "Splitting annotation file into multiple files by file ID");
        splitFileOnColumn(splitDir, annotatedFilename, 1);
        String outputFilenameMerged = tempFilename + "_annotated_merged";
        ServerLogger.log(VariantManager.class, "File containing annotated variants, sorted by file: " + outputFilenameMerged);

        //merge
        ServerLogger.log(VariantManager.class, "Merging files");
        concatenateFilesInDir(splitDir, outputFilenameMerged);
        logFileSize(outputFilenameMerged);

        //create new variant table
        ServerLogger.log(VariantManager.class, "Creating new table from file");
        String newTableName = ProjectQueryUtil.getInstance().createVariantTable(sid,projectId, referenceId, updateId, AnnotationQueryUtil.getInstance().getAnnotationIds(sid,projectId, referenceId), false);

        //upload file
        ServerLogger.log(VariantManager.class, "Uploading variants to table: " + newTableName);
        VariantQueryUtil.getInstance().uploadFileToVariantTable(sid,new File(outputFilenameMerged), newTableName);

        //remove temporary files
        ServerLogger.log(VariantManager.class, "Removing temp files");
        /*removeTemp(tempFilename);
        removeTemp(annotatedFilename);
        removeTemp(sortedVariants);
        removeTemp(outputFilenameMerged);*/

        ServerLogger.log(VariantManager.class, "Annotation complete!");
    }

    private static void annotateTDF(String sid, String tdfFilename, String outputFilename, int[] annotationIds) throws Exception {
        (new VariantAnnotator(tdfFilename, outputFilename, annotationIds)).annotate(sid);
    }

    private static void appendToFile(String baseFilename, String appendingFilename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(baseFilename, true));
        BufferedReader reader = new BufferedReader(new FileReader(appendingFilename));
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line);
            writer.write("\r\n");
        }
        writer.close();
        reader.close();
    }

    private static void variantsToFile(String sid, String tableName, File file) throws SQLException {
        Connection c = (ConnectionController.connectPooled(sid));
        String query =
                "SELECT `upload_id`, `file_id`, `variant_id`, `dna_id`, `chrom`, `position`,"
                + " `dbsnp_id`, `ref`, `alt`, `qual`, `filter`, `custom_info`";
        /*for(int i = 0; i < customFields.size(); i++){
            query += "`" + customFields.get(i).getColumnName() + "`, ";
        }
        query += "`custom_info`";
        if(!annotationFields.isEmpty()) query += ", ";
        for(int i = 0; i < annotationFields.size(); i++){
            query += "`" + annotationFields.get(i).getColumnName() + "`";
            if(i != annotationFields.size()-1){
                query += ", ";
            }
        }*/
        query +=
                " INTO OUTFILE \"" + file.getAbsolutePath().replaceAll("\\\\", "/") + "\""
                + " FIELDS TERMINATED BY ',' ENCLOSED BY '\"'"
                + " LINES TERMINATED BY '\\r\\n'"
                + " FROM " + tableName;

        System.err.println(query);

        c.createStatement().executeQuery(query);
    }

//    private static void variantsToFile(String tableName, File file) throws SQLException {
//        variantsToFile(tableName, file, new ArrayList<CustomField>(), new ArrayList<CustomField>());
//    }
//
//    private static void variantsToFile(String tableName, File file, List<CustomField> customFields, List<CustomField> annotationFields) throws SQLException {
//        Connection c = (ConnectionController.connectPooled());
//        String query =
//                "SELECT `upload_id`, `file_id`, `variant_id`, `dna_id`, `chrom`, `position`,"
//                + " `dbsnp_id`, `ref`, `alt`, `qual`, `filter`, ";
//        for(int i = 0; i < customFields.size(); i++){
//            query += "`" + customFields.get(i).getColumnName() + "`, ";
//        }
//        query += "`custom_info`";
//        if(!annotationFields.isEmpty()) query += ", ";
//        for(int i = 0; i < annotationFields.size(); i++){
//            query += "`" + annotationFields.get(i).getColumnName() + "`";
//            if(i != annotationFields.size()-1){
//                query += ", ";
//            }
//        }
//        query +=
//                " INTO OUTFILE \"" + file.getAbsolutePath().replaceAll("\\\\", "/") + "\""
//                + " FIELDS TERMINATED BY ',' ENCLOSED BY '\"'"
//                + " LINES TERMINATED BY '\\r\\n'"
//                + " FROM " + tableName;
//        System.out.println(query);
//        c.createStatement().executeQuery(query);
//    }

    private static void removeTemp(String filename) {
        removeTemp(new File(filename));
    }

    private static void removeTemp(File temp) {
        if (!temp.delete()) {
            temp.deleteOnExit();
        }
    }

    private static void dropTableIfExists(String sid, String tableName) throws SQLException {
        Connection c = (ConnectionController.connectPooled(sid));
        c.createStatement().execute(
                "DROP TABLE IF EXISTS " + tableName + ";");
    }

    private static void splitFileOnColumn(File splitDir, String outputFilename, int i) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(outputFilename));

        String line = "";

        Map<String, BufferedWriter> outputFileMap = new HashMap<String, BufferedWriter>();

        BufferedWriter out;

        while ((line = br.readLine()) != null) {
            String[] parsedLine = line.split(",");

            String fileId = parsedLine[i];

            if (!outputFileMap.containsKey(fileId)) {
                outputFileMap.put(fileId, new BufferedWriter(new FileWriter(new File(splitDir, fileId))));
            }
            out = outputFileMap.get(fileId);

            out.write(line + "\n");
        }

        for (BufferedWriter bw : outputFileMap.values()) {
            bw.flush();
            bw.close();
        }
    }

    // might not be the most efficient
    private static void concatenateFilesInDir(File fromDir, String outputPath) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));

        String line;
        for (File inFile : fromDir.listFiles()) {

            if (inFile.getName().startsWith(".")) { continue; }

            line = "";

            ServerLogger.log(VariantManager.class, "Merging " + inFile.getAbsolutePath() + " with to the result file " + (new File(outputPath)).getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(inFile));
            while ((line = br.readLine()) != null) {
                bw.write(line + "\n");
            }
        }

        bw.flush();
        bw.close();

    }

    private static void sortFileByPosition(String inFile, String outfile) throws IOException, InterruptedException {
        String sortCommand = "sort -t , -k 5,5 -k 6,6n -k 7 " + ((new File(inFile)).getAbsolutePath());

        ServerLogger.log(VariantManager.class, "Sorting file: " + ((new File(inFile)).getAbsolutePath()));

        if (!(new File(inFile)).exists()) {
            throw new IOException("File not found " + ((new File(inFile)).getAbsolutePath()));
        }

        Process p = Runtime.getRuntime().exec(sortCommand);
        //p.waitFor();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        ServerLogger.log(VariantManager.class, "Writing results to file: " + ((new File(outfile)).getAbsolutePath()));

        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        String s = null;
        // read the output from the command
        while ((s = stdInput.readLine()) != null) {
            bw.write(s + "\n");
        }


        stdInput.close();
        bw.close();

        if (!(new File(outfile)).exists()) {
            throw new IOException("Problem sorting file; no output");
        }

        ServerLogger.log(VariantManager.class, "Done sorting");

    }

    private static void logFileSize(String fn) {
        ServerLogger.log(VariantManager.class, "Size of " + fn + ": " + ((new File(fn)).length()));
    }

    private static void addCustomVcfFields(String infile, String outfile, List<CustomField> customFields, int customInfoIndex) throws FileNotFoundException, IOException {

        String[] infoFields = new String[customFields.size()];
        Class[] infoClasses = new Class[customFields.size()];
        for(int i = 0; i < customFields.size(); i++){
            infoFields[i] = customFields.get(i).getColumnName();
            infoClasses[i] = customFields.get(i).getColumnClass();
        }

        BufferedReader reader = new BufferedReader(new FileReader(infile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
        String line = null;
        while((line = reader.readLine()) != null){
            String[] split = line.split(",");
            String info = split[customInfoIndex].substring(1, split[customInfoIndex].length()-1); //remove quotes

            writer.write(line + "," + VariantRecord.createTabString(VariantRecord.parseInfo(info, infoFields, infoClasses)) + "\n");
            /*String[] split = line.split(",");
            String info = split[customInfoIndex].substring(1, split[customInfoIndex].length()-1); //remove quotes

            int lengthBefore = 0;
            for(int i = 0; i < customInfoIndex; i++){
                lengthBefore += 1 + split[i].length();
            }

            line = line.substring(0, lengthBefore) + VariantRecord.createTabString(VariantRecord.parseInfo(info, infoFields, infoClasses)) + "," + line.substring(lengthBefore, line.length()) + "\n";
            writer.write(line);*/
        }
        reader.close();
        writer.close();
    }

}
