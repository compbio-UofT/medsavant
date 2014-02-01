package org.ut.biolab.medsavant.client.view.app;

import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.api.MedSavantClinicApp;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.plugin.MedSavantApp;
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

        List<MedSavantApp> clinicApps = AppController.getInstance().getPluginsOfClass(MedSavantClinicApp.class);

        for (int i = 0; i < clinicApps.size(); i++) {
            try {
                final MedSavantClinicApp app = (MedSavantClinicApp) clinicApps.get(i);

                s.addLaunchableApp(getLaunchableAppFromMedSavantApp(app));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        s.addLaunchableApp(new VariantNavigatorApp());
        s.addLaunchableApp(new SavantApp());
        s.addLaunchableApp(new AppStoreApp());
        s.addLaunchableApp(new PatientsApp());
        s.addLaunchableApp(new RegionsApp());
        s.addLaunchableApp(AppDirectory.getTaskManager());
        s.addLaunchableApp(AppDirectory.getAccountManager());

        return s;
    }

    private static LaunchableApp getLaunchableAppFromMedSavantApp(final MedSavantClinicApp app) {
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
        
        s.addLaunchableApp(new VCFUploadApp());
        //s.addLaunchableApp(new PhenotipsApp());
        
        s.addLaunchableApp(new SettingsApp());

        return s;
    }

}
