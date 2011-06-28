/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.cohorts;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.db.importfile.BedFormat;
import org.ut.biolab.medsavant.db.importfile.ImportFileView;
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
        return new SplitScreenView(
                new CohortListModel(), 
                new CohortDetailedView());
    }
    
    public Component[] getBanner() {
        Component[] result = new Component[1];
        result[0] = getAddCohortButton();
        return result;
    }

    private Component getAddCohortButton() {
        JButton b = new JButton("Add cohort");        
        return b;
    }
}
