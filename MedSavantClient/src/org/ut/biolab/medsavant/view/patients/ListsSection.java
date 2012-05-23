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

package org.ut.biolab.medsavant.view.patients;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.region.RegionPage;
import org.ut.biolab.medsavant.view.manage.VariantFilesPage;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class ListsSection extends SectionView {

    private SubSectionView[] pages;
    {
        pages = new SubSectionView[4];
        pages[0] = new IndividualsPage(this);
        pages[1] = new CohortsPage(this);
        pages[2] = new RegionPage(this);
        pages[3] = new VariantFilesPage(this);

     }

    @Override
    public String getName() {
        return "Tables";
    }

    @Override
    public Icon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_PATIENTS);
    }

    @Override
    public SubSectionView[] getSubSections() {
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return null;
    }

}
