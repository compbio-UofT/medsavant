/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.individual.cohorts;

import java.awt.Component;
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
    
    public String getName() {
        return "Cohorts";
    }

    public JPanel getView() {
        return new SplitScreenView(new CohortListModel(), new CohortDetailedView());
    }
    
    public Component[] getBanner() {
        Component[] result = new Component[1];
        result[0] = new JButton("Add cohort");
        return result;
    }
}
