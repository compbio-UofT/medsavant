package org.ut.biolab.medsavant.client.view.app.settings;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.app.MultiSectionDashboardApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.app.settings.ManageSection;
import org.ut.biolab.medsavant.client.view.subview.MultiSection;

/**
 *
 * @author mfiume
 */
public class SettingsApp extends MultiSectionDashboardApp {

    public SettingsApp() {
        super(new ManageSection());
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_ADMIN);
    }

    @Override
    public String getName() {
        return "Settings";
    }

    @Override
    public void didLogout() {
    }

    @Override
    public void didLogin() {
    }

}
