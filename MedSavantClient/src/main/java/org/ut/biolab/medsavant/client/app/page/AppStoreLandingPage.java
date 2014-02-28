/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.app.page;

import org.ut.biolab.medsavant.client.app.component.FlowView;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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

/**
 *
 * @author mfiume
 */
public class AppStoreLandingPage implements AppStorePage {

    private final AppStoreInstalledPage installedPage;
    private final AppInfoFetcher fetcher;
    private FlowView flowView;
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
        container.setLayout(new MigLayout("fillx,insets 30"));

        flowView = new FlowView();
        flowView.setBorder(BorderFactory.createEmptyBorder());
        container.add(ViewUtil.getLargeGrayLabel("Available apps"),"wrap");
        container.add(flowView,"growx 1.0");
        
        p.add(new StandardAppContainer(container),BorderLayout.CENTER);

        return p;
    }

    private synchronized void setAppInfo(List<AppInfo> info) {
        flowView.removeAll();

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
            flowView.add(infoBox);
        }

        flowView.updateUI();
    }
}
