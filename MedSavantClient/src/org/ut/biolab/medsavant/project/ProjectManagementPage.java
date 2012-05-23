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
import java.util.concurrent.ExecutionException;
import java.util.List;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.model.ProjectDetails;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ProjectManagementPage extends SubSectionView implements ProjectListener {
    private static final Log LOG = LogFactory.getLog(ProjectManagementPage.class);

    private static class ProjectDetailedListEditor extends DetailedListEditor {

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

                String projectName = (String) items[0];

                int projectId = MedSavantClient.ProjectQueryUtilAdapter.getProjectId(LoginController.sessionId, projectName);
                ProjectWizard wiz = new ProjectWizard(
                        projectId,
                        projectName,
                        MedSavantClient.PatientQueryUtilAdapter.getCustomPatientFields(LoginController.sessionId, projectId),
                        MedSavantClient.ProjectQueryUtilAdapter.getProjectDetails(LoginController.sessionId, projectId));
                wiz.setVisible(true);
                if (wiz.isModified()) {
                    ProjectController.getInstance().fireProjectRemovedEvent(projectName);
                    ProjectController.getInstance().fireProjectAddedEvent(MedSavantClient.ProjectQueryUtilAdapter.getProjectName(LoginController.sessionId, projectId));
                }
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error fetching projects.", ex);
            }
        }

        @Override
        public void deleteItems(List<Object[]> items) {
           int nameIndex = 0;
           int keyIndex = 0;

            int result;

            if (items.size() == 1) {
                String name = (String) items.get(0)[nameIndex];
                result = JOptionPane.showConfirmDialog(MedSavantFrame.getInstance(),
                             "Are you sure you want to remove " + name + "?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
            } else {
                result = JOptionPane.showConfirmDialog(MedSavantFrame.getInstance(),
                             "Are you sure you want to remove these " + items.size() + " projects?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
            }

            if (result == JOptionPane.YES_OPTION) {
                for (Object[] v : items) {
                    String projectName = (String) v[keyIndex];
                    ProjectController.getInstance().removeProject(projectName);
                }

                DialogUtils.displayMessage("Successfully removed " + items.size() + " project(s)");
            }
        }
    }

    @Override
    public void projectAdded(String projectName) {
        if (panel != null) {
            /*try {
            int projectid = ProjectController.getInstance().getProjectName(projectName);

            NewVariantTableDialog d = new NewVariantTableDialog(projectid, MainFrame.getInstance(), true);
            d.setCancellable(false);
            d.setVisible(true);

            } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(ProjectManagementPage.class.getName()).log(Level.SEVERE, null, ex);
            }*/

            panel.refresh();
        }
    }

    @Override
    public void projectRemoved(String projectName) {
        if (panel != null) {
            panel.refresh();
        }
    }

    @Override
    public void projectChanged(String projectName) {
        if (panel != null) {
            panel.refresh();
        }
    }

    @Override
    public void projectTableRemoved(int projid, int refid) {
    }


    private static class ProjectsDetailedView extends DetailedView implements ProjectListener {

        private final JPanel content;
        private String projectName;
        private ProjectDetailsSW sw;

        private static JPanel details;
        private CollapsiblePanel tableInfoPanel;


        public ProjectsDetailedView() {

            JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
            viewContainer.setLayout(new BorderLayout());

            JPanel infoContainer = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(infoContainer);

            viewContainer.add(ViewUtil.getClearBorderlessJSP(infoContainer), BorderLayout.CENTER);

            tableInfoPanel = new CollapsiblePanel("Variant Tables");
            infoContainer.add(tableInfoPanel);
            infoContainer.add(Box.createVerticalGlue());

            content = tableInfoPanel.getContentPane();

            details = ViewUtil.getClearPanel();

            content.add(details);

            ProjectController.getInstance().addProjectListener(this);

        }

        @Override
        public void setSelectedItem(Object[] item) {
            projectName = (String) item[0];
            refreshSelectedProject();
        }

        @Override
        public void projectAdded(String projectName) {
        }

        @Override
        public void projectRemoved(String projectName) {
        }

        @Override
        public void projectChanged(String projectName) {
        }

        @Override
        public void projectTableRemoved(int projid, int refid) {
            refreshSelectedProject();
        }

        private void refreshSelectedProject() {
            setTitle(projectName);

            details.removeAll();
            details.updateUI();

            if (sw != null) {
                sw.cancel(true);
            }
            sw = new ProjectDetailsSW(projectName);
            sw.execute();
        }

        @Override
        public void setRightClick(MouseEvent e) {
            //nothing yet
        }

        private class ProjectDetailsSW extends MedSavantWorker {

            private String projectName;
            Dimension buttonDim = new Dimension(100, 23);

            public ProjectDetailsSW(String projectName) {
                super(getName());
                this.projectName = projectName;
            }

            @Override
            protected Object doInBackground() throws Exception {
                int projectId = ProjectController.getInstance().getProjectID(projectName);
                return MedSavantClient.ProjectQueryUtilAdapter.getProjectDetails(LoginController.sessionId, projectId);
            }

            @Override
            protected void showProgress(double fraction) {
                //
            }

            @Override
            protected void showSuccess(Object result) {
                try {
                    List<ProjectDetails> list = (List)get();
                    setDetailsList(list);
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    LOG.error("Error fetching project details.", ex);
                }
            }
        }

        public synchronized void setDetailsList(List<ProjectDetails> projectDetails) {

            details.removeAll();

            ViewUtil.setBoxYLayout(details);

            String[][] values = new String[projectDetails.size()][2];
            for(int i = 0; i < projectDetails.size(); i++){
                values[i][0] = projectDetails.get(i).getReferenceName();
                values[i][1] = projectDetails.get(i).getNumAnnotations() + " annotation(s) applied";
            }

            details.add(ViewUtil.getKeyValuePairList(values));

            details.updateUI();

        }

        @Override
        public void setMultipleSelections(List<Object[]> items) {
            if (items.isEmpty()) {
                setTitle("");
            } else {
            setTitle("Multiple projects (" + items.size() + ")");
            }
            details.removeAll();
            details.updateUI();
        }

    }


    private SplitScreenView panel;

    public ProjectManagementPage(SectionView parent) {
        super(parent);
        ProjectController.getInstance().addProjectListener(this);
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
                    new SimpleDetailedListModel("Projects") {
                        @Override
                        public List getData() throws Exception {
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
}
