/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SavantExportForm.java
 *
 * Created on 5-Jul-2011, 12:51:11 PM
 */

package org.ut.biolab.medsavant.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author AndrewBrook
 */
public class SavantExportForm extends javax.swing.JDialog {
    
    private JPanel checkBoxPane;
    private List<String> dnaIds;
    private File outputFile;
    private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
    private JDialog progressDialog;

    /** Creates new form SavantExportForm */
    public SavantExportForm() {
        
        
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
        try {
            List<String> temp = VariantQueryUtil.getDistinctValuesForColumn(ProjectController.getInstance().getCurrentPatientTableName(), DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS);
            dnaIds = new ArrayList<String>();
            for(String s : temp){
                for(String s1 : s.split(",")){
                    if(s1 != null && !s1.equals("") && !dnaIds.contains(s1)){
                        dnaIds.add(s1);
                    }
                }
            }
            for(String id : dnaIds){
                addId(id);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SavantExportForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);  
    }
    
    private void addId(String id){
        JCheckBox box = new JCheckBox(id);
        checkBoxPane.add(box);       
        checkBoxes.add(box);
    }
    
    private void export(){
        //get selected DNA IDs
        List<String> selectedIds = new ArrayList<String>();
        for(JCheckBox box : checkBoxes){
            if(box.isSelected()){
                selectedIds.add(box.getText());
            }
        }
        if(selectedIds.isEmpty()){
            progressLabel.setText("No individuals selected");
            progressDialog.setVisible(false);
            this.setVisible(true);
            return;
        }
        
        
        //get bookmarks
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        try {
            map = VariantQueryUtil.getSavantBookmarkPositionsForDNAIds(
                    ProjectController.getInstance().getCurrentProjectId(), 
                    ReferenceController.getInstance().getCurrentReferenceId(), 
                    FilterController.getQueryFilterConditions(),
                    selectedIds, 
                    -1);
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        
        //get BAM files
        List<String> bamFiles = new ArrayList<String>();
        try {
            bamFiles = PatientQueryUtil.getValuesFromDNAIds(ProjectController.getInstance().getCurrentProjectId(), DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL, selectedIds);
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        //genome version
        //TODO: currently hard coded; need to store this somewhere
        String genomeName = "hg19.fa.savant";
        String genomeUrl = "http://savantbrowser.com/data/hg19/hg19.fa.savant";
        
        //create file
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
        
            out.write("<?xml version=\"1.0\" ?>\n"
                    + "<savant version=\"1\" range=\"chr1:1-1000\">\n"
                    + " <genome name=\"" + genomeName + "\" uri=\"" + genomeUrl + "\" />\n");

            for(String path : bamFiles){
                out.write("  <track uri=\"" + path + "\"/>\n");
            }  

            Object[] keys = map.keySet().toArray();
            for(Object keyObject : keys){
                String key = (String) keyObject;
                List<String> positions = map.get(key);
                for(String p : positions){
                    out.write("  <bookmark range=\"" + p + "\">" + key + "</bookmark>\n");
                }           
            }

            out.write("</savant>\n");

        
            //out.write(s);
            out.close();
        } catch (Exception e){
            e.printStackTrace();
        }
                
        progressDialog.setVisible(false);
        this.setVisible(false);
        progressDialog.dispose();
        this.dispose();
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
        //for(JCheckBox box : checkBoxes){
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
        
        Thread thread = new Thread() {
            @Override
            public void run() {
                export();
            }
        };
        thread.start();
        
}//GEN-LAST:event_exportButtonActionPerformed

    private void chooseFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFileButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Savant Project");
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.addChoosableFileFilter(new ExtensionFileFilter("svp"));
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

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SavantExportForm().setVisible(true);
            }
        });
    }

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
