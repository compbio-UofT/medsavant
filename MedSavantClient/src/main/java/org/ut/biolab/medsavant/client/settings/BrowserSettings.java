/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.settings;

import org.ut.biolab.medsavant.client.controller.SettingsController;

/**
 *
 * @author mfiume
 */
public class BrowserSettings {

    private static final String CHECKVERSION_KEY = "CHECKVERSION";
    private static final String COLLECTSTATS_KEY = "COLLECTSTATS";

    private static SettingsController settings = SettingsController.getInstance();

    /**
     * Does Savant need to check it's version number on startup?  Usually controlled by the
     * CHECKVERSION key, except in the case of beta releases, which always check.
     */
    public static boolean getCheckVersionOnStartup() {
        return VersionSettings.isBeta() || settings.getBoolean(CHECKVERSION_KEY, true);
    }

    public static boolean getCollectAnonymousUsage() {
        return settings.getBoolean(COLLECTSTATS_KEY, true);
    }

    public static boolean getCachingEnabled() {
        return true;
    }

}
