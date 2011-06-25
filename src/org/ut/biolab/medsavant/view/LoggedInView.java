/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.annotations.AnnotationsSection;
import org.ut.biolab.medsavant.view.genetics.GeneticsSection;
import org.ut.biolab.medsavant.view.patients.PatientsSection;
import org.ut.biolab.medsavant.view.subview.SectionView;


/**
 *
 * @author mfiume
 */
public class LoggedInView extends JPanel {
    
    private ViewController viewController;

    public LoggedInView() {
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        initViewContainer();
        initTabs();
    }

    private void initViewContainer() {
        viewController = ViewController.getInstance();
        this.add(viewController,BorderLayout.CENTER);
    }
    
    private void addSection(SectionView view) {
        viewController.addSection(view);
    }

    private void initTabs() {
        addSection(new PatientsSection());
        addSection(new GeneticsSection());
        addSection(new AnnotationsSection());
    }
    
}
