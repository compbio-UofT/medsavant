package org.ut.biolab.medsavant.client.app;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.client.plugin.PluginController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.view.dialog.DownloadDialog;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.api.AppInstaller;

/**
 *
 * @author mfiume
 */
public class MedSavantAppInstaller implements AppInstaller {

    private final HashSet<AppInfo> installedApps;

    public MedSavantAppInstaller() {
        installedApps = new HashSet<AppInfo>();
    }

    @Override
    public boolean installApp(AppInfo i) {

        System.out.println("Installing app " + i.getName());
        //TODO: use Apache commons-io instead
        String url = i.getDownloadURL().getPath();

        System.out.println("Downloading from " + url);

        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        try {

            System.out.println("Downloading to " + DirectorySettings.getTmpDirectory().getAbsolutePath());

            File downloadedFile = NetworkUtils.downloadFile(i.getDownloadURL(), DirectorySettings.getTmpDirectory(), fileName);

            System.out.println("Downloaded " + downloadedFile.getAbsolutePath());
            System.out.println("Installing from " + downloadedFile.getAbsolutePath());

            PluginController.getInstance().installPlugin(downloadedFile);

            System.out.println("Plugin installed to  " + DirectorySettings.getPluginsDirectory().getAbsolutePath());

        } catch (Throwable ex) {
            return false;
        }

        System.out.println("Done installing app " + i.getName());
        installedApps.add(i);
        return true;
    }

    @Override
    public Set<AppInfo> getInstallRegistry() {
        return installedApps;
    }

    @Override
    public boolean uninstallApp(AppInfo appInfo) {
        installedApps.remove(appInfo);
        return true;
    }
}
