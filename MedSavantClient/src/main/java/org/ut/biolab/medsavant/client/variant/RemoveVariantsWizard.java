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

import com.jidesoft.dialog.AbstractDialogPage;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class RemoveVariantsWizard extends WizardDialog {
    private static final Log LOG = LogFactory.getLog(RemoveVariantsWizard.class);

    private final int projectID;
    private final int referenceID;
    private final List<SimpleVariantFile> files;
    private JTextField emailField;
    private JCheckBox autoPublish;

    public RemoveVariantsWizard(List<SimpleVariantFile> files) {
        this.projectID = ProjectController.getInstance().getCurrentProjectID();
        this.referenceID = ReferenceController.getInstance().getCurrentReferenceID();
        this.files = files;

        setTitle("Remove Variants Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        //add pages
        PageList model = new PageList();
        model.append(getWelcomePage());
        model.append(getNotificationsPage());
        model.append(getQueuePage());
        model.append(getCompletePage());
        setPageList(model);

        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());
    }

    private AbstractWizardPage getWelcomePage() {

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Remove Variants") {

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        String projectName = ProjectController.getInstance().getCurrentProjectName();
        String referenceName = ReferenceController.getInstance().getCurrentReferenceName();

        page.addText("This wizard will help you remove the following variant files from \nproject " + projectName + " and reference " + referenceName + ": ");

        for (SimpleVariantFile f : files) {
            JLabel nameLabel = new JLabel(f.getName());
            nameLabel.setFont(ViewUtil.getMediumTitleFont());
            page.addComponent(nameLabel);
        }

        return page;
    }

    private AbstractWizardPage getQueuePage() {
        //setup page
        return new DefaultWizardPage("Remove & Publish Variants") {
            private final JLabel progressLabel = new JLabel("You are now ready to remove variants.");
            private final JProgressBar progressBar = new JProgressBar();
            private final JButton workButton = new JButton("Remove Files");
            /*private final JButton publishButton = new JButton("Publish Variants");
            private final JCheckBox autoPublishVariants = new JCheckBox("Automatically publish variants after removal");
            private final JLabel publishProgressLabel = new JLabel("Ready to publish variants.");
            private final JProgressBar publishProgressBar = new JProgressBar();*/

            {
                addComponent(progressLabel);
                addComponent(progressBar);
                addComponent(ViewUtil.alignRight(workButton));

                final JComponent j = new JLabel("<html>You may continue. The removal process will continue in the<br>background and you will be notified upon completion.</html>");
                addComponent(j);
                j.setVisible(false);

                /*addComponent(autoPublishVariants);
                JLabel l = new JLabel("WARNING:");
                l.setForeground(Color.red);
                l.setFont(l.getFont().deriveFont(Font.BOLD));
                addComponent(l);
                addText("All users logged into the system will be logged out\nat the time of publishing.");

                addComponent(publishProgressLabel);
                addComponent(publishProgressBar);
                addComponent(ViewUtil.alignRight(publishButton));

                publishButton.setVisible(false);
                publishProgressLabel.setVisible(false);
                publishProgressBar.setVisible(false);*/

                final boolean ap = autoPublish.isSelected();
                final String email = emailField.getText();

                workButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        j.setVisible(true);
                        fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                        fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                        progressBar.setIndeterminate(true);
                        new UpdateWorker("Removing variants", RemoveVariantsWizard.this, progressLabel, progressBar, workButton) {
                            @Override
                            protected Void doInBackground() throws Exception {
                                updateID = MedSavantClient.VariantManager.removeVariants(LoginController.getInstance().getSessionID(), projectID, referenceID, files, ap, email);
                                return null;
                            }
                        }.execute();
                    }
                });

            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };
    }

    private AbstractWizardPage getCompletePage() {

        final CompletionWizardPage page = new CompletionWizardPage("Complete") {

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("You have finished requesting variant file removal.");

        return page;
    }

   private AbstractWizardPage getNotificationsPage() {
        final DefaultWizardPage page = new DefaultWizardPage("Notifications") {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("Project modification may take some time. Enter your email address to be notified when the process completes.");

        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(p);
        JLabel l = new JLabel("Email: ");
        emailField = new JTextField();
        p.add(l);
        p.add(emailField);
        page.addComponent(p);

        autoPublish = new JCheckBox("Automatically publish data upon import completion");
        autoPublish.setSelected(true);
        page.addComponent(autoPublish);
        page.addText("If you choose not to automatically publish, you will be prompted to publish manually upon completion. Variant publication logs all users out.");

        return page;
    }

}
