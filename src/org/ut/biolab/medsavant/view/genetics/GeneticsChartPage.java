/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import org.ut.biolab.medsavant.view.genetics.charts.ChartView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.record.Chromosome;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.util.view.PeekingPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class GeneticsChartPage extends SubSectionView {

    private JPanel panel;
    //private ChartContainer cc;
    private ChartView cc;
    
    public GeneticsChartPage(SectionView parent) { super(parent); }

    public String getName() {
        return "  Chart";
    }

    public JPanel getView() {
        if (panel == null) {
            try {
                setPanel();
            } catch (NonFatalDatabaseException ex) {
            }
        }
        return panel;
    }

    private void setPanel() throws NonFatalDatabaseException {

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        //PeekingPanel detailView = new PeekingPanel("Filters", BorderLayout.EAST, new FilterPanel(), true,400);
        //panel.add(detailView, BorderLayout.WEST);

        cc = new ChartView();
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
    public void viewLoading() {
    }

    @Override
    public void viewDidUnload() {
    }
    
}
