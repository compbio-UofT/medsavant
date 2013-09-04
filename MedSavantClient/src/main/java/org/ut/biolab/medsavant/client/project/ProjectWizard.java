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
package org.ut.biolab.medsavant.client.project;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.shared.model.Reference;
import org.ut.biolab.medsavant.client.reference.NewReferenceDialog;
import org.ut.biolab.medsavant.shared.serverapi.ProjectManagerAdapter;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.ProjectWorker;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author Andrew
 */
public class ProjectWizard extends WizardDialog implements BasicPatientColumns, BasicVariantColumns {

    private static final Log LOG = LogFactory.getLog(ProjectWizard.class);
    private static final String PAGENAME_NAME = "Project Name";
    private static final String PAGENAME_PATIENTS = "Patients";
    private static final String PAGENAME_VCF = "Custom VCF Fields";
    private static final String PAGENAME_REF = "Reference";
    private static final String PAGENAME_NOTIFICATIONS = "Notifications";
    private static final String PAGENAME_COMPLETE = "Complete";
    private static String PAGENAME_CREATE = "Create";
    private final boolean modify;
    private boolean isModified = false;
    private final int projectID;
    private final String originalProjectName;
    private String projectName;
    private CustomField[] customFields;
    private final ProjectDetails[] projectDetails;
    private DefaultTableModel patientFormatModel;
    private DefaultTableModel variantFormatModel;
    private CustomField[] variantFields;
    private List<CheckListItem> checkListItems = new ArrayList<CheckListItem>();
    private boolean variantFieldsChanged = false;
    private final ProjectManagerAdapter manager;
    private JTextField emailField;
    private JCheckBox autoPublish;

    /*
     * modify existing project
     */
    public ProjectWizard(int projID, String projName, CustomField[] fields, ProjectDetails[] details) throws SQLException, RemoteException {
        this.modify = true;
        this.projectID = projID;
        this.originalProjectName = projName;
        this.projectName = projName;
        this.customFields = fields;
        this.projectDetails = details;
        PAGENAME_CREATE = "Modify";
        manager = MedSavantClient.ProjectManager;           
        setupWizard();                
    }

    /*
     * create new project
     */
    public ProjectWizard() throws SQLException, RemoteException {
        modify = false;
        projectID = -1;
        originalProjectName = null;
        projectDetails = new ProjectDetails[0];
        manager = MedSavantClient.ProjectManager;
        setupWizard();
    }

