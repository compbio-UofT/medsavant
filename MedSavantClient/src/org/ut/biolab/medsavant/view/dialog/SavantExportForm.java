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
package org.ut.biolab.medsavant.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.ExtensionsFileFilter;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.util.WaitPanel;


/**
 *
 * @author AndrewBrook
 */
public class SavantExportForm extends javax.swing.JDialog {
    
    private static final Log LOG = LogFactory.getLog(SavantExportForm.class);

    private JPanel checkBoxPane;
    private List<String> dnaIDs;
    private File outputFile;
    private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
    private JDialog progressDialog;

    /** Creates new form SavantExportForm */
    public SavantExportForm() throws RemoteException, SQLException {
        
        
        //System.err.println("NOT IMPLEMENTED YET");
        //this.dispose();
        //return;
        
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        
        initComponents();
        JPanel container = new JPanel(new BorderLayout());
        scrollPane.getViewport().add(container);
        checkBoxPane = new JPanel();
        checkBoxPane.setLayout(new BoxLayout(checkBoxPane, BoxLayout.Y_AXIS));
        container.add(checkBoxPane, BorderLayout.CENTER); 
        exportButton.setEnabled(false);   

        //populate individuals
        List<String> temp = MedSavantClient.VariantManager.getDistinctValuesForColumn(
                LoginController.sessionId, 
                ProjectController.getInstance().getCurrentPatientTableName(), 
                DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, 
                false);
        dnaIDs = new ArrayList<String>();
        for (String s : temp) {
            for (String s1 : s.split(",")) {
                if (s1 != null && !s1.equals("") && !dnaIDs.contains(s1)) {
                    dnaIDs.add(s1);
                }
            }
        }
        for (String id : dnaIDs) {
            addID(id);
        }
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void addID(String id) {
        JCheckBox box = new JCheckBox(id);
        checkBoxPane.add(box);       
        checkBoxes.add(box);
    }
    
    private void export() throws RemoteException, SQLException, IOException {
        //get selected DNA IDs
        List<String> selectedIds = new ArrayList<String>();
        for (JCheckBox box : checkBoxes) {
            if (box.isSelected()) {
                selectedIds.add(box.getText());
            }
        }
        if (selectedIds.isEmpty()) {
            progressLabel.setText("No individuals selected");
            progressDialog.setVisible(false);
            this.setVisible(true);
            return;
        }
        
        
        //get bookmarks
        Map<String, List<String>> map = MedSavantClient.VariantManager.getSavantBookmarkPositionsForDNAIDs(
                LoginController.sessionId, 
                ProjectController.getInstance().getCurrentProjectID(), 
                ReferenceController.getInstance().getCurrentReferenceID(), 
                FilterController.getQueryFilterConditions(),
                selectedIds, 
                -1);
        
        //get BAM files
        List<String> bamFiles = MedSavantClient.PatientManager.getValuesFromDNAIDs(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL, selectedIds);
        
        //genome version
        String genomeName = ReferenceController.getInstance().getCurrentReferenceName();
        String genomeUrl = ReferenceController.getInstance().getCurrentReferenceUrl();
        
        //create file
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));

        out.write("<?xml version=\"1.0\" ?>\n"
                + "<savant version=\"1\" range=\"chr1:1-1000\">\n");

        if (genomeUrl != null) {
            out.write(" <genome name=\"" + genomeName + "\" uri=\"" + genomeUrl + "\" />\n");
        } else {
            out.write(" <genome name=\"" + genomeName + "\" >\n");               
            for (Chromosome c : ReferenceController.getInstance().getChromosomes()) {
                out.write("   <reference name=\"" + c.getName() + "\" length=\"" + c.getLength() + "\" />\n");
            }  
            out.write(" </genome>\n");          
        }

        for (String path : bamFiles) {
            out.write("  <track uri=\"" + path + "\"/>\n");
        }  

        Object[] keys = map.keySet().toArray();
        for (Object keyObject : keys) {
            String key = (String) keyObject;
            List<String> positions = map.get(key);
            for (String p : positions) {
                out.write("  <bookmark range=\"" + p + "\">" + key + "</bookmark>\n");
            }           
        }

        out.write("</savant>\n");


        //out.write(s);
        out.close();
                
        progressDialog.setVisible(false);
        setVisible(false);
        progressDialog.dispose();
        dispose();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        exportButton = new javax.swing.JButton();
        chooseFileButton = new javax.swing.JButton();
        outputFileField = new javax.swing.JTextField();
        scrollPane = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        progressLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Show in Savant");

        jLabel3.setText("Output Project File: ");

        exportButton.setText("Export");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        chooseFileButton.setText("...");
        chooseFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileButtonActionPerformed(evt);
            }
        });

        outputFileField.setEditable(false);
        outputFileField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputFileFieldActionPerformed(evt);
            }
        });

        scrollPane.setMaximumSize(new java.awt.Dimension(380, 254));
        scrollPane.setMinimumSize(new java.awt.Dimension(380, 254));
        scrollPane.setPreferredSize(new java.awt.Dimension(380, 254));

        jLabel1.setText("Choose Individuals to Export:");

        progressLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        progressLabel.setText(" ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(jLabel3)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(outputFileField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(chooseFileButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(progressLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(exportButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 231, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputFileField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chooseFileButton))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(progressLabel)
                    .add(exportButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        
        //exportButton.setEnabled(false);
        //chooseFileButton.setEnabled(false);
        //for (JCheckBox box : checkBoxes) {
        //    box.setEnabled(false);
        //}
        
        progressDialog = new JDialog();
        progressDialog.setTitle("Show in Savant");
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(300,100));
        p.setLayout(new BorderLayout());
        p.add(new WaitPanel("Exporting Savant Project"));
        progressDialog.getContentPane().add(p);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(null);
        this.setVisible(false);
        progressDialog.setVisible(true);
        
        new MedSavantWorker<Void>("SavantExportForm") {

            @Override
            protected void showProgress(double fraction) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            protected void showSuccess(Void result) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            protected Void doInBackground() throws Exception {
                export();
                return null;
            }
        }.execute();
      
}//GEN-LAST:event_exportButtonActionPerformed

    private void chooseFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFileButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Savant Project");
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.addChoosableFileFilter(new ExtensionsFileFilter("svp"));
        fc.setMultiSelectionEnabled(false);
        
        int result = fc.showDialog(null, null);
        if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
            return;
        }
        
        outputFile = fc.getSelectedFile();
        String path = outputFile.getAbsolutePath();
        outputFileField.setText(path);
        exportButton.setEnabled(true);
}//GEN-LAST:event_chooseFileButtonActionPerformed

    private void outputFileFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputFileFieldActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_outputFileFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseFileButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField outputFileField;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

}
