/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.app;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;

/**
 *
 * @author mfiume
 */
public class TaskManagerApp implements DashboardApp {

    public TaskManagerApp() {
    }

      private JPanel view;

    @Override
    public JPanel getView() {
        return view;
    }

    private void initView() {
        if (view == null) {
            view = new JPanel();
        }
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
        initView();
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public String getName() {
        return "Task Manager";
    }
    
       @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_TASKMANAGER);
    }
    
}
