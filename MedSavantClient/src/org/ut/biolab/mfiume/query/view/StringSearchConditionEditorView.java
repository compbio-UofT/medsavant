package org.ut.biolab.mfiume.query.view;

import com.jidesoft.list.FilterableCheckBoxList;
import com.jidesoft.list.QuickListFilterField;
import com.jidesoft.swing.SearchableUtils;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;
import org.ut.biolab.medsavant.client.filter.TabularFilterView;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.value.StringConditionValueGenerator;

/**
 *
 * @author mfiume
 */
public class StringSearchConditionEditorView extends SearchConditionEditorView {

    private final StringConditionValueGenerator generator;
    private QuickListFilterField field;
    private final int FIELD_WIDTH = 200;
    private FilterableCheckBoxList filterableList;

    public StringSearchConditionEditorView(SearchConditionItem i, final StringConditionValueGenerator g) {
        super(i);
        this.generator = g;
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
                return "(null)";
            }
            return val;
        }
    }

    @Override
    public void loadViewFromSearchConditionParameters(String encoding) throws ConditionRestorationException {

        System.out.println("Loading view from existing condition parameters " + item.getName());

        List<String> selectedValues;
        if (encoding == null) {
            selectedValues = null;
        } else {
            selectedValues = StringSearchConditionEditorView.unencodeConditions(encoding);
        }

        System.out.println("Asking generator for values");
        final List<String> values = generator.getStringValues();
        this.removeAll();

        AbstractListModel model = new SimpleListModel(values);

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


        if (selectedValues == null) {
            setAllSelected(true);
        } else {
            int[] selectedIndices = new int[selectedValues.size()];
            for (int i = 0; i < selectedValues.size(); i++) {
                selectedIndices[i] = values.indexOf(selectedValues.get(i));
                if (selectedIndices[i] == -1) {
                    throw new ConditionRestorationException(selectedValues.get(i) + " is not an allowable option for " + item.getName());
                }
            }
            ClientMiscUtils.selectOnlyTheseIndicies(filterableList, selectedIndices);
        }

        SearchableUtils.installSearchable(filterableList);

        filterableList.getCheckBoxListSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // TODO save encoding
                    int[] indices = filterableList.getCheckBoxListSelectedIndices();
                    List<String> chosenValues = new ArrayList<String>();
                    for (int i : indices) {
                        chosenValues.add(values.get(i));
                    }
                    saveSearchConditionParameters(StringSearchConditionEditorView.encodeConditions(chosenValues));
                    if (chosenValues.isEmpty()) {
                        item.setDescription("is none");
                    } else if (chosenValues.size() == 1) {
                        item.setDescription("is " + chosenValues.get(0));
                    } else {
                        item.setDescription("is any of " + chosenValues.size());
                    }
                }
            }
        });

        final StringSearchConditionEditorView instance = this;

        JScrollPane jsp = new JScrollPane(filterableList) {
            @Override
            public Dimension getPreferredSize() {
                Dimension result = super.getPreferredSize();
                result = new Dimension(Math.min(result.width, instance.getWidth() - 20), result.height);
                return result;
            }
        };

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 15, 3, 15);
        add(field, gbc);

        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        add(jsp, gbc);

        JButton selectAll = ViewUtil.getSoftButton("Select All");
        selectAll.setFocusable(false);
        selectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllSelected(true);
            }
        });

        JButton selectNone = ViewUtil.getSoftButton("Select None");
        selectNone.setFocusable(false);

        selectNone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllSelected(false);
            }
        });

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
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
    }
    /**
     * Serialization
     */
    private static String DELIM = ",";

    public static String encodeConditions(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (Object string : values) {
            result.append(string);
            result.append(DELIM);
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }

    public static List<String> unencodeConditions(String s) {
        String[] arr = s.split(DELIM);
        return Arrays.asList(arr);
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
}
