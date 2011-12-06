/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.server.update;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.server.log.ServerLogger;
import org.ut.biolab.medsavant.vcf.VariantRecord;

/**
 *
 * @author Andrew
 */
public class UpdateVariantTable {

    public static Random rand = new Random();


    public static void performUpdateAnnotations(int projectId, int referenceId, int updateId) throws SQLException, Exception {

        Date now = new Date();
        String basedir = (now.getYear()+1900) + "_" + now.getMonth() + "_" + now.getDay() + "_" + now.getHours() + "_" + now.getMinutes() + "_project_" + projectId + "_reference_" + referenceId + "_" + (rand).nextInt();
        (new File(basedir)).mkdir();

        ServerLogger.log(UpdateVariantTable.class, "Creating writable directory " + basedir);

        Process p = Runtime.getRuntime().exec("chmod -R o+w " + basedir);
        p.waitFor();

        ServerLogger.log(UpdateVariantTable.class, "Updating project=" + projectId + " reference=" + referenceId);

        String tableName = ProjectQueryUtil.getVariantTablename(projectId, referenceId);

        //dump existing variants
        File variantDumpFile = new File(basedir,"temp_proj" + projectId + "_ref" + referenceId);
        String variantDump = variantDumpFile.getAbsolutePath();
        ServerLogger.log(UpdateVariantTable.class, "Dumping variants to file: " + variantDump);
        variantsToFile(tableName, new File(variantDump));
        //variantsToFile(tableName, new File(variantDump), ProjectQueryUtil.getActualCustomVariantFields(projectId), new ArrayList<CustomField>());

        //sort variants
        ServerLogger.log(UpdateVariantTable.class, "Sorting variants");
        String sortedVariants = variantDump + "_sorted";
        sortFileByPosition(variantDump, sortedVariants);

        //add custom vcf fields
        ServerLogger.log(UpdateVariantTable.class, "Adding custom vcf fields");
        List<CustomField> customFields = ProjectQueryUtil.getCustomVariantFields(projectId);
        String vcfAnnotatedVariants = sortedVariants + "_vcf";
        addCustomVcfFields(sortedVariants, vcfAnnotatedVariants, customFields, DefaultVariantTableSchema.INDEX_OF_FILTER + 1); //last of the default fields

        //annotate
        String outputFilename = vcfAnnotatedVariants + "_annotated";
        ServerLogger.log(UpdateVariantTable.class, "File containing annotated variants, sorted by position: " + outputFilename);
        int[] annotationIds = AnnotationQueryUtil.getAnnotationIds(projectId, referenceId);
        annotateTDF(vcfAnnotatedVariants, outputFilename, annotationIds);

        //split
        File splitDir = new File(basedir,"splitDir");
        splitDir.mkdir();
        ServerLogger.log(UpdateVariantTable.class, "Splitting annotation file into multiple files by file ID");
        for (File f : splitDir.listFiles()) {
            removeTemp(f);
        }
        splitFileOnColumn(splitDir, outputFilename, 1);
        String outputFilenameMerged = outputFilename + "_merged";

        //merge
        ServerLogger.log(UpdateVariantTable.class, "File containing annotated variants, sorted by file: " + outputFilenameMerged);
        ServerLogger.log(UpdateVariantTable.class, "Merging files");
        concatenateFilesInDir(splitDir, outputFilenameMerged);

        //create new table from file
        ServerLogger.log(UpdateVariantTable.class, "Creating new table");
        String newTableName = ProjectQueryUtil.createVariantTable(projectId, referenceId, updateId, AnnotationQueryUtil.getAnnotationIds(projectId, referenceId), false); //recreate with annotations

        //upload variants
        ServerLogger.log(UpdateVariantTable.class, "Uploading variants to table: " + newTableName);
        VariantQueryUtil.uploadFileToVariantTable(new File(outputFilenameMerged), newTableName);

        //remove temporary files
        ServerLogger.log(UpdateVariantTable.class, "Removing temp files");
        /*removeTemp(variantDump);
        removeTemp(outputFilename);
        removeTemp(outputFilenameMerged);

        for (File f : splitDir.listFiles()) {
            removeTemp(f);
        }
        splitDir.delete();*/

        //drop old table
        ServerLogger.log(UpdateVariantTable.class, "Dropping old table: " + tableName);
        dropTable(tableName);

        ServerLogger.log(UpdateVariantTable.class, "Annotation complete!");
    }

