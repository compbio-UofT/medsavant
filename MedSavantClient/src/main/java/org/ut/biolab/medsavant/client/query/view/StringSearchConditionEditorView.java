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
package org.ut.biolab.medsavant.client.query.view;

import com.jidesoft.list.FilterableCheckBoxList;
import com.jidesoft.list.QuickListFilterField;
import com.jidesoft.swing.SearchableUtils;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;

import org.jdesktop.swingx.prompt.PromptSupport;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.query.SearchConditionItem;
import org.ut.biolab.medsavant.client.query.value.encode.StringConditionEncoder;
import org.ut.biolab.medsavant.client.query.value.StringConditionValueGenerator;

/**
 *
 * @author mfiume
 */
public class StringSearchConditionEditorView extends SearchConditionEditorView {

    private final StringConditionValueGenerator valueGenerator;
    protected QuickListFilterField field;
    private final int FIELD_WIDTH = 600;
    protected FilterableCheckBoxList filterableList;
    private List<String> values;
    private boolean cacheOn = true;
    private JButton selectNone;
    private JButton selectAll;
    private JScrollPane jsp;
    private boolean makingBatchChanges = false;
    private boolean isUserSpecifiedTextMatch = false;

    public StringSearchConditionEditorView(SearchConditionItem i, final StringConditionValueGenerator vg) {
        super(i);
        if (vg == null) {
            isUserSpecifiedTextMatch = true;
        }
        this.valueGenerator = vg;
    }

