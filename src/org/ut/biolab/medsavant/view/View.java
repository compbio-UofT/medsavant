/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class View extends JPanel {

    private PatientFilterDialog patientFilterDialog;
    private FilterController mfc;

    public View() {
        this.setBackground(Color.darkGray);
        init();
    }

    private void init() {
        initFilters();
    }

    private void initFilters() {
        mfc = new FilterController();
        patientFilterDialog = new PatientFilterDialog();
        
        JButton addPatientFilterButton = new JButton("Add patient filter");
        this.add(addPatientFilterButton);
        addPatientFilterButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                patientFilterDialog.reset();
                patientFilterDialog.setVisible(true);
                Filter filter = patientFilterDialog.getFilter();
                if (filter != null) {
                    mfc.addFilter(filter);
                }
            }

        });
    }

}
