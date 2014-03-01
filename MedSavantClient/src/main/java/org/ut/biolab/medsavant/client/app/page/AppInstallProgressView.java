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
package org.ut.biolab.medsavant.client.app.page;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.client.app.AppInfo;
import org.ut.biolab.medsavant.client.app.jAppStore;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AppInstallProgressView extends JPanel {

    public AppInstallProgressView(AppInfo i) {
        this.setBackground(Color.white);
        int padding = 10;
        this.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));

        this.add(new JLabel("<html><b>" + i.getName() + "</b> " + i.getVersion() + " <small> by " + i.getAuthor() + "</small></html>"));
        this.add(Box.createHorizontalGlue());
        this.add(getIndeterminantProgressBar());
        jAppStore.wrapComponentWithLineBorder(this);
    }

    private ProgressWheel getIndeterminantProgressBar() {

        return ViewUtil.getIndeterminateProgressBar();

    }

}
