/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app.builtin.task;

import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.builtin.task.TaskWorker.TaskStatus;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.component.StripyTable;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.list.DetailedListModel;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class TaskManagerApp implements LaunchableApp, Listener<TaskWorker> {

    private ArrayList<TaskWorker> tasks;
    private SplitScreenView container;
    private TaskDetailedView detailedView;
    private boolean isShowing;

    public TaskManagerApp() {
        tasks = new ArrayList<TaskWorker>();
        initView();
    }

    @Override
    public JPanel getView() {
        return container;
    }

    private void initView() {
        if (container == null) {

            detailedView = new TaskDetailedView();

            container = new SplitScreenView(new DetailedListModel() {

                @Override
                public Object[][] getList(int limit) throws Exception {
                    Object[][] results = new Object[tasks.size() + 2][]; // 2 default entries
                    int counter = 0;
                    for (TaskWorker t : tasks) {
                        results[counter++] = new Object[]{t.getTaskName(), t};
                    }
                    
                    // server general log
                    TaskWorker t = new ServerLogTaskWorker();
                    results[counter] = new Object[]{t.getTaskName(), t};
                    counter++;
                    
                    // server job log
                    t = new ServerJobMonitorTaskWorker();
                    results[counter] = new Object[]{t.getTaskName(), t};                    
                    return results;
                }

                @Override
                public String[] getColumnNames() {
                    return new String[]{"Task name", "Task"};
                }

                @Override
                public Class[] getColumnClasses() {
                    return new Class[]{String.class, TaskWorker.class};
                }

                @Override
                public int[] getHiddenColumns() {
                    return new int[0];
                }

            }, detailedView);
        }
    }

    public void showMessageForTask(TaskWorker t, final String message) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                DialogUtils.displayMessage("Task Manager", message);
            }

        });
    }
    
     public void showErrorForTask(final TaskWorker t, final Exception e) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                DialogUtils.displayException("Task Manager", "Error running task " + t.getTaskName(), e);
            }

        });
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
    }

    @Override
    public void viewDidUnload() {
        isShowing = false;
    }

    @Override
    public void viewDidLoad() {
        isShowing = true;
    }

    @Override
    public String getName() {
        return "Task Manager";
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_TASKMANAGER);
    }

    public void submitTask(TaskWorker t) {
        tasks.add(t);

        t.addListener(this);

        container.refresh();

        int result = DialogUtils.askYesNo("Task Submitted", "View in Task Manager?");

        if (result == DialogUtils.YES) {
            MedSavantFrame.getInstance().getDashboard().launchApp(this);
        }
    }

    @Override
    public void didLogout() {

        tasks.removeAll(tasks);
        container.refresh();

    }

    @Override
    public void didLogin() {
    }

    @Override
    public void handleEvent(TaskWorker t) {
        if (detailedView.getSelectedWorker() == t && isShowing) {
            detailedView.updateView(t);
        }
    }
    
    class TaskDetailedView extends DetailedView {
        private static final int REFRESH_DELAY = 2000;
        private TaskWorker selectedWorker;
        private Timer refreshTimer;        
        public TaskDetailedView() {
            super("Task Manager");
            refreshTimer = null;
        }

        @Override
        public void setSelectedItem(Object[] selectedRow) {

            if (selectedRow.length == 0) {
                updateView(null);
                return;
            }

            TaskWorker t = (TaskWorker) selectedRow[1];
            selectedWorker = t;
            updateView(t);
        }

        public TaskWorker getSelectedWorker() {
            return this.selectedWorker;
        }

        @Override
        public void setMultipleSelections(List<Object[]> selectedRows) {
        }

        @Override
        public JPopupMenu createPopup() {
            return null;
        }

        private void updateView(final TaskWorker t) {

            if(refreshTimer != null){
                refreshTimer.stop();
                refreshTimer = null;
            }
            // show a block panel
            if (t == null) {
                this.removeAll();
                BlockingPanel p;
                this.add(p = new BlockingPanel("No item selected", new JPanel()));
                p.block();
                this.updateUI();
                return;
            }

            // show task information
            this.removeAll();

            this.setLayout(new BorderLayout());

            JPanel view = new JPanel();
            view.setBackground(Color.white);

            MigLayout l = new MigLayout("fillx, nogrid, insets 0");
            view.setLayout(l);

            JLabel taskTitle = ViewUtil.getLargeGrayLabel(t.getTaskName());
            view.add(taskTitle, "wrap");

            if (t.getCurrentStatus() != TaskStatus.PERSISTENT && t.getCurrentStatus() != TaskStatus.PERSISTENT_AUTOREFRESH) {
                view.add(new JLabel(t.getCurrentStatus().toString()));

                if (t.getCurrentStatus() == TaskStatus.INPROGRESS) {
                    view.add(ViewUtil.getIndeterminateProgressBar());
                }
            }

            // add a button that launches the responsible app when pressed
            if (t.getOwner() != null) {
                JButton appButton = new JButton(String.format("Open %s App", t.getOwner().getName()));
                appButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        MedSavantFrame.getInstance().getDashboard().launchApp(t.getOwner());
                    }

                });
                view.add(appButton);
            }

            // add a refresh button
            if (t.getCurrentStatus() == TaskStatus.PERSISTENT || t.getCurrentStatus() == TaskStatus.PERSISTENT_AUTOREFRESH) {
                JButton refreshButton = new JButton("Refresh");
                ActionListener al = new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        detailedView.updateView(t);
                    }

                };
                
                refreshButton.addActionListener(al);
                view.add(refreshButton);
                if(t.getCurrentStatus() == TaskStatus.PERSISTENT_AUTOREFRESH){
                    refreshTimer = new Timer(REFRESH_DELAY, al);
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run(){
                            refreshTimer.start();
                        }
                    });
                }
            }

            // add a cancel button
            if (t.getCurrentStatus() == TaskWorker.TaskStatus.INPROGRESS) {

                JButton cancelButton;
                view.add(cancelButton = new JButton("Cancel"));
                cancelButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        t.cancel();
                    }

                });
            }

            List<String> log = t.getLog();
            Object[][] tableData = new Object[log.size()][];
            int counter = 0;
            for (String s : log) {
                tableData[counter++] = new Object[]{s};
            }

            StripyTable table = new StripyTable(tableData, new String[]{"Log"});
            table.disableSelection();
            view.add(table.getTableHeader(), "newline, growx");
            view.add(ViewUtil.getClearBorderlessScrollPane(table), "newline 0, growx 1.0, height 100%");

            table.setRowSelectionAllowed(false);

            JPanel container = new StandardAppContainer(view);
            this.add(container, BorderLayout.CENTER);

            this.updateUI();
        }
    }
}
