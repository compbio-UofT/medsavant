/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.db.model.Annotation;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedListModel;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AnnotationsPage extends SubSectionView {

    private static class ExternalAnnotationDetailedListEditer extends DetailedListEditor {

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

    public String getName() {
        return "Annotations";
    }

    public JPanel getView(boolean update) {
        panel = new SplitScreenView(
                new ExternalAnnotationListModel(),
                new ExternalAnnotationDetailedView(),
                new ExternalAnnotationDetailedListEditer());
        return panel;
    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        Component[] result = new Component[0];
        //result[0] = getAddExternalAnnotationButton();
        return result;
    }

    private static class ExternalAnnotationListModel implements DetailedListModel {

        private ArrayList<String> cnames;
        private ArrayList<Class> cclasses;
        private ArrayList<Integer> chidden;

        public ExternalAnnotationListModel() {
        }

        public List<Object[]> getList(int limit) throws Exception {
            List<Annotation> annotations = ExternalAnnotationController.getInstance().getExternalAnnotations();
            List<Object[]> annotationVector = new ArrayList<Object[]>();
            for (Annotation p : annotations) {
                Object[] v = new Object[] {p};
                annotationVector.add(v);
            }
            return annotationVector;
        }

        public List<String> getColumnNames() {
            if (cnames == null) {
                cnames = new ArrayList<String>();
                cnames.add("Program");
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
