package org.ut.biolab.mfiume.app.page;

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
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.AppInstallUtils;
import org.ut.biolab.mfiume.app.AppStorePage;
import org.ut.biolab.mfiume.app.api.AppInstaller;
import org.ut.biolab.mfiume.app.component.TitleBar;

/**
 *
 * @author mfiume
 */
public class AppStoreInstalledPage implements AppStorePage {

    AppInstaller installer;
    private final HashSet<AppInfo> installQueue;
    private final JPanel view;
    private final HashSet<AppInfo> recentlyInstalled;
    private final HashSet<AppInfo> recentlyUninstalled;

    public AppStoreInstalledPage(AppInstaller installer) {
        this.installer = installer;
        this.installQueue = new HashSet<AppInfo>();
        this.view = new JPanel();

        view.setOpaque(false);

        recentlyInstalled = new HashSet<AppInfo>();
        recentlyUninstalled = new HashSet<AppInfo>();
    }

    @Override
    public String getName() {
        return "Installed";
    }

    @Override
    public JPanel getView() {
        return view;
    }

    @Override
    public void viewDidLoad() {
        updateInstalledList();
    }

    @Override
    public void viewDidUnload() {
    }

    protected void updateInstalledList() {

        System.out.println("Updating download list " + this.installQueue.size() + " installs in queue");

        view.removeAll();
        MigLayout ml = new MigLayout("wrap 1, gapx 0, gapy 0");
        view.setLayout(ml);

        if (!installQueue.isEmpty()) {
            TitleBar queuedTitle = new TitleBar("Installing...");
            view.add(queuedTitle);

            for (AppInfo i : this.installQueue) {
                view.add(new AppInstallProgressView(i), "width 100%");
            }
        }
        view.add(Box.createVerticalStrut(5));

        TitleBar queuedTitle = new TitleBar("Installed");
        view.add(queuedTitle);

        Set<AppInfo> installedApps = installer.getInstallRegistry();

        for (AppInfo i : installedApps) {
            if (recentlyInstalled.contains(i) || recentlyUninstalled.contains(i)) {
                continue;
            }
            view.add(new AppInstallInstalledView(i, installer, this, AppInstallInstalledView.State.INSTALLED), "width 100%");
        }

        for (AppInfo i : recentlyInstalled) {
            view.add(new AppInstallInstalledView(i, installer, this, AppInstallInstalledView.State.JUST_INSTALLED), "width 100%");
        }

        for (AppInfo i : recentlyUninstalled) {
            view.add(new AppInstallInstalledView(i, installer, this, AppInstallInstalledView.State.JUST_UNINSTALLED), "width 100%");
        }

        if (installedApps.isEmpty()) {
            view.add(new JLabel("No apps installed"));
        }

        if (!recentlyInstalled.isEmpty() || !recentlyUninstalled.isEmpty()) {
            view.add(new JLabel("<html><font color=RED>Restart MedSavant for changes to take effect</font></html>"));
        }

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
    private static final String iconroot = "/org/ut/biolab/mfiume/app/icon/";

    @Override
    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource(iconroot + "icon_installed_selected.png"));
    }

    public void addRecentlyUninstalledApp(AppInfo i) {
        recentlyUninstalled.add(i);
    }

    public void addRecentlyInstalledApp(AppInfo i) {
        recentlyInstalled.add(i);
    }

}
