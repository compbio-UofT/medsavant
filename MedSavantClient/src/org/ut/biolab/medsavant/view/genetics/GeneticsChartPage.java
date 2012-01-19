/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.sql.SQLException;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.view.genetics.charts.ChartView;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class GeneticsChartPage extends SubSectionView implements FiltersChangedListener, ReferenceListener {

    private JPanel panel;
    //private ChartContainer cc;
    private ChartView cc;
    private boolean isLoaded = false;

    public GeneticsChartPage(SectionView parent) { 
        super(parent); 
        FilterController.addFilterListener(this);
        ReferenceController.getInstance().addReferenceListener(this);
    }

    public String getName() {
        return "Chart";
    }

    public JPanel getView(boolean update) {
        if (panel == null || update) {
            try {
                setPanel();
            } catch (NonFatalDatabaseException ex) {
            }
        }
        cc.updateIfRequired();
        return panel;
    }

    private void setPanel() throws NonFatalDatabaseException {

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        //PeekingPanel detailView = new PeekingPanel("Filters", BorderLayout.EAST, new FilterPanel(), true,400);
        //panel.add(detailView, BorderLayout.WEST);

        cc = new ChartView(getName());
        panel.add(cc, BorderLayout.CENTER);
    }

    public Component[] getBanner() {
        /*
        Component[] cs = new Component[1];
        JButton addButton = new JButton("Add chart");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cc.addChart();
                cc.updateUI();
            }
        });
        cs[0] = addButton;
        return cs;
         *
         */
        return null;
    }

    @Override
    public void viewDidLoad() {
        isLoaded = true;
    }

    @Override
    public void viewDidUnload() {
        isLoaded = false;
        ThreadController.getInstance().cancelWorkers(getName());
    }

    @Override
    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        tryUpdate();
    }

    @Override
    public void referenceAdded(String name) {}

    @Override
    public void referenceRemoved(String name) {}

    @Override
    public void referenceChanged(String prnameojectName) {
        tryUpdate();
    }
    
    private void tryUpdate(){
        if(cc != null){
            cc.setUpdateRequired(true);
            if(isLoaded){
                cc.updateIfRequired();
            }
        }
    }

}
