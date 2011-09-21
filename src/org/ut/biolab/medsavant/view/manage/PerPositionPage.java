/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.patients.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class PerPositionPage extends SubSectionView {

    private SplitScreenView view;

    public PerPositionPage(SectionView parent) { super(parent); }

    
    public String getName() {
        return "Annotations";
    }

    public JPanel getView() {
        view =  new SplitScreenView(
                new PerPositionAnnotationListModel(), 
                new PerPositionDetailedView());
        return view;
    }

    @Override
    public void viewLoading() {
    }

    @Override
    public void viewDidUnload() {
    }
    
}
