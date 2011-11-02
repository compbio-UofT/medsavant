/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.vcf;

import au.com.bytecode.opencsv.CSVReader;
//import fiume.vcf.VariantRecord.GenotypeField;
//import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

    /*private static VariantSet parseVariantsFromReader(CSVReader r, int variant_id, int genome_id, int pipeline_id) throws IOException, SQLException {
        String [] nextLine;

        SortedVariantSet s = new SortedVariantSet();

        Logger.log(VCFParser.class, "Parsing variant file");
        
        int numRecords = 0;

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
                    for(VariantRecord rec : records){
                        rec.setVariantID(variant_id);
                        rec.setGenomeID(genome_id);
                        rec.setPipelineID(pipeline_id);
                    }
                    s.addRecords(records);
                    //for(VariantRecord rec : records){
                    //    insertRecord(rec, ps, variant_id, genome_id, pipeline_id, columns);
                    //    variant_id++;
                    //    numRecords++;
                    //}
                }
            }
        }

        Logger.log(VCFParser.class, "Parsed " + s.getRecords().size() + " variant records");

        return s;
    }*/
    
    /*public static void insertRecord(VariantRecord record, PreparedStatement ps, int variant_id, int genome_id, int pipeline_id, List<DbColumn> columns) throws SQLException{
        for(int i = 0; i < columns.size(); i++){
            DbColumn col = columns.get(i);
            switch(VariantTableSchema.FIELD_NAMES.valueOf(col.getColumnNameSQL().toUpperCase())){
                case VARIANT_ID:
                    ps.setInt(i+1, variant_id);
                    break;
                case GENOME_ID:
                    ps.setInt(i+1, genome_id);
                    break;
                case PIPELINE_ID:
                    ps.setInt(i+1, pipeline_id);
                    break;
                case DNA_ID:
                    ps.setString(i+1, record.getDnaID());
                    break;
                case CHROM:
                    ps.setString(i+1, record.getChrom());
                    break;
                case POSITION:
                    ps.setLong(i+1, record.getPosition());
                    break;
                case DBSNP_ID:
                    ps.setString(i+1, record.getDbSNPID());
                    break;
                case REF:
                    ps.setString(i+1, record.getRef());
                    break;
                case ALT:
                    ps.setString(i+1, record.getAlt());
                    break;
                case QUAL:
                    ps.setFloat(i+1, record.getQual());
                    break;
                case FILTER:
                    ps.setString(i+1, record.getFilter());
                    break;
                case AA:
                    ps.setString(i+1, record.getAA());
                    break;
                case AC:
                    ps.setString(i+1, record.getAC());
                    break;
                case AF:
                    ps.setString(i+1, record.getAF());
                    break;
                case AN:
                    ps.setInt(i+1, record.getAN());
                    break;
                case BQ:
                    ps.setFloat(i+1, record.getBQ());
                    break;
                case CIGAR:
                    ps.setString(i+1, record.getCigar());
                    break;
                case DB:
                    ps.setBoolean(i+1, record.getDB());
                    break;
                case DP:
                    ps.setInt(i+1, record.getDP());
                    break;
                case END:
                    ps.setLong(i+1, record.getEnd());
                    break;
                case H2:
                    ps.setBoolean(i+1, record.getH2());
                    break;
                case MQ:
                    ps.setFloat(i+1, record.getMQ());
                    break;
                case MQ0:
                    ps.setInt(i+1, record.getMQ0());
                    break;
                case NS:
                    ps.setInt(i+1, record.getNS());
                    break;
                case SB:
                    ps.setFloat(i+1, record.getSB());
                    break;
                case SOMATIC:
                    ps.setBoolean(i+1, record.getSomatic());
                    break;
                case VALIDATED:
                    ps.setBoolean(i+1, record.getValidated());
                    break;
                case CUSTOM_INFO:
                    ps.setString(i+1, record.getCustomInfo());
                    break;
                //case VARIANT_ANNOTATION_SIFT_ID:
                //    ps.setInt(i+1, 0);
                //    break;
                default:
                    break;
            }              
        }
        ps.executeUpdate();
    }*/
    
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

    /*public static VariantSet parseVariants(File vcffile) throws FileNotFoundException, IOException{
        return parseVariants(vcffile, defaultDelimiter);
    }
    
    public static VariantSet parseVariants(File vcffile, int variant_id, int genome_id, int pipeline_id) throws IOException, SQLException {
        return parseVariants(vcffile,defaultDelimiter, variant_id, genome_id, pipeline_id);
    }*/
       
    /*public static GenotypeField[] parseFormat(String formatString){
        formatString = formatString.trim();
        String[] list = formatString.split(":");
        GenotypeField[] result = new GenotypeField[list.length];
        for(int i = 0; i < list.length; i++){
            String s = list[i].toUpperCase();
            try {
                GenotypeField f = GenotypeField.valueOf(s);
                result[i] = f;
            } catch (IllegalArgumentException ex) {
                result[i] = GenotypeField.NOTSTANDARD;
            }
        }
        return result;
    }*/
      
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
