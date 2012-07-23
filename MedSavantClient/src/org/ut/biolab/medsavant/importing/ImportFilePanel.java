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

package org.ut.biolab.medsavant.importing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import org.ut.biolab.medsavant.view.component.SearchableTablePanel.TableSelectionType;
import org.ut.biolab.medsavant.util.DataRetriever;
import org.ut.biolab.medsavant.view.util.PathField;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class ImportFilePanel extends JPanel {
    
    private char delimiter = '\t';
    private boolean importAccepted;
    private PathField pathField;
    private JComboBox formatComboBox;
    private int numHeaderLines = 0;
    private final HashMap<String, FileFormat> formatMap;
    private JPanel previewPanel;
    private WaitPanel waitPanel = new WaitPanel("Generating preview");
    private PreviewWorker worker;
    
    public ImportFilePanel(){
        initGUI();
        importAccepted = false;
        formatMap = new HashMap<String,FileFormat>();
    }
    
    private void initGUI() {

        //setMinimumSize(new Dimension(600, 600));
        //setPreferredSize(new Dimension(600, 600));
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel h1 = new JPanel();
        h1.setBorder(ViewUtil.getMediumBorder());

        h1.setLayout(new BoxLayout(h1,BoxLayout.Y_AXIS));

        //Delimiter bar
        JPanel delimiterBarPanel = new JPanel();
        delimiterBarPanel.setOpaque(false);
        delimiterBarPanel.setLayout(new BoxLayout(delimiterBarPanel,BoxLayout.X_AXIS));

        delimiterBarPanel.add(Box.createHorizontalGlue());

        ButtonGroup delimiterBG = new ButtonGroup();
        addDelimiterRadioButton("Tab",'\t',delimiterBarPanel,delimiterBG,true);
        addDelimiterRadioButton("Space",' ',delimiterBarPanel,delimiterBG,false);
        addDelimiterRadioButton("Comma",',',delimiterBarPanel,delimiterBG,false);

        delimiterBarPanel.add(Box.createHorizontalGlue());

        h1.add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel("Delimiter")));
        h1.add(delimiterBarPanel);

        h1.add(ViewUtil.getSmallVerticalSeparator());

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

            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

        });


        h1.add(pathField);
        h1.add(ViewUtil.getSmallVerticalSeparator());

        h1.add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel("Preview")));

        h1.add(Box.createVerticalGlue());

        h1.setOpaque(false);
        add(h1,BorderLayout.NORTH);

        previewPanel = new JPanel();
        previewPanel.setLayout(new BorderLayout());
        previewPanel.setPreferredSize(new Dimension(1000,1000));
        previewPanel.setBorder(ViewUtil.getTinyLineBorder());

        add(previewPanel, BorderLayout.CENTER);

        updatePreview();
    }
    
    private void addDelimiterRadioButton(
            String string, final char delim,
            JPanel delimiterBarPanel,
            ButtonGroup delimiterBG,
            boolean defaultSelected) {

        JRadioButton rb = new JRadioButton(string);
        rb.setOpaque(false);
        delimiterBarPanel.add(rb);
        delimiterBG.add(rb);
        rb.addActionListener(new ActionListener() {
            @Override
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
        delimiter = delim;
        updatePreview();
    }

    public char getDelimiter() {
        return delimiter;
    }

    public boolean isImportAccepted() {
        return importAccepted;
    }

    public boolean validateForm() {
        return pathField.getFile().exists();
    }

    //private addFormat(FileFormat ff) {
    //}

    public void addFileFormat(FileFormat f) {
        this.formatComboBox.addItem(f.getName());
        formatMap.put(f.getName(),f);
        this.formatComboBox.updateUI();
    }

    public void updatePreview() {

        if (pathField == null) { return; }

        String path = pathField.getPath();
        File file = new File(path);

        previewPanel.removeAll();

        if (path.equals("")) {
            previewPanel.add(new JLabel("No file to preview.", JLabel.CENTER), BorderLayout.CENTER);
            setReady(false);
        } else if (!file.exists() || !file.isFile()) {
            previewPanel.add(new JLabel("No file at path.", JLabel.CENTER), BorderLayout.CENTER);
            setReady(false);
        } else {
            worker = new PreviewWorker();
            worker.execute();
        }

        this.previewPanel.updateUI();
    }

    public FileFormat getFileFormat() {
        return formatMap.get((String)formatComboBox.getSelectedItem());
    }


    public String getPath() {
        return pathField.getPath();
    }

    public int getNumHeaderLines() {
        return numHeaderLines;
    }
    
    public void setReady(boolean ready){
        // Override if necessary. 
        // Notifies parent that there is a file ready to go. 
    }

    /**
     * SwingWorker whose job it is to fetch a fifty-line preview of the file being imported.
     */
    class PreviewWorker extends MedSavantWorker<List<String[]>> {

        private static final int NUM_LINES = 50;

        PreviewWorker() {
            super("X");
            if (worker != null) {
                worker.cancel(true);
                worker = null;
            }
        }

        @Override
        protected List<String[]> doInBackground() throws Exception {
            showProgress(-1.0);

            // This method returns two lists: header and data.  We only care about the data.
            return ImportDelimitedFile.getPreview(pathField.getPath(), getDelimiter(), numHeaderLines, NUM_LINES, getFileFormat())[1];
        }

        @Override
        protected void showSuccess(List<String[]> data) {

            int[] fields = getFileFormat().getRequiredFieldIndexes();
            List<String> columnNames = new ArrayList<String>();
            List<Class> columnClasses = new ArrayList<Class>();
            for (int i : fields) {
                columnNames.add(getFileFormat().getFieldNumberToFieldNameMap().get(i));
                columnClasses.add(getFileFormat().getFieldNumberToClassMap().get(i));
            }

            SearchableTablePanel searchableTablePanel = new SearchableTablePanel(
                    ImportFileView.class.getName(),
                    columnNames.toArray(new String[0]),
                    columnClasses.toArray(new Class[0]),
                    new int[0],
                    false,
                    false,
                    50,
                    false,
                    TableSelectionType.ROW,
                    1000,
                    DataRetriever.createPrefetchedDataRetriever(data));
            searchableTablePanel.forceRefreshData();

            //boolean allowSearch, boolean allowSort, int defaultRows, boolean allowSelection
            previewPanel.add(searchableTablePanel,BorderLayout.CENTER);
            previewPanel.updateUI();
            importAccepted = true;
            setReady(true);
        }

        @Override
        protected void showProgress(double fraction) {
            if (fraction < 0.0) {
                previewPanel.add(waitPanel);
            } else if (fraction >= 1.0) {
                worker = null;
                previewPanel.remove(waitPanel);
            }
        }

        @Override
        protected void showFailure(Throwable t) {
            previewPanel.removeAll();
            previewPanel.add(new JLabel("<html><center><font color=\"#ff0000\">Problem generating preview.<br>Please check that the file is formatted correctly.</font></center></html>", JLabel.CENTER), BorderLayout.CENTER);
            previewPanel.updateUI();
            importAccepted = false;
            setReady(false);
        }
    }    
}
