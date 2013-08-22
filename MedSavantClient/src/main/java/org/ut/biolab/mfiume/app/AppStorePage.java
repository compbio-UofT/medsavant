package org.ut.biolab.mfiume.app;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public interface AppStorePage {

    public String getName();
    public JPanel getView();
    public void viewDidLoad();
    public void viewDidUnload();

    public ImageIcon getIcon();
}
