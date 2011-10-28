/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AddPatientsForm.java
 *
 * Created on 29-Jul-2011, 1:34:59 PM
 */
package org.ut.biolab.medsavant.view.dialog;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.format.AnnotationField;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.view.ViewController;

/**
 *
 * @author AndrewBrook
 */
public class AddPatientsForm extends javax.swing.JDialog {

    /** Creates new form AddPatientsForm */
    public AddPatientsForm() {
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);      
        
        createTable();
        
        this.setLocationRelativeTo(null);
        this.setVisible(true);          
    }
    
    private void createTable(){       
        scrollPane.getViewport().setBackground(Color.white);
        
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int col) {  
                if (col == 0) return false;
                return true;        
            }  
        };
        model.addColumn("Short Name");
        model.addColumn("Value");
        
        //List<ModifiableColumn> columns = MedSavantDatabase.getInstance().getPatientTableSchema().getModColumns();
        //for(ModifiableColumn c : columns){
        //    model.addRow(new Object[]{c, ""});
        //}
        try {
            List<AnnotationField> fields = PatientQueryUtil.getPatientFields(ProjectController.getInstance().getCurrentProjectId());
            for(int i = 1; i < fields.size(); i++){ //skip patient id
                model.addRow(new Object[]{fields.get(i), ""});
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddPatientsForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
        table.setModel(model);    
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                setTip();
            }
        });  
        
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }
    
    private void setTip(){
        int index = table.getSelectedRow();
        Object o = table.getValueAt(index, 0);
        if(o == null) return;
        /*ModifiableColumn c = (ModifiableColumn)o;
        String s = c.getShortName() + " | " + c.getType().toString() + "(" + c.getLength() + ")  ";
        switch(c.getType()){
            case DATE:
                s += "(yyyy-mm-dd)";
                break;
            case BOOLEAN:
                s += "(true/false)";
                break;
        }*/
        CustomField f = (CustomField)o;
        String s = f.getAlias() + " | " + f.getFieldType().toString().toLowerCase();
        switch(f.getFieldType()){
            case DATE:
                s += "(yyyy-mm-dd)";
                break;
            case BOOLEAN:
                s += "(true/false)";
                break;
        }
        this.tipLabel.setText(s);   
    }
    
    private void addPatient() throws SQLException{/*
        List<String> values = new ArrayList<String>();
        List<ModifiableColumn> cols = new ArrayList<ModifiableColumn>();
        for(int i = 0; i < table.getRowCount(); i++){
            cols.add((ModifiableColumn)table.getModel().getValueAt(i, 0));
            values.add((String)table.getModel().getValueAt(i, 1));
        }
        DBUtil.addPatient(cols, values);
        clearTable();*/
        
        List<String> values = new ArrayList<String>();
        List<CustomField> cols = new ArrayList<CustomField>();
        for(int i = 0; i < table.getRowCount(); i++){
            String value = (String) table.getModel().getValueAt(i, 1);
            if(value != null && !value.equals("")){
                cols.add((CustomField) table.getModel().getValueAt(i, 0));
                values.add((String)table.getModel().getValueAt(i, 1));
            }           
        }
        
        // replace empty strings for nulls
                for (int i = 0; i < values.size(); i++) {
                    values.set(i, values.get(i).equals("") ? null : values.get(i));
                }
        
        PatientQueryUtil.addPatient(ProjectController.getInstance().getCurrentProjectId(), cols, values);
        clearTable();    
    }
    
    private void clearTable(){
        for(int i = 0; i < table.getRowCount(); i++){
            table.getModel().setValueAt("", i, 1);
        }
    }
    
    private void generateTemplate() throws SQLException{
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Generate Template");
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setMultiSelectionEnabled(false);
        
        int result = fc.showDialog(null, null);
        if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
            return;
        }
        
        File file = fc.getSelectedFile();
        
        List<AnnotationField> fields = PatientQueryUtil.getPatientFields(ProjectController.getInstance().getCurrentProjectId());
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file, false));           
            for(int i = 1; i < fields.size(); i++){ //skip patientId
                out.write(fields.get(i).getAlias());
                if(i != fields.size()-1)
                    out.write("\t");
            }
            out.newLine(); 
            out.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
    
    private void importFile() throws SQLException{
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import File");
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setMultiSelectionEnabled(false);
        
        int result = fc.showDialog(null, null);
        if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
            return;
        }
        
        File file = fc.getSelectedFile();
        //List<ModifiableColumn> columns = MedSavantDatabase.getInstance().getPatientTableSchema().getModColumns();
        
        List<AnnotationField> fields = PatientQueryUtil.getPatientFields(ProjectController.getInstance().getCurrentProjectId());
        
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            String[] headers = line.split("\t");
            //List<ModifiableColumn> headerToColumn = new ArrayList<ModifiableColumn>();
            List<CustomField> headerToField = new ArrayList<CustomField>();
            for(String s : headers){
                boolean found = false;
                //for(ModifiableColumn m : columns){
                for(CustomField f : fields){
                    //if(s.equals(m.getShortName())){
                    if(s.equals(f.getAlias())){
                        headerToField.add(f);
                        found = true;
                        break;
                    }
                }
                if(!found){
                    JOptionPane.showMessageDialog(
                                null, 
                                "<HTML>The headers in this file do not match those in the database.<BR>Please regenerate the template file.</HTML>", 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            while ((line = bufferedReader.readLine()) != null) {
                List<String> values = new ArrayList<String>();
                values.addAll(Arrays.asList(line.split("\t")));
                
                // replace empty strings for nulls
                for (int i = 0; i < values.size(); i++) {
                    values.set(i, values.get(i).equals("") ? null : values.get(i));
                }
                
                //DBUtil.addPatient(headerToColumn, values);
                PatientQueryUtil.addPatient(ProjectController.getInstance().getCurrentProjectId(), headerToField, values);
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {           
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void close(){
        ViewController.getInstance().refreshView();
        this.setVisible(false);
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

        scrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        doneButton = new javax.swing.JButton();
        tipLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Import Patients");

        scrollPane.setBackground(new java.awt.Color(255, 255, 255));

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollPane.setViewportView(table);

        jButton1.setText("Add Patient");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Add Single Patient:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Batch Add:");

        jButton2.setText("Generate Template");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Import From File");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        tipLabel.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(tipLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(doneButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(tipLabel))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(doneButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        close();
    }//GEN-LAST:event_doneButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            addPatient();
        } catch (SQLException ex) {
            Logger.getLogger(AddPatientsForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            generateTemplate();
        } catch (SQLException ex) {
            Logger.getLogger(AddPatientsForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            importFile();
        } catch (SQLException ex) {
            Logger.getLogger(AddPatientsForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton doneButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
    private javax.swing.JLabel tipLabel;
    // End of variables declaration//GEN-END:variables
}
