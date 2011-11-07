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
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.util.query.RegionQueryUtil;
import org.ut.biolab.medsavant.importfile.BedFormat;
import org.ut.biolab.medsavant.importfile.FileFormat;
import org.ut.biolab.medsavant.importfile.ImportDelimitedFile;
import org.ut.biolab.medsavant.importfile.ImportFileView;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.patients.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.GenericProgressDialog;

/**
 *
 * @author mfiume
 */
public class IntervalPage extends SubSectionView {

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
        return new Component[] { getAddRegionListButton() };
    }

    private Component getAddRegionListButton() {
        JButton b = new JButton("Add region list");
        
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String geneListName = DialogUtils.displayInputMessage("Import Region List", "Name of Region List:", null);
                if (geneListName != null) {

                    ImportFileView d = new ImportFileView(DialogUtils.getMainWindow(), "Import List from File");
                    d.addFileFormat(new BedFormat());
                    d.setVisible(true);
                    if (d.isImportAccepted()) {
                        new ImportRegionListWorker(geneListName, d.getPath(), d.getDelimiter(), d.getNumHeaderLines(), d.getFileFormat()).execute();
                    }

                    d.dispose();
                }
            }
            
        });
        
        return b;
    }
    
    private class ImportRegionListWorker extends MedSavantWorker<Object> {
        private final String path;
        private final char delim;
        private final int numHeaderLines;
        private final FileFormat fileFormat;
        private GenericProgressDialog progressDialog;
        private final String geneListName;

        private ImportRegionListWorker(String geneListName, String path, char delimiter, int numHeaderLines, FileFormat ff) {
            this.geneListName = geneListName;
            this.path = path;
            this.delim = delimiter;
            this.numHeaderLines = numHeaderLines;
            this.fileFormat = ff;
        }

        @Override
        protected Object doInBackground() throws Exception {
            showProgress(-1.0);
            Iterator<String[]> i = ImportDelimitedFile.getFileIterator(path, delim, numHeaderLines,fileFormat);
            
            RegionQueryUtil.addRegionList(geneListName, ReferenceController.getInstance().getCurrentReferenceId(), i);
            
            return null;
            
        }
        
        @Override
        protected void showProgress(double fraction) {
            if (fraction == 1.0) {
                progressDialog.setComplete();
            } else {
                if (progressDialog == null) {
                    progressDialog = new GenericProgressDialog("Importing gene list","Importing gene list") {
                        @Override
                        public void cancelRequested() {
                            cancel(true);
                        }
                    };
                    progressDialog.setVisible(true);
                }
            }
        }

        @Override
        protected void showSuccess(Object result) {
            view.refresh();
        }
    }
    
    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }
    
}
