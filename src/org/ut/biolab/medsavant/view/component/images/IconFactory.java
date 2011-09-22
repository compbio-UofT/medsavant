/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.component.images;

import javax.swing.ImageIcon;

/**
 *
 * @author Marc Fiume
 */
public class IconFactory {

    static IconFactory instance;

    public IconFactory() {
        instance = this;
    }

    public static IconFactory getInstance() {
        if (instance == null) {
            instance = new IconFactory();
        }
        return instance;
    }

    public ImageIcon getIcon(String resourcePath) {
        return new ImageIcon(getClass().getResource(resourcePath));
    }

    public enum StandardIcon { 
        FIRST,
        PREVIOUS,
        NEXT,
        LAST};

    private static final String iconroot = "/fiume/table/images/icon/";

    public ImageIcon getIcon(StandardIcon icon) {
        switch(icon) {
            case FIRST:
                return getIcon(iconroot + "first.png");
            case PREVIOUS:
                return getIcon(iconroot + "previous.png");
            case NEXT:
                return getIcon(iconroot + "next.png");
            case LAST:
                return getIcon(iconroot + "last.png");
            default:
                return null;
        }
    }
}
