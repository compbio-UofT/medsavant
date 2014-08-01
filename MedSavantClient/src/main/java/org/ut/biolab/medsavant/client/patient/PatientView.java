/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.patient;

import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommHandler;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommRegistry;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.BAMFileComm;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.PatientVariantAnalyzeComm;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.user.UserController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.patient.pedigree.PedigreeCanvas;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.dialog.ComboForm;
import org.ut.biolab.medsavant.client.view.util.StandardFixableWidthAppPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.component.field.editable.EditableField;
import org.ut.biolab.medsavant.component.field.editable.EnumEditableField;
import org.ut.biolab.medsavant.component.field.editable.FieldCommittedListener;
import org.ut.biolab.medsavant.component.field.editable.StringEditableField;
import org.ut.biolab.medsavant.component.field.validator.URLValidator;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;

/**
 *
 * @author mfiume
 */
public class PatientView extends JPanel implements FieldCommittedListener {

    private Patient patient;
    private KeyValuePairPanel profileKVP;

    private boolean isCurrentUserAdmin = false;

    // profile keys
    public static final String FATHER_ID = "Father ID";
    public static final String MOTHER_ID = "Mother ID";
    public static final String FAMILY_ID = "Family ID";
    public static final String AFFECTED = "Affected";
    public static final String HOSPITAL_ID = "Hospital ID";
    public static final String SEX = "Sex";

    // genetic keys
    public static final String DNA_ID = "DNA ID";
    public static final String BAM_URL = "Read Alignment URL";

    // phenotype keys
    public static final String PHENOTYPE = "HPO IDs";
    private KeyValuePairPanel geneticsKVP;
    private KeyValuePairPanel phenotypeKVP;
    private StandardFixableWidthAppPanel content;
    private JPanel cohortListPanel;

    public PatientView() {
        initView();
    }

    public PatientView(Patient patient) {
        initView();
        setPatient(patient);
    }

