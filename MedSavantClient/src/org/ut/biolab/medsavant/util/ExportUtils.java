/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.util;

import au.com.bytecode.opencsv.CSVWriter;
import com.healthmarketscience.rmiio.RemoteInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;
import org.ut.biolab.medsavant.db.util.shared.ExtensionFileFilter;
import org.ut.biolab.medsavant.db.util.shared.MiscUtils;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.view.util.ChromosomeComparator;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author Andrew
 */
public class ExportUtils {
    
    //private static int BIN_SIZE = 10000;
    
    public static void exportTable(JTable table) throws FileNotFoundException, IOException{
        File out = DialogUtils.chooseFileForSave("Export Table", "table_export", ExtensionFileFilter.createFilters(new String[]{"xls", "xlsx", "csv"}), null);
        
        if(out == null) return;

        String extension = MiscUtils.getExtension(out.getAbsolutePath());
        
        if(extension.equals("xls") || extension.equals("xlsx")){
            exportExcel(out, table);
        } else { // default
            exportCSV(out, table);
        }
        
    }
    
    private static void exportExcel(File file, JTable table) throws FileNotFoundException, IOException{
        
        //create workbook
        Workbook wb;
        if(file.getAbsolutePath().endsWith(".xlsx")){
            wb = new XSSFWorkbook();
        } else {
            wb = new HSSFWorkbook();            
        }       
        Sheet sheet = wb.createSheet();
        
        //add headers
        org.apache.poi.ss.usermodel.Row header= sheet.createRow(0);
        for(int j = 0; j < table.getColumnCount(); j++){
            header.createCell(j).setCellValue(table.getColumnName(j));
        }
        
        //add cells
        for(int i = 0; i < table.getRowCount(); i++){
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(i+1);
            for(int j = 0; j < table.getColumnCount(); j++){
                row.createCell(j).setCellValue(getString(table.getValueAt(i, j)));
            }
        }
        
        //write output
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fileOut.close();
    }
    
