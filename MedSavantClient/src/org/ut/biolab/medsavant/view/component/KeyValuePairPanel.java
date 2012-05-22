package org.ut.biolab.medsavant.view.component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class KeyValuePairPanel extends JPanel {

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

    public KeyValuePairPanel() {
        this(0);
    }
    private static final Font keyFont = new Font("Arial", Font.BOLD, 10);

    public static JLabel getKeyLabel(String s) {
        JLabel l = new JLabel(s.toUpperCase());
        l.setFont(keyFont);
        return l;
    }

    public KeyValuePairPanel(int additionalColumns) {

        this.setOpaque(false);

        this.additionalColumns = additionalColumns;
        keyKeyComponentMap = new HashMap<String, JLabel>();
        keyValueComponentMap = new HashMap<String, JPanel>();
        keyDetailComponentMap = new HashMap<String, JPanel>();
        keySettingsMap = new HashMap<String, JComponent[]>();

        columnConstraints = new ArrayList<GridBagConstraints>();
        keysInMoreSection = new ArrayList<String>();

        // Contstraints of value components
        /*withinValueConstraints = new GridBagConstraints();
        withinValueConstraints.anchor = GridBagConstraints.SOUTHWEST;
        withinValueConstraints.weightx = 0;
        withinValueConstraints.gridx = 0;
        withinValueConstraints.gridy = 0;*/

        // Full-width detail component
        keyDetailConstraints = new GridBagConstraints();
        keyDetailConstraints.anchor = GridBagConstraints.SOUTHWEST;
        keyDetailConstraints.weightx = 0;
        keyDetailConstraints.gridx = 0;
        keyDetailConstraints.gridy = 0;
        keyDetailConstraints.gridwidth = 2 + this.additionalColumns;

        // Constraints for keys
        GridBagConstraints keyConstraints = new GridBagConstraints();
        keyConstraints.anchor = GridBagConstraints.WEST;
        keyConstraints.fill = GridBagConstraints.BOTH;
        keyConstraints.weightx = 0;
        keyConstraints.gridx = 0;
        keyConstraints.gridy = 0;

        // Constraints for values
        GridBagConstraints valueConstraints = new GridBagConstraints();
        valueConstraints.anchor = GridBagConstraints.WEST;
        valueConstraints.fill = GridBagConstraints.BOTH;
        valueConstraints.weightx = 1;
        valueConstraints.gridx = 1;
        valueConstraints.gridy = 0;

        columnConstraints.add(keyConstraints);
        columnConstraints.add(valueConstraints);

        // Constraints for additional columns
        for (int i = 0; i < additionalColumns; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 0;
            c.gridx = i + columnConstraints.size();
            c.gridy = 0;
            columnConstraints.add(c);
        }

        this.setLayout(new GridBagLayout());
    }

    public JComponent getComponent(String key) {
        return keyValueComponentMap.get(key);
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

        addKey(KEY_MORE, true);
        setValue(KEY_MORE, " "); // required to keep vertical height
        newRowsGoIntoMoreSection = true;

    }

    public void toggleMoreVisibility() {
        setMoreVisibility(!isShowingMore);
    }

    private void setMoreVisibility(boolean b) {
        isShowingMore = b;
        for (String key : this.keysInMoreSection) {
            keyKeyComponentMap.get(key).setVisible(b);
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
            rowColor = ViewUtil.getMenuColor();
        }

        if (newRowsGoIntoMoreSection) {
            keysInMoreSection.add(key);
        }

        JPanel valuePanel = new JPanel();
        valuePanel.setBackground(rowColor);
        valuePanel.setLayout(new GridBagLayout());

        JComponent[] extraComponents = new JComponent[additionalColumns];

        int i = 0;

        JPanel keyPanel = new JPanel();
        ViewUtil.applyHorizontalBoxLayout(keyPanel);
        keyPanel.setBackground(rowColor);

        final JLabel keyLabel = getKeyLabel(key);
        keyKeyComponentMap.put(key, keyLabel);

        if (showExpand) {

            keyLabel.setText("► " + key.toUpperCase());

            keyLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            keyLabel.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent me) {
                    toggleMoreVisibility();

                    if (keyLabel.getText().startsWith("►")) {
                        keyLabel.setText("▼ " + key.toUpperCase());
                    } else {
                        keyLabel.setText("► " + key.toUpperCase());
                    }

                }

                @Override
                public void mousePressed(MouseEvent me) {
                }

                @Override
                public void mouseReleased(MouseEvent me) {
                }

                @Override
                public void mouseEntered(MouseEvent me) {
                }

                @Override
                public void mouseExited(MouseEvent me) {
                }
            });
        }

        keyPanel.add(keyLabel);
        keyPanel.add(Box.createHorizontalGlue());

        this.add(keyPanel, incrementConstraintRow(i++));
        this.add(valuePanel, incrementConstraintRow(i++));

        for (int j = 0; j < this.additionalColumns; j++) {
            JPanel panel = new JPanel();
            panel.setBackground(rowColor);
            ViewUtil.applyHorizontalBoxLayout(panel);
            panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            extraComponents[j] = panel;//ViewUtil.getClearPanel();
            this.add(extraComponents[j], incrementConstraintRow(i++));
        }

        /**
         * add hidden panel
         */
        keyDetailConstraints.gridy++;
        keyDetailConstraints.gridy++;

        JPanel detailPanel = new JPanel();
        detailPanel.setVisible(false);
        keyDetailComponentMap.put(key, detailPanel);

        this.add(detailPanel, keyDetailConstraints);


        // update all constraints to skip a line
        for (int k = 0; k < 2 + this.additionalColumns; k++) {
            incrementConstraintRow(k);
        }

        keyValueComponentMap.put(key, valuePanel);
        keySettingsMap.put(key, extraComponents);

        setMoreVisibility(this.isShowingMore);
    }

    public void setValue(String key, JComponent value) {
        keyValueComponentMap.get(key).removeAll();
        keyValueComponentMap.get(key).add(value);
        keyValueComponentMap.get(key).repaint();
        keyValueComponentMap.get(key).getParent().repaint();
    }

    public void setValue(String key, String value) {
        JLabel c = new JLabel(value);
        setValue(key, c);
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
        p.setVisible(!p.isVisible());
    }

    public void setDetailComponent(String key, JComponent c) {
        JPanel p = keyDetailComponentMap.get(key);
        p.removeAll();
        p.setLayout(new BorderLayout());
        p.add(c, BorderLayout.CENTER);

    }
}
