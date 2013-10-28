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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.component;

import com.jidesoft.document.DocumentComponent;
import com.jidesoft.document.DocumentPane;
import com.jidesoft.icons.JideIconsFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop.Action;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import savant.view.icon.SavantIconFactory;

/**
 *
 * @author mfiume
 */
public class DocumentViewer extends JFrame {

    DocumentPane pane;
    java.util.List<DocumentComponent> list;
    private int maxLineCount;
    JTextField textNumLines;

    static DocumentViewer instance;

    public static DocumentViewer getInstance() {
        if (instance == null) {
            instance = new DocumentViewer();
        }
        return instance;
    }

    public DocumentViewer() {
        this("Document Viewer",500);
    }

    public DocumentViewer(int maxLineCount) {
        this("Document Viewer",maxLineCount);
    }

    public DocumentViewer(String title, int maxLineCount) {
        this.setTitle(title);
        this.maxLineCount = maxLineCount;
        init();
    }

    public void init() {

        this.setMinimumSize(new Dimension(700,500));
        this.setPreferredSize(new Dimension(700,500));
        this.setMaximumSize(new Dimension(700,500));

        pane = new DocumentPane();
        list = new ArrayList<DocumentComponent>();


        this.setLayout(new BorderLayout());

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.add(new JLabel("Show at most this many lines: "));

        textNumLines = new JTextField(maxLineCount + "");
        textNumLines.setMaximumSize(new Dimension(100,20));
        textNumLines.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                try {
                    int newNum = Integer.parseInt(textNumLines.getText().trim());
                    setMaximumNumberOfLines(newNum);
                } catch (Exception ex) {
                    textNumLines.requestFocus();
                }
            }

        });
        textNumLines.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {

                try {
                    int newNum = Integer.parseInt(textNumLines.getText().trim());
                    setMaximumNumberOfLines(newNum);
                } catch (Exception ex) {
                    textNumLines.requestFocus();
                }

            }

        });

        bar.add(textNumLines);

        JButton upButt = new JButton();
        upButt.setIcon(SavantIconFactory.getInstance().getIcon(SavantIconFactory.StandardIcon.UP));
        upButt.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                decreaseMaximumNumberOfLines();
            }

        });
        bar.add(upButt);

        JButton downButt = new JButton();
        downButt.setIcon(SavantIconFactory.getInstance().getIcon(SavantIconFactory.StandardIcon.DOWN));
        downButt.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                increaseMaximumNumberOfLines();
            }

        });
        bar.add(downButt);

        this.add(bar, BorderLayout.NORTH);
        this.add(pane, BorderLayout.CENTER);
    }

    double factor = 0.5;
    public void increaseMaximumNumberOfLines() {
        setMaximumNumberOfLines((int) (1 + maxLineCount*(1+factor)));
    }

    public void decreaseMaximumNumberOfLines() {
        setMaximumNumberOfLines((int) (1 + maxLineCount*(1-factor)));
    }

    public void setMaximumNumberOfLines(int newnum) {
        textNumLines.setText(newnum + "");
        maxLineCount = newnum;
        for (String key : fileNameToDocumentMap.keySet()) {
            fillDocument(fileNameToDocumentMap.get(key),key,maxLineCount);
        }
    }

    public void addDocument(String path) {

        if (!(new File(path)).exists()) {
            JOptionPane.showMessageDialog(this, "Error opening file: \"" + path + "\"");
            return;
        }

        JComponent editor = createTextArea(path);
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        final DocumentComponent txtDocument = new DocumentComponent(new JScrollPane(editor),
                path, path,
                JideIconsFactory.getImageIcon(JideIconsFactory.FileType.TEXT));
        txtDocument.setDefaultFocusComponent(editor);
        list.add(0,txtDocument);
        pane.setOpenedDocuments(list);
        pane.getLayoutPersistence().setProfileKey("documents");
        pane.getLayoutPersistence().loadLayoutData();

        this.pack();
        this.setVisible(true);
    }

    private Map<String,Document> fileNameToDocumentMap = new HashMap<String, Document>();

     public JTextArea createTextArea(String fileName) {
        JTextArea area = new JTextArea();
        Document doc = new PlainDocument();
        fillDocument(doc, fileName, maxLineCount);
        area.setEditable(false);
        area.setDocument(doc);
        fileNameToDocumentMap.put(fileName, doc);
        return area;
    }

     public static void fillDocument(Document doc, String filename, int numLines) {

            try {
                doc.remove(0, doc.getLength());
                BufferedReader br = new BufferedReader(new FileReader(filename));

                String line = "";
                int i;
                for (i = 0; (numLines == -1 || i < numLines) && ((line = br.readLine()) != null); i++) {
                    doc.insertString(doc.getLength(), line + "\n", null);
                }
                if (br.ready()) {
                    doc.insertString(doc.getLength(), "[ Stopped at " + numLines + " lines ]", null);
                }

                br.close();
            } catch (Exception ex) {
            try {
                doc.insertString(0, "Error reading file", null);
            } catch (BadLocationException ex1) {}
            }
     }
}
