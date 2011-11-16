package org.ut.biolab.medsavant.view.patients.individual;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.dialog.AddPatientsForm;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedListModel;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
class IndividualDetailEditor extends DetailedListEditor {

    @Override
    public boolean doesImplementAdding() {
        return true;
    }

    @Override
    public boolean doesImplementDeleting() {
        return true;
    }

    public void addItems() {
        new AddPatientsForm();
    }

    public void deleteItems(final List<Object[]> items) {

        int keyIndex = 0;
        int nameIndex = 3;

        int result;

        if (items.size() == 1) {
            String name = (String) items.get(0)[nameIndex];
            result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
                    "Are you sure you want to remove " + name + "?\nThis cannot be undone.",
                    "Confirm", JOptionPane.YES_NO_OPTION);
        } else {
            result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
                    "Are you sure you want to remove these " + items.size() + " individuals?\nThis cannot be undone.",
                    "Confirm", JOptionPane.YES_NO_OPTION);
        }

        if (result == JOptionPane.YES_OPTION) {
            final int[] patients = new int[items.size()];
            int index = 0;
            for (Object[] v : items) {
                int id = (Integer) v[keyIndex];
                System.out.println("Removing individual " + id);
                patients[index++] = id;
            }
                      
            final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
                    "Removing Patient(s)", 
                    patients.length + " patient(s) being removed. Please wait.", 
                    true);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        PatientQueryUtil.removePatient(ProjectController.getInstance().getCurrentProjectId(), patients);
                        dialog.close();  
                        DialogUtils.displayMessage("Successfully removed " + (items.size()) + " individuals(s)");
                    } catch (SQLException ex) {
                        dialog.close();  
                        DialogUtils.displayErrorMessage("Couldn't remove patient(s)", ex);
                    }
                    
                }
            };
            thread.start(); 
            dialog.setVisible(true);
        }
        
        
        /*
        int[] patientIds = new int[selected.size()];
        int i = 0;
        for (Vector v : selected) {
            patientIds[i++] = (Integer) v.get(0);
        }

        if (patientIds != null && patientIds.length > 0) {
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to delete these individuals?\nThis cannot be undone.",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                PatientQueryUtil.removePatient(ProjectController.getInstance().getCurrentProjectId(), patientIds);
            } catch (SQLException ex) {
                Logger.getLogger(IndividualDetailedView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         * 
         */
    }

    @Override
    public void editItems(Object[] results) {
    }
}
