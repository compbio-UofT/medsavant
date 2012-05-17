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
package org.ut.biolab.medsavant.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.healthmarketscience.rmiio.RemoteInputStream;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.ColumnType;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.settings.DirectorySettings;

/**
 *
 * @author Andrew
 */
public class ExportVCF {
    
    //important locations in intermediate file created pre-merge
    private static int INTERMEDIATE_INDEX_DNA_ID = 0;
    private static int INTERMEDIATE_INDEX_GENOTYPE = 1;
    private static int INTERMEDIATE_INDEX_CHROM = 2;
    private static int INTERMEDIATE_INDEX_POSITION = 3;
    private static int INTERMEDIATE_INDEX_DBSNP = 4;
    private static int INTERMEDIATE_INDEX_REF = 5;
    private static int INTERMEDIATE_INDEX_ALT = 6;
    private static int INTERMEDIATE_INDEX_QUAL = 7;
    private static int INTERMEDIATE_INDEX_FILTER = 8;
    private static int INTERMEDIATE_INDEX_CUSTOM = 9; //and on

    public static void exportVCF(File file) throws Exception {        
        
        RemoteInputStream ris = MedSavantClient.VariantManager.exportVariants(
                LoginController.sessionId, 
                ProjectController.getInstance().getCurrentProjectId(), 
                ReferenceController.getInstance().getCurrentReferenceId(), 
                FilterController.getQueryFilterConditions());
        
        //copy stream to file
        File tempFile = ClientNetworkUtils.copyFileFromRemoteStream(ris);   
        BufferedReader in = new BufferedReader(new FileReader(tempFile));
        
        //maintain lists of chrs, dnaids, ...
        Map<String, BufferedWriter> out = new HashMap<String, BufferedWriter>();
        Map<String, File> files = new HashMap<String, File>();
        List<String> chrs = new ArrayList<String>();
        Set<String> dnaIds = new HashSet<String>();

        //info fields
        TableSchema table = ProjectController.getInstance().getCurrentVariantTableSchema();
        String[] customColumnNames = new String[table.getNumFields() - DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO - 1];
        for (int i = DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO+2; i <= table.getNumFields(); i++) {
            customColumnNames[i-2-DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO] = table.getDBName(i).toUpperCase();
        }
        int infoMin = DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO+1;
        int infoMax = table.getNumFields();

        String line;
        while ((line = in.readLine()) != null) {
            
            //parse row
            String[] record = line.split(",");
            String row = "";
            
            //dna id
            String dnaId = cleanField(record[DefaultVariantTableSchema.INDEX_OF_DNA_ID]);
            row += dnaId + "\t";
            dnaIds.add(dnaId); 
            
            //genotype
            row += cleanField(record[DefaultVariantTableSchema.INDEX_OF_GT]) + "\t";
            
            //default fields
            row += 
                    cleanField(record[DefaultVariantTableSchema.INDEX_OF_CHROM]) + "\t" + 
                    cleanField(record[DefaultVariantTableSchema.INDEX_OF_POSITION]) + "\t" +
                    parseMandatoryField(cleanField(record[DefaultVariantTableSchema.INDEX_OF_DBSNP_ID])) + "\t" + 
                    parseMandatoryField(cleanField(record[DefaultVariantTableSchema.INDEX_OF_REF])) + "\t" + 
                    parseMandatoryField(cleanField(record[DefaultVariantTableSchema.INDEX_OF_ALT])) + "\t" + 
                    parseMandatoryField(cleanField(record[DefaultVariantTableSchema.INDEX_OF_QUAL])) + "\t" + 
                    parseMandatoryField(cleanField(record[DefaultVariantTableSchema.INDEX_OF_FILTER])) + "\t";
            
            //extra fields
            for (int j = infoMin; j < infoMax; j++) {
                if (j < record.length) row += cleanField(record[j]);
                if (j != infoMax-1) row += "\t";
            }

            //get writer for chrom, or create
            BufferedWriter writer = out.get(cleanField(record[DefaultVariantTableSchema.INDEX_OF_CHROM]));
            if (writer == null) {
                String chrom = cleanField(record[DefaultVariantTableSchema.INDEX_OF_CHROM]);
                File f = new File(DirectorySettings.getTmpDirectory() + File.separator + file.getName() + chrom);
                writer = new BufferedWriter(new FileWriter(f, false));
                out.put(chrom, writer);
                chrs.add(chrom);
                files.put(chrom, f);
            }

            //write record
            writer.write(row + "\n");
        }
        
        //close writers
        for (String key : out.keySet()) {
            out.get(key).close();
        }
        
        //concatenate separate chrom files in proper order
        Collections.sort(chrs, new ChromosomeComparator());      
        File temp1 = new File(DirectorySettings.getTmpDirectory() + File.separator + file.getName() + "_complete");
        for (int i = 0; i < files.size(); i++) {
            copyFile(files.get(chrs.get(i)), temp1, i != 0);
        }
        
        //merge vcf to remove duplicates
        mergeVCF(temp1, file, dnaIds, customColumnNames);
    }
    
    private static String parseMandatoryField(String s) {
        return (s == null || s.length() == 0) ? "." : s;
    }
    
