/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.importfile;

import au.com.bytecode.opencsv.CSVReader;
import com.jidesoft.utils.SwingWorker;
import fiume.table.SearchableTablePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.view.util.PathField;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class ImportFileView extends javax.swing.JDialog {

    private char delimiter;
    private boolean importAccepted;
    private PathField pathField;
    private JComboBox formatComboBox;
    private int numHeaderLines = 0;
    private final HashMap<String, FileFormat> formatMap;
    private JPanel previewPanel;
    private JPanel waitPanel = new WaitPanel("Generating preview");
    
    /** Creates new form ThreadManagerDialog */
    public ImportFileView(java.awt.Frame parent, String title) {
        super(parent, true);
        this.setTitle(title);
        initGUI();
        this.setLocationRelativeTo(null);
        
        importAccepted = false;
        formatMap = new HashMap<String,FileFormat>();
        
    }
    
    private void initGUI() {

        this.setMinimumSize(new Dimension(600, 600));
        this.setPreferredSize(new Dimension(600, 600));
        this.setLayout(new BorderLayout());

        JPanel h1 = new JPanel();
        h1.setBorder(ViewUtil.getMediumBorder());
        
        h1.setLayout(new BoxLayout(h1,BoxLayout.Y_AXIS));
        
        JPanel delimiterBarPanel = new JPanel();
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
        
        h1.add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel("Format")));
        
        formatComboBox = new JComboBox();
        h1.add(formatComboBox);
        
        h1.add(ViewUtil.getSmallVerticalSeparator());
        
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
        
        this.add(h1,BorderLayout.NORTH);
        
        previewPanel = new JPanel();
        previewPanel.setBorder(ViewUtil.getTinyLineBorder());
        
        this.add(previewPanel,BorderLayout.CENTER);
        
        updatePreview();

        JPanel bottomPanel = ViewUtil.getBannerPanel();

        final JDialog thisDialog = this;
        
        JButton importButton = new JButton("Import");
        importButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (validateForm()) {
                    setImportAccepted(true);
                }
                thisDialog.setVisible(false);
            }
        });
        
        
        JButton cancelButton = new JButton("Cancel");
        
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setImportAccepted(false);
                thisDialog.setVisible(false);
            }
        });
        
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(importButton);
        bottomPanel.add(cancelButton);
        
        this.getRootPane().setDefaultButton(importButton);

        this.add(bottomPanel, BorderLayout.SOUTH);
        
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

    public void setImportAccepted(boolean importAccepted) {
        this.importAccepted = importAccepted;
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
            
            this.previewPanel.add(waitPanel);
            
            PreviewSwingWorker psw = new PreviewSwingWorker(
                    this.pathField.getPath(),
                    this.getDelimiter(),
                    this.getFileFormat()
                    );
            
            psw.execute();
        }
        
        this.previewPanel.updateUI();
    }

    public FileFormat getFileFormat() {
        return this.formatMap.get((String) this.formatComboBox.getSelectedItem());
    }
    
    public synchronized void setPreview(List<String[]> header, List<String[]> rest) {
        
        List<Vector> data = Util.convertToListOfVectors(rest);
        
        int[] fields = this.getFileFormat().getRequiredFieldIndexes();
        List<String> columnNames = new ArrayList<String>();
        List<Class> columnClasses = new ArrayList<Class>();
        for (int i : fields) {
            columnNames.add(this.getFileFormat().getFieldNumberToFieldNameMap().get(i));
            columnClasses.add(this.getFileFormat().getFieldNumberToClassMap().get(i));
        }
        
        SearchableTablePanel searchableTablePanel = new SearchableTablePanel(
                Util.listToVector(data),columnNames,columnClasses,new ArrayList<Integer>(),
                false,false,50,false,false, 1000);
        
        //boolean allowSearch, boolean allowSort, int defaultRows, boolean allowSelection
        this.previewPanel.remove(waitPanel);
        this.previewPanel.add(searchableTablePanel,BorderLayout.CENTER);
        this.previewPanel.updateUI();
        
        //searchableTablePanel.updateData(Util.listToVector(data));
    }
    
    class PreviewSwingWorker extends SwingWorker {

        
        private int numLines = 50;
        
        private String path;
        private char separator;
        private FileFormat ff;

        public PreviewSwingWorker(String path, char separator, FileFormat ff) {
            this.path = path;
            this.separator = separator;
            this.ff = ff;
        }
        
        @Override
        protected Object doInBackground() throws Exception {
            return ImportDelimitedFile.getPreview(path, separator, numHeaderLines, numLines, ff);
        }
        
        @Override
        protected void done() {
            
            try {
                Object o = get();
                Object[] preview = (Object[]) o;
                
                List<String[]> header = (List<String[]>) preview[0];
                List<String[]> rest = (List<String[]>) preview[1];
                
                

                
                setPreview(header,rest);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
        }
        
    }
    
    public String getPath() {
        return this.pathField.getPath();
    }
    
    public int getNumHeaderLines() {
        return this.numHeaderLines;
    }
}
