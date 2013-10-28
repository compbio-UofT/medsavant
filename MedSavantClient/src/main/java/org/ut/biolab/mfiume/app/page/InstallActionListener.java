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
package org.ut.biolab.mfiume.app.page;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.AppInstallUtils;
import org.ut.biolab.mfiume.app.AppStoreViewManager;

/**
 *
 * @author mfiume
 */
public class InstallActionListener implements ActionListener {

    private boolean doUpdate = false;
    private final AppStoreInstalledPage installedPage;
    private final AppInfo i;
    private final AppStoreViewManager avm;

    public InstallActionListener(AppStoreInstalledPage installedPage, AppInfo i, AppStoreViewManager avm) {
        this.installedPage = installedPage;
        this.i = i;
        this.avm = avm;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (doUpdate) { // must uninstall the app first
            installedPage.queueAppWithNameForUninstallation(i.getName(), false);
        }
        installedPage.queueAppForInstallation(i);
        avm.switchToPage(installedPage);
    }

    void setModeToUpdate(boolean b) {
        this.doUpdate = b;
    }
}
