/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class AggregatePage extends SubSectionView {

    private JPanel panel;
    private AggregatesStatsPanel asp;

    public AggregatePage(SectionView parent) { super(parent); }

    public String getName() {
        return "Aggregate";
    }

    public JPanel getView(boolean update) {
        if (panel == null) {
            try {
                setPanel();
            } catch (NonFatalDatabaseException ex) {
            }
        }
        return panel;
    }

    private void setPanel() throws NonFatalDatabaseException{

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        asp = new AggregatesStatsPanel(getName());
        panel.add(asp, BorderLayout.CENTER);
    }

    public Component[] getBanner() {
//        Component[] cs = new Component[1];
//        JButton addButton = new JButton("Add Region Statistics");
//        addButton.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                rsc.addRegionStats();
//            }
//        });
//        cs[0] = addButton;
//        return cs;
        return null;
    }

    @Override
    public void viewDidLoad() {
        if(asp != null)
            asp.update();
        //if (asp != null)
        //    asp.resumeAggregation();
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
        //if (asp != null)
        //    asp.stopAggregation();
    }


}
