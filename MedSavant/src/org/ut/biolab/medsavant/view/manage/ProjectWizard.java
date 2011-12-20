/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.event.ActionEvent;
import org.ut.biolab.medsavant.view.dialog.NewReferenceDialog;
import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.format.PatientFormat;
import org.ut.biolab.medsavant.db.format.VariantFormat;
import org.ut.biolab.medsavant.db.model.Annotation;
import org.ut.biolab.medsavant.db.model.ProjectDetails;
import org.ut.biolab.medsavant.db.model.Reference;
import org.ut.biolab.medsavant.db.util.query.AnnotationQueryUtil;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.MainFrame;
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

    /* modify existing project */
    public ProjectWizard(int projectId, String projectName, List<CustomField> fields, List<ProjectDetails> projectDetails){
        this.projectId = projectId;
        this.modify = true;
        this.originalProjectName = projectName;
        this.projectName = projectName;
        this.patientFields = fields;
        this.projectDetails = projectDetails;
        this.PAGENAME_CREATE = "Modify";
        setupWizard();
    }

    /* create new project */
    public ProjectWizard(){
        setupWizard();
    }

    private void setupWizard(){
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
        final WizardDialog instance = this;
        this.setNextAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String pagename = instance.getCurrentPage().getTitle();
                if(pagename.equals(PAGENAME_NAME) && validateProjectName()){
                    instance.setCurrentPage(PAGENAME_PATIENTS);
                } else if (pagename.equals(PAGENAME_PATIENTS) && validatePatientFormatModel()){
                    instance.setCurrentPage(PAGENAME_VCF);
                } else if (pagename.equals(PAGENAME_VCF) && validateVariantFormatModel()){
                    instance.setCurrentPage(PAGENAME_REF);
                } else if (pagename.equals(PAGENAME_REF) && validateReferences()){
                    instance.setCurrentPage(PAGENAME_CREATE);
                }
            }
        });

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private AbstractWizardPage getNamePage(){

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_NAME){
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                if(projectName == null || projectName.equals("")){
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
        namefield.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                if(namefield.getText() != null && !namefield.getText().equals("")){
                    projectName = namefield.getText();
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        });
        page.addComponent(namefield);
        if(modify) namefield.setText(projectName);

        return page;
    }

    private AbstractWizardPage getPatientFieldsPage(){

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_PATIENTS){
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

        final JTable table = new JTable(){
            @Override
            public Class<?> getColumnClass(int column) {
                if(column == 2){
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }
        };

        patientFormatModel = new DefaultTableModel(){
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

        if(modify){
            for(CustomField f : patientFields){
                patientFormatModel.addRow(new Object[]{f.getColumnName(), f.getColumnTypeString(), f.isFilterable(), f.getAlias(), f.getDescription()});
            }
        }

        table.setModel(patientFormatModel);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        scrollpane.getViewport().add(table);
        page.addComponent(scrollpane);

        JButton addFieldButton = new JButton("Add Field");
        addFieldButton.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e){
                patientFormatModel.addRow(new Object[5]);
                table.setModel(patientFormatModel);
            }
        });
        page.addComponent(addFieldButton);

        JButton removeFieldButton = new JButton("Remove Field");
        removeFieldButton.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e){
                int row = table.getSelectedRow();
                if(row >= MedSavantDatabase.DefaultpatientTableSchema.getNumFields()-1){
                    patientFormatModel.removeRow(row);
                }
                table.setModel(patientFormatModel);
            }
        });
        page.addComponent(removeFieldButton);

        return page;
    }

    private AbstractWizardPage getVcfFieldsPage(){

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_VCF){
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

        final JTable table = new JTable(){
            @Override
            public Class<?> getColumnClass(int column) {
                if(column == 2){
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

        if(modify){
            try {
                List<CustomField> fields = ProjectQueryUtil.getCustomVariantFields(projectId);
                for(CustomField f : fields){
                    variantFormatModel.addRow(new Object[]{f.getColumnName().toUpperCase(), f.getColumnTypeString(), f.isFilterable(), f.getAlias(), f.getDescription()});
                }
            } catch (SQLException ex) {
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
            public void keyTyped(KeyEvent e) {
                variantFieldsChanged = true;
            }
            public void keyPressed(KeyEvent e) {
                variantFieldsChanged = true;
            }
            public void keyReleased(KeyEvent e) {
                variantFieldsChanged = true;
            }
        });

        JButton addFieldButton = new JButton("Add Field");
        addFieldButton.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e){
                variantFormatModel.addRow(new Object[2]);
                table.setModel(variantFormatModel);
                variantFieldsChanged = true;
            }
        });
        page.addComponent(addFieldButton);

        JButton removeFieldButton = new JButton("Remove Field");
        removeFieldButton.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e){
                int row = table.getSelectedRow();
                if(row >= 0){
                    variantFormatModel.removeRow(row);
                    table.setModel(variantFormatModel);
                    variantFieldsChanged = true;
                }
            }
        });
        page.addComponent(removeFieldButton);

        return page;
    }

    private String getLengthString(int len){
        if(len > 0){
            return "(" + len + ")";
        } else {
            return "";
        }
    }

    private AbstractWizardPage getReferencePage(){

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_REF){
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
            public void mouseReleased(MouseEvent e){
                NewReferenceDialog d = new NewReferenceDialog(MainFrame.getInstance(),true);
                d.setVisible(true);
                refreshReferencePanel(p);
            }
        });
        page.addComponent(addRefButton);

        return page;
    }

    private void refreshReferencePanel(JPanel p){
        List<Reference> references = null;
        List<Annotation> annotations = null;
        try {
            references = ReferenceQueryUtil.getReferences();
            annotations = AnnotationQueryUtil.getAnnotations();
        } catch(SQLException ex) {
            Logger.getLogger(ProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
        }

        p.removeAll();
        this.checkListItems.clear();
        for(Reference r : references){
            CheckListItem cli = new CheckListItem(r, annotations);
            this.checkListItems.add(cli);
            p.add(cli);
        }
        p.add(Box.createVerticalGlue());

        p.updateUI();
        p.repaint();
    }

    private AbstractWizardPage getCreatePage() {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_CREATE){
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };
        page.addText(
                "You are now ready to " + (modify ? "make changes to" : "create") + " this project. ");

        final WizardDialog instance = this;

        final JLabel progressLabel = new JLabel("");
        final JProgressBar progressBar = new JProgressBar();

        page.addComponent(progressLabel);
        page.addComponent(progressBar);

        final JButton startButton = new JButton((modify ? "Modify Project" : "Create Project"));
        startButton.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e){
                startButton.setEnabled(false);
                page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                progressBar.setIndeterminate(true);
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            createProject();
                            ((CompletionWizardPage)instance.getPageByTitle(PAGENAME_COMPLETE)).addText(
                                    "Project " + projectName + " has been " + (modify ? "modified." : "created."));
                            instance.setCurrentPage(PAGENAME_COMPLETE);
                        } catch (SQLException ex) {
                            MiscUtils.checkSQLException(ex);
                            DialogUtils.displayException("Error", "There was an error while trying to create your project. ", ex);
                            Logger.getLogger(ProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
                            instance.setVisible(false);
                            instance.dispose();
                        }
                    }
                };
                t.start();
            }

        });

        page.addComponent(ViewUtil.alignRight(startButton));

        return page;
    }

    private AbstractWizardPage getCompletionPage(){
        CompletionWizardPage page = new CompletionWizardPage(PAGENAME_COMPLETE){
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };
        return page;
    }

    private boolean validateProjectName(){
        try {
            if(ProjectQueryUtil.containsProject(projectName) && (!modify || !projectName.equals(originalProjectName))){
                JOptionPane.showMessageDialog(this, "Project name already in use. ", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
            DialogUtils.displayException("Error", "Error trying to create project", ex);
        }
        return false;
    }

    private boolean validateReferences() {
        for(CheckListItem cli : checkListItems){
            if(cli.isSelected()){
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
        if(!success){
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

    private boolean validateVariantFormatModel(){
        variantFields = new ArrayList<CustomField>();
        boolean success = validateFormatModel(variantFields, variantFormatModel, 0);
        if(!success){
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

    private boolean validateFormatModel(List<CustomField> fields, DefaultTableModel model, int firstRow){

        for(int row = firstRow; row < model.getRowCount(); row++){
            String fieldName = (String)model.getValueAt(row, 0);
            String fieldType = (String)model.getValueAt(row, 1);
            Boolean fieldFilterable = (Boolean)model.getValueAt(row, 2);
            String fieldAlias = (String)model.getValueAt(row, 3);
            String fieldDescription = (String)model.getValueAt(row, 4);

            if(fieldName == null || fieldType == null){
                continue;
            }

            if(!fieldName.matches("^([a-z]|[A-Z]|_|[0-9])+$")){// ||
                    //!fieldType.matches("^([a-z]|[A-Z])+\\([0-9]+\\)$")){
                return false;
            }

            if(!fieldName.equals("") && !fieldType.equals("")){
                if(fieldFilterable == null) fieldFilterable = false;
                if(fieldAlias == null) fieldAlias = fieldName;
                if(fieldDescription == null) fieldDescription = "";
                fields.add(new CustomField(fieldName, fieldType, fieldFilterable, fieldAlias, fieldDescription));
            }
        }

        return true;
    }

    private void createProject() throws SQLException {
        if(modify){

            //change project name
            if(!projectName.equals(originalProjectName)){
                ProjectQueryUtil.renameProject(projectId, projectName);
            }

            //modify patientFields
            PatientQueryUtil.updateFields(projectId, patientFields);

            //modify variantFields
            if(variantFieldsChanged){
                ProjectQueryUtil.setCustomVariantFields(projectId, variantFields, false, LoginController.getUsername());
            }

            //edit references and annotations
            for(CheckListItem cli : checkListItems){
                ProjectDetails pd = getProjectDetails(cli.getReference().getId());

                //add, remove refs
                if(pd == null && cli.isSelected()){
                    ProjectQueryUtil.createVariantTable(projectId, cli.getReference().getId(), 0);
                } else if (pd != null && !cli.isSelected()){
                    ProjectQueryUtil.removeReferenceForProject(projectId, cli.getReference().getId());
                }

                //add, remove annotations
                if(cli.isSelected()){
                    List<Integer> annotationIds = cli.getAnnotationIds();
                    List<Integer> currentIds = new ArrayList<Integer>();
                    int[] l1 = AnnotationQueryUtil.getAnnotationIds(projectId, cli.getReference().getId());
                    for(int i = 0; i < l1.length; i++){
                        currentIds.add(l1[i]);
                    }

                    boolean containsAll = true;
                    for(Integer id : annotationIds){
                        if(!currentIds.contains(id)){
                            containsAll = false;
                        }
                    }
                    if(!containsAll || annotationIds.size() != currentIds.size()){
                        String s = "";
                        if(!annotationIds.isEmpty()){
                            for(Integer i : annotationIds){
                                s += i + ",";
                            }
                            s = s.substring(0, s.length()-1);
                        }
                        ProjectQueryUtil.setAnnotations(projectId, cli.getReference().getId(), s, true, LoginController.getUsername());
                    }
                }
            }


            isModified = true;
        } else {

            //create project
            int projectid = ProjectController.getInstance().addProject(projectName, patientFields);

            //set custom vcf fields
            ProjectQueryUtil.setCustomVariantFields(projectid, variantFields, true, LoginController.getUsername());

            //add references and annotations
            for(CheckListItem cli : checkListItems){
                if(cli.isSelected()){

                    List<Integer> annotationIds = cli.getAnnotationIds();
                    if(!annotationIds.isEmpty()){
                        String s = "";
                        for(Integer i : annotationIds){
                            s += i + ",";
                        }
                        s = s.substring(0, s.length()-1);
                        ProjectQueryUtil.setAnnotations(projectid, cli.getReference().getId(), s, false, LoginController.getUsername());
                    }

                    int[] annIds = new int[annotationIds.size()];
                    for(int i = 0; i < annotationIds.size(); i++) { annIds[i] = annotationIds.get(i); }
                    ProjectQueryUtil.createVariantTable(projectid, cli.getReference().getId(), 0, (annotationIds.isEmpty() ? null : annIds), false);

                    //ProjectQueryUtil.createVariantTable(projectid, cli.getReference().getId(), 0);

                }
            }
        }
    }

    private class CheckListItem extends JPanel {

        private boolean selected = false;
        private Reference reference;
        private Map<Integer, Boolean> annIdsMap = new HashMap<Integer, Boolean>();
        private List<JCheckBox> annBoxes = new ArrayList<JCheckBox>();

        public CheckListItem(Reference reference, List<Annotation> annotations){

            this.reference = reference;

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBackground(Color.white);

            ProjectDetails pd = getProjectDetails(reference.getId());
            selected = (pd != null);

            final JCheckBox b = new JCheckBox(reference.getName());
            b.setMaximumSize(new Dimension(1000,20));
            b.setBackground(Color.white);
            b.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    selected = b.isSelected();
                    for(JCheckBox annBox : annBoxes){
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

            for(final Annotation a : annotations){

                //make sure annotation is for this reference
                if(a.getReferenceId() != reference.getId()){
                    continue;
                }

                final JCheckBox b1 = new JCheckBox(a.getProgram() + " " + a.getVersion());
                b1.setMaximumSize(new Dimension(1000,20));
                b1.setBackground(Color.white);
                b1.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        annIdsMap.put(a.getId(), b1.isSelected());
                    }
                });
                annBoxes.add(b1);
                b1.setEnabled(selected);
                b1.setSelected(false);
                annIdsMap.put(a.getId(), false);
                if(pd != null && pd.getAnnotationIds().contains(a.getId())){
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

        public boolean isSelected(){
            return selected;
        }

        public Reference getReference(){
            return reference;
        }

        public List<Integer> getAnnotationIds(){
            List<Integer> ids = new ArrayList<Integer>();
            for(Integer key : annIdsMap.keySet()){
                if(annIdsMap.get(key)){
                    ids.add(key);
                }
            }
            return ids;
        }
    }

    private ProjectDetails getProjectDetails(int referenceId){
        for(ProjectDetails pd : projectDetails){
            if(pd.getReferenceId() == referenceId){
                return pd;
            }
        }
        return null;
    }

    public boolean isModified(){
        return isModified;
    }

}
