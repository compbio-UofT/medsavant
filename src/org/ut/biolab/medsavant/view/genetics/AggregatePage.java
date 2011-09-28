/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;
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
        return "  Aggregate";
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
        
        asp = new AggregatesStatsPanel();
        panel.add(asp, BorderLayout.CENTER);
    }
    
    public Component[] getBanner() {
//        Component[] cs = new Component[1];
//        JButton addButton = new JButton("Add Region Statistics");
//        addButton.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
////                System.out.println("Pressed button");
//                rsc.addRegionStats();
//            }
//        });
//        cs[0] = addButton;
//        return cs;
        return null;
    }  
    
    @Override
    public void viewLoading() {
        if (asp != null)
            asp.resumeAggregation();
    }

    @Override
    public void viewDidUnload() {
        if (asp != null)
            asp.stopAggregation();
    }
    

}
