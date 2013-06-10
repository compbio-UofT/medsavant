package org.ut.biolab.mfiume.query.view;

import com.jidesoft.list.FilterableCheckBoxList;
import com.jidesoft.list.QuickListFilterField;
import com.jidesoft.swing.SearchableUtils;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.value.encode.StringConditionEncoder;
import org.ut.biolab.mfiume.query.value.StringConditionValueGenerator;

/**
 *
 * @author mfiume
 */
public class StringSearchConditionEditorView extends SearchConditionEditorView {

    private final StringConditionValueGenerator valueGenerator;
    private QuickListFilterField field;
    private final int FIELD_WIDTH = 200;
    private FilterableCheckBoxList filterableList;
    private List<String> values;
    private boolean cacheOn = true;
    private JButton selectNone;
    private JButton selectAll;
    private JScrollPane jsp;
    private boolean makingBatchChanges = false;

    public StringSearchConditionEditorView(SearchConditionItem i, final StringConditionValueGenerator vg) {
        super(i);
        this.valueGenerator = vg;
    }

    private void loadLooseStringMatchViewFromSearchConditionParameters(String encoding) {
        this.removeAll();
        final JTextField f = new JTextField();
        PromptSupport.setPrompt("Enter " + item.getName(), f);
        PromptSupport.setFocusBehavior(PromptSupport.FocusBehavior.SHOW_PROMPT, f);
        f.setPreferredSize(new Dimension(200, f.getPreferredSize().height));
        if (encoding != null) {
            f.setText(encoding);
        }
        f.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent ce) {
                saveSearchConditionParameters(f.getText());
                item.setDescription(f.getText());
            }
        });
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

        if (valueGenerator == null) {
            loadLooseStringMatchViewFromSearchConditionParameters(encoding);
            return;
        }

        System.out.println("Loading view from existing condition parameters " + item.getName());

        if (!cacheOn || values == null) {
            values = valueGenerator.getStringValues();
        }


        this.removeAll();

        if (values == null || values.isEmpty()) {
            this.add(new JLabel("This field is not populated"));
            return;
        }

        final JRadioButton isNull = new JRadioButton("null");
        final JRadioButton isNotNull = new JRadioButton("not null");
        final JRadioButton is = new JRadioButton("is");
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
                    item.setDescription("null");
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
                    item.setDescription("not null");
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
        ViewUtil.applyHorizontalBoxLayout(controlButtons);
        controlButtons.add(isNull);
        controlButtons.add(isNotNull);
        controlButtons.add(is);


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
            saveSearchConditionParameters();

        } else if (StringConditionEncoder.encodesNotNull(encoding) || StringConditionEncoder.encodesNull(encoding)) {
            setAllSelected(false);
        } else {

            int[] selectedIndices = new int[selectedValues.size()];
            for (int i = 0; i < selectedValues.size(); i++) {
                selectedIndices[i] = values.indexOf(selectedValues.get(i));
                if (selectedIndices[i] == -1) {
                    System.err.println(selectedValues.get(i) + " is not an allowable option for " + item.getName());
                }
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

        jsp = new JScrollPane(filterableList) {
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

        add(controlButtons, gbc);
        add(field, gbc);

        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        add(jsp, gbc);

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

        if (StringConditionEncoder.encodesNull(encoding)) {
            isNull.setSelected(true);
            //hideListFields();
        } else if (StringConditionEncoder.encodesNotNull(encoding)) {
            isNotNull.setSelected(true);
            //hideListFields();
        } else {
            is.setSelected(true);
            setDescriptionBasedOnSelections();
            //showListFields();
        }
    }

    private void saveSearchConditionParameters() {
        saveSearchConditionParameters(StringConditionEncoder.encodeConditions(getSelectedOptions()));
    }

    private List<String> getAvailableOptions() {
        List<String> values = new ArrayList<String>();
        int n = filterableList.getCheckBoxListSelectionModel().getModel().getSize();
        for (int i = 0; i < n; i++) {
            values.add(filterableList.getCheckBoxListSelectionModel().getModel().getElementAt(i).toString());
            //System.out.println(values.get(i));
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
        List<String> values = getAvailableOptions();
        List<String> chosenValues = getSelectedOptions();

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
