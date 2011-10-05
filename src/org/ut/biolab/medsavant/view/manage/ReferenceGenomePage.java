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
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ReferenceController.ReferenceListener;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.manage.NewReferenceDialog;
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
public class ReferenceGenomePage extends SubSectionView implements ReferenceListener {

    public void referenceAdded(String name) {
        if (panel != null)
            panel.refresh();    
    }

    public void referenceRemoved(String name) {
        if (panel != null)
            panel.refresh();
    }

    public void referenceChanged(String name) {
        if (panel != null)
            panel.refresh();
    }
    private SplitScreenView panel;

    public ReferenceGenomePage(SectionView parent) {
        super(parent);
        ReferenceController.getInstance().addReferenceListener(this);
    }

    public String getName() {
        return "Reference Genomes";
    }

    public JPanel getView(boolean update) {
        panel = new SplitScreenView(
                new ReferenceGenomeListModel(),
                new ReferenceDetailedView());
        return panel;
    }

    @Override
    public Component[] getBanner() {
        Component[] result = new Component[1];
        result[0] = getAddPatientsButton();
        return result;
    }

    private JButton getAddPatientsButton() {
        JButton button = new JButton("New Reference");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                NewReferenceDialog npd = new NewReferenceDialog(MainFrame.getInstance(), true);
                npd.setVisible(true);
            }
        });
        return button;
    }

    private static class ReferenceGenomeListModel implements DetailedListModel {

        private ArrayList<String> cnames;
        private ArrayList<Class> cclasses;
        private ArrayList<Integer> chidden;

        public ReferenceGenomeListModel() {
        }

        public List<Vector> getList(int limit) throws Exception {
            List<String> refs = ReferenceController.getInstance().getReferenceNames();
            List<Vector> refVector = new ArrayList<Vector>();
            for (String p : refs) {
                Vector v = new Vector();
                v.add(p);
                refVector.add(v);
            }
            return refVector;
        }

        public List<String> getColumnNames() {
            if (cnames == null) {
                cnames = new ArrayList<String>();
                cnames.add("Reference");
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

    private class ReferenceDetailedView extends DetailedView {

        private final JPanel details;
        private final JPanel content;
        private String refName;

        public ReferenceDetailedView() {

            content = this.getContentPanel();

            details = ViewUtil.getClearPanel();

            //menu.add(setDefaultCaseButton());
            //menu.add(setDefaultControlButton());
            this.addBottomComponent(deleteButton());
            //menu.add(deleteCohortButton());
            

            content.setLayout(new BorderLayout());

            content.add(details, BorderLayout.CENTER);
        }

        @Override
        public void setSelectedItem(Vector item) {
            refName = (String) item.get(0);
            setTitle(refName);

            details.removeAll();
            details.setLayout(new BorderLayout());

            details.updateUI();
        }
        
        public final JButton deleteButton() {
            JButton b = new JButton("Delete Reference");
            b.setOpaque(false);
            b.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {

                    int result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
                            "Are you sure you want to delete " + refName + "?\nThis cannot be undone.",
                            "Confirm", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        boolean refRemoved = ReferenceController.getInstance().removeReference(refName);
                        if (!refRemoved) {
                            JOptionPane.showMessageDialog(MainFrame.getInstance(), "Cannot remove this reference because projects\nor annotations still refer to it.", "", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            return b;
        }

        @Override
        public void setMultipleSelections(List<Vector> selectedRows) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void viewLoading() {
    }

    @Override
    public void viewDidUnload() {
    }
}
