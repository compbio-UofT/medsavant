/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import com.jidesoft.utils.SwingWorker;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.view.genetics.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ProjectController.ProjectListener;
import org.ut.biolab.medsavant.db.Manage;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.model.record.Chromosome;
import org.ut.biolab.medsavant.model.record.Genome;
import org.ut.biolab.medsavant.util.view.PeekingPanel;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.dialog.SavantExportForm;
import org.ut.biolab.medsavant.view.patients.DetailedListModel;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.patients.SplitScreenView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ProjectManagementPage extends SubSectionView implements ProjectListener {

    public void projectAdded(String projectName) {
        if (panel != null) {
            try {
                int projectid = ProjectController.getInstance().getProjectId(projectName);

                NewVariantTableDialog d = new NewVariantTableDialog(projectid, MainFrame.getInstance(), true);
                d.setCancellable(false);
                d.setVisible(true);

            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(ProjectManagementPage.class.getName()).log(Level.SEVERE, null, ex);
            }

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

        private final JPanel menu;
        private final static JPanel details = ViewUtil.getClearPanel();
        private final JPanel content;
        private String projectName;
        private ProjectDetailsSW sw;

        public ProjectsDetailedView() {

            content = this.getContentPanel();

            menu = ViewUtil.getButtonPanel();

            menu.add(deleteProjectButton());
            menu.add(addTableButton());

            menu.setVisible(false);

            content.setLayout(new BorderLayout());

            content.add(details, BorderLayout.CENTER);
            content.add(menu, BorderLayout.SOUTH);

            ProjectController.getInstance().addProjectListener(this);

        }

        public final JButton addTableButton() {

            JButton b = new JButton("Add table for different reference");
            b.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    try {
                        int projectid = ProjectController.getInstance().getProjectId(projectName);

                        NewVariantTableDialog d = new NewVariantTableDialog(projectid, MainFrame.getInstance(), true);
                        d.setVisible(true);

                        refreshSelectedProject();

                    } catch (SQLException ex) {
                        Logger.getLogger(ProjectManagementPage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            return b;
        }

        public final JButton deleteProjectButton() {
            JButton b = new JButton("Delete Project");
            b.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {

                    int result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
                            "Are you sure you want to delete " + projectName + "?\nThis cannot be undone.",
                            "Confirm", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        ProjectController.getInstance().removeProject(projectName);
                    }
                }
            });
            return b;
        }

        @Override
        public void setSelectedItem(Vector item) {

            projectName = (String) item.get(0);
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

            if (menu != null) {
                menu.setVisible(true);




            }
        }

        private class ProjectDetailsSW extends SwingWorker {

            private String projectName;

            public ProjectDetailsSW(String projectName) {
                this.projectName = projectName;
            }
            Dimension buttonDim = new Dimension(100, 23);

            @Override
            protected Object doInBackground() throws Exception {
                final int projectId = ProjectController.getInstance().getProjectId(projectName);

                ResultSet rs = org.ut.biolab.medsavant.db.util.ConnectionController.connect().createStatement().executeQuery(
                        "SELECT * FROM " + org.ut.biolab.medsavant.db.util.DBSettings.TABLENAME_VARIANTTABLEINFO
                        + " LEFT JOIN " + org.ut.biolab.medsavant.db.util.DBSettings.TABLENAME_REFERENCE + " ON "
                        + org.ut.biolab.medsavant.db.util.DBSettings.TABLENAME_VARIANTTABLEINFO + ".reference_id = "
                        + org.ut.biolab.medsavant.db.util.DBSettings.TABLENAME_REFERENCE + ".reference_id "
                        + "WHERE project_id=" + projectId + ";");

                JPanel p = ViewUtil.getClearPanel();
                ViewUtil.applyVerticalBoxLayout(p);

                int numTables = 0;
                p.add(ViewUtil.getLeftAlignedComponent(ViewUtil.getDetailHeaderLabel("Variant Tables:")));

                //JComboBox defaultTableBox = new JComboBox();

                JPanel tablePanel = ViewUtil.getClearPanel();
                tablePanel.setLayout(new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.ipadx = 10;
                
                JButton removeTable = null;

                while (rs.next()) {
                    numTables++;

                    c.gridx = 0;

                    //defaultTableBox.addItem(rs.getString("name"));

                    //tablePanel.add(ViewUtil.getDetailLabel("  " + ));
                    
                    final int refId = rs.getInt("reference_id");
                    final String refName = rs.getString("name");
                    tablePanel.add(ViewUtil.getDetailLabel(refName),c);
                    c.gridx++;
                    //tablePanel.add(Box.createHorizontalGlue());

                    int numAnnotations = 0;
                    final String annotationIds = rs.getString("annotation_ids");
                    if (annotationIds != null) {
                        numAnnotations = annotationIds.length() - annotationIds.replaceAll(",", "").length() + 1;
                    }

                    tablePanel.add(ViewUtil.getDetailLabel(numAnnotations + " annotation(s) applied"), c);
                    c.gridx++;

                    JButton editTable = new JButton("Change");
                    editTable.addMouseListener(new MouseAdapter() {
                        public void mouseReleased(MouseEvent e) {
                            try {
                                new ChangeVariantDialog(MainFrame.getInstance(), true, projectId, refId, refName, annotationIds).setVisible(true);
                                refreshSelectedProject();
                            } catch (SQLException ex) {
                                Logger.getLogger(ProjectManagementPage.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    editTable.setPreferredSize(buttonDim);
                    editTable.setMaximumSize(buttonDim);

                    //final int project_id = rs.getInt("project_id");

                    tablePanel.add(editTable, c);
                    c.gridx++;

                    //tablePanel.add(Box.createHorizontalStrut(strutwidth));

                    removeTable = new JButton("Delete");
                    removeTable.setPreferredSize(buttonDim);
                    removeTable.setMaximumSize(buttonDim);


                    removeTable.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent ae) {
                            int result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
                                    "Are you sure you want to delete " + refName + "?\nThis cannot be undone.",
                                    "Confirm", JOptionPane.YES_NO_OPTION);
                            if (result == JOptionPane.YES_OPTION) {
                                ProjectController.getInstance().removeVariantTable(projectId, refId);
                            }
                        }
                    });

                    tablePanel.add(removeTable, c);

                    c.gridy++;
                }


                if (numTables == 0) {
                    p.add(ViewUtil.alignLeft(ViewUtil.getDetailLabel("No variant tables")));
                    
                } else {

                    if (numTables == 1 && removeTable != null) {
                        removeTable.setEnabled(false);
                    }
                    //JPanel defaultP = ViewUtil.getClearPanel();
                    //ViewUtil.applyHorizontalBoxLayout(defaultP);

                    //defaultP.add(ViewUtil.getDetailLabel("Default reference: "));
                    //defaultP.add(defaultTableBox);
                    //defaultP.add(Box.createHorizontalGlue()); 
                    //tablePanel.add(Box.createVerticalGlue());
                    //p.add(defaultP);
                    p.add(ViewUtil.alignLeft(tablePanel));
                }


                p.add(Box.createVerticalGlue());

                return p;
            }

            @Override
            protected void done() {
                try {
                    JPanel p = (JPanel) get();
                    updateDetails(p);
                } catch (java.util.concurrent.CancellationException ex1) {
                } catch (Exception ex) {
                    ex.printStackTrace();
                    updateDetails(ViewUtil.getMessagePanel("Problem getting project details"));
                }

            }
        }

        @Override
        public void setMultipleSelections(List<Vector> items) {
        }

        public static synchronized void updateDetails(JPanel p) {

            if (details == null) {
                return;
            }
            details.removeAll();

            details.setLayout(new BorderLayout());
            //.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        /*
            details.add(ViewUtil.getKeyValuePairPanel("Patients in cohort", patients.size() + ""), BorderLayout.NORTH);
            DefaultListModel lm = new DefaultListModel();
            for (Vector v : patients) {
            JLabel l = new JLabel(v.get(CohortViewTableSchema.INDEX_HOSPITALID-1).toString()); l.setForeground(Color.white);
            //details.add(l);
            lm.addElement((String) v.get(CohortViewTableSchema.INDEX_HOSPITALID-1));
            }
            list = (JList) ViewUtil.clear(new JList(lm));
            list.setBackground(ViewUtil.getDetailsBackgroundColor());
            list.setForeground(Color.white);
             * 
             */
            JScrollPane jsp = ViewUtil.getClearBorderlessJSP(p);
            details.add(jsp, BorderLayout.CENTER);
            //list.setOpaque(false);

            details.updateUI();





        }
    }

    private static class ProjectsListModel implements DetailedListModel {

        private ArrayList<String> cnames;
        private ArrayList<Class> cclasses;
        private ArrayList<Integer> chidden;

        public ProjectsListModel() {
        }

        public List<Vector> getList(int limit) throws Exception {
            List<String> projects = ProjectController.getInstance().getProjectNames();
            List<Vector> projectVector = new ArrayList<Vector>();
            for (String p : projects) {
                Vector v = new Vector();
                v.add(p);
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

    public JPanel getView() {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }

    public void setPanel() {
        panel = new SplitScreenView(
                new ProjectsListModel(),
                new ProjectsDetailedView());
    }

    @Override
    public Component[] getBanner() {
        Component[] result = new Component[1];
        result[0] = getAddPatientsButton();
        return result;
    }

    private JButton getAddPatientsButton() {
        JButton button = new JButton("New Project");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                NewProjectDialog npd = new NewProjectDialog(MainFrame.getInstance(), true);
                npd.setVisible(true);
            }
        });
        return button;
    }

    @Override
    public void viewLoading() {
    }

    @Override
    public void viewDidUnload() {
    }
}
