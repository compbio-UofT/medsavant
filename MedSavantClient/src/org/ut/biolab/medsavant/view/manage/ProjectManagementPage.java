/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.model.ProjectDetails;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
import org.ut.biolab.medsavant.view.list.DetailedListModel;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ProjectManagementPage extends SubSectionView implements ProjectListener {

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
            new ProjectWizard();
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
                        if (wiz.isModified()) {
                            ProjectController.getInstance().fireProjectRemovedEvent(projectName);
                            ProjectController.getInstance().fireProjectAddedEvent(MedSavantClient.ProjectQueryUtilAdapter.getProjectName(LoginController.sessionId, projectId));
                        }
                    } catch (SQLException ex) {
                        MiscUtils.checkSQLException(ex);
                        Logger.getLogger(ProjectManagementPage.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RemoteException ex) {
                        Logger.getLogger(ProjectManagementPage.class.getName()).log(Level.SEVERE, null, ex);
                    }
        }

        @Override
        public void deleteItems(List<Object[]> items) {
           int nameIndex = 0;
           int keyIndex = 0;

            int result;

            if (items.size() == 1) {
                String name = (String) items.get(0)[nameIndex];
                result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
                             "Are you sure you want to remove " + name + "?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
            } else {
                result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
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

    public void projectRemoved(String projectName) {
        if (panel != null) {
            panel.refresh();
        }
    }

    public void projectChanged(String projectName) {
        if (panel != null) {
            panel.refresh();
        }
    }

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

        public void projectAdded(String projectName) {
        }

        public void projectRemoved(String projectName) {
        }

        public void projectChanged(String projectName) {
        }

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
                int projectId = ProjectController.getInstance().getProjectId(projectName);
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
                    Logger.getLogger(ProjectManagementPage.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(ProjectManagementPage.class.getName()).log(Level.SEVERE, null, ex);
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


    private static class ProjectsListModel implements DetailedListModel {

        private ArrayList<String> cnames;
        private ArrayList<Class> cclasses;
        private ArrayList<Integer> chidden;

        public ProjectsListModel() {
        }

        public List<Object[]> getList(int limit) throws Exception {
            List<String> projects = ProjectController.getInstance().getProjectNames();
            List<Object[]> projectVector = new ArrayList<Object[]>();
            for (String p : projects) {
                Object[] v = new Object[]{ p };
                projectVector.add(v);
            }
            return projectVector;
        }

        public List<String> getColumnNames() {
            if (cnames == null) {
                cnames = new ArrayList<String>();
                cnames.add("Project");
            }
            return cnames;
        }

        public List<Class> getColumnClasses() {
            if (cclasses == null) {
                cclasses = new ArrayList<Class>();
                cclasses.add(String.class);
            }
            return cclasses;
        }

        public List<Integer> getHiddenColumns() {
            if (chidden == null) {
                chidden = new ArrayList<Integer>();
            }
            return chidden;
        }
    }
    private SplitScreenView panel;

    public ProjectManagementPage(SectionView parent) {
        super(parent);
        ProjectController.getInstance().addProjectListener(this);
    }

    public String getName() {
        return "Projects";
    }

    public JPanel getView(boolean update) {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }

    public void setPanel() {
        panel = new SplitScreenView(
                new ProjectsListModel(),
                new ProjectsDetailedView(),
                new ProjectDetailedListEditor());
    }

    @Override
    public Component[] getBanner() {
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