    private void initView() {
        try {
            isCurrentUserAdmin = MedSavantClient.UserManager.getSessionUsersLevel(LoginController.getSessionID()) == UserLevel.ADMIN;
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setLayout(new BorderLayout());
        content = new StandardFixableWidthAppPanel();
        this.add(content, BorderLayout.CENTER);
        initBlocks();
    }

    private KeyValuePairPanel getKVP() {
        KeyValuePairPanel kvp = new KeyValuePairPanel(1, true);
        return kvp;
    }

    private void initBlocks() {
        JPanel subsectionBasicInfo = content.addBlock("Basic Information");
        JPanel subsectionCohort = content.addBlock("Cohort(s)");
        JPanel subsectionGenetics = content.addBlock("Genetics");
        JPanel subsectionPhenotypes = content.addBlock("Phenotypes");

        JButton addToCohortButton = ViewUtil.getSoftButton("Add to cohort...");
        cohortListPanel = ViewUtil.getClearPanel();
        addToCohortButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Cohort[] cohorts = MedSavantClient.CohortManager.getCohorts(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID());
                    ComboForm form = new ComboForm(cohorts, "Select Cohort", "Select which cohort to add to:");
                    form.setVisible(true);
                    Cohort selected = (Cohort) form.getSelectedValue();
                    if (selected == null) {
                        return;
                    }
                    MedSavantClient.CohortManager.addPatientsToCohort(LoginController.getSessionID(), new int[]{patient.getID()}, selected.getId());
                    PatientView.this.refreshView();
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error adding individuals to cohort: %s", ex);
                }
            }
        });

        subsectionCohort.setLayout(new MigLayout("insets 0"));
        subsectionCohort.add(cohortListPanel, "wrap");
        if (isCurrentUserAdmin) {
            subsectionCohort.add(addToCohortButton);
        }

        profileKVP = getKVP();

        profileKVP.addKeyWithValue(HOSPITAL_ID,
                "");
        profileKVP.addKeyWithValue(SEX,
                "");
        profileKVP.addKeyWithValue(AFFECTED,
                "");
        profileKVP.addKeyWithValue(FAMILY_ID,
                "");
        profileKVP.addKeyWithValue(MOTHER_ID,
                "");
        profileKVP.addKeyWithValue(FATHER_ID,
                "");

        subsectionBasicInfo.add(profileKVP);

        geneticsKVP = getKVP();

        geneticsKVP.addKeyWithValue(DNA_ID,
                "");
        geneticsKVP.addKeyWithValue(BAM_URL,
                "");

        subsectionGenetics.add(geneticsKVP);

        phenotypeKVP = getKVP();

        phenotypeKVP.addKeyWithValue(PHENOTYPE,
                "");

        subsectionPhenotypes.add(phenotypeKVP);

    }

    void setPatient(Patient patient) {
        this.patient = patient;
        refreshView();
    }

    private void refreshView() {

        content.setTitle(patient.getHospitalID());

        StringEditableField individualIDField = new StringEditableField();
        individualIDField.setTag(HOSPITAL_ID);
        individualIDField.setValue(patient.getHospitalID());
        individualIDField.addFieldComittedListener(this);

        EnumEditableField sexField = new EnumEditableField(new String[]{"Undesignated", "Male", "Female"});
        sexField.setValue(patient.getSex());
        sexField.setTag(SEX);
        sexField.addFieldComittedListener(this);

        EnumEditableField affectedField = new EnumEditableField(new String[]{"Yes", "No"});
        affectedField.setValue(patient.isAffected() ? "Yes" : "No");
        affectedField.setTag(AFFECTED);
        affectedField.addFieldComittedListener(this);

        EditablePatientField motherField = new EditablePatientField(true);
        motherField.setValue(patient.getMotherHospitalID());
        motherField.setTag(MOTHER_ID);
        motherField.addFieldComittedListener(this);

        EditablePatientField fatherField = new EditablePatientField(true);
        fatherField.setValue(patient.getFatherHospitalID());
        fatherField.setTag(FATHER_ID);
        fatherField.addFieldComittedListener(this);

        StringEditableField familyIDField = new StringEditableField();
        familyIDField.setValue(patient.getFamilyID());
        familyIDField.setTag(FAMILY_ID);
        familyIDField.addFieldComittedListener(this);

        JButton pedigree = ViewUtil.getSoftButton("Pedigree");

        if (patient.getFamilyID() == null || patient.getFamilyID().isEmpty()) {
            pedigree.setEnabled(false);
        } else {
            pedigree.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JDialog f = new JDialog(MedSavantFrame.getInstance(), "Pedigree Viewer", true);
                    PedigreeCanvas pc = new PedigreeCanvas();
                    pc.setFamilyName(patient.getFamilyID());
                    pc.showPedigreeFor(patient.getID());

                    f.setPreferredSize(new Dimension(650, 500));
                    f.setLayout(new BorderLayout());
                    f.add(pc, BorderLayout.CENTER);
                    f.pack();
                    f.setLocationRelativeTo(MedSavantFrame.getInstance());
                    f.setVisible(true);
                }

            });
        }
        
        final JButton dnaIDButton = ViewUtil.getSoftButton("Open with...");

        if (patient.getDnaID() == null || patient.getDnaID().isEmpty()) {
            dnaIDButton.setEnabled(false);
        } else {
            dnaIDButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JPopupMenu m = new JPopupMenu();
                    Set<AppCommHandler> handlers = AppCommRegistry.getInstance().getHandlersForEvent(PatientVariantAnalyzeComm.class);
                    final PatientVariantAnalyzeComm event = new PatientVariantAnalyzeComm(null, patient.getID());
                    for (final AppCommHandler handler : handlers) {
                        JMenuItem item = new JMenuItem(handler.getHandlerName());

                        ImageIcon icon = handler.getHandlerIcon();

                        if (icon != null) {
                            int iconSize = 22;
                            Image img = ViewUtil.getScaledInstance(
                                    icon.getImage(),
                                    iconSize,
                                    iconSize,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR,
                                    true);
                            item.setIcon(new ImageIcon(img));
                        }

                        ActionListener l = new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                handler.handleCommEvent(event);
                            }

                        };
                        item.addActionListener(l);
                        m.add(item);
                    }

                    m.show(dnaIDButton, 0, (int) dnaIDButton.getSize().getHeight());
                }

            });
        }

        final JButton bamViewButton = ViewUtil.getSoftButton("Open with...");

        if (patient.getBamURL() == null || patient.getBamURL().isEmpty()) {
            bamViewButton.setEnabled(false);
        } else {
            bamViewButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JPopupMenu m = new JPopupMenu();
                    Set<AppCommHandler> handlers = AppCommRegistry.getInstance().getHandlersForEvent(BAMFileComm.class);
                    URL u = null;
                    try {
                        u = new URL(patient.getBamURL());
                    } catch (MalformedURLException ex) {
                    }
                    final BAMFileComm event = new BAMFileComm(null, u);
                    for (final AppCommHandler handler : handlers) {
                        JMenuItem item = new JMenuItem(handler.getHandlerName());

                        ImageIcon icon = handler.getHandlerIcon();

                        if (icon != null) {
                            int iconSize = 22;
                            Image img = ViewUtil.getScaledInstance(
                                    icon.getImage(),
                                    iconSize,
                                    iconSize,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR,
                                    true);
                            item.setIcon(new ImageIcon(img));
                        }

                        ActionListener l = new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                handler.handleCommEvent(event);
                            }

                        };
                        item.addActionListener(l);
                        m.add(item);
                    }

                    m.show(bamViewButton, 0, (int) bamViewButton.getSize().getHeight());
                }

            });
        }

        profileKVP.setValue(PatientView.HOSPITAL_ID, individualIDField);
        profileKVP.setValue(PatientView.SEX, sexField);
        profileKVP.setValue(PatientView.AFFECTED, affectedField);
        profileKVP.setValue(PatientView.MOTHER_ID, motherField);
        profileKVP.setValue(PatientView.FATHER_ID, fatherField);
        profileKVP.setValue(PatientView.FAMILY_ID, familyIDField);
        profileKVP.setAdditionalColumn(PatientView.FAMILY_ID, 0, pedigree);

        StringEditableField dnaIDField = new StringEditableField();
        dnaIDField.setValue(patient.getDnaID());
        dnaIDField.setTag(DNA_ID);
        dnaIDField.setValue(patient.getDnaID());
        dnaIDField.addFieldComittedListener(this);

        StringEditableField bamURLField = new StringEditableField();
        bamURLField.setValidator(new URLValidator());
        bamURLField.setValue(patient.getBamURL());
        bamURLField.setTag(BAM_URL);
        bamURLField.addFieldComittedListener(this);

        geneticsKVP.setValue(PatientView.DNA_ID, dnaIDField);
        geneticsKVP.setAdditionalColumn(PatientView.DNA_ID, 0, dnaIDButton);

        geneticsKVP.setValue(PatientView.BAM_URL, bamURLField);
        geneticsKVP.setAdditionalColumn(PatientView.BAM_URL, 0, bamViewButton);

        StringEditableField phenotypeField = new StringEditableField();
        phenotypeField.setValue(patient.getPhenotypes());
        phenotypeField.setTag(PHENOTYPE);
        phenotypeField.setValue(patient.getPhenotypes());
        phenotypeField.addFieldComittedListener(this);

        if (!isCurrentUserAdmin) {
            individualIDField.setAutonomousEditingEnabled(false);
            sexField.setAutonomousEditingEnabled(false);
            affectedField.setAutonomousEditingEnabled(false);
            motherField.setAutonomousEditingEnabled(false);
            fatherField.setAutonomousEditingEnabled(false);
            individualIDField.setAutonomousEditingEnabled(false);
            familyIDField.setAutonomousEditingEnabled(false);
            bamURLField.setAutonomousEditingEnabled(false);
            dnaIDField.setAutonomousEditingEnabled(false);
            phenotypeField.setAutonomousEditingEnabled(false);
        }

        phenotypeKVP.setValue(PatientView.PHENOTYPE, phenotypeField);

        try {
            List<Cohort> cohorts = MedSavantClient.PatientManager.getCohortsForPatient(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), patient.getID());

            cohortListPanel.removeAll();
            cohortListPanel.setLayout(new MigLayout("wrap, insets 0"));
            if (cohorts.isEmpty()) {
                JLabel noCohortMemberLabel = ViewUtil.getGrayItalicizedLabel("This individual is not in a cohort");
                cohortListPanel.add(noCohortMemberLabel);
            } else {
                for (Cohort c : cohorts) {
                    cohortListPanel.add(new JLabel(c.getName()));
                }
            }

        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.updateUI();
    }

    @Override
    public void handleCommitEvent(EditableField f) {

        if (f.getTag().equals(FATHER_ID)) {
            patient.setFatherHospitalID((String) f.getValue());

        } else if (f.getTag().equals(MOTHER_ID)) {
            patient.setMotherHospitalID((String) f.getValue());

        } else if (f.getTag().equals(FAMILY_ID)) {
            patient.setFamilyID((String) f.getValue());

        } else if (f.getTag().equals(AFFECTED)) {
            patient.setAffected(((String) f.getValue()).equals("Yes"));

        } else if (f.getTag().equals(HOSPITAL_ID)) {
            patient.setHospitalID((String) f.getValue());

        } else if (f.getTag().equals(SEX)) {
            patient.setSex((String) f.getValue());

        } else if (f.getTag().equals(DNA_ID)) {
            patient.setDnaID((String) f.getValue());

        } else if (f.getTag().equals(BAM_URL)) {
            patient.setBamURL((String) f.getValue());

        } else if (f.getTag().equals(PHENOTYPE)) {
            patient.setPhenotypes((String) f.getValue());
        }

        patient.saveToDatabase();
    }
}
