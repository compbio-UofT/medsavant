package org.ut.biolab.medsavant.view.component;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

    private static final Font keyFont = new Font("Arial",Font.BOLD,11);

    public static JLabel getKeyLabel(String s) {
        JLabel l = new JLabel(s.toUpperCase());
        l.setFont(keyFont);
        return l;
    }

    public KeyValuePairPanel(int additionalColumns) {

        this.setOpaque(false);

        this.additionalColumns = additionalColumns;
        keyValueMap = new HashMap<String, JLabel>();
        keySettingsMap = new HashMap<String, JComponent[]>();

        columnConstraints = new ArrayList<GridBagConstraints>();

        GridBagConstraints keyConstraints = new GridBagConstraints();
        keyConstraints.anchor = GridBagConstraints.SOUTHEAST;
        keyConstraints.weightx = 0;
        keyConstraints.gridx = 0;
        keyConstraints.gridy = 0;


        GridBagConstraints valueConstraints = new GridBagConstraints();
        valueConstraints.anchor = GridBagConstraints.SOUTHWEST;
        valueConstraints.insets = new Insets(1, 5, 1, 2);
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

        this.add(getKeyLabel(key), incrementConstraintRow(i++));
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
