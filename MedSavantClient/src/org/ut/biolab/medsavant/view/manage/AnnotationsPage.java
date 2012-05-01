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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.controller.ExternalAnnotationController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.model.Annotation;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
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
            JOptionPane.showMessageDialog(MedSavantFrame.getInstance(),
                        "Annotations can only be added using the \n"
                        + "MedSavant Database Utility.",
                        "",JOptionPane.INFORMATION_MESSAGE);
        }

        @Override
        public void editItems(Object[] items) {
        }

        @Override
        public void deleteItems(List<Object[]> items) {
            JOptionPane.showMessageDialog(MedSavantFrame.getInstance(),
                        "Annotations can only be deleted using the \n"
                        + "MedSavant Database Utility.",
                        "",JOptionPane.INFORMATION_MESSAGE);
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
                new SimpleDetailedListModel("Program") {
                    @Override
                    public List getData() throws Exception {
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

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }


    private class ExternalAnnotationDetailedView extends DetailedView {

        private final JPanel details;
        private final JPanel content;
        private CollapsiblePanel infoPanel;

        public ExternalAnnotationDetailedView() {

            JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
            viewContainer.setLayout(new BorderLayout());

            JPanel infoContainer = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(infoContainer);

            viewContainer.add(ViewUtil.getClearBorderlessJSP(infoContainer), BorderLayout.CENTER);

            infoPanel = new CollapsiblePanel("Annotation Information");
            infoContainer.add(infoPanel);
            infoContainer.add(Box.createVerticalGlue());

            content = infoPanel.getContentPane();

            details = ViewUtil.getClearPanel();

            content.add(details);
        }

        @Override
        public void setSelectedItem(Object[] item) {

            Annotation annotation = (Annotation) item[0];

            String title = annotation.toString();
            setTitle(title);

            details.removeAll();
            details.updateUI();

            List<String[]> infoList = new ArrayList<String[]>();

            infoList.add(new String[]{"Program", annotation.getProgram()});
            infoList.add(new String[]{"Version", annotation.getVersion()});
            infoList.add(new String[]{"Reference Genome", annotation.getReferenceName()});
            infoList.add(new String[]{"Type", AnnotationFormat.annotationTypToString(annotation.getAnnotationType())});

            setDetailsList(infoList);
        }

        @Override
        public void setMultipleSelections(List<Object[]> selectedRows) {
            if (selectedRows.isEmpty()) {
                setTitle("");
            } else {
                setTitle("Multiple annotations (" + selectedRows.size() + ")");
            }
            details.removeAll();
            details.updateUI();
        }

        @Override
        public void setRightClick(MouseEvent e) {
            //nothing yet
        }

        private synchronized void setDetailsList(List<String[]> info) {

            details.removeAll();

            ViewUtil.setBoxYLayout(details);

            String[][] values = new String[info.size()][2];
            for(int i = 0; i < info.size(); i++){
                values[i][0] = info.get(i)[0];
                values[i][1] = info.get(i)[1];
            }

            details.add(ViewUtil.getKeyValuePairList(values));

            details.updateUI();

        }

    }

}
