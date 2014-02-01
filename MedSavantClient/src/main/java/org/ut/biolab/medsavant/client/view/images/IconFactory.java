/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.images;

import javax.swing.ImageIcon;

/**
 *
 * @author Marc Fiume
 */
public class IconFactory {

    static IconFactory instance;
    public static final String ICON_ROOT = "/org/ut/biolab/medsavant/client/view/images/icon/";


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
        IMPORT_VCF,
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
        FONT_DECREASE,
        WAIT,
        APP_REGIONS,
        APP_PANELS,
        APP_PATIENTS,
        APP_IMPORTVCF,
        APP_APPSTORE,
        APP_VARIANTNAVIGATOR,
        APP_ADMIN,
        APP_PHENOTIPS,
        APP_TASKMANAGER,
        APP_ACCOUNT,
        APP_SAVANT,
        BTN_LEFTARROW,
        BTN_UPARROW,
        MEDSAVANT_TEXT_LOGO,
        DASHBOARD,
        BTN_MENU
    };
    
    
    public ImageIcon getIcon(StandardIcon icon) {
        switch (icon) {
            case DASHBOARD:
                return getIcon(ICON_ROOT + "icon-home-inmenu.png");
            case IMPORT_VCF:
                return getIcon(ICON_ROOT + "import-dd-vcf.png");
            case BTN_MENU:
                return getIcon(ICON_ROOT + "btn-menu.png");
            case BTN_LEFTARROW:
                return getIcon(ICON_ROOT + "btn-left-light.png");
            case BTN_UPARROW:
                return getIcon(ICON_ROOT + "btn-up.png");
            case MEDSAVANT_TEXT_LOGO:
                return getIcon(ICON_ROOT + "medsavant-text-logo.png");
            case APP_SAVANT:
                return getIcon(ICON_ROOT + "icon-savant.png");
            case APP_ACCOUNT:
                return getIcon(ICON_ROOT + "icon-user.png");
            case APP_TASKMANAGER:
                return getIcon(ICON_ROOT + "icon-taskmanager.png");
            case APP_PHENOTIPS:
                return getIcon(ICON_ROOT + "icon-phenotips.png");
             case APP_ADMIN:
                return getIcon(ICON_ROOT + "icon-admin.png");
             case APP_APPSTORE:
                return getIcon(ICON_ROOT + "icon-appstore.png");
            case APP_VARIANTNAVIGATOR:
                return getIcon(ICON_ROOT + "icon-variantnavigator.png");
            case APP_IMPORTVCF:
                return getIcon(ICON_ROOT + "icon-importvcf.png");
            case APP_PATIENTS:
                return getIcon(ICON_ROOT + "icon-patients.png");
            case APP_REGIONS:
                return getIcon(ICON_ROOT + "icon-regions.png");
            case APP_PANELS:
                return getIcon(ICON_ROOT + "icon-panels.png");
            case WAIT:
                return getIcon(ICON_ROOT + "wait.gif");
            case SEARCH_PH:
                return getIcon(ICON_ROOT + "search_placeholder.png");
            case CLOSE:
                return getIcon(ICON_ROOT + "close.png");
            case ADDKVP:
                return getIcon(ICON_ROOT + "icon_add.png");
            case BROWSER:
                return getIcon(ICON_ROOT + "icon_gbrowse.png");
            case BAMFILE:
                return getIcon(ICON_ROOT + "icon_bam.png");
            case INSPECTOR:
                return getIcon(ICON_ROOT + "inspector2.png");
            case CLEAR:
                return getIcon(ICON_ROOT + "clear.png");
            case MENU_NOTIFY:
                return getIcon(ICON_ROOT + "menu-notify.png");
            case CONFIGURE:
                return getIcon(ICON_ROOT + "configure-light.png");
            case CHART_SMALL:
                return getIcon(ICON_ROOT + "chart_1.png");
            case MORE:
                return getIcon(ICON_ROOT + "more2.png");
            case COPY:
                return getIcon(ICON_ROOT + "cpy.png");
            case GOOGLE_LOGO:
                return getIcon(ICON_ROOT + "googleLog.png");
            case LINKOUT:
                return getIcon(ICON_ROOT + "linkout.png");
            case FILTER:
                return getIcon(ICON_ROOT + "filter.png");
            case SECTION_CLINIC:
                return getIcon(ICON_ROOT + "menu-clinic.png");
            case SECTION_ADMIN:
                return getIcon(ICON_ROOT + "menu-tune.png");
            case SECTION_OTHER:
                return getIcon(ICON_ROOT + "menu-project.png");
            case SECTION_PATIENTS:
                return getIcon(ICON_ROOT + "section_patients.png");
            case SECTION_VARIANTS:
                return getIcon(ICON_ROOT + "section_variants2.png");
            case MENU_USER:
                return getIcon(ICON_ROOT + "menu-user.png");
            case MENU_STORE:
                return getIcon(ICON_ROOT + "menu-store.png");
            case SECTION_TABLE:
                return getIcon(ICON_ROOT + "menu-search.png");
            case SECTION_DATA:
                return getIcon(ICON_ROOT + "menu-database.png");
            case SECTION_SEARCH:
                return getIcon(ICON_ROOT + "menu-binoculars.png");
            case TRASH:
                return getIcon(ICON_ROOT + "trash.png");
            case DELETE:
                return getIcon(ICON_ROOT + "delete.png");
            case SAVE:
                return getIcon(ICON_ROOT + "save2.png");
            case GREEN:
                return getIcon(ICON_ROOT + "green.png");
            case ORANGE:
                return getIcon(ICON_ROOT + "orange.png");
            case RED:
                return getIcon(ICON_ROOT + "red.png");
            case WHITE:
                return getIcon(ICON_ROOT + "white.png");
            case EXPAND:
                return getIcon(ICON_ROOT + "expand.png");
            case COLLAPSE:
                return getIcon(ICON_ROOT + "collapse.png");
            case ADD_ON_TOOLBAR:
                return getIcon(ICON_ROOT + "mac_add.png");
            case REMOVE_ON_TOOLBAR:
                return getIcon(ICON_ROOT + "mac_remove.png");
            case HISTORY_ON_TOOLBAR:
                return getIcon(ICON_ROOT + "mac_history.png");
            case CLEAR_ON_TOOLBAR:
                return getIcon(ICON_ROOT + "mac_clear.png");
            case SAVE_ON_TOOLBAR:
                return getIcon(ICON_ROOT + "mac_save.png");
            case LOAD_ON_TOOLBAR:
                return getIcon(ICON_ROOT + "mac_load.png");
            case ACTION_ON_TOOLBAR:
                return getIcon(ICON_ROOT + "mac_action.png");
            case ADD:
                return getIcon(ICON_ROOT + "add_f.png");
            case EDIT:
                return getIcon(ICON_ROOT + "mac_edit.png");
            case GEAR:
                return getIcon(ICON_ROOT + "mac_gear.png");
            case NETWORK:
                return getIcon(ICON_ROOT + "mac_link.png");
            case IMPORT:
                return getIcon(ICON_ROOT + "import.png");
            case INFO:
                return getIcon(ICON_ROOT + "info_circle.png");
            case EXPORT:
                return getIcon(ICON_ROOT + "export.png");
            case RESULTS:
                return getIcon(ICON_ROOT + "results.png");
            case CHART:
                return getIcon(ICON_ROOT + "chart.png");
            case LOGGED_IN:
                return getIcon(ICON_ROOT + "loggedin.png");
            case SPIRAL:
                return getIcon(ICON_ROOT + "spiral_green.png");
            case LOGO:
                return getIcon(ICON_ROOT + "medsavantlogo.png");
            case FIRST:
                return getIcon(ICON_ROOT + "first.png");
            case PREVIOUS:
                return getIcon(ICON_ROOT + "previous.png");
            case NEXT:
                return getIcon(ICON_ROOT + "next.png");
            case LAST:
                return getIcon(ICON_ROOT + "last.png");
            case TAB_LEFT:
                return getIcon(ICON_ROOT + "tab_l.png");
            case TAB_RIGHT:
                return getIcon(ICON_ROOT + "tab_r.png");
            case MENU_SERVER:
                return getIcon(ICON_ROOT + "menu-server.png");
            case UNDOCK:
                return getIcon(ICON_ROOT + "export.png");
            case DOCK:
                return getIcon(ICON_ROOT + "import.png");
            case FONT_INCREASE:
                return getIcon(ICON_ROOT + "font_increase.png");
            case FONT_DECREASE:
                return getIcon(ICON_ROOT + "font_decrease.png");
            default:
                return null;
        }
    }
}
