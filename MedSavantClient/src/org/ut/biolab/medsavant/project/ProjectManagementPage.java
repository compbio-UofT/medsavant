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
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.ProjectDetails;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MedSavantWorker;
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
        super(parent, "Projects");
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
        public void editItem(Object[] items) {
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

                            } finally {
                                try {
                                    MedSavantClient.SettingsManager.releaseDBLock(LoginController.sessionId);
                                } catch (Exception ex1) {
                                    LOG.error("Error releasing database lock.", ex1);
                                }
                            }
                        } else {
                            DialogUtils.displayMessage("Cannot Modify Project", "The database is currently locked.\nTo unlock, see the Projects page in the Administration section.");
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

    private class ProjectsDetailedView extends DetailedView {

        private final JPanel content;
        private String projectName;
        private DetailsWorker detailsWorker;

        private JPanel details;
        private CollapsiblePane infoPanel;


        public ProjectsDetailedView() {
            super(pageName);

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

            if (detailsWorker != null) {
                detailsWorker.cancel(true);
            }
            detailsWorker = new DetailsWorker(projectName);
            detailsWorker.execute();
        }

        @Override
        public JPopupMenu createPopup() {
            return null;    //nothing yet
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
            try {
                if (MedSavantClient.SettingsManager.getSetting(LoginController.sessionId, "db lock").equals("true")) {
                    JPanel p = new JPanel();
                    ViewUtil.applyHorizontalBoxLayout(p);
                    p.add(ViewUtil.alignLeft(new JLabel("The database is locked. Administrators cannot make further changes.")));

                    JButton b = new JButton("Unlock");
                    b.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            try {
                                int result = DialogUtils.askYesNo("Warning", "Unlocking the database while another administrator is making changes can\n"
                                        + "cause permanent damage. Only unlock if you are sure no one is in the process of\n"
                                        + "making changes. Are you sure you want to proceed?");

                                if (result == DialogUtils.YES) {
                                    MedSavantClient.SettingsManager.releaseDBLock(LoginController.sessionId);
                                    refreshSelectedProject();
                                }
                            } catch (Exception ex) {
                            }
                        }
                    });
                    p.add(b);
                    details.add(Box.createVerticalStrut(10));
                    details.add(p);
                } else {
                    JPanel p = new JPanel();
                    ViewUtil.applyHorizontalBoxLayout(p);
                    p.add(ViewUtil.alignLeft(new JLabel("The database is unlocked. Administrators can make changes.")));

                    JButton b = new JButton("Lock");
                    b.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            try {
                                MedSavantClient.SettingsManager.getDBLock(LoginController.sessionId);
                                refreshSelectedProject();
                            } catch (Exception ex) {
                            }
                        }
                    });
                    p.add(b);
                    details.add(Box.createVerticalStrut(10));
                    details.add(p);
                }
            } catch (Exception ex) {
            }


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
