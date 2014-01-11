/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app.task;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class TaskPanel extends JPanel implements Listener<TaskWorker> {

    private TaskWorker task;
    private TaskManagerApp tasksparent;
    private JLabel title;
    private ProgressWheel progress;
    private JLabel statusLabel;
    private JToggleButton fullStatusButton;
    private JButton cancelButton;

    public TaskPanel(TaskManagerApp parent, BackgroundTaskWorker task) {
        this.setOpaque(false);

        this.task = task;
        this.task.addListener(this);

        this.tasksparent = parent;

        initView();
    }

    @Override
    public void handleEvent(TaskWorker event) {
        refreshView();
    }

    private void refreshView() {
        this.removeAll();
        
        progress.setVisible(task.getCurrentStatus().equals("Running"));
        
        statusLabel.setText(task.getCurrentStatus());
    }

    private void initView() {
        MigLayout l = new MigLayout("fillx");
        this.setLayout(l);
        
        title = new JLabel(task.getTaskName());
        title.setFont(ViewUtil.getMediumTitleFont());
        
        progress = ViewUtil.getIndeterminateProgressBar();
        
        statusLabel = new JLabel();
        
        fullStatusButton = ViewUtil.getSoftToggleButton("Full Log");
        
        cancelButton = new JButton("Cancel");
        
        this.add(title,"left");
        this.add(progress,"right, wrap");
        this.add(statusLabel,"wrap");
        this.add(fullStatusButton,"left");
        this.add(fullStatusButton,"right, wrap");
        
        this.updateUI();
        
        refreshView();
    }

}
