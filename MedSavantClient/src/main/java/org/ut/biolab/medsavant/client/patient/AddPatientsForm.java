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
package org.ut.biolab.medsavant.client.patient;

import java.awt.Color;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 *
 * @author AndrewBrook
 */
public class AddPatientsForm extends JDialog {

    private static final Log LOG = LogFactory.getLog(AddPatientsForm.class);

    /** Creates new form AddPatientsForm */
    public AddPatientsForm() throws RemoteException, SQLException {
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        createTable();

        setLocationRelativeTo(null);
    }

    private void createTable() throws RemoteException, SQLException {
        scrollPane.getViewport().setBackground(Color.white);

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col != 0;
            }
        };
        model.addColumn("Short Name");
        model.addColumn("Value");

        CustomField[] fields = null;
        try {
            fields = MedSavantClient.PatientManager.getPatientFields(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
        }
        for (int i = 1; i < fields.length; i++) { //skip patient id
            model.addRow(new Object[]{ fields[i], ""} );
        }


        table.setModel(model);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                setTip();
            }
        });

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    private void setTip() {
        int index = table.getSelectedRow();
        Object o = table.getValueAt(index, 0);
        if (o != null) {

            CustomField f = (CustomField)o;
            String s = f.getAlias() + " | " + f.getColumnType().toString().toLowerCase();
            switch(f.getColumnType()) {
                case DATE:
                    s += "(yyyy-mm-dd)";
                    break;
                case BOOLEAN:
                    s += "(true/false)";
                    break;
            }
            tipLabel.setText(s);
        }
    }

    private void addPatient() throws SQLException, RemoteException {

        List<String> values = new ArrayList<String>();
        List<CustomField> cols = new ArrayList<CustomField>();
        for (int i = 0; i < table.getRowCount(); i++) {
            String value = (String) table.getModel().getValueAt(i, 1);
            if (value != null && !value.equals("")) {
                cols.add((CustomField) table.getModel().getValueAt(i, 0));
                values.add((String)table.getModel().getValueAt(i, 1));
            }
        }

        // replace empty strings for nulls
        for (int i = 0; i < values.size(); i++) {
            values.set(i, values.get(i).equals("") ? null : values.get(i));
        }
        try {
            MedSavantClient.PatientManager.addPatient(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), cols, values);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
        }
        clearTable();
    }

    private void clearTable() {
        for (int i = 0; i < table.getRowCount(); i++) {
            table.getModel().setValueAt("", i, 1);
        }
    }

    private void close() {
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
        addButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        doneButton = new javax.swing.JButton();
        tipLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Import Patients");

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

        addButton.setText("Add Individual");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Add Single Patient:");

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
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(tipLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(addButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(doneButton)))
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
                    .addComponent(addButton)
                    .addComponent(tipLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(doneButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        close();
    }//GEN-LAST:event_doneButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        try {
            addPatient();
            DialogUtils.displayMessage("Individual Added");
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error adding individual: %s", ex);
        }
    }//GEN-LAST:event_addButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton doneButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
    private javax.swing.JLabel tipLabel;
    // End of variables declaration//GEN-END:variables
}
