/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view;

import org.ut.biolab.medsavant.client.view.notify.NotificationsPanel;
import org.ut.biolab.medsavant.client.view.dashboard.Dashboard;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.explodingpixels.macwidgets.MacUtils;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;

import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.view.notify.Notification;
import org.ut.biolab.medsavant.client.view.app.DashboardSectionFactory;
import org.ut.biolab.medsavant.client.view.component.StackableJPanelContainer;
import org.ut.biolab.medsavant.client.view.dashboard.AppSwitchPanel;

/**
 *
 * @author mfiume
 */
public class MedSavantFrame extends JFrame {

    private static Log LOG = LogFactory.getLog(MedSavantFrame.class);
    private static MedSavantFrame instance;
    private StackableJPanelContainer view;
    private Dashboard sessionDashboard;
    private static Map<String, Runnable> debugFunctions = new HashMap<String, Runnable>();

    //public static final String FEEDBACK_URI = "mailto:feedback@genomesavant.com?subject=MedSavant%20Feedback";
    //public static final String USERGUIDE_URI = "http://genomesavant.com/p/medsavant/learn";   

    //Adds a new function under the 'Debug' menu. The debug menu is not shown if
    //it is empty
    public static void addDebugFunction(String name, Runnable r) {
        System.out.println("Adding " + name + " to debug menu");
        debugFunctions.put(name, r);
    }

    public static JMenu getDebugMenu() {
        if (debugFunctions.size() < 1) {
            return null;
        }
        JMenu menu = new JMenu("Debug");
        for (final Map.Entry<String, Runnable> e : debugFunctions.entrySet()) {
            JMenuItem debugItem = new JMenuItem(e.getKey());
            debugItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    e.getValue().run();
                }
            });
            menu.add(debugItem);
        }
        return menu;
    }

    public static MedSavantFrame getInstance() {
        if (instance == null) {
            instance = new MedSavantFrame();
        }
        return instance;
    }
    private NotificationsPanel notificationPanel;

    private MedSavantFrame() {
        super("");

        MacUtils.makeWindowLeopardStyle(this.getRootPane());
        UIManager.put("Panel.background", new Color(237, 237, 237)); // the above line makes the bg dark, setting back

        setIconImage(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.MENU_USER).getImage());

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(550, 550));

        view = new StackableJPanelContainer();
        view.setDoubleBuffered(true);
        view.setBackground(new Color(217, 222, 229));

        UIManager.put("ToolTip.background", Color.black);
        UIManager.put("ToolTip.foreground", Color.white);
        UIManager.put("ToolTip.border", ViewUtil.getMediumBorder());
        UIManager.put("ToolTip.font", ViewUtil.detailFontBold);
        UIManager.put("Table.gridColor", new Color(250, 250, 250));

        add(view, BorderLayout.CENTER);

        LOG.info("Loading apps...");
        AppController pc = AppController.getInstance();
        pc.loadPlugins(DirectorySettings.getPluginsDirectory());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                requestClose();
            }
        });
    }

    public void showNotification(Notification n) {
        notificationPanel.addNotification(n);
    }

    public void showNotficationMessage(String notificationMsg) {
        Notification n = new Notification();
        n.setName(notificationMsg);
        notificationPanel.addNotification(n);
    }

    public void initializeSessionView() {

        final JPanel dashBoardContainer = ViewUtil.getClearPanel();
        view.push(dashBoardContainer);

        notificationPanel = new NotificationsPanel();
        view.push(notificationPanel);

        AppSwitchPanel switchPanel = new AppSwitchPanel(dashBoardContainer);
        view.push(switchPanel);

        new MedSavantWorker<Void>("MedSavantFrame") {
            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(Void result) {
            }

            @Override
            protected Void doInBackground() throws Exception {

                System.out.println("Creating dashboard");
                Dashboard dash = new Dashboard();
                dash.addDashboardSection(DashboardSectionFactory.getUberSection());
                sessionDashboard = dash;
                
                dashBoardContainer.setLayout(new BorderLayout());
                dashBoardContainer.add(dash, BorderLayout.CENTER);
                dashBoardContainer.updateUI();

                return null;
            }
        }.execute();

    }

    public Dashboard getDashboard() {
        return sessionDashboard;
    }

    public void forceRestart() {
        requestRestart(false);
    }

    public void requestLogoutAndRestart() {
        requestRestart(true);
    }

    private void requestRestart(boolean askFirst) {
        LOG.info("Asking to restart");
        final LoginController controller = LoginController.getInstance();

        if (controller.isLoggedIn()) {

            boolean doAction;

            if (askFirst) {
                doAction = DialogUtils.askYesNo("Sign Out", "Are you sure you want to sign out?") == DialogUtils.YES;
            } else {
                doAction = true;
            }

            if (doAction) {
                controller.logout();
                MedSavantClient.restart();
                return;
            }
        }
        LOG.info("Refusing to restart");
    }

    public void forceClose() {
        requestClose(false);
    }

    public void requestClose() {
        requestClose(true);
    }

    private void requestClose(boolean askFirst) {
        LOG.info("Asking to quit");
        final LoginController controller = LoginController.getInstance();

        //String jobsMsg = "";
        //if(ThreadController.getInstance().areJobsRunning()){
        //    jobsMsg = "Jobs are running.  If you quit, job progress will be lost. ";
        //}
        boolean doAction;

        if (askFirst) {
            doAction = !controller.isLoggedIn() || DialogUtils.askYesNo("Quit MedSavant", "Are you sure you want to quit?") == DialogUtils.YES;
        } else {
            doAction = true;
        }

        if (doAction) {
            controller.logout();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
            LOG.info("System exiting");
            System.exit(0);
            return;
        }

        LOG.info("Refusing to quit");
    }

    public NotificationsPanel getNotificationPanel() {
        return this.notificationPanel;
    }
}
