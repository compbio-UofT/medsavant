package org.ut.biolab.medsavant.view.component;

import com.jidesoft.grid.AutoFilterTableHeader;
import com.jidesoft.grid.AutoResizePopupMenuCustomizer;
import com.jidesoft.grid.FilterableTableModel;
import com.jidesoft.grid.QuickTableFilterField;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableColumnChooserPopupMenuCustomizer;
import com.jidesoft.grid.TableHeaderPopupMenuInstaller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class ListViewTablePanel extends JPanel {

    private QuickTableFilterField filterField;
    private GenericTableModel model;
    private SortableTable table;
    private Vector data;
    private List<String> columnNames;
    private List<Class> columnClasses;
    private ColumnChooser columnChooser;
    private List<Integer> hiddenColumns;
    private final JPanel fieldPanel;

    public SortableTable getTable() {
        return table;
    }

    public synchronized void updateData(Vector data) {
        this.data = data;
        model.fireTableDataChanged();
        table.repaint();
    }

    public void updateView() {

        if (data == null) {
            return;
        }

        boolean first = false;
        if (model == null) {
            model = new GenericTableModel(data, columnNames, columnClasses);
            first = true;
        } else {
            model.getDataVector().removeAllElements();
            model.getDataVector().addAll(data);
        }


        if (first) {
            int[] columns = new int[columnNames.size()];
            for (int i = 0; i < columns.length; i++) {
                columns[i] = i;
            }
            filterField.setTableModel(model);
            filterField.setColumnIndices(columns);           
            filterField.setObjectConverterManagerEnabled(true);

            table.setModel(new FilterableTableModel(filterField.getDisplayTableModel()));
            columnChooser.hideColumns(table, hiddenColumns);

            int[] favColumns = new int[columnNames.size() - hiddenColumns.size()];
            int pos = 0;
            for (int i = 0; i < columnNames.size(); i++) {
                if (!hiddenColumns.contains(i)) {
                    favColumns[pos] = i;
                    pos++;
                }
            }
            columnChooser.setFavoriteColumns(favColumns);
        } else {
            model.fireTableDataChanged();
        }
    }

    private void setTableModel(Vector data, List<String> columnNames, List<Class> columnClasses) {
        this.data = data;
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;
        updateView();
    }
    
    public ListViewTablePanel(Vector data, List<String> columnNames, List<Class> columnClasses, List<Integer> hiddenColumns) {
        this(data, columnNames, columnClasses, hiddenColumns, true, true, true, true);
    }

    public ListViewTablePanel(
        Vector data, List<String> columnNames, List<Class> columnClasses, List<Integer> hiddenColumns,
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
                        comp.setBackground(ViewUtil.evenRowColor);
                    } else {
                        comp.setBackground(ViewUtil.oddRowColor);
                    }
                }
                comp.setForeground(ViewUtil.detailForeground);
                
                comp.setFont(new Font((comp.getFont().getFamily()),Font.PLAIN,14));
                
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
        //table.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        table.setAutoResizeMode(SortableTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        
        table.setMinimumSize(new Dimension(500,999));
        //table.setPreferredSize(new Dimension(500,999));

        //column chooser
        TableHeaderPopupMenuInstaller installer = new TableHeaderPopupMenuInstaller(table);
        installer.addTableHeaderPopupMenuCustomizer(new AutoResizePopupMenuCustomizer());
        columnChooser = new ColumnChooser();
        installer.addTableHeaderPopupMenuCustomizer(columnChooser);

        AutoFilterTableHeader header = new AutoFilterTableHeader(table);
        header.setAutoFilterEnabled(true);
        header.setShowFilterIcon(true);
        header.setShowFilterName(true);
        table.setTableHeader(header);

        filterField = new QuickTableFilterField(model);
        filterField.setHintText("Type to search");

        this.setLayout(new BorderLayout());
        fieldPanel = new JPanel();

        if (allowSearch) {
            fieldPanel.add(filterField);
        }
        
        setTableModel(
                data,
                Util.listToVector(columnNames),
                Util.listToVector(columnClasses));

        if (allowSort) {
            this.add(fieldPanel, BorderLayout.NORTH);
        }
        
        JScrollPane jsp = new JScrollPane(table);
        jsp.setBorder(null);
        this.add(jsp, BorderLayout.CENTER);

        this.updateData(data);
    }
    
    private int getTotalRowCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    private JButton niceButton() {
        JButton b = new JButton();
        b.setBorder(null);
        b.setBorderPainted(false);
        b.setOpaque(false);
        return b;
    }

    public void setSelectionMode(int selectionMode) {
        table.setSelectionMode(selectionMode);
    }

    public void forceRefreshData(){
        //override this in parent
    }

    
    
    private class ColumnChooser extends TableColumnChooserPopupMenuCustomizer {

        public void hideColumns(JTable table, List<Integer> indices) {
            for (Integer i : indices) {
                hideColumn(table, i);
            }
        }
    }
}
