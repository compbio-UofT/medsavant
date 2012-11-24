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

package org.ut.biolab.medsavant.patient;

import java.awt.Component;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class IndividualsPage extends SubSectionView {
    private SplitScreenView view;

    public IndividualsPage(SectionView parent) {
        super(parent, "Individuals");
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            try {
                view = new SplitScreenView(
                        new IndividualListModel(),
                        new IndividualDetailedView(pageName),
                        new IndividualDetailEditor());
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Unable to create individuals page: %s", ex);
            }
        }
        return view;
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        return new Component[0];
    }
}
