package org.ut.biolab.mfiume.app.page;

import java.util.HashSet;
import java.util.Set;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.mfiume.app.AppInfo;
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

    public AppStoreInstalledPage(AppInstaller installer) {
        this.installer = installer;
        this.installQueue = new HashSet<AppInfo>();
        this.view = new JPanel();

        view.setOpaque(false);
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
            view.add(new AppInstallInstalledView(i, installer,this), "width 100%");
        }

        if (installedApps.isEmpty()) {
            view.add(new JLabel("No apps installed"));
        }

        view.updateUI();
    }

    synchronized void dequeueAppForInstallation(AppInfo i) {
        installQueue.remove(i);
    }

    synchronized void queueAppForInstallation(final AppInfo i) {

        if (installer.getInstallRegistry().contains(i) || this.installQueue.contains(i)) {
            return;
        }

        installQueue.add(i);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = installer.installApp(i);
                if (success) {
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

    }

    private static final String iconroot = "/org/ut/biolab/mfiume/app/icon/";

    @Override
    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource(iconroot + "icon_installed_selected.png"));
    }

}
