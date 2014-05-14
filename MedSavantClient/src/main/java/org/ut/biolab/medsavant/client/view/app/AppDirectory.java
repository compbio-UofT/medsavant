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

import javax.swing.ImageIcon;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.builtin.RegionsApp;
import org.ut.biolab.medsavant.client.view.app.builtin.SavantApp;
import org.ut.biolab.medsavant.client.view.app.builtin.VariantNavigatorApp;
import org.ut.biolab.medsavant.client.view.app.builtin.patients.PatientsApp;
import org.ut.biolab.medsavant.client.view.app.builtin.task.TaskManagerApp;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;

/**
 *
 * @author mfiume
 */
public class AppDirectory {

    private static TaskManagerApp taskManager;
    private static AccountManagerApp accountManager;
    private static SavantApp genomeBrowser;
    private static VariantNavigatorApp variantNavigator;
    private static PatientsApp patients;
    private static RegionsApp regions;
    private static AppStoreApp appStore;

    public static AppStoreApp getAppStore() {
        return appStore;
    }

    public static enum BuiltInApp {
        TASK_MANAGER,
        ACCOUNT_MANAGER,
        PATIENTS,
        REGIONS,
        GENOME_BROWSER,
        VARIANT_NAVIGATOR,
        APP_STORE
    };

    public static void launchApp(BuiltInApp builtInApp) {

        LaunchableApp app = null;

        switch (builtInApp) {
            case TASK_MANAGER:
                app = taskManager;
                break;
            case ACCOUNT_MANAGER:
                app = accountManager;
                break;
            case GENOME_BROWSER:
                app = genomeBrowser;
                break;
            case PATIENTS:
                app = patients;
                break;
            case REGIONS:
                app = regions;
                break;
            case VARIANT_NAVIGATOR:
                app = variantNavigator;
                break;
            case APP_STORE:
                app = appStore;
                break;
        }

        // launch the app
        if (app != null) {
            MedSavantFrame.getInstance().getDashboard().launchApp(app);
        }
    }

    public static VariantNavigatorApp getVariantNavigator() {
        if (variantNavigator == null) {
            variantNavigator = new VariantNavigatorApp();
        }
        return variantNavigator;
    }

    public static AppStoreApp getAppStoreApp() {
        if (appStore == null) {
            appStore = new AppStoreApp();
        }
        return appStore;
    }

    public static PatientsApp getPatientsApp() {
        if (patients == null) {
            patients = new PatientsApp();
        }
        return patients;
    }

    public static RegionsApp getRegionsApp() {
        if (regions == null) {
            regions = new RegionsApp();
        }
        return regions;
    }

    public static SavantApp getGenomeBrowser() {
        if (genomeBrowser == null) {
            genomeBrowser = new SavantApp();
        }
        return genomeBrowser;
    }

    public static TaskManagerApp getTaskManager() {
        if (taskManager == null) {
            taskManager = new TaskManagerApp();
        }
        return taskManager;
    }

    public static AccountManagerApp getAccountManager() {
        if (accountManager == null) {
            accountManager = new AccountManagerApp();
        }
        return accountManager;
    }

}
