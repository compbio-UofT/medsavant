/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app.builtin;

import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommHandler;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommRegistry;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.PatientVariantAnalyzeComm;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.apache.commons.lang3.ArrayUtils;
import org.ut.biolab.medsavant.shared.appapi.MedSavantVariantSectionApp;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.shared.appapi.MedSavantApp;
import org.ut.biolab.medsavant.client.view.app.AppSubSection;
import org.ut.biolab.medsavant.client.view.app.MultiSectionApp;
import org.ut.biolab.medsavant.client.view.app.builtin.settings.PluginPage;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsChartPage;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsFilterPage;
import org.ut.biolab.medsavant.client.view.genetics.QueryUtils;
import org.ut.biolab.medsavant.client.view.genetics.SpreadsheetPage;
import org.ut.biolab.medsavant.client.view.images.IconFactory;

/**
 *
 * @author mfiume
 */
public class VariantNavigatorApp extends MultiSectionApp implements AppCommHandler<PatientVariantAnalyzeComm> {

    public static boolean isInitialized = false;
    private JPanel[] persistencePanels;

    public VariantNavigatorApp() {
        super("Variant Navigator");
        getSectionMenuComponents();
        AppCommRegistry.getInstance().registerHandler(this, PatientVariantAnalyzeComm.class);
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

    @Override
    public String getHandlerName() {
        return this.getName();
    }

    @Override
    public ImageIcon getHandlerIcon() {
        return this.getIcon();
    }

    @Override
    public void handleCommEvent(PatientVariantAnalyzeComm value) {
        Integer patientID = value.getEventData();
        MedSavantFrame.getInstance().getDashboard().launchApp(this);
        QueryUtils.addQueryOnPatients(new int[]{patientID});
        MedSavantFrame.getInstance().repaint();
    }

}
