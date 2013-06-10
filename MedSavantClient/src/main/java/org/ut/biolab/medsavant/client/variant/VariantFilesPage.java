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
package org.ut.biolab.medsavant.client.variant;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.view.ViewController;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;


/**
 *
 * @author Andrew
 */
public class VariantFilesPage extends SubSectionView {
    static final Log LOG = LogFactory.getLog(VariantFilesPage.class);

    private SplitScreenView view;
    private boolean updateRequired = false;
    private boolean showPeekOnUnload = false;

    public VariantFilesPage(SectionView parent) {
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


                            SimpleVariantFile[] files = MedSavantClient.VariantManager.getUploadedFiles(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID());
                            return files;
                        }
                    },
                    new VariantFilesDetailedView(pageName),
                    new VariantFilesDetailedListEditor());
        }
        return view;
    }

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();
        showPeekOnUnload = ViewController.getInstance().isPeekRightShown();
        ViewController.getInstance().setPeekRightShown(false);
    }

    @Override
    public void viewDidUnload() {
        ViewController.getInstance().setPeekRightShown(showPeekOnUnload);
        super.viewDidUnload();
    }

    public void update() {
        view.refresh();
    }
}
