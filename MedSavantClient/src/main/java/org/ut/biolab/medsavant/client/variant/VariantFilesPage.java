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
package org.ut.biolab.medsavant.client.variant;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.app.MultiSectionApp;
import org.ut.biolab.medsavant.client.view.app.AppSubSection;


/**
 *
 * @author Andrew
 */
public class VariantFilesPage extends AppSubSection {
    static final Log LOG = LogFactory.getLog(VariantFilesPage.class);

    private SplitScreenView view;
    private boolean updateRequired = false;

    public VariantFilesPage(MultiSectionApp parent) {
        super(parent, "Variant Files");
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    if (loaded) {
                        update();
                    } else {
                        updateRequired = true;
                    }
                }
            }
        });
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = new SplitScreenView(
                    new SimpleDetailedListModel<SimpleVariantFile>("Variant File") {
                        @Override
                        public SimpleVariantFile[] getData() throws Exception {
                            SimpleVariantFile[] files = MedSavantClient.VariantManager.getUploadedFiles(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID());
                            return files;
                        }
                    },
                    new VariantFilesDetailedView(pageName),
                    new VariantFilesDetailedListEditor());
             view.setSearchBarEnabled(true);
        }
        return view;
    }

    @Override
    public void viewWillLoad() {
        super.viewWillLoad();
        update();
    }

    @Override
    public void viewDidUnload() {
        super.viewDidUnload();
    }

    public void update() {
        view.refresh();
    }
}
