package org.ut.biolab.medsavant.client.clinic;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class GalleryItem {
    private final ImageIcon icon;
    private final JPanel panel;
    private final String name;

    public GalleryItem(ImageIcon icon, JPanel panel, String name) {
        this.icon = icon;
        this.panel = panel;
        this.name = name;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getName() {
        return name;
    }
}
