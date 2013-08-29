package org.ut.biolab.mfiume.app.page;

import org.ut.biolab.mfiume.app.component.FlowView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
import org.ut.biolab.mfiume.app.api.AppInfoFetcher;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.AppInstallUtils;
import org.ut.biolab.mfiume.app.AppStorePage;
import org.ut.biolab.mfiume.app.AppStoreViewManager;
import org.ut.biolab.mfiume.app.api.AppInstaller;
import org.ut.biolab.mfiume.app.component.TitleBar;

/**
 *
 * @author mfiume
 */
public class AppStoreLandingPage implements AppStorePage {

    private final AppStoreInstalledPage installedPage;
    private final AppInfoFetcher fetcher;
    private FlowView flowView;
    private final AppStoreViewManager avm;
    private final AppInstaller installer;

    public AppStoreLandingPage(AppInfoFetcher fetcher, AppInstaller installer, AppStoreViewManager avm, AppStoreInstalledPage installedPage) {
        this.fetcher = fetcher;
        this.installer = installer;
        this.avm = avm;
        this.installedPage = installedPage;
    }

    @Override
    public String getName() {
        return "Apps";
    }

    @Override
    public JPanel getView() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BorderLayout());

        flowView = new FlowView();
        p.add(new TitleBar("Available apps"),BorderLayout.NORTH);
        p.add(flowView, BorderLayout.CENTER);

        return p;
    }

    private synchronized void setAppInfo(List<AppInfo> info) {
        flowView.removeAll();

        Set<AppInfo> installRegistry = installer.getInstallRegistry();

        for (AppInfo i : info) {
            boolean installedAlready = installRegistry.contains(i);
            boolean canUpdate = false;
            if (installedAlready) {
                AppInfo installedApp = AppInstallUtils.getAppWithName(installer,i.getName());
                if (new AppDescriptor.Version(i.getVersion()).isNewerThan(new AppDescriptor.Version(installedApp.getVersion()))) {
                    canUpdate = true;
                }
            }
            AppInfoFlowView infoBox = new AppInfoFlowView(i,avm,installedPage,installedAlready,canUpdate);
            flowView.add(infoBox);
        }

        flowView.updateUI();
        System.out.println("Done setting app info for " + info.size() + " apps");
    }

    @Override
    public void viewDidLoad() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<AppInfo> appInfo = fetcher.fetchApplicationInformation(null);
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            setAppInfo(appInfo);
                        }
                    });
                } catch (Exception ex) {
                    Logger.getLogger(AppStoreLandingPage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }

    @Override
    public void viewDidUnload() {
    }


    private static final String iconroot = "/org/ut/biolab/mfiume/app/icon/";

    @Override
    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource(iconroot + "icon_shop_selected.png"));
    }
}
