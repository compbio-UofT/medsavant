/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.menu.DualTabbedPane;


/**
 *
 * @author mfiume
 */
public class SessionView extends JPanel {
    private DualTabbedPane pane;


    public SessionView() {
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        initViewContainer();
    }

    private void initViewContainer() {
        
        pane = new DualTabbedPane();
        
        pane.addDualTab(SectionFactory.generatePatientsSection());
        pane.addDualTab(SectionFactory.generateGeneticsSection());
        pane.addDualTab(SectionFactory.generateAnnotateSection());
        
        this.add(pane,BorderLayout.CENTER);
        
    }


    
}
