/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.view.genetics.GeneticsSection;
import org.ut.biolab.medsavant.view.manage.ManageSection;
import org.ut.biolab.medsavant.view.manage.ProjectWizard;
import org.ut.biolab.medsavant.view.manage.OtherSection;
import org.ut.biolab.medsavant.view.manage.PluginsSection;
import org.ut.biolab.medsavant.view.patients.PatientsSection;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;


/**
 *
 * @author mfiume
 */
public class LoggedInView extends JPanel implements ProjectListener {
    private static final Logger LOG = Logger.getLogger(LoggedInView.class.getName());
    private ViewController viewController;
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
        ViewController.reset();
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

        refreshProjectDropDown();

        viewController.addComponent(projectDropDown);

        addSection(new PatientsSection());
        addSection(new GeneticsSection());

        viewController.addComponent(getSeparator());

        addSection(new PluginsSection());
        addSection(new OtherSection());

        if (LoginController.isAdmin()) {
            addSection(new ManageSection());
        }
    }
    
    
    private void refreshProjectDropDown() {
        try {
            projectDropDown.removeAllItems();

            List<String> projects = null;

            while (projects == null || projects.isEmpty()) {
                projects = ProjectController.getInstance().getProjectNames();

                if (!projects.isEmpty()) {
                    break;
                }

                if (projects.isEmpty() && !LoginController.isAdmin()) {
                    DialogUtils.displayMessage("Welcome to MedSavant. No projects have been started. Please contact your administrator.");
                    LoginController.logout();
                    return;
                }

                if (projects.isEmpty()) {
                    while (true) {
                        int result = DialogUtils.askYesNo("Welcome to MedSavant", "To begin using MedSavant, you will need to create a project.");
                        if (result == DialogUtils.NO) {
                            MainFrame.getInstance().requestClose();
                            // don't break, the user chose not to quit
                        } else {
                            ProjectWizard npd = new ProjectWizard();
                            break;
                        }
                    }
                }
            }

            for (String s : projects) {
                projectDropDown.addItem(s);
            }
            if (projects.isEmpty()) {
                projectDropDown.addItem("No Projects");
                projectDropDown.setEnabled(false);
            } else {
                projectDropDown.setEnabled(true);
                projectDropDown.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        String currentName = ProjectController.getInstance().getCurrentProjectName();
                        if (!ProjectController.getInstance().setProject((String) projectDropDown.getSelectedItem())) {
                            projectDropDown.setSelectedItem(currentName);
                        }
                    }
                });
                ProjectController.getInstance().setProject((String) projectDropDown.getSelectedItem());
            }               
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
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
}
