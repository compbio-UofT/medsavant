/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolTip;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.AccountManagerApp;
import org.ut.biolab.medsavant.client.view.app.AccountManagerApp;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.app.builtin.task.TaskManagerApp;
import org.ut.biolab.medsavant.client.view.dashboard.Dashboard;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dialog.ChangePasswordDialog;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class MenuFactory {

    public static JPopupMenu generateMenu() {

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
        if (MedSavantFrame.getInstance().getDashboard().getCurrentApp() != AppDirectory.getTaskManager()) {
            m.add(getTasksMenu());
        }
        if (MedSavantFrame.getInstance().getDashboard().getCurrentApp() != AppDirectory.getAccountManager()) {
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
        ImageIcon homeIcon = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.DASHBOARD);

        Image image = homeIcon.getImage().getScaledInstance(23, 23, Image.SCALE_SMOOTH);
        appLauncher.setIcon(new ImageIcon(image));

        return appLauncher;

    }

    private static JMenuItem getAccountMenu() {
        return getMenuItemAppLauncherForApp(AppDirectory.getAccountManager());
    }

    private static JMenuItem getTasksMenu() {
        return getMenuItemAppLauncherForApp(AppDirectory.getTaskManager());
    }

    private static JMenuItem getLogoutItem() {

        JMenuItem logoutItem = new JMenuItem("Sign Out");
        logoutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                MedSavantFrame.getInstance().requestLogout();
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

        if (app instanceof LaunchableApp) {
            ImageIcon appicon = ((LaunchableApp) app).getIcon();
            Image image = appicon.getImage().getScaledInstance(23, 23, Image.SCALE_SMOOTH);
            appLauncher.setIcon(new ImageIcon(image));
        }

        return appLauncher;
    }

    public static JPopupMenu generatePrettyMenu() {

        JPopupMenu m = new JPopupMenu();
        m.setBorder(BorderFactory.createEmptyBorder());

        int maxWidth = (int) MedSavantFrame.getInstance().getSize().getWidth(); //390; // 5 apps without scrollbar

        JPanel container = new JPanel();
        container.setBackground(Color.white);

        JPanel p = new JPanel();
        p.setBackground(Color.white);

        //MigLayout l = new MigLayout(String.format("wrap %d, gapx 25, gapy 25, insets 25",numRecents));
        MigLayout l = new MigLayout("gapx 25, gapy 25, insets 25 25 25 25");
        p.setLayout(l);

        int iconWidth = 64;

        
        int count = 0;
        
        /*if (MedSavantFrame.getInstance().getDashboard().getCurrentApp() != null) {
            count++;
            ActionListener goHome = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MedSavantFrame.getInstance().getDashboard().goHome();
                }

            };
            ImageIcon homeIcon = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.DASHBOARD);

            JPanel appPanel = Dashboard.getRepresentationForLauncher("Dashboard", homeIcon, iconWidth, closeMenuThenPerformAction(m, goHome));
            p.add(appPanel);
        }
                */
        
        for (final LaunchableApp app : MedSavantFrame.getInstance().getDashboard().getLaunchHistory()) {

            ActionListener launchApp = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MedSavantFrame.getInstance().getDashboard().launchApp(app);
                }

            };

            boolean selected = (app == MedSavantFrame.getInstance().getDashboard().getCurrentApp());
            if (selected) {
                launchApp = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    }
                };
            }

            JPanel appPanel = Dashboard.getRepresentationForLauncher(app.getName(), app.getIcon(), iconWidth, closeMenuThenPerformAction(m, launchApp), selected);
            p.add(appPanel);

            count++;
        }

        int width
                = 25 // inset left
                + 25 // inset right
                + count * iconWidth // icon widths
                + 25 * (count - 1);      // gaps

        width = Math.min(width, maxWidth);

        MigLayout containerLayout = new MigLayout(String.format("width %d, insets 0, wrap, gapy 0, fillx", width));
        container.setLayout(containerLayout);

        JScrollPane scroll;

        //container.add(ViewUtil.getEmphasizedLabel("Recent Apps".toUpperCase()),"width 100%, wrap, center, gapy 0");
        container.add(scroll = ViewUtil.getClearBorderlessScrollPane(p));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        m.add(container);

        return m;
    }

    private static ActionListener closeMenuThenPerformAction(final JPopupMenu menu, final ActionListener l) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                menu.setVisible(false);
                l.actionPerformed(e);
            }
        };
    }

}
