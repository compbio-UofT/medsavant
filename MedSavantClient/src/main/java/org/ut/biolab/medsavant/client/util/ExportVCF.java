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
package org.ut.biolab.medsavant.client.util;

import com.healthmarketscience.sqlbuilder.Condition;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;
import org.ut.biolab.medsavant.shared.util.IOUtils;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

/**
 * @author Andrew
 */
public class ExportVCF implements BasicVariantColumns {

    private static final Log LOG = LogFactory.getLog(ExportVCF.class);

    // important locations in intermediate file created pre-merge
    private static int INTERMEDIATE_INDEX_DNA_ID = 0;

    private static int INTERMEDIATE_INDEX_GENOTYPE = 1;

    private static int INTERMEDIATE_INDEX_CHROM = 2;

    private static int INTERMEDIATE_INDEX_POSITION = 3;

    private static int INTERMEDIATE_INDEX_DBSNP = 4;

    private static int INTERMEDIATE_INDEX_REF = 5;

    private static int INTERMEDIATE_INDEX_ALT = 6;

    private static int INTERMEDIATE_INDEX_QUAL = 7;

    private static int INTERMEDIATE_INDEX_FILTER = 8;

    private static int INTERMEDIATE_INDEX_CUSTOM = 9; // and on

    public static File exportTDF(File destFile) throws Exception {
        return exportTDF(destFile, null, FilterController.getInstance().getAllFilterConditions());
    }

    public static File exportVCF(File destFile) throws Exception {
         return exportVCF(destFile, null, FilterController.getInstance().getAllFilterConditions());
    }

    public static File exportTDF(File destFile, MedSavantWorker worker, Condition[][] conditions) throws Exception {
        System.out.println("Requesting table export from server...");
        int fileID
                = MedSavantClient.VariantManager.exportVariants(LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance()
                        .getCurrentReferenceID(), conditions, false, true);

        if (worker != null) {
            if (worker.isCancelled()) {
                throw new InterruptedException();
            }
            worker.showProgress(0.5);
        }

        System.out.println("Transferring export from server to " + destFile.getAbsolutePath() + " ...");
        ClientNetworkUtils.moveFileFromServer(fileID, destFile);
        System.out.println("Table transferred");

        File resultingFile;

        if (IOUtils.isZipped(destFile)) {
            resultingFile = IOUtils.unzipFile(destFile, destFile.getParentFile().getAbsolutePath()).get(0);
            destFile.delete();
        } else {
            resultingFile = destFile;
        }

        if (worker != null) {
            worker.showProgress(1);
        }

        return resultingFile;
    }

    public static File exportVCF(File destFile, MedSavantWorker worker, Condition[][] conditions) throws Exception {
        int fileID
                = MedSavantClient.VariantManager.exportVariants(LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance()
                        .getCurrentReferenceID(), conditions, true, false);

        if (worker != null) {
            if (worker.isCancelled()) {
                throw new InterruptedException();
            }
            worker.showProgress(0.5);
        }

        LOG.info("Copying file " + fileID + " from sever to " + destFile.getAbsolutePath());
        ClientNetworkUtils.moveFileFromServer(fileID, destFile);
        LOG.info("Done copying file to " + destFile);

        // maintain lists of chrs, dnaids, ...
        Map<String, BufferedWriter> out = new HashMap<String, BufferedWriter>();
        Map<String, File> files = new HashMap<String, File>();
        List<String> chrs = new ArrayList<String>();
        Set<String> dnaIds = new HashSet<String>();

        // info fields
        TableSchema table = ProjectController.getInstance().getCurrentVariantTableSchema();
        String[] customColumnNames = new String[table.getNumFields() - INDEX_OF_CUSTOM_INFO - 1];
        List<DbColumn> allColumns = table.getColumns();

        for (int i = INDEX_OF_CUSTOM_INFO + 1; i < table.getNumFields(); i++) {
            customColumnNames[i - 1 - INDEX_OF_CUSTOM_INFO] = allColumns.get(i).getColumnNameSQL().toUpperCase();
        }
        int infoMin = INDEX_OF_CUSTOM_INFO + 1;
        int infoMax = table.getNumFields();

        BufferedReader in = new BufferedReader(new FileReader(destFile));

        double numSteps = ReferenceController.getInstance().getChromosomes().length * 6;
        String line;

        while ((line = in.readLine()) != null) {
            // parse row         
            //String[] record = line.split(",");
            String[] record = line.split("\t");
            String row = "";

            // dna id
            String dnaId = cleanField(record[BasicVariantColumns.INDEX_OF_DNA_ID]);
            row += dnaId + "\t";
            dnaIds.add(dnaId);

            // genotype
            row += cleanField(record[BasicVariantColumns.INDEX_OF_GT]) + "\t";

            // default fields
            row
                    += cleanField(record[BasicVariantColumns.INDEX_OF_CHROM]) + "\t"
                    + cleanField(record[BasicVariantColumns.INDEX_OF_START_POSITION]) + "\t"
                    + parseMandatoryField(cleanField(record[BasicVariantColumns.INDEX_OF_DBSNP_ID])) + "\t"
                    + parseMandatoryField(cleanField(record[BasicVariantColumns.INDEX_OF_REF])) + "\t"
                    + parseMandatoryField(cleanField(record[BasicVariantColumns.INDEX_OF_ALT])) + "\t"
                    + parseMandatoryField(cleanField(record[BasicVariantColumns.INDEX_OF_QUAL])) + "\t"
                    + parseMandatoryField(cleanField(record[BasicVariantColumns.INDEX_OF_FILTER])) + "\t";

            // extra fields
            for (int j = infoMin; j < infoMax; j++) {
                if (j < record.length) {
                    row += cleanField(record[j]);
                }
                if (j != infoMax - 1) {
                    row += "\t";
                }
            }

            // get writer for chrom, or create
            BufferedWriter writer = out.get(cleanField(record[BasicVariantColumns.INDEX_OF_CHROM]));
            if (writer == null) {
                String chrom = cleanField(record[BasicVariantColumns.INDEX_OF_CHROM]);
                File f = new File(DirectorySettings.getTmpDirectory() + File.separator + destFile.getName() + chrom);
                writer = new BufferedWriter(new FileWriter(f, false));
                out.put(chrom, writer);
                chrs.add(chrom);
                files.put(chrom, f);
                if (worker != null) {
                    worker.showProgress(files.size() / numSteps + 0.5);
                }
            }

            // write record
            writer.write(row + "\n");
            if (worker != null) {
                if (worker.isCancelled()) {
                    throw new InterruptedException();
                }
            }
        }

        // close writers
        for (String key : out.keySet()) {
            out.get(key).close();
        }

        // concatenate separate chrom files in proper order
        Collections.sort(chrs, new ChromosomeComparator());
        File temp1 = new File(DirectorySettings.getTmpDirectory() + File.separator + destFile.getName() + "_complete");
        for (int i = 0; i < files.size(); i++) {
            copyFile(files.get(chrs.get(i)), temp1, i != 0);

            if (worker != null) {
                if (worker.isCancelled()) {
                    throw new InterruptedException();
                }
                worker.showProgress((i + files.size()) / numSteps + 0.5);
            }
        }

        // merge vcf to remove duplicates
        mergeVCF(temp1, destFile, dnaIds, customColumnNames);

        if (worker != null) {
            worker.showProgress(1.0);

        }
        
        return destFile;
    }

