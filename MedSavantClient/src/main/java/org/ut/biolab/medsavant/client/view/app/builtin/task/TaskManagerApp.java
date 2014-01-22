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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.builtin.task.TaskWorker.TaskStatus;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.component.StripyTable;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
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
public class TaskManagerApp implements DashboardApp, Listener<TaskWorker> {

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
                    Object[][] results = new Object[tasks.size() + 1][];
                    int counter = 0;
                    System.out.println("Enumerating tasks:");
                    for (TaskWorker t : tasks) {
                        System.out.println("\tAdding task t " + t.toString());
                        results[counter++] = new Object[]{t.getTaskName(), t};
                    }
                    TaskWorker t = new ServerLogTaskWorker();
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

        private TaskWorker selectedWorker;

        public TaskDetailedView() {
            super("Task Manager");
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

            if (t.getCurrentStatus() != TaskStatus.PERSISTENT) {
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
            if (t.getCurrentStatus() == TaskStatus.PERSISTENT) {
                JButton refreshButton = new JButton("Refresh");
                refreshButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        detailedView.updateView(t);
                    }

                });
                view.add(refreshButton);
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

            Object[][] tableData = new Object[t.getLog().size()][];
            int counter = 0;
            for (String s : t.getLog()) {
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
