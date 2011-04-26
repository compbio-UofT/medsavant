/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import com.jidesoft.dashboard.Gadget;
import com.jidesoft.dashboard.GadgetManager;
import com.jidesoft.dashboard.SingleDashboardHolder;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import org.ut.biolab.medsavant.view.gadget.GadgetFactory;

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
        initDashboard();
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

    private void initDashboard() {

        this.setLayout(new BorderLayout());

        GadgetManager m = new GadgetManager();
        m.setAllowMultipleGadgetInstances(true);

        SingleDashboardHolder sdh = new SingleDashboardHolder(m);
        sdh.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sdh.setColumnCount(2);

        sdh.setBackground(Color.darkGray);
        sdh.setColumnResizable(true);
        sdh.setRowResizable(true);

        this.add(sdh, BorderLayout.CENTER);

        sdh.showPalette();

        Gadget g;
        g = GadgetFactory.createFilterGadget();
        m.addGadget(g);
        m.showGadget(g);
        g = GadgetFactory.createResultsGadget();
        m.addGadget(g);
        m.showGadget(g);
        g = GadgetFactory.createChartGadget();
        m.addGadget(g);
        m.showGadget(g);

    }
}
