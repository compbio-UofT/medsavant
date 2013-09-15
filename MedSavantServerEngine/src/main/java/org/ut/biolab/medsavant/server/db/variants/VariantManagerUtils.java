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

package org.ut.biolab.medsavant.server.db.variants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.shard.db.ShardedDatabaseSetupHelper;
import org.ut.biolab.medsavant.shard.file.FileUtils;
import org.ut.biolab.medsavant.shard.variant.ShardedVariantManagerUtilsHelper;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

/**
 * 
 * @author Andrew
 */
public class VariantManagerUtils {

    private static final Log LOG = LogFactory.getLog(VariantManagerUtils.class);
    private static final int OUTPUT_LINES_LIMIT = 1000000;
    private static final int MIN_SUBSET_SIZE = 1000000; // 100000000; //bytes =
                                                        // 100MB
    public static final String FIELD_DELIMITER = "\t";
    public static final String ENCLOSED_BY = "\"";
    public static final String ESCAPE_CHAR = "\\";

    private static final String varchar = DbColumn.getTypeName(java.sql.Types.VARCHAR);
    private static ShardedDatabaseSetupHelper setupHelper = new ShardedDatabaseSetupHelper();
    private static ShardedVariantManagerUtilsHelper shardedHelper = new ShardedVariantManagerUtilsHelper();
    private static VariantManagerUtilsHelper unshardedHelper = new VariantManagerUtilsHelper();

    public static void appendToFile(String baseFilename, String appendingFilename) throws IOException {
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

    private static boolean isVarchar(String typeNameSQL) {
        return typeNameSQL.equals(varchar);
    }

    private static File cleanVariantFile(File inputFile, String tableName, String sessionId) throws SQLException, RemoteException, IOException, SessionExpiredException {

        LOG.info("Cleaning file " + inputFile.getAbsolutePath() + " for table " + tableName);

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessionId, tableName);
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        File outputFile = new File(inputFile.getCanonicalPath() + "_clean");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line;
        int expectedColumnCount = table.getColumns().size();
        long row = 0;
        int[] expectedColumnLengths = new int[table.getColumns().size()];
        LOG.info("Cleaning " + expectedColumnCount + " fields...");
        for (int i = 0; i < expectedColumnCount; ++i) {
            if (isVarchar(table.getColumns().get(i).getTypeNameSQL())) {
                expectedColumnLengths[i] = table.getColumns().get(i).getTypeLength();
            } else {
                expectedColumnLengths[i] = Integer.MAX_VALUE;
            }
        }

        int announceEvery = 100000;

        LOG.info("Cleaning " + inputFile.getAbsolutePath());

        int maxWarningsToLog = 30;
        int warnings = 0;
        while ((line = br.readLine()) != null) {
            ++row;

            if (row % announceEvery == 0) {
                LOG.info("Cleaned " + row + " lines of " + inputFile.getAbsolutePath());
            }

            // int numFields =
            // StringUtils.countMatches(line,VariantManagerUtils.FIELD_DELIMITER)
            // + 1;
            String[] values = line.split(VariantManagerUtils.FIELD_DELIMITER, -1);
            if (values.length != expectedColumnCount) {
                if (warnings < maxWarningsToLog) {
                    LOG.warn("Unexpected number of columns: expected [" + expectedColumnCount + "] found [" + values.length + "] at line [" + row + "] in file ["
                            + inputFile.getAbsolutePath() + "]");
                } else if (warnings == maxWarningsToLog) {
                    LOG.warn(maxWarningsToLog + "+ warnings");
                }
                warnings++;
                continue;
            }

            for (int i = 0; i < expectedColumnCount; ++i) {

                // truncate fields that are too long
                int lengthOfField = values[i].length() - 2;
                if (lengthOfField >= expectedColumnLengths[i]) {
                    LOG.warn("Value too long: [" + values[i] + "]; trimmed to [" + expectedColumnLengths[i] + "] characters");
                    String unenclosed = values[i].replace(VariantManagerUtils.ENCLOSED_BY, "");
                    values[i] = VariantManagerUtils.ENCLOSED_BY + unenclosed.substring(0, expectedColumnLengths[i]) + VariantManagerUtils.ENCLOSED_BY;
                }
            }

            bw.append(StringUtils.join(values, VariantManagerUtils.FIELD_DELIMITER));
            bw.append("\n");
        }

        LOG.info("Done cleaning " + inputFile.getAbsolutePath() + " output to " + outputFile.getAbsolutePath());
        LOG.warn(warnings + " warnings while cleaning");

        bw.close();
        br.close();
        return outputFile;
    }

    public static void uploadTSVFileToVariantTable(String sid, File file, String tableName) throws SQLException, IOException, SessionExpiredException {
        file = cleanVariantFile(file, tableName, sid);

        shardedHelper.uploadTSVFileToVariantTable(file, tableName, FIELD_DELIMITER, ENCLOSED_BY, ESCAPE_CHAR);
    }

