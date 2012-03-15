/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.vcf;

import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.ut.biolab.medsavant.vcf.VariantRecord.Zygosity;

/**
 *
 * @author mfiume
 */
public class VCFParser {

    public static final char defaultDelimiter = '\t';
    public static final String headerChars = "#";
    public static final String commentSplitter = "=";
    public static final String commentChars = "##";

    public static VCFHeader parseVCFHeader(CSVReader r) throws IOException {
        String[] nextLine;
        while (true) {
            if ((nextLine = r.readNext()) == null) {
                break;
            }
            // a comment line
            if (nextLine[0].startsWith(commentChars)) {
                //do nothing
            } // header line
            else if (nextLine[0].startsWith(headerChars)) {
                return parseHeader(nextLine);
            } // a data line
        }

        return null;
    }

    public static int parseVariantsFromReader(CSVReader r, int outputLinesLimit, File outfile, int updateId, int fileId) throws IOException {
        return parseVariantsFromReader(r, null, outputLinesLimit, outfile, updateId, fileId);
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
    public static int parseVariantsFromReader(CSVReader r, VCFHeader header, int outputLinesLimit, File outfile, int updateId, int fileId) throws IOException {

        System.out.println("Starting to parse variants from reader");

        String[] nextLine;
        int numRecords = 0;


        BufferedWriter out = new BufferedWriter(new FileWriter(outfile, true));

        int variantId = 0;
        int numLinesWritten = 0;

        while (true) {
            if (numLinesWritten >= outputLinesLimit) {
                break;
            }
            if ((nextLine = r.readNext()) == null) {
                break;
            }

            // a comment line
            if (nextLine[0].startsWith(commentChars)) {
                //do nothing
            } // header line
            else if (nextLine[0].startsWith(headerChars)) {
                if (header == null) {
                    header = parseHeader(nextLine);
                }
            } // a data line
            else {
                List<VariantRecord> records = null;
                try {
                    records = parseRecord(nextLine, header);
                } catch (NullPointerException e) {
                    System.out.println("Next line: " + nextLine);
                    throw e;
                }
                //add records to tdf
                for (VariantRecord v : records) {
                    if (v.getZygosity() != Zygosity.HomoRef) {
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

    public static void parseVariants(File vcffile, File outfile, char delimiter, int updateId, int fileId) throws FileNotFoundException, IOException {
        CSVReader r = openFile(vcffile, delimiter);
        parseVariantsFromReader(r, Integer.MAX_VALUE, outfile, updateId, fileId);
        r.close();
    }

    private static CSVReader openFile(File vcffile, char delim) throws FileNotFoundException, IOException {

        Reader reader;
        if (vcffile.getAbsolutePath().endsWith(".gz") || vcffile.getAbsolutePath().endsWith(".zip")) {
            FileInputStream fin = new FileInputStream(vcffile.getAbsolutePath());
            reader = new InputStreamReader(new GZIPInputStream(fin));
        } else {
            reader = new FileReader(vcffile);
        }

        return new CSVReader(reader, delim);
    }

    private static VCFHeader parseHeader(String[] headerLine) {

        VCFHeader result = new VCFHeader();

        // has genotype information
        if (headerLine.length > VCFHeader.getNumMandatoryFields()) {

            // get the genotype labels
            for (int i = VCFHeader.getNumMandatoryFields() + 1; i < headerLine.length; i++) {
                result.addGenotypeLabel(headerLine[i]);
            }
        }

        return result;
    }

    private static String[] parseComment(String commentLine) {
        commentLine = commentLine.replaceFirst(commentChars, "");
        int indexOfSplit = commentLine.indexOf(commentSplitter);
        String[] result = new String[2];
        result[0] = commentLine.substring(0, indexOfSplit);
        result[1] = commentLine.substring(indexOfSplit + 1);
        return result;
    }

    public static void parseVariants(File vcffile, File outfile, int updateId, int fileId) throws FileNotFoundException, IOException {
        parseVariants(vcffile, outfile, defaultDelimiter, updateId, fileId);
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

        //GenotypeField[] formatHeader = parseFormat(line[numMandatoryFields]);

        List<VariantRecord> records = new ArrayList<VariantRecord>();

        VariantRecord r = null;
        try {
            r = new VariantRecord(line);
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("WARNING: error parsing line " + line + ". Skipping");
            /*for(String s : line){
                System.err.print(s + "\t");
            }
            System.err.println();*/
            return new ArrayList<VariantRecord>();
        }

        int indexGT = getIndexGT(line);
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            VariantRecord r2 = new VariantRecord(r);
            r2.setDnaID(id);

            //add gt and zygosity;
            if(indexGT != -1){
                r2.setGenotype(line[numMandatoryFields+i+1].split(":")[indexGT]);
                r2.setZygosity(calculateZygosity(r2.getGenotype()));
            }

            records.add(r2);
        }

        return records;
    }

    private static int getIndexGT(String[] line){
        if(line.length < VCFHeader.getNumMandatoryFields()+1) return -1;
        String[] list = line[VCFHeader.getNumMandatoryFields()].trim().split(":");
        for(int i = 0; i < list.length; i++){
            if(list[i].equals("GT")){
                return i;
            }
        }
        return -1;
    }

    private static Zygosity calculateZygosity(String gt){
        String[] split = gt.split("/|\\\\|\\|"); // splits on / or \ or |
        if(split.length < 2 || split[0] == null || split[1] == null || split[0].length() == 0 || split[1].length() == 0) return null;

        try {
            int a = Integer.parseInt(split[0]);
            int b = Integer.parseInt(split[1]);
            if(a == 0 && b == 0){
                return Zygosity.HomoRef;
            } else if (a == b){
                return Zygosity.HomoAlt;
            } else if (a == 0 || b == 0){
                return Zygosity.Hetero;
            } else {
                return Zygosity.HeteroTriallelic;
            }
        } catch (NumberFormatException e){
            return null;
        }
    }
    
    /*
     * Useful for testing
     */
    static public void main(String args[]) {
        File input = new File(args[0]);
        try {
            parseVariants(input, new File(input.getName() + "_parsed"), 0, 0);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VCFParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VCFParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
