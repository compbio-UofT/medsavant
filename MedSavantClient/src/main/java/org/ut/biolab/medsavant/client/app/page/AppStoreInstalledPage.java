/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.app.page;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.app.AppInfo;
import org.ut.biolab.medsavant.client.app.AppInstallUtils;
import org.ut.biolab.medsavant.client.app.AppStorePage;
import org.ut.biolab.medsavant.client.view.component.LazyPanel;
import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.app.api.AppInstaller;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.notify.NotificationsPanel.Notification;

/**
 *
 * @author mfiume
 */
public class AppStoreInstalledPage implements AppStorePage {

    AppInstaller installer;
    private final HashSet<AppInfo> installQueue;
    private final LazyPanel view;
    private final HashSet<AppInfo> recentlyInstalled;
    private final HashSet<AppInfo> recentlyUninstalled;

    public AppStoreInstalledPage(AppInstaller installer) {
        this.installer = installer;
        this.installQueue = new HashSet<AppInfo>();
        this.view = new LazyPanel() {

            @Override
            public void viewDidLoad() {
                updateInstalledList();
            }

            @Override
            public void viewDidUnload() {
            }
            
        };

        view.setOpaque(false);

        recentlyInstalled = new HashSet<AppInfo>();
        recentlyUninstalled = new HashSet<AppInfo>();
    }

    @Override
    public String getName() {
        return "Installed";
    }

    @Override
    public LazyPanel getView() {
        return view;
    }

    protected void updateInstalledList() {
        
        System.out.println("Updating download list " + this.installQueue.size() + " installs in queue");

        view.removeAll();
        view.setLayout(new BorderLayout());
        
        JPanel container = ViewUtil.getClearPanel();
        
        MigLayout ml = new MigLayout("wrap 1, gapy 5, hidemode 3");
        container.setLayout(ml);

        if (!installQueue.isEmpty()) {
            JLabel queuedTitle = ViewUtil.getLargeGrayLabel("Installing...");
            container.add(queuedTitle);

            for (AppInfo i : this.installQueue) {
                container.add(new AppInstallProgressView(i), "width 100%");
            }
            container.add(Box.createVerticalStrut(20));
        }

        JLabel queuedTitle = ViewUtil.getLargeGrayLabel("Installed");
        container.add(queuedTitle);

        Set<AppInfo> installedApps = installer.getInstallRegistry();

        for (AppInfo i : installedApps) {
            if (recentlyInstalled.contains(i) || recentlyUninstalled.contains(i)) {
                continue;
            }
            container.add(new AppInstallInstalledView(i, installer, this, AppInstallInstalledView.State.INSTALLED), "width 100%");
        }

        for (AppInfo i : recentlyInstalled) {
            container.add(new AppInstallInstalledView(i, installer, this, AppInstallInstalledView.State.JUST_INSTALLED), "width 100%");
        }

        for (AppInfo i : recentlyUninstalled) {
            container.add(new AppInstallInstalledView(i, installer, this, AppInstallInstalledView.State.JUST_UNINSTALLED), "width 100%");
        }

        if (installedApps.isEmpty()) {
            container.add(new JLabel("No apps installed"));
        }

        if (!recentlyInstalled.isEmpty() || !recentlyUninstalled.isEmpty()) {
            container.add(Box.createVerticalStrut(5));
            container.add(new JLabel("<html><font color=RED>Restart " + installer.getProgramName() +  " for changes to take effect</font></html>"));
        }
        
        view.add(new StandardAppContainer(container),BorderLayout.CENTER);

        view.updateUI();
    }

    private synchronized void dequeueAppForInstallation(AppInfo i) {
        installQueue.remove(i);
    }

    synchronized void queueAppForInstallation(final AppInfo i) {
        queueAppForInstallation(i, true);
    }

    synchronized void queueAppForInstallation(final AppInfo i, boolean async) {

        System.out.println("Installing " + i.toString());

        if (!recentlyUninstalled.contains(i)
                && (installer.getInstallRegistry().contains(i) || this.installQueue.contains(i))) {
            System.out.println("App already installed, stopping");
            return;
        }

        installQueue.add(i);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Notification n = new Notification();
                n.setName("Installing " + i.getName());
                n.setIsIndeterminateProgress(true);
                n.setIcon(AppDirectory.getAppStore().getIcon());
                n.setShowsProgress(true);
                
                MedSavantFrame.getInstance().showNotification(n);
                
                boolean success = installer.installApp(i);
                if (success) {
                    addRecentlyInstalledApp(i);
                    dequeueAppForInstallation(i);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateInstalledList();
                        }
                    });
                    n.setDescription("Installed. Requires restart.");
                    n.setShowsProgress(false);
                    //n.close();
                } else {
                    n.setDescription("Error installing App");
                    n.setShowsProgress(false);
                }
                
                n.setIsIndeterminateProgress(false);
            }
        });
        t.start();

        if (!async) {
            try {
                t.join();
            } catch (InterruptedException ex) {
            }
        }

    }

    synchronized void queueAppWithNameForUninstallation(String name) {
        queueAppWithNameForUninstallation(name, true);
    }

    synchronized void queueAppWithNameForUninstallation(String name, boolean async) {

        final AppInfo i = AppInstallUtils.getAppWithName(installer,name);

        if (i == null || !installer.getInstallRegistry().contains(i)) {
            return;
        }

        final AppStoreInstalledPage thisInstance = this;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                boolean success = installer.uninstallApp(i);
                if (success) {
                    thisInstance.addRecentlyUninstalledApp(i);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            thisInstance.updateInstalledList();
                        }
                    });
                }
            }
        });
        t.start();

        if (!async) {
            try {
                t.join();
            } catch (InterruptedException ex) {
            }
        }

    }

    public void addRecentlyUninstalledApp(AppInfo i) {
        recentlyUninstalled.add(i);
    }

    public void addRecentlyInstalledApp(AppInfo i) {
        recentlyInstalled.add(i);
    }

}
