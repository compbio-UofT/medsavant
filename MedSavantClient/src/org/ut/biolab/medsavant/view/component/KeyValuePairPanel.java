package org.ut.biolab.medsavant.view.component;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class KeyValuePairPanel extends JPanel {

    private Map<String, JLabel> keyValueMap;
    private final Map<String, JComponent[]> keySettingsMap;
    private final int additionalColumns;
    private final ArrayList<GridBagConstraints> columnConstraints;

    public KeyValuePairPanel() {
        this(0);
    }

    public KeyValuePairPanel(int additionalColumns) {

        this.additionalColumns = additionalColumns;
        keyValueMap = new HashMap<String, JLabel>();
        keySettingsMap = new HashMap<String, JComponent[]>();

        columnConstraints = new ArrayList<GridBagConstraints>();

        GridBagConstraints keyConstraints = new GridBagConstraints();
        keyConstraints.anchor = GridBagConstraints.EAST;
        keyConstraints.weightx = 0;
        keyConstraints.gridx = 0;
        keyConstraints.gridy = 0;


        GridBagConstraints valueConstraints = new GridBagConstraints();
        valueConstraints.anchor = GridBagConstraints.WEST;
        valueConstraints.weightx = 1;
        valueConstraints.gridx = 1;
        valueConstraints.gridy = 0;

        columnConstraints.add(keyConstraints);
        columnConstraints.add(valueConstraints);

        for (int i = 0; i < additionalColumns; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.gridx = i + columnConstraints.size();
            c.gridy = 0;
            columnConstraints.add(c);
        }

        this.setLayout(new GridBagLayout());
    }

    public void addKey(String key) {
        JLabel valueLabel = new JLabel();
        JComponent[] extraComponents = new JComponent[additionalColumns];

        int i = 0;

        this.add(new JLabel(key), incrementConstraintRow(i++));
        this.add(valueLabel, incrementConstraintRow(i++));

        for (int j = 0; j < this.additionalColumns; j++) {
            extraComponents[j] = ViewUtil.getClearPanel();
            this.add(extraComponents[j], incrementConstraintRow(i++));
        }

        keyValueMap.put(key, valueLabel);
        keySettingsMap.put(key, extraComponents);

    }

    public void setValue(String key, String value) {
        keyValueMap.get(key).setText(value);
    }

    public void setAdditionalColumn(String key, int additionalColumnIndex, Component c) {
        keySettingsMap.get(key)[additionalColumnIndex].removeAll();
        keySettingsMap.get(key)[additionalColumnIndex].add(c);
    }


    public GridBagConstraints incrementConstraintRow(int col) {
        GridBagConstraints c = columnConstraints.get(col);
        c.gridy++;
        return c;
    }
}
