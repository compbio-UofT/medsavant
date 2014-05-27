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
package org.ut.biolab.medsavant.client.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;
import org.ut.biolab.medsavant.client.app.api.AppInstaller;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;

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
            ex.printStackTrace();
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

    @Override
    public ActionListener getRestartActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MedSavantFrame.getInstance().requestLogoutAndRestart();
            }
            
        };
        
    }
}
