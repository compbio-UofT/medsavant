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

import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.listener.ReferenceListener;
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
                getName(),
                new ReferenceGenomeListModel(),
                new ReferenceDetailedView());
        return panel;
    }

    @Override
    public Component[] getBanner() {
        return new Component[] { getAddReferenceButton() };
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
                refVector.add(new Object[] { p });
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
        public void setSelectedItem(Object[] item) {
            refName = (String)item[0];
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
            //throw new UnsupportedOperationException("Not supported yet.");
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
