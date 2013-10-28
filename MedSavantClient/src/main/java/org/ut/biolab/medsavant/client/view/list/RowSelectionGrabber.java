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
package org.ut.biolab.medsavant.client.view.list;

import java.util.ArrayList;
import java.util.List;

import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableModelWrapperUtils;


/**
 *
 * @author mfiume
 */
public class RowSelectionGrabber {

    private final SortableTable table;
    private final Object[][] data;

    public RowSelectionGrabber(SortableTable t, Object[][] data) {
        this.table = t;
        this.data = data;
    }

    public List<Object[]> getSelectedItems() {
        //set all selected
        int[] allRows = table.getSelectedRows();
        int length = allRows.length;
        if (allRows.length > 0 && allRows[allRows.length - 1] >= data.length) {
            length--;
        }
        List<Object[]> selected = new ArrayList<Object[]>();

        for (int i = 0; i < length; i++) {
            int currentRow = getActualRowAt(allRows[i]);
            if (currentRow >= 0 && data.length > 0 && currentRow < data.length) {
                selected.add(data[currentRow]);
            }
        }

        return selected;
    }

    public int getActualRowAt(int row){
        return TableModelWrapperUtils.getActualRowAt(table.getModel(), row);
    }
}
