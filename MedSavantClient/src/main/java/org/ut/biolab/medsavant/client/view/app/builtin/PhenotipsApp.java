package org.ut.biolab.medsavant.client.view.app.builtin;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.patient.IndividualDetailEditor;
import org.ut.biolab.medsavant.client.patient.IndividualDetailedView;
import org.ut.biolab.medsavant.client.patient.IndividualListModel;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;

/**
 *
 * @author mfiume
 */
public class PhenotipsApp implements LaunchableApp {

    private JPanel view;

    public PhenotipsApp() {

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
            view = new JPanel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_PHENOTIPS);
    }

    @Override
    public String getName() {
        return "Patient Import";
    }

    @Override
    public void didLogout() {
    }

    @Override
    public void didLogin() {
    }
}
