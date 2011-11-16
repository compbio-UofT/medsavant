/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import org.ut.biolab.medsavant.view.dialog.NewReferenceDialog;
import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;
import java.awt.BorderLayout;
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
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.Annotation;
import org.ut.biolab.medsavant.db.model.ProjectDetails;
import org.ut.biolab.medsavant.db.model.Reference;
import org.ut.biolab.medsavant.db.util.query.AnnotationQueryUtil;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil;
import org.ut.biolab.medsavant.view.MainFrame;

/**
 *
 * @author Andrew
 */
public class ProjectWizard extends WizardDialog {
    
    private boolean modify = false;
    private boolean isModified = false;
    
    private int projectId;
    private String originalProjectName;
    private String projectName;
    private DefaultTableModel formatModel;
    private String validationError = "";
    private List<CustomField> fields;
    private List<ProjectDetails> projectDetails = new ArrayList<ProjectDetails>();
    private List<CheckListItem> checkListItems = new ArrayList<CheckListItem>();
    
    /* modify existing project */
    public ProjectWizard(int projectId, String projectName, List<CustomField> fields, List<ProjectDetails> projectDetails){
        this.projectId = projectId;
        this.modify = true;
        this.originalProjectName = projectName;
        this.projectName = projectName;
        this.fields = fields;
        this.projectDetails = projectDetails;
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
        model.append(getReferencePage());
        model.append(getCompletionPage());
        setPageList(model);
        
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private AbstractWizardPage getNamePage(){
        
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Project Name"){          
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
        final DefaultWizardPage page = new DefaultWizardPage("Patients"){          
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
        
        formatModel = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int col) {  
                return row >= 5;   
            }  
        };
             
        formatModel.addColumn("Name");
        formatModel.addColumn("Type");
        formatModel.addColumn("Filterable");
        formatModel.addColumn("Alias");
        formatModel.addColumn("Description");
        
        formatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID, DefaultPatientTableSchema.TYPE_OF_FAMILY_ID + "(" + DefaultPatientTableSchema.LENGTH_OF_FAMILY_ID + ")", false, DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID, ""});
        formatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID, DefaultPatientTableSchema.TYPE_OF_PEDIGREE_ID + "(" + DefaultPatientTableSchema.LENGTH_OF_PEDIGREE_ID + ")", false, DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID, ""});
        formatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, DefaultPatientTableSchema.TYPE_OF_HOSPITAL_ID + "(" + DefaultPatientTableSchema.LENGTH_OF_HOSPITAL_ID + ")", false, DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, ""});
        formatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, DefaultPatientTableSchema.TYPE_OF_DNA_IDS + "(" + DefaultPatientTableSchema.LENGTH_OF_DNA_IDS + ")", false, DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, ""});
        formatModel.addRow(new Object[]{DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL, DefaultPatientTableSchema.TYPE_OF_BAM_URL + "(" + DefaultPatientTableSchema.LENGTH_OF_BAM_URL + ")", false, DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL, ""});

        if(modify){
            for(CustomField f : fields){
                formatModel.addRow(new Object[]{f.getColumnName(), f.getColumnType(), f.isFilterable(), f.getAlias(), f.getDescription()});
            }
        }

        table.setModel(formatModel);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        scrollpane.getViewport().add(table);
        page.addComponent(scrollpane);
        
        JButton addFieldButton = new JButton("Add Field");
        addFieldButton.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e){
                formatModel.addRow(new Object[5]);
                table.setModel(formatModel);
            }
        });
        page.addComponent(addFieldButton);
        
        JButton removeFieldButton = new JButton("Remove Field");
        removeFieldButton.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e){
                int row = table.getSelectedRow();
                if(row >= MedSavantDatabase.DefaultpatientTableSchema.getNumFields()-1){
                    formatModel.removeRow(row);
                }
                table.setModel(formatModel);
            }
        });
        page.addComponent(removeFieldButton);

        return page;
    }
    
    private AbstractWizardPage getReferencePage(){
        
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Reference"){          
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                //if(referenceName == null || referenceName.equals("")){                  
                //    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);                   
                //} else {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);              
                //}
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
    
    private AbstractWizardPage getCompletionPage(){
        CompletionWizardPage page = new CompletionWizardPage("Complete");
        String specific = "create";
        if(modify) specific = "make changes to";
        page.addText("Click finish to " + specific + " project. ");
        return page;
    }
    
    @Override
    public ButtonPanel createButtonPanel(){
        ButtonPanel bp = super.createButtonPanel();
        
        //remove finish button
        bp.removeButton((AbstractButton)bp.getButtonByName(ButtonNames.FINISH));
        
        //add new finish button
        JButton finishButton = new JButton("Finish");
        finishButton.setName(ButtonNames.FINISH);      
        finishButton.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e){
                finish();
            }
        });       
        bp.addButton(finishButton);
        
        return bp;
    }
    
    private void finish(){      
        try {
            if(validateProject()){
                createProject();
                this.setVisible(false);
                this.dispose();
                return;
            } 
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(
                this, 
                "<HTML>There was an error while trying to create your project:<BR>" + validationError + "</HTML>", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);  
    }
    
    private boolean validateProject() throws SQLException {
        if(ProjectQueryUtil.containsProject(projectName) && (!modify || !projectName.equals(originalProjectName))){
            validationError = "Project name already in use";
        } else if(!validateFormatModel()) {
            validationError = "Patient table format contains errors\n"
                    + "Name cannot only contain letters, numbers and underscores. \n"
                    + "Type must be in format: COLUMNTYPE(LENGTH)";
        } else if (!validateReferences()) {
            validationError = "You must select at least one reference genome. ";
        } else {
            return true;
        }
        return false;
    }
    
    private boolean validateReferences() {
        for(CheckListItem cli : checkListItems){
            if(cli.isSelected()){
                return true;
            }
        }
        return false;
    }
    
    private boolean validateFormatModel() {
        
        fields = new ArrayList<CustomField>();
        
        for(int row = 5; row < formatModel.getRowCount(); row++){
            String fieldName = (String)formatModel.getValueAt(row, 0);
            String fieldType = (String)formatModel.getValueAt(row, 1);
            Boolean fieldFilterable = (Boolean)formatModel.getValueAt(row, 2);
            String fieldAlias = (String)formatModel.getValueAt(row, 3);
            String fieldDescription = (String)formatModel.getValueAt(row, 4);
            
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
            
            //modify fields
            PatientQueryUtil.updateFields(projectId, fields);
            
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
                        ProjectQueryUtil.setAnnotations(projectId, cli.getReference().getId(), s);
                    }
                }
            }
            
            
            isModified = true;
        } else {
            
            //create project
            int projectid = ProjectController.getInstance().addProject(projectName, fields);
            
            //add references and annotations
            for(CheckListItem cli : checkListItems){
                if(cli.isSelected()){
                    ProjectQueryUtil.createVariantTable(projectid, cli.getReference().getId(),0);
                    
                    List<Integer> annotationIds = cli.getAnnotationIds();
                    if(!annotationIds.isEmpty()){
                        String s = "";
                        for(Integer i : annotationIds){
                            s += i + ",";
                        }
                        s = s.substring(0, s.length()-1);
                        ProjectQueryUtil.setAnnotations(projectid, cli.getReference().getId(), s);
                    }
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
