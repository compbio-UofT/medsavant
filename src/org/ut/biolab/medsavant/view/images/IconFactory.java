/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.images;

import javax.swing.ImageIcon;

/**
 *
 * @author Marc Fiume
 */
public class IconFactory {

    static IconFactory instance;

    public IconFactory() {
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
        FILTER,
        RESULTS,
        CHART,
        LOGGED_IN
    };

    private static final String iconroot = "/org/ut/biolab/medsavant/view/images/icon/";

    public ImageIcon getIcon(StandardIcon icon) {
        switch(icon) {
            case FILTER:
                return getIcon(iconroot + "filter.gif");
            case RESULTS:
                return getIcon(iconroot + "results.png");
            case CHART:
                return getIcon(iconroot + "chart.png");
            case LOGGED_IN:
                return getIcon(iconroot + "loggedin.png");
            default:
                return null;
        }
    }
}
