/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.app;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.util.DownloadEvent;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.builtin.task.BackgroundTaskWorker;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderTextField;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.view.notify.Notification;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.serverapi.SettingsManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.shared.util.ExtensionsFileFilter;
import org.ut.biolab.medsavant.shared.util.IOUtils;

/**
 *
 * @author mfiume, rammar
 */
public class VCFUploadApp implements LaunchableApp {

    private static final Log LOG = LogFactory.getLog(VCFUploadApp.class);
    private static VariantManagerAdapter variantManager = MedSavantClient.VariantManager;
    private static SettingsManagerAdapter settingsManager = MedSavantClient.SettingsManager;

    private ExecutorService executor;

    List<File> filesToImport;
    private JPanel fileListView;
    private static FileFilter fileFilter = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if (f.isFile()) {
                if (f.getAbsolutePath().endsWith(".vcf") || IOUtils.isArchive(f)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "VCF file(s)";
        }
    };

    private JPanel advancedOptionsPanel;
    private JButton importButton;
    private JXCollapsiblePane dragDropContainer;
    private CardLayout cardLayout = new CardLayout();
    private JCheckBox annovarCheckbox;
    private JCheckBox phasingCheckbox;
    private PlaceHolderTextField emailPlaceholder;
	private JCheckBox includeReferenceCheckbox;

    public VCFUploadApp() {
        filesToImport = new ArrayList<File>();
    }

    private JPanel view;
    private JPanel innerView = new JPanel(cardLayout);

    @Override
    public JPanel getView() {
        return view;
    }

