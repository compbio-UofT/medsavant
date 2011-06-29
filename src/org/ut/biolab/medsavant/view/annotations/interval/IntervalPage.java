package org.ut.biolab.medsavant.view.annotations.interval;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.jidesoft.utils.SwingWorker;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.db.DBUtil;
import org.ut.biolab.medsavant.db.importfile.BedFormat;
import org.ut.biolab.medsavant.db.importfile.FileFormat;
import org.ut.biolab.medsavant.db.importfile.ImportDelimitedFile;
import org.ut.biolab.medsavant.db.importfile.ImportFileView;
import org.ut.biolab.medsavant.view.patients.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class IntervalPage extends SubSectionView {

    int importID = 0;
    
    public IntervalPage(SectionView parent) { 
        super(parent);
    }
    
    public String getName() {
        return "Region Lists";
    }

    public JPanel getView() {
        return new SplitScreenView(
                new IntervalListModel(), 
                new IntervalDetailedView());
    }
    
    public Component[] getBanner() {
        Component[] result = new Component[1];
        result[0] = getAddCohortButton();
        return result;
    }

    private Component getAddCohortButton() {
        JButton b = new JButton("Add region list");
        
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                
                String geneListName = (String) JOptionPane.showInputDialog(
                    null,
                    "Name of Region List:",
                    "Import Region List",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
                
                if (geneListName == null) { return; }
                
                ImportFileView d = new ImportFileView(null,"Import List from File");
                d.addFileFormat(new BedFormat());
                d.setVisible(true);
                
                if (d.isImportAccepted()) {
                    
                    final ImportCohortSW importsw = new ImportCohortSW(
                            geneListName,
                            d.getPath(),
                            d.getDelimiter(),
                            d.getNumHeaderLines(),
                            d.getFileFormat());
                    
                    GenericProgressDialog importpd = new GenericProgressDialog("Importing gene list","Importing gene list");
                    
                    importsw.setProgressDialog(importpd);
                    importpd.setCancelListener(new GenericProgressDialog.CancelRequestListener() {

                        public void cancelRequested() {
                            importsw.cancel(true);
                        }
                    });
                    
                    importsw.execute();
                    
                    importpd.setVisible(true);
                }
                
                d.dispose();
            }
            
        });
        
        return b;
    }
    
    private class ImportCohortSW extends SwingWorker {
        private final String path;
        private final char delim;
        private final int numHeaderLines;
        private final FileFormat fileFormat;
        private GenericProgressDialog progressDialog;
        private final String geneListName;

        private ImportCohortSW(String genelistname, String path, char delimiter, int numHeaderLines, FileFormat ff) {
            this.geneListName = genelistname;
            this.path = path;
            this.delim = delimiter;
            this.numHeaderLines = numHeaderLines;
            this.fileFormat = ff;
        }

        @Override
        protected Object doInBackground() throws Exception {
            
            System.out.println("Importing in the background");
            
            Iterator<String[]> i = ImportDelimitedFile.getFileIterator(path, delim, numHeaderLines,fileFormat);
            DBUtil.addGeneListToDatabase(geneListName,i);
            
            System.out.println("Done importing in the background");
            
            
            return null;
            
        }
        
        @Override
        protected void done() {
            try {
                get();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            if (progressDialog != null) {
                if (!progressDialog.wasCancelRequested()) {
                    progressDialog.setComplete();
                }
            }
        }

        private void setProgressDialog(GenericProgressDialog importpd) {
            this.progressDialog = importpd;
        }

        private void setLinesProcessed(int i) {
            if (progressDialog != null) {
                progressDialog.setStatus(i + " lines parsed");
            }
        }
        
    }
}
