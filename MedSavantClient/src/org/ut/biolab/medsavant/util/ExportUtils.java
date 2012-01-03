/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.util;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JTable;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ut.biolab.medsavant.db.util.shared.ExtensionFileFilter;
import org.ut.biolab.medsavant.db.util.shared.MiscUtils;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author Andrew
 */
public class ExportUtils {
    
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
}
