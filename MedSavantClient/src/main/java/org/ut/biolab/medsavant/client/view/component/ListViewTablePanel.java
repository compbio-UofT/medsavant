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
package org.ut.biolab.medsavant.client.view.component;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import com.jidesoft.grid.AutoFilterTableHeader;
import com.jidesoft.grid.AutoResizePopupMenuCustomizer;
import com.jidesoft.grid.FilterableTableModel;
import com.jidesoft.grid.QuickTableFilterField;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableHeaderPopupMenuInstaller;
import com.jidesoft.grid.TableModelWrapperUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class ListViewTablePanel extends JPanel {

    private static final Log LOG = LogFactory.getLog(ListViewTablePanel.class);
    private QuickTableFilterField filterField;
    private GenericTableModel model;
    private SortableTable table;
    private Object[][] data;
    private String[] columnNames;
    private Class[] columnClasses;
    private ColumnChooser columnChooser;
    private int[] hiddenColumns;
    private final JPanel fieldPanel;
    private float fontSize = 14.0f;
    //Maps a key to one or several row indices.  
    protected Map<Object, Set<Integer>> keyRowIndexMap;
    private Color evenRowColor = ViewUtil.evenRowColor;
    private Color oddRowColor = ViewUtil.oddRowColor;

    public ListViewTablePanel setEvenRowColor(Color c) {
        evenRowColor = c;
        return this;
    }

    public ListViewTablePanel setOddRowColor(Color c) {
        oddRowColor = c;
        return this;
    }

    public ListViewTablePanel(Object[][] data, String[] columnNames, Class[] columnClasses, int[] hiddenColumns) {
        this(data, columnNames, columnClasses, hiddenColumns, true, true, true, true);
    }

    public ListViewTablePanel(Object[][] data, String[] columnNames, Class[] columnClasses, int[] hiddenColumns,
            boolean allowSearch, boolean allowSort, boolean allowPages, boolean allowSelection) {


        this.hiddenColumns = hiddenColumns;
        table = new SortableTable() {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int Index_row, int Index_col) {
                Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
                //even index, selected or not selected

                if (isRowSelected(Index_row)) {
                    comp.setBackground(ViewUtil.detailSelectedBackground);
                } else {
                    if (Index_row % 2 == 0) {
                        comp.setBackground(evenRowColor);
                    } else {
                        comp.setBackground(oddRowColor);
                    }
                }
                comp.setForeground(ViewUtil.detailForeground);

                comp.setFont(comp.getFont().deriveFont(fontSize));

                return comp;
            }
        };

        table.setBorder(null);
        table.setSelectionForeground(Color.darkGray);
        table.setRowHeight(30);
        table.setClearSelectionOnTableDataChanges(true);
        table.setOptimized(true);
        table.setColumnAutoResizable(true);
        table.setAutoResort(false);
        //table.setDragEnabled(false);
        //table.setRowHeight(20);
        table.setSortable(allowSort);
        table.setSortingEnabled(allowSort);
        table.setFocusable(allowSelection);
        table.setCellSelectionEnabled(allowSelection);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        table.setAutoResizeMode(SortableTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);


        //table.setMinimumSize(new Dimension(500,999));
        //table.setPreferredSize(new Dimension(500,999));

        //column chooser
        TableHeaderPopupMenuInstaller installer = new TableHeaderPopupMenuInstaller(table);
        installer.addTableHeaderPopupMenuCustomizer(new AutoResizePopupMenuCustomizer());
        columnChooser = new ColumnChooser(table);
        installer.addTableHeaderPopupMenuCustomizer(columnChooser);

        AutoFilterTableHeader header = new AutoFilterTableHeader(table);
        header.setAutoFilterEnabled(true);
        header.setShowFilterIcon(true);
        header.setShowFilterName(true);
        table.setTableHeader(header);

        filterField = new QuickTableFilterField(model);
        filterField.setHintText("Type to search");

        setLayout(new BorderLayout(3, 3));
        fieldPanel = ViewUtil.getClearPanel();
        fieldPanel.setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.fill = GridBagConstraints.BOTH;
        if (allowSearch) {
            fieldPanel.add(filterField, gbc);
        }
        if (columnNames.length > 1) {
            JButton chooseColumnButton = new JButton("Fields");
            chooseColumnButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    columnChooser.showDialog();
                }
            });
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            fieldPanel.add(chooseColumnButton, gbc);
        }

        setTableModel(data, columnNames, columnClasses);

        if (allowSort) {
            add(fieldPanel, BorderLayout.NORTH);
        }

        JScrollPane jsp = new JScrollPane(table);
        jsp.setBorder(null);
        add(jsp, BorderLayout.CENTER);

        updateData(data);
        updateView();
    }

    public SortableTable getTable() {
        return table;
    }

    public final synchronized Object[][] getData() {
        return data;
    }

    public synchronized final void updateData(Object[][] newData) {
        data = newData;
        model.fireTableDataChanged();
        table.repaint();
    }

    protected Object getKey(Object[] row) {
        return row;
    }

    private void addToRowIndexMap(Object key, Integer rowIndex) {
        Set<Integer> rowIndices = keyRowIndexMap.get(key);
        if (rowIndices == null) {
            rowIndices = new HashSet<Integer>();
        }

        rowIndices.add(rowIndex);
        keyRowIndexMap.put(key, rowIndices);
    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    public final void updateView() {
        if (data == null) {
            return;
        }

        boolean first = false;
        if (model == null) {
            model = new GenericTableModel(data, columnNames, columnClasses);
            first = true;
        } else {
            java.util.Vector dataVec = model.getDataVector();
            dataVec.removeAllElements();
            int rowIndex = 0;
            keyRowIndexMap = new HashMap<Object, Set<Integer>>();
            for (Object[] row : data) {
                if (row == null) {
                    System.out.println("Got null row!");
                }
                dataVec.add(new java.util.Vector(Arrays.asList(row)));
                addToRowIndexMap(getKey(row), rowIndex++);
            }
        }

        if (first) {
            int[] columns = new int[columnNames.length];
            for (int i = 0; i < columns.length; i++) {
                columns[i] = i;
            }
            filterField.setTableModel(model);
            filterField.setColumnIndices(columns);
            filterField.setObjectConverterManagerEnabled(true);

            table.setModel(new FilterableTableModel(filterField.getDisplayTableModel()));
            columnChooser.hideColumns(hiddenColumns);

            int[] favColumns = new int[columnNames.length - hiddenColumns.length];
            int pos = 0;
            for (int i = 0; i < columnNames.length; i++) {
                boolean hidden = false;
                for (int j = 0; j < hiddenColumns.length; j++) {
                    if (hiddenColumns[j] == i) {
                        hidden = true;
                        break;
                    }
                }
                if (!hidden) {
                    favColumns[pos] = i;
                    pos++;
                }
            }
            columnChooser.setFavoriteColumns(favColumns);
        } else {
            model.fireTableDataChanged();
        }
    }

    private void setTableModel(Object[][] data, String[] columnNames, Class[] columnClasses) {
        this.data = data;
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;
        updateView();
    }

    public void setSelectionMode(int selectionMode) {
        table.setSelectionMode(selectionMode);
    }

    /**
     * Base class does nothing, but derived class may want to override this.
     */
    public void forceRefreshData() {
    }

    public void setFontSize(float newSize) {
        fontSize = newSize;

        Font newFont = table.getFont().deriveFont(newSize);
        table.setFont(newFont);
    }

    public void scrollToIndex(int index) {
        table.scrollRectToVisible(table.getCellRect(index, 0, true));
    }

    protected int[] getSelectedRows() {
        int[] selected = table.getSelectedRows();
        return TableModelWrapperUtils.getActualRowsAt(table.getModel(), selected, true);
    }

    protected void addRow(Object[] rowData) {
        Object[][] r = new Object[1][];
        r[0] = rowData;
        addRows(r);
    }

    protected void removeRow(int i) {
        removeRows(new int[]{i});
    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    protected Object[] getRowData(int i) {
        return ((java.util.Vector) model.getDataVector().elementAt(i)).toArray();
    }

    protected void removeRows(int[] rows) {
        Object[][] oldData = this.getData();
        Object[][] newData = new Object[oldData.length - rows.length][];

        Arrays.sort(rows);

        int ri = 0; //row index        
        int ndi = 0; //new data index        
        for (int odi = 0; odi < oldData.length; ++odi) {
            if ((ri < rows.length) && (rows[ri] == odi)) {
                ri++;
            } else {
                newData[ndi++] = oldData[odi];
            }
        }
        updateData(newData);
        updateView();
    }

    public void removeRows(Set<Object> keySet) {
        java.util.List<Integer> rowIndices = new ArrayList<Integer>(keySet.size());
        for (Object key : keySet) {
            Set<Integer> ri = keyRowIndexMap.get(key);
            if (ri != null) {
                rowIndices.addAll(ri);
            }
        }
        removeRows(ArrayUtils.toPrimitive(rowIndices.toArray(new Integer[rowIndices.size()])));
    }

    protected void addRows(Object[][] rows) {
        Object[][] oldData = getData();
        Object[][] newData = new Object[rows.length + oldData.length][];

        System.arraycopy(oldData, 0, newData, 0, oldData.length);
        System.arraycopy(rows, 0, newData, oldData.length, rows.length);
        updateData(newData);
        updateView();
    }

    protected void moveItemsFromRows(ListViewTablePanel toList, int[] rows, boolean copy) {
        Object[][] oldFromData = this.getData();
        Object[][] oldToData = toList.getData();
        Object[][] newToData = new Object[oldToData.length + rows.length][];
        System.arraycopy(oldToData, 0, newToData, 0, oldToData.length);
        int i = oldToData.length;
        if (copy) {
            for (int r : rows) {
                newToData[i++] = oldFromData[r];
            }
        } else {
            for (int r : rows) {
                newToData[i++] = oldFromData[r];
                oldFromData[r] = null;
            }
        }

        toList.updateData(newToData);
        toList.updateView();

        if (!copy) {
            Object[][] newFromData = new Object[oldFromData.length - rows.length][];
            i = 0;
            for (Object[] fromRow : oldFromData) {
                if (fromRow != null) {
                    newFromData[i++] = fromRow;
                }
            }
            this.updateData(newFromData);
            this.updateView();
        }
    }

    public int getNumSelected() {
        return getSelectedRows().length;
    }

    public Set<Object> getSelectedKeys() {
        Set<Object> selectedKeys = new HashSet<Object>();
        for (int row : getSelectedRows()) {
            Object[] rowData = getRowData(row);
            selectedKeys.add(rowData[0]);
        }
        return selectedKeys;
    }

    public void moveSelectedItems(final ListViewTablePanel toList) {
        LOG.debug("moveSelectedItems: Got keys " + getSelectedKeys());
        moveItems(toList, getSelectedKeys());
    }

    /**
     * Moves items corresponding to the row keys from 'this' list to the list
     * 'toList'. If 'toList'already contains a key for the item, the item is
     * removed from 'this' list but is not placed in the 'toList' list.
     */
    public void moveItems(final ListViewTablePanel toList, Set<Object> rowKeys) {
        copyItems(toList, rowKeys, false);
    }

    /**
     * Copies items corresponding to the row keys from 'this' list to the list
     * 'toList'. If 'toList' already contains a key for the item, the item is
     * not copied to the 'toList'. (i.e. a duplicate key is not introduced)
     */
    public void copyItems(final ListViewTablePanel toList, Set<Object> rowKeys) {
        copyItems(toList, rowKeys, true);
    }

    private void copyItems(final ListViewTablePanel toList, Set<Object> rowKeys, boolean copy) {
        Set<Integer> moveRowIndices = new HashSet<Integer>();
        Set<Integer> removeRowIndices = new HashSet<Integer>();
        for (Object rowKey : rowKeys) {
            Set<Integer> rowIndicesForObj = keyRowIndexMap.get(rowKey);
            if (rowIndicesForObj != null) {
                if (!toList.keyRowIndexMap.containsKey(rowKey)) {
                    moveRowIndices.addAll(rowIndicesForObj);
                } else {
                    removeRowIndices.addAll(rowIndicesForObj);
                }
            }

        }
        int[] mi = ArrayUtils.toPrimitive(moveRowIndices.toArray(new Integer[moveRowIndices.size()]));
        moveItemsFromRows(toList, mi, copy);
        if (!copy) {
            int[] ri = ArrayUtils.toPrimitive(removeRowIndices.toArray(new Integer[removeRowIndices.size()]));
            removeRows(ri);
        }
    }

    public boolean hasKey(Object key) {
        return keyRowIndexMap.containsKey(key);
    }

    public boolean hasRow(Object[] row) {
        return keyRowIndexMap.containsKey(getKey(row));
    }
}
