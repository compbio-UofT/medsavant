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
package org.ut.biolab.medsavant.client.patient;

import java.awt.Component;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.app.MultiSectionApp;
import org.ut.biolab.medsavant.client.view.app.AppSubSection;

/**
 *
 * @author mfiume
 */
public class IndividualsPage extends AppSubSection {

    private SplitScreenView view;

    public IndividualsPage(MultiSectionApp parent) {
        super(parent, "Patients");
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            try {
                view = new SplitScreenView(
                        new IndividualListModel(),
                        new IndividualDetailedView(pageName),
                        new IndividualDetailEditor());
                view.setSearchBarEnabled(true);
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Unable to create individuals page: %s", ex);
            }
        }
        view.refresh();
        return view;
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        return new Component[0];
    }
}
