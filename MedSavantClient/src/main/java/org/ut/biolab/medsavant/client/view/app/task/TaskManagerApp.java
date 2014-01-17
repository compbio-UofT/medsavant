/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app.task;

import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.list.DetailedListModel;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public class TaskManagerApp implements DashboardApp {

    private ArrayList<TaskWorker> tasks;
    private SplitScreenView container;

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

            container = new SplitScreenView(new DetailedListModel() {

                @Override
                public Object[][] getList(int limit) throws Exception {
                    Object[][] results = new Object[tasks.size()][];
                    int counter = 0;
                    for (TaskWorker t : tasks) {
                        results[counter++] = new Object[]{t.getTaskName(), t};
                    }
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

            }, new DetailedView("Task name") {

                @Override
                public void setSelectedItem(Object[] selectedRow) {

                    if (selectedRow.length == 0) {
                        updateView(null);
                        return;
                    }

                    TaskWorker t = (TaskWorker) selectedRow[1];
                    updateView(t);
                }

                @Override
                public void setMultipleSelections(List<Object[]> selectedRows) {
                }

                @Override
                public JPopupMenu createPopup() {
                    return null;
                }

                private void updateView(TaskWorker t) {

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

                    MigLayout l = new MigLayout();
                    view.setLayout(l);

                    view.add(new JLabel(t.getTaskName()), "wrap");

                    JPanel container = new StandardAppContainer(view);
                    this.add(container, BorderLayout.CENTER);

                    this.updateUI();
                }
            });
        }
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
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

    public void submitTask(TaskWorker t) {
        tasks.add(t);

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
}
