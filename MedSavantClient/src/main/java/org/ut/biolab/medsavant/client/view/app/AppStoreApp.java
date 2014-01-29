/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.app.MedSavantAppFetcher;
import org.ut.biolab.medsavant.client.app.MedSavantAppInstaller;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.mfiume.app.jAppStore;

/**
 *
 * @author mfiume
 */
public class AppStoreApp implements DashboardApp {
    private jAppStore appStore;

    public AppStoreApp() {
        if (appStore == null) {
            final MedSavantAppFetcher maf = new MedSavantAppFetcher();
            final MedSavantAppInstaller mai = new MedSavantAppInstaller();

            appStore = new jAppStore("MedSavant App Store", maf, mai);
        }
        
    }
    
    private JPanel view;

    @Override
    public JPanel getView() {
        return view;
    }

    private void initView() {
        if (view == null) {
            view = new JPanel();
        }
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
        initView();
        MedSavantFrame.getInstance().getDashboard().goHome();
        appStore.showStore();
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public void viewDidLoad() {
        MedSavantFrame.getInstance().getDashboard().goHome();
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_APPSTORE);
    }

    @Override
    public String getName() {
        return "App Store";
    }

    @Override
    public void didLogout() {
    }

    @Override
    public void didLogin() {
    }

}