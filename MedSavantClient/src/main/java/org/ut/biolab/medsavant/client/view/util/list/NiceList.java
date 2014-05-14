/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.util.list;

import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class NiceList extends JList {

    private NiceListColorScheme colorScheme = new GrayscaleNiceListColorScheme();

    private final Vector<NiceListItem> allItems;
    private JTextField searchBar;
    private boolean inTransaction;

    private class NiceListModel extends AbstractListModel {

        @Override
        public int getSize() {
            return allItems.size();
        }

        @Override
        public Object getElementAt(int index) {
            return allItems.get(index);
        }

    }

    public NiceList() {
        allItems = new Vector<NiceListItem>();
        this.setCellRenderer(new NiceListCellRenderer());
        this.setModel(new NiceListModel());
        initSearchBar();
    }

    public void addItem(final NiceListItem item) {
        allItems.add(item);
        updateListItems();
    }

    public void removeItem(final NiceListItem item) {
        allItems.remove(item);
        updateListItems();
    }

    public NiceListItem getItem(int index) {
        return allItems.get(index);
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

        if (inTransaction) {
            return;
        }

        this.setBackground(colorScheme.getBackgroundColor());

        final Vector<NiceListItem> v;
        if (searchBar.getText().isEmpty()) {
            v = new Vector<NiceListItem>(allItems);
        } else {
            v = getSearchResults();
        }

        setListData(v);
    }

    public void removeItems() {
        this.startTransaction();
        for (NiceListItem i : allItems) {
            this.removeItem(i);
        }
        this.endTransaction();
    }

    private Vector getSearchResults() {
        Vector v = new Vector();

        String searchTerm = searchBar.getText().toLowerCase();

        for (NiceListItem item : allItems) {
            if (item.toString().toLowerCase().contains(searchTerm)) {
                v.add(item);
            } else if (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchTerm)) {
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

        Object[] selected = this.getSelectedValues();

        for (Object item : selected) {
            if (item instanceof NiceListItem) {
                results.add((NiceListItem) item);
            }
        }
        return results;
    }

    public void selectItemWithKey(String key) {
        clearSearch();
        for (NiceListItem item : this.allItems) {
            if (item.toString().toLowerCase().equals(key.toLowerCase())) {
                this.setSelectedValue(item, true);
                return;
            }
        }
    }

    private void clearSearch() {
        if (!this.searchBar.getText().isEmpty()) {
            this.searchBar.setText("");
        }
    }

    public void selectItemsAtIndicies(List<Integer> indicies) {
        int[] indiciesArray = new int[indicies.size()];
        for (int i = 0; i < indicies.size(); i++) {
            indiciesArray[i] = indicies.get(i);
        }
        selectItemsAtIndicies(indiciesArray);
    }

    private void selectItemsAtIndicies(int[] indicies) {
        clearSearch();
        this.setSelectedIndices(indicies);
    }

    public void selectItemAtIndex(int i) {
        clearSearch();
        try {
            NiceListItem item = this.allItems.get(i);
            this.setSelectedValue(item, true);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    public NiceListColorScheme getColorScheme() {
        return this.colorScheme;
    }

    public void setColorScheme(NiceListColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    class NiceListCellRenderer implements ListCellRenderer {

        private static final int cellHeight = 45;

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

            p.setLayout(new MigLayout(String.format("fillx, height %d, hidemode 3, gapy 2", cellHeight)));

            JLabel l = new JLabel(mitem.toString());
            l.setFont(new Font(l.getFont().getFamily(), Font.PLAIN, 16));
            l.setForeground(isSelected ? colorScheme.getSelectedFontColor() : colorScheme.getUnselectedFontColor());

            p.add(l, "wrap");

            String description = mitem.getDescription();
            if (description != null) {
                JLabel dl = ViewUtil.getSubtleHeaderLabel(description);
                p.add(dl, "wrap");
            }

            p.setBorder(BorderFactory.createMatteBorder((index == 0) ? 1 : 0, 0, 1, 0, colorScheme.getBorderColor()));

            return p;
        }

    }

}
