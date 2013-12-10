/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.component;

import com.jidesoft.grid.AutoFilterTableHeader;
import com.jidesoft.grid.AutoResizePopupMenuCustomizer;
import com.jidesoft.grid.FilterableTableModel;
import com.jidesoft.grid.QuickTableFilterField;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableHeaderPopupMenuInstaller;
import com.jidesoft.grid.TableModelWrapperUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.swing.table.TableCellRenderer;

import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker.StateValue;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.util.DataRetriever;
import org.ut.biolab.medsavant.client.util.ExportTable;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume, AndrewBrook, rammar
 */
public class SearchableTablePanel extends JPanel {

    private static final Log LOG = LogFactory.getLog(SearchableTablePanel.class);
    private static final int ROWSPERPAGE_1 = 100;
    private static final int ROWSPERPAGE_2 = 500;
    private static final int ROWSPERPAGE_3 = 1000;
    private static int KEY_PRESS_TIMER_INTERVAL = 200; //should be greater than key repeat rate
    private static final int KEY_PRESS_TIMER_INTERVAL_LONG = 1000; //should be greater than key initial delay.
    private static final int KEY_PRESS_TIMER_INTERVAL_AUTOADJUST_RUNS = 5;
    private static final int KEY_PRESS_INTERVAL_EPSILON = 5;
    private static final int KEY_PRESS_TIMER_INTERVAL_AUTOADJUST_PADDING = 20;
    private static final int[] SCROLLING_KEYS = new int[]{KeyEvent.VK_DOWN, KeyEvent.VK_UP, KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_PAGE_UP, KeyEvent.VK_KP_DOWN, KeyEvent.VK_KP_UP};
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
    private JButton helpButton;
    private List<Integer> selectedRows;
    private Set<Integer> toggledRows;
    private static Color SELECTED_COLOUR = new Color(244, 237, 147);
    private static Color DARK_COLOUR = ViewUtil.getAlternateRowColor();
    private final JPanel bottomPanel;
    private final JButton chooseColumnButton;
    private long lastTime; //Only used for debugging
    private boolean waitlong = false;
    private boolean keydown = false;
    private SelectionChangedWorker selectionChangedWorker;
	
	
    public enum TableSelectionType {

        DISABLED, CELL, ROW
    }

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
                    JComponent comp = (JComponent) super.prepareRenderer(renderer, row, col);

