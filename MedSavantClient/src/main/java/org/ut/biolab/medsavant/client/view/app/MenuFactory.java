/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.AccountManagerApp;
import org.ut.biolab.medsavant.client.view.app.AccountManagerApp;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.app.task.TaskManagerApp;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dialog.ChangePasswordDialog;
import org.ut.biolab.medsavant.client.view.images.IconFactory;

/**
 *
 * @author mfiume
 */
public class MenuFactory {

    private static TaskManagerApp taskManager;
    private static AccountManagerApp accountManager;

    public static JPopupMenu generateMenu() {
        
        if (accountManager == null) {
            accountManager = new AccountManagerApp();
            MedSavantFrame.getInstance().getDashboard().blackListAppFromHistory(accountManager);
        }
        
        if (taskManager == null) {
            taskManager = new TaskManagerApp();
            MedSavantFrame.getInstance().getDashboard().blackListAppFromHistory(taskManager);
            AppDirectory.registerTaskManager(taskManager);
        }
        
        JPopupMenu m = new JPopupMenu();
        
        m.add(getRecentAppsMenu());

        for (final LaunchableApp app : MedSavantFrame.getInstance().getDashboard().getLaunchHistory()) {
            if (app == MedSavantFrame.getInstance().getDashboard().getCurrentApp()) {
                continue;
            }

            JMenuItem appLauncher = getMenuItemAppLauncherForApp(app);
            m.add(appLauncher);
        }

        m.add(new JSeparator());
        if (MedSavantFrame.getInstance().getDashboard().getCurrentApp() != null) {
            m.add(getDashboardMenuItem());
        }
        if (MedSavantFrame.getInstance().getDashboard().getCurrentApp() != taskManager) {
            m.add(getTasksMenu());
        }
        if (MedSavantFrame.getInstance().getDashboard().getCurrentApp() != accountManager) {
            m.add(getAccountMenu());
        }
        m.add(new JSeparator());
        //m.add(getChangePasswordItem());
        m.add(getLogoutItem());

        return m;
    }

    private static JMenuItem getRecentAppsMenu() {
        JMenuItem m = new JMenuItem("Recent Apps");

        m.setEnabled(false);
        m.setFocusable(false);
        m.setFocusPainted(false);

        return m;
    }

    private static JMenuItem getDashboardMenuItem() {

        JMenuItem appLauncher = new JMenuItem("Dashboard");
        appLauncher.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MedSavantFrame.getInstance().getDashboard().goHome();
            }

        });
        ImageIcon homeIcon = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.HOME);

        Image image = homeIcon.getImage().getScaledInstance(23, 23, Image.SCALE_SMOOTH);
        appLauncher.setIcon(new ImageIcon(image));

        return appLauncher;

    }

    private static JMenuItem getAccountMenu() {
        
        return getMenuItemAppLauncherForApp(accountManager);
    }

    private static JMenuItem getTasksMenu() {
        
        return getMenuItemAppLauncherForApp(taskManager);
    }

    private static JMenuItem getLogoutItem() {

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                MedSavantFrame.getInstance().requestClose();
            }
        });

        return logoutItem;
    }

    private static JMenuItem getChangePasswordItem() {

        JMenuItem changePassItem = new JMenuItem("Change Password");
        changePassItem.addActionListener(new ActionListener() {
            private final String OLDPASS_LABEL = "Enter Current Password";
            private final String NEWPASS_LABEL1 = "Enter New Password";
            private final String NEWPASS_LABEL2 = "Confirm New Password";

            @Override
            public void actionPerformed(ActionEvent ae) {
                JDialog jd = new ChangePasswordDialog();
                jd.setVisible(true);
            }

        });

        return changePassItem;
    }

    private static JMenuItem getMenuItemAppLauncherForApp(final LaunchableApp app) {
        JMenuItem appLauncher = new JMenuItem(app.getName());
        appLauncher.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MedSavantFrame.getInstance().getDashboard().launchApp(app);
            }

        });

        if (app instanceof DashboardApp) {
            System.out.println(app.getName() + " is instance of Dashboard app");
            ImageIcon appicon = ((DashboardApp) app).getIcon();
            Image image = appicon.getImage().getScaledInstance(23, 23, Image.SCALE_SMOOTH);
            appLauncher.setIcon(new ImageIcon(image));
        }

        return appLauncher;
    }

}
