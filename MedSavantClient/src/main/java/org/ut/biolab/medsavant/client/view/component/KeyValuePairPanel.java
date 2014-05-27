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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.Border;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.view.font.FontFactory;

import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class KeyValuePairPanel extends JPanel {

    public static final String NULL_VALUE = "<NONE>";
    public static final Font KEY_FONT = FontFactory.getGeneralFont().deriveFont(11f).deriveFont(Font.BOLD);
    private Map<String, JLabel> keyKeyComponentMap;
    private Map<String, JPanel> keyValueComponentMap;
    private Map<String, JPanel> keyDetailComponentMap;
    private final Map<String, JPanel[]> keyExtraComponentsMap;
    private final int additionalColumns;
    private final ArrayList<GridBagConstraints> columnConstraints;
    private boolean showingMore = false;
    private List<String> keysInMoreSection;
    private boolean newRowsGoIntoMoreSection;
    private final GridBagConstraints keyDetailConstraints;
    private JPanel kvpPanel;
    private JPanel toolbar;
    private boolean keysVisible = true;
    private static final Log LOG = LogFactory.getLog(KeyValuePairPanel.class);

    public KeyValuePairPanel() {
        this(0);
    }

    public KeyValuePairPanel(int additionalColumns) {
        this(additionalColumns, false);
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public KeyValuePairPanel(int addCols, boolean widelist) {

        setOpaque(false);

        ViewUtil.applyVerticalBoxLayout(this);
        kvpPanel = ViewUtil.getClearPanel();
        toolbar = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(toolbar);

        add(kvpPanel);
        add(toolbar);

        additionalColumns = addCols;
        keyKeyComponentMap = new HashMap<String, JLabel>();
        keyValueComponentMap = new HashMap<String, JPanel>();
        keyDetailComponentMap = new HashMap<String, JPanel>();
        keyExtraComponentsMap = new HashMap<String, JPanel[]>();

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

        
        GridBagLayout gbl = new GridBagLayout();
        kvpPanel.setLayout(gbl);
    }
    
    public void setXPadding(int padx) {
        columnConstraints.get(0).ipadx = padx;
        columnConstraints.get(1).ipadx = padx;
    }
    
     public void setYPadding(int pady) {
        columnConstraints.get(0).ipady = pady;
        columnConstraints.get(1).ipady = pady;
    }

    public static JLabel getKeyLabel(String s) {
        JLabel l = new JLabel(s);
        l.setFont(KEY_FONT);
        return l;
    }

    public JComponent getComponent(String key) {
        JPanel valuePanel = keyValueComponentMap.get(key);
        if (valuePanel.getComponentCount() > 0) {
            return (JComponent) valuePanel.getComponent(0);
        }
        return null;
    }

    public String getValue(String key) {

        JComponent c = getComponent(key);
        if (c instanceof JLabel) {
            // the text of the label may be truncated, use the tooltip instead
            return ((JLabel) c).getToolTipText();
        } else {
            LOG.warn("WARNING: accessing string value of non-string label");
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
        keysVisible = b;
        resetKeyVisibility();
    }

    private void resetKeyVisibility() {
        for (String key : keyKeyComponentMap.keySet()) {
            keyKeyComponentMap.get(key).setVisible(keysVisible);
        }
    }

    public void toggleMoreVisibility() {
        setMoreVisibility(!showingMore);
    }

    private void setMoreVisibility(boolean b) {
        showingMore = b;
        for (String key : keysInMoreSection) {
            keyKeyComponentMap.get(key).setVisible(b && keysVisible);
            keyValueComponentMap.get(key).setVisible(b);
            for (JComponent c : keyExtraComponentsMap.get(key)) {
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
    
    public void addKeyWithValue(String key, String value) {
        this.addKey(key);
        this.setValue(key, value);
    }

    public void addKeyWithValue(String key, JComponent value) {
        this.addKey(key);
        this.setValue(key, value);
    }
    
    public void addKey(final String key, boolean showExpand) {

        Color rowColor = Color.WHITE;
        if (keyKeyComponentMap.size() % 2 == 0) {
            rowColor = ViewUtil.getAlternateRowColor();
        }

        if (newRowsGoIntoMoreSection) {
            keysInMoreSection.add(key);
        }
        
        rowColor = Color.white;

        String layoutConstraints = "insets 3 3 3 3, filly";
        
        JPanel valuePanel = ViewUtil.getClearPanel();
        valuePanel.setLayout(new MigLayout(layoutConstraints));
        valuePanel.setBackground(rowColor);

        int i = 0;

        JPanel keyPanel = ViewUtil.getClearPanel();
        keyPanel.setLayout(new MigLayout(layoutConstraints + ", alignx right, hmin 30"));
        
        keyPanel.setBackground(rowColor);

        final JLabel keyLabel = getKeyLabel(key);
          
        //keyLabel.setBorder(ViewUtil.getMediumBorder());
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

        keyLabel.setVisible(keysVisible);

        kvpPanel.add(keyPanel, incrementConstraintRow(i++));
        kvpPanel.add(valuePanel, incrementConstraintRow(i++));

        JPanel[] extraComponents = new JPanel[additionalColumns];
        for (int j = 0; j < additionalColumns; j++) {
            JPanel panel = ViewUtil.getClearPanel();
            panel.setBackground(rowColor);
            ViewUtil.applyHorizontalBoxLayout(panel);
            //panel.setBorder(border);
            //panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            extraComponents[j] = panel;//ViewUtil.getClearPanel();
            kvpPanel.add(extraComponents[j], incrementConstraintRow(i++));
        }

        // add hidden panel
        keyDetailConstraints.gridy++;
        keyDetailConstraints.gridy++;

        JPanel detailPanel = ViewUtil.getClearPanel();
        detailPanel.setBackground(rowColor);
        //detailPanel.setBorder(ViewUtil.getTinyLineBorder());
        detailPanel.setVisible(false);
        keyDetailComponentMap.put(key, detailPanel);

        kvpPanel.add(detailPanel, keyDetailConstraints);


        // update all constraints to skip a line
        for (int k = 0; k < 2 + additionalColumns; k++) {
            incrementConstraintRow(k);
        }

        keyValueComponentMap.put(key, valuePanel);
        keyExtraComponentsMap.put(key, extraComponents);

        setMoreVisibility(showingMore);
    }

    public void setValue(String key, JComponent value) {
        JPanel p = keyValueComponentMap.get(key);
        p.removeAll();
        p.add(value);
        p.add(Box.createHorizontalGlue());
        p.repaint();
        p.getParent().repaint();
    }

    /**
     * Set the value for this key to be a
     * <code>JLabel</code> displaying said value. No attempt will be made to
     * split a long tooltip containing comma-delimited values.
     */
    public void setValue(String key, String value) {
        setValue(key, value, false);
    }

    /**
     * Set the value for this key to be a
     * <code>JLabel</code> displaying said value. If
     * <code>splitCommas</code> is true, the value's tooltip will display
     * comma-delimited values on separate rows.
     */
    public void setValue(String key, String value, boolean splitCommas) {

        if (value == null || value.equals(NULL_VALUE)) {
            setValue(key, getNullLabel());
            return;
        }

        if (splitCommas && 
                ((value.indexOf(',') > 0) || value.indexOf(';') > 0)) {
            // For comma-separated and colon-separated lists, we want the text to be multi-line HTML.
            value = "<html>" + value.replace(",", "<br/>").replace(";", "<br/>") + "</html>";
            
        }
        
        JLabel c = new JLabel(value);
        if (splitCommas) {
            c.setToolTipText(value);
        }
        setValue(key, c);
    }

    public void setToolTipForValue(String key, String toolTipText) {
        if (keysVisible) {
            keyKeyComponentMap.get(key).setToolTipText(toolTipText);
        } else {
            keyValueComponentMap.get(key).setToolTipText(toolTipText);
        }
    }

    private JLabel getNullLabel() {
        //JPanel p = ViewUtil.getClearPanel();
        JLabel l = new JLabel("NULL");
        l.setForeground(Color.red);
        ViewUtil.makeSmall(l);
        //p.add(ViewUtil.alignLeft(l));
        return l;
    }

    public void setAdditionalColumn(String key, int additionalColumnIndex, Component c) {
        keyExtraComponentsMap.get(key)[additionalColumnIndex].removeAll();
        keyExtraComponentsMap.get(key)[additionalColumnIndex].add(c);
    }

    public GridBagConstraints incrementConstraintRow(int col) {
        GridBagConstraints c = columnConstraints.get(col);
        c.gridy++;
        return c;
    }

    public void toggleDetailVisibility(String key) {
        JPanel p = keyDetailComponentMap.get(key);
        toggleDetailVisibility(key, !p.isVisible());
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
        keyKeyComponentMap.get(keyName).setForeground(c);
    }

    //@pre: param key is key of the bottom-most row in kvp
    public void removeBottomRow(String key) {
        if (keysVisible) {
            kvpPanel.remove(keyKeyComponentMap.remove(key));
        }
        kvpPanel.remove(keyValueComponentMap.remove(key));
        for (JPanel j : keyExtraComponentsMap.remove(key)) {
            kvpPanel.remove(j);
        }
        kvpPanel.remove(keyDetailComponentMap.remove(key));
        for (GridBagConstraints c : columnConstraints) {
            c.gridy--;
        }
        keyDetailConstraints.gridy--;
        kvpPanel.invalidate();
        kvpPanel.updateUI();
    }

    public boolean containsKey(String key) {
        return keyKeyComponentMap.containsKey(key);
    }

    public JComponent getAdditionalColumn(String key, int index) {
        JPanel p = keyExtraComponentsMap.get(key)[index];
        if (p.getComponentCount() > 0) {
            return (JComponent) p.getComponent(0);
        }
        return null;
    }

    public static Component getCopyButton(final String key, final KeyValuePairPanel p) {
        JButton button = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.COPY));
        button.setToolTipText("Copy " + key);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String selection = p.getValue(key);
                StringSelection data = new StringSelection(selection);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(data, data);
                DialogUtils.displayMessage("Copied \"" + selection + "\" to clipboard.");
            }
        });
        return button;
    }

    public void simpleEllipsify(){
        for(String k : keyValueComponentMap.keySet()){
            JComponent comp = getComponent(k);
            if(comp instanceof JLabel){
                JLabel l = (JLabel)comp;
                String s = l.getText();
                if(s.length() > 25){
                    s = s.substring(0, 20)+"...";
                }
                l.setText(s);
            }

        }
    }
    public void ellipsifyValues(int width) {
        int maxKeyWidth = 0;
        int[] maxAdditionalColumnsWidth = new int[additionalColumns];

        for (JComponent keyComp : keyKeyComponentMap.values()) {
            maxKeyWidth = Math.max(maxKeyWidth, keyComp.getPreferredSize().width);
        }

        for (String k : keyValueComponentMap.keySet()) {
            for (int i = 0; i < additionalColumns; i++) {
                JComponent extraComp = getAdditionalColumn(k, i);
                if (extraComp != null) {
                    maxAdditionalColumnsWidth[i] = Math.max(maxAdditionalColumnsWidth[i], extraComp.getPreferredSize().width);
                }
            }
        }

        width -= maxKeyWidth;
        for (int i : maxAdditionalColumnsWidth) {
            width -= i;
        }


        for (String k : keyValueComponentMap.keySet()) {
            JComponent comp = getComponent(k);
            if (comp != null) {
                int avail = width;
                /*for (int i = 0; i < additionalColumns; i++) {
                 JComponent extraComp = getAdditionalColumn(k, i);
                 if (extraComp != null) {
                 avail -= extraComp.getPreferredSize().width;
                 }
                 }*/

                if (comp instanceof JLabel) {
                    
                    while (avail < comp.getPreferredSize().width) {
                        String text = ((JLabel) comp).getText();
                        if (text.endsWith("…")) {
                            if (text.length() > 2) {
                                // Already truncated.
                                text = text.substring(0, text.length() - 2);
                            } else {
                                break; // As short as we can get.  Can't truncate any more.
                            }
                        } else {
                            // Reasonable first truncation is to trim off the last word.
                            int spacePos = text.lastIndexOf(' ');
                            if (spacePos > 0) {
                                text = text.substring(0, spacePos);
                            } else {
                                FontMetrics fm = comp.getFontMetrics(comp.getFont());

                                while (fm.stringWidth(text + "…") > avail) {
                                    //causes StringIndexOutOfBoundsException if text is empty.  
                                    if(text == null || text.length() < 2){
                                        LOG.info("Text is null or empty in KeyValuePairPanel");
                                        break;
                                    }
                                    text = text.substring(0, text.length() - 2);
                                }
                                //text = text + "…";

                                //text = text.substring(0, text.length() - 1);
                            }
                        }
                        ((JLabel) comp).setText(text + "…");
                    }
                } else {
                    // Can't truncate, but we can force the preferred size.
                    comp.setMaximumSize(new Dimension(avail, comp.getPreferredSize().height));
                }
            }
        }
    }
}