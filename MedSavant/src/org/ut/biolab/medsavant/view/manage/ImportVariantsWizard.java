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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.util.ImportVariants;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.model.VariantTag;
import org.ut.biolab.medsavant.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class ImportVariantsWizard extends WizardDialog {

    private boolean modify = false;
    private boolean isModified = false;
    private int projectId;
    private int referenceId;
    private JComboBox locationField;
    
    private List<VariantTag> variantTags;
    private File[] variantFiles;
    
    private Thread uploadThread = null;

    
    /* modify existing project */
    public ImportVariantsWizard(boolean modify) {
        this.modify = modify;
        setupWizard();
    }

    public ImportVariantsWizard() {
        this(false);
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
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("Choose the variant file(s) to be imported:");

        final JTextField outputFileField = new JTextField();
        JButton chooseFileButton = new JButton("...");
        chooseFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                variantFiles = DialogUtils.chooseFilesForOpen("Import Variants", new ExtensionFileFilter(new String[]{"vcf", "vcf.gz"}), null);
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
        //JLabel nameLabel = new JLabel(projectName + " (" + referenceName + ")"); 
        //nameLabel.setFont(ViewUtil.getMediumTitleFont());
        //page.addComponent(nameLabel);
        //page.addText("If the variants are with respect to another reference\ngenome, switch to that reference and try importing again.");

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

        String[] patternExamples = {
            "Tag",
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
        
        valueField.setText("Value");

        final JTextArea ta = new JTextArea();
        ta.setRows(10);
        ta.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        ta.setEditable(false);
        
        JButton button = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                // TODO: actually use uploadId
                VariantTag tag = new VariantTag((String)locationField.getSelectedItem(),valueField.getText());
                variantTags.add(tag);
                ta.append(tag.toString() + "\n");
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
            }
        });
        
        page.addComponent(ViewUtil.alignRight(clear));
        
        return page;

    }
    
    private AbstractWizardPage getCompletionPage() {
        CompletionWizardPage page = new CompletionWizardPage("Complete");
        String specific = "create";
        if (modify) {
            specific = "make changes to";
        }
        page.addText("Click finish to " + specific + " project. ");
        return page;
    }
    
    private AbstractWizardPage getCompletePage() {
        CompletionWizardPage page = new CompletionWizardPage("Complete");
        page.fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
        page.addText("You have completed importing your variants.\n\n"
                + "They are in the process of being annotated "
                + "and will be\nmade available shortly.");
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
                            final boolean success = ImportVariants.performImport(variantFiles, projectId, referenceId, progressLabel);

                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    progressBar.setIndeterminate(false);
                                    if (success) {
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

    public boolean isModified() {
        return isModified;
    }
}
