/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.app.page;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.client.app.AppInfo;

/**
 *
 * @author mfiume
 */
public class InstallActionListener implements ActionListener {

    private boolean doUpdate = false;
    private final AppStoreInstalledPage installedPage;
    private final AppInfo i;
    private final JTabbedPane parent;

    public InstallActionListener(AppStoreInstalledPage installedPage, AppInfo i, JTabbedPane parent) {
        this.installedPage = installedPage;
        this.i = i;
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (doUpdate) { // must uninstall the app first
            installedPage.queueAppWithNameForUninstallation(i.getName(), false);
        }
        installedPage.queueAppForInstallation(i);
        //parent.setSelectedIndex(1);
    }

    void setModeToUpdate(boolean b) {
        this.doUpdate = b;
    }
}
