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

package org.ut.biolab.medsavant.view.list;

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
