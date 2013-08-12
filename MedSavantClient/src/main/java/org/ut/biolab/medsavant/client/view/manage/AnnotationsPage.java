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
package org.ut.biolab.medsavant.client.view.manage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.annotation.InstallAnnotationWizard;

import org.ut.biolab.medsavant.client.controller.ExternalAnnotationController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import static org.ut.biolab.medsavant.client.view.util.DialogUtils.getFrontWindow;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AnnotationsPage extends SubSectionView {
    private static final Log LOG = LogFactory.getLog(AnnotationsPage.class);
    private SplitScreenView view;

    public AnnotationsPage(SectionView parent) {
        super(parent, "Annotations");
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = new SplitScreenView(
                    new SimpleDetailedListModel<Annotation>("Program") {
                @Override
                public Annotation[] getData() throws Exception {
                    return ExternalAnnotationController.getInstance().getExternalAnnotations();
                }
            },
                    new ExternalAnnotationDetailedView(),
                    new ExternalAnnotationDetailedListEditor());
        }
        return view;
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        Component[] result = new Component[0];
        //result[0] = getAddExternalAnnotationButton();
        return result;
    }

    private class ExternalAnnotationDetailedView extends DetailedView {

        private final JPanel details;
        private final JPanel content;
        private CollapsiblePane infoPanel;
        private final BlockingPanel blockPanel;

        public ExternalAnnotationDetailedView() {
            super(pageName);

            JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
            viewContainer.setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BorderLayout());

            JPanel infoContainer = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(infoContainer);

            contentPanel.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

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
            infoPanel.add(content, BorderLayout.CENTER);


            details = ViewUtil.getClearPanel();

            content.add(details);

            blockPanel = new BlockingPanel("No annotation selected", contentPanel);
            viewContainer.add(blockPanel, BorderLayout.CENTER);
        }

        @Override
        public void setSelectedItem(Object[] item) {

            if (item.length == 0) {
                blockPanel.block();
            } else {

                Annotation annotation = (Annotation) item[0];

                String title = annotation.toString();
                infoPanel.setTitle(title);

                details.removeAll();
                details.updateUI();

                List<String[]> infoList = new ArrayList<String[]>();

                infoList.add(new String[]{"Program", annotation.getProgram()});
                infoList.add(new String[]{"Version", annotation.getVersion()});
                infoList.add(new String[]{"Reference Genome", annotation.getReferenceName()});
                infoList.add(new String[]{"Type", annotation.getAnnotationType().toString()});

                setDetailsList(infoList);
            }
        }

        @Override
        public void setMultipleSelections(List<Object[]> selectedRows) {
            if (selectedRows.isEmpty()) {
                blockPanel.block();
            } else {

                if (selectedRows.isEmpty()) {
                    infoPanel.setTitle("");
                } else {
                    infoPanel.setTitle("Multiple annotations (" + selectedRows.size() + ")");
                }
                details.removeAll();
                details.updateUI();
            }
        }

        @Override
        public JPopupMenu createPopup() {
            return null;
        }

        private synchronized void setDetailsList(List<String[]> info) {

            details.removeAll();

            ViewUtil.setBoxYLayout(details);

            String[][] values = new String[info.size()][2];
            for (int i = 0; i < info.size(); i++) {
                values[i][0] = info.get(i)[0];
                values[i][1] = info.get(i)[1];
            }

            details.add(ViewUtil.getKeyValuePairList(values));

            details.updateUI();

            blockPanel.unblock();
        }
    }

    private static class ExternalAnnotationDetailedListEditor extends DetailedListEditor {

        @Override
        public boolean doesImplementAdding() {
            return true;
        }

        @Override
        public boolean doesImplementDeleting() {
            return true;
        }

        @Override
        public void addItems() {
            try {
                new InstallAnnotationWizard().setVisible(true);
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error installing annotations", ex);
            }
        }

        @Override
        public void deleteItems(List<Object[]> items) {

            if (items != null && items.size() > 0) {                
                final Annotation an = (Annotation) items.get(0)[0];

                ProgressDialog dialog = new ProgressDialog("Deleting annotation", "Checking for projects using this annotation...") {
                    private boolean inUse = false;

                    @Override
                    public void run() throws Exception {
                        String[] projectNames = ProjectController.getInstance().getProjectNames();
                        int referenceID = an.getReferenceID();
                        for (String projectName : projectNames) {
                            int projectID = ProjectController.getInstance().getProjectID(projectName);
                            int[] aid = ProjectController.getInstance().getAnnotationIDs(projectID, referenceID);
                            if (ArrayUtils.contains(aid, an.getID())) {                                
                                inUse = true;
                                break;
                            }
                        }                        
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dispose();
                                if (!inUse) {                                    
                                    //int response = DialogUtils.askYesNo("Confirm", "Are you sure you want to uninstall " + an.getProgram() + "?");
                                    int response = JOptionPane.showConfirmDialog(MedSavantFrame.getInstance().getRootPane(), "Are you sure you want to uninstall "+an.getProgram(), "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                    //end else.

                                    if (response == DialogUtils.YES) {
                                        try {
                                            MedSavantClient.AnnotationManagerAdapter.uninstallAnnotation(LoginController.getInstance().getSessionID(), an);
                                            DialogUtils.displayMessage("Annotation " + an.getProgram() + " uninstalled");
                                        } catch (Exception ex) {
                                            ClientMiscUtils.reportError("Error uninstalling annotations", ex);
                                        }
                                    }
                                }else{
                                    JOptionPane.showMessageDialog(MedSavantFrame.getInstance().getRootPane(), "This annotation is being used by other projects and cannot be uninstalled.  Please remove this annotation from these projects first.", "Notice", JOptionPane.PLAIN_MESSAGE);
                                }                                

                            }
                        });
                    }
                };
                try{
                    dialog.showDialog();
                }catch(Exception e){
                    LOG.error(e);
                }
                //dialog.setVisible(true);
                
            }


        }
    }

    public void referenceAdded(String name) {
        view.refresh();
    }

    public void referenceRemoved(String name) {
        view.refresh();
    }

    public void referenceChanged(String name) {
        view.refresh();
    }
}
