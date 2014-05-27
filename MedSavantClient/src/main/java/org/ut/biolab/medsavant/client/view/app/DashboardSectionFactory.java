package org.ut.biolab.medsavant.client.view.app;

import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.shared.appapi.MedSavantDashboardApp;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.shared.appapi.MedSavantApp;
import org.ut.biolab.medsavant.client.view.app.builtin.RegionsApp;
import org.ut.biolab.medsavant.client.view.app.builtin.SavantApp;
import org.ut.biolab.medsavant.client.view.app.builtin.VariantNavigatorApp;
import org.ut.biolab.medsavant.client.view.app.builtin.patients.PatientsApp;
import org.ut.biolab.medsavant.client.view.app.builtin.settings.SettingsApp;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardSection;
import org.ut.biolab.medsavant.shared.model.UserLevel;

/**
 *
 * @author mfiume
 */
public class DashboardSectionFactory {

    public static DashboardSection getUberSection() {
        DashboardSection s = new DashboardSection("Apps");

        List<MedSavantApp> clinicApps = AppController.getInstance().getPluginsOfClass(MedSavantDashboardApp.class);

        for (int i = 0; i < clinicApps.size(); i++) {
            try {
                final MedSavantDashboardApp app = (MedSavantDashboardApp) clinicApps.get(i);

                s.addLaunchableApp(getLaunchableAppFromMedSavantApp(app));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        s.addLaunchableApp(AppDirectory.getVariantNavigator());
        s.addLaunchableApp(AppDirectory.getGenomeBrowser());
        
        s.addLaunchableApp(AppDirectory.getPatientsApp());
        s.addLaunchableApp(AppDirectory.getRegionsApp());
        s.addLaunchableApp(AppDirectory.getTaskManager());
        
        if (LoginController.getInstance().getUserLevel() != UserLevel.GUEST) {
            s.addLaunchableApp(new VCFUploadApp());
        }
        
        if (LoginController.getInstance().getUserLevel() == UserLevel.ADMIN) {
            s.addLaunchableApp(new SettingsApp());
        }
        
        s.addLaunchableApp(AppDirectory.getAccountManager());
        s.addLaunchableApp(AppDirectory.getAppStoreApp());

        return s;
    }

    private static LaunchableApp getLaunchableAppFromMedSavantApp(final MedSavantDashboardApp app) {
        return new LaunchableApp() {

            @Override
            public JPanel getView() {
                return app.getContent();
            }

            @Override
            public void viewWillUnload() {
            }

            @Override
            public void viewWillLoad() {
            }

            @Override
            public void viewDidUnload() {
                app.viewDidUnload();
            }

            @Override
            public void viewDidLoad() {
                app.viewDidLoad();
            }

            @Override
            public ImageIcon getIcon() {
                return app.getIcon();
            }

            @Override
            public String getName() {
                return app.getTitle();
            }

            @Override
            public void didLogout() {
                // todo: make this a part of app API
            }

            @Override
            public void didLogin() {
                // todo: make this a part of app API
            }

        };
    }

    public static DashboardSection getManagementSection() {
        final DashboardSection s = new DashboardSection("Management Apps");
        
        // hide this section from the dashbord
        s.setEnabled(LoginController.getInstance().getUserLevel() == UserLevel.ADMIN);
        
        //s.addLaunchableApp(new VCFUploadApp());
        //s.addLaunchableApp(new PhenotipsApp());
        
        //s.addLaunchableApp(new SettingsApp());

        return s;
    }

}
