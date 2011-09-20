/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.view.manage.ManageSection;
import org.ut.biolab.medsavant.view.annotations.AnnotationsSection;
import org.ut.biolab.medsavant.view.genetics.GeneticsSection;
import org.ut.biolab.medsavant.view.patients.PatientsSection;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class LoggedInView extends JPanel {
    
    private ViewController viewController;
    private static boolean initiated = false;

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
        if (!initiated) {
            addSection(new PatientsSection());
            addSection(new GeneticsSection());
            
            JPanel p = new JPanel();
            Dimension d = new Dimension(200,1);
            p.setPreferredSize(d);
            p.setMaximumSize(d);
            p.setBackground(Color.lightGray);
            viewController.addComponent(p);
            
            addSection(new AnnotationsSection());
            addSection(new ManageSection());
        }
        initiated = true;
    }
}