    private static String parseMandatoryField(String s) {
        return (s == null || s.length() == 0) ? "." : s;
    }

    private static String cleanField(String s) {
        if (s.startsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1); // remove quotations
        }
        return s;
    }

    /*
     * Not the most efficient...
     */
    private static void copyFile(File sourceFile, File destFile, boolean append) throws IOException {

        InputStream in = new FileInputStream(sourceFile);
        OutputStream out = new FileOutputStream(destFile, append);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /*
     * Assumes rows in inFile are INTERMEDIATE format
     */
    private static void mergeVCF(File inFile, File outFile, Set<String> dnaIdsSet, String[] customColumnNames)
            throws Exception {

        BufferedReader in = new BufferedReader(new FileReader(inFile));
        BufferedWriter out = new BufferedWriter(new FileWriter(outFile, false));

        // create maps for dna ids
        Object[] dnaIds = dnaIdsSet.toArray();
        Map<String, Integer> idToPosition = new HashMap<String, Integer>();
        for (Integer i = 0; i < dnaIds.length; i++) {
            idToPosition.put((String) dnaIds[i], i);
        }

        // check for flags
        Boolean[] flagColumns = new Boolean[customColumnNames.length];
        clearArray(flagColumns, false);
        int pos = 0;
        AnnotationFormat[] formats = ProjectController.getInstance().getCurrentAnnotationFormats();
        for (int i = 1; i < formats.length; i++) {
            for (CustomField f : formats[i].getCustomFields()) {
                if (f.getColumnType() == ColumnType.BOOLEAN) {
                    flagColumns[pos] = true;
                }
                pos++;
            }
        }

        // write header
        out.write(createHeader(dnaIds));

        // position of last visited row
        String lastChr = "";
        String lastPos = "";

        // which dna ids are found with this variant
        Boolean[] dnaMatches = new Boolean[dnaIds.length];

        // which columns match among dna ids at this position
        Boolean[] columnMatches = new Boolean[2 + customColumnNames.length]; // qual, filter, customcolumns...

        String line;
        String[][] records = new String[dnaIds.length][];
        String[] record = null;
        Boolean[] availableIds = new Boolean[dnaIds.length];
        clearArray(records);
        clearArray(availableIds, false);

        while (true) {
            // parse current record
            line = in.readLine();
            if (line != null) {
                record = line.split("\t");
            }

            // write records for previous position
            if (line == null || !lastPos.equals(record[INTERMEDIATE_INDEX_POSITION])
                    || !lastChr.equals(record[INTERMEDIATE_INDEX_CHROM])) {

                for (int i = 0; i < dnaIds.length; i++) {

                    clearArray(dnaMatches, false);
                    clearArray(columnMatches, true);

                    String[] row1 = records[i];
                    if (!availableIds[i] || row1 == null) {
                        continue; // no variant for this dna id
                    }

                    dnaMatches[i] = true;

                    // look for other matching lines
                    for (int j = i + 1; j < dnaIds.length; j++) {
                        String[] row2 = records[j];
                        if (availableIds[j] && row2 != null && compareRows(row1, row2)) {
                            dnaMatches[j] = true;
                            determineMatches(columnMatches, row1, row2);
                            availableIds[j] = false; // skip next time
                        }
                    }

                    // write line
                    String output = "";
                    for (int col = INTERMEDIATE_INDEX_CHROM; col < INTERMEDIATE_INDEX_QUAL; col++) {
                        output += row1[col] + "\t";
                    }
                    for (int col = INTERMEDIATE_INDEX_QUAL; col < INTERMEDIATE_INDEX_CUSTOM; col++) {
                        if (columnMatches[col - INTERMEDIATE_INDEX_QUAL]) {
                            output += row1[col];
                        } else {
                            output += ".";
                        }
                        output += "\t";
                    }
                    for (int col = INTERMEDIATE_INDEX_CUSTOM; col < row1.length; col++) {
                        if (columnMatches[col - INTERMEDIATE_INDEX_QUAL] && !row1[col].equals("")) {
                            if (flagColumns[col - INTERMEDIATE_INDEX_CUSTOM] && row1[col].equals("1")) {
                                output += customColumnNames[col - INTERMEDIATE_INDEX_CUSTOM] + ";";
                            } else if (!flagColumns[col - INTERMEDIATE_INDEX_CUSTOM]) {
                                output += customColumnNames[col - INTERMEDIATE_INDEX_CUSTOM] + "=" + row1[col] + ";";
                            }
                        }
                    }
                    output += "\t";
                    output += "GT\t"; // format column
                    for (int id = 0; id < dnaIds.length; id++) {
                        if (dnaMatches[id]) {
                            output += records[id][INTERMEDIATE_INDEX_GENOTYPE];
                        } else {
                            output += ".";
                        }
                        if (id != dnaIds.length - 1) {
                            output += "\t";
                        }
                    }
                    out.write(output + "\n");

                }
                clearArray(records);
                clearArray(availableIds, false);
            }

            if (line == null) {
                break;
            }

            // parse current record
            String dnaId = record[INTERMEDIATE_INDEX_DNA_ID];
            records[idToPosition.get(dnaId)] = record;
            availableIds[idToPosition.get(dnaId)] = true;
            lastChr = record[INTERMEDIATE_INDEX_CHROM];
            lastPos = record[INTERMEDIATE_INDEX_POSITION];

        }

        in.close();
        out.close();
    }

    private static boolean compareRows(String[] row1, String[] row2) {
        if (row1.length != row2.length) {
            return false;
        }
        for (int i = INTERMEDIATE_INDEX_DBSNP; i < INTERMEDIATE_INDEX_QUAL; i++) { // compare ref, alt, dbsnp_id
            if (!row1[i].equals(row2[i])) {
                return false;
            }
        }
        return true;
    }

    private static void clearArray(String[][] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = null;
        }
    }

    private static void clearArray(Boolean[] array, boolean value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    private static void determineMatches(Boolean[] matches, String[] row1, String[] row2) {
        for (int i = INTERMEDIATE_INDEX_DBSNP; i < row1.length; i++) {
            if (!row1[i].equals(row2[i])) {
                matches[i - INTERMEDIATE_INDEX_QUAL] = false;
            }
        }
    }

    private static String createHeader(Object[] dnaIds) throws Exception {
        String header = "";

        // file format
        header += "##fileformat=VCFv4.0\n";

        // date
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        header += "##fileDate=" + sdf.format(cal.getTime()) + "\n";

        // source
        // TODO
        // reference
        header += "##reference=" + ReferenceController.getInstance().getCurrentReferenceName() + "\n";

        // info
        AnnotationFormat[] annotationFormats = ProjectController.getInstance().getCurrentAnnotationFormats();
        for (int i = 1; i < annotationFormats.length; i++) {
            AnnotationFormat af = annotationFormats[i];
            for (CustomField field : af.getCustomFields()) {
                header += "##INFO=<ID=" + field.getColumnName().toUpperCase() + ",";
                switch (field.getColumnType()) {
                    case INTEGER:
                        header += "Number=1,Type=Integer";
                        break;
                    case FLOAT:
                        header += "Number=1,Type=Float";
                        break;
                    case BOOLEAN:
                        header += "Number=0,Type=Flag";
                        break;
                    default:
                        header += "Number=1,Type=String";
                }
                header += ",Description=\"" + field.getDescription() + "\">\n";
            }
        }

        // format
        header += "##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">\n";

        // column headers
        header += "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\t";
        for (int i = 0; i < dnaIds.length; i++) {
            header += (String) dnaIds[i];
            if (i != dnaIds.length - 1) {
                header += "\t";
            }
        }
        header += "\n";

        return header;
    }
}
