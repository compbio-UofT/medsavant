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
package org.ut.biolab.medsavant.client.view.app;

import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.app.MedSavantAppFetcher;
import org.ut.biolab.medsavant.client.app.MedSavantAppInstaller;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.app.jAppStore;

/**
 *
 * @author mfiume
 */
public class AppStoreApp implements LaunchableApp {
    private jAppStore appStore;

    public AppStoreApp() {
        if (appStore == null) {
            final MedSavantAppFetcher maf = new MedSavantAppFetcher();
            final MedSavantAppInstaller mai = new MedSavantAppInstaller();

            appStore = new jAppStore("MedSavant App Store", maf, mai);
            
            view = new JPanel();
            view.setLayout(new BorderLayout());
            view.add(appStore.getContentPane(),BorderLayout.CENTER);
        }
        
    }
    
    private JPanel view;

    @Override
    public JPanel getView() {
        return view;
    }

    /*private void initView() {
        if (view == null) {
            view = new JPanel();
        }
    }*/

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
        //initView();
        //MedSavantFrame.getInstance().getDashboard().goHome();
        //appStore.showStore();
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public void viewDidLoad() {
        //MedSavantFrame.getInstance().getDashboard().goHome();
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_APPSTORE);
    }

    @Override
    public String getName() {
        return "App Library";
    }

    @Override
    public void didLogout() {
    }

    @Override
    public void didLogin() {
    }

}
