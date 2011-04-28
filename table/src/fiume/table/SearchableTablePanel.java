/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fiume.table;

import com.jidesoft.grid.AutoFilterTableHeader;
import com.jidesoft.grid.QuickTableFilterField;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.lucene.LuceneFilterableTableModel;
import com.jidesoft.lucene.LuceneQuickTableFilterField;
import fiume.table.images.IconFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author mfiume
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

    private int pageNum = 1;
    private int numRowsPerPage = ROWSPERPAGE_2;
    private final JComboBox rowsPerPageDropdown;
    
    private List<List> data;
    private List<String> columnNames;
    private List<Class> columnClasses;
    private final JLabel pageLabel;
    private final JButton gotoFirst;
    private final JButton gotoPrevious;
    private final JButton gotoNext;
    private final JButton gotoLast;

    public SortableTable getTable() {
        return table;
    }

    public void updateData(List data) {
        this.data = data;
        updateView();
        model.fireTableDataChanged();
        table.repaint();
    }

    private void updateView() {

        if (data == null) { return; }

        List<List> pageData = getDataOnPage(this.getPageNumber(),data);

        if (model == null) {
            model = new GenericTableModel(pageData, columnNames, columnClasses);
        } else {
            model.getDataVector().removeAllElements();
            model.getDataVector().addAll(pageData);
        }

        this.gotoFirst.setEnabled(true);
            this.gotoPrevious.setEnabled(true);
            this.gotoNext.setEnabled(true);
            this.gotoLast.setEnabled(true);

        if (pageNum == 1) {
            this.gotoFirst.setEnabled(false);
            this.gotoPrevious.setEnabled(false);
        }
        if (pageNum == getTotalNumPages()) {
            this.gotoNext.setEnabled(false);
            this.gotoLast.setEnabled(false);
        }
            

        pageLabel.setText("Page " + this.getPageNumber() + " of " + this.getTotalNumPages());
        int start = (this.getPageNumber()-1)*this.getRowsPerPage()+1;
        int end = Math.min(start + this.getRowsPerPage()-1, this.getTotalRowCount());
        amountLabel.setText("Showing " + start + " - " + end + " of " + data.size() + " records");

        int[] columns = new int[columnNames.size()];
        for (int i = 0; i < columns.length; i++) { columns[i] = i; }

        filterField.setTableModel(model);
        filterField.setColumnIndices(columns);
        filterField.setObjectConverterManagerEnabled(true);

        table.setModel(new LuceneFilterableTableModel(filterField.getDisplayTableModel()));
    }

    private void setTableModel(List<List> data, List<String> columnNames, List<Class> columnClasses) {
        this.data = data;
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;
        updateView();
    }
    
    public SearchableTablePanel(
            List<Vector> data, List<String> columnNames, List<Class> columnClasses) {

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
        table.setDragEnabled(false);

        table.setAutoResizeMode(SortableTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        AutoFilterTableHeader header = new AutoFilterTableHeader(table);
        header.setAutoFilterEnabled(true);
        header.setShowFilterIcon(true);
        header.setShowFilterName(true);
        table.setTableHeader(header);

        filterField = new LuceneQuickTableFilterField(model);
        //filterField.setColumns(50);
        filterField.setHintText("Type to search");

        this.setLayout(new BorderLayout(3, 3));
        fieldPanel = new JPanel();
        fieldPanel.add(filterField);

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

        rowsPerPageDropdown = new JComboBox(new Integer[]{ROWSPERPAGE_1,ROWSPERPAGE_2,ROWSPERPAGE_3});
        rowsPerPageDropdown.setPrototypeDisplayValue(ROWSPERPAGE_3);
        rowsPerPageDropdown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                int rowsPerPage = (Integer) cb.getSelectedItem();
                setNumRowsPerPage(rowsPerPage);
            }

        });
        rowsPerPageDropdown.setSelectedIndex(1);
        rowsPerPageDropdown.setPreferredSize(new Dimension(100,25));
        rowsPerPageDropdown.setMaximumSize(new Dimension(100,25));
        bottomPanel.add(rowsPerPageDropdown);

        setTableModel(
                Util.listToVector(data),
                Util.listToVector(columnNames),
                Util.listToVector(columnClasses));

        JPanel tablePanel = new JPanel(new BorderLayout(3, 3));
        tablePanel.add(new JScrollPane(table));

        this.add(fieldPanel, BorderLayout.BEFORE_FIRST_LINE);
        this.add(bottomPanel,BorderLayout.AFTER_LAST_LINE);

        this.add(tablePanel);
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
        if (i > getTotalNumPages()) { i = getTotalNumPages(); }
        if (i < 1) { i = 1; }
        this.pageNum = i;
        this.updateView();
    }

    private int getPageNumber() {
        return this.pageNum;
    }

    private void goToFirstPage() {
        setPageNumber(1);
    }

    private void goToLastPage() {
        setPageNumber(getTotalNumPages());
    }

    private void goToNextPage() {
        setPageNumber(pageNum+1);
    }

    private void goToPreviousPage() {
        setPageNumber(pageNum-1);
    }

    private int getTotalRowCount() {
        if (data == null) { return 0; }
        return data.size();
    }

    private int getTotalNumPages() {
        return (int) Math.ceil(((double)getTotalRowCount())/getRowsPerPage());
    }

    private int getRowsPerPage() {
        return this.numRowsPerPage;
    }

    private List<List> getDataOnPage(int pageNumber, List<List> data) {
        int start = (pageNumber-1)*this.getRowsPerPage();
        int end = start+this.getRowsPerPage();

        List<List> result = new ArrayList<List>();
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
}
