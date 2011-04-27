/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fiume.vcf;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

    private static VariantSet parseVariantsFromReader(CSVReader r) throws IOException {
        String [] nextLine;

        VariantSet s = new VariantSet();

        Logger.log(VCFParser.class, "Parsing variant file");

        while ((nextLine = r.readNext()) != null) {

            //Logger.log(VCFParser.class, "Parsing " + nextLine[0] + "...");

            if (nextLine.length > 0) {

                // a comment line
                if (nextLine[0].startsWith(commentChars)) {
                    String[] keyValue = parseComment(nextLine[0]);
                    s.addProperty(keyValue[0],keyValue[1]);
                }
                // header line
                else if (nextLine[0].startsWith(headerChars)) {
                    s.setHeader(parseHeader(nextLine));
                    Logger.log(VCFParser.class, s.getHeader().toString());
                }
                // a data line
                else {
                    List<VariantRecord> records = parseRecord(nextLine,s.getHeader());
                    s.addRecords(records);
                }
            }
        }

        Logger.log(VCFParser.class, "Parsed " + s.getRecords().size() + " variant records");

        return s;
    }

    private static String commentChars = "##";

    public static VariantSet parseVariants(File vcffile, char delimiter) throws FileNotFoundException, IOException {
        CSVReader r = openFile(vcffile, delimiter);
        return parseVariantsFromReader(r);
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

    public static VariantSet parseVariants(File vcffile) throws IOException {
        return parseVariants(vcffile,defaultDelimiter);
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

        List<VariantRecord> records = new ArrayList<VariantRecord>();
        VariantRecord r = new VariantRecord(line);
        for (int i = 0; i < ids.size(); i++) {
            String info = infos.get(i);
            String id = ids.get(i);
            VariantRecord r2 = new VariantRecord(r);
            r2.setSampleID(id);
            r2.setCallDetails(info);
            records.add(r2);
            //Logger.log(VCFParser.class, "Read " + r2.toString());
        }
        
        return records;
    }

}
