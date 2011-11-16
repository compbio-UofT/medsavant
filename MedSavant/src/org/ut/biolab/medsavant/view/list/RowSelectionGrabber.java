package org.ut.biolab.medsavant.view.list;

import com.jidesoft.grid.SortableTable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author mfiume
 */
public class RowSelectionGrabber {

    private final SortableTable table;
    private final List<Object[]> data;

    public RowSelectionGrabber(SortableTable t, List<Object[]> data) {
        this.table = t;
        this.data = data;
    }

    public List<Object[]> getSelectedItems() {
        //set all selected
        int[] allRows = table.getSelectedRows();
        int length = allRows.length;
        if (allRows.length > 0 && allRows[allRows.length - 1] >= data.size()) {
            length--;
        }
        List<Object[]> selected = new ArrayList<Object[]>();
        
        for (int i = 0; i < length; i++) {
            int currentRow = allRows[i];
            if (currentRow >= 0 && !data.isEmpty() && currentRow < data.size()) {
                selected.add(data.get(currentRow));
            }
        }
        

        return selected;
    }

    private Object[] toArray(Vector v) {
        Object[] o = new Object[v.size()];
        int i = 0;
        for (Object v1 : v) {
            o[i++] = v1;
        }
        return o;
    }
}
