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
package org.ut.biolab.medsavant.client.app.page;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import org.ut.biolab.medsavant.client.view.font.FontFactory;
import org.ut.biolab.medsavant.client.view.notify.Notification;

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
        
        view.removeAll();
        view.setLayout(new BorderLayout());
        
        JPanel container = ViewUtil.getClearPanel();
        
        MigLayout ml = new MigLayout("wrap 1, gapy 5, hidemode 3, insets 0");
        container.setLayout(ml);

        if (!installQueue.isEmpty()) {
            JLabel queuedTitle = ViewUtil.getLargeGrayLabel("Installing...");
            container.add(queuedTitle);

            for (AppInfo i : this.installQueue) {
                container.add(new AppInstallProgressView(i), "width 100%");
            }
            container.add(Box.createVerticalStrut(20));
        }

        //JLabel queuedTitle = ViewUtil.getLargeGrayLabel("Installed");
        JLabel queuedTitle = ViewUtil.getLargeSerifLabel("Installed");
        
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
            container.add(ViewUtil.getGrayItalicizedLabel("No apps installed"));
        }

        if (!recentlyInstalled.isEmpty() || !recentlyUninstalled.isEmpty()) {
            container.add(Box.createVerticalStrut(5));
            
            JButton restartButton = new JButton("Restart Now");
            restartButton.addActionListener(installer.getRestartActionListener());
            
            container.add(ViewUtil.horizontallyAlignComponents(
                    new Component[] { 
                        new JLabel("<html><font color=RED>Restart " + installer.getProgramName() +  " for changes to take effect</font></html>"),
                        restartButton 
                    }));
        }
        
        JPanel fixedWidth = ViewUtil.getDefaultFixedWidthPanel(container);
        
        StandardAppContainer sac = new StandardAppContainer(fixedWidth);
        view.add(sac,BorderLayout.CENTER);
        sac.setBackground(ViewUtil.getLightGrayBackgroundColor());

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
                    
                    n.setAction("Restart", new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            MedSavantFrame.getInstance().requestLogoutAndRestart();
                        }
                        
                    });
                    
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
