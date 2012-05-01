/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.importfile;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.ut.biolab.medsavant.importing.FileFormat;
import org.ut.biolab.medsavant.importing.ImportDelimitedFile;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel.TableSelectionType;
import org.ut.biolab.medsavant.view.component.Util;
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
    private static PreviewWorker worker;
    private JButton importButton;
    
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
        previewPanel.setPreferredSize(new Dimension(1000,1000));
        previewPanel.setBorder(ViewUtil.getTinyLineBorder());

        add(previewPanel,BorderLayout.CENTER);

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
        this.delimiter = delim;
        this.updatePreview();
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

        if (this.pathField == null) { return; }

        String path = this.pathField.getPath();
        File file = new File(path);

        this.previewPanel.removeAll();
        this.previewPanel.setLayout(new BorderLayout());

        if (path.equals("")) {
            this.previewPanel.add(
                    ViewUtil.getCenterAlignedComponent(
                        new JLabel("No file to preview")),
                    BorderLayout.CENTER);
            setReady(false);
        } else if (!file.exists() || !file.isFile()) {
            this.previewPanel.add(
                    ViewUtil.getCenterAlignedComponent(
                        new JLabel("No file at path")),
                    BorderLayout.CENTER);
            setReady(false);
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
    
    public void setReady(boolean ready){
        // Override if necessary. 
        // Notifies parent that there is a file ready to go. 
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

            try {
                return ImportDelimitedFile.getPreview(pathField.getPath(), getDelimiter(), numHeaderLines, NUM_LINES, getFileFormat())[1];
            }
            catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void showSuccess(List<String[]> data) {

            if (data == null) {
                waitPanel.setStatus("Problem generating preview.\nPlease check that the file is formatted correctly.");
                waitPanel.setComplete();
                previewPanel.add(waitPanel,BorderLayout.CENTER);
                previewPanel.updateUI();
                importAccepted = false;
                setReady(false);
            } else {
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
                        Util.createPrefetchedDataRetriever(data));
                searchableTablePanel.forceRefreshData();

                //boolean allowSearch, boolean allowSort, int defaultRows, boolean allowSelection
                previewPanel.add(searchableTablePanel,BorderLayout.CENTER);
                previewPanel.updateUI();
                importAccepted = true;
                setReady(true);
            }

            updateImportButton();
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

        private void updateImportButton() {
            if (importButton != null) {
                importButton.setEnabled(validateForm() && importAccepted);
            }
        }
    }
    
}
