package org.ut.biolab.mfiume.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class SearchAutoFillClone {

private JFrame frame = null;
private JTextField searchField = null;
private JPopupMenu popup = null;

private JTable searchTable = null;
private TableRowSorter<DefaultTableModel> rowSorter = null;
private DefaultTableModel searchTableModel = null;

public SearchAutoFillClone() {
    searchTableModel = new DefaultTableModel();
    initTableModel();

    rowSorter = new TableRowSorter<DefaultTableModel>(searchTableModel);
    searchTable = new JTable(searchTableModel);
    searchTable.setRowSorter(rowSorter);
    searchTable.setFillsViewportHeight(true);
    searchTable.getColumnModel().setColumnSelectionAllowed(false);
    searchTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    searchTable.getTableHeader().setReorderingAllowed(false);
    searchTable.setPreferredSize(new Dimension(775, 100));
    searchTable.setGridColor(Color.WHITE);

    searchField = new JTextField();
    searchField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void changedUpdate(DocumentEvent e) {
            showPopup(e);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            showPopup(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            showPopup(e);
        }
    });

    searchField.addKeyListener(new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();
            switch(code)
            {
                case KeyEvent.VK_UP:
                {
                    cycleTableSelectionUp();
                    break;
                }

                case KeyEvent.VK_DOWN:
                {
                    cycleTableSelectionDown();
                    break;
                }

                case KeyEvent.VK_LEFT:
                {
                    //Do whatever you want here
                    break;
                }

                case KeyEvent.VK_RIGHT:
                {
                    //Do whatever you want here
                    break;
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }
    });

    KeyStroke keyStroke = KeyStroke.getKeyStroke("ESCAPE");
    searchField.getInputMap().put(keyStroke, "ESCAPE");
    searchField.getActionMap().put("ESCAPE", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //Do what you wish here with the escape key.
        }
    });

    popup = new JPopupMenu();
    popup.add(searchTable);
    popup.setVisible(false);
    popup.setBorder(BorderFactory.createEmptyBorder());

    JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
    searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    searchPanel.add(searchField, BorderLayout.CENTER);

    frame = new JFrame();
    frame.setLayout(new BorderLayout(5, 5));
    frame.add(searchPanel, BorderLayout.NORTH);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 500);
    center(frame);
    frame.setVisible(true);
}

private final void newFilter() {
    RowFilter<DefaultTableModel, Object> rf = null;

    try {
        rf = RowFilter.regexFilter(getFilterText(), 0);
    }
    catch(PatternSyntaxException e) {
        return;
    }

    rowSorter.setRowFilter(rf);
}

private final String getFilterText() {
    String orig = searchField.getText();
    return "("+orig.toLowerCase()+")|("+orig.toUpperCase()+")";
}

private void showPopup(DocumentEvent e) {
    if(e.getDocument().getLength() > 0) {
        if(!popup.isVisible()) {
            Rectangle r = searchField.getBounds();
            popup.show(searchField, (r.x-4), (r.y+16));
            popup.setVisible(true);
        }

        newFilter();
        searchField.grabFocus();

    }
    else {
        popup.setVisible(false);
    }
}

private void cycleTableSelectionUp() {
    ListSelectionModel selModel = searchTable.getSelectionModel();
    int index0 = selModel.getMinSelectionIndex();
    if(index0 > 0) {
        selModel.setSelectionInterval(index0-1, index0-1);
    }
}

private void cycleTableSelectionDown() {
    ListSelectionModel selModel = searchTable.getSelectionModel();
    int index0 = selModel.getMinSelectionIndex();
    if(index0 == -1) {
        selModel.setSelectionInterval(0, 0);
    }
    else if(index0 > -1) {
        selModel.setSelectionInterval(index0+1, index0+1);
    }
}

private void initTableModel() {
    String[] columns = new String[] {"A"};
    String[][] data = new String[][]
    {
        new String[] {"a"},
        new String[] {"aa"},
        new String[] {"aaab"},
        new String[] {"aaabb"},
        new String[] {"aaabbbz"},
        new String[] {"b"},
        new String[] {"bb"},
        new String[] {"bbb"},
        new String[] {"bbbbbbb"},
        new String[] {"bbbbbbbeee"},
        new String[] {"bbbbbbbeeexxx"},
        new String[] {"ccc"},
        new String[] {"cccc"},
        new String[] {"ccccc"},
        new String[] {"cccccaaaa"},
        new String[] {"ccccccaaaa"},
    };

    searchTableModel.setDataVector(data, columns);
}

private void center(Window w) {
    int screenWidth  = Toolkit.getDefaultToolkit().getScreenSize().width;
    int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

    int windowWidth = w.getWidth();
    int windowHeight = w.getHeight();

    if (windowHeight > screenHeight) {
        return;
    }

    if (windowWidth > screenWidth) {
        return;
    }

    int x = (screenWidth - windowWidth) / 2;
    int y = (screenHeight - windowHeight) / 2;

    w.setLocation(x, y);
}

public static void main(String ... args) {
    new SearchAutoFillClone();
}
}