    private void initView() {
        // Restart any running threads.
        if (executor != null) {
            executor.shutdownNow();
        }
        executor = Executors.newCachedThreadPool();

        if (view == null) {
            innerView.add(getSettingsPanel(), "upload");
            innerView.add(getLockedDBNotice(), "lock");
            JPanel fixedWidth = ViewUtil.getDefaultFixedWidthPanel(innerView);
            view = new StandardAppContainer(fixedWidth, true);
            view.setBackground(ViewUtil.getLightGrayBackgroundColor());
        }


        if (getDBLockState()) {
            cardLayout.show(innerView, "lock");
            refreshWhenLockIs(false);
            MedSavantFrame.getInstance().repaint(); // window won't update until resize otherwise
        } else {
            cardLayout.show(innerView, "upload");
            refreshWhenLockIs(true);
            MedSavantFrame.getInstance().repaint(); // window won't update until resize otherwise

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    refreshFileList();
                }
            });
        }

    }

    private void initSettingsPanel() {

        advancedOptionsPanel = ViewUtil.getWhiteLineBorderedPanel();
        advancedOptionsPanel.setLayout(new MigLayout("fillx"));
        advancedOptionsPanel.setVisible(false);
        //settingsPanel.setOpaque(false);

        JLabel l = new JLabel("Advanced Options");
        l.setFont(ViewUtil.getMediumTitleFont());
        advancedOptionsPanel.add(l, "wrap");

        advancedOptionsPanel.add(ViewUtil.getSettingsHeaderLabel("Annotation"), "wrap");
        advancedOptionsPanel.add(annovarCheckbox = new JCheckBox("perform gene-based variant annotation"), "wrap");
        advancedOptionsPanel.add(phasingCheckbox = new JCheckBox("perform phasing"), "wrap");
        annovarCheckbox.setSelected(true);
        annovarCheckbox.setFocusable(false);
        phasingCheckbox.setSelected(false);
        phasingCheckbox.setFocusable(false);

		includeReferenceCheckbox= new JCheckBox("include all VCF lines, including reference calls (highly recommended for pharmacogenetic testing)");
		includeReferenceCheckbox.setSelected(true);
		includeReferenceCheckbox.setFocusable(false);
		advancedOptionsPanel.add(includeReferenceCheckbox, "wrap");
		
        advancedOptionsPanel.add(ViewUtil.getSettingsHeaderLabel("Notifications"), "wrap");

        emailPlaceholder = new PlaceHolderTextField();
        emailPlaceholder.setPlaceholder("email address");
        advancedOptionsPanel.add(ViewUtil.getSettingsHelpLabel("Email notifications are sent upon completion"), "wrap");
        advancedOptionsPanel.add(emailPlaceholder, "wrap, growx 1.0");

    }

    private void addFilesToImport(File[] files) {
        for (File f : files) {
            addFileToImport(f);
        }
        refreshFileList();
    }

    private void addFileToImport(File f) {

        if (!fileFilter.accept(f)) {
            DialogUtils.displayMessage(String.format("File %s does not appear to be in the correct format", f.getName()));
            return;
        }

        if (filesToImport.contains(f)) {
            DialogUtils.displayMessage(String.format("File %s already listed for import", f.getName()));
            return;
        }

        filesToImport.add(f);
    }

    @Override
    public void viewWillUnload() {
        executor.shutdownNow();
    }

    @Override
    public void viewWillLoad() {
        initView();
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_IMPORTVCF);
    }

    @Override
    public String getName() {
        return "VCF Upload";
    }

    public static void main(String[] argv) {
        JFrame f = new JFrame();
        VCFUploadApp app = new VCFUploadApp();
        f.setPreferredSize(new Dimension(400, 400));
        f.setMinimumSize(new Dimension(400, 400));
        app.viewWillLoad();
        f.add(app.getView());
        f.pack();
        f.show();
        app.viewDidLoad();
    }

    private void refreshFileList() {
        fileListView.removeAll();
        MigLayout ml = new MigLayout("wrap 2");

        fileListView.setLayout(ml);

        JLabel l = new JLabel("Files to upload");
        l.setFont(ViewUtil.getMediumTitleFont());
        fileListView.add(l, "span 2, wrap");

        for (final File f : this.filesToImport) {

            JButton b = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CLOSE));
            fileListView.add(b);

            fileListView.add(new JLabel(f.getAbsolutePath()));

            b.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    filesToImport.remove(f);
                    refreshFileList();
                }

            });
        }

        if (this.filesToImport.isEmpty()) {
            fileListView.add(ViewUtil.getGrayItalicizedLabel("No files selected for upload"), "wrap");
        } else {
            JButton clearAll = ViewUtil.getSoftButton("Clear All");
            clearAll.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    clearFiles();
                }
                
            });
            fileListView.add(clearAll, "span 2, wrap");
        }

        fileListView.updateUI();

        importButton.setEnabled(!this.filesToImport.isEmpty());
        importButton.setSelected(!this.filesToImport.isEmpty());
        if (filesToImport.isEmpty()) {
            importButton.setText("Import");
        } else if (filesToImport.size() == 1) {
            importButton.setText("Import 1 file...");
        } else {
            importButton.setText(String.format("Import %s Files...", this.filesToImport.size()));
        }
    }

    private JPanel getSettingsPanel() {

        JPanel container = new JPanel();
        container.setBackground(ViewUtil.getLightGrayBackgroundColor());
        MigLayout layout = new MigLayout("insets 30 200 30 200, fillx, hidemode 3");
        container.setLayout(layout);

        dragDropContainer = new JXCollapsiblePane();

        dragDropContainer.getContentPane().setBackground(Color.white);
        MigLayout dpMl = new MigLayout("wrap 1, center");

        dragDropContainer.getContentPane().setLayout(dpMl);

        RoundedPanel dp = new RoundedPanel(0);
        dp.setLayout(new MigLayout("wrap 1, center"));
        dp.setBackground(Color.white);
        dp.setOpaque(false);

        dp.setBorderDashed(true);
        dp.setDashThickness(2);

        ImagePanel ip = new ImagePanel(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.IMPORT_VCF).getImage(), 300, 200);

        dp.add(ip);

        dp.setFocusable(false);
        int topBorder = 0;
        int sideBorder = 50;
        dp.setBorder(BorderFactory.createEmptyBorder(topBorder, sideBorder, topBorder, sideBorder));

        JLabel l = new JLabel("Drag and drop .vcf (or .vcf.gz) files to be uploaded");
        l.setForeground(new Color(100, 100, 100));
        dp.add(l);

        dragDropContainer.add(dp);

        dragDropContainer.add(l = new JLabel("or"), "center");
        l.setForeground(new Color(100, 100, 100));
        JButton chooseButton = new JButton("Choose...");
        chooseButton.setFocusable(false);
        dragDropContainer.add(chooseButton, "center");

        chooseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                File[] files = DialogUtils.chooseFilesForOpen("Choose Variant Files", new ExtensionsFileFilter(new String[]{"vcf", "vcf.gz", "vcf.bz2"}), null);
                addFilesToImport(files);
            }

        });

        JPanel wrapper = ViewUtil.getWhiteLineBorderedPanel();
        wrapper.setLayout(new MigLayout("fillx"));
        wrapper.add(dragDropContainer, "growx 1.0");

        //JLabel title = ViewUtil.getLargeGrayLabel("VCF Upload");
        //container.add(title,"wrap");
        container.add(wrapper, "wrap, width 100%");

        new FileDrop(dp, new FileDrop.Listener() {
            public void filesDropped(java.io.File[] files) {
                addFilesToImport(files);
            }   // end filesDropped
        }); // end FileDrop.Listener

        fileListView = ViewUtil.getWhiteLineBorderedPanel();
        container.add(fileListView, "wrap, width 100%");

        JToggleButton advancedOptionsButton = ViewUtil.getSoftToggleButton("Advanced Options");//ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CONFIGURE));
        advancedOptionsButton.setFocusable(false);
        //container.add(advancedOptionsButton, "wrap, center");

        initSettingsPanel();

        advancedOptionsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                advancedOptionsPanel.setVisible(!advancedOptionsPanel.isVisible());
            }

        });

        importButton = new JButton("Import");
        importButton.setEnabled(false);
        importButton.setFocusable(false);
        JPanel bContainer = ViewUtil.getClearPanel();
        bContainer.setLayout(new MigLayout("fillx, insets 0"));

        bContainer.add(advancedOptionsButton, "left");
        bContainer.add(importButton, "right");

        container.add(advancedOptionsPanel, "wrap, width 100%");
        container.add(bContainer, "wrap,center");

        final VCFUploadApp instance = this;

        importButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!ProjectController.getInstance().promptForUnpublished()) {
                        DialogUtils.displayError("Can't add new variants until changes are published.");
                        return;
                    }
                } catch (Exception ex) {
                    LOG.error("Error checking for unpublished changes", ex);
                    DialogUtils.displayError("Can't import VCF files.  Please contact your system administrator.");
                    return;
                }

                new BackgroundTaskWorker(instance, "Upload Variants") {

                    @Override
                    protected Void doInBackground() throws Exception {

                        final BackgroundTaskWorker instance = this;

                        this.addLog("Upload started");

                        final Notification notification = this.getNotificationForWorker();

                        notification.setShowsProgress(true);

                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                MedSavantFrame.getInstance().showNotification(notification);
                            }
                        });

                        File[] copyOfFilesToImport = new File[filesToImport.size()];
                        int counter = 0;
                        for (File f : filesToImport) {
                            copyOfFilesToImport[counter++] = f;
                        }
                        clearFiles();

                        final int[] transferIDs = new int[copyOfFilesToImport.length];

                        int fileIndex = 0;

                        int numFiles = copyOfFilesToImport.length;

                        for (File file : copyOfFilesToImport) {
                            LOG.info("Created input stream for file");
                            this.addLog(String.format("Uploading (%d/%d) %s...", fileIndex + 1, numFiles, file.getName()));
                            transferIDs[fileIndex++] = ClientNetworkUtils.copyFileToServer(file, new Listener<DownloadEvent>() {

                                @Override
                                public void handleEvent(DownloadEvent event) {
                                    switch (event.getType()) {
                                        case PROGRESS:
                                            instance.setTaskProgress(event.getProgress());
                                    }
                                }
                            }).get();

                        }
                        this.addLog("Done uploading variants");

                        this.addLog("Queuing background import job...");

                        this.addLog("Annotating with Jannovar: " + annovarCheckbox.isSelected());
                        this.addLog("Phasing: " + phasingCheckbox.isSelected());
                        this.addLog("Emailing notifications to: " + emailPlaceholder.getText());

                        Thread t = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    if (ProjectController.getInstance().promptForUnpublished()) {
                                        SwingUtilities.invokeLater(new Runnable() {

                                            @Override
                                            public void run() {
                                                AppDirectory.getTaskManager().showMessageForTask(instance,
                                                        "<html>Variants have been uploaded and are now being processed.<br/>"
                                                        + "You may view progress in the Server Log in the Task Manager<br/><br/>"
                                                        + "You may log out or continue doing work.</html>");
                                                notification.close();
                                            }

                                        });
                                        variantManager.uploadVariants(
                                                LoginController.getSessionID(),
                                                transferIDs,
                                                ProjectController.getInstance().getCurrentProjectID(),
                                                ReferenceController.getInstance().getCurrentReferenceID(),
                                                new String[][]{},
                                                includeReferenceCheckbox.isSelected(),
                                                emailPlaceholder.getText(),
                                                true,
                                                annovarCheckbox.isSelected(),
                                                phasingCheckbox.isSelected());
												
                                        succeeded();
                                    } 
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    LOG.error("Error: ", ex);
                                    instance.addLog("Error: " + ex.getMessage());
                                    instance.setStatus(TaskStatus.ERROR);
                                    AppDirectory.getTaskManager().showErrorForTask(instance, ex);
                                }
                            }

                            private void succeeded() {
                                LOG.info("Upload succeeded");

                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        LOG.info("Upload succeeded");
                                        AppDirectory.getTaskManager().showMessageForTask(instance,
                                                "<html>Variants have completed being imported.<br/>"
                                                + "As a result, you must login again.</html>");
                                        MedSavantFrame.getInstance().requestLogoutAndRestart();
                                    }

                                });

                            }

                        });

                        t.start();

                        this.addLog("Done");

                        this.setStatus(TaskStatus.FINISHED);

                        return null;
                    }

                    @Override
                    protected void showSuccess(Object result) {
                    }

                    @Override
                    protected void showFailure(Exception e) {
                    }

                }.start();
            }

        });

        return container;
    }

    /**
     * Return a panel with a notice about the current DB being locked.
     * @return a panel with a notice about the current DB being locked.
     */
    private JPanel getLockedDBNotice() {
        JPanel container = new WaitPanel(String.format("An update is currently in progress. "
                + "Further updates must wait until the current update is complete."));

        container.setBackground(ViewUtil.getLightGrayBackgroundColor());

        return container;
    }

    /**
     * Wait for the current DB lock to reach a certain state, then refresh the view.
     * @param lockState true if the lock is held, false if not.
     * @return A Future that will complete when the lockState is achieved
     */
    private Future refreshWhenLockIs(final boolean lockState) {
        return executor.submit(new Runnable() {
            @Override
            public void run() {
                boolean lock = !lockState;

                while (lock != lockState) {
                    lock = getDBLockState();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                // Refresh View
                initView();
            }
        });
    }

    /**
     * Return whether or not the current DB is locked for modifications.
     * @return The Lock state of the current DB.
     */
    private boolean getDBLockState() {
        boolean dbLocked = true;

        try {
            String sessionID = LoginController.getSessionID();
            int projectID = ProjectController.getInstance().getCurrentProjectID();
            dbLocked = settingsManager.isProjectLockedForChanges(sessionID, projectID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dbLocked;
    }

    /**
     * Remove all files
     */
    private void clearFiles() {
        filesToImport.removeAll(filesToImport);
        refreshFileList();
    }

    @Override
    public void didLogout() {
    }

    @Override
    public void didLogin() {
    }
}
