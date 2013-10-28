/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.variant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.RemoteException;
import java.sql.SQLException;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.controller.ResultController;
import org.ut.biolab.medsavant.shared.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.client.util.ExportVCF;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class ExportVCFWizard extends WizardDialog {

    private static final Log LOG = LogFactory.getLog(ExportVCFWizard.class);
    private static final int NUM_WARNING = 1000000;
    private File variantFile = null;
    private JLabel completionLabel;

    public ExportVCFWizard() throws InterruptedException, SQLException, RemoteException {
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
        setResizable(true);
        setLocationRelativeTo(null);
    }

    private AbstractDialogPage getWelcomePage() throws InterruptedException, SQLException, RemoteException {

        return new DefaultWizardPage("Begin") {
            {
                addComponent(new JLabel("<html>This wizard will allow you to export all filtered variants to a VCF file.</html>"));
                if (ResultController.getInstance().getFilteredVariantCount() > NUM_WARNING) {
                    addComponent(new JLabel(String.format("<html><font color=\"red\">WARNING:</font><br>There are currently more than %,d records to be exported.<br><br>This may take a long time and produce a very large file!</html>", NUM_WARNING)));
                }
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };
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
                variantFile = DialogUtils.chooseFileForSave("Export Variants", "export.vcf", ExtensionFileFilter.createFilters(new String[]{"vcf"}), null);
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

        return new DefaultWizardPage("Export") {
            private final JLabel progressLabel;
            private final JProgressBar progress;
            private final JButton workButton;

            {
                progressLabel = new JLabel("Click \"Start\" to begin export.");
                addComponent(progressLabel);

                progress = new JProgressBar();
                progress.setValue(0);
                addComponent(progress);

                workButton = new JButton("Start");
                workButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                        new MedSavantWorker<Void>("ExportVCF") {
                            @Override
                            protected Void doInBackground() throws Exception {
                                // Change text of progress label.
                                progressLabel.setText("Exporting to VCF...");
                                // Switch "Start" button to "Cancel".
                                workButton.removeActionListener(workButton.getActionListeners()[0]);
                                workButton.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent ae) {
                                        workButton.setEnabled(false);
                                        workButton.setText("Cancelling...");
                                        cancel(true);
                                    }
                                });
                                workButton.setText("Cancel");

                                ExportVCF.exportVCF(variantFile, this);
                                return null;
                            }

                            @Override
                            protected void showProgress(double fraction) {
                                progress.setValue((int) Math.round(fraction * 100.0));
                            }

                            @Override
                            protected void showSuccess(Void ignored) {
                                progressLabel.setText("Export successful.");
                                workButton.setEnabled(false);
                                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                            }

                            @Override
                            protected void showFailure(Throwable ex) {
                                progress.setValue(0);
                                workButton.setEnabled(false);
                                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                                completionLabel.setText("Export was cancelled by the user.");
                                System.out.println("Exception " + ex.getClass() + ": " + ex.getMessage());
                            }
                        }.execute();
                    }
                });
                addComponent(ViewUtil.alignRight(workButton));
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };
    }

    private AbstractWizardPage getCompletionPage() {
        return new CompletionWizardPage("Complete") {
            {
                completionLabel = new JLabel("Export completed successfully.");
                addComponent(completionLabel);
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };
    }
}
