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

package org.ut.biolab.medsavant.view.manage;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.util.query.RegionQueryUtil;
import org.ut.biolab.medsavant.importfile.BedFormat;
import org.ut.biolab.medsavant.importfile.FileFormat;
import org.ut.biolab.medsavant.importfile.ImportDelimitedFile;
import org.ut.biolab.medsavant.importfile.ImportFileView;
import org.ut.biolab.medsavant.view.patients.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class IntervalPage extends SubSectionView {
    private static final Logger LOG = Logger.getLogger(IntervalPage.class.getName());

    int importID = 0;
    SplitScreenView view;
    
    public IntervalPage(SectionView parent) { 
        super(parent);
    }
    
    public String getName() {
        return "Region Lists";
    }

    public JPanel getView(boolean update) {
        view = new SplitScreenView(
                new IntervalListModel(), 
                new IntervalDetailedView());
        return view;
    }
    
    @Override
    public Component[] getBanner() {
        return new Component[] { getAddCohortButton() };
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
                view.refresh();
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
            
            Iterator<String[]> i = ImportDelimitedFile.getFileIterator(path, delim, numHeaderLines,fileFormat);
            
            //DBUtil.addRegionList(geneListName,i);
            RegionQueryUtil.addRegionList(geneListName, ReferenceController.getInstance().getCurrentReferenceId(), i);
            
            return null;
            
        }
        
        @Override
        protected void done() {
            try {
                get();
            } catch (Exception x) {
                // TODO: #90
                LOG.log(Level.SEVERE, null, x);
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
    
    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }
    
}
