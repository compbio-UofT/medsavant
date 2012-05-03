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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.format.PatientFormat;
import org.ut.biolab.medsavant.format.VariantFormat;
import org.ut.biolab.medsavant.model.Annotation;
import org.ut.biolab.medsavant.model.ProjectDetails;
import org.ut.biolab.medsavant.model.Reference;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.dialog.NewReferenceDialog;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class ProjectWizard extends WizardDialog {

    private final String PAGENAME_NAME = "Project Name";
    private final String PAGENAME_PATIENTS = "Patients";
    private final String PAGENAME_VCF = "Custom VCF Fields";
    private final String PAGENAME_REF = "Reference";
    private String PAGENAME_CREATE = "Create";
    private final String PAGENAME_COMPLETE = "Complete";

    private boolean modify = false;
    private boolean isModified = false;

    private int projectId;
    private String originalProjectName;
    private String projectName;
    private DefaultTableModel patientFormatModel;
    private DefaultTableModel variantFormatModel;
    //private String validationError = "";
    private List<CustomField> patientFields;
    private List<CustomField> variantFields;
    private List<ProjectDetails> projectDetails = new ArrayList<ProjectDetails>();
    private List<CheckListItem> checkListItems = new ArrayList<CheckListItem>();
    private boolean variantFieldsChanged = false;
    private Thread publishThread = null;

    /* modify existing project */
    public ProjectWizard(int projectId, String projectName, List<CustomField> fields, List<ProjectDetails> projectDetails) {
        this.projectId = projectId;
        this.modify = true;
        this.originalProjectName = projectName;
        this.projectName = projectName;
        this.patientFields = fields;
        this.projectDetails = projectDetails;
        this.PAGENAME_CREATE = "Modify";

        //check for existing unpublished changes to this project
        try {
            if (MedSavantClient.ProjectQueryUtilAdapter.existsUnpublishedChanges(LoginController.sessionId, projectId)) {
                DialogUtils.displayMessage("Cannot modify project", "There are unpublished changes to this project. Please publish and then try again.");
                return;
            }
        } catch (Exception ex) {
            DialogUtils.displayErrorMessage("Error checking for changes. ", ex);
            return;
        }

        //get lock
        try {
            if (!MedSavantClient.SettingsQueryUtilAdapter.getDbLock(LoginController.sessionId)) {
                DialogUtils.displayMessage("Cannot modify project", "Another user is making changes to the database. You must wait until this user has finished. ");
                return;
            }
        } catch (Exception ex) {
            DialogUtils.displayErrorMessage("Error getting database lock", ex);
            return;
        }

        catchClosing();
        setupWizard();
    }

    /* create new project */
    public ProjectWizard() {
        setupWizard();
    }

    private void catchClosing() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    MedSavantClient.SettingsQueryUtilAdapter.releaseDbLock(LoginController.sessionId);
                } catch (Exception ex) {
                    Logger.getLogger(ImportVariantsWizard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void setupWizard() {
        setTitle("Project Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        //add pages
        PageList model = new PageList();
        model.append(getNamePage());
        model.append(getPatientFieldsPage());
        model.append(getVcfFieldsPage());
        model.append(getReferencePage());
        model.append(getCreatePage());
        model.append(getCompletionPage());
        setPageList(model);

        //change next action
        setNextAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pagename = getCurrentPage().getTitle();
                if (pagename.equals(PAGENAME_NAME) && validateProjectName()) {
                    setCurrentPage(PAGENAME_PATIENTS);
                } else if (pagename.equals(PAGENAME_PATIENTS) && validatePatientFormatModel()) {
                    setCurrentPage(PAGENAME_VCF);
                } else if (pagename.equals(PAGENAME_VCF) && validateVariantFormatModel()) {
                    setCurrentPage(PAGENAME_REF);
                } else if (pagename.equals(PAGENAME_REF) && validateReferences()) {
                    setCurrentPage(PAGENAME_CREATE);
                } else if (pagename.equals(PAGENAME_CREATE)) {
                    setCurrentPage(PAGENAME_COMPLETE);
                }
            }
        });

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private AbstractWizardPage getNamePage() {

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_NAME) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                if (projectName == null || projectName.equals("")) {
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        };
        page.addText(
                "Choose a name for the project. \n"
                + "The name cannot already be in use. ");

        //setup text field
        final JTextField namefield = new JTextField();
        namefield.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (namefield.getText() != null && !namefield.getText().equals("")) {
                    projectName = namefield.getText();
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        });
        page.addComponent(namefield);
        if (modify) namefield.setText(projectName);

        return page;
    }

    private AbstractWizardPage getPatientFieldsPage() {

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_PATIENTS) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("Add relevant fields for patients. ");

        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setPreferredSize(new Dimension(300,250));
        scrollpane.getViewport().setBackground(Color.white);

        final JTable table = new JTable() {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2) {
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }
        };

        patientFormatModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) {
                return row >= 8;
            }
        };

        patientFormatModel.addColumn("Name");
        patientFormatModel.addColumn("Type");
        patientFormatModel.addColumn("Filterable");
        patientFormatModel.addColumn("Alias");
        patientFormatModel.addColumn("Description");

        patientFormatModel.addRow(new Object[]{DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID, DefaultpatientTableSchema.TYPE_OF_FAMILY_ID + getLengthString(DefaultpatientTableSchema.LENGTH_OF_FAMILY_ID), true, PatientFormat.ALIAS_OF_FAMILY_ID, ""});
        patientFormatModel.addRow(new Object[]{DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, DefaultpatientTableSchema.TYPE_OF_HOSPITAL_ID + getLengthString(DefaultpatientTableSchema.LENGTH_OF_HOSPITAL_ID), true, PatientFormat.ALIAS_OF_HOSPITAL_ID, ""});
        patientFormatModel.addRow(new Object[]{DefaultpatientTableSchema.COLUMNNAME_OF_IDBIOMOM, DefaultpatientTableSchema.TYPE_OF_IDBIOMOM + getLengthString(DefaultpatientTableSchema.LENGTH_OF_IDBIOMOM), true, PatientFormat.ALIAS_OF_IDBIOMOM, ""});
        patientFormatModel.addRow(new Object[]{DefaultpatientTableSchema.COLUMNNAME_OF_IDBIODAD, DefaultpatientTableSchema.TYPE_OF_IDBIODAD + getLengthString(DefaultpatientTableSchema.LENGTH_OF_IDBIODAD), true, PatientFormat.ALIAS_OF_IDBIODAD, ""});
        patientFormatModel.addRow(new Object[]{DefaultpatientTableSchema.COLUMNNAME_OF_GENDER, DefaultpatientTableSchema.TYPE_OF_GENDER + getLengthString(DefaultpatientTableSchema.LENGTH_OF_GENDER), true, PatientFormat.ALIAS_OF_GENDER, ""});
        patientFormatModel.addRow(new Object[]{DefaultpatientTableSchema.COLUMNNAME_OF_AFFECTED, DefaultpatientTableSchema.TYPE_OF_AFFECTED + getLengthString(DefaultpatientTableSchema.LENGTH_OF_AFFECTED), true, PatientFormat.ALIAS_OF_AFFECTED, ""});
        patientFormatModel.addRow(new Object[]{DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS, DefaultpatientTableSchema.TYPE_OF_DNA_IDS + getLengthString(DefaultpatientTableSchema.LENGTH_OF_DNA_IDS), true, PatientFormat.ALIAS_OF_DNA_IDS, ""});
        patientFormatModel.addRow(new Object[]{DefaultpatientTableSchema.COLUMNNAME_OF_BAM_URL, DefaultpatientTableSchema.TYPE_OF_BAM_URL + getLengthString(DefaultpatientTableSchema.LENGTH_OF_BAM_URL), true, PatientFormat.ALIAS_OF_BAM_URL, ""});

        if (modify) {
            for (CustomField f : patientFields) {
                patientFormatModel.addRow(new Object[]{f.getColumnName(), f.getColumnTypeString(), f.isFilterable(), f.getAlias(), f.getDescription()});
            }
        }

        table.setModel(patientFormatModel);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        scrollpane.getViewport().add(table);
        page.addComponent(scrollpane);

        JButton addFieldButton = new JButton("Add Field");
        addFieldButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                patientFormatModel.addRow(new Object[5]);
                table.setModel(patientFormatModel);
            }
        });
        page.addComponent(addFieldButton);

        JButton removeFieldButton = new JButton("Remove Field");
        removeFieldButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= MedSavantDatabase.DefaultpatientTableSchema.getNumFields()-1) {
                    patientFormatModel.removeRow(row);
                }
                table.setModel(patientFormatModel);
            }
        });
        page.addComponent(removeFieldButton);

        return page;
    }

    private AbstractWizardPage getVcfFieldsPage() {

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_VCF) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("Add extra fields to retrieve from VCF files. ");

        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setPreferredSize(new Dimension(300,250));
        scrollpane.getViewport().setBackground(Color.white);

        final JTable table = new JTable() {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2) {
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }
        };

        variantFormatModel = new DefaultTableModel();

        variantFormatModel.addColumn("Key");
        variantFormatModel.addColumn("Type");
        variantFormatModel.addColumn("Filterable");
        variantFormatModel.addColumn("Alias");
        variantFormatModel.addColumn("Description");

        if (modify) {
            try {
                int firstRef = MedSavantClient.ReferenceQueryUtilAdapter.getReferenceIdsForProject(LoginController.sessionId, projectId).get(0);
                List<CustomField> fields = MedSavantClient.ProjectQueryUtilAdapter.getCustomVariantFields(
                        LoginController.sessionId, projectId, firstRef,
                        MedSavantClient.ProjectQueryUtilAdapter.getNewestUpdateId(LoginController.sessionId, projectId, firstRef, false));
                for (CustomField f : fields) {
                    variantFormatModel.addRow(new Object[]{f.getColumnName().toUpperCase(), f.getColumnTypeString(), f.isFilterable(), f.getAlias(), f.getDescription()});
                }
            } catch (SQLException ex) {
                Logger.getLogger(ProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(ProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_AA.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_AA + getLengthString(DefaultVariantTableSchema.LENGTH_OF_AA), true, VariantFormat.ALIAS_OF_AA, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_AC.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_AC + getLengthString(DefaultVariantTableSchema.LENGTH_OF_AA), true, VariantFormat.ALIAS_OF_AC, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_AF.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_AF + getLengthString(DefaultVariantTableSchema.LENGTH_OF_AF), true, VariantFormat.ALIAS_OF_AF, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_AN.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_AN + getLengthString(DefaultVariantTableSchema.LENGTH_OF_AN), true, VariantFormat.ALIAS_OF_AN, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_BQ.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_BQ + getLengthString(DefaultVariantTableSchema.LENGTH_OF_BQ), true, VariantFormat.ALIAS_OF_BQ, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_CIGAR.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_CIGAR + getLengthString(DefaultVariantTableSchema.LENGTH_OF_CIGAR), true, VariantFormat.ALIAS_OF_CIGAR, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_DB.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_DB + getLengthString(DefaultVariantTableSchema.LENGTH_OF_DB), true, VariantFormat.ALIAS_OF_DB, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_DP.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_DP + getLengthString(DefaultVariantTableSchema.LENGTH_OF_DP), true, VariantFormat.ALIAS_OF_DP, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_END.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_END + getLengthString(DefaultVariantTableSchema.LENGTH_OF_END), true, VariantFormat.ALIAS_OF_END, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_H2.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_H2 + getLengthString(DefaultVariantTableSchema.LENGTH_OF_H2), true, VariantFormat.ALIAS_OF_H2, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_MQ.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_MQ + getLengthString(DefaultVariantTableSchema.LENGTH_OF_MQ), true, VariantFormat.ALIAS_OF_MQ, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_MQ0.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_MQ0 + getLengthString(DefaultVariantTableSchema.LENGTH_OF_MQ0), true, VariantFormat.ALIAS_OF_MQ0, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_NS.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_NS + getLengthString(DefaultVariantTableSchema.LENGTH_OF_NS), true, VariantFormat.ALIAS_OF_NS, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_SB.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_SB + getLengthString(DefaultVariantTableSchema.LENGTH_OF_SB), true, VariantFormat.ALIAS_OF_SB, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_SOMATIC.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_SOMATIC + getLengthString(DefaultVariantTableSchema.LENGTH_OF_SOMATIC), true, VariantFormat.ALIAS_OF_SOMATIC, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_VALIDATED.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_VALIDATED + getLengthString(DefaultVariantTableSchema.LENGTH_OF_VALIDATED), true, VariantFormat.ALIAS_OF_VALIDATED, ""});
        }

        table.setModel(variantFormatModel);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        scrollpane.getViewport().add(table);
        page.addComponent(scrollpane);

        table.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                variantFieldsChanged = true;
            }

            @Override
            public void keyPressed(KeyEvent e) {
                variantFieldsChanged = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                variantFieldsChanged = true;
            }
        });

        JButton addFieldButton = new JButton("Add Field");
        addFieldButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                variantFormatModel.addRow(new Object[2]);
                table.setModel(variantFormatModel);
                variantFieldsChanged = true;
            }
        });
        page.addComponent(addFieldButton);

        JButton removeFieldButton = new JButton("Remove Field");
        removeFieldButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    variantFormatModel.removeRow(row);
                    table.setModel(variantFormatModel);
                    variantFieldsChanged = true;
                }
            }
        });
        page.addComponent(removeFieldButton);

        return page;
    }

    private String getLengthString(int len) {
        if (len > 0) {
            return "(" + len + ")";
        } else {
            return "";
        }
    }

    private AbstractWizardPage getReferencePage() {

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_REF) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };
        page.addText(
                "Choose reference genome(s) to add to this project, along with\n"
                + "corresponding annotations. Annotations will be applied to all\n"
                + "variants added to these tables. ");

        //setup list
        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setPreferredSize(new Dimension(300,220));
        scrollpane.getViewport().setBackground(Color.white);

        final JPanel p = new JPanel();
        p.setBackground(Color.white);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        refreshReferencePanel(p);
        scrollpane.getViewport().add(p);

        page.addComponent(scrollpane);

        JButton addRefButton = new JButton("New Reference");
        addRefButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                new NewReferenceDialog().setVisible(true);
                refreshReferencePanel(p);
            }
        });
        page.addComponent(addRefButton);

        return page;
    }

    private void refreshReferencePanel(JPanel p) {
        List<Reference> references = null;
        List<Annotation> annotations = null;
        try {
            references = MedSavantClient.ReferenceQueryUtilAdapter.getReferences(LoginController.sessionId);
            annotations = MedSavantClient.AnnotationQueryUtilAdapter.getAnnotations(LoginController.sessionId);
        } catch(SQLException ex) {
            Logger.getLogger(ProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
        } catch(RemoteException ex) {
            Logger.getLogger(ProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
        }

        p.removeAll();
        checkListItems.clear();
        for (Reference r : references) {
            CheckListItem cli = new CheckListItem(r, annotations);
            checkListItems.add(cli);
            p.add(cli);
        }
        p.add(Box.createVerticalGlue());

        p.updateUI();
        p.repaint();
    }

    private AbstractWizardPage getCreatePage() {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_CREATE) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };
        page.addText(
                "You are now ready to " + (modify ? "make changes to" : "create") + " this project. ");

        final JLabel progressLabel = new JLabel("");
        final JProgressBar progressBar = new JProgressBar();

        page.addComponent(progressLabel);
        page.addComponent(progressBar);

        final JButton startButton = new JButton((modify ? "Modify Project" : "Create Project"));
        final JButton publishStartButton = new JButton("Publish Variants");

        //final JButton cancelButton = new JButton("Cancel");
        final JButton publishCancelButton = new JButton("Cancel");

        final JCheckBox autoPublishVariants = new JCheckBox("Automatically publish variants after modify");

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
                            //instance.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                            // do stuff
                            MedSavantClient.VariantManagerAdapter.publishVariants(LoginController.sessionId, projectId);

                            //success
                            publishProgressBar.setIndeterminate(false);
                            publishCancelButton.setVisible(false);
                            publishProgressBar.setValue(100);
                            publishProgressLabel.setText("Publish complete.");

                            //page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);


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
                                    ClientMiscUtils.checkSQLException((SQLException) ex);
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




        startButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                startButton.setEnabled(false);
                page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                progressBar.setIndeterminate(true);
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            createProject();
                            ((CompletionWizardPage)getPageByTitle(PAGENAME_COMPLETE)).addText(
                                    "Project " + projectName + " has been " + (modify ? "modified." : "created."));
                            //instance.setCurrentPage(PAGENAME_COMPLETE);
                            MedSavantClient.SettingsQueryUtilAdapter.releaseDbLock(LoginController.sessionId);

                            //success
                            if (modify) {
                                progressBar.setIndeterminate(false);
                                //cancelButton.setEnabled(false);
                                //cancelButton.setVisible(false);
                                progressBar.setValue(100);
                                progressLabel.setText("Upload complete.");

                                publishProgressLabel.setVisible(true);
                                publishProgressBar.setVisible(true);

                                autoPublishVariants.setVisible(false);

                                if (autoPublishVariants.isSelected()) {

                                    publishProgressLabel.setText("Publishing variants...");

                                    // publish
                                    MedSavantClient.VariantManagerAdapter.publishVariants(LoginController.sessionId, projectId);

                                    //success
                                    publishProgressBar.setIndeterminate(false);
                                    publishCancelButton.setVisible(false);
                                    publishProgressBar.setValue(100);
                                    publishProgressLabel.setText("Publish complete.");

                                    //page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);

                                } else {
                                    publishStartButton.setVisible(true);
                                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                                }
                            } else {
                                setCurrentPage(PAGENAME_COMPLETE);
                            }

                        } catch (Exception ex) {

                            try {
                                MedSavantClient.SettingsQueryUtilAdapter.releaseDbLock(LoginController.sessionId);
                            } catch (Exception e) {
                                Logger.getLogger(ProjectWizard.class.getName()).log(Level.SEVERE, null, e);
                            }

                            if (ex instanceof SQLException)
                                ClientMiscUtils.checkSQLException((SQLException)ex);
                            DialogUtils.displayException("Error", "There was an error while trying to create your project. ", ex);
                            Logger.getLogger(ProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
                            setVisible(false);
                            dispose();
                        }
                    }
                };
                t.start();
            }

        });

        /*cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelButton.setText("Cancelling...");
                cancelButton.setEnabled(false);
                uploadThread.interrupt();
            }
        });*/

        publishCancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                publishCancelButton.setText("Cancelling...");
                publishCancelButton.setEnabled(false);
                publishThread.interrupt();
            }
        });

        page.addComponent(ViewUtil.alignRight(startButton));
        //cancelButton.setVisible(false);
       // page.addComponent(ViewUtil.alignRight(cancelButton));

        if (modify) {
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
        }

        return page;
    }

    private AbstractWizardPage getCompletionPage() {
        CompletionWizardPage page = new CompletionWizardPage(PAGENAME_COMPLETE) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };
        return page;
    }

    private boolean validateProjectName() {
        try {
            if (MedSavantClient.ProjectQueryUtilAdapter.containsProject(LoginController.sessionId, projectName) && (!modify || !projectName.equals(originalProjectName))) {
                JOptionPane.showMessageDialog(this, "Project name already in use. ", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                return true;
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
            DialogUtils.displayException("Error", "Error trying to create project", ex);
        }
        return false;
    }

    private boolean validateReferences() {
        for (CheckListItem cli : checkListItems) {
            if (cli.isSelected()) {
                return true;
            }
        }
        JOptionPane.showMessageDialog(this, "You must select at least one reference. ", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    private boolean validatePatientFormatModel() {
        patientFields = new ArrayList<CustomField>();
        // 8 is the number of standard patientFields
        boolean success = validateFormatModel(patientFields, patientFormatModel, 8);
        if (!success) {
            JOptionPane.showMessageDialog(
                    this,
                    "Patient table format contains errors\n"
                    + "Name cannot only contain letters, numbers and underscores. \n"
                    + "Type must be in format: COLUMNTYPE(LENGTH)",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return success;
    }

    private boolean validateVariantFormatModel() {
        variantFields = new ArrayList<CustomField>();
        boolean success = validateFormatModel(variantFields, variantFormatModel, 0);
        if (!success) {
            JOptionPane.showMessageDialog(
                    this,
                    "Variant table format contains errors\n"
                    + "Name cannot only contain letters, numbers and underscores. \n"
                    + "Type must be in format: COLUMNTYPE(LENGTH)",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return success;
    }

    private boolean validateFormatModel(List<CustomField> fields, DefaultTableModel model, int firstRow) {

        for (int row = firstRow; row < model.getRowCount(); row++) {
            String fieldName = (String)model.getValueAt(row, 0);
            String fieldType = (String)model.getValueAt(row, 1);
            Boolean fieldFilterable = (Boolean)model.getValueAt(row, 2);
            String fieldAlias = (String)model.getValueAt(row, 3);
            String fieldDescription = (String)model.getValueAt(row, 4);

            if (fieldName == null || fieldType == null) {
                continue;
            }

            if (!fieldName.matches("^([a-z]|[A-Z]|_|[0-9])+$")) {// ||
                    //!fieldType.matches("^([a-z]|[A-Z])+\\([0-9]+\\)$")) {
                return false;
            }

            if (!fieldName.equals("") && !fieldType.equals("")) {
                if (fieldFilterable == null) fieldFilterable = false;
                if (fieldAlias == null) fieldAlias = fieldName;
                if (fieldDescription == null) fieldDescription = "";
                fields.add(new CustomField(fieldName, fieldType, fieldFilterable, fieldAlias, fieldDescription));
            }
        }

        return true;
    }

    private void createProject() throws SQLException, RemoteException, Exception {
        if (modify) {

            //change project name
            if (!projectName.equals(originalProjectName)) {
                MedSavantClient.ProjectQueryUtilAdapter.renameProject(LoginController.sessionId, projectId, projectName);
            }

            //modify patientFields
            MedSavantClient.PatientQueryUtilAdapter.updateFields(LoginController.sessionId, projectId, patientFields);

            //edit references and annotations
            for (CheckListItem cli : checkListItems) {
                ProjectDetails pd = getProjectDetails(cli.getReference().getId());

                //skip if not selected and not existing
                if (!cli.isSelected() && pd == null) {
                    continue;
                }

                //get annotation ids
                List<Integer> ai =cli.getAnnotationIds();
                int[] annotationIds = new int[ai.size()];
                for (int i = 0; i < annotationIds.length; i++) {
                    annotationIds[i] = cli.getAnnotationIds().get(i);
                }

                //add new ref
                if (pd == null && cli.isSelected()) {
                    String tablename = MedSavantClient.ProjectQueryUtilAdapter.createVariantTable(LoginController.sessionId, projectId, cli.getReference().getId(), 0, annotationIds, false);
                    MedSavantClient.ProjectQueryUtilAdapter.setCustomVariantFields(LoginController.sessionId, projectId, cli.getReference().getId(), 0, variantFields);
                    MedSavantClient.ProjectQueryUtilAdapter.addTableToMap(LoginController.sessionId, projectId, cli.getReference().getId(), 0, true, tablename, null);
                    continue;
                //remove existing ref
                } else if (pd != null && !cli.isSelected()) {
                    MedSavantClient.ProjectQueryUtilAdapter.removeReferenceForProject(LoginController.sessionId, projectId, cli.getReference().getId());
                    continue;
                }

                //make modifications
                MedSavantClient.VariantManagerAdapter.updateTable(LoginController.sessionId, projectId, cli.getReference().getId(), annotationIds, variantFields);

            }

            isModified = true;

        } else {

            //create project
            int projectid = ProjectController.getInstance().addProject(projectName, patientFields);

            //add references and annotations
            for (CheckListItem cli : checkListItems) {
                if (cli.isSelected()) {

                    //set custom vcf fields
                    MedSavantClient.ProjectQueryUtilAdapter.setCustomVariantFields(LoginController.sessionId, projectid, cli.getReference().getId(), 0, variantFields);

                    //get annotation ids
                    List<Integer> annotationIds = cli.getAnnotationIds();

                    int[] annIds = new int[annotationIds.size()];
                    for (int i = 0; i < annotationIds.size(); i++) {
                        annIds[i] = annotationIds.get(i);
                    }
                    String tablename = MedSavantClient.ProjectQueryUtilAdapter.createVariantTable(LoginController.sessionId, projectid, cli.getReference().getId(), 0, (annotationIds.isEmpty() ? null : annIds), false);
                    MedSavantClient.ProjectQueryUtilAdapter.addTableToMap(LoginController.sessionId, projectid, cli.getReference().getId(), 0, true, tablename, null);

                }
            }
        }
    }

    private class CheckListItem extends JPanel {

        private boolean selected = false;
        private Reference reference;
        private Map<Integer, Boolean> annIdsMap = new HashMap<Integer, Boolean>();
        private List<JCheckBox> annBoxes = new ArrayList<JCheckBox>();

        public CheckListItem(Reference reference, List<Annotation> annotations) {

            this.reference = reference;

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBackground(Color.white);

            ProjectDetails pd = getProjectDetails(reference.getId());
            selected = (pd != null);

            final JCheckBox b = new JCheckBox(reference.getName());
            b.setMaximumSize(new Dimension(1000,20));
            b.setBackground(Color.white);
            b.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    selected = b.isSelected();
                    for (JCheckBox annBox : annBoxes) {
                        annBox.setEnabled(selected);
                    }
                }
            });
            b.setSelected(selected);

            JPanel p = new JPanel();
            p.setMaximumSize(new Dimension(1000, 20));
            p.setBackground(Color.white);
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(b);
            p.add(Box.createHorizontalGlue());
            this.add(p);

            for (final Annotation a : annotations) {

                //make sure annotation is for this reference
                if (a.getReferenceId() != reference.getId()) {
                    continue;
                }

                final JCheckBox b1 = new JCheckBox(a.getProgram() + " " + a.getVersion());
                b1.setMaximumSize(new Dimension(1000,20));
                b1.setBackground(Color.white);
                b1.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        annIdsMap.put(a.getId(), b1.isSelected());
                    }
                });
                annBoxes.add(b1);
                b1.setEnabled(selected);
                b1.setSelected(false);
                annIdsMap.put(a.getId(), false);
                if (pd != null && pd.getAnnotationIds().contains(a.getId())) {
                    b1.setSelected(true);
                    annIdsMap.put(a.getId(), true);
                }

                JPanel p1 = new JPanel();
                p1.setMaximumSize(new Dimension(1000,20));
                p1.setBackground(Color.white);
                p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
                p1.add(Box.createHorizontalStrut(30));
                p1.add(b1);
                p1.add(Box.createHorizontalGlue());
                this.add(p1);
            }
        }

        public boolean isSelected() {
            return selected;
        }

        public Reference getReference() {
            return reference;
        }

        public List<Integer> getAnnotationIds() {
            List<Integer> ids = new ArrayList<Integer>();
            for (Integer key : annIdsMap.keySet()) {
                if (annIdsMap.get(key)) {
                    ids.add(key);
                }
            }
            return ids;
        }
    }

    private ProjectDetails getProjectDetails(int referenceId) {
        for (ProjectDetails pd : projectDetails) {
            if (pd.getReferenceId() == referenceId) {
                return pd;
            }
        }
        return null;
    }

    public boolean isModified() {
        return isModified;
    }
}
