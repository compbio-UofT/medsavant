/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fiume.table;

import com.jidesoft.grid.AutoFilterTableHeader;
import com.jidesoft.grid.AutoResizePopupMenuCustomizer;
import com.jidesoft.grid.FilterableTableModel;
import com.jidesoft.grid.QuickTableFilterField;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableColumnChooserPopupMenuCustomizer;
import com.jidesoft.grid.TableHeaderPopupMenuInstaller;
import fiume.table.images.IconFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class SearchableTablePanel extends JPanel {

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
    private Vector data;
    private List<String> columnNames;
    private List<Class> columnClasses;
    private final JLabel pageLabel;
    private final JButton gotoFirst;
    private final JButton gotoPrevious;
    private final JButton gotoNext;
    private final JButton gotoLast;
    private ColumnChooser columnChooser;
    private List<Integer> hiddenColumns;

    public SortableTable getTable() {
        return table;
    }

    public synchronized void updateData(Vector data) {
        this.data = data;
        this.setPageNumber(1); // updates the view
        model.fireTableDataChanged();
        table.repaint();
    }

    public void updateView() {

        if (data == null) {
            return;
        }

        List<List> pageData = getDataOnPage(this.getPageNumber(), data);

        boolean first = false;
        if (model == null) {
            model = new GenericTableModel(pageData, columnNames, columnClasses);
            first = true;
        } else {
            model.getDataVector().removeAllElements();
            model.getDataVector().addAll(pageData);
        }

        this.gotoFirst.setEnabled(true);
        this.gotoPrevious.setEnabled(true);
        this.gotoNext.setEnabled(true);
        this.gotoLast.setEnabled(true);

        if (pageNum == 1 || pageNum == 0) {
            this.gotoFirst.setEnabled(false);
            this.gotoPrevious.setEnabled(false);
        }
        if (pageNum == getTotalNumPages() || pageNum == 0) {
            this.gotoNext.setEnabled(false);
            this.gotoLast.setEnabled(false);
        }


        pageLabel.setText("Page " + this.getPageNumber() + " of " + this.getTotalNumPages());
        int start = getTotalNumPages() == 0 ? 0 : (this.getPageNumber() - 1) * this.getRowsPerPage() + 1;
        int end = getTotalNumPages() == 0 ? 0 : Math.min(start + this.getRowsPerPage() - 1, this.getTotalRowCount());
        amountLabel.setText("Showing " + start + " - " + end + " of " + data.size() + " records");

        if (first) {
            int[] columns = new int[columnNames.size()];
            for (int i = 0; i < columns.length; i++) {
                columns[i] = i;
            }
            filterField.setTableModel(model);
            filterField.setColumnIndices(columns);           
            filterField.setObjectConverterManagerEnabled(true);
            //filterField.setSearchingColumnIndices(columns);
            //filterField.setTable(table);

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
    
    public SearchableTablePanel(Vector data, List<String> columnNames, List<Class> columnClasses, List<Integer> hiddenColumns, int defaultRowsRetrieved) {
        this(data, columnNames, columnClasses, hiddenColumns, true, true, ROWSPERPAGE_2, true, true, defaultRowsRetrieved);
    }

    public SearchableTablePanel(
        Vector data, List<String> columnNames, List<Class> columnClasses, List<Integer> hiddenColumns,
        boolean allowSearch, boolean allowSort, int defaultRows, boolean allowPages, boolean allowSelection, int defaultRowsRetrieved) {

        this.ROWSPERPAGE_X = defaultRows;
        this.DEFAULT_ROWS_RETRIEVED = defaultRowsRetrieved;

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
        //table.setDragEnabled(false);
        table.setRowHeight(20);
        table.setSortable(allowSort);
        table.setSortingEnabled(allowSort);
        table.setFocusable(allowSelection);
        table.setCellSelectionEnabled(allowSelection);
        //table.setFont(new Font("Times New Roman", Font.PLAIN, 14));

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
        //filterField.setColumns(50);
        filterField.setHintText("Type to search");

        this.setLayout(new BorderLayout(3, 3));
        fieldPanel = new JPanel();

        if (allowSearch) {
            fieldPanel.add(filterField);
        }

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        gotoFirst = niceButton();//new JButton();
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

        amountLabel = new JLabel();
        bottomPanel.add(amountLabel);

        pageLabel = new JLabel();

        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(gotoFirst);
        bottomPanel.add(gotoPrevious);
        strut(bottomPanel);
        bottomPanel.add(pageLabel);
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
        rowsPerPageDropdown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                int rowsPerPage = (Integer) cb.getSelectedItem();
                setNumRowsPerPage(rowsPerPage);
            }
        });
        if (hasDefaultRowsPerPage) {
            rowsPerPageDropdown.setSelectedIndex(rowsList.indexOf(ROWSPERPAGE_X));
        } else {
            rowsPerPageDropdown.setSelectedIndex(1);
        }
        rowsPerPageDropdown.setPreferredSize(new Dimension(50, 25));
        rowsPerPageDropdown.setMaximumSize(new Dimension(50, 25));       
        bottomPanel.add(rowsPerPageDropdown);

        bottomPanel.add(new JLabel("   Results retrieved: "));
        rowsRetrievedBox = new JTextField();
        rowsRetrievedBox.setColumns(5);
        rowsRetrievedBox.setMaximumSize(new Dimension(50,25));
        rowsRetrievedBox.setText(String.valueOf(DEFAULT_ROWS_RETRIEVED));
        bottomPanel.add(rowsRetrievedBox);
        
        rowsRetrievedBox.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    forceRefreshData();
                }
            }                
        });
        
        setTableModel(
                data,
                Util.listToVector(columnNames),
                Util.listToVector(columnClasses));

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
        
        this.updateData(data);
    }

    private void setNumRowsPerPage(int num) {
        this.numRowsPerPage = num;
        this.goToFirstPage();
        updateView();
    }

    private void strut(JPanel p) {
        p.add(Box.createHorizontalStrut(5));
    }

    private void setPageNumber(int i) {
        if (0 == getTotalNumPages()) {
            i = 0;
        } else if (i > getTotalNumPages()) {
            i = getTotalNumPages();
        } else if (i < 1) {
            i = 1;
        }
        this.pageNum = i;
        this.updateView();
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
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    private int getTotalNumPages() {
        return (int) Math.ceil(((double) getTotalRowCount()) / getRowsPerPage());
    }

    public int getRowsPerPage() {
        return this.numRowsPerPage;
    }

    private List<List> getDataOnPage(int pageNumber, List<List> data) {

        List<List> result = new ArrayList<List>();

        if (pageNumber == 0) {
            return result;
        }

        int start = (pageNumber - 1) * this.getRowsPerPage();
        int end = start + this.getRowsPerPage();

        if (data != null) {
            for (int i = start; i < end && i < data.size(); i++) {
                result.add(data.get(i));
            }
        }
        return result;
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
