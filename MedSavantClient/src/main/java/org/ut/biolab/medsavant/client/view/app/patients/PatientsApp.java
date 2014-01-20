package org.ut.biolab.medsavant.client.view.app.patients;

import javax.swing.ImageIcon;
import org.ut.biolab.medsavant.client.cohort.CohortsPage;
import org.ut.biolab.medsavant.client.patient.IndividualsPage;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.subview.AppSubSection;
import org.ut.biolab.medsavant.client.view.subview.MultiSectionApp;

/**
 *
 * @author mfiume
 */
public class PatientsApp extends MultiSectionApp {

    public PatientsApp() {
        super("Patient Directory");
    }

    @Override
    public AppSubSection[] getSubSections() {
        return new AppSubSection[]{
            new IndividualsPage(this),
            new CohortsPage(this)};
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_PATIENTS);
    }
}
