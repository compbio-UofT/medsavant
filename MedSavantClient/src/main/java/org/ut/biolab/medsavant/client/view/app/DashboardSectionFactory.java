package org.ut.biolab.medsavant.client.view.app;

import org.ut.biolab.medsavant.client.view.app.settings.SettingsApp;
import org.ut.biolab.medsavant.client.view.app.RegionsApp;
import org.ut.biolab.medsavant.client.view.app.patients.PatientsApp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.MedSavantClinicApp;
import org.ut.biolab.medsavant.client.clinic.GalleryItem;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.plugin.MedSavantApp;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardSection;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardSection;
import org.ut.biolab.medsavant.client.view.images.IconFactory;

/**
 *
 * @author mfiume
 */
public class DashboardSectionFactory {

    public static DashboardSection getAppSection() {

        DashboardSection s = new DashboardSection("");

        List<MedSavantApp> clinicApps = AppController.getInstance().getPluginsOfClass(MedSavantClinicApp.class);

        for (int i = 0; i < clinicApps.size(); i++) {
            try {
                
                final MedSavantClinicApp app = (MedSavantClinicApp) clinicApps.get(i);
                for (int j = 0; j < 10; j++) {
                    s.addDashboardApp(getDashboardAppFromMedSavantApp(app));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return s;
    }

    public static DashboardSection getUberSection() {
        DashboardSection s = new DashboardSection("Uber Section");

        List<MedSavantApp> clinicApps = AppController.getInstance().getPluginsOfClass(MedSavantClinicApp.class);

        for (int i = 0; i < clinicApps.size(); i++) {
            try {
                final MedSavantClinicApp app = (MedSavantClinicApp) clinicApps.get(i);

                s.addDashboardApp(getDashboardAppFromMedSavantApp(app));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        s.addDashboardApp(new VariantNavigatorApp());
        s.addDashboardApp(new SavantApp());
        s.addDashboardApp(new PatientsApp());
        s.addDashboardApp(new RegionsApp());
        s.addDashboardApp(new VCFImportApp());
        s.addDashboardApp(new PhenotipsApp());

        s.addDashboardApp(new AppStoreApp());

        s.addDashboardApp(new SettingsApp());

        return s;
    }

    public static DashboardSection getBuiltInSection() {

        DashboardSection s = new DashboardSection("Built-In Apps");

        s.addDashboardApp(new VariantNavigatorApp());
        s.addDashboardApp(new SavantApp());
        s.addDashboardApp(new PatientsApp());
        s.addDashboardApp(new RegionsApp());
        s.addDashboardApp(new VCFImportApp());
        s.addDashboardApp(new PhenotipsApp());
        

        s.addDashboardApp(new AppStoreApp());

        s.addDashboardApp(new SettingsApp());

        return s;
    }

    private static DashboardApp getDashboardAppFromMedSavantApp(final MedSavantClinicApp app) {
        return new DashboardApp() {

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

        };
    }

}
