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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.dialog.ComboForm;
import org.ut.biolab.medsavant.client.view.util.StandardFixedWidthAppPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.component.field.editable.EditableField;
import org.ut.biolab.medsavant.component.field.editable.EnumEditableField;
import org.ut.biolab.medsavant.component.field.editable.FieldEditedListener;
import org.ut.biolab.medsavant.component.field.editable.StringEditableField;
import org.ut.biolab.medsavant.shared.model.Cohort;

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
    public static final String INDIVIDUAL_ID = "Individual ID";
    public static final String SEX = "Sex";

    // genetic keys
    public static final String DNA_ID = "DNA ID";
    public static final String BAM_URL = "Read Alignment URL";

    // phenotype keys
    public static final String PHENOTYPE = "HPO IDs";
    private KeyValuePairPanel geneticsKVP;
    private KeyValuePairPanel phenotypeKVP;
    private StandardFixedWidthAppPanel content;

    public PatientView() {
        initView();
    }

    public PatientView(Patient patient) {
        initView();
        setPatient(patient);
    }

    private void initView() {
        this.setLayout(new BorderLayout());
        content = new StandardFixedWidthAppPanel();
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
        JPanel subsectionPedigree = content.addBlock("Pedigree");
        JPanel subsectionGenetics = content.addBlock("Genetics");
        JPanel subsectionPhenotypes = content.addBlock("Phenotypes");

        JLabel notCohortMemberLabel = ViewUtil.getGrayItalicizedLabel("This individual is not in a cohort");
        JButton addToCohortButton = ViewUtil.getSoftButton("Add to cohort...");
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
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error adding individuals to cohort: %s", ex);
                }
            }
        });

        subsectionCohort.setLayout(new MigLayout("insets 0"));
        subsectionCohort.add(notCohortMemberLabel,"wrap");
        subsectionCohort.add(addToCohortButton);

        profileKVP = getKVP();

        profileKVP.addKeyWithValue(INDIVIDUAL_ID,
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

        System.out.println("Refreshing view for patient " + patient);

        StringEditableField individualIDField = new StringEditableField();
        individualIDField.setTag(INDIVIDUAL_ID);
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

        EditablePatientField motherField = new EditablePatientField();
        motherField.setValue(patient.getMotherHospitalID());
        motherField.setTag(MOTHER_ID);
        motherField.addFieldEditedListener(this);

        EditablePatientField fatherField = new EditablePatientField();
        fatherField.setValue(patient.getFatherHospitalID());
        fatherField.setTag(FATHER_ID);
        fatherField.addFieldEditedListener(this);

        StringEditableField familyIDField = new StringEditableField();
        familyIDField.setValue(patient.getFamilyID());
        familyIDField.setTag(FAMILY_ID);
        familyIDField.addFieldEditedListener(this);

        profileKVP.setValue(PatientView.INDIVIDUAL_ID, individualIDField);
        profileKVP.setValue(PatientView.SEX, sexField);
        profileKVP.setValue(PatientView.AFFECTED, affectedField);
        profileKVP.setValue(PatientView.MOTHER_ID, motherField);
        profileKVP.setValue(PatientView.FATHER_ID, fatherField);
        profileKVP.setValue(PatientView.FAMILY_ID, familyIDField);

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

        this.updateUI();
    }

    @Override
    public void handleEvent(EditableField f) {
        System.out.println("Field " + f.getTag() + " was edited to " + f.getValue());
        patient.saveToDatabase();
    }
}
