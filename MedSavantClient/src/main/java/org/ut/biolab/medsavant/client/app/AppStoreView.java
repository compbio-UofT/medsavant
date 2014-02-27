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
package org.ut.biolab.medsavant.client.app;

import org.ut.biolab.medsavant.client.view.component.LazyPanel;
import org.ut.biolab.medsavant.client.app.api.AppInfoFetcher;
import org.ut.biolab.medsavant.client.app.page.AppStoreLandingPage;
import org.ut.biolab.medsavant.client.app.page.AppStoreInstalledPage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.client.view.component.MSTabbedPaneUI;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.app.api.AppInstaller;

/**
 *
 * @author mfiume
 */
class AppStoreView extends JDialog {

    private final AppInfoFetcher fetcher;
    private final AppInstaller installer;

    public AppStoreView(String title, AppInfoFetcher fetcher, AppInstaller installer) {
        this.setTitle(title);
        this.setModal(true);
        this.setMinimumSize(new Dimension(600, 600));
        this.setLocationRelativeTo(null);
        this.fetcher = fetcher;
        this.installer = installer;
        initView();
        this.setLocationRelativeTo(null);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    private void initView() {

        JTabbedPane tabs = new JTabbedPane() {
            
            private int selectedIndex = -1;

            public Color getForegroundAt(int index) {
                if (getSelectedIndex() == index) {
                    return Color.WHITE;
                }
                return Color.BLACK;
            }

            public void setSelectedIndex(int index) {

                if (selectedIndex != -1 && this.getComponentAt(selectedIndex) instanceof LazyPanel) {
                    LazyPanel p = (LazyPanel) this.getComponentAt(selectedIndex);
                    p.viewDidUnload();
                }
                
                if (this.getComponentAt(index) instanceof LazyPanel) {
                    LazyPanel p = (LazyPanel) this.getComponentAt(index);
                    p.viewDidLoad();
                }
                
                selectedIndex = index;

                super.setSelectedIndex(index);
            }
        };

        tabs.setUI(new MSTabbedPaneUI());
        tabs.setFocusable(false);
        tabs.setBackground(ViewUtil.getDefaultBackgroundColor());
        
        AppStoreInstalledPage installedAppsPage = new AppStoreInstalledPage(installer);
        AppStoreLandingPage landingPage = new AppStoreLandingPage(fetcher, installer, tabs, installedAppsPage);

        tabs.add(landingPage.getName(), landingPage.getView());
        tabs.add(installedAppsPage.getName(), installedAppsPage.getView());

        this.setLayout(new BorderLayout());
        this.add(tabs, BorderLayout.CENTER);
        tabs.setSelectedIndex(0);
    }
}
