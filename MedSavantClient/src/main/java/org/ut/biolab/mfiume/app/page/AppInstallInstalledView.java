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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.api.AppInstaller;
import org.ut.biolab.mfiume.app.jAppStore;

/**
 *
 * @author mfiume
 */
class AppInstallInstalledView extends JPanel {

    private final AppInfo appInfo;
    private final AppInstaller installer;
    private final AppStoreInstalledPage installPage;
    private final State state;

    public AppInstallInstalledView(AppInfo i, AppInstaller installer, AppStoreInstalledPage parent, State state) {
        this.appInfo = i;
        this.installer = installer;
        this.installPage = parent;
        this.state = state;

        this.setBackground(Color.white);
        int padding = 10;
        this.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.add(new JLabel("<html><b>" + i.getName() + "</b> " + i.getVersion() + "</html>"));
        this.add(Box.createHorizontalGlue());
        this.add(getUninstallButton(state));

        jAppStore.wrapComponentWithLineBorder(this);
    }

    private JButton getUninstallButton(State state) {
        JButton b = AppInfoFlowView.getSoftButton("Uninstall");

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                installPage.queueAppWithNameForUninstallation(appInfo.getName());
            }
        });

        switch(state) {
            case INSTALLED:
                break;
            case JUST_INSTALLED:
                b.setEnabled(false);
                b.setText("Installed");
                break;
            case JUST_UNINSTALLED:
                b.setEnabled(false);
                b.setText("Uninstalled");
                break;
            default:
                throw new AssertionError(state.name());
        }
        return b;

    }

    static enum State {
        JUST_INSTALLED,
        JUST_UNINSTALLED,
        INSTALLED
    }
}