    private void loadLooseStringMatchViewFromSearchConditionParameters(String encoding) {
        this.removeAll();

        ViewUtil.applyVerticalBoxLayout(this);

        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(p);
        p.add(new JLabel("Filtering variants where " + item.getName() + ": "));
        ButtonGroup group = new ButtonGroup();
        JRadioButton isButton = new JRadioButton("is any of the following:");
        JRadioButton notNullButton = new JRadioButton("is not null");
        JRadioButton nullButton = new JRadioButton("is null");

        p.add(isButton);
        p.add(notNullButton);
        p.add(nullButton);

        group.add(isButton);
        group.add(notNullButton);
        group.add(nullButton);

        final JTextField f = new JTextField();
        PromptSupport.setPrompt("Enter " + item.getName(), f);
        PromptSupport.setFocusBehavior(PromptSupport.FocusBehavior.SHOW_PROMPT, f);
        f.setPreferredSize(new Dimension(200, f.getPreferredSize().height));



        f.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent ce) {
                saveSearchConditionParameters(f.getText());
                item.setDescription(f.getText());
            }
        });

        notNullButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                f.setEnabled(false);
                saveSearchConditionParameters(StringConditionEncoder.encodeConditions(
                        Arrays.asList(new String[]{StringConditionEncoder.ENCODING_NOTNULL})));
                item.setDescription("not null");
            }
        });

        nullButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                f.setEnabled(false);
                saveSearchConditionParameters(StringConditionEncoder.encodeConditions(
                        Arrays.asList(new String[]{StringConditionEncoder.ENCODING_NULL})));
                item.setDescription("null");
            }
        });

        isButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                f.setEnabled(true);
                saveSearchConditionParameters(f.getText());
                item.setDescription(f.getText());
            }
        });

        if (encoding != null) {
            if (StringConditionEncoder.encodesNull(encoding)) {
                nullButton.setSelected(true);
            } else if (StringConditionEncoder.encodesNotNull(encoding)) {
                notNullButton.setSelected(true);
            } else {
                isButton.setSelected(true);
                //saveSearchConditionParameters(f.getText());
                f.setText(encoding);
            }
        } else {
            isButton.setSelected(true);
        }

        this.add(p);
        this.add(f);
    }

    protected class SimpleListModel extends AbstractListModel {

        private final List<String> items;

        public SimpleListModel(List<String> items) {
            this.items = items;
        }

        @Override
        public int getSize() {
            return items.size();
        }

        @Override
        public Object getElementAt(int i) {
            String val = items.get(i);
            if (val instanceof String && ((String) val).length() == 0) {
                return "<empty>";
            }
            return val;
        }
    }

    protected class ListCellRendererWithTotals implements ListCellRenderer {

        private ListCellRenderer defaultListCellRenderer;
        private JLabel totalCount;

        public ListCellRendererWithTotals(ListCellRenderer defaultListCellRenderer) {
            super();
            this.defaultListCellRenderer = defaultListCellRenderer;
        }

        @Override
        public Component getListCellRendererComponent(JList jlist, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = defaultListCellRenderer.getListCellRendererComponent(jlist, value, index, isSelected, cellHasFocus);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(c);
            p.add(Box.createHorizontalGlue());
            totalCount = getNumberInCategory((String) value);
            p.add(totalCount);            
            return p;
        }

        public boolean isMouseXOverLabel(Point p) {
            Point l = totalCount.getLocation();
            if (p.x >= l.x && p.x <= (l.x + totalCount.getWidth())) {
                return true;
            }
            return false;
        }
    }

    private void hideListFields() {
        field.setVisible(false);
        selectAll.setVisible(false);
        selectNone.setVisible(false);
        jsp.setVisible(false);
        this.invalidate();
    }

    private void showListFields() {
        field.setVisible(true);
        selectAll.setVisible(true);
        selectNone.setVisible(true);
        jsp.setVisible(true);
        this.invalidate();
    }

    @Override
    public void loadViewFromSearchConditionParameters(String encoding) throws ConditionRestorationException {

        if (isUserSpecifiedTextMatch) {
            loadLooseStringMatchViewFromSearchConditionParameters(encoding);
            return;
        }

        // if (!cacheOn || values == null) {
        values = valueGenerator.getStringValues();
        // }


        this.removeAll();

        if (values == null || values.isEmpty()) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(Box.createHorizontalGlue());
            p.add(new JLabel("<html>All values are blank for this condition.</html>"));
            p.add(Box.createHorizontalGlue());
            this.add(p);
            return;
        }

        final JRadioButton isNull = new JRadioButton("is null");
        final JRadioButton isNotNull = new JRadioButton("is not null");
        final JRadioButton is = new JRadioButton("is any of the following:");
        ButtonGroup group = new ButtonGroup();

        is.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showListFields();
                setDescriptionBasedOnSelections();
            }
        });

        isNull.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (isNull.isSelected()) {
                    setAllSelected(false, false);
                    saveSearchConditionParameters(StringConditionEncoder.encodeConditions(
                            Arrays.asList(new String[]{StringConditionEncoder.ENCODING_NULL})));
                    item.setDescription("is null");
                    //hideListFields();
                }
            }
        });

        isNotNull.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (isNotNull.isSelected()) {
                    setAllSelected(false, false);
                    saveSearchConditionParameters(StringConditionEncoder.encodeConditions(
                            Arrays.asList(new String[]{StringConditionEncoder.ENCODING_NOTNULL})));
                    item.setDescription("is not null");
                    //hideListFields();
                }
            }
        });

        List<String> selectedValues;
        if (encoding == null) {
            selectedValues = null;
            is.setSelected(true);
        } else {
            selectedValues = StringConditionEncoder.unencodeConditions(encoding);
        }

        group.add(isNull);
        group.add(isNotNull);
        group.add(is);

        JPanel controlButtons = ViewUtil.getClearPanel();
        controlButtons.setLayout(new BoxLayout(controlButtons, BoxLayout.X_AXIS));
        //ViewUtil.applyHorizontalBoxLayout(controlButtons);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(new JLabel("Filtering variants where " + item.getName() + ": "));
        labelPanel.add(Box.createHorizontalGlue());
        p.add(labelPanel);

        controlButtons.add(Box.createHorizontalGlue());
        controlButtons.add(isNull);
        controlButtons.add(isNotNull);
        controlButtons.add(is);
        controlButtons.add(Box.createHorizontalGlue());

        p.add(controlButtons);

        AbstractListModel model = new SimpleListModel(values);

        field = new QuickListFilterField(model);
        field.setHintText("Type here to filter options");

        // the width of the field has to be less than the width
        // provided to the filter, otherwise, it will push the grid wider
        // and components will be inaccessible
        field.setMaximumSize(new Dimension(FIELD_WIDTH, 22));

        filterableList = new FilterableCheckBoxList(field.getDisplayListModel()) {
            @Override
            public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
                return -1;
            }

            @Override
            public boolean isCheckBoxEnabled(int index) {
                return true;
            }

            @Override
            public ListCellRenderer getCellRenderer() {
                final ListCellRenderer defaultListCellRenderer = super.getCellRenderer();
                return (new ListCellRendererWithTotals(defaultListCellRenderer));
            }
        };


        //Generate popup tooltips
        filterableList.addMouseMotionListener(new MouseMotionAdapter() {
            private JPopupMenu menu;
            private int lastIndex = -1;

            @Override
            public void mouseMoved(final MouseEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int index = filterableList.locationToIndex(e.getPoint());
                        if (index > -1) {
                            Rectangle bounds = filterableList.getCellBounds(index, index);
                            ListCellRendererWithTotals cellRenderer = (ListCellRendererWithTotals) filterableList.getCellRenderer();
                            Component renderComp = cellRenderer.getListCellRendererComponent(filterableList, filterableList.getModel().getElementAt(index), index, false, false);
                            renderComp.setBounds(bounds);

                            if (cellRenderer.isMouseXOverLabel(e.getPoint())) {
                                //if (index != lastIndex) {
                                    menu = getPopupMenu(filterableList.getModel().getElementAt(index).toString());
                                    menu.show(e.getComponent(), e.getX(), e.getY());
                                //} else if (menu != null && !menu.isVisible()) {
                                //    menu.show(e.getComponent(), e.getX(), e.getY());
                               // }
                            }
                        }
                        lastIndex = index;
                    }
                });


            }
        });

        filterableList.getCheckBoxListSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (model.getSize() > 0) {
            filterableList.setPrototypeCellValue(model.getElementAt(0));    // Makes it much faster to determine the view's preferred size.
        }

        if (selectedValues == null) {
            setAllSelected(true);
            saveSearchConditionParameters();

        } else if (StringConditionEncoder.encodesNotNull(encoding) || StringConditionEncoder.encodesNull(encoding)) {
            setAllSelected(false);
        } else {

            int[] selectedIndices = new int[selectedValues.size()];
            boolean err = false;
            for (int i = 0; i < selectedValues.size(); i++) {
                selectedIndices[i] = values.indexOf(selectedValues.get(i));
                if (selectedIndices[i] == -1) {
                    DialogUtils.displayError(selectedValues.get(i) + " is not an allowable option for " + item.getName());
                    System.err.println(selectedValues.get(i) + " is not an allowable option for " + item.getName());
                    err = true;
                }
            }
            if (err) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        saveSearchConditionParameters();
                        setDescriptionBasedOnSelections();
                    }
                });
            }

            ClientMiscUtils.selectOnlyTheseIndicies(filterableList, selectedIndices);
        }

        SearchableUtils.installSearchable(filterableList);

        filterableList.getCheckBoxListSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !makingBatchChanges) {
                    // TODO save encoding
                    int[] indices = filterableList.getCheckBoxListSelectedIndices();
                    List<String> chosenValues = new ArrayList<String>();
                    for (int i : indices) {
                        chosenValues.add(values.get(i));
                    }
                    saveSearchConditionParameters();
                    setDescriptionBasedOnSelections();
                    is.setSelected(true);
                }
            }
        });

        final StringSearchConditionEditorView instance = this;

        jsp = new JScrollPane(filterableList);

        p.add(field);

        JPanel jspContainer = new JPanel();
        jspContainer.setLayout(new BoxLayout(jspContainer, BoxLayout.Y_AXIS));
        jsp.add(Box.createVerticalGlue());
        jspContainer.add(jsp);

        p.add(jspContainer);
        selectAll = ViewUtil.getSoftButton("Select All");
        selectAll.setFocusable(false);
        selectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                is.setSelected(true);
                setAllSelected(true);

            }
        });

        selectNone = ViewUtil.getSoftButton("Select None");
        selectNone.setFocusable(false);

        selectNone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                is.setSelected(true);
                setAllSelected(false);
            }
        });


        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
        bp.add(selectAll);
        bp.add(selectNone);
        bp.add(Box.createHorizontalGlue());
        p.add(bp);

        if (StringConditionEncoder.encodesNull(encoding)) {
            isNull.setSelected(true);
        } else if (StringConditionEncoder.encodesNotNull(encoding)) {
            isNotNull.setSelected(true);
        } else {
            is.setSelected(true);
            setDescriptionBasedOnSelections();
        }
        add(p);
    }

    protected JPopupMenu getPopupMenu(String itemHoveredOver) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem(itemHoveredOver));
        return menu;
    }

    protected JLabel getNumberInCategory(String category) {
        return new JLabel("");
    }

    private void saveSearchConditionParameters() {
        saveSearchConditionParameters(StringConditionEncoder.encodeConditions(getSelectedOptions()));
    }

    private List<String> getAvailableOptions() {
        List<String> values = new ArrayList<String>();
        int n = filterableList.getCheckBoxListSelectionModel().getModel().getSize();
        for (int i = 0; i < n; i++) {
            values.add(filterableList.getCheckBoxListSelectionModel().getModel().getElementAt(i).toString());
        }
        return values;
    }

    private List<String> getSelectedOptions() {
        int[] indices = filterableList.getCheckBoxListSelectedIndices();
        List<String> chosenValues = new ArrayList<String>();
        for (int i : indices) {
            chosenValues.add(filterableList.getCheckBoxListSelectionModel().getModel().getElementAt(i).toString());
        }
        return chosenValues;
    }

    private void setDescriptionBasedOnSelections() {
        List<String> values = isUserSpecifiedTextMatch ? null : getAvailableOptions();
        List<String> chosenValues = isUserSpecifiedTextMatch ? null : getSelectedOptions();

        String d = StringConditionEncoder.getDescription(chosenValues, values);
        item.setDescription(d);
    }

    private void setAllSelected(boolean b) {
        setAllSelected(b, true);
    }

    private void setAllSelected(boolean b, boolean doSave) {
        makingBatchChanges = true;
        if (b) {
            for (int i = 0; i < filterableList.getModel().getSize(); i++) {
                filterableList.addCheckBoxListSelectedIndex(i);
            }
        } else {
            for (int i = 0; i < filterableList.getModel().getSize(); i++) {
                filterableList.removeCheckBoxListSelectedIndex(i);
            }
        }
        makingBatchChanges = false;
        if (doSave) {
            saveSearchConditionParameters();
            setDescriptionBasedOnSelections();
        }
    }
}
