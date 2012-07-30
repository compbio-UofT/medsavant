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

package org.ut.biolab.medsavant.project;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JPanel;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.model.ProjectDetails;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class ProjectManagementPage extends SubSectionView {
    private static final Log LOG = LogFactory.getLog(ProjectManagementPage.class);
    private ProjectController controller = ProjectController.getInstance();

    private SplitScreenView panel;

    public ProjectManagementPage(SectionView parent) {
        super(parent);
        controller.addListener(new Listener<ProjectEvent>() {
            @Override
            public void handleEvent(ProjectEvent event) {
                if (panel != null) {
                    panel.refresh();
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Projects";
    }

    @Override
    public JPanel getView(boolean update) {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }

    public void setPanel() {
        panel = new SplitScreenView(
                    new SimpleDetailedListModel<String>("Projects") {
                        @Override
                        public String[] getData() throws Exception {
                            return ProjectController.getInstance().getProjectNames();
                        }
                    },
                    new ProjectsDetailedView(),
                    new ProjectDetailedListEditor());
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        Component[] result = new Component[0];
        //result[0] = getAddPatientsButton();

        return result;
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
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
            new ProjectWizard().setVisible(true);
        }

        @Override
        public void editItems(Object[] items) {
            try {
                String projName = (String)items[0];
                int projID = MedSavantClient.ProjectManager.getProjectID(LoginController.sessionId, projName);

                // Check for existing unpublished changes to this project.
                if (ProjectController.getInstance().promptForUnpublished()) {
                    try {
                        // Get lock.
                        if (MedSavantClient.SettingsManager.getDBLock(LoginController.sessionId)) {
                            try {
                                ProjectWizard wiz = new ProjectWizard(projID, projName,
                                                                      MedSavantClient.PatientManager.getCustomPatientFields(LoginController.sessionId, projID),
                                                                      MedSavantClient.ProjectManager.getProjectDetails(LoginController.sessionId, projID));
                                wiz.setVisible(true);
                                if (wiz.isModified()) {
                                    // TODO: This looks suspicious to me.  Why aren't these being fired within a ProjectController method?
                                    controller.fireEvent(new ProjectEvent(ProjectEvent.Type.REMOVED, projName));
                                    controller.fireEvent(new ProjectEvent(ProjectEvent.Type.ADDED, MedSavantClient.ProjectManager.getProjectName(LoginController.sessionId, projID)));
                                }
                            } finally {
                                try {
                                    MedSavantClient.SettingsManager.releaseDBLock(LoginController.sessionId);
                                } catch (Exception ex1) {
                                    LOG.error("Error releasing database lock.", ex1);
                                }
                            }
                        } else {
                            DialogUtils.displayMessage("Cannot Modify Project", "Another user is making changes to the database. You must wait until this user has finished. ");
                        }
                    } catch (Exception ex) {
                        ClientMiscUtils.reportError("Error getting database lock: %s", ex);
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
                for (Object[] v : items) {
                    String projectName = (String) v[keyIndex];
                    controller.removeProject(projectName);
                }

                try {
                    if (controller.getProjectNames().length == 0) {
                        LoginController.getInstance().logout();
                    }
                    DialogUtils.displayMessage("Successfully removed " + items.size() + " project(s)");
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Unable to get updated project list: %s.", ex);
                }
            }
        }
    }
    private static class ProjectsDetailedView extends DetailedView {

        private final JPanel content;
        private String projectName;
        private DetailsWorker sw;

        private static JPanel details;
        private CollapsiblePane infoPanel;


        public ProjectsDetailedView() {

            JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
            viewContainer.setLayout(new BorderLayout());

            JPanel infoContainer = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(infoContainer);

            viewContainer.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

            CollapsiblePanes panes = new CollapsiblePanes();
        panes.setOpaque(false);
        infoContainer.add(panes);

	infoPanel = new CollapsiblePane();
        infoPanel.setStyle(CollapsiblePane.TREE_STYLE);
        infoPanel.setCollapsible(false);
        panes.add(infoPanel);
        panes.addExpansion();

        content = new JPanel();
        content.setLayout(new BorderLayout());
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(content,BorderLayout.CENTER);

            details = ViewUtil.getClearPanel();

            content.add(details);
        }

        @Override
        public void setSelectedItem(Object[] item) {
            projectName = (String) item[0];
            refreshSelectedProject();
        }

        private void refreshSelectedProject() {
            infoPanel.setTitle(projectName);

            details.removeAll();
            details.updateUI();

            if (sw != null) {
                sw.cancel(true);
            }
            sw = new DetailsWorker(projectName);
            sw.execute();
        }

        @Override
        public void setRightClick(MouseEvent e) {
            //nothing yet
        }

        private class DetailsWorker extends MedSavantWorker<ProjectDetails[]> {

            private String projectName;
            Dimension buttonDim = new Dimension(100, 23);

            public DetailsWorker(String projectName) {
                super(getName());
                this.projectName = projectName;
            }

            @Override
            protected ProjectDetails[] doInBackground() throws Exception {
                int projectId = ProjectController.getInstance().getProjectID(projectName);
                return MedSavantClient.ProjectManager.getProjectDetails(LoginController.sessionId, projectId);
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
            for(int i = 0; i < projectDetails.length; i++){
                values[i][0] = projectDetails[i].getReferenceName();
                values[i][1] = projectDetails[i].getNumAnnotations() + " annotation(s) applied";
            }

            details.add(ViewUtil.getKeyValuePairList(values));

            details.updateUI();

        }

        @Override
        public void setMultipleSelections(List<Object[]> items) {
            if (items.isEmpty()) {
                infoPanel.setTitle("");
            } else {
                infoPanel.setTitle("Multiple projects (" + items.size() + ")");
            }
            details.removeAll();
            details.updateUI();
        }

    }
}
