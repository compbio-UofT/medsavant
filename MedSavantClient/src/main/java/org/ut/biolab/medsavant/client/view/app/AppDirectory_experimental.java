/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.app;

import java.util.HashMap;
import org.ut.biolab.medsavant.client.view.app.patients.PatientsApp;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;

/**
 *
 * @author mfiume
 */
public class AppDirectory_experimental {
    private final HashMap<String, LaunchableApp> appDirectory;
    
    private PatientsApp patientsApp;
    private SavantApp savantApp;
    private VariantNavigatorApp variantNavigator;
    
    public AppDirectory_experimental(
            VariantNavigatorApp variantNavigator,
            SavantApp savantApp,
            PatientsApp patientsApp
    ) {
        appDirectory = new HashMap<String,LaunchableApp>();
        
        this.variantNavigator = variantNavigator;
        this.savantApp = savantApp;
        this.patientsApp = patientsApp;
        
        this.registerApp(variantNavigator);
        this.registerApp(savantApp);
        this.registerApp(patientsApp);
    }
    
    public final void registerApp(LaunchableApp a) {
        appDirectory.put(a.getName(), a);
    }
    
    public LaunchableApp retrieveApp(String name) {
        return this.appDirectory.get(name);
    }

    public PatientsApp getPatientsApp() {
        return patientsApp;
    }

    public SavantApp getSavantApp() {
        return savantApp;
    }

    public VariantNavigatorApp getVariantNavigator() {
        return variantNavigator;
    }
}
