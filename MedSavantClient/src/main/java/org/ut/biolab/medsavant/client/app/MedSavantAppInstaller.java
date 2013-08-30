package org.ut.biolab.medsavant.client.app;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.client.plugin.PluginBrowser;
import org.ut.biolab.medsavant.client.plugin.PluginController;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
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

    private HashSet<AppInfo> installedApps;

    public MedSavantAppInstaller() {
    }

    @Override
    public boolean installApp(AppInfo i) {

        System.out.println("Installing app " + i.getName());
        //TODO: use Apache commons-io instead
        String url = i.getDownloadURL().getFile();

        System.out.println("Downloading from " + i.getDownloadURL().toString());

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
        //installedApps.add(i);
        return true;
    }

    private void updateRegistry() {
        installedApps = new HashSet<AppInfo>();

        List<AppDescriptor> descriptors = PluginController.getInstance().getDescriptors();
        //public AppInfo(String name, String version, String category, String compatibleWith, String description, String author, String web, URL downloadURL) {

        for (AppDescriptor pd : descriptors) {
            String name = pd.getName();
            String version = pd.getVersion();
            String type = pd.getType().toString();
            String sdkVersion = pd.getSDKVersion();
            AppInfo ai = new AppInfo(name, version, type, sdkVersion, null, null, null, null);
            ai.setID(pd.getID());
            installedApps.add(ai);
        }
    }

    @Override
    public Set<AppInfo> getInstallRegistry() {
        updateRegistry();
        return installedApps;
    }

    @Override
    public boolean uninstallApp(AppInfo appInfo) {
        System.out.println("Uninstalling app " + appInfo.toString());
        return PluginController.getInstance().queuePluginForRemoval(appInfo.getID());
    }

    @Override
    public String getProgramName() {
        return "MedSavant";
    }
}
