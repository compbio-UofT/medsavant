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
package org.ut.biolab.medsavant.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.clinic.ClinicSection;

import org.ut.biolab.medsavant.client.cohort.CohortsPage;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.client.patient.IndividualsPage;
import org.ut.biolab.medsavant.client.project.ProjectsSection;
import org.ut.biolab.medsavant.client.region.RegionPage;
import org.ut.biolab.medsavant.client.variant.VariantFilesPage;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsSection;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.app.settings.ManageSection;
import org.ut.biolab.medsavant.client.view.subview.MultiSection;
import org.ut.biolab.medsavant.client.view.subview.SubSection;


/**
 *
 * @author mfiume
 */
public class LoggedInView extends JPanel {
    private ViewController viewController;

    public LoggedInView() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        viewController = ViewController.loggedIn(this);
        viewController.clearMenu();

        viewController.addSection(new ProjectsSection());
        viewController.addSection(new GeneticsSection());

        viewController.addSection(new ClinicSection());

        if (LoginController.getInstance().getUserLevel() == UserLevel.ADMIN) {
            viewController.addSection(new ManageSection());
        }

        viewController.selectFirstItem();
    }

    private class ListsSection extends MultiSection {
        private ListsSection() {
            super("Tables");
        }

        @Override
        public SubSection[] getSubSections() {
            return new SubSection[] {
                new IndividualsPage(this),
                new CohortsPage(this),
                new RegionPage(this),
                new VariantFilesPage(this)
            };
        }

        @Override
        public ImageIcon getIcon() {
            return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_DATA);
        }
    }
}
