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
package org.ut.biolab.medsavant.client.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.shared.util.ExtensionsFileFilter;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

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

    /**
     * Creates new form SavantExportForm
     */
    public SavantExportForm() throws InterruptedException, SQLException, RemoteException {


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
        List<String> temp;
        try {
            temp = MedSavantClient.DBUtils.getDistinctValuesForColumn(
                    LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentPatientTableName(),
                    BasicPatientColumns.DNA_IDS.getColumnName(),
                    false,
                    false);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
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

    private void export() throws InterruptedException, IOException, SQLException, RemoteException {
        try {
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
                    LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    ReferenceController.getInstance().getCurrentReferenceID(),
                    FilterController.getInstance().getAllFilterConditions(),
                    selectedIds,
                    -1);

            //get BAM files
            Collection<String> bamFiles = MedSavantClient.PatientManager.getValuesFromDNAIDs(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), BasicPatientColumns.BAM_URL.getColumnName(), selectedIds).values();

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
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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
        p.setPreferredSize(new Dimension(300, 100));
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
        fc.addChoosableFileFilter(new ExtensionsFileFilter(new String[]{"svp"}));
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
