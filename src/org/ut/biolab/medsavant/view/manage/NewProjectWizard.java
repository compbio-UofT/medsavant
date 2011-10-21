/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.PageList;
import com.jidesoft.swing.JideButton;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WelcomeWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.model.Annotation;
import org.ut.biolab.medsavant.db.util.query.AnnotationQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil;
import org.ut.biolab.medsavant.util.ExtensionFileFilter;

/**
 *
 * @author Andrew
 */
public class NewProjectWizard extends WizardDialog {
    
    private String projectName;
    private File formatFile;
    private String referenceName;
    private List<Annotation> chosenAnnotations = new ArrayList<Annotation>();
    private String validationError = "";
    
    public NewProjectWizard(){

        setTitle("New Project Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);
        
        
        //add pages
        PageList model = new PageList();
        model.append(getWelcomePage());
        model.append(getNamePage());
        model.append(getPatientFieldsPage());
        model.append(getReferencePage());
        model.append(getAnnotationsPage());
        model.append(getCompletionPage());
        setPageList(model);
        
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        
    }
    
    private AbstractWizardPage getWelcomePage(){
        WelcomeWizardPage page = new WelcomeWizardPage("New Project Wizard");
        page.addText(
                "Use this wizard to create a new project. Projects contain a set\n"
                + "of individuals, and a set of variants for each reference genome. ");
        return page;  
    }
    
    private AbstractWizardPage getNamePage(){
        
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Project Name"){          
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                if(projectName == null || projectName.equals("")){                  
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);                   
                } else {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);              
                }
            }     
        };
        page.addText(
                "Choose a name for the new project. \n"
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
        
        return page;
    }
    
    private AbstractWizardPage getPatientFieldsPage(){
        
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Patients"){          
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);   
            }     
        };
        page.addText(
                "Choose an xml file which specifies the fields used for patient\n"
                + "data. See documentation for proper xml schema. If no file is\n"
                + "specified, only the default fields will be used");
        
        //setup file chooser
        final JTextField filefield = new JTextField();
        filefield.setEnabled(false);
        
        JButton button = new JButton(" ... ");
        button.addMouseListener(new MouseAdapter() {     
            @Override
            public void mouseReleased(MouseEvent e){
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Format File");
                fc.setDialogType(JFileChooser.OPEN_DIALOG);
                fc.addChoosableFileFilter(new ExtensionFileFilter("xml"));

                int result = fc.showDialog(null, null);
                if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
                    return;
                }

                formatFile = fc.getSelectedFile();
                if(formatFile != null){
                    filefield.setText(formatFile.getAbsolutePath());
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);   
                } else {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);   
                }
            }    
        });
        
        Component spacer = Box.createHorizontalStrut(10);
        spacer.setBackground(Color.white);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(filefield);
        panel.add(spacer);
        panel.add(button);
        
        page.addComponent(panel);
        
        return page;
    }
    
    private AbstractWizardPage getReferencePage(){
        
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Reference"){          
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                if(referenceName == null || referenceName.equals("")){                  
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);                   
                } else {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);              
                }
            }     
        };
        page.addText(
                "Choose a reference genome to use with this project. You can add\n"
                + "more references later. ");
        
        //setup combo
        JComboBox combo = new JComboBox();
        combo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                referenceName = (String)e.getItem();
            }
        });
        try {
            List<String> refNames = ReferenceQueryUtil.getReferenceNames();
            for(String name : refNames){
                combo.addItem(name);
            }
        } catch (SQLException ex) {
            Logger.getLogger(NewProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
        }       
        page.addComponent(combo);
        
        return page;
    }
    
    private AbstractWizardPage getAnnotationsPage(){
        
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Annotations"){          
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                if(referenceName == null || referenceName.equals("")){                  
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);                   
                } else {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);              
                }
            }     
        };
        page.addText(
                "Choose annotations for the default reference (" + referenceName + "). \n"
                + "These annotations will be applied to any variants added to\n"
                + "this table. More annotations can be applied later. ");
        
        //setup list
        JList list = new JList();
        List<Annotation> annotations = new ArrayList<Annotation>();
        try {           
            annotations = AnnotationQueryUtil.getAnnotations();
        } catch (SQLException ex) {
            Logger.getLogger(NewProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
        }
        DefaultListModel model = new DefaultListModel();
        for(Annotation a : annotations){
            if(a.getReference().equals(referenceName)){
                model.addElement(new CheckListItem(a, false));
            }
        }
        list.setModel(model);
        list.setCellRenderer(new CheckListRenderer());
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event){
                JList list = (JList) event.getSource();
                int index = list.locationToIndex(event.getPoint());
                CheckListItem item = (CheckListItem) list.getModel().getElementAt(index);
                item.setSelected(! item.isSelected());
                if(item.isSelected){
                    chosenAnnotations.add(item.getAnnotation());
                } else {
                    chosenAnnotations.remove(item.getAnnotation());
                }
                list.repaint();
            }
        });
        if(annotations.isEmpty()){
            page.addText("\nNo annotations available. ");
        } else {
            page.addComponent(list);
        }
        
        return page;        
    }
    
    private AbstractWizardPage getCompletionPage(){
        CompletionWizardPage page = new CompletionWizardPage("Complete");
        page.addText("Click finish to create project. ");
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
        } catch (SQLException ex) {}
        JOptionPane.showMessageDialog(
                this, 
                "<HTML>There was an error while trying to create your project:<BR>" + validationError + "</HTML>", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);  
    }
    
    private boolean validateProject() throws SQLException {
        if(ProjectQueryUtil.containsProject(projectName)){
            validationError = "Project name already in use";
        } else {
            return true;
        }
        return false;
    }
    
    private void createProject() throws SQLException {
        //create project
        int projectid = ProjectController.getInstance().addProject(projectName, formatFile);
        
        //add reference
        int refid = ReferenceQueryUtil.getReferenceId(referenceName);
        ProjectQueryUtil.createVariantTable(projectid,refid);
        
        //add annotations
        if(!chosenAnnotations.isEmpty()){
            String annotationIds = "";
            for(Annotation a : chosenAnnotations){
                annotationIds += a.getId() + ",";
            }
            annotationIds = annotationIds.substring(0, annotationIds.length()-1);
            ProjectQueryUtil.setAnnotations(projectid, refid, annotationIds);
        }       
    }
    
    
    class CheckListItem{

        private String  label;
        private boolean isSelected = false;
        private Annotation a;

        public CheckListItem(Annotation a, boolean selected){
            this.label = a.getProgram() + " " + a.getVersion();
            this.isSelected = selected;
            this.a = a;
        }

        public boolean isSelected(){
            return isSelected;
        }

        public void setSelected(boolean isSelected){
            this.isSelected = isSelected;
        }

        public Annotation getAnnotation(){
            return a;
        }

        public String toString(){
            return label;
        }
    }

    class CheckListRenderer extends JCheckBox implements ListCellRenderer {

        public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean hasFocus){

            setEnabled(list.isEnabled());
            setSelected(((CheckListItem)value).isSelected());
            setFont(list.getFont());
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(value.toString());
            return this;
        }
    }
    
}