    public static void performAddVCF(int projectId, int referenceId, int updateId) throws SQLException, IOException, Exception {

        Date now = new Date();
        String basedir = (now.getYear()+1900) + "_" + now.getMonth() + "_" + now.getDay() + "_" + now.getHours() + "_" + now.getMinutes() + "_project_" + projectId + "_reference_" + referenceId + "_" + (rand).nextInt();
        (new File(basedir)).mkdir();

        ServerLogger.log(UpdateVariantTable.class, "Creating writable directory " + basedir);

        Process p = Runtime.getRuntime().exec("chmod -R o+w " + basedir);
        p.waitFor();

        ServerLogger.log(UpdateVariantTable.class, "Adding VCFs to project=" + projectId + " reference=" + referenceId);

        String tableName = ProjectQueryUtil.getVariantTablename(projectId, referenceId);

        //dump variants from staging table
        String stagingTableName = DBSettings.createVariantStagingTableName(projectId, referenceId, updateId);
        File tempFile = new File(basedir,"temp_proj" + projectId + "_ref" + referenceId + "_update" + updateId);
        String tempFilename = tempFile.getAbsolutePath();
        ServerLogger.log(UpdateVariantTable.class, "Dumping variants to file");
        variantsToFile(stagingTableName, new File(tempFilename));
        logFileSize(tempFilename);

        //sort variants
        ServerLogger.log(UpdateVariantTable.class, "Sorting variants");
        String sortedVariants = tempFilename + "_sorted";
        sortFileByPosition(tempFilename, sortedVariants);
        logFileSize(sortedVariants);

        //add custom fields
        ServerLogger.log(UpdateVariantTable.class, "Adding custom vcf fields");
        List<CustomField> customFields = ProjectQueryUtil.getCustomVariantFields(projectId);
        String vcfAnnotatedVariants = sortedVariants + "_vcf";
        addCustomVcfFields(sortedVariants, vcfAnnotatedVariants, customFields, DefaultVariantTableSchema.INDEX_OF_FILTER + 1); //last of the default fields

        //annotate
        String annotatedFilename = sortedVariants + "_annotated";
        ServerLogger.log(UpdateVariantTable.class, "File containing annotated variants, sorted by position: " + annotatedFilename);
        int[] annotationIds = AnnotationQueryUtil.getAnnotationIds(projectId, referenceId);
        annotateTDF(vcfAnnotatedVariants, annotatedFilename, annotationIds);
        logFileSize(annotatedFilename);

        //split
        File splitDir = new File(basedir,"splitDir");
        splitDir.mkdir();
        ServerLogger.log(UpdateVariantTable.class, "Splitting annotation file into multiple files by file ID");
        splitFileOnColumn(splitDir, annotatedFilename, 1);
        String outputFilenameMerged = tempFilename + "_annotated_merged";
        ServerLogger.log(UpdateVariantTable.class, "File containing annotated variants, sorted by file: " + outputFilenameMerged);

        //merge
        ServerLogger.log(UpdateVariantTable.class, "Merging files");
        concatenateFilesInDir(splitDir, outputFilenameMerged);
        logFileSize(outputFilenameMerged);

        //create new variant table
        ServerLogger.log(UpdateVariantTable.class, "Creating new table from file");
        String newTableName = ProjectQueryUtil.createVariantTable(projectId, referenceId, updateId, AnnotationQueryUtil.getAnnotationIds(projectId, referenceId), false);

        //upload file
        ServerLogger.log(UpdateVariantTable.class, "Uploading variants to table: " + newTableName);
        VariantQueryUtil.uploadFileToVariantTable(new File(outputFilenameMerged), newTableName);

        //remove temporary files
        ServerLogger.log(UpdateVariantTable.class, "Removing temp files");
        /*removeTemp(tempFilename);
        removeTemp(annotatedFilename);
        removeTemp(sortedVariants);
        removeTemp(outputFilenameMerged);*/

        //remove staging tables and old table
        ServerLogger.log(UpdateVariantTable.class, "Removing staging table and old variant table");
        dropTable(stagingTableName);
        dropTable(tableName);

        ServerLogger.log(UpdateVariantTable.class, "Annotation complete!");
    }

