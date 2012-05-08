package org.ut.biolab.medsavant.view.util;

import com.jidesoft.grid.SortableTable;

/**
 *
 * @author mfiume
 */
public class TableUtils {

    public static Object[] getRowFromTable(SortableTable table, int rowNumber) {
        int cols = table.getColumnCount();
        Object[] row = new Object[cols];
        for (int i = 0; i < cols; i++) {
            row[i] = table.getValueAt(rowNumber, i);
        }
        return row;
    }

}
