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
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ProjectWorker;
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
        setResizable(true);
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
            JLabel nameLabel = new JLabel(f.getPath());
            nameLabel.setFont(ViewUtil.getMediumTitleFont());
            page.addComponent(nameLabel);
        }

        return page;
    }

    private AbstractWizardPage getQueuePage() {
        //setup page
        return new DefaultWizardPage("Remove & Publish Variants") {
            private final JLabel progressLabel = new JLabel("You are now ready to remove variants.");
            private final JButton workButton = new JButton("Remove Files");

            {
                addComponent(progressLabel);
                addComponent(ViewUtil.alignRight(workButton));

                final JComponent j = new JLabel("<html>You may continue. The removal process will continue in the<br>background and you will be notified upon completion.</html>");
                addComponent(j);
                j.setVisible(false);


                workButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        j.setVisible(true);
                        fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                        fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                        // progressBar.setIndeterminate(true);

                        workButton.setEnabled(false);
                        new ProjectWorker<Void>("Removing variants", autoPublish.isSelected(), LoginController.getSessionID(), projectID) {
                            @Override
                            protected Void runInBackground() throws Exception {
                                MedSavantClient.VariantManager.removeVariants(LoginController.getSessionID(), projectID, referenceID, files, autoPublish.isSelected(), emailField.getText());
                                return null;
                            }
                        }.execute();

                        toFront();
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
