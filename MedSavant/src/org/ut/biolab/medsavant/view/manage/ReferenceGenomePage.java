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
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.dialog.NewReferenceDialog;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedListModel;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ReferenceGenomePage extends SubSectionView implements ReferenceListener {

    private static class ReferenceDetailedListEditer extends DetailedListEditor {

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
            NewReferenceDialog npd = new NewReferenceDialog(MainFrame.getInstance(), true);
            npd.setVisible(true);
        }

        @Override
        public void editItems(Object[] items) {
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
                             "Are you sure you want to remove these " + items.size() + " references?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
            }
            
            if (result == JOptionPane.YES_OPTION) {
                int numCouldntRemove = 0;
                for (Object[] v : items) {
                    String refName = (String) v[keyIndex];
                    ReferenceController.getInstance().removeReference(refName);
                }
                
                if (items.size() != numCouldntRemove) {
                    DialogUtils.displayMessage("Successfully removed " + (items.size()-numCouldntRemove) + " reference(s)");
                }
            }
        }
    }

    public void referenceAdded(String name) {
        if (panel != null) {
            panel.refresh();
        }
    }

    public void referenceRemoved(String name) {
        if (panel != null) {
            panel.refresh();
        }
    }

    public void referenceChanged(String name) {
        if (panel != null) {
            panel.refresh();
        }
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
                new ReferenceDetailedView(),
                new ReferenceDetailedListEditer());
        return panel;
    }

    @Override
    public Component[] getBanner() {
        Component[] result = new Component[0];
        //result[0] = getAddPatientsButton();
        return result;
    }

    private JButton getAddReferenceButton() {
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

        public List<Object[]> getList(int limit) throws Exception {
            List<String> refs = ReferenceController.getInstance().getReferenceNames();
            List<Object[]> refVector = new ArrayList<Object[]>();
            for (String p : refs) {
                Object[] v = new Object[] {p};
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
            //this.addBottomComponent(deleteButton());
            //menu.add(deleteCohortButton());


            content.setLayout(new BorderLayout());

            content.add(details, BorderLayout.CENTER);
        }

        @Override
        public void setSelectedItem(Object[] item) {
            refName = (String) item[0];
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
                        ReferenceController.getInstance().removeReference(refName);
                    }
                }
            });
            return b;
        }

        @Override
        public void setMultipleSelections(List<Object[]> selectedRows) {
            setTitle("Multiple references (" + selectedRows.size() + ")");
            details.removeAll();
            details.updateUI();
        }
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }
}
