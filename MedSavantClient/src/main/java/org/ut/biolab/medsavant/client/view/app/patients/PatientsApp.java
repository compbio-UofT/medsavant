package org.ut.biolab.medsavant.client.view.app.patients;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.patient.IndividualDetailEditor;
import org.ut.biolab.medsavant.client.patient.IndividualDetailedView;
import org.ut.biolab.medsavant.client.patient.IndividualListModel;
import org.ut.biolab.medsavant.client.view.app.MultiSectionDashboardApp;
import org.ut.biolab.medsavant.client.view.app.patients.PatientsSection;
import org.ut.biolab.medsavant.client.view.app.settings.ManageSection;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;

/**
 *
 * @author mfiume
 */
public class PatientsApp extends MultiSectionDashboardApp {

    public PatientsApp() {
        super(new PatientsSection());
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
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_PATIENTS);
    }

    @Override
    public String getName() {
        return "Patient Directory";
    }


}
