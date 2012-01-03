package org.ut.biolab.medsavant.view.manage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class IntervalPage extends SubSectionView {

    int importID = 0;
    SplitScreenView view;
    
    public IntervalPage(SectionView parent) { 
        super(parent);
    }
    
    public String getName() {
        return "Region Lists";
    }

    public JPanel getView(boolean update) {
        view = new SplitScreenView(
                new IntervalListModel(), 
                new IntervalDetailedView(),
                new IntervalDetailedListEditor());
        
        return view;
    }
    
    public Component[] getBanner() {
        Component[] result = new Component[0];
        //result[0] = getAddCohortButton();
        return result;
    }
    
    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }
    
}
