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
package org.ut.biolab.medsavant.variant;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.SimpleVariantFile;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.reference.ReferenceEvent;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;


/**
 *
 * @author Andrew
 */
public class VariantFilesPage extends SubSectionView {
    static final Log LOG = LogFactory.getLog(VariantFilesPage.class);

    private SplitScreenView panel;
    private boolean isLoaded = false;
    private boolean updateRequired = false;
    private boolean showPeekOnUnload = false;

    public VariantFilesPage(SectionView parent) {
        super(parent);
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    if (isLoaded) {
                        update();
                    } else {
                        updateRequired = true;
                    }
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Variant Files";
    }

    @Override
    public JPanel getView(boolean update) {
        if (panel == null || updateRequired) {
            setPanel();
        }
        return panel;
    }

    @Override
    public void viewDidLoad() {
        isLoaded = true;
        showPeekOnUnload = ViewController.getInstance().isPeekRightShown();
        ViewController.getInstance().setPeekRightShown(false);
    }

    @Override
    public void viewDidUnload() {
        isLoaded = false;
        ViewController.getInstance().setPeekRightShown(showPeekOnUnload);
        ThreadController.getInstance().cancelWorkers(getName());
    }

    public void setPanel() {
        panel = new SplitScreenView(
                new SimpleDetailedListModel<SimpleVariantFile>("Variant File") {
                    @Override
                    public SimpleVariantFile[] getData() throws Exception {
                        return MedSavantClient.VariantManager.getUploadedFiles(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID());
                    }
                },
                new VariantFilesDetailedView(getName()),
                new VariantFilesDetailedListEditor());
    }

    public void update() {
        panel.refresh();
    }
}