                    // Even index, selected or not selected
                    if (isRowToggled(TableModelWrapperUtils.getActualRowAt(this.getSortableTableModel(), row))) { //this.getActualRowAt(this.getSortedRowAt(row)))) {
                        comp.setBackground(new Color(178, 225, 92));
                    } else if (isCellSelected(row, col)) {
                        //comp.setBackground(new Color(75, 149, 229));
                    } else if (selectedRows != null && selectedRows.contains(TableModelWrapperUtils.getActualRowAt(getModel(), row))) {
                        //comp.setBackground(SELECTED_COLOUR);
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

        chooseColumnButton = new JButton("Customize Fields");
        chooseColumnButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                columnChooser.showDialog();
            }
        });

        helpButton = ViewUtil.getHelpButton("About Variant List", "Variants are sorted first by DNA ID, then by position.  The list of variants within each page can be sorted by various fields by clicking the corresponding column name, but note that this will only sort the current page.");
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

        fieldPanel.add(helpButton);
        fieldPanel.add(chooseColumnButton);
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

        pageText = new JTextField();
        ViewUtil.makeSmall(pageText);
        pageText.setColumns(5);
        pageText.setMaximumSize(new Dimension(50, 20));
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

        pageLabel1 = new JLabel("Page ");
        ViewUtil.makeSmall(pageLabel1);
        pageLabel2 = new JLabel();
        ViewUtil.makeSmall(pageLabel2);

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

        rowsPerPageDropdown = new JComboBox(finalList);
        ViewUtil.makeSmall(rowsPerPageDropdown);
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

        add(tablePanel, BorderLayout.CENTER);

        initEmpty();
    }

    public SortableTable getTable() {
        return table;
    }

    public boolean isUpdating() {
        return worker != null && worker.getState() != StateValue.DONE;
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
            List<Object[]> results = retriever.retrieve((pageNum - 1) * getRowsPerPage(), getRowsPerPage());
            return results;
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
     * Brute-force replacement of current table data with the given data,
     * blowing away the current table selection.
     *
     * @param pageData the new data
     */
    @SuppressWarnings("UseOfObsoleteCollectionType")
    public synchronized void applyData(List<Object[]> pageData) {

        if (pageData != null) {
            // We can't call setDataVector directly because that blows away any custom table renderers we've set.
            java.util.Vector v = model.getDataVector();
            v.removeAllElements();
            for (Object[] r : pageData) {
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
        return row + ((this.getPageNumber() - 1) * rowsPerPageX);
    }

    public void setToggledRows(Set<Integer> rows) {
        this.toggledRows = rows;
        this.getTable().updateUI();
    }

    public boolean isRowToggled(int row) {
        if (toggledRows == null) {
            return false;
        }
        return toggledRows.contains(row);
    }

    public void setSelectedRows(List<Integer> rows) {
        this.selectedRows = rows;
    }

    public boolean isRowSelected(int row) {
        if (selectedRows == null) {
            return false;
        }
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

    /**
     * The given action will be executed when the selection changes AND a
     * scrolling key (pg-up, pg-down, arrow keys, num lock arrow keys) is not
     * being held down. This method should be used as a scroll-safe alternative
     * to registering a selection listener.
     *
     * This makes it safe for the user to scroll through the scroll panel
     * without repeating the action unnecessarily. The action executes in a new
     * thread.
     */
    public void scrollSafeSelectAction(final Runnable onSelectTask) {

        final KeyTimer keyTimer = new KeyTimer(KEY_PRESS_TIMER_INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                keydown = false;
                waitlong = false;
                //printTime("Starting Worker");
                resetSelectionChangedWorker(onSelectTask);

            }
        });
        keyTimer.setRepeats(false);

        getTable().addKeyListener(new KeyAdapter() {
            private long lastDelta = -1;
            private long lastTime = System.currentTimeMillis();
            private int deltaSame = 0;
            private boolean first = true;

            @Override
            public void keyPressed(KeyEvent ke) {
                super.keyPressed(ke);

                if (!ArrayUtils.contains(SCROLLING_KEYS, ke.getKeyCode())) {
                    //System.out.println("Detected key press that wasn't a scrolling key! (keycode=" + ke.getKeyCode() + ")");
                    return;
                }

                long currentTime = System.currentTimeMillis();
                long delta = currentTime - lastTime;

                if (Math.abs(delta - lastDelta) < KEY_PRESS_INTERVAL_EPSILON) {
                    deltaSame++;
                } else {
                    deltaSame = 0;
                }

                if (deltaSame > KEY_PRESS_TIMER_INTERVAL_AUTOADJUST_RUNS) {
                    //If this is the first time we've detected an interval, or if the detected interval
                    //is getting close to the current (padded) interval, then change the current interval to
                    //the detected one + padding.
                    if (first || (delta - KEY_PRESS_TIMER_INTERVAL) > (KEY_PRESS_TIMER_INTERVAL_AUTOADJUST_PADDING / 2.0f)) {
                        KEY_PRESS_TIMER_INTERVAL = (int) delta + KEY_PRESS_TIMER_INTERVAL_AUTOADJUST_PADDING;
                        LOG.info("Detected " + deltaSame + " keypresses with delta ~" + delta + ", setting new repeat-interval to " + KEY_PRESS_TIMER_INTERVAL);
                        first = false;
                    }
                    deltaSame = 0;
                }

                lastDelta = delta;
                lastTime = currentTime;

                keydown = true;
                stopSelectionChangedWorker();
                if (keyTimer.isRunning()) {
                    keyTimer.stop("keyPressed stop");
                    if (waitlong) {
                        waitlong = false;
                        keyTimer.setInitialDelay(KEY_PRESS_TIMER_INTERVAL_LONG);
                    } else {
                        keyTimer.setInitialDelay(KEY_PRESS_TIMER_INTERVAL);
                    }
                    keyTimer.restart("keyPressed restart");
                } else {
                    waitlong = false;
                    keyTimer.setInitialDelay(KEY_PRESS_TIMER_INTERVAL_LONG);
                    keyTimer.start("keyTimer start");
                }

            }
            //In Linux, holding a keydown fires pairs of keyPressed and keyReleased events
            //continually, so we cannot rely on 'keyReleased'.
                /*@Override
             public void keyReleased(KeyEvent ke) {
             super.keyReleased(ke); //To change body of generated methods, choose Tools | Templates.
             keydown = false;
             }*/
        });
        getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                if (keydown == false) {
                    if (!keyTimer.isRunning()) {
                        waitlong = true;
                        // printTime("valueChanged, setting waitlong=true: ");
                        keyTimer.setInitialDelay(KEY_PRESS_TIMER_INTERVAL);
                        keyTimer.start("valueChanged start");
                    }
                }
            }
        });
    }

    private synchronized void stopSelectionChangedWorker() {
        if (selectionChangedWorker != null && !selectionChangedWorker.isDone() && !selectionChangedWorker.isCancelled()) {
            try {
                //cancels all subinspector working threads,including selectionChangedWorker
                ThreadController.getInstance().cancelWorkers(SubInspector.PAGE_NAME);
            } catch (Exception ex) {
                //System.out.println(ex);
            }
        }
    }

    private synchronized void resetSelectionChangedWorker(Runnable task) {
        stopSelectionChangedWorker();
        selectionChangedWorker = new SelectionChangedWorker(task);
        selectionChangedWorker.execute();

    }

    private void printTime(String msg) {
        long curr = System.currentTimeMillis();
        long delta = curr - lastTime;
        System.out.println(msg + " curr: " + curr + " last: " + lastTime + " delta: " + delta);
        lastTime = curr;
    }

    //This doesn't need to be a separate class, but it helps with debugging.
    private class KeyTimer extends Timer {

        private long startTime;

        public KeyTimer(int interval, ActionListener al) {
            super(interval, al);
        }

        public synchronized void start(String msg) {
            super.start();
            //this.startTime = System.currentTimeMillis();
            //System.out.println(this.hashCode()+" "+msg+" started at "+startTime+" delaySetting="+this.getInitialDelay());
        }

        public synchronized void stop(String msg) {
            //long stopped = System.currentTimeMillis();
            super.stop();
            //System.out.println(this.hashCode()+" "+msg+" stopped at "+stopped+" timeRunning was="+(stopped-startTime)+" delaySetting="+this.getInitialDelay());
        }

        public synchronized void restart(String msg) {
            //long restarted = System.currentTimeMillis();
            super.restart();
            //System.out.println(this.hashCode()+" "+msg+" restarted at "+restarted+" timeRunning was="+(restarted-startTime)+" delaySetting="+this.getDelay());
        }
    }

    class SelectionChangedWorker extends MedSavantWorker<Object> {

        Runnable task;

        public SelectionChangedWorker(Runnable task) {
            super(SubInspector.PAGE_NAME);
            this.task = task;
        }

        @Override
        protected void showSuccess(Object result) {
        }

        @Override
        protected Object doInBackground() throws Exception {
            task.run();
            return null;
        }
    }

    public void setResizeOff() {
        table.setAutoResizeMode(SortableTable.AUTO_RESIZE_OFF);
    }

    public void setHelpButtonVisible(boolean enable) {
        helpButton.setVisible(enable);
    }
	
	public ColumnChooser getColumnChooser() {
		return columnChooser;
	}

}
