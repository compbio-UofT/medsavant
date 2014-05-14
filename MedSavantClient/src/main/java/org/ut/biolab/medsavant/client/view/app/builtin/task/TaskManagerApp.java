/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.app.builtin.task;

import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
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
import org.ut.biolab.medsavant.client.view.util.StandardFixableWidthAppPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.GeneralLog;

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
                    TaskWorker t;
                    
                    t = new ServerLogTaskWorker();
                    results[counter] = new Object[]{t.getTaskName(), t};
                    counter++;

                    // server job log
                    t = new ServerJobMonitorTaskWorker();
                    results[counter++] = new Object[]{t.getTaskName(), t};
                    
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
        private StandardFixableWidthAppPanel container;
        private final JPanel logPanel;
        private final BlockingPanel blockingPanel;
        private final JPanel statusPanel;

        public TaskDetailedView() {
            super("Task Manager");
            refreshTimer = null;

            container = new StandardFixableWidthAppPanel(true, false);
            this.setLayout(new BorderLayout());
            this.add(container, BorderLayout.CENTER);

            statusPanel = container.addBlock();
            logPanel = container.addBlock();

            blockingPanel = new BlockingPanel("No item selected", container);
            this.add(blockingPanel, BorderLayout.CENTER);
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

            if (refreshTimer != null) {
                refreshTimer.stop();
                refreshTimer = null;
            }
            // show a block panel
            if (t == null) {
                blockingPanel.block();
                this.updateUI();
                return;
            }

            blockingPanel.unblock();

            // show task information
            statusPanel.removeAll();
            logPanel.removeAll();

            MigLayout l = new MigLayout("fillx, nogrid, insets 0");
            logPanel.setLayout(l);

            container.setTitle(t.getTaskName());

            if (t.getCurrentStatus() != TaskStatus.PERSISTENT && t.getCurrentStatus() != TaskStatus.PERSISTENT_AUTOREFRESH) {
                statusPanel.add(new JLabel(t.getCurrentStatus().toString()));

                if (t.getCurrentStatus() == TaskStatus.INPROGRESS) {
                    statusPanel.add(ViewUtil.getIndeterminateProgressBar());
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
                statusPanel.add(appButton);
            }

            // add a refresh button
            if (t.getCurrentStatus() == TaskStatus.PERSISTENT || t.getCurrentStatus() == TaskStatus.PERSISTENT_AUTOREFRESH) {
                JButton refreshButton = ViewUtil.getRefreshButton();
                ActionListener al = new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        detailedView.updateView(t);
                    }

                };

                refreshButton.addActionListener(al);
                statusPanel.add(refreshButton);
                if (t.getCurrentStatus() == TaskStatus.PERSISTENT_AUTOREFRESH) {
                    refreshTimer = new Timer(REFRESH_DELAY, al);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            refreshTimer.start();
                        }
                    });
                }
            }

            // add a cancel button
            if (t.getCurrentStatus() == TaskWorker.TaskStatus.INPROGRESS) {

                JButton cancelButton;
                statusPanel.add(cancelButton = new JButton("Cancel"));
                cancelButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        t.cancel();
                    }

                });
            }

            List<GeneralLog> log = t.getLog();
            Object[][] tableData = new Object[log.size()][];
            int counter = 0;
            for (GeneralLog s : log) {
                tableData[counter++] = new Object[]{
                    // a timestamped string
                    (s.getTimestamp() == null ? "" : new Date(s.getTimestamp().getTime()).toLocaleString() + " - ")
                    + s.getDescription()};
            }

            if (tableData.length > 0) {
                StripyTable table = new StripyTable(tableData, new String[]{"Log"});
                table.disableSelection();
                logPanel.add(table, "newline 0, growx 1.0");
                table.setRowSelectionAllowed(false);
            } else {
                logPanel.add(ViewUtil.getGrayItalicizedLabel("No logs"));
            }

            

            this.updateUI();
        }
    }
}