    private static void exportCSV(File file, JTable table) throws IOException {

        //setup file
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));      
        CSVWriter out = new CSVWriter(writer, ',', '"');

        //add headers
        String[] header = new String[table.getColumnCount()];
        for(int j = 0; j < table.getColumnCount(); j++){
            header[j] = table.getColumnName(j);
        }
        out.writeNext(header);
        
        //add cells
        for(int i = 0; i < table.getRowCount(); i++){
            String[] row = new String[table.getColumnCount()];
            for(int j = 0; j < table.getColumnCount(); j++){
                row[j] = getString(table.getValueAt(i, j));
            }
            out.writeNext(row);
        }
        
        //close output
        out.close();
        writer.close();

    }
    
    private static String getString(Object o){
        if(o == null){
            return "";
        } else {
            return o.toString();
        }
    }   
    
    public static void exportVCF(File file) throws IOException, NonFatalDatabaseException, SQLException, RemoteException, InterruptedException {        
        
        RemoteInputStream ris = MedSavantClient.VariantManagerAdapter.exportVariants(
                LoginController.sessionId, 
                ProjectController.getInstance().getCurrentProjectId(), 
                ReferenceController.getInstance().getCurrentReferenceId(), 
                FilterController.getQueryFilterConditions());
        
        //copy stream to file
        File tempFile = NetworkUtils.copyFileFromRemoteStream(ris);   
        BufferedReader in = new BufferedReader(new FileReader(tempFile));
        
        //maintain lists of chrs, dnaids, ...
        Map<String, BufferedWriter> out = new HashMap<String, BufferedWriter>();
        Map<String, File> files = new HashMap<String, File>();
        List<String> chrs = new ArrayList<String>();
        Set<String> dnaIds = new HashSet<String>();

        //info fields
        TableSchema table = ProjectController.getInstance().getCurrentVariantTableSchema();
        String[] customColumnNames = new String[table.getNumFields() - DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO - 1];
        for(int i = DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO+2; i <= table.getNumFields(); i++){
            customColumnNames[i-2-DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO] = table.getDBName(i).toUpperCase();
        }
        int infoMin = DefaultVariantTableSchema.INDEX_OF_CUSTOM_INFO+1;
        int infoMax = table.getNumFields();

        String line;
        while((line = in.readLine()) != null){
            
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
            for(int j = infoMin; j < infoMax; j++){
                if(j < record.length) row += cleanField(record[j]);
                if(j != infoMax-1) row += "\t";
            }

            //get writer for chrom, or create
            BufferedWriter writer = out.get(cleanField(record[DefaultVariantTableSchema.INDEX_OF_CHROM]));
            if(writer == null){
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
        for(String key : out.keySet()){
            out.get(key).close();
        }
        
        //concatenate separate chrom files in proper order
        Collections.sort(chrs, new ChromosomeComparator());      
        File temp1 = new File(DirectorySettings.getTmpDirectory() + File.separator + file.getName() + "_complete");
        for(int i = 0; i < files.size(); i++){
            copyFile(files.get(chrs.get(i)), temp1, i != 0);
        }
        
        //merge vcf to remove duplicates
        mergeVcf(temp1, file, dnaIds, customColumnNames);
    }
    
    private static String parseMandatoryField(String s){
        return (s == null || s.length() == 0) ? "." : s;
    }
    
    private static String cleanField(String s){
        if(s.startsWith("\"") && s.length()>=2){
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
        while ((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /*
     * Assumes rows in inFile are of format:
     * 0      1         2    3      4    5    6     7       8...
     * dnaid, genotype, chr, pos, dbsnp, ref, alt, qual, filter, custom...
     */
    private static void mergeVcf(File inFile, File outFile, Set<String> dnaIdsSet, String[] customColumnNames) throws IOException {
        
        BufferedReader in = new BufferedReader(new FileReader(inFile));
        BufferedWriter out = new BufferedWriter(new FileWriter(outFile, false));
        
        //create maps for dna ids
        Object[] dnaIds = dnaIdsSet.toArray();
        Map<String, Integer> idToPosition = new HashMap<String, Integer>();
        for(Integer i = 0; i < dnaIds.length; i++){
            idToPosition.put((String)dnaIds[i], i);
        }
        
        //check for flags
        Boolean[] flagColumns = new Boolean[customColumnNames.length];
        clearArray(flagColumns, false);
        int pos = 0;
        AnnotationFormat[] formats = ProjectController.getInstance().getCurrentAnnotationFormats();
        for(int i = 1; i < formats.length; i++){
            for(CustomField f : formats[i].getCustomFields()){
                if(f.getColumnType() == ColumnType.BOOLEAN){
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
        
        while(true){

            //parse current record
            line = in.readLine();
            if(line != null){
                record = line.split("\t");
            }
            
            //write records for previous position
            if(line == null || !lastPos.equals(record[3]) || !lastChr.equals(record[2])){              
                
                for(int i = 0; i < dnaIds.length; i++){   
                    
                    clearArray(dnaMatches, false);
                    clearArray(columnMatches, true);
                    
                    String[] row1 = records[i];
                    if(!availableIds[i] || row1 == null) continue; //no variant for this dna id                  
                    
                    dnaMatches[i] = true;

                    //look for other matching lines
                    for(int j = i+1; j < dnaIds.length; j++){                     
                        String[] row2 = records[j];
                        if(availableIds[j] && row2 != null && compareRows(row1, row2)){
                            dnaMatches[j] = true;
                            determineMatches(columnMatches, row1, row2);
                            availableIds[j] = false; //skip next time
                        }
                    }
                    
                    //write line
                    String output = "";
                    for(int col = 2; col < 7; col++){
                        output += row1[col] + "\t";
                    }
                    for(int col = 7; col < 9; col++){
                        if(columnMatches[col-7]) output += row1[col];
                        else output += ".";    
                        output += "\t";
                    }
                    for(int col = 9; col < row1.length; col++){
                        if(columnMatches[col-7] && !row1[col].equals("")){
                            if(flagColumns[col-9] && row1[col].equals("1")) output += customColumnNames[col-9] + ";";
                            else if (!flagColumns[col-9]) output += customColumnNames[col-9] + "=" + row1[col] + ";";                          
                        }
                    }
                    output += "\t";
                    output += "GT\t"; //format column
                    for(int id = 0; id < dnaIds.length; id++){
                        if(dnaMatches[id]) output += records[id][1];
                        else output += ".";
                        if(id != dnaIds.length-1) output += "\t";
                    }
                    out.write(output + "\n");                    
                    
                }               
                clearArray(records);    
                clearArray(availableIds, false);
            }
            
            if(line == null){
                break;
            }
            
            //parse current record
            String dnaId = record[0];
            records[idToPosition.get(dnaId)] = record;
            availableIds[idToPosition.get(dnaId)] = true;
            lastChr = record[2];
            lastPos = record[3];

        }
        
        in.close();
        out.close();
        
    }
    
    private static boolean compareRows(String[] row1, String[] row2){
        if(row1.length != row2.length || row1.length < 4) return false;
        for(int i = 4; i < 7; i++){ //compare ref, alt, dbsnp_id
            if(!row1[i].equals(row2[i])){
                return false;
            }
        }
        return true;
    }
    
    private static void clearArray(String[][] array){
        for(int i = 0; i < array.length; i++){
            array[i] = null;
        }
    }
    
    private static void clearArray(Boolean[] array, boolean value){
        for(int i = 0; i < array.length; i++){
            array[i] = value;
        }
    }
    
    private static void determineMatches(Boolean[] matches, String[] row1, String[] row2){
        for(int i = 4; i < row1.length; i++){
            if(!row1[i].equals(row2[i])){
                matches[i-7] = false;
            }
        }
    }
    
    private static String createHeader(Object[] dnaIds){
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
        for(int i = 1; i < annotationFormats.length; i++){
            AnnotationFormat af = annotationFormats[i];
            for(CustomField field : af.getCustomFields()){
                header += "##INFO=<ID=" + field.getColumnName().toUpperCase() + ",";
                switch(field.getColumnType()){
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
        for(int i = 0; i < dnaIds.length; i++){
            header += (String)dnaIds[i];
            if(i != dnaIds.length-1) header += "\t";
        }
        header += "\n";
        
        return header;
    }

}
