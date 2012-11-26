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

package org.ut.biolab.medsavant.vcf;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import net.sf.samtools.util.BlockCompressedInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.db.connection.ConnectionController;

import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.vcf.VariantRecord.Zygosity;


/**
 *
 * @author mfiume
 */
public class VCFParser {


    private static final Log LOG = LogFactory.getLog(VCFParser.class);

    private static final String HEADER_CHARS = "#";
    private static final String COMMENT_SPLITTER = "=";
    private static final String COMMENT_CHARS = "##";

    public static VCFHeader parseVCFHeader(BufferedReader r) throws IOException {
        String nextLineString;
        while (true) {
            if ((nextLineString = r.readLine()) == null) {
                break;
            }
            // a comment line
            String[] nextLine = nextLineString.split("\t");
            if (nextLine[0].startsWith(COMMENT_CHARS)) {
                //do nothing
            } else if (nextLine[0].startsWith(HEADER_CHARS)) {
                // header line
                return parseHeader(nextLine);
            } // a data line
        }

        return null;
    }

    public static int parseVariantsFromReader(BufferedReader r, int outputLinesLimit, File outfile, int updateId, int fileId) throws IOException {
        return parseVariantsFromReader(r, null, outputLinesLimit, outfile, updateId, fileId, false);
    }

    /**
     * Like parseVariantsFromReader, but use a pre-parsed header object (useful when
     * reusing a header)
     * @param r A reader for .vcf file being parsed
     * @param header The VCF header object to use. If header == null, the first header line in the file will be used
     * @param outputLinesLimit The number of lines to write to output before returning
     * @param outfile The temporary output file, with variants parsed 1 per position per individual
     * @param updateId The updateId to prepend to each line
     * @param fileId The fileId to prepend to each line
     * @return number of lines written to outfile
     * @throws IOException
     */
    public static int parseVariantsFromReader(BufferedReader r, VCFHeader header, int outputLinesLimit, File outfile, int updateId, int fileId, boolean includeHomoRef) throws IOException {
        return parseVariantsFromReader(r, header, outputLinesLimit, outfile, updateId, fileId, includeHomoRef, 0);
    }

    public static int parseVariantsFromReader(BufferedReader r, VCFHeader header, int outputLinesLimit, File outfile, int updateId, int fileId, boolean includeHomoRef, int variantIdOffset) throws IOException {

        System.out.println("Starting to parse variants from reader");

        String nextLineString;
        int numRecords = 0;


        BufferedWriter out = new BufferedWriter(new FileWriter(outfile, true));

        int variantId = variantIdOffset;
        int numLinesWritten = 0;

        while (true) {
            if (numLinesWritten >= outputLinesLimit) {
                break;
            }
            if ((nextLineString = r.readLine()) == null) {
                System.out.println("reader returned null after " + numLinesWritten + " lines.");
                break;
            }

            if (numRecords % 100000 == 0 && numRecords != 0) {
                LOG.info("Processed " + numRecords + " lines (" + numLinesWritten + " variants) so far...");
            }

            String[] nextLine = nextLineString.split("\t");

            if (nextLine[0].startsWith(COMMENT_CHARS)) {
                // a comment line
                //do nothing
            } else if (nextLine[0].startsWith(HEADER_CHARS)) {
                // header line
                if (header == null) {
                    header = parseHeader(nextLine);
                }
            } else {
                // a data line
                List<VariantRecord> records = null;
                try {
                    records = parseRecord(nextLine, header);
                } catch (Exception ex) {
                    System.out.println("Next line: " + nextLineString);
                    throw new IOException(ex);
                }
                //add records to tdf
                for (VariantRecord v : records) {
                    if (includeHomoRef || v.getZygosity() != Zygosity.HomoRef) {
                        out.write(v.toTabString(updateId, fileId, variantId));
                        numLinesWritten++;
                        out.write("\r\n");
                        variantId++;
                    }
                }
                numRecords++;
            }
        }
        out.close();

        System.out.println("Read " + numRecords + " lines");

        return numLinesWritten;
    }

    public static void parseVariants(File vcffile, File outfile, int updateId, int fileId) throws FileNotFoundException, IOException {
        BufferedReader r = openFile(vcffile);
        parseVariantsFromReader(r, Integer.MAX_VALUE, outfile, updateId, fileId);
        r.close();
    }

    static BufferedReader openFile(File vcf) throws FileNotFoundException, IOException {
        if (vcf.getAbsolutePath().endsWith(".gz")) {
            return new BufferedReader(new InputStreamReader(new BlockCompressedInputStream(vcf)));
        } else {
            return new BufferedReader(new FileReader(vcf));
        }
    }

    private static VCFHeader parseHeader(String[] headerLine) {

        VCFHeader result = new VCFHeader();

        // has genotype information
        if (headerLine.length > VCFHeader.getNumMandatoryFields()) {

            // get the genotype labels
            for (int i = VCFHeader.getNumMandatoryFields() + 1; i < headerLine.length; i++) {
                if (headerLine[i] != null && headerLine[i].length() != 0) {
                    result.addGenotypeLabel(headerLine[i]);
                }
            }
        }

        return result;
    }

    private static List<VariantRecord> parseRecord(String[] line, VCFHeader h) {
        int numMandatoryFields = VCFHeader.getNumMandatoryFields();

        List<String> infos = new ArrayList<String>();
        List<String> ids;

        for (int i = numMandatoryFields; i < line.length; i++) {
            infos.add(line[i]);
        }

        if (infos.isEmpty()) {
            infos.add(".");
            ids = new ArrayList<String>();
            ids.add(".");
        } else {
            ids = h.getGenotypeLabels();
        }

        List<VariantRecord> records = new ArrayList<VariantRecord>();

        try {
            VariantRecord r = new VariantRecord(line);
            int indexGT = getIndexGT(line);
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                VariantRecord r2 = new VariantRecord(r);
                r2.setDnaID(id);

                //add gt and zygosity;
                if (indexGT != -1) {
                    r2.setGenotype(line[numMandatoryFields+i+1].split(":")[indexGT]);
                    r2.setZygosity(calculateZygosity(r2.getGenotype()));
                }

                records.add(r2);
            }
        } catch (Exception ex) {
            System.err.println("Error parsing line " + line[0] + ": " + MiscUtils.getMessage(ex));
        }
        return records;
    }

    private static int getIndexGT(String[] line) {
        if (line.length >= VCFHeader.getNumMandatoryFields() + 1) {
            String[] list = line[VCFHeader.getNumMandatoryFields()].trim().split(":");
            for(int i = 0; i < list.length; i++) {
                if (list[i].equals("GT")) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static Zygosity calculateZygosity(String gt) {
        String[] split = gt.split("/|\\\\|\\|"); // splits on / or \ or |
        if (split.length < 2 || split[0] == null || split[1] == null || split[0].length() == 0 || split[1].length() == 0) return null;

        try {
            int a = Integer.parseInt(split[0]);
            int b = Integer.parseInt(split[1]);
            if (a == 0 && b == 0) {
                return Zygosity.HomoRef;
            } else if (a == b) {
                return Zygosity.HomoAlt;
            } else if (a == 0 || b == 0) {
                return Zygosity.Hetero;
            } else {
                return Zygosity.HeteroTriallelic;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
