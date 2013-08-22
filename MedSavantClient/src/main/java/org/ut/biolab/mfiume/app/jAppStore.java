/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.mfiume.app;

import java.awt.Color;
import org.ut.biolab.medsavant.client.app.MedSavantAppInstaller;
import org.ut.biolab.mfiume.app.api.AppInfoFetcher;
import org.ut.biolab.medsavant.client.app.MedSavantAppFetcher;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import org.ut.biolab.mfiume.app.api.AppInstaller;


/**
 *
 * @author mfiume
 */
public class jAppStore {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        MedSavantAppFetcher maf = new MedSavantAppFetcher();
        MedSavantAppInstaller mai = new MedSavantAppInstaller();

        jAppStore as = new jAppStore("MedSavant App Store",maf,mai);
        as.showStore();
    }
    private final AppStoreView view;
    private final AppInfoFetcher fetcher;

    public jAppStore(String title, AppInfoFetcher fetcher, AppInstaller installer) {
        this.view = new AppStoreView(title,fetcher,installer);
        this.fetcher = fetcher;
    }

    public void showStore() {
        this.view.setVisible(true);
    }

    public static void wrapComponentWithLineBorder(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(235,235,235), 1), c.getBorder()));
    }

}
