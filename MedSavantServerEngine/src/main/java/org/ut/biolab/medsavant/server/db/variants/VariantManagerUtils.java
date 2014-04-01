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

import org.ut.biolab.medsavant.server.serverapi.VariantManager;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerJob;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;

import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

/**
 *
 * @author Andrew
 */
public class VariantManagerUtils {

    private static final Log LOG = LogFactory.getLog(VariantManagerUtils.class);
    private static final int OUTPUT_LINES_LIMIT = 1000000;
    private static final int MIN_SUBSET_SIZE = 1000000; // 100000000; //bytes = 100MB

    public static final String FIELD_DELIMITER = "\t";
    public static final String ENCLOSED_BY = "\"";
    public static final String ESCAPE_CHAR = "\\";

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

    private static final String varchar = DbColumn.getTypeName(java.sql.Types.VARCHAR);

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

            //int numFields =  StringUtils.countMatches(line,VariantManagerUtils.FIELD_DELIMITER) + 1;
            String[] values = line.split(VariantManagerUtils.FIELD_DELIMITER, -1);
            if (values.length != expectedColumnCount) {
                if (warnings < maxWarningsToLog) {
                    LOG.warn("Unexpected number of columns: expected [" + expectedColumnCount + "] found [" + values.length + "] at line [" + row + "] in file [" + inputFile.getAbsolutePath() + "]");
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
        if (inputFile.exists()) {
            inputFile.delete();
        }
        return outputFile;
    }

    public static int uploadTSVFileToVariantTable(String sid, File file, String tableName) throws SQLException, IOException, SessionExpiredException {

        file = cleanVariantFile(file, tableName, sid);

        BufferedReader br = new BufferedReader(new FileReader(file));

        // TODO: for some reason the connection is closed going into this function
        Connection c = null;
        int lineNumber = 0;

        try {
            c = ConnectionController.connectPooled(sid);

            c.setAutoCommit(false);

            int chunkSize = 100000; // number of lines per chunk (100K lines = ~50MB for a standard VCF file)

            String parentDirectory = file.getParentFile().getAbsolutePath();

            BufferedWriter bw = null;
            //BufferedWriter sw = null;
            String subsetFileName = parentDirectory + "/" + MiscUtils.extractFileName(file.getAbsolutePath()) + "_subset";
            /*if (step > 0) { //assert
             sw = new BufferedWriter(new FileWriter(subsetFileName));
             } else {
             throw new IllegalArgumentException("Can't upload TSV file " + file + " to variant table, invalid step size=" + step);
             }*/

            String currentOutputPath = null;

            boolean stateOpen = false;

            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;

                // start a new output file
                if (lineNumber % chunkSize == 1) {
                    currentOutputPath = parentDirectory + "/" + MiscUtils.extractFileName(file.getAbsolutePath()) + "_" + (lineNumber / chunkSize);
                    LOG.info("Opening new partial file " + currentOutputPath);
                    bw = new BufferedWriter(new FileWriter(currentOutputPath));
                    stateOpen = true;
                }

                /*if (sw != null && ((lineNumber - 1) % step == 0)) {
                 sw.write(line + "\r\n");
                 }*/
                // write line to chunk file
                bw.write(line + "\r\n");

                // close and upload this output file
                if (lineNumber % chunkSize == 0) {
                    bw.close();

                    LOG.info("Closing and uploading final partial file " + currentOutputPath);

                    String query = "LOAD DATA LOCAL INFILE '" + currentOutputPath.replaceAll("\\\\", "/") + "' "
                            + "INTO TABLE " + tableName + " "
                            + "FIELDS TERMINATED BY '" + VariantManagerUtils.FIELD_DELIMITER + "' ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' "
                            + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "' "
                            + " LINES TERMINATED BY '\\r\\n'"
                            + ";";

                    //  LOG.info(query);
                    Statement s = null;
                    try {
                        s = c.createStatement();
                        s.setQueryTimeout(30 * 60); // 30 minutes
                        s.execute(query);
                    } finally {
                        s.close();
                    }


                    /*if (VariantManager.REMOVE_TMP_FILES) {
                     boolean deleted = new File(currentOutputPath).delete();
                     LOG.info("Deleting " + currentOutputPath + " - " + (deleted ? "successful" : "failed"));
                     }*/
                    stateOpen = false;
                    (new File(currentOutputPath)).delete();
                }
            }

            // write the remaining open file
            if (bw != null && stateOpen) {
                bw.close();
                String query = "LOAD DATA LOCAL INFILE '" + currentOutputPath.replaceAll("\\\\", "/") + "' "
                        + "INTO TABLE " + tableName + " "
                        + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.FIELD_DELIMITER) + "' ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' "
                        + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "'"
                        + " LINES TERMINATED BY '\\r\\n'"
                        + ";";

                LOG.info("Closing and uploading last partial file " + currentOutputPath);

                LOG.info(query);
                Statement s = null;
                try {
                    s = c.createStatement();
                    s.setQueryTimeout(60 * 60); // 1 hour
                    s.execute(query);                    
                } finally {
                    s.close();
                }

                (new File(currentOutputPath)).delete();
                /*if (VariantManager.REMOVE_TMP_FILES) {
                 boolean deleted = new File(currentOutputPath).delete();
                 LOG.info("Deleting " + currentOutputPath + " - " + (deleted ? "successful" : "failed"));
                 }*/
            }
            LOG.info("Imported " + lineNumber + " lines of variants in total");
            /*
             if (sw != null) {
             sw.close();

             String query = "LOAD DATA LOCAL INFILE '" + subsetFileName.replaceAll("\\\\", "/") + "' "
             + "INTO TABLE " + subTableName + " "
             + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.FIELD_DELIMITER) + "' ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' "
             + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "'"
             + " LINES TERMINATED BY '\\r\\n'"
             + ";";

             LOG.info("Closing and uploading subset file " + subsetFileName);
             LOG.info(query);
             Statement s = c.createStatement();
             s.setQueryTimeout(60 * 60); // 1 hour
             s.execute(query);
             LOG.info("Subset table import done");
             (new File(subsetFileName)).delete();
             }
             */
            c.commit();
            c.setAutoCommit(true);
        } finally {
            c.close();
        }


