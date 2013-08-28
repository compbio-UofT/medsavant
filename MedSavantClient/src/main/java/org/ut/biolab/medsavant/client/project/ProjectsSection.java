/*
 *    Copyright 2012 University of Toronto
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
package org.ut.biolab.medsavant.client.project;

import javax.swing.ImageIcon;
import org.ut.biolab.medsavant.client.cohort.CohortsPage;
import org.ut.biolab.medsavant.client.patient.IndividualsPage;
import org.ut.biolab.medsavant.client.region.RegionPage;
import org.ut.biolab.medsavant.client.variant.VariantFilesPage;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;

/**
 * Section which displays information about the current project.
 *
 * @author tarkvara
 */
public class ProjectsSection extends SectionView {

    private SubSectionView[] subSections;

    public ProjectsSection() {
        super("Project");//ProjectController.getInstance().getCurrentProjectName());
    }

    @Override
    public SubSectionView[] getSubSections() {
        if (subSections == null) {
            subSections = new SubSectionView[]{
                new ProjectSummaryPage(this),
                new IndividualsPage(this),
                new CohortsPage(this),
                new RegionPage(this),
                new VariantFilesPage(this)
            };
        }
        return subSections;
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_OTHER);
    }
}