    /*public static void performUpdateFormat(int projectId, int referenceId, int updateId) throws SQLException, IOException, Exception {

        Date now = new Date();
        String basedir = (now.getYear()+1900) + "_" + now.getMonth() + "_" + now.getDay() + "_" + now.getHours() + "_" + now.getMinutes() + "_project_" + projectId + "_reference_" + referenceId + "_" + (rand).nextInt();
        (new File(basedir)).mkdir();
        (new File(basedir)).setWritable(true);

        ServerLogger.log(UpdateVariantTable.class, "Updating project=" + projectId + " reference=" + referenceId);

        String tableName = ProjectQueryUtil.getVariantTablename(projectId, referenceId);

        //dump existing variants
        File variantDumpFile = new File(basedir,"temp_proj" + projectId + "_ref" + referenceId);
        String variantDump = variantDumpFile.getAbsolutePath();
        ServerLogger.log(UpdateVariantTable.class, "Dumping variants to file: " + variantDump);

        TableSchema table = DBUtil.importTableSchema(tableName);
        int customInfoIndex = DBUtil.getIndexOfField(table, DefaultVariantTableSchema.COLUMNNAME_OF_CUSTOM_INFO);
        List<DbColumn> columns = table.getColumns();
        List<CustomField> annotationFields = new ArrayList<CustomField>();
        for(int i = customInfoIndex+1; i < columns.size(); i++){
            annotationFields.add(new CustomField(columns.get(i).getColumnNameSQL(), "", false, "", ""));
        }
        variantsToFile(tableName, new File(variantDump), new ArrayList<CustomField>(), annotationFields);

        //add custom fields
        ServerLogger.log(UpdateVariantTable.class, "Adding custom vcf fields");
        List<CustomField> customFields = ProjectQueryUtil.getCustomVariantFields(projectId);
        String vcfAnnotatedVariants = variantDump + "_vcf";
        addCustomVcfFields(variantDump, vcfAnnotatedVariants, customFields, DefaultVariantTableSchema.INDEX_OF_FILTER + 1);

        //create new table from file
        ServerLogger.log(UpdateVariantTable.class, "Creating new table");
        String newTableName = ProjectQueryUtil.createVariantTable(projectId, referenceId, updateId, AnnotationQueryUtil.getAnnotationIds(projectId, referenceId), false); //recreate with annotations

        //upload variants
        ServerLogger.log(UpdateVariantTable.class, "Uploading variants to table: " + newTableName);
        VariantQueryUtil.uploadFileToVariantTable(new File(vcfAnnotatedVariants), newTableName);

        //remove temporary files
        ServerLogger.log(UpdateVariantTable.class, "Removing temp files");
        //TODO

        //drop old table
        ServerLogger.log(UpdateVariantTable.class, "Dropping old table: " + tableName);
        dropTable(tableName);

        ServerLogger.log(UpdateVariantTable.class, "VCF annotation complete!");

    }*/

    private static void annotateTDF(String tdfFilename, String outputFilename, int[] annotationIds) throws Exception {
        (new Annotate(tdfFilename, outputFilename, annotationIds)).annotate();
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

    private static void variantsToFile(String tableName, File file) throws SQLException {
        Connection c = (ConnectionController.connectPooled());
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

    private static void dropTable(String tableName) throws SQLException {
        Connection c = (ConnectionController.connectPooled());
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

            ServerLogger.log(UpdateVariantTable.class, "Merging " + inFile.getAbsolutePath() + " with to the result file " + (new File(outputPath)).getAbsolutePath());
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

        ServerLogger.log(UpdateVariantTable.class, "Sorting file: " + ((new File(inFile)).getAbsolutePath()));

        if (!(new File(inFile)).exists()) {
            throw new IOException("File not found " + ((new File(inFile)).getAbsolutePath()));
        }

        Process p = Runtime.getRuntime().exec(sortCommand);
        //p.waitFor();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        ServerLogger.log(UpdateVariantTable.class, "Writing results to file: " + ((new File(outfile)).getAbsolutePath()));

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

        ServerLogger.log(UpdateVariantTable.class, "Done sorting");

    }

    private static void logFileSize(String fn) {
        ServerLogger.log(UpdateVariantTable.class, "Size of " + fn + ": " + ((new File(fn)).length()));
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

