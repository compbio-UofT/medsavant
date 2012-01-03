/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.dialog;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.importfile.BedFormat;
import org.ut.biolab.medsavant.importfile.FileFormat;
import org.ut.biolab.medsavant.importfile.ImportDelimitedFile;
import org.ut.biolab.medsavant.importfile.ImportFilePanel;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class IntervalWizard extends WizardDialog {
    
    private static final String PAGENAME_NAME = "List Name";
    private static final String PAGENAME_CHOOSE = "Choose File";
    private static final String PAGENAME_IMPORT = "Create and Import";
    private static final String PAGENAME_COMPLETE = "Complete";
    
    private String listName;
    private String path;
    private char delim;
    private FileFormat fileFormat;
    private int numHeaderLines;
    
    public IntervalWizard(){
        setTitle("Region Lists Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);
        
        //add pages
        PageList model = new PageList();
        model.append(getNamePage());
        model.append(getFilePage());
        model.append(getImportPage());
        model.append(getCompletionPage());
        setPageList(model);
        
        //change next action
        final WizardDialog instance = this;
        this.setNextAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String pagename = instance.getCurrentPage().getTitle();
                if(pagename.equals(PAGENAME_NAME) && validateListName()){
                    instance.setCurrentPage(PAGENAME_CHOOSE);
                } else if (pagename.equals(PAGENAME_CHOOSE)) {
                    instance.setCurrentPage(PAGENAME_IMPORT);
                } else if (pagename.equals(PAGENAME_IMPORT)) {
                    instance.setCurrentPage(PAGENAME_COMPLETE);
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
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                if(listName == null || listName.equals("")){
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        };
        page.addText(
                "Choose a name for the region list. \n"
                + "The name cannot already be in use. ");

        //setup text field
        final JTextField namefield = new JTextField();
        namefield.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                if(namefield.getText() != null && !namefield.getText().equals("")){
                    listName = namefield.getText();
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        });
        page.addComponent(namefield);

        return page;
    }
    
    private AbstractWizardPage getFilePage(){

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_CHOOSE){
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                if(path == null || path.equals("")){
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        };
        
        ImportFilePanel importPanel = new ImportFilePanel(){          
            @Override
            public void setReady(boolean ready){
                if(ready){
                    path = getPath();
                    delim = getDelimiter();
                    fileFormat = getFileFormat();
                    numHeaderLines = getNumHeaderLines();
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                }
            }          
        };
        importPanel.addFileFormat(new BedFormat());
        page.addComponent(importPanel);

        return page;
    }
    
    private AbstractWizardPage getImportPage(){

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage(PAGENAME_IMPORT){
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };
        
        page.addText("You are now ready to create this region list. ");

        final WizardDialog instance = this;

        final JLabel progressLabel = new JLabel("");
        final JProgressBar progressBar = new JProgressBar();

        page.addComponent(progressLabel);
        page.addComponent(progressBar);

        final JButton startButton = new JButton("Create List");
        startButton.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e){
                startButton.setEnabled(false);
                page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                progressBar.setIndeterminate(true);
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            createList();
                            ((CompletionWizardPage)instance.getPageByTitle(PAGENAME_COMPLETE)).addText(
                                    "List " + listName + " has been successfully created.");
                            instance.setCurrentPage(PAGENAME_COMPLETE);
                        } catch (Exception ex) {
                            DialogUtils.displayException("Error", "There was an error while trying to create your list. ", ex);
                            Logger.getLogger(IntervalWizard.class.getName()).log(Level.SEVERE, null, ex);
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
    
    private boolean validateListName(){
        try {
            boolean valid = !MedSavantClient.RegionQueryUtilAdapter.listNameExists(LoginController.sessionId, listName);
            if(!valid){
                JOptionPane.showMessageDialog(this, "List name already in use. ", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return valid;
        } catch (Exception ex) {
            Logger.getLogger(IntervalWizard.class.getName()).log(Level.SEVERE, null, ex);
            DialogUtils.displayException("Error", "Error trying to create interval list", ex);
            return false;
        }
    }
    
    private void createList() throws SQLException, NonFatalDatabaseException, IOException{
        Iterator<String[]> i = ImportDelimitedFile.getFileIterator(path, delim, numHeaderLines, fileFormat);
        MedSavantClient.RegionQueryUtilAdapter.addRegionList(LoginController.sessionId, listName, ReferenceController.getInstance().getCurrentReferenceId(), i);
    }
       
}