    private void setupWizard() throws SQLException, RemoteException {
        setTitle(modify ? "Modify Project" : "Create Project");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        //add pages
        PageList model = new PageList();
        model.append(getNamePage());
        model.append(getPatientFieldsPage());
        model.append(getVCFFieldsPage());
        model.append(getReferencePage());
        if (modify) {
            model.append(getNotificationsPage());
            model.append(getCreatePage());
        }
        model.append(getCompletionPage());
        setPageList(model);

        //change next action
        setNextAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String pagename = getCurrentPage().getTitle();
                    if (pagename.equals(PAGENAME_NAME) && validateProjectName()) {
                        setCurrentPage(PAGENAME_PATIENTS);
                    } else if (pagename.equals(PAGENAME_PATIENTS) && validatePatientFormatModel()) {
                        setCurrentPage(PAGENAME_VCF);
                    } else if (pagename.equals(PAGENAME_VCF) && validateVariantFormatModel()) {
                        setCurrentPage(PAGENAME_REF);
                    } else if (pagename.equals(PAGENAME_REF) && validateReferences()) {
                        if (modify) {
                            setCurrentPage(PAGENAME_NOTIFICATIONS);
                        } else {
                            setCurrentPage(PAGENAME_COMPLETE);
                        }
                    } else if (pagename.equals(PAGENAME_NOTIFICATIONS)) {
                        setCurrentPage(PAGENAME_CREATE);
                    } else if (pagename.equals(PAGENAME_CREATE)) {
                        setCurrentPage(PAGENAME_COMPLETE);
                    }
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Unable to proceed: %s", ex);
                }
            }
        });

        pack();
        setResizable(true);
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
        if (modify) {
            namefield.setText(projectName);
        }

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
        scrollpane.setPreferredSize(new Dimension(300, 250));
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

        patientFormatModel.addRow(new Object[]{FAMILY_ID.getColumnName(), FAMILY_ID.getTypeString(), true, FAMILY_ID.getAlias(), ""});
        patientFormatModel.addRow(new Object[]{HOSPITAL_ID.getColumnName(), HOSPITAL_ID.getTypeString(), true, HOSPITAL_ID.getAlias(), ""});
        patientFormatModel.addRow(new Object[]{IDBIOMOM.getColumnName(), IDBIOMOM.getTypeString(), true, IDBIOMOM.getAlias(), ""});
        patientFormatModel.addRow(new Object[]{IDBIODAD.getColumnName(), IDBIODAD.getTypeString(), true, IDBIODAD.getAlias(), ""});
        patientFormatModel.addRow(new Object[]{GENDER.getColumnName(), GENDER.getTypeString(), true, GENDER.getAlias(), ""});
        patientFormatModel.addRow(new Object[]{AFFECTED.getColumnName(), AFFECTED.getTypeString(), true, AFFECTED.getAlias(), ""});
        patientFormatModel.addRow(new Object[]{DNA_IDS.getColumnName(), DNA_IDS.getTypeString(), true, DNA_IDS.getAlias(), ""});
        patientFormatModel.addRow(new Object[]{BAM_URL.getColumnName(), BAM_URL.getTypeString(), true, BAM_URL.getAlias(), ""});
        patientFormatModel.addRow(new Object[]{PHENOTYPES.getColumnName(), PHENOTYPES.getTypeString(), true, PHENOTYPES.getAlias(), ""});

        if (modify) {
            for (CustomField f : customFields) {
                patientFormatModel.addRow(new Object[]{f.getColumnName(), f.getTypeString(), f.isFilterable(), f.getAlias(), f.getDescription()});
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
        removeFieldButton.setEnabled(false);
        removeFieldButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int row = table.getSelectedRow();
                // Minus one because patient_id isn't in the table.
                if (row >= BasicPatientColumns.REQUIRED_PATIENT_FIELDS.length - 1) {
                    patientFormatModel.removeRow(row);
                }
                table.setModel(patientFormatModel);
            }
        });
        table.getSelectionModel().addListSelectionListener(new RemovalEnabler(BasicPatientColumns.REQUIRED_PATIENT_FIELDS.length - 1, removeFieldButton));
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

        page.addText("Add extra fields to parse from INFO text in VCF files. ");

        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setPreferredSize(new Dimension(300, 250));
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
                int firstRef = manager.getReferenceIDsForProject(LoginController.getInstance().getSessionID(), projectID)[0];
                CustomField[] fields = manager.getCustomVariantFields(
                        LoginController.getInstance().getSessionID(), projectID, firstRef,
                        manager.getNewestUpdateID(LoginController.getInstance().getSessionID(), projectID, firstRef, false));
                for (CustomField f : fields) {
                    variantFormatModel.addRow(new Object[]{f.getColumnName().toUpperCase(), f.getTypeString(), f.isFilterable(), f.getAlias(), f.getDescription()});
                }
            } catch (Exception ex) {
                LOG.error("Error getting reference IDs for project.", ex);
            }
        } else {
            variantFormatModel.addRow(new Object[]{AA.getColumnName().toUpperCase(), AA.getTypeString(), true, AA.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{AC.getColumnName().toUpperCase(), AC.getTypeString(), true, AC.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{AF.getColumnName().toUpperCase(), AF.getTypeString(), true, AF.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{AN.getColumnName().toUpperCase(), AN.getTypeString(), true, AN.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{BQ.getColumnName().toUpperCase(), BQ.getTypeString(), true, BQ.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{CIGAR.getColumnName().toUpperCase(), CIGAR.getTypeString(), true, CIGAR.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{DB.getColumnName().toUpperCase(), DB.getTypeString(), true, DB.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{DP.getColumnName().toUpperCase(), DP.getTypeString(), true, DP.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{END.getColumnName().toUpperCase(), END.getTypeString(), true, END.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{H2.getColumnName().toUpperCase(), H2.getTypeString(), true, H2.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{MQ.getColumnName().toUpperCase(), MQ.getTypeString(), true, MQ.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{MQ0.getColumnName().toUpperCase(), MQ0.getTypeString(), true, MQ0.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{NS.getColumnName().toUpperCase(), NS.getTypeString(), true, NS.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{SB.getColumnName().toUpperCase(), SB.getTypeString(), true, SB.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{SOMATIC.getColumnName().toUpperCase(), SOMATIC.getTypeString(), true, SOMATIC.getAlias(), ""});
            variantFormatModel.addRow(new Object[]{VALIDATED.getColumnName().toUpperCase(), VALIDATED.getTypeString(), true, VALIDATED.getAlias(), ""});
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
        removeFieldButton.setEnabled(false);
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
        table.getSelectionModel().addListSelectionListener(new RemovalEnabler(0, removeFieldButton));
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

    private AbstractWizardPage getReferencePage() throws SQLException, RemoteException {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_REF) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };
        page.addText("Choose reference genome(s) to add to this project, along\n"
                + "with corresponding annotations. Annotations will be\n"
                + "applied to all variants added to these tables.");

        //setup list
        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setPreferredSize(new Dimension(300, 220));
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
                try {
                    new NewReferenceDialog().setVisible(true);
                    refreshReferencePanel(p);
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Unable to retrieve references and annotations: %s", ex);
                }
            }
        });
        page.addComponent(addRefButton);

        return page;
    }

    private void refreshReferencePanel(JPanel p) throws SQLException, RemoteException {
        try {
            Reference[] references = MedSavantClient.ReferenceManager.getReferences(LoginController.getInstance().getSessionID());
            Annotation[] annotations = MedSavantClient.AnnotationManagerAdapter.getAnnotations(LoginController.getInstance().getSessionID());

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
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
    }

    private AbstractWizardPage getCreatePage() {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_CREATE) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };
        page.addText("You are now ready to " + (modify ? "make changes to" : "create") + " this project. ");

        final JLabel progressLabel = new JLabel("");

        page.addComponent(progressLabel);

        final JButton workButton = new JButton((modify ? "Modify Project" : "Create Project"));
        final JButton publishButton = new JButton("Publish Variants");

        final JComponent j = new JLabel("<html><p>You may continue. The import process will continue in the< background and you will be notified upon completion.</p></html>");


        workButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                j.setVisible(true);
                page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);

                workButton.setEnabled(false);

                new ProjectWorker<Void>("Modifying project", autoPublish.isSelected(), LoginController.getSessionID(), projectID) {
                    @Override
                    protected Void backgroundTask() throws Exception {
                        LOG.info("Requesting modification from server");
                        modifyProject(true, true, true, this);
                        LOG.info("Modification complete");
                        return null;
                    }
                }.execute();

                toFront();
            }
        });

        page.addComponent(ViewUtil.alignRight(workButton));

        page.addComponent(j);
        j.setVisible(false);

        if (modify) {
            page.addComponent(ViewUtil.alignRight(publishButton));
            publishButton.setVisible(false);
        }

        return page;
    }

    private AbstractWizardPage getCompletionPage() {
        final CompletionWizardPage page = new CompletionWizardPage(PAGENAME_COMPLETE) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
                if (modify) {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                } else {
                    fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);

                    new SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            try {
                                createNewProject();
                            } catch (Exception e) {
                                DialogUtils.displayException("Error", "Error trying to create project", e);
                                LOG.error(e);
                            }
                            return null;
                        }

                        @Override
                        protected void done() {
                            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                        }
                    }.execute();
                }
            }
        };

        if (modify) {
            page.addText("You have completed the project modification process.");
        } else {
            page.addText("The project has been created.");
        }

        return page;
    }

    private boolean validateProjectName() {
        try {
            if (manager.containsProject(LoginController.getInstance().getSessionID(), projectName) && (!modify || !projectName.equals(originalProjectName))) {
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
        String validationErr = validateFormatModel(fields, patientFormatModel, BasicPatientColumns.REQUIRED_PATIENT_FIELDS.length - 1);
        if (validationErr == null) {
            customFields = fields.toArray(new CustomField[0]);
            return true;
        } else {
            DialogUtils.displayError(String.format("Individuals table format contains errors\n%s", validationErr));
            return false;
        }
    }

    private boolean validateVariantFormatModel() {
        List<CustomField> fields = new ArrayList<CustomField>();
        String validationErr = validateFormatModel(fields, variantFormatModel, 0);
        if (validationErr == null) {
            variantFields = fields.toArray(new CustomField[0]);
            return true;
        } else {
            DialogUtils.displayError(String.format("Variant table format contains errors.\n%s", validationErr));
            return false;
        }
    }

    private String validateFormatModel(List<CustomField> fields, DefaultTableModel model, int firstRow) {

        for (int row = firstRow; row < model.getRowCount(); row++) {
            String fieldName = (String) model.getValueAt(row, 0);
            String fieldType = (String) model.getValueAt(row, 1);
            Boolean fieldFilterable = (Boolean) model.getValueAt(row, 2);
            String fieldAlias = (String) model.getValueAt(row, 3);
            String fieldDescription = (String) model.getValueAt(row, 4);

            if (fieldName == null || fieldType == null) {
                continue;
            }

            if (!fieldName.matches("^([a-z]|[A-Z]|_|[0-9])+$")) {// ||
                //!fieldType.matches("^([a-z]|[A-Z])+\\([0-9]+\\)$")) {
                return "Field name can contain only letters, numbers, and underscores.";
            }

            if (!fieldName.equals("") && !fieldType.equals("")) {
                if (fieldFilterable == null) {
                    fieldFilterable = false;
                }
                if (fieldAlias == null) {
                    fieldAlias = fieldName;
                }
                if (fieldDescription == null) {
                    fieldDescription = "";
                }
                fields.add(new CustomField(fieldName, fieldType, fieldFilterable, fieldAlias, fieldDescription));
            }
        }

        return null;
    }

    private void createNewProject() throws Exception {
        //create project
        int projID = ProjectController.getInstance().addProject(projectName, customFields);

        //add references and annotations
        for (CheckListItem cli : checkListItems) {
            if (cli.isSelected()) {

                //set custom vcf fields
                int refID = cli.getReference().getID();
                manager.setCustomVariantFields(LoginController.getInstance().getSessionID(), projID, refID, 0, variantFields);

                int[] annIDs = cli.getAnnotationIDs();
                String tablename = manager.createVariantTable(LoginController.getInstance().getSessionID(), projID, refID, 0, annIDs, false);
                manager.addTableToMap(LoginController.getInstance().getSessionID(), projID, refID, 0, true, tablename, annIDs, null);
            }
        }
    }

    private int modifyProject(boolean modifyProjectName, boolean modifyPatientFields, boolean modifyVariants, ProjectWorker projectWorker) throws Exception {

        int updateID = -1;

        if (modifyProjectName) {
            //change project name
            if (!projectName.equals(originalProjectName)) {
                manager.renameProject(LoginController.getInstance().getSessionID(), projectID, projectName);
            }
        }

        if (modifyPatientFields) {
            //modify patientFields
            MedSavantClient.PatientManager.updateFields(LoginController.getInstance().getSessionID(), projectID, customFields);
        }

        if (modifyVariants) {

            //edit references and annotations
            for (CheckListItem cli : checkListItems) {
                ProjectDetails pd = getProjectDetails(cli.getReference().getID());

                //skip if not selected and not existing
                if (!cli.isSelected() && pd == null) {
                    continue;
                }

                //get annotation ids
                int[] annIDs = cli.getAnnotationIDs();


                //add new ref
                if (pd == null && cli.isSelected()) {
                    int refID = cli.getReference().getID();
                    String tablename = manager.createVariantTable(LoginController.getInstance().getSessionID(), projectID, refID, 0, annIDs, false);
                    manager.setCustomVariantFields(LoginController.getInstance().getSessionID(), projectID, refID, 0, variantFields);
                    manager.addTableToMap(LoginController.getInstance().getSessionID(), projectID, refID, 0, true, tablename, annIDs, null);
                    continue;
                } else if (pd != null && !cli.isSelected()) {
                    //remove existing ref
                    manager.removeReferenceForProject(LoginController.getInstance().getSessionID(), projectID, cli.getReference().getID());
                    continue;
                }

                String email = this.emailField.getText();
                //boolean autoPublishWhenComplete = this.autoPublish.isSelected();
                boolean autoPublishWhenComplete = false;
                updateID = MedSavantClient.VariantManager.updateTable(LoginController.getInstance().getSessionID(), projectID, cli.getReference().getID(), annIDs, variantFields, autoPublishWhenComplete, email);
            }
        }

        // TODO: this looks like a hack, what is this used to do?
        ProjectController.getInstance().fireEvent(new ProjectEvent(ProjectEvent.Type.REMOVED, projectName));
        ProjectController.getInstance().fireEvent(new ProjectEvent(ProjectEvent.Type.ADDED, projectName));

        return updateID;
    }

    private AbstractWizardPage getNotificationsPage() {
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_NOTIFICATIONS) {
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

        autoPublish = new JCheckBox("Publish data upon import completion");
        autoPublish.setSelected(true);
        page.addComponent(autoPublish);
        page.addText("If you choose not to automatically publish, you will be prompted to publish manually upon completion. Variant publication logs all users out.");

        return page;
    }

    private class CheckListItem extends JPanel {

        private final Reference reference;
        private final Set<Integer> selectedAnnotations = new HashSet<Integer>();
        private final JCheckBox checkBox;
        private final List<JCheckBox> annBoxes = new ArrayList<JCheckBox>();

        public CheckListItem(Reference ref, Annotation[] annotations) {

            this.reference = ref;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.WHITE);


            checkBox = new JCheckBox(ref.getName());
            checkBox.setMaximumSize(new Dimension(1000, 20));
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
            ProjectDetails pd = getProjectDetails(ref.getID());
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
                if (a.getReferenceID() != reference.getID()) {
                    continue;
                }

                final JCheckBox b1 = new JCheckBox(a.getProgram() + " " + a.getVersion());
                b1.setMaximumSize(new Dimension(1000, 20));
                b1.setBackground(Color.white);
                b1.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (b1.isSelected()) {
                            selectedAnnotations.add(a.getID());
                        } else {
                            selectedAnnotations.remove(a.getID());
                        }
                    }
                });
                annBoxes.add(b1);
                b1.setEnabled(selected);
                b1.setSelected(false);
                if (pd != null && ArrayUtils.contains(pd.getAnnotationIDs(), a.getID())) {
                    b1.setSelected(true);
                    selectedAnnotations.add(a.getID());
                }

                JPanel p1 = new JPanel();
                p1.setMaximumSize(new Dimension(1000, 20));
                p1.setBackground(Color.white);
                p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
                p1.add(Box.createHorizontalStrut(30));
                p1.add(b1);
                p1.add(Box.createHorizontalGlue());
                add(p1);
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
            int[] result = new int[selectedAnnotations.size()];
            int i = 0;
            for (Integer ann : selectedAnnotations) {
                result[i++] = ann;
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

    private class RemovalEnabler implements ListSelectionListener {

        private final int lockedRows;
        private final JButton button;

        RemovalEnabler(int n, JButton b) {
            lockedRows = n;
            button = b;
        }

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            int n = ((ListSelectionModel) lse.getSource()).getMinSelectionIndex();
            button.setEnabled(n >= lockedRows);
        }
    };
}
