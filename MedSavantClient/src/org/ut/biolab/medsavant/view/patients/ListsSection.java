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

import org.ut.biolab.medsavant.region.RegionPage;
import org.ut.biolab.medsavant.variant.VariantFilesPage;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;

/**
 *
 * @author mfiume
 */
public class ListsSection extends SectionView {

    @Override
    public String getName() {
        return "Tables";
    }

    @Override
    public SubSectionView[] getSubSections() {
        return new SubSectionView[] {
            new IndividualsPage(this),
            new CohortsPage(this),
            new RegionPage(this),
            new VariantFilesPage(this)
        };
    }
}