        /*if (VariantManager.REMOVE_TMP_FILES) {
         boolean deleted = file.delete();
         LOG.info("Deleting " + file.getAbsolutePath() + " - " + (deleted ? "successful" : "failed"));
         }*/
        return lineNumber;
    }

    @Deprecated
    public static int uploadTSVFileToVariantTableOld(String sid, File file, String tableName) throws SQLException, IOException, SessionExpiredException {

        file = cleanVariantFile(file, tableName, sid);

        BufferedReader br = new BufferedReader(new FileReader(file));

        // TODO: for some reason the connection is closed going into this function
        Connection c = ConnectionController.connectPooled(sid);

        c.setAutoCommit(false);

        int chunkSize = 100000; // number of lines per chunk (100K lines = ~50MB for a standard VCF file)
        int lineNumber = 0;

        BufferedWriter bw = null;
        String currentOutputPath = null;

        boolean stateOpen = false;

        String parentDirectory = file.getParentFile().getAbsolutePath();

        String line;
        while ((line = br.readLine()) != null) {
            lineNumber++;

            // start a new output file
            if (lineNumber % chunkSize == 1) {
                currentOutputPath = parentDirectory + "/" + MiscUtils.extractFileName(file.getAbsolutePath()) + "_" + (lineNumber / chunkSize);
                LOG.info("Opening new partial file " + currentOutputPath);
                bw = new BufferedWriter(new FileWriter(currentOutputPath));
                stateOpen = true;
            }

            // write line to chunk file
            bw.write(line + "\r\n");

            // close and upload this output file
            if (lineNumber % chunkSize == 0) {
                bw.close();

                LOG.info("Closing and uploading partial file " + currentOutputPath);

                String query = "LOAD DATA LOCAL INFILE '" + currentOutputPath.replaceAll("\\\\", "/") + "' "
                        + "INTO TABLE " + tableName + " "
                        + "FIELDS TERMINATED BY '" + VariantManagerUtils.FIELD_DELIMITER + "' ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' "
                        + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "' "
                        + " LINES TERMINATED BY '\\r\\n'"
                        + ";";

                //  LOG.info(query);
                Statement s = c.createStatement();
                s.setQueryTimeout(30 * 60); // 30 minutes
                s.execute(query);

                /*if (VariantManager.REMOVE_TMP_FILES) {
                 boolean deleted = new File(currentOutputPath).delete();
                 LOG.info("Deleting " + currentOutputPath + " - " + (deleted ? "successful" : "failed"));
                 }*/
                stateOpen = false;
            }
        }

        // write the remaining open file
        if (bw != null && stateOpen) {
            bw.close();
            String query = "LOAD DATA LOCAL INFILE '" + currentOutputPath.replaceAll("\\\\", "/") + "' "
                    + "INTO TABLE " + tableName + " "
                    + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.FIELD_DELIMITER) + "' ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' "
                    + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "'"
                    + " LINES TERMINATED BY '\\r\\n'"
                    + ";";

            LOG.info("Closing and uploading last partial file " + currentOutputPath);

            LOG.info(query);
            Statement s = c.createStatement();
            s.setQueryTimeout(60 * 60); // 1 hour
            s.execute(query);

            /*if (VariantManager.REMOVE_TMP_FILES) {
             boolean deleted = new File(currentOutputPath).delete();
             LOG.info("Deleting " + currentOutputPath + " - " + (deleted ? "successful" : "failed"));
             }*/
        }

        LOG.info("Imported " + lineNumber + " lines of variants in total");

        c.commit();
        c.setAutoCommit(true);

        c.close();

        /*if (VariantManager.REMOVE_TMP_FILES) {
         boolean deleted = file.delete();
         LOG.info("Deleting " + file.getAbsolutePath() + " - " + (deleted ? "successful" : "failed"));
         }*/
        return lineNumber;
    }

    public static void variantTableToTSVFile(String sid, String tableName, File file) throws SQLException, SessionExpiredException {
        variantTableToTSVFile(sid, tableName, file, null);
    }

    public static void variantTableToTSVFile(String sid, String tableName, File file, String conditions) throws SQLException, SessionExpiredException {

        String query = "SELECT `upload_id`, `file_id`, `variant_id`, `dna_id`, `chrom`, `position`, `end`, `dbsnp_id`, `ref`, `alt`, `alt_number`, `qual`, `filter`, `variant_type`, `zygosity`, `gt`, `custom_info`";

        query += " INTO OUTFILE \"" + file.getAbsolutePath().replaceAll("\\\\", "/") + "\" "
                + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(FIELD_DELIMITER) + "' ENCLOSED BY '\"' "
                + "ESCAPED BY '" + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "' "
                //+ " LINES TERMINATED BY '\\r\\n'"
                + "FROM " + tableName;

        if (conditions != null && conditions.length() > 1) {
            query += " WHERE " + conditions;
        }

        LOG.info(query);

        ConnectionController.executeQuery(sid, query);
    }

    public static void removeTemp(String filename) {
        removeTemp(new File(filename));
    }

    public static void removeTemp(File temp) {
        if (!temp.delete()) {
            temp.deleteOnExit();
        }
    }

    public static void dropViewIfExists(String sessID, String tableName) throws SQLException, SessionExpiredException {
        ConnectionController.executeUpdate(sessID, "DROP VIEW IF EXISTS " + tableName);
    }

    public static void dropTableIfExists(String sessID, String tableName) throws SQLException, SessionExpiredException {
        ConnectionController.executeUpdate(sessID, "DROP TABLE IF EXISTS " + tableName);
    }

    static File[] splitFileOnColumns(File splitDir, String inputFile, int[] cols) throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new FileReader(inputFile));

        String line;

        Map<String, BufferedWriter> outputFileMap = new HashMap<String, BufferedWriter>();

        BufferedWriter out;

        boolean conservingFilePointers = false;
        int filesOpened = 0;

        Set<String> filePaths = new HashSet<String>();

        int lines = 0;
        while ((line = br.readLine()) != null) {
            lines++;

            String[] parsedLine = line.split(FIELD_DELIMITER + "");

            String id = "";
            for (int i : cols) {
                // LOG.info("Trying to parse column "+i+" from line "+line+" in file "+inputFile);
                id += parsedLine[i] + "-";
            }

            if (!outputFileMap.containsKey(id)) {
                File f = new File(splitDir, (id + "part").replace("\"", ""));
                outputFileMap.put(id, new BufferedWriter(new FileWriter(f, true)));

                String path = f.getAbsolutePath();
                filesOpened++;
                if (!filePaths.contains(path)) {
                    //LOG.info("Opening split file " + path + " with conserving file pointers " + conservingFilePointers + ", opened " + filePaths.size() + " files so far");
                    filePaths.add(path);
                }
            }

            out = outputFileMap.get(id);
            out.write(line + "\n");

            if (outputFileMap.keySet().size() >= MAX_FILES) {
                if (!conservingFilePointers) {
                    LOG.info("Conserving file pointers, " + outputFileMap.keySet().size() + " opened");
                }
                conservingFilePointers = true;

                out.close();
                outputFileMap.remove(id);
            }
        }

        LOG.info("Closing split files...");

        for (BufferedWriter bw : outputFileMap.values()) {
            bw.close();
        }

        LOG.info("Split " + lines + " lines into " + filePaths.size() + " files, in " + splitDir.getAbsolutePath());

        File[] fileArray = new File[filePaths.size()];
        int counter = 0;
        for (String path : filePaths) {
            File f = new File(path);
            fileArray[counter++] = f;
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
        //LOG.info("Size of " + fn + ": " + ((new File(fn)).length()));
    }

    public static void addCustomVCFFields(String infile, String outfile, CustomField[] customFields, int customInfoIndex) throws FileNotFoundException, IOException {

        //System.out.println("Adding custom VCF fields infile=" + infile + " oufile=" + outfile + " customInfoIndex=" + customInfoIndex);
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
            String info = split[customInfoIndex].substring(1, split[customInfoIndex].length() - 1); //remove quotes
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

    /*
     @Deprecated
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
     */
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

    static int MAX_FILES = 20;

    /**
     * This function can BLOCK for a long time.
     *
     * @return
     * @throws Exception
     */
    public static TSVFile[] annotateTSVFiles(String sessID, File[] tsvFiles, Annotation[] annotations, CustomField[] customFields, MedSavantServerJob parentJob) throws Exception {
        LOG.info("Annotating " + tsvFiles.length + " TSV files");

        try {
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                    sessID,
                    LogManagerAdapter.LogType.INFO,
                    "Annotating " + tsvFiles.length + " TSV files");
        } catch (RemoteException ex) {
        } catch (SessionExpiredException ex) {
        }

        List<MedSavantServerJob> annotationThreads = new ArrayList<MedSavantServerJob>(tsvFiles.length);

        for (int i = 0; i < tsvFiles.length; i++) {
            File toAnnotate = tsvFiles[i];
            String outFile = toAnnotate + "_annotated";
            //LOG.info("\tDEBUG: Adding annotation thread on file "+outFile);
            annotationThreads.add(new VariantAnnotator(sessID, parentJob, toAnnotate, new File(outFile), annotations, customFields));
        }
        //LOG.info("DEBUG: Using executor service to invoke "+annotationThreads.size()+" threads");
        //MedSavantServerEngine.getLongExecutorService().invokeAll(annotationThreads);
        MedSavantServerEngine.submitLongJobsAndWait(annotationThreads);
        //LOG.info("DEBUG: All annotation threads done");

        TSVFile[] annotatedTsvFiles = new TSVFile[annotationThreads.size()];
        //File[] annotatedTsvFiles = new File[annotationThreads.size()];

        int i = 0;
        for (MedSavantServerJob msj : annotationThreads) {
            VariantAnnotator t = (VariantAnnotator) msj;
            File f = new File(t.getOutputFilePath());
            annotatedTsvFiles[i++] = new TSVFile(f, t.getNumVariantsWritten());

            if (!t.didSucceed()) {
                LOG.info("At least one annotation thread errored out");
                LOG.info("Failed annotating file " + f.getAbsolutePath());
                throw t.getException();
            } else {
                LOG.info("Annotated file " + f.getAbsolutePath());
            }
        }

        return annotatedTsvFiles;
    }

    static File[] splitTSVFileByFileAndDNAID(File basedir, File tsvFile) throws FileNotFoundException, IOException {
        File splitDir = new File(basedir, "split-by-id");
        splitDir.mkdir();
        LOG.info("Splitting " + tsvFile + " by DNA and FileIDs");
        return VariantManagerUtils.splitFileOnColumns(splitDir, tsvFile.getAbsolutePath(), new int[]{0, 1, 3});
    }

}
