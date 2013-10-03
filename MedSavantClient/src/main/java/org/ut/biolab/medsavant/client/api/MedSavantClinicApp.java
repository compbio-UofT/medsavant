package org.ut.biolab.medsavant.client.api;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.plugin.MedSavantApp;

/**
 *
 * @author mfiume
 */
public abstract class MedSavantClinicApp extends MedSavantApp {

    public abstract ImageIcon getIcon();
    public abstract JPanel getContent();

    public abstract void viewDidLoad();
    public abstract void viewDidUnload();

}
