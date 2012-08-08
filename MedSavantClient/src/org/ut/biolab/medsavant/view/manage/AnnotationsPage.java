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
package org.ut.biolab.medsavant.view.manage;

import com.healthmarketscience.sqlbuilder.DeleteQuery;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import javax.swing.JPopupMenu;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.annotation.InstallAnnotationWizard;

import org.ut.biolab.medsavant.controller.ExternalAnnotationController;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Annotation;
import org.ut.biolab.medsavant.ontology.OntologyWizard;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
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
public class AnnotationsPage extends SubSectionView {

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
            try {

                Annotation an = (Annotation) items.get(0)[0];

                int response = DialogUtils.askYesNo("Confirm", "Are you sure you want to uninstall " + an.getProgram() + "?");

                if (response == DialogUtils.YES) {
                    MedSavantClient.AnnotationManagerAdapter.uninstallAnnotation(LoginController.sessionId, an);
                    DialogUtils.displayMessage("Annotation " + an.getProgram() + " uninstalled");
                }

            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error uninstalling annotations", ex);
            }

        }
    }
//implements ExternalAnnotationListener {

    public void referenceAdded(String name) {
        panel.refresh();
    }

    public void referenceRemoved(String name) {
        panel.refresh();
    }

    public void referenceChanged(String name) {
        panel.refresh();
    }
    private SplitScreenView panel;

    public AnnotationsPage(SectionView parent) {
        super(parent);
        //ExternalAnnotationController.getInstance().addExternalAnnotationListener(this);
    }

    @Override
    public String getName() {
        return "Annotations";
    }

    @Override
    public JPanel getView(boolean update) {
        panel = new SplitScreenView(
                new SimpleDetailedListModel<Annotation>("Program") {

                    @Override
                    public Annotation[] getData() throws Exception {
                        return ExternalAnnotationController.getInstance().getExternalAnnotations();
                    }
                },
                new ExternalAnnotationDetailedView(),
                new ExternalAnnotationDetailedListEditor());
        return panel;
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

        public ExternalAnnotationDetailedView() {

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
            infoPanel.add(content, BorderLayout.CENTER);


            details = ViewUtil.getClearPanel();

            content.add(details);
        }

        @Override
        public void setSelectedItem(Object[] item) {

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

        @Override
        public void setMultipleSelections(List<Object[]> selectedRows) {
            if (selectedRows.isEmpty()) {
                infoPanel.setTitle("");
            } else {
                infoPanel.setTitle("Multiple annotations (" + selectedRows.size() + ")");
            }
            details.removeAll();
            details.updateUI();
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

        }
    }
}
