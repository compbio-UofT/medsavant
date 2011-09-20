/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ManageDB.java
 *
 * Created on 28-Jul-2011, 11:50:38 AM
 */
package org.ut.biolab.medsavant.olddb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.ut.biolab.medsavant.olddb.table.TableSchema.ColumnType;
import org.ut.biolab.medsavant.olddb.table.ModifiableColumn;
import org.ut.biolab.medsavant.olddb.table.ModifiableTableSchema;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.view.ViewController;

/**
 *
 * @author AndrewBrook
 */
public class ManageDB extends javax.swing.JDialog {
    
    private String tableComboDefault = "Choose table to modify";
    private ModifiableTableSchema currentSchema; 
    private ModifiableColumn currentColumn;

    /** Creates new form ManageDB */
    public ManageDB(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        //listeners
        columnList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Object o = columnList.getSelectedValue();
                if(o != null){
                    populateDetails((ModifiableColumn)o);
                }      
            }
        });
        columnNameField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {}
            public void focusLost(FocusEvent e) {
                if(currentColumn != null){
                    currentColumn.setColumnName(columnNameField.getText());
                }
            }
        });
        shortNameField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {}
            public void focusLost(FocusEvent e) {
                if(currentColumn != null){
                    currentColumn.setShortName(shortNameField.getText());
                }
            }
        });
        descriptionField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {}
            public void focusLost(FocusEvent e) {
                if(currentColumn != null){
                    currentColumn.setDescription(descriptionField.getText());
                }
            }
        });
        typeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(currentColumn != null){
                    currentColumn.setType((ColumnType)typeCombo.getSelectedItem());
                }
            }
        });
        lengthField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {}
            public void focusLost(FocusEvent e) {
                if(currentColumn == null) return;
                try {
                    currentColumn.setLength(Integer.parseInt(lengthField.getText()));
                } catch (NumberFormatException ex){
                    lengthField.setText(String.valueOf(currentColumn.getLength()));
                }             
            }
        });       
        useAsFilterCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(currentColumn != null){
                    currentColumn.setIsFilter(useAsFilterCheckBox.isSelected());
                }
            }
        });
        
        //set up visibility
        setMandatory(true);
        mandatoryLabel.setVisible(false);
        addButton.setEnabled(false);
        removeButton.setEnabled(false);
        applyButton.setEnabled(false);
        
        //fill tableCombo
        tableCombo.addItem(tableComboDefault);
        for(ModifiableTableSchema m : MedSavantDatabase.getInstance().getModifiableTables()){
            tableCombo.addItem(m);
        }
        
        //fill typeCombo
        for(ColumnType ct : ColumnType.values()){
            typeCombo.addItem(ct);
        }
        
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    
    private void populateColumnList(ModifiableTableSchema m, ModifiableColumn select){
        currentColumn = null;
        List<ModifiableColumn> columns = m.getModColumns();
        DefaultListModel model = new DefaultListModel();        
        for(ModifiableColumn col : columns){
            if(!col.isRemoved())
                model.addElement(col);
        }
        columnList.setModel(model);
        if(select != null){
            int index = model.indexOf(select);
            if(index >= 0){
                columnList.setSelectedIndex(index);
            }
        }
        applyButton.setEnabled(true);
        addButton.setEnabled(true);
        removeButton.setEnabled(true);
    }
    
    private void setMandatory(boolean mandatory){
        mandatoryLabel.setVisible(mandatory);
        columnNameField.setEnabled(!mandatory);
        shortNameField.setEnabled(!mandatory);
        descriptionField.setEnabled(!mandatory);
        typeCombo.setEnabled(!mandatory);
        lengthField.setEnabled(!mandatory);
        useAsFilterCheckBox.setEnabled(!mandatory);
    }
    
    private void populateDetails(ModifiableColumn c){
        currentColumn = null;
        columnNameField.setText(c.getColumnName());
        shortNameField.setText(c.getShortName());
        descriptionField.setText(c.getDescription());
        typeCombo.setSelectedItem(c.getType());
        lengthField.setText(String.valueOf(c.getLength()));
        useAsFilterCheckBox.setSelected(c.isFilter());
        setMandatory(c.isMandatory());
        if(!c.isNew()){
            typeCombo.setEnabled(false);
            lengthField.setEnabled(false);
        } 
        currentColumn = c;
    }
    
    private void clearDetails(){
        columnNameField.setText("");
        shortNameField.setText("");
        descriptionField.setText("");
        lengthField.setText("");
        setMandatory(true);
        mandatoryLabel.setVisible(false);
    }

    private void addNewColumn(){
        String tempName = currentSchema.getUntitledString();
        ModifiableColumn c = new ModifiableColumn(tempName, tempName, "", ColumnType.VARCHAR, 250, false, false, true);
        currentSchema.addModColumn(c);
        populateColumnList(currentSchema, c);
        populateDetails(c);
    }
    
    private void removeSelectedColumn(){
        if(currentColumn != null && !currentColumn.isMandatory()){
            currentColumn.setRemoved(true);
            populateColumnList(currentSchema, null);
            clearDetails();
            currentColumn = null;
        }
    }
    
    private void apply(){
        
        //TODO: error checking on updates!!!
        
        for(ModifiableTableSchema m : MedSavantDatabase.getInstance().getModifiableTables()){
            try {
                m.applyChanges();
            } catch (SQLException ex) {
                Logger.getLogger(ManageDB.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NonFatalDatabaseException ex) {
                Logger.getLogger(ManageDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void cancel(){
        for(ModifiableTableSchema m : MedSavantDatabase.getInstance().getModifiableTables()){
            m.removeChanges();
        }
    }
    
    private void close(){
        MedSavantDatabase.getInstance().refreshModifiableTables();
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

        tableCombo = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        columnList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        columnNameField = new javax.swing.JTextField();
        shortNameField = new javax.swing.JTextField();
        descriptionField = new javax.swing.JTextField();
        lengthField = new javax.swing.JTextField();
        mandatoryLabel = new javax.swing.JLabel();
        typeCombo = new javax.swing.JComboBox();
        removeButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        useAsFilterCheckBox = new javax.swing.JCheckBox();
        applyButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Manage Database");

        tableCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableComboActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        jLabel1.setText("Choose column:");

        jScrollPane1.setViewportView(columnList);

        addButton.setText("Add Column");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("DB Column Name:");

        jLabel3.setText("Short Name:");

        jLabel4.setText("Description:");

        jLabel5.setText("Type:");

        jLabel6.setText("Length");

        columnNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                columnNameFieldActionPerformed(evt);
            }
        });

        mandatoryLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mandatoryLabel.setText("This is a mandatory column and cannot be modified. ");

        removeButton.setText("Remove Column");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        jLabel7.setText("Use as Filter:");

        useAsFilterCheckBox.setText(" ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(removeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(useAsFilterCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lengthField, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .addComponent(descriptionField, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .addComponent(shortNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .addComponent(columnNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .addComponent(typeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(mandatoryLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(columnNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(shortNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(typeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(lengthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(useAsFilterCheckBox))
                        .addGap(18, 18, 18)
                        .addComponent(mandatoryLabel))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tableCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(553, Short.MAX_VALUE)
                .addComponent(okButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(applyButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(applyButton)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        addNewColumn();
    }//GEN-LAST:event_addButtonActionPerformed

    private void columnNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_columnNameFieldActionPerformed
    }//GEN-LAST:event_columnNameFieldActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancel();
        close();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void tableComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableComboActionPerformed
        Object o = tableCombo.getSelectedItem();
        if(!o.equals(tableComboDefault)){
            populateColumnList((ModifiableTableSchema)o, null);
            this.currentSchema = (ModifiableTableSchema)o;
        }
    }//GEN-LAST:event_tableComboActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        apply();
        close();
    }//GEN-LAST:event_okButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        apply();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        removeSelectedColumn();
    }//GEN-LAST:event_removeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton applyButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JList columnList;
    private javax.swing.JTextField columnNameField;
    private javax.swing.JTextField descriptionField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField lengthField;
    private javax.swing.JLabel mandatoryLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JTextField shortNameField;
    private javax.swing.JComboBox tableCombo;
    private javax.swing.JComboBox typeCombo;
    private javax.swing.JCheckBox useAsFilterCheckBox;
    // End of variables declaration//GEN-END:variables
}
