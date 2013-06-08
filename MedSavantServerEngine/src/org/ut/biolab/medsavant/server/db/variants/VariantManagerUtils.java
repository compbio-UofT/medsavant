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

import java.io.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.format.CustomField;
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

    public static void variantsToFile(String sid, String tableName, File file, String conditions, boolean complete, int step) throws SQLException {
        String query;

        if (complete) {
            query = "SELECT *";
        } else {
            query = "SELECT `upload_id`, `file_id`, `variant_id`, `dna_id`, `chrom`, `position`, `dbsnp_id`, `ref`, `alt`, `qual`, `filter`, `variant_type`, `zygosity`, `gt`, `custom_info`";
        }

        query += " INTO OUTFILE \"" + file.getAbsolutePath().replaceAll("\\\\", "/") + "\" "
                + "FIELDS TERMINATED BY '" + StringEscapeUtils.escapeJava(FIELD_DELIMITER) + "' ENCLOSED BY '\"' "
                + "ESCAPED BY '"+StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR)+"' "
                //+ " LINES TERMINATED BY '\\r\\n'"
                + "FROM " + tableName;


        if (step > 1) {
            if (conditions != null && conditions.length()> 1) {
                conditions += " AND `variant_id`%" + step + "=0";
            } else {
                conditions = "`variant_id`%" + step + "=0";
            }
        }

        if (conditions != null && conditions.length()> 1) {
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

    public static void dropTableIfExists(String sessID, String tableName) throws SQLException {
        ConnectionController.executeUpdate(sessID, "DROP TABLE IF EXISTS " + tableName);
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

            if (inFile.getName().startsWith(".")) { continue; }

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
            String info = split[customInfoIndex].substring(1, split[customInfoIndex].length()-1); //remove quotes
            writer.write(line + FIELD_DELIMITER + VariantRecord.createTabString(VariantRecord.parseInfo(info, infoFields, infoClasses)) + "\n");
        }
        reader.close();
        writer.close();
    }

    public static void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException, RemoteException {
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
            step = (int)Math.ceil((double)length / (double)targetSize);
        }
        return step;
    }

    public static void generateSubset(File inFile, File outFile) throws IOException, InterruptedException{

        System.out.println("generate subset");
        long length = inFile.length();
        int step;
        if (length <= MIN_SUBSET_SIZE) {
            step = 1;
        } else {
            long targetSize = Math.max(MIN_SUBSET_SIZE, length / 1000);
            step = (int)Math.ceil((double)length / (double)targetSize);
        }

        System.out.println("length: " + length + "  step: " + step);

        BufferedReader in = new BufferedReader(new FileReader(inFile));
        BufferedWriter out = new BufferedWriter(new FileWriter(outFile));

        int i = 1;
        String line;
        while((line = in.readLine()) != null) {
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


}