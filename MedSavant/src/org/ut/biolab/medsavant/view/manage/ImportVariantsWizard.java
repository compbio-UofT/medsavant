/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

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
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.util.ImportVariants;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil;
import org.ut.biolab.medsavant.model.VariantTag;
import org.ut.biolab.medsavant.db.util.ExtensionsFileFilter;
import org.ut.biolab.medsavant.settings.DirectorySettings;
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

    public ImportVariantsWizard() {
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
        model.append(getCompletePage());
        setPageList(model);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
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

        this.projectId = ProjectController.getInstance().getCurrentProjectId();
        this.referenceId = ReferenceController.getInstance().getCurrentReferenceId();
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
                if(variantFiles != null && variantFiles.length > 0){
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
                if(variantFiles == null || variantFiles.length == 0){
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
                addDefaultTags(variantTags,ta);
            }
        });

        addDefaultTags(variantTags,ta);

        page.addComponent(ViewUtil.alignRight(clear));

        return page;

    }

    private void addDefaultTags(List<VariantTag> variantTags, JTextArea ta) {

        VariantTag tag1 = new VariantTag("Uploader",LoginController.getUsername());
        VariantTag tag2 = new VariantTag("Upload Date",(new Date()).toString());
        variantTags.add(tag1);
        variantTags.add(tag2);
        ta.append(tag1.toString() + "\n");
        ta.append(tag2.toString() + "\n");
    }

    private enum ServerState {

        NOT_CONNECTED, SERVER_CONNECTED, SERVER_IDLE, SERVER_NEVER_CONNECTED
    };

    private AbstractWizardPage getCompletePage() {

        Date lastCheckIn;
        ServerState state = ServerState.SERVER_NEVER_CONNECTED;

        try {
            lastCheckIn = ServerLogQueryUtil.getDateOfLastServerLog();

            if (lastCheckIn == null) {
                state = ServerState.SERVER_NEVER_CONNECTED;
            } else {

                long elapsed = System.currentTimeMillis() - lastCheckIn.getTime();

                if (elapsed > 1000 * 60 * 30) { // 30 minutes
                    state = ServerState.SERVER_IDLE;
                } else {
                    state = ServerState.SERVER_CONNECTED;
                }
            }
        } catch (SQLException ex) {
        }

        CompletionWizardPage page = new CompletionWizardPage("Complete");
        page.fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
        page.addText("You have finished queueing your variants for import.");

        if (state == ServerState.SERVER_CONNECTED) {
            page.addText("They are in the process of being annotated "
                    + "and will be\nmade available shortly.");
        } else {
            JLabel l = new JLabel("WARNING:");
            l.setForeground(Color.red);
            l.setFont(new Font(l.getFont().getFamily(),Font.BOLD,l.getFont().getSize()));
            page.addComponent(l);
            page.addText("The MedSavant Server Utility is not running.\n"
                    + "Please contact your administrator to start this process.\n"
                    + "In the meantime, these variants will remain queued.");
        }
        return page;
    }

    private AbstractWizardPage getQueuePage() {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Upload files") {

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("You are now ready to upload variants.");

        final JLabel progressLabel = new JLabel("Ready to upload variant files.");
        final JProgressBar progressBar = new JProgressBar();

        page.addComponent(progressLabel);
        page.addComponent(progressBar);

        final JButton cancelButton = new JButton("Cancel");
        final JButton startButton = new JButton("Start Upload");

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

                            final int uploadID = ImportVariants.performImport(variantFiles, DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory()), projectId, referenceId, progressLabel);

                            boolean successUploadVariants = uploadID != -1;

                            boolean successUploadTags = ImportVariants.addTagsToUpload(uploadID,tagsToStringArray(variantTags));

                            final boolean success = successUploadVariants && successUploadTags;

                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    progressBar.setIndeterminate(false);
                                    if (success) {
                                        cancelButton.setEnabled(false);
                                        progressBar.setValue(100);
                                        progressLabel.setText("Upload complete.");
                                        page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                                    } else {
                                        startButton.setEnabled(false);
                                        progressLabel.setText("An error occured while importing variants.");
                                    }
                                }
                            });
                        } catch (SQLException ex){
                        } catch (InterruptedException ex){

                            String[] uploadInfo = ex.getMessage().split(";");
                            int updateId = Integer.parseInt(uploadInfo[0]);
                            String tableName = uploadInfo[1];

                            VariantQueryUtil.cancelUpload(updateId, tableName);

                            progressBar.setIndeterminate(false);
                            progressBar.setValue(0);
                            progressLabel.setText("Upload cancelled.");
                            startButton.setVisible(true);
                            startButton.setEnabled(true);
                            cancelButton.setText("Cancel");
                            cancelButton.setEnabled(true);
                            cancelButton.setVisible(false);
                            System.out.println("Update " + updateId + " was cancelled");
                        }
                        instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    }

                    private String[][] tagsToStringArray(List<VariantTag> variantTags) {

                        String[][] result = new String[variantTags.size()][2];

                        int row = 0;
                        for (VariantTag t : variantTags) {
                            result[row][0] = t.key;
                            result[row][1] = t.value;
                            row++;
                        }

                        return result;
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

}
