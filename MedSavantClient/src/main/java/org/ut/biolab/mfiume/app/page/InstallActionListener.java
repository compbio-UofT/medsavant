package org.ut.biolab.mfiume.app.page;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.AppInstallUtils;
import org.ut.biolab.mfiume.app.AppStoreViewManager;

/**
 *
 * @author mfiume
 */
public class InstallActionListener implements ActionListener {

    private boolean doUpdate = false;
    private final AppStoreInstalledPage installedPage;
    private final AppInfo i;
    private final AppStoreViewManager avm;

    public InstallActionListener(AppStoreInstalledPage installedPage, AppInfo i, AppStoreViewManager avm) {
        this.installedPage = installedPage;
        this.i = i;
        this.avm = avm;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (doUpdate) { // must uninstall the app first
            installedPage.queueAppWithNameForUninstallation(i.getName(), false);
        }
        installedPage.queueAppForInstallation(i);
        avm.switchToPage(installedPage);
    }

    void setModeToUpdate(boolean b) {
        this.doUpdate = b;
    }
}
