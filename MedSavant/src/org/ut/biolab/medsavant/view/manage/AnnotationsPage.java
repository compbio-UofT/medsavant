/*
 *    Copyright 2011 University of Toronto
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.controller.ExternalAnnotationController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.model.Annotation;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.patients.DetailedListModel;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.patients.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AnnotationsPage extends SubSectionView {//implements ExternalAnnotationListener {

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
                getName(),
                new ExternalAnnotationListModel(),
                new ExternalAnnotationDetailedView());
        return panel;
    }

    @Override
    public Component[] getBanner() {
        Component[] result = new Component[1];
        result[0] = getAddExternalAnnotationButton();
        return result;
    }

    private JButton getAddExternalAnnotationButton() {
        JButton button = new JButton("Add Annotation");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainFrame.getInstance(), 
                        "Annotations can only be added using the \n"
                        + "MedSavant Database Utility.", 
                        "",JOptionPane.INFORMATION_MESSAGE);
                //NewReferenceDialog npd = new ADialog(MainFrame.getInstance(), true);
                //npd.setVisible(true);
            }
        });
        return button;
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
                annotationVector.add(new Object[] { p.getProgram(), p.getVersion(), p.getReferenceName() });
            }
            return annotationVector;
        }

        public List<String> getColumnNames() {
            if (cnames == null) {
                cnames = new ArrayList<String>();
                cnames.add("Program");
                cnames.add("Version");
                cnames.add("Reference");
            }
            return cnames;
        }

        public List<Class> getColumnClasses() {
            if (cclasses == null) {
                cclasses = new ArrayList<Class>();
                cclasses.add(String.class);
                cclasses.add(String.class);
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

        public ExternalAnnotationDetailedView() {

            content = this.getContentPanel();

            details = ViewUtil.getClearPanel();
            this.addBottomComponent(deleteButton());

            content.setLayout(new BorderLayout());

            content.add(details, BorderLayout.CENTER);
        }
        
        public JButton deleteButton() {
            JButton b = new JButton("Delete Annotation");
            b.setOpaque(false);
            b.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), 
                        "Annotations can only be deleted using the \n"
                        + "MedSavant Database Utility.", 
                        "",JOptionPane.INFORMATION_MESSAGE);
                }
            });
             
            return b;
        }

        @Override
        public void setSelectedItem(Object[] item) {
            
            String title = (String) item[0] + " (v" + item[1] + ")";
            setTitle(title);

            details.removeAll();
            details.setLayout(new BorderLayout());

            details.updateUI();
        }

        @Override
        public void setMultipleSelections(List<Object[]> selectedRows) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
