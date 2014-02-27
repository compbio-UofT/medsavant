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
package org.ut.biolab.medsavant.client.app;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;
import org.ut.biolab.medsavant.client.app.api.AppInstaller;

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

            AppController.getInstance().installPlugin(downloadedFile);

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

        List<AppDescriptor> descriptors = AppController.getInstance().getDescriptors();
        //public AppInfo(String name, String version, String category, String compatibleWith, String description, String author, String web, URL downloadURL) {

        for (AppDescriptor pd : descriptors) {
            String name = pd.getName();
            String version = pd.getVersion();
            String category = pd.getCategory().toString();
            String sdkVersion = pd.getSDKVersion();
            AppInfo ai = new AppInfo(name, version, category, sdkVersion, null, null, null, null, null, null);
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
        return AppController.getInstance().queuePluginForRemoval(appInfo.getID());
    }

    @Override
    public String getProgramName() {
        return "MedSavant";
    }
}
