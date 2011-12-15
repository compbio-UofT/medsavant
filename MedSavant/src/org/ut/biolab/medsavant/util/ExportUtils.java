/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JTable;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ut.biolab.medsavant.db.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author Andrew
 */
public class ExportUtils {
    
    public static void exportTable(JTable table) throws FileNotFoundException, IOException{
        File out = DialogUtils.chooseFileForSave("Export Table", "table.xls", ExtensionFileFilter.createFilters(new String[]{"xls", "xlsx"}), null);
        
        if(out == null){
            return;
        }
        
        exportExcel(out.getAbsolutePath(), table);
    }
    
    private static void exportExcel(String filename, JTable table) throws FileNotFoundException, IOException{
        
        //create workbook
        Workbook wb;
        if(filename.endsWith(".xlsx")){
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
                row.createCell(j).setCellValue(table.getValueAt(i, j).toString());
            }
        }
        
        //write output
        FileOutputStream fileOut = new FileOutputStream(filename);
        wb.write(fileOut);
        fileOut.close();
    }
    
}
