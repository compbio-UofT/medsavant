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

package org.ut.biolab.medsavant.view.dialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.jidesoft.dialog.AbstractDialogPage;
import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;

import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.NonFatalDatabaseException;
import org.ut.biolab.medsavant.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.ExportVCF;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author Andrew
 */
public class ExportVcfWizard extends WizardDialog {
    
    private static int NUM_WARNING = 1000000;
    private File variantFile = null;
    private boolean running = false;
    private boolean cancelled = false;
    private Thread exportThread;
    
    public ExportVcfWizard() {
        setTitle("Export VCF Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        //add pages
        PageList model = new PageList();
        model.append(getWelcomePage());
        model.append(getFilePage());
        model.append(getExportPage());
        model.append(getCompletionPage());
        setPageList(model);
        
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private AbstractDialogPage getWelcomePage() {
        
        DefaultWizardPage page = new DefaultWizardPage("Begin") {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };
        page.addText(
                "This wizard will allow you to export all filtered variants to a \n"
                + "VCF file. ");
        try {
            if (ResultController.getInstance().getNumFilteredVariants() > NUM_WARNING) {
                JLabel l = new JLabel("WARNING:");
                l.setForeground(Color.red);
                l.setFont(new Font(l.getFont().getFamily(), Font.BOLD, l.getFont().getSize()));
                page.addComponent(l);
                page.addText(
                        "There are currenly more than " + ClientMiscUtils.numToString(NUM_WARNING) + " records to be exported. \n"
                        + "This may take a long time and produce a very large file!");
            }
        } catch (NonFatalDatabaseException ex) {
            Logger.getLogger(ExportVcfWizard.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return page;
    }
    
    private AbstractDialogPage getFilePage() {
        
        final DefaultWizardPage page = new DefaultWizardPage("Choose File") {

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                if (variantFile != null) {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        };

        page.addText("Choose the file to save to:");

        final JTextField outputFileField = new JTextField();
        outputFileField.setEnabled(false);
        JButton chooseFileButton = new JButton("...");
        chooseFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                variantFile = DialogUtils.chooseFileForSave("Export Variants", "export.vcf", new ExtensionFileFilter[] { new ExtensionFileFilter("vcf")}, null);//.chooseFilesForOpen("Import Variants", new ExtensionsFileFilter(new String[]{"vcf", "vcf.gz"}), null);
                if (variantFile == null) {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                    outputFileField.setText(variantFile.getAbsolutePath());
                }               
            }
            
        });
        JPanel container = new JPanel();
        ViewUtil.clear(container);
        ViewUtil.applyHorizontalBoxLayout(container);

        container.add(outputFileField);
        container.add(chooseFileButton);

        page.addComponent(container);
        page.addText("Files will be exported in Variant Call Format (*.vcf)");
        
        return page;
    }
    
    private AbstractDialogPage getExportPage() {
        
        final DefaultWizardPage page = new DefaultWizardPage("Export") {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };
               
        page.addText("Click \"Start\" to begin export. ");
        
        final JProgressBar progress = new JProgressBar();
        progress.setValue(0);
        page.addComponent(progress);
        
        final JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                
                if (running) {
                    running = false;
                    cancelled = true;
                    startButton.setEnabled(false);                                      
                    if (exportThread != null) {
                        exportThread.interrupt();
                    }               
                } else {
                    startButton.setText("Cancel");
                    progress.setIndeterminate(true);
                    exportThread = new Thread() {
                        @Override
                        public void run() {                        
                            try {
                                ExportVCF.exportVCF(variantFile);
                            } catch (Exception ex) {
                                Logger.getLogger(ExportVcfWizard.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            progress.setIndeterminate(false);
                            progress.setValue(100);
                            startButton.setEnabled(false);
                            page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                        }
                    };
                    exportThread.start();
                    
                }            
            }
        });
        page.addComponent(ViewUtil.alignRight(startButton));       
        
        return page;
    }
    
    private AbstractWizardPage getCompletionPage() {
        CompletionWizardPage page = new CompletionWizardPage("Complete") {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };
        
        if (cancelled) {
            page.addText("Export was cancelled by the user. ");
        } else {
            page.addText("Export completed successfully. ");
        }
        
        return page;
    }
    
}
