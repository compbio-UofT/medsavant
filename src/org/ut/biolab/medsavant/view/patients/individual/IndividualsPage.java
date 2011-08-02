/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.individual;

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
public class IndividualsPage extends SubSectionView {

    public IndividualsPage(SectionView parent) { super(parent); }
    
    public String getName() {
        return "Individuals";
    }

    public JPanel getView() { 
        //return new SplitScreenView(new CohortListModel(), new CohortDetailedView());
        return new SplitScreenView(
                new IndividualListModel(), 
                new IndividualDetailedView());
    }
    
    public Component[] getBanner() {
        Component[] result = new Component[1];
        result[0] = new JButton("Import Patient(s)");
        return result;
    }
}
