/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.importfile;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.component.Util;
import org.ut.biolab.medsavant.view.util.PathField;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class ImportFileView extends JDialog {
    private char delimiter = '\t';
    private boolean importAccepted;
    private PathField pathField;
    private JComboBox formatComboBox;
    private int numHeaderLines = 0;
    private final HashMap<String, FileFormat> formatMap;
    private JPanel previewPanel;
    private JPanel waitPanel = new WaitPanel("Generating preview");
    private static PreviewWorker worker;
    
    /** Creates new form ThreadManagerDialog */
    public ImportFileView(Window parent, String title) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        this.setTitle(title);
        initGUI();
        this.setLocationRelativeTo(parent);
        
        importAccepted = false;
        formatMap = new HashMap<String,FileFormat>();
        
    }
    
    private void initGUI() {

        setMinimumSize(new Dimension(600, 600));
        setPreferredSize(new Dimension(600, 600));
        setLayout(new BorderLayout());

        JPanel h1 = new JPanel();
        h1.setBorder(ViewUtil.getMediumBorder());
        
        h1.setLayout(new BoxLayout(h1,BoxLayout.Y_AXIS));
       
        //Delimiter bar
        /*JPanel delimiterBarPanel = new JPanel();
        delimiterBarPanel.setLayout(new BoxLayout(delimiterBarPanel,BoxLayout.X_AXIS));
        
        delimiterBarPanel.add(Box.createHorizontalGlue());
        
        ButtonGroup delimiterBG = new ButtonGroup();
        addDelimiterRadioButton("Tab",'\t',delimiterBarPanel,delimiterBG,true);
        addDelimiterRadioButton("Space",' ',delimiterBarPanel,delimiterBG,false);
        addDelimiterRadioButton("Comma",',',delimiterBarPanel,delimiterBG,false);
        
        delimiterBarPanel.add(Box.createHorizontalGlue());

        h1.add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel("Delimiter")));
        h1.add(delimiterBarPanel); 
              
        h1.add(ViewUtil.getSmallVerticalSeparator());*/

        //File format bar
        h1.add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel("Format")));
        
        formatComboBox = new JComboBox();
        h1.add(formatComboBox);
        
        h1.add(ViewUtil.getSmallVerticalSeparator());
        
        //File chooser bar
        h1.add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel("File")));
        pathField = new PathField(JFileChooser.OPEN_DIALOG);
        
        pathField.getPathArea().getDocument().addDocumentListener(
                new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }

            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            public void changedUpdate(DocumentEvent e) {
            }

        });
        
        
        h1.add(pathField);
        h1.add(ViewUtil.getSmallVerticalSeparator());
        
        h1.add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel("Preview")));
        
        h1.add(Box.createVerticalGlue());
        
        add(h1,BorderLayout.NORTH);
        
        previewPanel = new JPanel();
        previewPanel.setBorder(ViewUtil.getTinyLineBorder());
        
        add(previewPanel,BorderLayout.CENTER);
        
        updatePreview();

        JPanel bottomPanel = ViewUtil.getSecondaryBannerPanel();

        JButton importButton = new JButton("Import");
        importButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (validateForm()) {
                    importAccepted = true;
                    ImportFileView.this.setVisible(false);
                }
            }
        });
        
        
        JButton cancelButton = new JButton("Cancel");
        
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                importAccepted = false;
                ImportFileView.this.setVisible(false);
            }
        });
        
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(importButton);
        bottomPanel.add(cancelButton);
        
        getRootPane().setDefaultButton(importButton);

        add(bottomPanel, BorderLayout.SOUTH);
        
    }

    private void addDelimiterRadioButton(
            String string, final char delim, 
            JPanel delimiterBarPanel, 
            ButtonGroup delimiterBG,
            boolean defaultSelected) {
        
        JRadioButton rb = new JRadioButton(string);
        delimiterBarPanel.add(rb);
        delimiterBG.add(rb);
        rb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setDelimiter(delim);
            }
            
        });
        if (defaultSelected) { 
            rb.setSelected(true);
            this.setDelimiter(delim);
        }
    }
    
    private void setDelimiter(char delim) {
        this.delimiter = delim;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public boolean isImportAccepted() {
        return importAccepted;
    }

    public boolean validateForm() {
        
        File f = new File(pathField.getPath());
        if (!f.exists()) {
            return false;
        }
        
        return true;
    }
    
    //private addFormat(FileFormat ff) {
    //}

    public void addFileFormat(FileFormat f) {
        this.formatComboBox.addItem(f.getName());
        formatMap.put(f.getName(),f);
        this.formatComboBox.updateUI();
    }
    
    public void updatePreview() {
        
        String path = this.pathField.getPath();
        File file = new File(path);
        
        this.previewPanel.removeAll();
        this.previewPanel.setLayout(new BorderLayout());
        
        if (path.equals("")) {
            this.previewPanel.add(
                    ViewUtil.getCenterAlignedComponent(
                        new JLabel("No file to preview")),
                    BorderLayout.CENTER);
        } else if (!file.exists() || !file.isFile()) {
            this.previewPanel.add(
                    ViewUtil.getCenterAlignedComponent(
                        new JLabel("No file at path")),
                    BorderLayout.CENTER);
        } else {
            
            worker = new PreviewWorker();
            worker.execute();
        }
        
        this.previewPanel.updateUI();
    }

    public FileFormat getFileFormat() {
        return this.formatMap.get((String) this.formatComboBox.getSelectedItem());
    }
    
        
    public String getPath() {
        return this.pathField.getPath();
    }
    
    public int getNumHeaderLines() {
        return this.numHeaderLines;
    }

    /**
     * SwingWorker whose job it is to fetch a fifty-line preview of the file being imported.
     */
    class PreviewWorker extends MedSavantWorker<List<String[]>> {

        private static final int NUM_LINES = 50;

        @SuppressWarnings("LeakingThisInConstructor")
        PreviewWorker() {
            super("X");
            if (worker != null) {
                worker.cancel(true);
            }
            worker = this;
        }

        @Override
        protected List<String[]> doInBackground() throws Exception {
            showProgress(-1.0);
            // This method returns two lists: header and data.  We only care about the data.
            return ImportDelimitedFile.getPreview(pathField.getPath(), getDelimiter(), numHeaderLines, NUM_LINES, getFileFormat())[1];
        }
        
        @SuppressWarnings("unchecked")
        protected void showSuccess(List<String[]> data) {
            int[] fields = getFileFormat().getRequiredFieldIndexes();
            List<String> columnNames = new ArrayList<String>();
            List<Class> columnClasses = new ArrayList<Class>();
            for (int i : fields) {
                columnNames.add(getFileFormat().getFieldNumberToFieldNameMap().get(i));
                columnClasses.add(getFileFormat().getFieldNumberToClassMap().get(i));
            }

            SearchableTablePanel searchableTablePanel = new SearchableTablePanel(ImportFileView.class.getName(), columnNames, columnClasses, new ArrayList<Integer>(), false,false,50,false,false, 1000, Util.createPrefetchedDataRetriever(data));

            //boolean allowSearch, boolean allowSort, int defaultRows, boolean allowSelection
            previewPanel.add(searchableTablePanel,BorderLayout.CENTER);
            previewPanel.updateUI();
        }

        @Override
        protected void showProgress(double fraction) {
            if (fraction < 0.0) {
                previewPanel.add(waitPanel);
            } else {
                worker = null;
                previewPanel.remove(waitPanel);        
            }
        }
    }
}
