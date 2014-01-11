/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app.task;

import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import com.explodingpixels.macwidgets.MacIcons;
import com.explodingpixels.macwidgets.SourceList;
import com.explodingpixels.macwidgets.SourceListCategory;
import com.explodingpixels.macwidgets.SourceListControlBar;
import com.explodingpixels.macwidgets.SourceListItem;
import com.explodingpixels.macwidgets.SourceListModel;
import com.explodingpixels.macwidgets.SourceListSelectionListener;
import com.explodingpixels.widgets.PopupMenuCustomizer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public class TaskManagerApp implements DashboardApp {

    private ArrayList<TaskWorker> tasks;
    private JPanel container;
    private SourceListModel model;
    private SourceListCategory runningCategory;
    private final HashMap<SourceListItem, TaskWorker> itemToTaskMap;
    private JPanel detailView;

    public TaskManagerApp() {
        tasks = new ArrayList<TaskWorker>();
        itemToTaskMap = new HashMap<SourceListItem, TaskWorker>();
        initView();
        
    }

    //private JPanel taskContainer;
    @Override
    public JPanel getView() {
        return container;
    }

    private void initView() {
        if (container == null) {

            model = new SourceListModel();
            runningCategory = new SourceListCategory("Tasks");
            model.addCategory(runningCategory);

            SourceList sourceList = new SourceList(model);
            sourceList.useIAppStyleScrollBars();
            
            detailView = new JPanel();
            
            StandardAppContainer spc = new StandardAppContainer(detailView);
            
            JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    sourceList.getComponent(), spc);
            p.setBorder(BorderFactory.createEmptyBorder());
            p.setDividerSize(0);
            p.setDividerLocation(200);

            SourceListControlBar controlBar = new SourceListControlBar();
            sourceList.installSourceListControlBar(controlBar);
            controlBar.installDraggableWidgetOnSplitPane(p);
            controlBar.createAndAddButton(MacIcons.PLUS, null);
            controlBar.createAndAddButton(MacIcons.MINUS, null);
            
            sourceList.addSourceListSelectionListener(new SourceListSelectionListener() {

                @Override
                public void sourceListItemSelected(SourceListItem sli) {
                    System.out.println("Selected " + sli.getText());
                    showDetailsForTask(itemToTaskMap.get(sli));
                }
                
            });

            container = new JPanel();
            container.setLayout(new BorderLayout());
            container.add(p, BorderLayout.CENTER);
        }
    }

    private void showDetailsForTask(TaskWorker w) {
        detailView.removeAll();
        detailView.setLayout(new MigLayout("wrap 1"));
        for (int i = 0; i < 100; i++) {
            detailView.add(new JLabel("Selected " + w.getTaskName() + " " + w.getCurrentStatus()));
        }
        detailView.updateUI();
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

        SourceListItem item = new SourceListItem(t.getTaskName());
        
        itemToTaskMap.put(item,t);
        
        
        /*if (t.getOwner() != null) {
            if (t.getOwner() instanceof DashboardApp) {
                item.setIcon(new ImageIcon(((DashboardApp) t.getOwner()).getIcon().getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
            }
        }*/
        
        model.addItemToCategory(item, runningCategory);

        int result = DialogUtils.askYesNo("Task Submitted", "View in Task Manager?");

        if (result == DialogUtils.YES) {
            MedSavantFrame.getInstance().getDashboard().launchApp(this);
        }
    }

    public static void main(String[] argx) {
        SourceListModel model = new SourceListModel();
        SourceListCategory runningCategory = new SourceListCategory("Running Tasks");
        model.addCategory(runningCategory);
        model.addItemToCategory(new SourceListItem("sample item"), runningCategory);
        
        SourceList sourceList = new SourceList(model);

        JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                sourceList.getComponent(), new JPanel());
        p.setDividerSize(0);
        p.setDividerLocation(300);

        SourceListControlBar controlBar = new SourceListControlBar();
        sourceList.installSourceListControlBar(controlBar);
        controlBar.installDraggableWidgetOnSplitPane(p);
        controlBar.createAndAddButton(MacIcons.PLUS, null);
        controlBar.createAndAddButton(MacIcons.MINUS, null);
        controlBar.createAndAddPopdownButton(MacIcons.GEAR,
                new PopupMenuCustomizer() {
                    public void customizePopup(JPopupMenu popup) {
                        popup.removeAll();
                        popup.add(new JMenuItem("Item One"));
                        popup.add(new JMenuItem("Item Two"));
                        popup.add(new JMenuItem("Item Three"));
                    }
                });

        JFrame f = new JFrame();

        f.setSize(new Dimension(600, 600));
        f.add(p);
        f.show();

        p.updateUI();

    }
    
    @Override
    public void didLogout() {
        
        tasks.removeAll(tasks);
        
        for (SourceListItem i : itemToTaskMap.keySet()) {
            model.removeItemFromCategory(i, runningCategory);
        }
        
        itemToTaskMap.clear();
        
        detailView.removeAll();
        
    }

    @Override
    public void didLogin() {
    }
}
