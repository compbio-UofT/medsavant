/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.project;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.app.MultiSectionApp;
import org.ut.biolab.medsavant.client.view.app.AppSubSection;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.StandardFixableWidthAppPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ProjectManagementPage extends AppSubSection {

    private static final Log LOG = LogFactory.getLog(ProjectManagementPage.class);

    private ProjectController controller = ProjectController.getInstance();
    private SplitScreenView view;

    public ProjectManagementPage(MultiSectionApp parent) {
        super(parent, "Projects");
        controller.addListener(new Listener<ProjectEvent>() {
            @Override
            public void handleEvent(ProjectEvent event) {
                if (view != null) {
                    view.refresh();
                }
            }
        });
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = new SplitScreenView(
                    new SimpleDetailedListModel<String>("Projects") {
                        @Override
                        public String[] getData() throws Exception {
                            return ProjectController.getInstance().getProjectNames();
                        }
                    },
                    new ProjectsDetailedView(),
                    new ProjectDetailedListEditor());
        }
        return view;
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        Component[] result = new Component[0];
        return result;
    }

    private class ProjectDetailedListEditor extends DetailedListEditor {

        @Override
        public boolean doesImplementAdding() {
            return true;
        }

        @Override
        public boolean doesImplementDeleting() {
            return true;
        }

        @Override
        public boolean doesImplementEditing() {
            return true;
        }

        @Override
        public void addItems() {
            try {
                new ProjectWizard().setVisible(true);
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Unable to launch project wizard: %s", ex);
            }
        }

        @Override
        public void editItem(Object[] items) {
            try {
                String projName = (String) items[0];
                String sessionID = LoginController.getSessionID();
                int projID = MedSavantClient.ProjectManager.getProjectID(sessionID, projName);

                // Check for existing unpublished changes to this project.
                if (ProjectController.getInstance().promptForUnpublished()) {
                    if (!MedSavantClient.SettingsManager.isProjectLockedForChanges(sessionID, ProjectController.getInstance().getCurrentProjectID())) {
                        ProjectWizard wiz = new ProjectWizard(projID, projName,
                                MedSavantClient.PatientManager.getCustomPatientFields(sessionID, projID),
                                MedSavantClient.ProjectManager.getProjectDetails(sessionID, projID));
                        wiz.setVisible(true);
                    } else {
                        DialogUtils.displayMessage("Cannot Modify Project", "This project is currently locked for changes.\nTo unlock, see the Projects page in the Administration section.");
                    }
                }
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error checking for changes: %s", ex);
            }
        }

        @Override
        public void deleteItems(List<Object[]> items) {
            int nameIndex = 0;
            int keyIndex = 0;

            int result;

            if (items.size() == 1) {
                String name = (String) items.get(0)[nameIndex];
                result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This cannot be undone.</html>", name);
            } else {
                result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove these %d projects?<br>This cannot be undone.</html>", items.size());
            }

            if (result == DialogUtils.YES) {

                boolean didRequestToRemoveCurrentProject = false;

                for (Object[] v : items) {
                    String projectName = (String) v[keyIndex];
                    if (projectName.equals(ProjectController.getInstance().getCurrentProjectName())) {
                        didRequestToRemoveCurrentProject = true;
                    }
                    controller.removeProject(projectName);
                }
                try {
                    if (didRequestToRemoveCurrentProject) {
                        DialogUtils.displayMessage("Successfully removed " + items.size() + " project(s).\n\n"
                                + "The current project was removed.\nYou'll now be logged out.");
                        MedSavantFrame.getInstance().forceRestart();
                    } else {
                        DialogUtils.displayMessage("Successfully removed " + items.size() + " project(s)");
                    }
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Unable to get updated project list: %s.", ex);
                }
            }
        }
    }

    private class ProjectsDetailedView extends DetailedView {

        private String projectName;
        private DetailsWorker detailsWorker;
        private JPanel details;
        private final BlockingPanel blockingPanel;
        private final StandardFixableWidthAppPanel canvas;

        public ProjectsDetailedView() {
            super(pageName);
            canvas = new StandardFixableWidthAppPanel();
            blockingPanel = new BlockingPanel("No project selected",canvas);
            details = canvas.addBlock("Basic Information");
            blockingPanel.block();
            this.setLayout(new BorderLayout());
            this.add(blockingPanel,BorderLayout.CENTER);            
        }

        @Override
        public void setSelectedItem(Object[] item) {

            if (item.length == 0) {
                blockingPanel.block();
                return;
            }

            projectName = (String) item[0];
            refreshSelectedProject();
        }

        private void refreshSelectedProject() {
            canvas.setTitle(projectName);

            details.removeAll();
            details.updateUI();

            if (detailsWorker != null) {
                detailsWorker.cancel(true);
            }
            detailsWorker = new DetailsWorker(projectName);
            detailsWorker.execute();
        }

        private class DetailsWorker extends MedSavantWorker<ProjectDetails[]> {

            private String projectName;
            Dimension buttonDim = new Dimension(100, 23);

            public DetailsWorker(String projectName) {
                super(pageName);
                this.projectName = projectName;
            }

            @Override
            protected ProjectDetails[] doInBackground() throws Exception {
                int projectId = ProjectController.getInstance().getProjectID(projectName);
                return MedSavantClient.ProjectManager.getProjectDetails(LoginController.getSessionID(), projectId);
            }

            @Override
            protected void showProgress(double fraction) {
                //
            }

            @Override
            protected void showSuccess(ProjectDetails[] result) {
                setDetailsList(result);
            }
        }

        private synchronized void setDetailsList(ProjectDetails[] projectDetails) {

            details.removeAll();

            ViewUtil.setBoxYLayout(details);

            String[][] values = new String[projectDetails.length][2];
            for (int i = 0; i < projectDetails.length; i++) {
                values[i][0] = projectDetails[i].getReferenceName();
                values[i][1] = projectDetails[i].getNumAnnotations() + " annotation(s) applied";
            }

            details.add(ViewUtil.getKeyValuePairList(values));

            int projectID = projectDetails[0].getProjectID();
            if (projectID == ProjectController.getInstance().getCurrentProjectID()) {
                JPanel p = ViewUtil.getClearPanel();
                ViewUtil.applyHorizontalBoxLayout(p);
                p.add(ViewUtil.getGrayItalicizedLabel("This is the current project."));
                p.add(Box.createHorizontalGlue());
                details.add(Box.createVerticalStrut(10));
                details.add(p);
            }

            String sessionID = LoginController.getSessionID();

            try {
                if (MedSavantClient.SettingsManager.isProjectLockedForChanges(sessionID, projectID)) {
                    JPanel p = ViewUtil.getClearPanel();
                    ViewUtil.applyHorizontalBoxLayout(p);

                    p.add(ViewUtil.getGrayItalicizedLabel("This project is locked. Administrators cannot make further changes."));
                    JButton b = new JButton("Unlock");
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            try {
                                int result = DialogUtils.askYesNo("Warning", "Unlocking the database while another administrator is making changes can\n"
                                        + "cause permanent damage. Only unlock if you are sure no one is in the process of\n"
                                        + "making changes. Are you sure you want to proceed?");

                                if (result == DialogUtils.YES) {
                                    MedSavantClient.SettingsManager.forceReleaseLockForProject(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID());
                                    refreshSelectedProject();
                                }
                            } catch (Exception ex) {
                                LOG.error(ex);
                            }
                        }
                    });

                    JButton refreshButton = ViewUtil.getRefreshButton();

                    p.add(b);
                    p.add(Box.createHorizontalGlue());
                    details.add(Box.createVerticalStrut(10));
                    details.add(p);
                } else {
                    JPanel p = ViewUtil.getClearPanel();
                    ViewUtil.applyHorizontalBoxLayout(p);
                    p.add(ViewUtil.alignLeft(ViewUtil.getGrayItalicizedLabel("This project is unlocked. Administrators can make changes.")));
                    details.add(Box.createVerticalStrut(10));
                    details.add(p);
                }
            } catch (Exception ex) {
            }

            details.updateUI();
            blockingPanel.unblock();
        }

        @Override
        public void setMultipleSelections(List<Object[]> items) {
            if (items.isEmpty()) {
                canvas.setTitle("");
            } else {
                canvas.setTitle("Multiple projects (" + items.size() + ")");
            }
            details.removeAll();
            details.updateUI();
            blockingPanel.unblock();
        }
    }
}
