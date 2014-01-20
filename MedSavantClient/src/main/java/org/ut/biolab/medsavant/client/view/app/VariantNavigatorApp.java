/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.apache.commons.lang3.ArrayUtils;
import org.ut.biolab.medsavant.client.api.MedSavantVariantSectionApp;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.plugin.MedSavantApp;
import org.ut.biolab.medsavant.client.view.app.settings.PluginPage;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsChartPage;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsFilterPage;
import org.ut.biolab.medsavant.client.view.genetics.SpreadsheetPage;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.subview.AppSubSection;
import org.ut.biolab.medsavant.client.view.subview.MultiSectionApp;

/**
 *
 * @author mfiume
 */
public class VariantNavigatorApp extends MultiSectionApp {

    public static boolean isInitialized = false;
    private JPanel[] persistencePanels;

    public VariantNavigatorApp() {
        super("Variant Navigator");
        getSectionMenuComponents();
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_VARIANTNAVIGATOR);
    }

    @Override
    public final Component[] getSectionMenuComponents() {
        isInitialized = true;
        return new Component[]{ //createExportVCFButton()
        };
    }

    @Override
    public AppSubSection[] getSubSections() {

        List<AppSubSection> appSections = new LinkedList<AppSubSection>();

        List<MedSavantApp> variantSectionApps = AppController.getInstance().getPluginsOfClass(MedSavantVariantSectionApp.class);

        int numApps = variantSectionApps.size();

        for (MedSavantApp app : variantSectionApps) {
            appSections.add(new PluginPage(this, (MedSavantVariantSectionApp) app));
        }

        AppSubSection[] builtInSections = new AppSubSection[]{
            new SpreadsheetPage(this),
            //new BrowserPage(this),
            new GeneticsChartPage(this)};

        return ArrayUtils.addAll(builtInSections, appSections.toArray(new AppSubSection[numApps]));
    }

    @Override
    public JPanel[] getPersistentPanels() {
        if (persistencePanels == null) {
            persistencePanels = new JPanel[]{
                new GeneticsFilterPage(this).getView()
            };
        }
        return persistencePanels;
    }

}