    public static void variantTableToTSVFile(String sid, String tableName, File file, String conditions, boolean complete, int step) throws SQLException, SessionExpiredException {
        String query;

        if (complete) {
            query = "SELECT *";
        } else {
            query = "SELECT `upload_id`, `file_id`, `variant_id`, `dna_id`, `chrom`, `position`, `dbsnp_id`, `ref`, `alt`, `qual`, `filter`, `variant_type`, `zygosity`, `gt`, `custom_info`";
        }

        query += " INTO OUTFILE \"" + FileUtils.getParametrizedFilePath(file) + "\" " + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(FIELD_DELIMITER)
                + "' ENCLOSED BY '\"' " + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "' "
                // + " LINES TERMINATED BY '\\r\\n'"
                + "FROM " + tableName;

        if (step > 1) {
            if (conditions != null && conditions.length() > 1) {
                conditions += " AND `variant_id`%" + step + "=0";
            } else {
                conditions = "`variant_id`%" + step + "=0";
            }
        }

        if (conditions != null && conditions.length() > 1) {
            query += " WHERE " + conditions;
        }

        // export
        LOG.info(query);
        shardedHelper.exportVariantTablesToSingleFile(file, query);
    }

    public static void removeTemp(String filename) {
        removeTemp(new File(filename));
    }

    public static void removeTemp(File temp) {
        if (!temp.delete()) {
            temp.deleteOnExit();
        }
    }

    public static void dropTableIfExists(String sessID, String tableName) throws SQLException, SessionExpiredException {
        ConnectionController.executeUpdate(sessID, "DROP TABLE IF EXISTS " + tableName);

        // remove table from shards as well
        setupHelper.dropVariantTables(tableName);
    }

