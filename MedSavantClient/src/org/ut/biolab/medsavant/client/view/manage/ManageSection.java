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

package org.ut.biolab.medsavant.client.view.manage;

import javax.swing.ImageIcon;
import org.ut.biolab.medsavant.client.geneset.GeneSetPage;
import org.ut.biolab.medsavant.client.ontology.OntologyPage;
import org.ut.biolab.medsavant.client.project.ProjectManagementPage;
import org.ut.biolab.medsavant.client.reference.ReferenceGenomePage;
import org.ut.biolab.medsavant.client.user.UserManagementPage;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class ManageSection extends SectionView {

    public ManageSection() {
        super("Admin");
    }

    @Override
    public SubSectionView[] getSubSections() {
        return new SubSectionView[] {
            new UserManagementPage(this),
            new ProjectManagementPage(this),
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
