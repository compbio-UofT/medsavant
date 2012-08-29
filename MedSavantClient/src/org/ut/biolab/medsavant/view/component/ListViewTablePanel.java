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
package org.ut.biolab.medsavant.view.component;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
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

import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author mfiume, AndrewBrook
 */
public class ListViewTablePanel extends JPanel {

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

    public ListViewTablePanel(Object[][] data, String[] columnNames, Class[] columnClasses, int[] hiddenColumns) {
        this(data, columnNames, columnClasses, hiddenColumns, true, true, true, true, true);
    }

    public ListViewTablePanel(Object[][] data, String[] columnNames, Class[] columnClasses, int[] hiddenColumns,
        boolean allowSearch, boolean allowSort, boolean allowPages, boolean allowSelection, boolean allowHideShow) {


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
                        comp.setBackground(ViewUtil.evenRowColor);
                    } else {
                        comp.setBackground(ViewUtil.oddRowColor);
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

        table.setMinimumSize(new Dimension(500,999));
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
        gbc.fill = GridBagConstraints.HORIZONTAL;
        if (allowSearch) {
            fieldPanel.add(filterField, gbc);
        }
        if (allowHideShow) {
            JButton chooseColumnButton = new JButton("<html><center><font size=\"-2\">Show/Hide<br>Fields</font></center></html>");
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
    }

    public SortableTable getTable() {
        return table;
    }

    public final synchronized Object[][] getData() {
        return data;
    }

    public final synchronized void updateData(Object[][] newData) {
        data = newData;
        model.fireTableDataChanged();
        table.repaint();
    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    public void updateView() {

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
            for (Object[] row: data) {
                dataVec.add(new java.util.Vector(Arrays.asList(row)));
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

    public int[] getSelectedRows() {
        int[] selected = table.getSelectedRows();
        return TableModelWrapperUtils.getActualRowsAt(table.getModel(), selected, true);
    }

    public void addRow(Object[] rowData) {
        model.addRow(rowData);
    }

    public void removeRow(int i) {
        model.removeRow(i);
    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    public Object[] getRowData(int i) {
        return ((java.util.Vector)model.getDataVector().elementAt(i)).toArray();
    }
}
