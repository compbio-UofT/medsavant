/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.query.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.query.SearchConditionItem;
import org.ut.biolab.medsavant.client.query.value.encode.StringConditionEncoder;

import savant.api.util.DialogUtils;

/**
 *
 * @author jim
 */
public class GeneSearchConditionEditorView extends SearchConditionEditorView {

    private static final int[] REFRESH_KEY_CODES = {KeyEvent.VK_ENTER, KeyEvent.VK_SPACE, KeyEvent.VK_TAB, KeyEvent.VK_PASTE};
    private static final String GENE_DELIMITER = "\\s+|\\t+|,|\\n|\\r\\n";
    private static final Color GENE_INVALID_COLOR = Color.RED;
    private static final Color GENE_VALID_COLOR = Color.BLACK;
    private static final Dimension PREFERRED_SIZE = new Dimension(80, 150);

    private JTextPane textArea;
    private boolean hasInvalidGene = false;

    public GeneSearchConditionEditorView(SearchConditionItem item) {
        super(item);
    }

    public void clearTextArea() {
        textArea.setText(null);
        hasInvalidGene = false;
    }

    private void refreshTextArea() {
        refreshTextArea(false);
    }

    private void refreshTextArea(boolean clean) {
        //reads from text area, reformats spaces with newlines, removes spurious newlines, checks that entered 
        //genes are valid.                           
        String s = textArea.getText();
        if (s.isEmpty()) {
            hasInvalidGene = true;
            return;
        }
        textArea.setText("");
        hasInvalidGene = false;

        String[] geneNames = s.trim().split(GENE_DELIMITER);
        try {
            Set<String> geneSet = new LinkedHashSet<String>();
            for (String geneName : geneNames) {
                geneSet.add(geneName.toUpperCase());
            }

            Set<String> toRemove = new HashSet<String>();
            for (String geneName : geneSet) {
                if (GeneSetController.getInstance().getGene(geneName.toUpperCase()) == null) { //gene not found
                    if (!clean) { //flag gene as invalid, color it invalid.
                        hasInvalidGene = true;
                        appendToPane(textArea, geneName + "\n", GENE_INVALID_COLOR);
                    } else {//else skip this gene.
                        toRemove.add(geneName);
                    }
                } else { //gene is valid
                    appendToPane(textArea, geneName + "\n", GENE_VALID_COLOR);
                }
            }
            geneSet.removeAll(toRemove);

            List<String> geneList = new ArrayList<String>(geneSet);
            item.setSearchConditionEncoding(StringConditionEncoder.encodeConditions(geneList));
            item.setDescription(StringConditionEncoder.getDescription(geneList));
        } catch (SQLException se) {
            DialogUtils.displayException("Error checking genes", "Error communicating with remote database", se);
        } catch (RemoteException re) {
            DialogUtils.displayException("Error checking genes", "Error communicating with remote database", re);
        }
    }

    @Override
    public boolean saveChanges() throws IllegalArgumentException {
        super.saveChanges();
        refreshTextArea();
        if (textArea.getText().isEmpty()) {
            throw new IllegalArgumentException("Please enter at least one gene");
        }
        if (hasInvalidGene) {
            if (DialogUtils.askYesNo("One or more of the entered genes was not recognized.  Do you want to ignore them and continue?") == JOptionPane.NO_OPTION) {
                return false;
            } else {
                refreshTextArea(true);
            }
        }
        return true;
    }

    private void appendToPane(JTextPane tp, String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }

    @Override
    public void loadViewFromSearchConditionParameters(String encoding) throws SearchConditionEditorView.ConditionRestorationException {
        if (textArea != null) {
            return;
        }
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        textArea = new JTextPane();
        textArea.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent me) {
                super.mouseReleased(me);
                if (me.getButton() == MouseEvent.BUTTON2) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            refreshTextArea();
                        }
                    });
                }
            }
        });

        //If text area focused or unfocused, refresh and check genes.
        textArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
                refreshTextArea();
            }

            @Override
            public void focusLost(FocusEvent fe) {
                refreshTextArea();
            }
        });

        //if Any of the refresh_key_codes are hit, refresh and check genes.
        textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {

                if (ke.getKeyCode() == KeyEvent.VK_PASTE || (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_V)
                        || (ke.isShiftDown() && ke.getKeyCode() == KeyEvent.VK_INSERT)) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            refreshTextArea();
                        }
                    });

                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                for (int refreshCode : REFRESH_KEY_CODES) {
                    ke.isControlDown();
                    if ((ke.getKeyCode() == refreshCode)) {
                        refreshTextArea();
                    }

                }
            }
        });

        JScrollPane jsp = new JScrollPane(textArea);
        p.add(new JLabel("Please type or paste gene names: "), BorderLayout.NORTH);
        p.add(jsp, BorderLayout.CENTER);
        this.add(p);
        if (encoding != null) {
            List<String> geneNames = StringConditionEncoder.unencodeConditions(encoding);
            item.setSearchConditionEncoding(encoding);
            item.setDescription(StringConditionEncoder.getDescription(geneNames));
            String s = "";
            for (String geneName : geneNames) {
                s = s + geneName + "\n";
            }
            textArea.setText(s);
            refreshTextArea();
            revalidate();
        }
        textArea.setPreferredSize(PREFERRED_SIZE);

    }
};
