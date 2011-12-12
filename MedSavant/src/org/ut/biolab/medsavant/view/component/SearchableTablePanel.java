/*
 *    Copyright 2011 University of Toronto
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import com.jidesoft.grid.*;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.component.Util.DataRetriever;
import org.ut.biolab.medsavant.view.images.IconFactory;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class SearchableTablePanel extends JPanel {

    private String pageName;
    private QuickTableFilterField filterField;
    private GenericTableModel model;
    private SortableTable table;
    private JPanel fieldPanel;
    private JLabel amountLabel;
    private static final int ROWSPERPAGE_1 = 100;
    private static final int ROWSPERPAGE_2 = 500;
    private static final int ROWSPERPAGE_3 = 1000;
    private static int ROWSPERPAGE_X;
    private int pageNum = 1;
    private int numRowsPerPage = ROWSPERPAGE_2;
    private final JComboBox rowsPerPageDropdown;
    private JTextField rowsRetrievedBox;
    private int DEFAULT_ROWS_RETRIEVED = 1000;
    private static final int MAX_ROWS_RETRIEVED = 100000;
    private List<Object[]> data;
    private List<String> columnNames;
    private List<Class> columnClasses;
    private final JLabel pageLabel1;
    private final JLabel pageLabel2;
    private final JTextField pageText;
    private final JButton gotoFirst;
    private final JButton gotoPrevious;
    private final JButton gotoNext;
    private final JButton gotoLast;
    private ColumnChooser columnChooser;
    private List<Integer> hiddenColumns;
    private DataRetriever retriever;
    private int totalNumRows;
    private GetDataSwingWorker worker;
    
    public enum TableSelectionType {DISABLED, CELL, ROW}

    public SortableTable getTable() {
        return table;
    }
    
    private synchronized void updateView(boolean newData){
        if (worker != null) worker.cancel(true);
        (worker = new GetDataSwingWorker(pageName, newData)).execute();
    }
    
    private class GetDataSwingWorker extends MedSavantWorker {
        
        boolean update;
        
        protected GetDataSwingWorker(String pageName, boolean newData){
            super(pageName);
            this.update = newData;
        }
        
        @Override
        protected List<Object[]> doInBackground() {
            try {
                if(this.isThreadCancelled()) return null;
                if(update){
                    setTotalRowCount(retriever.getTotalNum());
                    pageNum = 1;
                }
                if(this.isThreadCancelled()) return null;
                return retriever.retrieve((pageNum-1) * getRowsPerPage(), getRowsPerPage());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void showProgress(double fraction) {
            //do nothing
        }

        @Override
        protected void showSuccess(Object result) {
            List<Object[]> pageData = (List<Object[]>)result;
            applyData(pageData);
            retriever.retrievalComplete();
        }
    }
         
    public void applyData(List<Object[]> pageData){
        
        boolean first = false;
        if (model == null) {
            model = new GenericTableModel(pageData, columnNames, columnClasses);
            first = true;
        } else {
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
        pageLabel2.setText(" of " + getTotalNumPages());
        int start = getTotalNumPages() == 0 ? 0 : (getPageNumber() - 1) * getRowsPerPage() + 1;
        int end = getTotalNumPages() == 0 ? 0 : Math.min(start + getRowsPerPage() - 1, getTotalRowCount());
        amountLabel.setText("Showing " + start + " - " + end + " of " + getTotalRowCount() + " records");

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
    

    private void setTableModel(List<Object[]> data, List<String> columnNames, List<Class> columnClasses) {
        if (data == null) {
            this.data = new ArrayList<Object[]>();
        } else {
            this.data = data;
        }
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;
    }
    
    public SearchableTablePanel(String pageName, List<String> columnNames, List<Class> columnClasses, List<Integer> hiddenColumns, int defaultRowsRetrieved, DataRetriever retriever) {
        this(pageName, columnNames, columnClasses, hiddenColumns, true, true, ROWSPERPAGE_2, true, TableSelectionType.ROW, defaultRowsRetrieved, retriever);
    }

    public SearchableTablePanel(String pageName, List<String> columnNames, List<Class> columnClasses, List<Integer> hiddenColumns,
        boolean allowSearch, boolean allowSort, int defaultRows, boolean allowPages, TableSelectionType selectionType, int defaultRowsRetrieved, DataRetriever retriever) {

        this.pageName = pageName;
        this.ROWSPERPAGE_X = defaultRows;
        this.DEFAULT_ROWS_RETRIEVED = defaultRowsRetrieved;

        this.retriever = retriever;
        this.hiddenColumns = hiddenColumns;
        table = new SortableTable() {

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int Index_row, int Index_col) {
                Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
                //even index, selected or not selected

                if (isCellSelected(Index_row, Index_col)) {
                    comp.setBackground(new Color(75, 149, 229));

                } else {
                    if (Index_row % 2 == 0 && !isCellSelected(Index_row, Index_col)) {
                        comp.setBackground(Color.white);
                    } else {
                        comp.setBackground(new Color(242, 245, 249));
                    }
                }
                return comp;
            }
        };

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
        columnChooser = new ColumnChooser();
        installer.addTableHeaderPopupMenuCustomizer(columnChooser);

        AutoFilterTableHeader header = new AutoFilterTableHeader(table);
        header.setAutoFilterEnabled(true);
        header.setShowFilterIcon(true);
        header.setShowFilterName(true);
        table.setTableHeader(header);

        filterField = new QuickTableFilterField(model);
        filterField.setHintText("Type to search");

        this.setLayout(new BorderLayout(3, 3));
        fieldPanel = new JPanel();

        if (allowSearch) {
            fieldPanel.add(filterField);
        }
        
        JButton chooseColumnButton = new JButton("Choose Columns");
        chooseColumnButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                columnChooser.showDialog();
            }
        });
        fieldPanel.add(chooseColumnButton);

        JPanel bottomPanel = new JPanel();
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

            public void actionPerformed(ActionEvent e) {
                goToFirstPage();
            }
        });
        gotoPrevious.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                goToPreviousPage();
            }
        });
        gotoNext.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                goToNextPage();
            }
        });
        gotoLast.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                goToLastPage();
            }
        });
        
        pageText = new JTextField();
        pageText.setColumns(5);
        pageText.setMaximumSize(new Dimension(50,20));
        pageText.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    try {
                        setPageNumber(Integer.parseInt(pageText.getText()));
                    } catch (NumberFormatException ex){
                        setPageNumber(0);
                    }
                }
            }                
        });

        amountLabel = new JLabel();
        bottomPanel.add(amountLabel);

        pageLabel1 = new JLabel("Page ");       
        pageLabel2 = new JLabel();

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

        bottomPanel.add(new JLabel("Results per page:"));


        strut(bottomPanel);


        boolean hasDefaultRowsPerPage = true;
        if (ROWSPERPAGE_X == ROWSPERPAGE_1 || ROWSPERPAGE_X == ROWSPERPAGE_2 || ROWSPERPAGE_X == ROWSPERPAGE_3) {
            hasDefaultRowsPerPage = false;
        }
        ArrayList<Integer> rowsList = new ArrayList<Integer>();
        rowsList.add(ROWSPERPAGE_1);
        rowsList.add(ROWSPERPAGE_2);
        rowsList.add(ROWSPERPAGE_3);
        Integer[] finalList = new Integer[3];
        if (hasDefaultRowsPerPage) {
            rowsList.add(ROWSPERPAGE_X);
            Collections.sort(rowsList);
        }
        finalList = rowsList.toArray(finalList);

        rowsPerPageDropdown = new JComboBox(finalList);
        rowsPerPageDropdown.setPrototypeDisplayValue(ROWSPERPAGE_3);
        if (hasDefaultRowsPerPage) {
            rowsPerPageDropdown.setSelectedIndex(rowsList.indexOf(ROWSPERPAGE_X));
        } else {
            rowsPerPageDropdown.setSelectedIndex(1);
        }
        rowsPerPageDropdown.addActionListener(new ActionListener() {

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
            this.add(fieldPanel, BorderLayout.BEFORE_FIRST_LINE);
        }
        
        if (allowPages) {
            this.add(bottomPanel, BorderLayout.AFTER_LAST_LINE);
        }

        this.add(tablePanel);
        
        applyData(new ArrayList<Object[]>());
        //updateView(true);
    }

    private void setNumRowsPerPage(int num) {
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

    public int getRetrievalLimit(){
        int limit;
        try {
            limit = Integer.parseInt(this.rowsRetrievedBox.getText());
        } catch (NumberFormatException ex){
            rowsRetrievedBox.setText(String.valueOf(DEFAULT_ROWS_RETRIEVED));
            return DEFAULT_ROWS_RETRIEVED;
        }
        if(limit > MAX_ROWS_RETRIEVED){
            rowsRetrievedBox.setText(String.valueOf(MAX_ROWS_RETRIEVED));
            return MAX_ROWS_RETRIEVED;
        }
        return limit;
    }
    
    public void forceRefreshData(){
        updateView(true);
    }
    
    private class ColumnChooser extends TableColumnChooserPopupMenuCustomizer {

        public void hideColumns(JTable table, List<Integer> indices) {
            for (Integer i : indices) {
                hideColumn(table, i);
            }
        }
        
        public void showDialog(){
            TableColumnChooserDialog dialog = super.createTableColumnChooserDialog(MainFrame.getInstance(), "Choose Columns to Display", table);
            dialog.setPreferredSize(new Dimension(300,500));
            dialog.setSize(new Dimension(300,500));
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
        
    }   
    
    public int getActualRowAt(int row){
        return TableModelWrapperUtils.getActualRowAt(table.getModel(), row);
    }

}
