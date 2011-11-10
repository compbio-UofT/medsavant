/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.cohorts;

import org.ut.biolab.medsavant.view.dialog.CohortWizard;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.patients.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class CohortsPage extends SubSectionView {

    public CohortsPage(SectionView parent) { super(parent); }
    private SplitScreenView view;
    
    public String getName() {
        return "Cohorts";
    }

    public JPanel getView(boolean update) {
        view =  new SplitScreenView(
                new CohortListModel(), 
                new CohortDetailedView());
        return view;
    }
    
    public Component[] getBanner() {
        Component[] result = new Component[1];
        result[0] = getAddCohortButton();
        return result;
    }
    
    private JButton getAddCohortButton(){
        JButton button = new JButton("Add cohort");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CohortWizard();
                if(view != null) view.refresh();
            }
        }); 
        return button;
    }
    
    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }
    
}
