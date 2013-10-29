/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.util;

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
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.util.ExtensionFileFilter;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Andrew
 */
public class ExportTable {
    public static void exportTable(JTable table) throws FileNotFoundException, IOException {
        File out =
            DialogUtils.chooseFileForSave("Export Table", "table_export",
                ExtensionFileFilter.createFilters(new String[] {"xls", "xlsx", "csv"}), null);

        if (out == null) {
            return;
        }

        String extension = ClientMiscUtils.getExtension(out.getAbsolutePath());

        if (extension.equals("xls") || extension.equals("xlsx")) {
            exportExcel(out, table);
        } else { // default
            exportCSV(out, table);
        }
    }

    private static void exportExcel(File file, JTable table) throws FileNotFoundException, IOException {
        // create workbook
        Workbook wb;
        if (file.getAbsolutePath().endsWith(".xlsx")) {
            wb = new XSSFWorkbook();
        } else {
            wb = new HSSFWorkbook();
        }
        Sheet sheet = wb.createSheet();

        // add headers
        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        for (int j = 0; j < table.getColumnCount(); j++) {
            header.createCell(j).setCellValue(table.getColumnName(j));
        }

        // add cells
        for (int i = 0; i < table.getRowCount(); i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
            for (int j = 0; j < table.getColumnCount(); j++) {
                row.createCell(j).setCellValue(getString(table.getValueAt(i, j)));
            }
        }

        // write output
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fileOut.close();
    }

    private static void exportCSV(File file, JTable table) throws IOException {
        // setup file
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
        CSVWriter out = new CSVWriter(writer, ',', '"');

        // add headers
        String[] header = new String[table.getColumnCount()];
        for (int j = 0; j < table.getColumnCount(); j++) {
            header[j] = table.getColumnName(j);
        }
        out.writeNext(header);

        // add cells
        for (int i = 0; i < table.getRowCount(); i++) {
            String[] row = new String[table.getColumnCount()];
            for (int j = 0; j < table.getColumnCount(); j++) {
                row[j] = getString(table.getValueAt(i, j));
            }
            out.writeNext(row);
        }

        // close output
        out.close();
        writer.close();
    }

    private static String getString(Object o) {
        if (o == null) {
            return "";
        } else {
            return o.toString();
        }
    }
}
