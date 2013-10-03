/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.client.view.images;

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

        ADDKVP,
        BROWSER,
        BAMFILE,
        INSPECTOR,
        CLEAR,
        GOOGLE_LOGO,
        SECTION_ADMIN,
        SECTION_OTHER,
        SECTION_VARIANTS,
        SECTION_PATIENTS,
        SECTION_TABLE,
        SECTION_SEARCH,
        SECTION_DATA,
        SECTION_CLINIC,
        MENU_NOTIFY,
        TRASH,
        DELETE,
        ADD_ON_TOOLBAR,
        REMOVE_ON_TOOLBAR,
        ADD,
        EXPAND,
        COLLAPSE,
        EDIT,
        GEAR,
        IMPORT,
        INFO,
        EXPORT,
        FILTER,
        RESULTS,
        CHART,
        LOGGED_IN,
        SPIRAL,
        TAB_LEFT,
        TAB_RIGHT,
        LOGO,
        SAVE,
        FIRST,
        LAST,
        NEXT,
        PREVIOUS,
        GREEN,
        ORANGE,
        RED,
        WHITE,
        LINKOUT,
        COPY,
        MORE,
        NETWORK,
        CHART_SMALL,
        CONFIGURE,
        CLEAR_ON_TOOLBAR,
        HISTORY_ON_TOOLBAR,
        SAVE_ON_TOOLBAR,
        ACTION_ON_TOOLBAR,
        LOAD_ON_TOOLBAR,
        MENU_USER,
        MENU_SERVER,
        MENU_STORE,
        CLOSE,
        SEARCH_PH,
        DOCK,
        UNDOCK,
        FONT_INCREASE,
        FONT_DECREASE
    };
    private static final String iconroot = "/org/ut/biolab/medsavant/client/view/images/icon/";

    public ImageIcon getIcon(StandardIcon icon) {
        switch (icon) {
            case SEARCH_PH:
                return getIcon(iconroot + "search_placeholder.png");
            case CLOSE:
                return getIcon(iconroot + "close.png");
            case ADDKVP:
                return getIcon(iconroot + "icon_add.png");
            case BROWSER:
                return getIcon(iconroot + "icon_gbrowse.png");
            case BAMFILE:
                return getIcon(iconroot + "icon_bam.png");
            case INSPECTOR:
                return getIcon(iconroot + "inspector2.png");
            case CLEAR:
                return getIcon(iconroot + "clear.png");
            case MENU_NOTIFY:
                return getIcon(iconroot + "menu-notify.png");
            case CONFIGURE:
                return getIcon(iconroot + "configure.png");
            case CHART_SMALL:
                return getIcon(iconroot + "chart_1.png");
            case MORE:
                return getIcon(iconroot + "more2.png");
            case COPY:
                return getIcon(iconroot + "cpy.png");
            case GOOGLE_LOGO:
                return getIcon(iconroot + "googleLog.png");
            case LINKOUT:
                return getIcon(iconroot + "linkout.png");
            case FILTER:
                return getIcon(iconroot + "filter.png");
            case SECTION_CLINIC:
                return getIcon(iconroot + "menu-clinic.png");
            case SECTION_ADMIN:
                return getIcon(iconroot + "menu-tune.png");
            case SECTION_OTHER:
                return getIcon(iconroot + "menu-project.png");
            case SECTION_PATIENTS:
                return getIcon(iconroot + "section_patients.png");
            case SECTION_VARIANTS:
                return getIcon(iconroot + "section_variants2.png");
            case MENU_USER:
                return getIcon(iconroot + "menu-user.png");
            case MENU_STORE:
                return getIcon(iconroot + "menu-store.png");
            case SECTION_TABLE:
                return getIcon(iconroot + "menu-search.png");
            case SECTION_DATA:
                return getIcon(iconroot + "menu-database.png");
            case SECTION_SEARCH:
                return getIcon(iconroot + "menu-binoculars.png");
            case TRASH:
                return getIcon(iconroot + "trash.png");
            case DELETE:
                return getIcon(iconroot + "delete.png");
            case SAVE:
                return getIcon(iconroot + "save2.png");
            case GREEN:
                return getIcon(iconroot + "green.png");
            case ORANGE:
                return getIcon(iconroot + "orange.png");
            case RED:
                return getIcon(iconroot + "red.png");
            case WHITE:
                return getIcon(iconroot + "white.png");
            case EXPAND:
                return getIcon(iconroot + "expand.png");
            case COLLAPSE:
                return getIcon(iconroot + "collapse.png");
            case ADD_ON_TOOLBAR:
                return getIcon(iconroot + "mac_add.png");
            case REMOVE_ON_TOOLBAR:
                return getIcon(iconroot + "mac_remove.png");
            case HISTORY_ON_TOOLBAR:
                return getIcon(iconroot + "mac_history.png");
            case CLEAR_ON_TOOLBAR:
                return getIcon(iconroot + "mac_clear.png");
            case SAVE_ON_TOOLBAR:
                return getIcon(iconroot + "mac_save.png");
            case LOAD_ON_TOOLBAR:
                return getIcon(iconroot + "mac_load.png");
            case ACTION_ON_TOOLBAR:
                return getIcon(iconroot + "mac_action.png");
            case ADD:
                return getIcon(iconroot + "add_f.png");
            case EDIT:
                return getIcon(iconroot + "mac_edit.png");
            case GEAR:
                return getIcon(iconroot + "mac_gear.png");
            case NETWORK:
                return getIcon(iconroot + "mac_link.png");
            case IMPORT:
                return getIcon(iconroot + "import.png");
            case INFO:
                return getIcon(iconroot + "info_circle.png");
            case EXPORT:
                return getIcon(iconroot + "export.png");
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
            case MENU_SERVER:
                return getIcon(iconroot + "menu-server.png");
            case UNDOCK:
                return getIcon(iconroot + "export.png");
            case DOCK:
                return getIcon(iconroot + "import.png");
            case FONT_INCREASE:
                return getIcon(iconroot + "font_increase.png");
            case FONT_DECREASE:
                return getIcon(iconroot + "font_decrease.png");
            default:
                return null;
        }
    }
}
