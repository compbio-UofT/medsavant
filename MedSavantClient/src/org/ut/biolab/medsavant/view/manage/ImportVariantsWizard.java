/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.model.VariantTag;
import org.ut.biolab.medsavant.db.util.shared.ExtensionsFileFilter;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class ImportVariantsWizard extends WizardDialog {

    private int projectId;
    private int referenceId;
    private JComboBox locationField;
    private List<VariantTag> variantTags;
    private File[] variantFiles;
    private Thread uploadThread = null;
    private Thread publishThread = null;

    public ImportVariantsWizard() {
        
        this.projectId = ProjectController.getInstance().getCurrentProjectId();
        this.referenceId = ReferenceController.getInstance().getCurrentReferenceId();
        
        //check for existing unpublished changes to this project + reference
        try {           
            if(MedSavantClient.ProjectQueryUtilAdapter.existsUnpublishedChanges(LoginController.sessionId, projectId, referenceId)){
                DialogUtils.displayMessage("Cannot perform import", "There are unpublished changes to this table. Please publish and then try again.");
                return;
            }
        } catch (Exception ex) {
            DialogUtils.displayErrorMessage("Error checking for changes. ", ex);
            return;
        }
        
        //get lock
        try {            
            if(!MedSavantClient.SettingsQueryUtilAdapter.getDbLock(LoginController.sessionId)){
                DialogUtils.displayMessage("Cannot perform import", "Another user is making changes to the database. You must wait until this user has finished. ");
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
        setTitle("Import Variants Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        variantTags = new ArrayList<VariantTag>();

        //add pages
        PageList model = new PageList();
        model.append(getWelcomePage());
        model.append(getChooseFilesPage());
        model.append(getAddTagsPage());
        model.append(getQueuePage());
        //model.append(getSetLivePage());
        model.append(getCompletePage());
        setPageList(model);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void catchClosing(){
        this.addWindowListener(new WindowAdapter() {       
            public void windowClosed(WindowEvent e){
                try {
                    MedSavantClient.SettingsQueryUtilAdapter.releaseDbLock(LoginController.sessionId);
                } catch (Exception ex) {
                    Logger.getLogger(ImportVariantsWizard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private AbstractWizardPage getWelcomePage() {

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Import Variants") {

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
                "This wizard will help you import variants for:");

        JLabel nameLabel = new JLabel(projectName + " (" + referenceName + ")");
        nameLabel.setFont(ViewUtil.getMediumTitleFont());
        page.addComponent(nameLabel);
        page.addText("If the variants are with respect to another reference\ngenome, switch to that reference and try importing again.");

        return page;
    }

    private AbstractWizardPage getChooseFilesPage() {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Choose Files") {

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                if (variantFiles != null && variantFiles.length > 0) {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        };

        page.addText("Choose the variant file(s) to be imported:");

        final JTextField outputFileField = new JTextField();
        outputFileField.setEnabled(false);
        JButton chooseFileButton = new JButton("...");
        chooseFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                variantFiles = DialogUtils.chooseFilesForOpen("Import Variants", new ExtensionsFileFilter(new String[]{"vcf", "vcf.gz"}), null);
                if (variantFiles == null || variantFiles.length == 0) {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
                String path = getPathString(variantFiles);
                outputFileField.setText(path);
                if (variantFiles.length > 0) {
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
            }

            private String getPathString(File[] files) {
                if (files.length > 1) {
                    return files.length + " files";
                } else if (files.length == 1) {
                    return files[0].getAbsolutePath();
                } else {
                    return "";
                }
            }
        });
        JPanel container = new JPanel();
        ViewUtil.clear(container);
        ViewUtil.applyHorizontalBoxLayout(container);

        container.add(outputFileField);
        container.add(chooseFileButton);

        page.addComponent(container);
        page.addText("Files can be in Variant Call Format (*.vcf) or BGZipped\nVCF (*.vcf.gz).");

        return page;

    }
    private static final Dimension LOCATION_SIZE = new Dimension(150, 22);

    private AbstractWizardPage getAddTagsPage() {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Add Tags") {

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("Variants can be filtered by tag value "
                + "in the Filter section.");


        page.addText("Add tags for this set of variants:");

        final String[] patternExamples = {
            "<Tag Name>",
            "Sequencer",
            "Sequencer Version",
            "Variant Caller",
            "Variant Caller Version",
            "Technician"
        };

        locationField = new JComboBox(patternExamples);
        locationField.setEditable(true);

        final JPanel tagContainer = new JPanel();
        ViewUtil.applyVerticalBoxLayout(tagContainer);

        final JTextField valueField = new JTextField();


        final String startingValue = "<Value>";
        valueField.setText(startingValue);

        final JTextArea ta = new JTextArea();
        ta.setRows(10);
        ta.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        ta.setEditable(false);

        JLabel button = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (locationField.getSelectedItem().toString().isEmpty()) {
                    DialogUtils.displayError("Tag cannot be empty");
                    locationField.requestFocus();
                    return;
                } else if (locationField.getSelectedItem().toString().equals(patternExamples[0])) {
                    DialogUtils.displayError("Enter a valid tag name");
                    locationField.requestFocus();
                    return;
                }

                if (valueField.getText().toString().isEmpty()) {
                    DialogUtils.displayError("Value cannot be empty");
                    valueField.requestFocus();
                    return;
                } else if (valueField.getText().equals(startingValue)) {
                    DialogUtils.displayError("Enter a valid value");
                    valueField.requestFocus();
                    return;
                }

                VariantTag tag = new VariantTag((String) locationField.getSelectedItem(), valueField.getText());


                variantTags.add(tag);
                ta.append(tag.toString() + "\n");
                valueField.setText("");
            }
        });

        JPanel container2 = new JPanel();
        ViewUtil.clear(container2);
        ViewUtil.applyHorizontalBoxLayout(container2);
        container2.add(locationField);
        container2.add(ViewUtil.clear(new JLabel(" = ")));
        container2.add(valueField);
        container2.add(button);

        page.addComponent(container2);
        locationField.setToolTipText("Current display range");

        locationField.setPreferredSize(LOCATION_SIZE);
        locationField.setMinimumSize(LOCATION_SIZE);

        valueField.setPreferredSize(LOCATION_SIZE);
        valueField.setMinimumSize(LOCATION_SIZE);

        //tagContainer.setPreferredSize(new Dimension(900,10));
        //tagContainer.setBorder(BorderFactory.createTitledBorder("Tags"));
        page.addComponent(tagContainer);

        page.addComponent(new JScrollPane(ta));

        JButton clear = new JButton("Clear");
        clear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                variantTags.clear();
                ta.setText("");
                addDefaultTags(variantTags, ta);
            }
        });

        addDefaultTags(variantTags, ta);

        page.addComponent(ViewUtil.alignRight(clear));

        return page;

    }

    private void addDefaultTags(List<VariantTag> variantTags, JTextArea ta) {

        VariantTag tag1 = new VariantTag("Uploader", LoginController.getUsername());
        VariantTag tag2 = new VariantTag("Upload Date", (new Date()).toString());
        variantTags.add(tag1);
        variantTags.add(tag2);
        ta.append(tag1.toString() + "\n");
        ta.append(tag2.toString() + "\n");
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

        page.addText("You have finished importing variants.");

        return page;
    }
    private int updateID;

    private AbstractWizardPage getQueuePage() {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Upload, Annotate, & Publish Variants") {

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("You are now ready to upload variants.");

        final JLabel progressLabel = new JLabel("Ready to upload and annotate variant files.");
        final JProgressBar progressBar = new JProgressBar();

        page.addComponent(progressLabel);
        page.addComponent(progressBar);


        //final JButton cancelButton = new JButton("Cancel");
        final JButton startButton = new JButton("Upload & Annotate");
        final JButton publishStartButton = new JButton("Publish Variants");

        final JButton cancelButton = new JButton("Cancel");
        final JButton publishCancelButton = new JButton("Cancel");

        final JCheckBox autoPublishVariants = new JCheckBox("Automatically publish variants after upload");

        final JLabel publishProgressLabel = new JLabel("Ready to publish variants.");
        final JProgressBar publishProgressBar = new JProgressBar();


        final JDialog instance = this;

        /*
         *
        publishProgressLabel.setText("Publishing variants...");

        // do stuff
        MedSavantClient.VariantManagerAdapter.publishVariants(LoginController.sessionId,projectId, referenceId, updateID);

        //success
        publishProgressBar.setIndeterminate(false);
        publishCancelButton.setEnabled(false);
        publishProgressBar.setValue(100);
        publishProgressLabel.setText("Upload complete.");
         */

        publishStartButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {

                publishProgressBar.setIndeterminate(true);
                publishProgressLabel.setText("Publishing variants...");

                publishThread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            instance.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                            // do stuff
                            MedSavantClient.VariantManagerAdapter.publishVariants(LoginController.sessionId, projectId, referenceId, updateID);
                            
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
                                if (ex instanceof SQLException) {
                                    MiscUtils.checkSQLException((SQLException) ex);
                                }
                                publishProgressBar.setIndeterminate(false);
                                publishProgressBar.setValue(0);
                                publishProgressLabel.setForeground(Color.red);
                                publishProgressLabel.setText(ex.getMessage());
                                publishStartButton.setVisible(false);
                                publishCancelButton.setVisible(false);
                            }
                            Logger.getLogger(ImportVariantsWizard.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };

                publishCancelButton.setVisible(true);
                publishStartButton.setVisible(false);
                publishThread.start();
            }
        });


        startButton.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                        progressBar.setIndeterminate(true);
                        startButton.setEnabled(false);
                        startButton.setVisible(false);

                        uploadThread = new Thread() {

                            @Override
                            public void run() {
                                instance.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

                                try {
                                    int i = 0;
                                    RemoteInputStream[] streams = new RemoteInputStream[variantFiles.length];
                                    String[] fileNames = new String[variantFiles.length];
                                    for (File file : variantFiles) {
                                        streams[i] = (new SimpleRemoteInputStream(new FileInputStream(file.getAbsolutePath()))).export();
                                        fileNames[i] = file.getName();
                                        i++;
                                    }

                                    //upload variants
                                    progressLabel.setText("Uploading variant files...");
                                    updateID = MedSavantClient.VariantManagerAdapter.uploadVariants(LoginController.sessionId, streams, fileNames, projectId, referenceId, tagsToStringArray(variantTags));
                                    MedSavantClient.SettingsQueryUtilAdapter.releaseDbLock(LoginController.sessionId);

                                    //success
                                    progressBar.setIndeterminate(false);
                                    cancelButton.setEnabled(false);
                                    cancelButton.setVisible(false);
                                    progressBar.setValue(100);
                                    progressLabel.setText("Upload complete.");

                                    publishProgressLabel.setVisible(true);
                                    publishProgressBar.setVisible(true);

                                    autoPublishVariants.setVisible(false);

                                    if (autoPublishVariants.isSelected()) {

                                        publishProgressLabel.setText("Publishing variants...");

                                        // publish
                                        MedSavantClient.VariantManagerAdapter.publishVariants(LoginController.sessionId, projectId, referenceId, updateID);

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
                                        Logger.getLogger(ImportVariantsWizard.class.getName()).log(Level.SEVERE, null, ex1);
                                    }

                                    //cancellation
                                    if (ex instanceof InterruptedException) {
                                        progressBar.setIndeterminate(false);
                                        progressBar.setValue(0);
                                        progressLabel.setText("Upload cancelled.");
                                        startButton.setVisible(true);
                                        startButton.setEnabled(true);
                                        cancelButton.setText("Cancel");
                                        cancelButton.setEnabled(true);
                                        cancelButton.setVisible(false);

                                        //failure
                                    } else {
                                        if (ex instanceof SQLException) {
                                            MiscUtils.checkSQLException((SQLException) ex);
                                        }
                                        progressBar.setIndeterminate(false);
                                        progressBar.setValue(0);
                                        progressLabel.setForeground(Color.red);
                                        progressLabel.setText(ex.getMessage());
                                        startButton.setVisible(false);
                                        cancelButton.setVisible(false);
                                    }
                                    Logger.getLogger(ImportVariantsWizard.class.getName()).log(Level.SEVERE, null, ex);
                                }



                                instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                            }

                        };
                        cancelButton.setVisible(true);
                        startButton.setVisible(false);
                        uploadThread.start();
                    }
                });

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelButton.setText("Cancelling...");
                cancelButton.setEnabled(false);
                uploadThread.interrupt();
            }
        });

        publishCancelButton.addActionListener(new ActionListener() {

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

    private AbstractWizardPage getSetLivePage() {

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Publish variants") {

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("New variants have been uploaded and annotated, but\n"
                + "are not yet available to users. To make them available,\n"
                + "you must Publish Variants.");

        JLabel l = new JLabel("WARNING:");
        l.setForeground(Color.red);
        l.setFont(new Font(l.getFont().getFamily(), Font.BOLD, l.getFont().getSize()));
        page.addComponent(l);
        page.addText("All users currently logged into the system will be\n"
                + "logged out upon publishing variants.");

        final JLabel progressLabel = new JLabel("Ready to publish variants.");
        final JProgressBar progressBar = new JProgressBar();

        page.addComponent(progressLabel);
        page.addComponent(progressBar);

        final JButton cancelButton = new JButton("Cancel");
        final JButton startButton = new JButton("Publish Variants");

        final JDialog instance = this;

        startButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                progressBar.setIndeterminate(true);
                startButton.setEnabled(false);


                uploadThread = new Thread() {

                    @Override
                    public void run() {
                        instance.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        try {

                            progressLabel.setText("Publishing variants...");

                            // do stuff
                            MedSavantClient.VariantManagerAdapter.publishVariants(LoginController.sessionId, projectId, referenceId, updateID);

                            //success
                            progressBar.setIndeterminate(false);
                            cancelButton.setEnabled(false);
                            progressBar.setValue(100);
                            progressLabel.setText("Variants published.");


                            page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);

                            instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                        } catch (Exception ex) {
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(0);
                            progressLabel.setText("Problem publishing variants.");
                        }
                    }
                  
                };
                cancelButton.setVisible(true);
                startButton.setVisible(false);
                uploadThread.start();
            }
        });

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelButton.setText("Cancelling...");
                cancelButton.setEnabled(false);
                uploadThread.interrupt();
            }
        });

        page.addComponent(ViewUtil.alignRight(startButton));
        cancelButton.setVisible(false);
        page.addComponent(ViewUtil.alignRight(cancelButton));

        return page;
    }
    
    private static String[][] tagsToStringArray(List<VariantTag> variantTags) {

        String[][] result = new String[variantTags.size()][2];

        int row = 0;
        for (VariantTag t : variantTags) {
            result[row][0] = t.key;
            result[row][1] = t.value;
            row++;
        }

        return result;
    }
}
