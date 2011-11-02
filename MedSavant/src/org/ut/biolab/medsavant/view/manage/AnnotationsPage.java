/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ExternalAnnotationController;
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

        public List<Vector> getList(int limit) throws Exception {
            List<Annotation> annotations = ExternalAnnotationController.getInstance().getExternalAnnotations();
            List<Vector> annotationVector = new ArrayList<Vector>();
            for (Annotation p : annotations) {
                Vector v = new Vector();
                v.add(p.getProgram());
                v.add(p.getVersion());
                v.add(p.getReference());
                annotationVector.add(v);
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
        public void setSelectedItem(Vector item) {
            
            String title = (String) item.get(0) + " (v" + item.get(1) + ")";
            setTitle(title);

            details.removeAll();
            details.setLayout(new BorderLayout());

            details.updateUI();
        }

        @Override
        public void setMultipleSelections(List<Vector> selectedRows) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
