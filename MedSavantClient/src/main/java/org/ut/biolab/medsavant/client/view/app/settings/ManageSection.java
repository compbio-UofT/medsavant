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
package org.ut.biolab.medsavant.client.view.app.settings;

import javax.swing.ImageIcon;
import org.ut.biolab.medsavant.client.geneset.GeneSetPage;
import org.ut.biolab.medsavant.client.ontology.OntologyPage;
import org.ut.biolab.medsavant.client.project.ProjectManagementPage;
import org.ut.biolab.medsavant.client.reference.ReferenceGenomePage;
import org.ut.biolab.medsavant.client.user.UserManagementPage;
import org.ut.biolab.medsavant.client.variant.VariantFilesPage;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.subview.AppSubSection;
import org.ut.biolab.medsavant.client.view.subview.MultiSectionApp;

/**
 *
 * @author mfiume
 */
public class ManageSection extends MultiSectionApp {

    public ManageSection() {
        super("Admin");
    }

    @Override
    public AppSubSection[] getSubSections() {
        return new AppSubSection[] {
            new UserManagementPage(this),
            new ProjectManagementPage(this),
            new VariantFilesPage(this),
            new AnnotationsPage(this),
            new OntologyPage(this),
            new ReferenceGenomePage(this),
            new GeneSetPage(this),
            new ServerLogPage(this) };
    }

    @Override
        public ImageIcon getIcon() {
            return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_ADMIN);
        }
}
