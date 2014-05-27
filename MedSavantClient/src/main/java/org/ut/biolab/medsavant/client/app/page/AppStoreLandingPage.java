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
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
import org.ut.biolab.medsavant.client.app.api.AppInfoFetcher;
import org.ut.biolab.medsavant.client.app.AppInfo;
import org.ut.biolab.medsavant.client.app.AppInstallUtils;
import org.ut.biolab.medsavant.client.app.AppStorePage;
import org.ut.biolab.medsavant.client.view.component.LazyPanel;
import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.app.api.AppInstaller;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public class AppStoreLandingPage implements AppStorePage {

    private final AppStoreInstalledPage installedPage;
    private final AppInfoFetcher fetcher;
    private JPanel appListView;
    private final JTabbedPane parent;
    private final AppInstaller installer;

    public AppStoreLandingPage(AppInfoFetcher fetcher, AppInstaller installer, JTabbedPane parent, AppStoreInstalledPage installedPage) {
        this.fetcher = fetcher;
        this.installer = installer;
        this.parent = parent;
        this.installedPage = installedPage;
    }

    @Override
    public String getName() {
        return "Apps";
    }

    @Override
    public LazyPanel getView() {
        LazyPanel p = new LazyPanel() {

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

        };

        p.setOpaque(false);
        p.setLayout(new BorderLayout());
        
        JPanel container = ViewUtil.getClearPanel();
        container.setLayout(new MigLayout("fillx,insets 0"));

        appListView = ViewUtil.getClearPanel();
        appListView.setLayout(new MigLayout("wrap, fillx, insets 0"));
        
        JLabel titleLabel = ViewUtil.getLargeSerifLabel("Available Apps");
        
        container.add(titleLabel,"wrap");
        container.add(appListView,"growx 1.0");
        
        JPanel fixedWidth = ViewUtil.getDefaultFixedWidthPanel(container);
        
        StandardAppContainer sac = new StandardAppContainer(fixedWidth);
        p.add(sac,BorderLayout.CENTER);
        sac.setBackground(ViewUtil.getLightGrayBackgroundColor());

        return p;
    }

    private synchronized void setAppInfo(List<AppInfo> info) {
        appListView.removeAll();

        Set<AppInfo> installRegistry = installer.getInstallRegistry();

        for (AppInfo i : info) {
            boolean installedAlready = installRegistry.contains(i);
            boolean canUpdate = false;
            if (installedAlready) {
                AppInfo installedApp = AppInstallUtils.getAppWithName(installer, i.getName());
                if (new AppDescriptor.AppVersion(i.getVersion()).isNewerThan(new AppDescriptor.AppVersion(installedApp.getVersion()))) {
                    canUpdate = true;
                }
            }
            AppInfoFlowView infoBox = new AppInfoFlowView(i, parent, installedPage, installedAlready, canUpdate);
            appListView.add(infoBox, "width 100%");
        }

        appListView.updateUI();
    }
}
