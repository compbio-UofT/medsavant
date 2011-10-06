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
import java.sql.SQLException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ProjectController.ProjectListener;
import org.ut.biolab.medsavant.view.manage.ManageSection;
import org.ut.biolab.medsavant.view.genetics.GeneticsSection;
import org.ut.biolab.medsavant.view.manage.OtherSection;
import org.ut.biolab.medsavant.view.patients.PatientsSection;
import org.ut.biolab.medsavant.view.subview.SectionView;


/**
 *
 * @author mfiume
 */
public class LoggedInView extends JPanel implements ProjectListener {
    
    private ViewController viewController;
    private static boolean initiated = false;
    private JComboBox projectDropDown;

    public LoggedInView() {
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        initViewContainer();
        initTabs();
        ProjectController.getInstance().addProjectListener(this);
    }

    private void initViewContainer() {
        viewController = ViewController.getInstance();
        this.add(viewController,BorderLayout.CENTER);
    }
    
    private void addSection(SectionView view) {
        viewController.addSection(view);
    }

    private void initTabs() {
        
        viewController.clearMenu();
        
        projectDropDown = new JComboBox();
            
        projectDropDown.setMinimumSize(new Dimension(210,23));
        projectDropDown.setPreferredSize(new Dimension(210,23));
        projectDropDown.setMaximumSize(new Dimension(210,23));
        projectDropDown.setOpaque(false);

        refreshProjectDropDown();

        viewController.addComponent(projectDropDown);

            //viewController.addComponent(getSeparator());
            
            
        //if (!initiated) {
        addSection(new PatientsSection());

        addSection(new GeneticsSection());


        viewController.addComponent(getSeparator());

        addSection(new OtherSection());

        //addSection(new AnnotationsSection());
        if (LoginController.isAdmin()) {
            addSection(new ManageSection());
        }
        //}
        //initiated = true;
    }
    
    
        private void refreshProjectDropDown() {
            try {
                projectDropDown.removeAllItems();
                
                List<String> projects = ProjectController.getInstance().getProjectNames();

                for (String s : projects) {
                    projectDropDown.addItem(s);
                }
                projectDropDown.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        String currentName = ProjectController.getInstance().getCurrentProjectName();
                        if(!ProjectController.getInstance().setProject((String) projectDropDown.getSelectedItem())){
                            projectDropDown.setSelectedItem(currentName);
                        }
                    }
                });
                ProjectController.getInstance().setProject((String) projectDropDown.getSelectedItem());
                
            } catch (SQLException ex) {
            }

        }


    private Component getSeparator() {
        JPanel p = new JPanel();
            Dimension d = new Dimension(200,1);
            p.setPreferredSize(d);
            p.setMaximumSize(d);
            p.setBackground(Color.lightGray);
            return p;
    }
    
    public void projectAdded(String projectName) {
            refreshProjectDropDown();
        }

        public void projectRemoved(String projectName) {
            refreshProjectDropDown();
        }

        public void projectChanged(String projectName) {
        }

        public void projectTableRemoved(int projid, int refid) {
            refreshProjectDropDown();
        }

        public void referenceChanged(String referenceName) {}
}
