/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.component;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;

import com.jidesoft.list.FilterableCheckBoxList;
import com.jidesoft.list.QuickListFilterField;
import com.jidesoft.swing.SearchableUtils;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;

import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * Base class shared by StringListFilterView, RegionSetFilterView, and
 * OntologyFilterView, all of which consist of a table containing checkable
 * items.
 *
 * @author tarkvara
 */
public class SelectableListView<T> extends JPanel {

    private static final Log LOG = LogFactory.getLog(SelectableListView.class);
    private static final int FIELD_WIDTH = 260;
    private List<Listener<SelectionEvent>> listeners;
    private List<T> availableValues;
    protected List<T> appliedValues;
    private QuickListFilterField field;
    protected FilterableCheckBoxList filterableList;
    private JButton selectAll;

    protected SelectableListView() {
        listeners = new ArrayList<Listener<SelectionEvent>>();
    }

    public void setAvailableValues(List<T> v) {
        this.availableValues = v;
        setAppliedValues(v);
    }

    public void setAppliedValues(List<T> v) {
        this.appliedValues = v;
        fireSelectionsChangedEvent();
    }

    protected final void initContentPanel() {

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        if (availableValues == null) {
            JTextArea label = new JTextArea("There are too many values to display.");
            label.setOpaque(false);
            label.setLineWrap(true);
            label.setWrapStyleWord(true);

            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(3, 3, 3, 3);
            add(label, gbc);
            return;
        }

        AbstractListModel model = new SelectableListView.SimpleListModel();

        field = new QuickListFilterField(model);
        field.setHintText("Type here to filter options");

        // the width of the field has to be less than the width
        // provided to the filter, otherwise, it will push the grid wider
        // and components will be inaccessible
        field.setPreferredSize(new Dimension(FIELD_WIDTH, 22));

        filterableList = new FilterableCheckBoxList(field.getDisplayListModel()) {
            @Override
            public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
                return -1;
            }

            @Override
            public boolean isCheckBoxEnabled(int index) {
                return true;
            }
        };
        filterableList.getCheckBoxListSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (model.getSize() > 0) {
            filterableList.setPrototypeCellValue(model.getElementAt(0));    // Makes it much faster to determine the view's preferred size.
        }

        SearchableUtils.installSearchable(filterableList);

        setAllSelected(true);

        JScrollPane jsp = new JScrollPane(filterableList) {
            @Override
            public Dimension getPreferredSize() {
                Dimension result = super.getPreferredSize();
                result = new Dimension(Math.min(result.width, SelectableListView.this.getWidth() - 20), result.height);
                return result;
            }
        };

        selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllSelected(true);
            }
        });

        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

        selectNone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllSelected(false);
            }
        });

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 15, 3, 15);
        add(field, gbc);

        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        add(jsp, gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel bottom = new JPanel();
        ViewUtil.applyHorizontalBoxLayout(bottom);

        bottom.add(selectAll);
        bottom.add(selectNone);
        bottom.add(Box.createHorizontalGlue());

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                saveSelections();
            }

        });
        bottom.add(applyButton);
        add(bottom,gbc);
    }

    private void saveSelections() {
        int[] selectedIndices = filterableList.getCheckBoxListSelectedIndices();
        List<T> selections = new ArrayList<T>();
        for (int i : selectedIndices) {
            selections.add(availableValues.get(i));
        }
        appliedValues = selections;
        fireSelectionsChangedEvent();
    }

    public List<T> getSelections() {
        return appliedValues;
    }

    public boolean areAllSelected() {
        return appliedValues.size() == availableValues.size();
    }

    public boolean areNoneSelected() {
        return appliedValues.isEmpty();
    }

    public final void setFilterValues(Collection<String> list) {

        int[] selectedIndices = new int[list.size()];
        int i = 0;
        for (String s : list) {
            int j = 0;
            for (T t : availableValues) {
                if (t.toString().equals(s)) {
                    break;
                }
                j++;
            }
            selectedIndices[i++] = j;   // If element is not in availableValues, j will be > availableValues.size()
        }

        ClientMiscUtils.selectOnlyTheseIndicies(filterableList,selectedIndices);

    }

    /**
     * Shared code which derived classes can call to set things up properly in
     * their
     * <code>applyFilter</code> calls.
     */
    protected void preapplyFilter() {
        appliedValues = new ArrayList<T>();

        int[] indices = filterableList.getCheckBoxListSelectedIndices();
        for (int i : indices) {
            appliedValues.add((T) filterableList.getModel().getElementAt(i));
        }
    }

    private void setAllSelected(boolean b) {

        if (b) {
            for (int i = 0; i < filterableList.getModel().getSize(); i++) {
                filterableList.addCheckBoxListSelectedIndex(i);
            }
        } else {
            for (int i = 0; i < filterableList.getModel().getSize(); i++) {
                filterableList.removeCheckBoxListSelectedIndex(i);
            }
        }
    }

    /**
     * Update our list model when the available values list has changed. It
     * should be possible to get this working without resetting the whole model,
     * but I couldn't get it to update correctly, hence the brute force
     * approach.
     */
    protected void updateModel() {
        field.setListModel(new SelectableListView.SimpleListModel());
        filterableList.setModel(field.getDisplayListModel());
        LOG.info("Model updated, " + field.getDisplayListModel().getSize() + " of " + field.getListModel().getSize() + "(" + availableValues.size() + ") rows visible.");
    }

    public void addListener(Listener<SelectionEvent> l) {
        listeners.add(l);
    }

    private void fireSelectionsChangedEvent() {
        SelectionEvent e = new SelectionEvent(SelectionEvent.Type.CHANGED, this.getSelections());
        for (Listener l : listeners) {
            l.handleEvent(e);
        }
    }

    public static class SelectionEvent  {

        public enum Type {
            CHANGED
        };
        private final Type type;
        private final List selections;

        public SelectionEvent(Type type, List selections) {
            this.type = type;
            this.selections = selections;
        }

        public Type getType() {
            return type;
        }

        public List getSelections() {
            return selections;
        }
    }

    public class SimpleListModel extends AbstractListModel {

        @Override
        public int getSize() {
            return availableValues.size();
        }

        @Override
        public Object getElementAt(int i) {
            return availableValues.get(i);
        }
    }
}
