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
package org.ut.biolab.medsavant.client.filter;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.component.SelectableListView;

import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * Base class shared by StringListFilterView, RegionSetFilterView, and
 * OntologyFilterView, all of which consist of a table containing checkable
 * items.
 *
 * @author tarkvara
 */
public abstract class TabularFilterView<T> extends FilterView {

    private static final Log LOG = LogFactory.getLog(TabularFilterView.class);
    private static final int FIELD_WIDTH = 260;
    final Object contentInitializedSemaphore = new Object();
    private List<T> availableValues;
    protected List<T> appliedValues;
    private QuickListFilterField field;
    protected FilterableCheckBoxList filterableList;
    protected JButton applyButton;
    private JButton selectAll;
    private boolean contentInitialized;

    protected TabularFilterView(String name, int queryID) {
        super(name, queryID);
    }

    public List<T> getAvailableValues() {
        return availableValues;
    }

    public boolean areAvailableValuesSet() {
        return getAvailableValues() == null;
    }

    public void setAvailableValues(List<T> vals) {
        this.availableValues = vals;
    }

    protected final void initContentPanel() {

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        if (availableValues == null) {
            JTextArea label = new JTextArea("There are too many unique values to generate this list. You will not be able to filter on this column. ");
            label.setOpaque(false);
            label.setLineWrap(true);
            label.setWrapStyleWord(true);

            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(3, 3, 3, 3);
            add(label, gbc);
            this.showViewCard();
            return;
        }

        applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        AbstractListModel model = new SimpleListModel();

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

        filterableList.getCheckBoxListSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    applyButton.setEnabled(true);
                }
            }
        });

        setAllSelected(true);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });

        JScrollPane jsp = new JScrollPane(filterableList) {
            @Override
            public Dimension getPreferredSize() {
                Dimension result = super.getPreferredSize();
                result = new Dimension(Math.min(result.width, TabularFilterView.this.getWidth() - 20), result.height);
                return result;
            }
        };

        selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllSelected(true);
                applyButton.setEnabled(true);
            }
        });

        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

        selectNone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllSelected(false);
                applyButton.setEnabled(true);
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
        gbc.fill = GridBagConstraints.NONE;
        add(selectAll, gbc);
        add(selectNone, gbc);

        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        add(applyButton, gbc);

        this.showViewCard();

    }

    public final void setFilterValues(Collection<String> list) {

        this.waitForFilterValuesToBeReady();

        int[] selectedIndices = new int[list.size()];
        int i = 0;
        for (String s : list) {
            int j = 0;

            for (T t : availableValues) {
                System.out.println("\t" + t + " looking for " + s);
                if (t.toString().equals(s)) {
                    break;
                }
                j++;
            }
            selectedIndices[i++] = j;   // If element is not in availableValues, j will be > availableValues.size()
        }

        ClientMiscUtils.selectOnlyTheseIndicies(filterableList, selectedIndices);

        applyFilter();
    }

    protected abstract void applyFilter();

    /**
     * Shared code which derived classes can call to set things up properly in
     * their
     * <code>applyFilter</code> calls.
     */
    protected void preapplyFilter() {
        applyButton.setEnabled(false);

        appliedValues = new ArrayList<T>();

        int[] indices = filterableList.getCheckBoxListSelectedIndices();
        for (int i : indices) {
            appliedValues.add(availableValues.get(i));
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
        field.setListModel(new SimpleListModel());
        filterableList.setModel(field.getDisplayListModel());
        LOG.info("Model updated, " + field.getDisplayListModel().getSize() + " of " + field.getListModel().getSize() + "(" + availableValues.size() + ") rows visible.");
    }

    protected class SimpleListModel extends AbstractListModel {

        @Override
        public int getSize() {
            return availableValues.size();
        }

        @Override
        public Object getElementAt(int i) {
            T val = availableValues.get(i);
            if (val instanceof String && ((String) val).length() == 0) {
                return "(null)";
            }
            return val;
        }
    }
}
