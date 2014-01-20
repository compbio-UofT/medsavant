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
package org.ut.biolab.medsavant.client.view.subview;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.util.PeekingPanel;

/**
 *
 * @author mfiume
 */
public abstract class MultiSectionApp implements DashboardApp {

    private final String name;
    private JPanel view;
    private AppSubSection currentSubSection;

    protected MultiSectionApp(String name) {
        this.name = name;

    }

    @Override
    public final String getName() {
        return name;
    }

    public abstract AppSubSection[] getSubSections();

    public JPanel[] getPersistentPanels() {
        return null;
    }

    public Component[] getSectionMenuComponents() {
        return null;
    }

    @Override
    public abstract ImageIcon getIcon();

    @Override
    public JPanel getView() {
        if (view == null) {

            view = new JPanel();
            view.setLayout(new BorderLayout());

            JTabbedPane tabs = new JTabbedPane();
            tabs.setFocusable(false);

            AppSubSection[] subsections = getSubSections();
            for (AppSubSection sub : subsections) {
                tabs.add(sub.getPageName(), sub.getView());
            }
            if (subsections.length > 0) {
                currentSubSection = subsections[0];
            }

            if (getPersistentPanels() != null
                    && getPersistentPanels().length > 0) {
                PeekingPanel pp = new PeekingPanel("", BorderLayout.EAST, getPersistentPanels()[0], true);
                pp.setToggleBarVisible(false);

                view.add(pp, BorderLayout.WEST);
            }

            view.add(tabs, BorderLayout.CENTER);

        }
        return view;
    }

    @Override
    public void viewWillUnload() {

    }

    @Override
    public void viewWillLoad() {
    }

    @Override
    public void viewDidUnload() {
        if (currentSubSection != null) {
            currentSubSection.viewDidUnload();
        }
    }

    @Override
    public void viewDidLoad() {
        if (currentSubSection != null) {
            currentSubSection.viewWillLoad();
        }
    }

    @Override
    public void didLogout() {
        if (currentSubSection != null) {
            currentSubSection.didLogout();
        }
    }

    @Override
    public void didLogin() {
        if (currentSubSection != null) {
            currentSubSection.didLogin();
        }
    }

}
