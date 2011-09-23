/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ReferenceController.ReferenceListener;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.manage.NewReferenceDialog;
import org.ut.biolab.medsavant.view.patients.DetailedListModel;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.patients.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class PatientsPage extends SubSectionView {


    private JPanel panel;

    public PatientsPage(SectionView parent) {
        super(parent);
    }

    public String getName() {
        return "Patients";
    }

    public JPanel getView() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new PatientsManagementPanel(),BorderLayout.CENTER);
        return panel;
    }

    @Override
    public Component[] getBanner() {
        return null;
    }

    @Override
    public void viewLoading() {
    }

    @Override
    public void viewDidUnload() {
    }
}
