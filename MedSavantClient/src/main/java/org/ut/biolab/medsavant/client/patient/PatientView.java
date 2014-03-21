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

import org.ut.biolab.medsavant.client.view.component.TiledJPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXPanel;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.component.HeroPanel;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.component.RoundedShadowPanel;
import org.ut.biolab.medsavant.client.view.dialog.ComboForm;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.component.field.editable.EditableField;
import org.ut.biolab.medsavant.component.field.editable.EnumEditableField;
import org.ut.biolab.medsavant.component.field.editable.FieldEditedListener;
import org.ut.biolab.medsavant.component.field.editable.StringEditableField;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author mfiume
 */
public class PatientView extends JPanel implements FieldEditedListener {

    private Patient patient;
    private JPanel content;
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
    private JLabel title;

    public PatientView() {
        initView();
    }

    public static void main(String[] arg) {
        JFrame f = new JFrame();
        PatientView v = new PatientView();
        f.add(v);
        f.pack();
        f.setVisible(true);
    }

    public PatientView(Patient patient) {
        initView();
        setPatient(patient);
    }

    private void initView() {

        this.setLayout(new BorderLayout());
        //this.heroPanel = new HeroPanel();
        //this.add(heroPanel, BorderLayout.CENTER);

        content = ViewUtil.getClearPanel();
        //content.setBackground(ViewUtil.getLightGrayBackgroundColor());
        
        JPanel fixedWidth = ViewUtil.getDefaultFixedWidthPanel(content);
        
        StandardAppContainer sac = new StandardAppContainer(fixedWidth,true);
        this.add(sac,BorderLayout.CENTER);
        
        sac.setBackground(ViewUtil.getLightGrayBackgroundColor());

        initContent();
    }

    private JPanel initContent() {
        
        content.setLayout(new MigLayout("fillx, filly, wrap"));
        
        title = ViewUtil.getLargeGrayLabel("");
        content.add(title);

        /*
         JTabbedPane pane = ViewUtil.getMSTabedPane(true);
         pane.add("Profile", ViewUtil.getClearBorderlessScrollPane(getProfileSection()));
         pane.add("Genetics", getGeneticsSection());
         pane.add("Phenotypes", getPhenotypesSection());
         */
        content.add(getProfileSection(),"width 100%");

        return content;
    }

    /*
     private void addSectionHeader(String string, JPanel p, Color c) {
     JLabel l = new JLabel(string);
     l.setFont(ViewUtil.getBigTitleFont().deriveFont(Font.PLAIN));

     p.add(l, "wrap, gapy 0");

     JPanel band = new JPanel();
     band.setPreferredSize(new Dimension(999, 1));
     band.setBackground(new Color(61, 61, 61));
     band.setBackground(c);

     p.add(band, "growx 1.0, wrap, height 1, hmax 1");
     }
     */
    private void styleButton(JButton button) {
        button.setFocusable(false);
        ViewUtil.makeSmall(button);
    }

    private KeyValuePairPanel getKVP() {
        KeyValuePairPanel kvp = new KeyValuePairPanel(1, true);
        //kvp.setXPadding(10);
        //kvp.setYPadding(5);
        return kvp;
    }

    int currentSection = 0;
    int numSections = 5;

    private JPanel getProfileSection() {

        JPanel section = ViewUtil.getClearPanel();
        section.setLayout(new MigLayout("insets 0, fillx, wrap"));

        //JPanel subSectionContainer = new RoundedShadowPanel();
        //subSectionContainer.setLayout(new MigLayout("wrap, width 800"));
        JPanel subsectionBasicInfo = createSubSectionTemplate("Basic Information", "icon/patient/info-25.png");//ViewUtil.getClearPanel();

        JPanel subsectionCohort = createSubSectionTemplate("Cohort(s)", "icon/patient/info-25.png");//ViewUtil.getClearPanel();

        JPanel subsectionPedigree = createSubSectionTemplate("Pedigree", "icon/patient/info-25.png");//ViewUtil.getClearPanel();

        JPanel subsectionGenetics = createSubSectionTemplate("Genetics", "icon/patient/info-25.png");//ViewUtil.getClearPanel();

        JPanel subsectionPhenotypes = createSubSectionTemplate("Phenotypes", "icon/patient/info-25.png");//ViewUtil.getClearPanel();

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
                    MedSavantClient.CohortManager.addPatientsToCohort(LoginController.getSessionID(), new int[] {patient.getID()}, selected.getId());
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error adding individuals to cohort: %s", ex);
                }
            }
        });
        
        subsectionCohort.add(notCohortMemberLabel);
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

        section.add(subsectionBasicInfo,"width 100%");

        section.add(subsectionCohort,"width 100%");

        section.add(subsectionPedigree,"width 100%");

        section.add(subsectionGenetics,"width 100%");

        section.add(subsectionPhenotypes,"width 100%");

        //section.add(subSectionContainer,"align 0 0");
        return section;
    }

    private JPanel createSubSectionTemplate(String name, String iconPath) {

        Color c = ViewUtil.getColor(currentSection++, numSections);
        JPanel subsection = ViewUtil.getWhiteLineBorderedPanel();//ViewUtil.getClearPanel();//new JPanel();
        subsection.setLayout(new MigLayout("wrap 1, fillx"));

        JLabel l = new JLabel(name);
        l.setFont(ViewUtil.getMediumTitleFont());
        subsection.add(l);

        return subsection;
    }

    private JPanel getPhenotypesSection() {
        JPanel section = ViewUtil.getClearPanel();
        section.setLayout(new MigLayout());

        phenotypeKVP = getKVP();
        phenotypeKVP.addKeyWithValue(PHENOTYPE, "");

        section.add(phenotypeKVP, "wrap");

        return section;
    }

    private void addSection(JPanel section, JPanel content) {
        addSection(section, content, 1, 1);
    }

    private void addSection(JPanel section, JPanel content, int spanx) {
        addSection(section, content, spanx, 1);
    }

    private void addSection(JPanel section, JPanel content, int spanx, int spany) {
        content.add(section, String.format("align 0 0, span %d %d", spanx, spany));
    }

    private JPanel getCohortSection() {
        JPanel section = ViewUtil.getClearPanel();
        section.setLayout(new MigLayout());
        return section;
    }

    private JPanel getPedigreeSection() {
        JPanel section = ViewUtil.getClearPanel();
        section.setLayout(new MigLayout());
        return section;
    }

    private JPanel getNotesSection() {
        JPanel section = ViewUtil.getClearPanel();
        section.setLayout(new MigLayout());
        return section;
    }

    void setPatient(Patient patient) {
        this.patient = patient;
        refreshView();
    }

    private void refreshView() {
        
        title.setText(patient.getHospitalID());

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

    private String substituteNoneIfNull(String str) {
        return substituteIfNull(str, "<none>");
    }

    private String substituteIfNull(String str, String substitute) {
        return str;
    }

    @Override
    public void handleEvent(EditableField f) {
        System.out.println("Field " + f.getTag() + " was edited to " + f.getValue());
        patient.saveToDatabase();
    }
}
