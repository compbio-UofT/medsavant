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

package org.ut.biolab.medsavant.project;

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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.db.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.format.PatientFormat;
import org.ut.biolab.medsavant.format.VariantFormat;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Annotation;
import org.ut.biolab.medsavant.model.ProjectDetails;
import org.ut.biolab.medsavant.model.Reference;
import org.ut.biolab.medsavant.reference.NewReferenceDialog;
import org.ut.biolab.medsavant.serverapi.ProjectManagerAdapter;
import org.ut.biolab.medsavant.variant.UpdateWorker;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class ProjectWizard extends WizardDialog {

    private static final Log LOG = LogFactory.getLog(ProjectWizard.class);
    private static final String PAGENAME_NAME = "Project Name";
    private static final String PAGENAME_PATIENTS = "Patients";
    private static final String PAGENAME_VCF = "Custom VCF Fields";
    private static final String PAGENAME_REF = "Reference";
    private static final String PAGENAME_COMPLETE = "Complete";

    private static String PAGENAME_CREATE = "Create";

    private final boolean modify;
    private boolean isModified = false;

    private final int projectID;
    private final String originalProjectName;
    private String projectName;
    private CustomField[] patientFields;
    private final ProjectDetails[] projectDetails;
    private DefaultTableModel patientFormatModel;
    private DefaultTableModel variantFormatModel;
    private CustomField[] variantFields;
    private List<CheckListItem> checkListItems = new ArrayList<CheckListItem>();
    private boolean variantFieldsChanged = false;

    private final ProjectManagerAdapter manager;

    /* modify existing project */
    public ProjectWizard(int projID, String projName, CustomField[] fields, ProjectDetails[] details) {
        this.modify = true;
        this.projectID = projID;
        this.originalProjectName = projName;
        this.projectName = projName;
        this.patientFields = fields;
        this.projectDetails = details;
        PAGENAME_CREATE = "Modify";
        manager = MedSavantClient.ProjectManager;

        setupWizard();
    }

    /* create new project */
    public ProjectWizard() {
        modify = false;
        projectID = -1;
        originalProjectName = null;
        projectDetails = new ProjectDetails[0];
        manager = MedSavantClient.ProjectManager;
        setupWizard();
    }

    private void setupWizard() {
        setTitle("Project Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        //add pages
        PageList model = new PageList();
        model.append(getNamePage());
        model.append(getPatientFieldsPage());
        model.append(getVCFFieldsPage());
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

        patientFormatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID, DefaultPatientTableSchema.TYPE_OF_FAMILY_ID + getLengthString(DefaultPatientTableSchema.LENGTH_OF_FAMILY_ID), true, PatientFormat.ALIAS_OF_FAMILY_ID, ""});
        patientFormatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, DefaultPatientTableSchema.TYPE_OF_HOSPITAL_ID + getLengthString(DefaultPatientTableSchema.LENGTH_OF_HOSPITAL_ID), true, PatientFormat.ALIAS_OF_HOSPITAL_ID, ""});
        patientFormatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_IDBIOMOM, DefaultPatientTableSchema.TYPE_OF_IDBIOMOM + getLengthString(DefaultPatientTableSchema.LENGTH_OF_IDBIOMOM), true, PatientFormat.ALIAS_OF_IDBIOMOM, ""});
        patientFormatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_IDBIODAD, DefaultPatientTableSchema.TYPE_OF_IDBIODAD + getLengthString(DefaultPatientTableSchema.LENGTH_OF_IDBIODAD), true, PatientFormat.ALIAS_OF_IDBIODAD, ""});
        patientFormatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_GENDER, DefaultPatientTableSchema.TYPE_OF_GENDER + getLengthString(DefaultPatientTableSchema.LENGTH_OF_GENDER), true, PatientFormat.ALIAS_OF_GENDER, ""});
        patientFormatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_AFFECTED, DefaultPatientTableSchema.TYPE_OF_AFFECTED + getLengthString(DefaultPatientTableSchema.LENGTH_OF_AFFECTED), true, PatientFormat.ALIAS_OF_AFFECTED, ""});
        patientFormatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, DefaultPatientTableSchema.TYPE_OF_DNA_IDS + getLengthString(DefaultPatientTableSchema.LENGTH_OF_DNA_IDS), true, PatientFormat.ALIAS_OF_DNA_IDS, ""});
        patientFormatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL, DefaultPatientTableSchema.TYPE_OF_BAM_URL + getLengthString(DefaultPatientTableSchema.LENGTH_OF_BAM_URL), true, PatientFormat.ALIAS_OF_BAM_URL, ""});

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
                if (row >= DefaultPatientTableSchema.NUM_FIELDS) {
                    patientFormatModel.removeRow(row);
                }
                table.setModel(patientFormatModel);
            }
        });
        page.addComponent(removeFieldButton);

        return page;
    }

    private AbstractWizardPage getVCFFieldsPage() {

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
                int firstRef = manager.getReferenceIDsForProject(LoginController.sessionId, projectID)[0];
                CustomField[] fields = manager.getCustomVariantFields(
                        LoginController.sessionId, projectID, firstRef,
                        manager.getNewestUpdateID(LoginController.sessionId, projectID, firstRef, false));
                for (CustomField f : fields) {
                    variantFormatModel.addRow(new Object[]{f.getColumnName().toUpperCase(), f.getColumnTypeString(), f.isFilterable(), f.getAlias(), f.getDescription()});
                }
            } catch (SQLException ex) {
                LOG.error("Error getting reference IDs for project.", ex);
            } catch (RemoteException ex) {
                LOG.error("Error getting reference IDs for project.", ex);
            }
        } else {
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_AA.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_AA + getLengthString(DefaultVariantTableSchema.LENGTH_OF_AA), true, VariantFormat.ALIAS_OF_AA, ""});
            variantFormatModel.addRow(new Object[]{DefaultVariantTableSchema.COLUMNNAME_OF_AC.toUpperCase(), DefaultVariantTableSchema.TYPE_OF_AC + getLengthString(DefaultVariantTableSchema.LENGTH_OF_AC), true, VariantFormat.ALIAS_OF_AC, ""});
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
        page.addText("Choose reference genome(s) to add to this project, along\n" +
                     "with corresponding annotations. Annotations will be\n" +
                     "applied to all variants added to these tables.");

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
        Reference[] references = null;
        Annotation[] annotations = null;
        try {
            references = MedSavantClient.ReferenceManager.getReferences(LoginController.sessionId);
            annotations = MedSavantClient.AnnotationManagerAdapter.getAnnotations(LoginController.sessionId);
        } catch(SQLException ex) {
            LOG.error("Error getting references and annotations.", ex);
        } catch(RemoteException ex) {
            LOG.error("Error getting references and annotations.", ex);
        }

        p.removeAll();
        checkListItems.clear();
        for (Reference r : references) {
            CheckListItem cli = new CheckListItem(r, annotations);
            checkListItems.add(cli);
            p.add(cli);
        }
        // By default, select the last check-box.  It's either the one they just added, or hg19.
        if (checkListItems.size() > 0) {
            checkListItems.get(checkListItems.size() - 1).setSelected(true);
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

        final JButton workButton = new JButton((modify ? "Modify Project" : "Create Project"));
        final JButton publishButton = new JButton("Publish Variants");

        final JCheckBox autoPublishVariants = new JCheckBox("Automatically publish variants after modify");

        final JLabel publishProgressLabel = new JLabel("Ready to publish variants.");
        final JProgressBar publishProgressBar = new JProgressBar();

        workButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new UpdateWorker(modify ? "Modifying project" : "Creating project", ProjectWizard.this, progressLabel, progressBar, workButton, autoPublishVariants, publishProgressLabel, publishProgressBar, publishButton) {
                    @Override
                    protected Void doInBackground() throws Exception {
                        createProject();
                        return null;
                    }

                    @Override
                    protected void showSuccess(Void result) {
                        if (modify) {
                            ((CompletionWizardPage)getPageByTitle(PAGENAME_COMPLETE)).addText("Project " + projectName + " has been modified.");
                            super.showSuccess(result);
                        } else {
                            ((CompletionWizardPage)getPageByTitle(PAGENAME_COMPLETE)).addText("Project " + projectName + " has been created.");
                            setCurrentPage(PAGENAME_COMPLETE);
                        }
                    }

                }.execute();
            }

        });

        page.addComponent(ViewUtil.alignRight(workButton));
        //cancelButton.setVisible(false);
       // page.addComponent(ViewUtil.alignRight(cancelButton));

        if (modify) {
            page.addComponent(autoPublishVariants);

            JLabel l = new JLabel("WARNING:");
            l.setForeground(Color.red);
            l.setFont(new Font(l.getFont().getFamily(), Font.BOLD, l.getFont().getSize()));
            page.addComponent(l);
            page.addText("All users logged into the system will be logged out\nat the time of publishing.");

            page.addComponent(publishProgressLabel);
            page.addComponent(publishProgressBar);
            page.addComponent(ViewUtil.alignRight(publishButton));

            publishButton.setVisible(false);
            publishProgressLabel.setVisible(false);
            publishProgressBar.setVisible(false);
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
            if (manager.containsProject(LoginController.sessionId, projectName) && (!modify || !projectName.equals(originalProjectName))) {
                JOptionPane.showMessageDialog(this, "Project name already in use. ", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                return true;
            }
        } catch (Exception ex) {
            LOG.error("Error checking for existence of project.", ex);
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
        DialogUtils.displayError("You must select at least one reference.");
        return false;
    }

    private boolean validatePatientFormatModel() {
        // 8 is the number of standard patientFields
        List<CustomField> fields = new ArrayList<CustomField>();
        if (validateFormatModel(fields, patientFormatModel, 8)) {
            patientFields = fields.toArray(new CustomField[0]);
            return true;
        } else {
            DialogUtils.displayError(
                    "Patient table format contains errors\n"
                    + "Name cannot only contain letters, numbers and underscores. \n"
                    + "Type must be in format: COLUMNTYPE(LENGTH)");
            return false;
        }
    }

    private boolean validateVariantFormatModel() {
        List<CustomField> fields = new ArrayList<CustomField>();
        if (validateFormatModel(fields, variantFormatModel, 0)) {
            variantFields = fields.toArray(new CustomField[0]);
            return true;
        } else {
            DialogUtils.displayError(
                    "Variant table format contains errors\n"
                    + "Name cannot only contain letters, numbers and underscores. \n"
                    + "Type must be in format: COLUMNTYPE(LENGTH)");
            return false;
        }
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

    private void createProject() throws Exception {
        if (modify) {

            //change project name
            if (!projectName.equals(originalProjectName)) {
                manager.renameProject(LoginController.sessionId, projectID, projectName);
            }

            //modify patientFields
            MedSavantClient.PatientManager.updateFields(LoginController.sessionId, projectID, patientFields);

            //edit references and annotations
            for (CheckListItem cli : checkListItems) {
                ProjectDetails pd = getProjectDetails(cli.getReference().getId());

                //skip if not selected and not existing
                if (!cli.isSelected() && pd == null) {
                    continue;
                }

                //get annotation ids
                int[] annIDs = cli.getAnnotationIDs();

                //add new ref
                if (pd == null && cli.isSelected()) {
                    int refID = cli.getReference().getId();
                    String tablename = manager.createVariantTable(LoginController.sessionId, projectID, refID, 0, annIDs, false);
                    manager.setCustomVariantFields(LoginController.sessionId, projectID, refID, 0, variantFields);
                    manager.addTableToMap(LoginController.sessionId, projectID, refID, 0, true, tablename, null);
                    continue;
                } else if (pd != null && !cli.isSelected()) {
                    //remove existing ref
                    manager.removeReferenceForProject(LoginController.sessionId, projectID, cli.getReference().getId());
                    continue;
                }

                //make modifications
                MedSavantClient.VariantManager.updateTable(LoginController.sessionId, projectID, cli.getReference().getId(), annIDs, variantFields);

            }

            isModified = true;

        } else {

            //create project
            int projID = ProjectController.getInstance().addProject(projectName, patientFields);

            //add references and annotations
            for (CheckListItem cli : checkListItems) {
                if (cli.isSelected()) {

                    //set custom vcf fields
                    int refID = cli.getReference().getId();
                    manager.setCustomVariantFields(LoginController.sessionId, projID, refID, 0, variantFields);

                    int[] annIDs = cli.getAnnotationIDs();
                    String tablename = manager.createVariantTable(LoginController.sessionId, projID, refID, 0, annIDs, false);
                    manager.addTableToMap(LoginController.sessionId, projID, refID, 0, true, tablename, null);
                }
            }
        }
    }

    private class CheckListItem extends JPanel {

        private final Reference reference;
        private final Map<Integer, Boolean> annIDsMap = new HashMap<Integer, Boolean>();

        private final JCheckBox checkBox;
        private final List<JCheckBox> annBoxes = new ArrayList<JCheckBox>();

        public CheckListItem(Reference ref, Annotation[] annotations) {

            this.reference = ref;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.WHITE);


            checkBox = new JCheckBox(ref.getName());
            checkBox.setMaximumSize(new Dimension(1000,20));
            checkBox.setBackground(getBackground());
            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    boolean selected = checkBox.isSelected();
                    for (JCheckBox annBox : annBoxes) {
                        annBox.setEnabled(selected);
                    }
                }
            });
            ProjectDetails pd = getProjectDetails(ref.getId());
            boolean selected = pd != null;
            checkBox.setSelected(selected);

            JPanel p = new JPanel();
            p.setMaximumSize(new Dimension(1000, 20));
            p.setBackground(Color.white);
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(checkBox);
            p.add(Box.createHorizontalGlue());
            add(p);

            for (final Annotation a : annotations) {

                //make sure annotation is for this reference
                if (a.getReferenceID() != reference.getId()) {
                    continue;
                }

                final JCheckBox b1 = new JCheckBox(a.getProgram() + " " + a.getVersion());
                b1.setMaximumSize(new Dimension(1000,20));
                b1.setBackground(Color.white);
                b1.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        annIDsMap.put(a.getID(), b1.isSelected());
                    }
                });
                annBoxes.add(b1);
                b1.setEnabled(selected);
                b1.setSelected(false);
                annIDsMap.put(a.getID(), false);
                if (pd != null && ArrayUtils.contains(pd.getAnnotationIDs(), a.getID())) {
                    b1.setSelected(true);
                    annIDsMap.put(a.getID(), true);
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
            return checkBox.isSelected();
        }

        public void setSelected(boolean value) {
            checkBox.setSelected(value);
        }

        public Reference getReference() {
            return reference;
        }

        public int[] getAnnotationIDs() {
            int[] result = new int[annIDsMap.size()];
            int i = 0;
            for (Integer key : annIDsMap.keySet()) {
                result[i++] = key;
            }
            return result;
        }
    }

    private ProjectDetails getProjectDetails(int referenceId) {
        for (ProjectDetails pd : projectDetails) {
            if (pd.getReferenceID() == referenceId) {
                return pd;
            }
        }
        return null;
    }

    public boolean isModified() {
        return isModified;
    }
}
