/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.vcf;

import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class VCFParser {
    private static char defaultDelimiter = '\t';
    private static String headerChars = "#";
    private static String commentSplitter = "=";
    private static String commentChars = "##";

    private static void parseVariantsFromReader(CSVReader r, File outfile, int updateId, int fileId) throws IOException {
        
        String[] nextLine;
        int numRecords = 0;
        VCFHeader header = null;
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outfile, false));
            
            int variantId = 0;
            while((nextLine = r.readNext()) != null) {
                // a comment line
                if (nextLine[0].startsWith(commentChars)) {
                    //do nothing
                }
                // header line
                else if (nextLine[0].startsWith(headerChars)) {
                    header = parseHeader(nextLine);
                }
                // a data line
                else {
                    List<VariantRecord> records = parseRecord(nextLine,header);
                    //add records to tdf
                    for(VariantRecord v : records){
                        out.write(v.toTabString(updateId, fileId, variantId));
                        out.write("\r\n");
                        variantId++;
                    }
                    numRecords++;
                }
            }
            out.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
    
    public static void parseVariants(File vcffile, File outfile, char delimiter, int updateId, int fileId) throws FileNotFoundException, IOException{
        CSVReader r = openFile(vcffile, delimiter);
        parseVariantsFromReader(r, outfile, updateId, fileId);
    }

    private static CSVReader openFile(File vcffile, char delim) throws FileNotFoundException {
        return new CSVReader(new FileReader(vcffile), delim);
    }

    private static VCFHeader parseHeader(String[] headerLine) {

        VCFHeader result = new VCFHeader();

        // has genotype information
        if (headerLine.length > VCFHeader.getNumMandatoryFields()) {

            // get the genotype labels
            for (int i = VCFHeader.getNumMandatoryFields()+1; i < headerLine.length; i++) {
                result.addGenotypeLabel(headerLine[i]);
            }
        }

        return result;
    }

    private static String[] parseComment(String commentLine) {
        commentLine = commentLine.replaceFirst(commentChars, "");
        int indexOfSplit = commentLine.indexOf(commentSplitter);
        String[] result = new String[2];
        result[0] = commentLine.substring(0,indexOfSplit);
        result[1] = commentLine.substring(indexOfSplit+1);
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
        }
        else {
            ids = h.getGenotypeLabels();
        }
        
        //GenotypeField[] formatHeader = parseFormat(line[numMandatoryFields]);
        
        List<VariantRecord> records = new ArrayList<VariantRecord>();
        VariantRecord r = new VariantRecord(line);
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            VariantRecord r2 = new VariantRecord(r);
            r2.setDnaID(id);
            //r2.setGenotypeFields(formatHeader, line[numMandatoryFields+1+i]);
            //r2.setCallDetails(info);
            records.add(r2);
            //Logger.log(VCFParser.class, "Read " + r2.toString());
        }
        
        return records;
    }

}
