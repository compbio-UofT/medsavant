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
        ADD,
        REMOVE,
        EXPAND,
        COLLAPSE,
        EDIT,
        FILTER,
        RESULTS,
        CHART,
        LOGGED_IN,
        SPIRAL,
        TAB_LEFT,
        TAB_RIGHT,
        LOGO,
        FIRST,
        LAST,
        NEXT,
        PREVIOUS
    };
    private static final String iconroot = "/org/ut/biolab/medsavant/view/images/icon/";

    public ImageIcon getIcon(StandardIcon icon) {
        switch (icon) {
            case EXPAND:
                return getIcon(iconroot + "expand.png");//"open_normal_green.png");
            case COLLAPSE:
                return getIcon(iconroot + "collapse.png");//"close_normal_green.png");
            case ADD:
                return getIcon(iconroot + "mac_add.png");//"open_normal_green.png");
            case REMOVE:
                return getIcon(iconroot + "mac_remove.png");//"close_normal_green.png");
            case EDIT:
                return getIcon(iconroot + "mac_edit.png");//"close_normal_green.png");
            case FILTER:
                return getIcon(iconroot + "filter.gif");
            case RESULTS:
                return getIcon(iconroot + "results.png");
            case CHART:
                return getIcon(iconroot + "chart.png");
            case LOGGED_IN:
                return getIcon(iconroot + "loggedin.png");
            case SPIRAL:
                return getIcon(iconroot + "spiral_green.png");
            case LOGO:
                return getIcon(iconroot + "medsavantlogo.png");
            case FIRST:
                return getIcon(iconroot + "first.png");
            case PREVIOUS:
                return getIcon(iconroot + "previous.png");
            case NEXT:
                return getIcon(iconroot + "next.png");
            case LAST:
                return getIcon(iconroot + "last.png");
            case TAB_LEFT:
                
                
                return getIcon(iconroot + "tab_l.png");
            case TAB_RIGHT:
                return getIcon(iconroot + "tab_r.png");
            default:
                return null;
        }
    }
}
