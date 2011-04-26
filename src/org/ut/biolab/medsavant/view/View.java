/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import org.ut.biolab.medsavant.view.gadget.MedSavantGadgetFactory;
import com.jidesoft.dashboard.Dashboard;
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
import fiume.table.SearchableTablePanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import org.ut.biolab.medsavant.util.Util;
import java.util.Vector;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.VariantRecordModel;
import org.ut.biolab.medsavant.view.gadget.chart.ChartGadget;
import org.ut.biolab.medsavant.view.gadget.table.TableGadget;

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
        //initFilters();
        //initTable();
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

    private void initTable() {
        Vector records = Util.getVariantRecordsVector(ResultController.getVariantRecords());
        this.add(new SearchableTablePanel(records, VariantRecordModel.getFieldNames(), VariantRecordModel.getFieldClasses()));
    }

    private void initDashboard() {

        this.setLayout(new BorderLayout());

        GadgetManager m = new GadgetManager();
        m.setAllowMultipleGadgetInstances(true);

        SingleDashboardHolder sdh = new SingleDashboardHolder(m);
        sdh.setColumnCount(2);

        sdh.setBackground(Color.white);
        sdh.setColumnResizable(true);
        sdh.setRowResizable(true);

        this.add(sdh, BorderLayout.CENTER);

        sdh.showPalette();

        Gadget g;
        g = new TableGadget();
        m.addGadget(g);
        m.showGadget(g);
        g = new ChartGadget();
        m.addGadget(g);
        m.showGadget(g);
        //g = new RedGadget();
        //m.addGadget(g);
        //m.showGadget(g);

    }
}
