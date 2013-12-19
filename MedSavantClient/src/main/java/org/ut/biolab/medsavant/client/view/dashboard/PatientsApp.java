package org.ut.biolab.medsavant.client.view.dashboard;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.patient.IndividualDetailEditor;
import org.ut.biolab.medsavant.client.patient.IndividualDetailedView;
import org.ut.biolab.medsavant.client.patient.IndividualListModel;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;

/**
 *
 * @author mfiume
 */
class PatientsApp implements DashboardApp {
    private SplitScreenView view;

    public PatientsApp() {

    }

    @Override
    public JPanel getView() {
        return view;
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
        initView();

    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public void viewDidLoad() {
    }

    private void initView() {
        try {
            view = new SplitScreenView(
                    new IndividualListModel(),
                    new IndividualDetailedView("Patients"),
                    new IndividualDetailEditor());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_PANELS);
    }

    @Override
    public String getName() {
        return "Patients";
    }

}
