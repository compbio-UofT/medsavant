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
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
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
import org.ut.biolab.medsavant.view.patients.ListsSection;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;


/**
 *
 * @author mfiume
 */
public class LoggedInView extends JPanel {
    private static final Logger LOG = Logger.getLogger(LoggedInView.class.getName());
    private ViewController viewController;
    //private JComboBox projectDropDown;

    public LoggedInView() {
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        initViewContainer();
        initTabs();
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

        addSection(new ListsSection());
        addSection(new GeneticsSection());

        //viewController.addComponent(getSeparator());

        //addSection(new PluginsSection());
        //addSection(new OtherSection());

        if (LoginController.isAdmin()) {
            addSection(new ManageSection());
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

}