    static File[] splitFileOnColumns(File splitDir, String outputFilename, int[] cols) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(outputFilename));

        String line;

        Map<String, BufferedWriter> outputFileMap = new HashMap<String, BufferedWriter>();

        BufferedWriter out;

        List<File> files = new ArrayList<File>();
        while ((line = br.readLine()) != null) {
            String[] parsedLine = line.split(FIELD_DELIMITER + "");

            String id = "";
            for (int i : cols) {
                id += parsedLine[i] + "-";
            }

            if (!outputFileMap.containsKey(id)) {
                File f = new File(splitDir, (id + "part").replace("\"", ""));
                LOG.info("Creating split file " + f.getAbsolutePath());
                files.add(f);
                outputFileMap.put(id, new BufferedWriter(new FileWriter(f)));
            }
            out = outputFileMap.get(id);

            out.write(line + "\n");
        }

        for (BufferedWriter bw : outputFileMap.values()) {
            bw.flush();
            bw.close();
        }

        File[] fileArray = new File[files.size()];
        for (int i = 0; i < fileArray.length; i++) {
            File f = files.get(i);
            LOG.info("Closing split file " + f.getAbsolutePath());
            fileArray[i] = f;
        }

        return fileArray;
    }

    public static void splitFileOnColumn(File splitDir, String outputFilename, int i) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(outputFilename));

        String line;

        Map<String, BufferedWriter> outputFileMap = new HashMap<String, BufferedWriter>();

        BufferedWriter out;

        while ((line = br.readLine()) != null) {
            String[] parsedLine = line.split(FIELD_DELIMITER + "");

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
    public static void concatenateFilesInDir(File fromDir, String outputPath) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));

        String line;
        for (File inFile : fromDir.listFiles()) {

            if (inFile.getName().startsWith(".")) {
                continue;
            }

            LOG.info("Merging " + inFile.getAbsolutePath() + " with to the result file " + (new File(outputPath)).getAbsolutePath());
            BufferedReader br = new BufferedReader(new FileReader(inFile));
            while ((line = br.readLine()) != null) {
                bw.write(line + "\n");
            }
        }

        bw.flush();
        bw.close();

    }

    public static void sortFileByPosition(String inFile, String outfile) throws IOException, InterruptedException {
        String sortCommand = "sort -k 5,5 -k 6,6n -k 7 " + ((new File(inFile)).getAbsolutePath());

        LOG.info("Sorting file: " + (new File(inFile).getAbsolutePath()) + " to " + outfile);

        if (!(new File(inFile)).exists()) {
            throw new IOException("File not found " + (new File(inFile).getAbsolutePath()));
        }

        Process p = Runtime.getRuntime().exec(sortCommand);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        String s;
        // read the output from the command
        while ((s = stdInput.readLine()) != null) {
            bw.write(s + "\n");
        }

        stdInput.close();
        bw.close();

        if (!(new File(outfile)).exists()) {
            throw new IOException("Problem sorting file; no output");
        }

        LOG.info("Done sorting");

    }

    public static void logFileSize(String fn) {
        LOG.info("Size of " + fn + ": " + ((new File(fn)).length()));
    }

    public static void addCustomVCFFields(String infile, String outfile, CustomField[] customFields, int customInfoIndex) throws FileNotFoundException, IOException {

        // System.out.println("Adding custom VCF fields infile=" + infile +
        // " oufile=" + outfile + " customInfoIndex=" + customInfoIndex);

        String[] infoFields = new String[customFields.length];
        Class[] infoClasses = new Class[customFields.length];
        for (int i = 0; i < customFields.length; i++) {
            infoFields[i] = customFields[i].getColumnName();
            infoClasses[i] = customFields[i].getColumnClass();
        }

        BufferedReader reader = new BufferedReader(new FileReader(infile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split = line.split(FIELD_DELIMITER + "");
            String info = split[customInfoIndex].substring(1, split[customInfoIndex].length() - 1); // remove
                                                                                                    // quotes
            writer.write(line + FIELD_DELIMITER + VariantRecord.createTabString(VariantRecord.parseInfo(info, infoFields, infoClasses)) + "\n");
        }
        reader.close();
        writer.close();
    }

    public static void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException, RemoteException, SessionExpiredException {
        try {
            VariantManager.getInstance().addTagsToUpload(sid, uploadID, variantTags);
        } catch (SQLException e) {
            throw new SQLException("Error adding tags", e);
        }
    }

    public static int determineStepForSubset(long length) {
        int step;
        if (length <= MIN_SUBSET_SIZE) {
            step = 1;
        } else {
            long targetSize = Math.max(MIN_SUBSET_SIZE, length / 1000);
            step = (int) Math.ceil((double) length / (double) targetSize);
        }
        return step;
    }

    public static void generateSubset(File inFile, File outFile) throws IOException, InterruptedException {

        System.out.println("generate subset");
        long length = inFile.length();
        int step;
        if (length <= MIN_SUBSET_SIZE) {
            step = 1;
        } else {
            long targetSize = Math.max(MIN_SUBSET_SIZE, length / 1000);
            step = (int) Math.ceil((double) length / (double) targetSize);
        }

        System.out.println("length: " + length + "  step: " + step);

        BufferedReader in = new BufferedReader(new FileReader(inFile));
        BufferedWriter out = new BufferedWriter(new FileWriter(outFile));

        int i = 1;
        String line;
        while ((line = in.readLine()) != null) {
            if (i >= step) {
                out.write(line + "\n");
                i = 1;
            } else {
                i++;
            }
        }

        in.close();
        out.close();
    }

    public static File[] convertVCFFilesToTSV(File[] vcfFiles, File outDir, int updateID, boolean includeHomozygousReferenceCalls) throws Exception {

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
            t.start();
        }

        // wait for all the threads to finish
        LOG.info("Waiting for parsing threads to finish...");
        for (Thread t : threads) {
            t.join();
        }

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

    public static File[] annotateTSVFiles(String sessID, File[] tsvFiles, Annotation[] annotations, CustomField[] customFields) throws Exception {

        LOG.info("Annotating " + tsvFiles.length + " TSV files");

        VariantAnnotator[] annotationThreads = new VariantAnnotator[tsvFiles.length];

        for (int i = 0; i < annotationThreads.length; i++) {
            File toAnnotate = tsvFiles[i];
            String outFile = toAnnotate + "_annotated";
            LOG.info("Queueing annotation of " + toAnnotate.getAbsolutePath());
            VariantAnnotator t = new VariantAnnotator(sessID, toAnnotate, new File(outFile), annotations, customFields);
            t.start();
            annotationThreads[i] = t;
        }

        // wait for all the threads to finish
        LOG.info("Waiting for annotation threads to finish...");
        for (Thread t : annotationThreads) {
            t.join();
        }
        LOG.info("All annotation threads done");

        File[] annotatedTsvFiles = new File[annotationThreads.length];

        int i = 0;
        for (VariantAnnotator t : annotationThreads) {
            File f = new File(t.getOutputFilePath());
            annotatedTsvFiles[i++] = f;
            LOG.info("Annotated file " + f.getAbsolutePath());
            if (!t.didSucceed()) {
                LOG.info("At least one annotation thread errored out");
                throw t.getException();
            }
        }

        return annotatedTsvFiles;
    }

    static File[] splitTSVFileByFileAndDNAID(File basedir, File tsvFile) throws FileNotFoundException, IOException {
        File splitDir = new File(basedir, "split-by-id");
        splitDir.mkdir();
        LOG.info("Splitting " + tsvFile + " by DNA and FileIDs");
        return VariantManagerUtils.splitFileOnColumns(splitDir, tsvFile.getAbsolutePath(), new int[] { 0, 1, 3 });
    }

}
