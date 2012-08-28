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
package org.ut.biolab.medsavant.view.component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class KeyValuePairPanel extends JPanel {

    public static String NULL_VALUE = "<NONE>";
    private Map<String, JLabel> keyKeyComponentMap;
    private Map<String, JPanel> keyValueComponentMap;
    private Map<String, JPanel> keyDetailComponentMap;
    private final Map<String, JComponent[]> keySettingsMap;
    private final int additionalColumns;
    private final ArrayList<GridBagConstraints> columnConstraints;
    //private final GridBagConstraints withinValueConstraints;
    private boolean isShowingMore = false;
    private static final String KEY_MORE = "More";
    private List<String> keysInMoreSection;
    private boolean newRowsGoIntoMoreSection;
    private final GridBagConstraints keyDetailConstraints;
    private JPanel kvpPanel;
    private JPanel toolbar;
    private boolean isKeysVisible = true;

    public KeyValuePairPanel() {
        this(0);
    }
    public static final Font KEY_FONT = new Font("Arial", Font.BOLD, 10);

    public static JLabel getKeyLabel(String s) {
        JLabel l = new JLabel(s.toUpperCase());
        l.setFont(KEY_FONT);
        return l;
    }

    public KeyValuePairPanel(int additionalColumns) {
        this(additionalColumns, false);
    }

    public KeyValuePairPanel(int additionalColumns, boolean widelist) {

        this.setOpaque(false);

        ViewUtil.applyVerticalBoxLayout(this);
        kvpPanel = ViewUtil.getClearPanel();
        toolbar = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(toolbar);

        this.add(kvpPanel);
        this.add(toolbar);

        this.additionalColumns = additionalColumns;
        keyKeyComponentMap = new HashMap<String, JLabel>();
        keyValueComponentMap = new HashMap<String, JPanel>();
        keyDetailComponentMap = new HashMap<String, JPanel>();
        keySettingsMap = new HashMap<String, JComponent[]>();

        columnConstraints = new ArrayList<GridBagConstraints>();
        keysInMoreSection = new ArrayList<String>();

        // Full-width detail component
        keyDetailConstraints = new GridBagConstraints();
        keyDetailConstraints.anchor = GridBagConstraints.SOUTHWEST;
        keyDetailConstraints.weightx = 1.0;
        keyDetailConstraints.fill = GridBagConstraints.BOTH;
        keyDetailConstraints.gridx = 0;
        keyDetailConstraints.gridy = 0;
        keyDetailConstraints.gridwidth = GridBagConstraints.REMAINDER;

        // Constraints for keys
        GridBagConstraints keyConstraints = new GridBagConstraints();
        keyConstraints.anchor = GridBagConstraints.SOUTHWEST;
        keyConstraints.fill = GridBagConstraints.BOTH;
        keyConstraints.weightx = 0;
        keyConstraints.gridx = 0;
        keyConstraints.gridy = 0;
        keyConstraints.ipadx = 5;

        // Constraints for values
        GridBagConstraints valueConstraints = new GridBagConstraints();
        valueConstraints.anchor = GridBagConstraints.SOUTHWEST;
        valueConstraints.fill = GridBagConstraints.BOTH;
        valueConstraints.weightx = widelist ? 0 : 1;
        valueConstraints.gridx = 1;
        valueConstraints.gridy = 0;
        valueConstraints.ipadx = 5;

        columnConstraints.add(keyConstraints);
        columnConstraints.add(valueConstraints);

        // Constraints for additional columns
        for (int i = 0; i < additionalColumns; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.BOTH;

            // 1 iff widelist and last additional column
            c.weightx = widelist ? ((i == additionalColumns - 1) ? 1 : 0) : 0;
            c.gridx = i + columnConstraints.size();
            c.gridy = 0;
            columnConstraints.add(c);
        }

        kvpPanel.setLayout(new GridBagLayout());
    }

    public JComponent getComponent(String key) {
        return (JComponent) keyValueComponentMap.get(key).getComponent(0);
    }

    public String getValue(String key) {

        JComponent c = (JComponent) keyValueComponentMap.get(key).getComponent(0);
        if (c instanceof JLabel) {
            return ((JLabel) keyValueComponentMap.get(key).getComponent(0)).getText();
        } else {
            System.err.println("WARNING: accessing string value of non-string label");
            return c.toString();
        }
    }

    public void addMoreRow() {

        final JToggleButton b = ViewUtil.getSoftToggleButton("MORE");
        ViewUtil.makeMini(b);
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                toggleMoreVisibility();

                if (b.getText().equals("MORE")) {
                    b.setText("LESS");
                } else {
                    b.setText("MORE");
                }
            }
        });

        /*
         * final JLabel keyLabel = new JLabel(); ViewUtil.makeMini(keyLabel);
         * keyLabel.setForeground(Color.darkGray);
         * keyLabel.setBorder(ViewUtil.getMediumBorder());
         *
         * keyLabel.setText("▼ MORE ▼");
         *
         * keyLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
         * keyLabel.addMouseListener(new MouseAdapter() { @Override public void
         * mouseClicked(MouseEvent me) { toggleMoreVisibility();
         *
         * if (keyLabel.getText().startsWith("▲")) { keyLabel.setText("▼ MORE
         * ▼"); } else { keyLabel.setText("▲ LESS ▲"); }
         *
         * }
         * });
         */

        toolbar.add(ViewUtil.getCenterAlignedComponent(b));
        newRowsGoIntoMoreSection = true;

    }

    public void setKeysVisible(boolean b) {
        this.isKeysVisible = b;
        resetKeyVisibility();
    }

    private void resetKeyVisibility() {
        for (String key : this.keyKeyComponentMap.keySet()) {
            this.keyKeyComponentMap.get(key).setVisible(isKeysVisible);
        }
    }

    public void toggleMoreVisibility() {
        setMoreVisibility(!isShowingMore);
    }

    private void setMoreVisibility(boolean b) {
        isShowingMore = b;
        for (String key : this.keysInMoreSection) {
            keyKeyComponentMap.get(key).setVisible(b && this.isKeysVisible);
            keyValueComponentMap.get(key).setVisible(b);
            for (JComponent c : keySettingsMap.get(key)) {
                c.setVisible(b);
            }
            // close opened sub components in more if closing more
            if (!b && keyDetailComponentMap.get(key).isVisible()) {
                keyDetailComponentMap.get(key).setVisible(false);
            }
        }
    }

    public void addKey(String key) {
        addKey(key, false);
    }

    public void addKey(final String key, boolean showExpand) {

        Color rowColor = Color.white;
        if (keyKeyComponentMap.size() % 2 == 0) {
            rowColor = ViewUtil.getAlternateRowColor();
        }

        if (newRowsGoIntoMoreSection) {
            keysInMoreSection.add(key);
        }

        JPanel valuePanel = new JPanel();
        valuePanel.setBackground(rowColor);
        ViewUtil.applyHorizontalBoxLayout(valuePanel);

        JComponent[] extraComponents = new JComponent[additionalColumns];

        int i = 0;

        JPanel keyPanel = new JPanel();
        ViewUtil.applyHorizontalBoxLayout(keyPanel);
        keyPanel.setBackground(rowColor);

        final JLabel keyLabel = getKeyLabel(key);
        keyLabel.setBorder(ViewUtil.getMediumBorder());
        keyKeyComponentMap.put(key, keyLabel);

        if (showExpand) {

            keyLabel.setText("? " + key.toUpperCase());

            keyLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            keyLabel.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent me) {
                    toggleMoreVisibility();

                    if (keyLabel.getText().startsWith("?")) {
                        keyLabel.setText("? " + key.toUpperCase());
                    } else {
                        keyLabel.setText("? " + key.toUpperCase());
                    }
                }
            });
        }

        keyPanel.add(keyLabel);
        keyPanel.add(Box.createHorizontalGlue());

        keyLabel.setVisible(isKeysVisible);

        kvpPanel.add(keyPanel, incrementConstraintRow(i++));
        kvpPanel.add(valuePanel, incrementConstraintRow(i++));

        for (int j = 0; j < this.additionalColumns; j++) {
            JPanel panel = new JPanel();
            panel.setBackground(rowColor);
            ViewUtil.applyHorizontalBoxLayout(panel);
            panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            extraComponents[j] = panel;//ViewUtil.getClearPanel();
            kvpPanel.add(extraComponents[j], incrementConstraintRow(i++));
        }

        /**
         * add hidden panel
         */
        keyDetailConstraints.gridy++;
        keyDetailConstraints.gridy++;

        JPanel detailPanel = new JPanel();
        detailPanel.setBorder(ViewUtil.getTinyLineBorder());
        detailPanel.setVisible(false);
        keyDetailComponentMap.put(key, detailPanel);

        kvpPanel.add(detailPanel, keyDetailConstraints);


        // update all constraints to skip a line
        for (int k = 0; k < 2 + this.additionalColumns; k++) {
            incrementConstraintRow(k);
        }

        keyValueComponentMap.put(key, valuePanel);
        keySettingsMap.put(key, extraComponents);

        setMoreVisibility(this.isShowingMore);
    }

    public void setValue(String key, JComponent value) {
        JPanel p = keyValueComponentMap.get(key);
        p.removeAll();
        p.add(value);
        p.add(Box.createHorizontalGlue());
        p.repaint();
        p.getParent().repaint();
    }

    public void setValue(String key, String value) {

        if (value.equals(NULL_VALUE)) {
            setValue(key, getNullLabel());
            return;
        }

        JLabel c = new JLabel(value); // horizontal padding
        setValue(key, c);
    }
    
    public void setToolTipForValue(String key, String toolTipText){
        if (isKeysVisible) {
            keyKeyComponentMap.get(key).setToolTipText(toolTipText);
        }
        else{
            keyValueComponentMap.get(key).setToolTipText(toolTipText);
        }
    }

    private JComponent getNullLabel() {
        //JPanel p = ViewUtil.getClearPanel();
        JLabel l = new JLabel("NULL");
        l.setForeground(Color.red);
        ViewUtil.makeSmall(l);
        //p.add(ViewUtil.alignLeft(l));
        return l;
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

    public void toggleDetailVisibility(String key) {
        JPanel p = keyDetailComponentMap.get(key);
        toggleDetailVisibility(key,!p.isVisible());
    }

    public void toggleDetailVisibility(String key, boolean visible) {
        JPanel p = keyDetailComponentMap.get(key);
        p.setVisible(visible);
    }

    public void setDetailComponent(String key, JComponent c) {
        JPanel p = keyDetailComponentMap.get(key);
        p.removeAll();
        p.setLayout(new BorderLayout());
        p.add(c, BorderLayout.CENTER);

    }

    public void setKeyColour(String keyName, Color c) {
        this.keyKeyComponentMap.get(keyName).setForeground(c);
    }

    //@pre: param key is key of the bottom-most row in kvp
    public void removeBottomRow(String key){
        if(isKeysVisible)
            kvpPanel.remove(keyKeyComponentMap.remove(key));
        kvpPanel.remove(keyValueComponentMap.remove(key));
        for (JComponent j: keySettingsMap.remove(key)){
            kvpPanel.remove(j);
        }
        kvpPanel.remove(keyDetailComponentMap.remove(key));
        for(GridBagConstraints c: columnConstraints){
            c.gridy--;
        }
        keyDetailConstraints.gridy--;
        kvpPanel.invalidate();
        kvpPanel.updateUI();
    }

    public boolean containsKey(String key) {
        return this.keyKeyComponentMap.containsKey(key);
    }

    public JComponent getAdditionalColumn(String key, int index) {
        return this.keySettingsMap.get(key)[index];
    }
}