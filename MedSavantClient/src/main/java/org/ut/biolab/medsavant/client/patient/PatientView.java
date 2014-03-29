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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
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
import org.ut.biolab.medsavant.component.field.editable.FieldEditedListener;
import org.ut.biolab.medsavant.component.field.editable.StringEditableField;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.SimplePatient;

/**
 *
 * @author mfiume
 */
public class PatientView extends JPanel implements FieldEditedListener {

    private Patient patient;
    private KeyValuePairPanel profileKVP;

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
        subsectionCohort.add(addToCohortButton);

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
        individualIDField.addFieldEditedListener(this);

        EnumEditableField sexField = new EnumEditableField(new String[]{"Undesignated", "Male", "Female"});
        sexField.setValue(patient.getSex());
        sexField.setTag(SEX);
        sexField.addFieldEditedListener(this);

        EnumEditableField affectedField = new EnumEditableField(new String[]{"Yes", "No"});
        affectedField.setValue(patient.isAffected() ? "Yes" : "No");
        affectedField.setTag(AFFECTED);
        affectedField.addFieldEditedListener(this);

        EditablePatientField motherField = new EditablePatientField(true);
        motherField.setValue(patient.getMotherHospitalID());
        motherField.setTag(MOTHER_ID);
        motherField.addFieldEditedListener(this);

        EditablePatientField fatherField = new EditablePatientField(true);
        fatherField.setValue(patient.getFatherHospitalID());
        fatherField.setTag(FATHER_ID);
        fatherField.addFieldEditedListener(this);

        StringEditableField familyIDField = new StringEditableField();
        familyIDField.setValue(patient.getFamilyID());
        familyIDField.setTag(FAMILY_ID);
        familyIDField.addFieldEditedListener(this);

        JButton pedigree = ViewUtil.getSoftButton("Pedigree");
        System.out.println(patient.getFamilyID());
        if (patient.getFamilyID() == null || patient.getFamilyID().isEmpty()) {
            pedigree.setEnabled(false);
        } else {
            pedigree.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JDialog f = new JDialog(MedSavantFrame.getInstance(), "Pedigree", true);
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

        profileKVP.setValue(PatientView.HOSPITAL_ID, individualIDField);
        profileKVP.setValue(PatientView.SEX, sexField);
        profileKVP.setValue(PatientView.AFFECTED, affectedField);
        profileKVP.setValue(PatientView.MOTHER_ID, motherField);
        profileKVP.setValue(PatientView.FATHER_ID, fatherField);
        profileKVP.setValue(PatientView.FAMILY_ID, familyIDField);
        profileKVP.setAdditionalColumn(FAMILY_ID, 0, pedigree);

        StringEditableField dnaIDField = new StringEditableField();
        dnaIDField.setValue(patient.getDnaID());
        dnaIDField.setTag(DNA_ID);
        dnaIDField.setValue(patient.getDnaID());
        dnaIDField.addFieldEditedListener(this);

        StringEditableField bamURLField = new StringEditableField();
        bamURLField.setValue(patient.getBamURL());
        bamURLField.setTag(BAM_URL);
        bamURLField.addFieldEditedListener(this);

        geneticsKVP.setValue(PatientView.DNA_ID, dnaIDField);
        geneticsKVP.setValue(PatientView.BAM_URL, bamURLField);

        StringEditableField phenotypeField = new StringEditableField();
        phenotypeField.setValue(patient.getPhenotypes());
        phenotypeField.setTag(PHENOTYPE);
        phenotypeField.setValue(patient.getPhenotypes());
        phenotypeField.addFieldEditedListener(this);

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
    public void handleEvent(EditableField f) {

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
