/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.util.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author mfiume
 */
public class NiceList extends JList {

    private NiceListColorScheme colorScheme = new DefaultNiceListColorScheme();

    private final Vector<NiceListItem> allItems;
    private JTextField searchBar;
    private boolean inTransaction;

    public NiceList() {
        allItems = new Vector<NiceListItem>();

        this.setCellRenderer(new NiceListCellRenderer());

        initSearchBar();
    }

    public void addItem(NiceListItem item) {
        allItems.add(item);
        updateListItems();
    }

    public void removeItem(NiceListItem item) {
        allItems.remove(item);
        updateListItems();
    }

    public JTextField getSearchBar() {
        return searchBar;
    }

    private void initSearchBar() {
        searchBar = new JTextField();
        searchBar.putClientProperty("JTextField.variant", "search");
        searchBar.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent e) {
                updateListItems();
            }

        });
    }

    private void updateListItems() {
        
        if (inTransaction) { return; }
        
        this.setBackground(colorScheme.getBackgroundColor());
        
        Vector v;
        if (searchBar.getText().isEmpty()) {
            v = new Vector(allItems);
        } else {
            v = getSearchResults();
        }

        this.setListData(v);
    }

    private Vector getSearchResults() {
        Vector v = new Vector();

        String searchTerm = searchBar.getText().toLowerCase();

        for (NiceListItem item : allItems) {
            if (item.toString().toLowerCase().contains(searchTerm)) {
                v.add(item);
            }
        }

        return v;
    }

    public void startTransaction() {
        inTransaction = true;
    }

    public void endTransaction() {
        inTransaction = false;
        updateListItems();
    }

    public List<NiceListItem> getSelectedItems() {
        
        List<NiceListItem> results = new ArrayList<NiceListItem>();
        for (Object item : this.getSelectedValues()) {
            if (item instanceof NiceListItem) {
                results.add((NiceListItem) item);
            }
        }
        return results;
    }

    public void selectItemWithKey(String key) {
        clearSearch();
        System.out.print("Selecting item with key " + key);
        for (NiceListItem item : this.allItems) {
            if (item.toString().toLowerCase().equals(key.toLowerCase())) {
                System.out.println("\t ... yes");
                this.setSelectedValue(item, true);
                return;
            }
        }
        System.out.println("\t ... no");
    }

    private void clearSearch() {
        if (!this.searchBar.getText().isEmpty()) {
            this.searchBar.setText("");
        }
    }

    public void selectItemAtIndex(int i) {
        clearSearch();
        NiceListItem item = this.allItems.get(i);
        this.setSelectedValue(item, true);
    }

    public NiceListColorScheme getColorScheme() {
        return this.colorScheme;
    }
    
    class NiceListCellRenderer implements ListCellRenderer {

        private static final int cellHeight = 53;
        
        public NiceListCellRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            

            if (value instanceof NiceListItem) {

                NiceListItem mitem = (NiceListItem) value;
                return getListCellRendererRegularComponent(list, mitem, index, isSelected, cellHasFocus);
                
            } else {
                throw new UnsupportedOperationException("NiceList can't render items of type " + value.getClass().getCanonicalName());
            }
        }

        private Component getListCellRendererRegularComponent(JList list, NiceListItem mitem, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel p = new JPanel();
            
            p.setBackground(isSelected ? colorScheme.getSelectedColor() : colorScheme.getUnselectedColor());

            p.setLayout(new MigLayout(String.format("fillx, height %d",cellHeight)));

            JLabel l = new JLabel(mitem.toString());
            l.setFont(new Font(l.getFont().getFamily(), Font.PLAIN, 18));//l.getFont().deriveFont(40));
            l.setForeground(isSelected ? colorScheme.getSelectedFontColor() : colorScheme.getUnselectedFontColor() );
            
            p.add(l);

            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, colorScheme.getBorderColor()));

            return p;
        }

    }

}
