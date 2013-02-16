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

package org.ut.biolab.medsavant.client.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import com.jidesoft.grid.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.util.DataRetriever;
import org.ut.biolab.medsavant.client.util.ExportTable;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class SearchableTablePanel extends JPanel {

    private static final Log LOG = LogFactory.getLog(SearchableTablePanel.class);
    private static final int ROWSPERPAGE_1 = 100;
    private static final int ROWSPERPAGE_2 = 500;
    private static final int ROWSPERPAGE_3 = 1000;

    private String pageName;
    private QuickTableFilterField filterField;
    private GenericTableModel model;
    private SortableTable table;
    private JPanel fieldPanel;
    private JLabel amountLabel;
    private int rowsPerPageX;
    private int pageNum = 1;
    private int numRowsPerPage = ROWSPERPAGE_2;
    private final JComboBox rowsPerPageDropdown;
    private JTextField rowsRetrievedBox;
    private int defaultRowsRetrieved = 1000;
    private static final int MAX_ROWS_RETRIEVED = 100000;
    private List<Object[]> data;
    private String[] columnNames;
    private Class[] columnClasses;
    private final JLabel pageLabel1;
    private final JLabel pageLabel2;
    private final JTextField pageText;
    private final JButton gotoFirst;
    private final JButton gotoPrevious;
    private final JButton gotoNext;
    private final JButton gotoLast;
    private ColumnChooser columnChooser;
    private int[] hiddenColumns;
    private DataRetriever<Object[]> retriever;
    private int totalNumRows;
    private GetDataWorker worker;
    private JButton exportButton;
    private List<Integer> selectedRows;
    private static Color SELECTED_COLOUR = new Color(244, 237, 147);
    private static Color DARK_COLOUR = ViewUtil.getAlternateRowColor();
    private final JPanel bottomPanel;
    private final JButton chooseColumnButton;

    public enum TableSelectionType {DISABLED, CELL, ROW}

    public SearchableTablePanel(String pageName, String[] columnNames, Class[] columnClasses, int[] hiddenColumns, int defaultRowsRetrieved, DataRetriever<Object[]> retriever) {
        this(pageName, columnNames, columnClasses, hiddenColumns, true, true, ROWSPERPAGE_2, true, TableSelectionType.ROW, defaultRowsRetrieved, retriever);
    }

    public SearchableTablePanel(String pageName, String[] columnNames, Class[] columnClasses, int[] hiddenColumns,
        boolean allowSearch, boolean allowSort, int defaultRows, boolean allowPages, TableSelectionType selectionType, int defaultRowsRetrieved, DataRetriever<Object[]> retriever) {

        this.pageName = pageName;
        this.rowsPerPageX = defaultRows;
        this.defaultRowsRetrieved = defaultRowsRetrieved;

        this.retriever = retriever;
        this.hiddenColumns = hiddenColumns;
        table = new SortableTable() {

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                synchronized (SearchableTablePanel.this) {
                    JComponent comp = (JComponent)super.prepareRenderer(renderer, row, col);

                    // Even index, selected or not selected
                    if (isCellSelected(row, col)) {
                        comp.setBackground(new Color(75, 149, 229));
                    } else if (selectedRows != null && selectedRows.contains(TableModelWrapperUtils.getActualRowAt(getModel(), row))) {
                        comp.setBackground(SELECTED_COLOUR);
                    } else if (row % 2 == 0 && !isCellSelected(row, col)) {
                        comp.setBackground(Color.WHITE);
                    } else {
                        comp.setBackground(DARK_COLOUR);
                    }

                    comp.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
                    return comp;
                }
            }

            @Override
            public String getToolTipText(MouseEvent e) {
                return getToolTip(TableModelWrapperUtils.getActualRowAt(table.getModel(), table.rowAtPoint(e.getPoint())));
            }
        };

        table.setToolTipText(""); //necessary to force check for tooltip text

        table.setClearSelectionOnTableDataChanges(true);
        table.setOptimized(true);
        table.setColumnAutoResizable(true);
        table.setAutoResort(false);
        table.setRowHeight(20);
        table.setSortable(allowSort);
        table.setSortingEnabled(allowSort);
        table.setFocusable(selectionType != TableSelectionType.DISABLED);
        //table.setCellSelectionEnabled(allowSelection);
        table.setCellSelectionEnabled(selectionType == TableSelectionType.CELL);
        table.setRowSelectionAllowed(selectionType == TableSelectionType.ROW);

        table.setAutoResizeMode(SortableTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

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

        if (allowPages) {
            filterField.setHintText("Type to search page");
        } else {
            filterField.setHintText("Type to search");
        }

        setLayout(new BorderLayout(3, 3));
        fieldPanel = ViewUtil.getClearPanel();

        if (allowSearch) {
            fieldPanel.add(filterField);
        }

        chooseColumnButton = new JButton("More Fields");
        chooseColumnButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                columnChooser.showDialog();
            }
        });
        fieldPanel.add(chooseColumnButton);

        exportButton = new JButton("Export Page");
        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    ExportTable.exportTable(table);
                } catch (Exception ex) {
                    LOG.error("Error while exporting.", ex);
                    DialogUtils.displayException("MedSavant", "<HTML>A problem occurred while exporting.<BR>Make sure the output file is not already in use.</HTML>", ex);
                }
            }
        });
        fieldPanel.add(exportButton);

        bottomPanel = ViewUtil.getClearPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        gotoFirst = niceButton();
        gotoPrevious = niceButton();
        gotoNext = niceButton();
        gotoLast = niceButton();

        gotoFirst.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FIRST));
        gotoPrevious.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.PREVIOUS));
        gotoNext.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.NEXT));
        gotoLast.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LAST));

        gotoFirst.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToFirstPage();
            }
        });
        gotoPrevious.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToPreviousPage();
            }
        });
        gotoNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToNextPage();
            }
        });
        gotoLast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToLastPage();
            }
        });

        pageText = new JTextField(); ViewUtil.makeSmall(pageText);
        pageText.setColumns(5);
        pageText.setMaximumSize(new Dimension(50,20));
        pageText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    try {
                        setPageNumber(Integer.parseInt(pageText.getText()));
                    } catch (NumberFormatException ex) {
                        setPageNumber(0);
                    }
                }
            }
        });

        amountLabel = new JLabel();
        ViewUtil.makeSmall(amountLabel);
        bottomPanel.add(amountLabel);

        pageLabel1 = new JLabel("Page "); ViewUtil.makeSmall(pageLabel1);
        pageLabel2 = new JLabel(); ViewUtil.makeSmall(pageLabel2);

        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(gotoFirst);
        bottomPanel.add(gotoPrevious);
        strut(bottomPanel);
        bottomPanel.add(pageLabel1);
        bottomPanel.add(pageText);
        bottomPanel.add(pageLabel2);
        strut(bottomPanel);
        bottomPanel.add(gotoNext);
        bottomPanel.add(gotoLast);
        bottomPanel.add(Box.createHorizontalGlue());

        strut(bottomPanel);

        JLabel perpageL = new JLabel("Per page:");
        ViewUtil.makeSmall(perpageL);
        bottomPanel.add(perpageL);


        strut(bottomPanel);


        boolean hasDefaultRowsPerPage = true;
        if (rowsPerPageX == ROWSPERPAGE_1 || rowsPerPageX == ROWSPERPAGE_2 || rowsPerPageX == ROWSPERPAGE_3) {
            hasDefaultRowsPerPage = false;
        }
        ArrayList<Integer> rowsList = new ArrayList<Integer>();
        rowsList.add(ROWSPERPAGE_1);
        rowsList.add(ROWSPERPAGE_2);
        rowsList.add(ROWSPERPAGE_3);
        Integer[] finalList = new Integer[3];
        if (hasDefaultRowsPerPage) {
            rowsList.add(rowsPerPageX);
            Collections.sort(rowsList);
        }
        finalList = rowsList.toArray(finalList);

        rowsPerPageDropdown = new JComboBox(finalList); ViewUtil.makeSmall(rowsPerPageDropdown);
        rowsPerPageDropdown.setPrototypeDisplayValue(ROWSPERPAGE_3);
        if (hasDefaultRowsPerPage) {
            rowsPerPageDropdown.setSelectedIndex(rowsList.indexOf(rowsPerPageX));
        } else {
            rowsPerPageDropdown.setSelectedIndex(1);
        }
        rowsPerPageDropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                int rowsPerPage = (Integer) cb.getSelectedItem();
                setNumRowsPerPage(rowsPerPage);
            }
        });
        rowsPerPageDropdown.setPreferredSize(new Dimension(100, 25));
        rowsPerPageDropdown.setMaximumSize(new Dimension(100, 25));
        bottomPanel.add(rowsPerPageDropdown);

        setTableModel(data, columnNames, columnClasses);

        JPanel tablePanel = new JPanel(new BorderLayout(3, 3));
        JScrollPane jsp = new JScrollPane(table);
        jsp.setBorder(null);
        tablePanel.add(jsp);

        if (allowSort) {
            add(fieldPanel, BorderLayout.NORTH);
        }

        if (allowPages) {
            add(bottomPanel, BorderLayout.SOUTH);
        }

        add(tablePanel,BorderLayout.CENTER);

        initEmpty();
    }

    public SortableTable getTable() {
        return table;
    }

    private synchronized void updateView(boolean newData) {
        if (worker != null) {
            worker.cancel(true);
        }
        worker = new GetDataWorker(pageName, newData);
        worker.execute();
    }

    public void setBottomBarVisible(boolean b) {
        this.bottomPanel.setVisible(b);
    }

    public void setChooseColumnsButtonVisible(boolean b) {
        this.chooseColumnButton.setVisible(b);
    }

    public void setExportButtonVisible(boolean b) {
        this.exportButton.setVisible(b);
    }

    private class GetDataWorker extends MedSavantWorker<List<Object[]>> {

        boolean update;

        protected GetDataWorker(String pageName, boolean newData) {
            super(pageName);
            this.update = newData;
        }

        @Override
        protected List<Object[]> doInBackground() throws Exception {
            if (update) {
                setTotalRowCount(retriever.getTotalNum());
                pageNum = 1;
            }
            return retriever.retrieve((pageNum-1) * getRowsPerPage(), getRowsPerPage());
        }

        @Override
        protected void showProgress(double fraction) {
            //do nothing
        }

        @Override
        protected void showSuccess(List<Object[]> result) {
            applyData(result);
            retriever.retrievalComplete();
        }
    }

    /**
     * Initialise the table to an empty state.
     */
    private void initEmpty() {
        model = new GenericTableModel(new Object[0][0], columnNames, columnClasses);

        gotoFirst.setEnabled(false);
        gotoPrevious.setEnabled(false);
        gotoNext.setEnabled(false);
        gotoLast.setEnabled(false);

        pageText.setText("0");
        pageLabel2.setText(" of 0");
        amountLabel.setText("  Showing 0 - 0 of 0");

        int[] columns = new int[columnNames.length];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = i;
        }
        filterField.setTableModel(model);
        filterField.setColumnIndices(columns);
        filterField.setObjectConverterManagerEnabled(true);

        //table.setModel(model);
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
    }

    /**
     * Brute-force replacement of current table data with the given data, blowing away the current table selection.
     *
     * @param pageData the new data
     */
    @SuppressWarnings("UseOfObsoleteCollectionType")
    public synchronized void applyData(List<Object[]> pageData) {

        if (pageData != null) {
            // We can't call setDataVector directly because that blows away any custom table renderers we've set.
            java.util.Vector v = model.getDataVector();
            v.removeAllElements();
            for (Object[] r: pageData) {
                v.add(new java.util.Vector(Arrays.asList(r)));
            }
        }

        gotoFirst.setEnabled(true);
        gotoPrevious.setEnabled(true);
        gotoNext.setEnabled(true);
        gotoLast.setEnabled(true);

        if (pageNum == 1 || pageNum == 0) {
            gotoFirst.setEnabled(false);
            gotoPrevious.setEnabled(false);
        }
        if (pageNum == getTotalNumPages() || pageNum == 0) {
            gotoNext.setEnabled(false);
            gotoLast.setEnabled(false);
        }

        pageText.setText(Integer.toString(getPageNumber()));
        pageLabel2.setText(" of " + ViewUtil.numToString(getTotalNumPages()));
        int start = getTotalNumPages() == 0 ? 0 : (getPageNumber() - 1) * getRowsPerPage() + 1;
        int end = getTotalNumPages() == 0 ? 0 : Math.min(start + getRowsPerPage() - 1, getTotalRowCount());
        amountLabel.setText("  Showing " + ViewUtil.numToString(start) + " - " + ViewUtil.numToString(end) + " of " + ViewUtil.numToString(getTotalRowCount()));

        model.fireTableDataChanged();
    }

    private void setTableModel(List<Object[]> data, String[] columnNames, Class[] columnClasses) {
        if (data == null) {
            this.data = new ArrayList<Object[]>();
        } else {
            this.data = data;
        }
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;
    }

    public void setNumRowsPerPage(int num) {
        this.numRowsPerPage = num;
        this.goToFirstPage();
    }

    private void strut(JPanel p) {
        p.add(Box.createHorizontalStrut(5));
    }

    private void setPageNumber(int i) {
        if (0 == getTotalNumPages()) {
            i = 1;
        } else if (i > getTotalNumPages()) {
            i = getTotalNumPages();
        } else if (i < 1) {
            i = 1;
        }
        this.pageNum = i;
        this.updateView(false);
    }

    public int getPageNumber() {
        return this.pageNum;
    }

    private void goToFirstPage() {
        setPageNumber(1);
    }

    private void goToLastPage() {
        setPageNumber(getTotalNumPages());
    }

    private void goToNextPage() {
        setPageNumber(pageNum + 1);
    }

    private void goToPreviousPage() {
        setPageNumber(pageNum - 1);
    }

    private int getTotalRowCount() {
        return this.totalNumRows;
    }

    private void setTotalRowCount(int num) {
        this.totalNumRows = num;
    }

    private int getTotalNumPages() {
        return (int) Math.ceil(((double) getTotalRowCount()) / getRowsPerPage());
    }

    public int getRowsPerPage() {
        return this.numRowsPerPage;
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

    public int getRetrievalLimit() {
        int limit;
        try {
            limit = Integer.parseInt(this.rowsRetrievedBox.getText());
        } catch (NumberFormatException ex) {
            rowsRetrievedBox.setText(String.valueOf(defaultRowsRetrieved));
            return defaultRowsRetrieved;
        }
        if (limit > MAX_ROWS_RETRIEVED) {
            rowsRetrievedBox.setText(String.valueOf(MAX_ROWS_RETRIEVED));
            return MAX_ROWS_RETRIEVED;
        }
        return limit;
    }

    public void forceRefreshData() {
        updateView(true);
    }

    public void setExportButtonEnabled(boolean enable) {
        exportButton.setEnabled(enable);
    }

    public int getActualRowAt(int row) {

        return TableModelWrapperUtils.getActualRowAt(table.getModel(), row);
    }

    public int getActualRowAcrossAllPages(int row) {
        return row+((this.getPageNumber()-1)*rowsPerPageX);
    }

    public void setSelectedRows(List<Integer> rows) {
        this.selectedRows = rows;
    }

    public boolean isRowSelected(int row) {
        if (selectedRows == null) return false;
        return selectedRows.contains(row);
    }

    public void addSelectedRow(Integer row) {
        selectedRows.add(row);
    }

    public void removeSelectedRow(Integer row) {
        while (selectedRows.remove(row)) {
        }
    }

    public void addSelectedRows(List<Integer> rows) {
        selectedRows.addAll(rows);
    }

    public String getToolTip(int actualRow) {
        return null;
    }

}
