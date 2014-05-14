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
                    return Color.BLACK;
                }
                return new Color(40, 40, 40);
                
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
        tabs.setBackground(ViewUtil.getLightGrayBackgroundColor());

        AppStoreInstalledPage installedAppsPage = new AppStoreInstalledPage(installer);
        AppStoreLandingPage landingPage = new AppStoreLandingPage(fetcher, installer, tabs, installedAppsPage);

        tabs.add(landingPage.getName(), landingPage.getView());
        tabs.add(installedAppsPage.getName(), installedAppsPage.getView());

        this.setLayout(new BorderLayout());
        this.add(tabs, BorderLayout.CENTER);
        tabs.setSelectedIndex(0);
    }
}
