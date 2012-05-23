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
package org.ut.biolab.medsavant.view.manage;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
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
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.model.SimpleVariantFile;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class RemoveVariantsWizard extends WizardDialog {
    private static final Log LOG = LogFactory.getLog(RemoveVariantsWizard.class);
    private int projectId;
    private int referenceId;
    private List<SimpleVariantFile> files;
    private Thread uploadThread = null;
    private Thread publishThread = null;
    private int updateId;
    
    public RemoveVariantsWizard(List<SimpleVariantFile> files) {
        this.projectId = ProjectController.getInstance().getCurrentProjectID();
        this.referenceId = ReferenceController.getInstance().getCurrentReferenceId();
        this.files = files;
        
        if (files.isEmpty()) return;
        
        //check for existing unpublished changes to this project + reference
        try {           
            if (MedSavantClient.ProjectQueryUtilAdapter.existsUnpublishedChanges(LoginController.sessionId, projectId, referenceId)) {
                DialogUtils.displayMessage("Cannot perform removal", "There are unpublished changes to this table. Please publish and then try again.");
                return;
            }
        } catch (Exception ex) {
            DialogUtils.displayErrorMessage("Error checking for changes. ", ex);
            return;
        }
        
        //get lock
        try {            
            if (!MedSavantClient.SettingsQueryUtilAdapter.getDbLock(LoginController.sessionId)) {
                DialogUtils.displayMessage("Cannot perform removal", "Another user is making changes to the database. You must wait until this user has finished. ");
                return;
            }
        } catch (Exception ex) {
            DialogUtils.displayErrorMessage("Error getting database lock", ex);
            return;
        }

        catchClosing();
        setupWizard();
    }
    
    private void setupWizard() {   
        setTitle("Remove Variants Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        //add pages
        PageList model = new PageList();
        model.append(getWelcomePage());
        model.append(getQueuePage());
        model.append(getCompletePage());
        setPageList(model);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void catchClosing() {
        this.addWindowListener(new WindowAdapter() {       
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    MedSavantClient.SettingsQueryUtilAdapter.releaseDbLock(LoginController.sessionId);
                } catch (Exception ex) {
                    LOG.error("Error releasing DB lock.", ex);
                }
            }
        });
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

        page.addText(
                "This wizard will help you remove the following variant files from \nproject " + projectName + " and reference " + referenceName + ": ");

        for(SimpleVariantFile f : files) {
            JLabel nameLabel = new JLabel(f.getName());
            nameLabel.setFont(ViewUtil.getMediumTitleFont());
            page.addComponent(nameLabel);
        }

        return page;
    }
    
    private AbstractWizardPage getQueuePage() {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Remove & Publish Variants") {

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("You are now ready to remove variants.");

        final JLabel progressLabel = new JLabel("Ready to remove variant files.");
        final JProgressBar progressBar = new JProgressBar();

        page.addComponent(progressLabel);
        page.addComponent(progressBar);


        final JButton startButton = new JButton("Remove Files");
        final JButton publishStartButton = new JButton("Publish Variants");

        final JButton cancelButton = new JButton("Cancel");
        final JButton publishCancelButton = new JButton("Cancel");

        final JCheckBox autoPublishVariants = new JCheckBox("Automatically publish variants after removal");

        final JLabel publishProgressLabel = new JLabel("Ready to publish variants.");
        final JProgressBar publishProgressBar = new JProgressBar();


        publishStartButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                publishProgressBar.setIndeterminate(true);
                publishProgressLabel.setText("Publishing variants...");

                publishThread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                            // do stuff
                            MedSavantClient.VariantManager.publishVariants(LoginController.sessionId, projectId, referenceId, updateId);
                            
                            //success
                            publishProgressBar.setIndeterminate(false);
                            publishCancelButton.setVisible(false);
                            publishProgressBar.setValue(100);
                            publishProgressLabel.setText("Publish complete.");

                            page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);


                        } catch (Exception ex) {

                            //cancellation
                            if (ex instanceof InterruptedException) {
                                publishProgressBar.setIndeterminate(false);
                                publishProgressBar.setValue(0);
                                publishProgressLabel.setText("Publish cancelled.");
                                publishStartButton.setVisible(true);
                                publishStartButton.setEnabled(true);
                                publishCancelButton.setText("Cancel");
                                publishCancelButton.setEnabled(true);
                                publishCancelButton.setVisible(false);

                                //failure
                            } else {
                                ClientMiscUtils.checkSQLException(ex);
                                publishProgressBar.setIndeterminate(false);
                                publishProgressBar.setValue(0);
                                publishProgressLabel.setForeground(Color.red);
                                publishProgressLabel.setText(ex.getMessage());
                                publishStartButton.setVisible(false);
                                publishCancelButton.setVisible(false);
                            }
                            LOG.error("Error publishing variants.", ex);
                        }
                    }
                };

                publishCancelButton.setVisible(true);
                publishStartButton.setVisible(false);
                publishThread.start();
            }
        });


        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                progressBar.setIndeterminate(true);
                startButton.setEnabled(false);
                startButton.setVisible(false);

                uploadThread = new Thread() {

                    @Override
                    public void run() {
                        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

                        try {

                            //remove variants
                            progressLabel.setText("Removing variant files...");
                            updateId = MedSavantClient.VariantManager.removeVariants(LoginController.sessionId, projectId, referenceId, files);
                            MedSavantClient.SettingsQueryUtilAdapter.releaseDbLock(LoginController.sessionId);

                            //success
                            progressBar.setIndeterminate(false);
                            cancelButton.setEnabled(false);
                            cancelButton.setVisible(false);
                            progressBar.setValue(100);
                            progressLabel.setText("Removal complete.");

                            publishProgressLabel.setVisible(true);
                            publishProgressBar.setVisible(true);

                            autoPublishVariants.setVisible(false);

                            if (autoPublishVariants.isSelected()) {

                                publishProgressLabel.setText("Publishing variants...");

                                // publish
                                MedSavantClient.VariantManager.publishVariants(LoginController.sessionId, projectId, referenceId, updateId);

                                //success
                                publishProgressBar.setIndeterminate(false);
                                publishCancelButton.setVisible(false);
                                publishProgressBar.setValue(100);
                                publishProgressLabel.setText("Publish complete.");

                                page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);

                            } else {
                                publishStartButton.setVisible(true);
                                page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                            }

                        } catch (Exception ex) {

                            //release lock always
                            try {
                                MedSavantClient.SettingsQueryUtilAdapter.releaseDbLock(LoginController.sessionId);
                            } catch (Exception ex1) {
                                LOG.error("Error releasing database lock.", ex);
                            }

                            //cancellation
                            if (ex instanceof InterruptedException) {
                                progressBar.setIndeterminate(false);
                                progressBar.setValue(0);
                                progressLabel.setText("Removal cancelled.");
                                startButton.setVisible(true);
                                startButton.setEnabled(true);
                                cancelButton.setText("Cancel");
                                cancelButton.setEnabled(true);
                                cancelButton.setVisible(false);

                                //failure
                            } else {
                                ClientMiscUtils.checkSQLException(ex);
                                progressBar.setIndeterminate(false);
                                progressBar.setValue(0);
                                progressLabel.setForeground(Color.red);
                                progressLabel.setText(ex.getMessage());
                                startButton.setVisible(false);
                                cancelButton.setVisible(false);
                            }
                            LOG.error("Error publishing variants.", ex);
                        }

                        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    }

                };
                cancelButton.setVisible(true);
                startButton.setVisible(false);
                uploadThread.start();
            }
        });

        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButton.setText("Cancelling...");
                cancelButton.setEnabled(false);
                uploadThread.interrupt();
            }
        });

        publishCancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                publishCancelButton.setText("Cancelling...");
                publishCancelButton.setEnabled(false);
                publishThread.interrupt();
            }
        });

        page.addComponent(ViewUtil.alignRight(startButton));
        cancelButton.setVisible(false);
        page.addComponent(ViewUtil.alignRight(cancelButton));

        page.addComponent(autoPublishVariants);
        JLabel l = new JLabel("WARNING:");
        l.setForeground(Color.red);
        l.setFont(new Font(l.getFont().getFamily(), Font.BOLD, l.getFont().getSize()));
        page.addComponent(l);
        page.addText("All users logged into the system will be "
                + "logged out\nat the time of publishing.");


        page.addComponent(publishProgressLabel);
        page.addComponent(publishProgressBar);
        page.addComponent(ViewUtil.alignRight(publishStartButton));
        page.addComponent(ViewUtil.alignRight(publishCancelButton));

        publishStartButton.setVisible(false);
        publishProgressLabel.setVisible(false);
        publishProgressBar.setVisible(false);
        publishCancelButton.setVisible(false);


        return page;
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

        page.addText("You have finished removing variants.");

        return page;
    }
    
}