    private static String cleanField(String s) {
        if (s.startsWith("\"") && s.length()>=2) {
            return s.substring(1, s.length()-1); //remove quotations
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
    private static void mergeVCF(File inFile, File outFile, Set<String> dnaIdsSet, String[] customColumnNames) throws Exception {
        
        BufferedReader in = new BufferedReader(new FileReader(inFile));
        BufferedWriter out = new BufferedWriter(new FileWriter(outFile, false));
        
        //create maps for dna ids
        Object[] dnaIds = dnaIdsSet.toArray();
        Map<String, Integer> idToPosition = new HashMap<String, Integer>();
        for (Integer i = 0; i < dnaIds.length; i++) {
            idToPosition.put((String)dnaIds[i], i);
        }
        
        //check for flags
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
        
        //write header
        out.write(createHeader(dnaIds));       
        
        //position of last visited row
        String lastChr = "";
        String lastPos = "";
        
        //which dna ids are found with this variant
        Boolean[] dnaMatches = new Boolean[dnaIds.length];

        //which columns match among dna ids at this position
        Boolean[] columnMatches = new Boolean[2 + customColumnNames.length]; //qual, filter, customcolumns...
        
        String line;
        String[][] records = new String[dnaIds.length][];
        String[] record = null;
        Boolean[] availableIds = new Boolean[dnaIds.length];
        clearArray(records);    
        clearArray(availableIds, false);
        
        while (true) {

            //parse current record
            line = in.readLine();
            if (line != null) {
                record = line.split("\t");
            }
            
            //write records for previous position
            if (line == null || !lastPos.equals(record[INTERMEDIATE_INDEX_POSITION]) || !lastChr.equals(record[INTERMEDIATE_INDEX_CHROM])) {              
                
                for (int i = 0; i < dnaIds.length; i++) {   
                    
                    clearArray(dnaMatches, false);
                    clearArray(columnMatches, true);
                    
                    String[] row1 = records[i];
                    if (!availableIds[i] || row1 == null) continue; //no variant for this dna id                  
                    
                    dnaMatches[i] = true;

                    //look for other matching lines
                    for (int j = i+1; j < dnaIds.length; j++) {                     
                        String[] row2 = records[j];
                        if (availableIds[j] && row2 != null && compareRows(row1, row2)) {
                            dnaMatches[j] = true;
                            determineMatches(columnMatches, row1, row2);
                            availableIds[j] = false; //skip next time
                        }
                    }
                    
                    //write line
                    String output = "";
                    for (int col = INTERMEDIATE_INDEX_CHROM; col < INTERMEDIATE_INDEX_QUAL; col++) {
                        output += row1[col] + "\t";
                    }
                    for (int col = INTERMEDIATE_INDEX_QUAL; col < INTERMEDIATE_INDEX_CUSTOM; col++) {
                        if (columnMatches[col-INTERMEDIATE_INDEX_QUAL]) output += row1[col];
                        else output += ".";    
                        output += "\t";
                    }
                    for (int col = INTERMEDIATE_INDEX_CUSTOM; col < row1.length; col++) {
                        if (columnMatches[col-INTERMEDIATE_INDEX_QUAL] && !row1[col].equals("")) {
                            if (flagColumns[col-INTERMEDIATE_INDEX_CUSTOM] && row1[col].equals("1")) output += customColumnNames[col-INTERMEDIATE_INDEX_CUSTOM] + ";";
                            else if (!flagColumns[col-INTERMEDIATE_INDEX_CUSTOM]) output += customColumnNames[col-INTERMEDIATE_INDEX_CUSTOM] + "=" + row1[col] + ";";                          
                        }
                    }
                    output += "\t";
                    output += "GT\t"; //format column
                    for (int id = 0; id < dnaIds.length; id++) {
                        if (dnaMatches[id]) output += records[id][INTERMEDIATE_INDEX_GENOTYPE];
                        else output += ".";
                        if (id != dnaIds.length-1) output += "\t";
                    }
                    out.write(output + "\n");                    
                    
                }               
                clearArray(records);    
                clearArray(availableIds, false);
            }
            
            if (line == null) {
                break;
            }
            
            //parse current record
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
        if (row1.length != row2.length) return false;
        for (int i = INTERMEDIATE_INDEX_DBSNP; i < INTERMEDIATE_INDEX_QUAL; i++) { //compare ref, alt, dbsnp_id
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
                matches[i-INTERMEDIATE_INDEX_QUAL] = false;
            }
        }
    }
    
    private static String createHeader(Object[] dnaIds) throws Exception {
        String header = "";
        
        //file format
        header += "##fileformat=VCFv4.0\n";
        
        //date
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        header += "##fileDate=" + sdf.format(cal.getTime()) + "\n";
        
        //source
        //TODO
        
        //reference
        header += "##reference=" + ReferenceController.getInstance().getCurrentReferenceName() + "\n";
        
        //info
        AnnotationFormat[] annotationFormats = ProjectController.getInstance().getCurrentAnnotationFormats();
        for (int i = 1; i < annotationFormats.length; i++) {
            AnnotationFormat af = annotationFormats[i];
            for (CustomField field : af.getCustomFields()) {
                header += "##INFO=<ID=" + field.getColumnName().toUpperCase() + ",";
                switch(field.getColumnType()) {
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
        
        //format
        header += "##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">\n";
        
        //column headers
        header += "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\t";
        for (int i = 0; i < dnaIds.length; i++) {
            header += (String)dnaIds[i];
            if (i != dnaIds.length-1) header += "\t";
        }
        header += "\n";
        
        return header;
    }    
}
