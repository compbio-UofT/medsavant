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
package org.ut.biolab.medsavant.view.patients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import com.jidesoft.utils.SwingWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Cohort;
import org.ut.biolab.medsavant.model.SimplePatient;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.view.component.StripyTable;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class CohortDetailedView extends DetailedView {

    private static final Log LOG = LogFactory.getLog(CohortDetailedView.class);
    private Cohort cohort;
    private Cohort[] cohorts;
    private boolean multipleSelected = false;
    private CohortDetailsWorker sw;
    private final JPanel details;
    private JTable list;
    private final CollapsiblePane membersPane;

    public CohortDetailedView() {

        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        viewContainer.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

        CollapsiblePanes panes = new CollapsiblePanes();
        panes.setOpaque(false);
        infoContainer.add(panes);

        membersPane = new CollapsiblePane();
        membersPane.setStyle(CollapsiblePane.TREE_STYLE);
        membersPane.setCollapsible(false);
        panes.add(membersPane);


        details = new JPanel();
        details.setLayout(new BorderLayout());
        membersPane.setLayout(new BorderLayout());
        membersPane.add(details, BorderLayout.CENTER);

        panes.addExpansion();

        //addBottomComponent(menu);
    }

    private class CohortDetailsWorker extends SwingWorker {

        private final Cohort cohort;

        public CohortDetailsWorker(Cohort cohort) {
            this.cohort = cohort;
        }

        @Override
        protected Object doInBackground() throws Exception {
            List<SimplePatient> patientList = MedSavantClient.CohortManager.getIndividualsInCohort(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectID(),
                    cohort.getId());
            return patientList;
        }

        @Override
        protected void done() {
            try {
                List<SimplePatient> result = (List<SimplePatient>) get();
                setPatientList(result);

            } catch (Exception ex) {
                return;
            }
        }
    }

    public synchronized void setPatientList(List<SimplePatient> patients) {

        details.removeAll();

        Object[][] data = new Object[patients.size()][1];
        for (int i = 0; i < patients.size(); i++) {
            data[i][0] = patients.get(i);
        }

        JPanel p = new JPanel();
        p.setBackground(Color.white);
        p.setBorder(ViewUtil.getBigBorder());
        ViewUtil.applyVerticalBoxLayout(p);

        list = new StripyTable(data, new String[]{"Member Hospital IDs"});
        list.setBorder(null);
        list.setShowGrid(false);
        list.setRowHeight(21);

        p.add(ViewUtil.alignLeft(new JLabel(ViewUtil.numToString(list.getRowCount()) + " members")));
        p.add(Box.createRigidArea(new Dimension(10, 10)));
        p.add(ViewUtil.getClearBorderedScrollPane(list));

        JPanel menu = ViewUtil.getClearPanel();
        menu.add(removeIndividualsButton());
        p.add(menu);

        details.add(p, BorderLayout.CENTER);

        details.updateUI();
    }

    @Override
    public void setSelectedItem(Object[] item) {
        multipleSelected = false;
        cohort = ((Cohort) item[0]);
        membersPane.setTitle(cohort.getName());

        details.removeAll();
        details.updateUI();

        if (sw != null) {
            sw.cancel(true);
        }
        sw = new CohortDetailsWorker(cohort);
        sw.execute();
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        multipleSelected = true;
        cohorts = new Cohort[items.size()];
        for (int i = 0; i < items.size(); i++) {
            cohorts[i] = (Cohort) items.get(i)[0];
        }
        if (items.isEmpty()) {
            membersPane.setTitle("");
        } else {
            membersPane.setTitle("Multiple cohorts (" + items.size() + ")");
        }
        details.removeAll();
        details.updateUI();
    }

    /*
     * private JButton setDefaultCaseButton() { JButton button = new
     * JButton("Set default Case cohort");
     * button.setBackground(ViewUtil.getDetailsBackgroundColor());
     * button.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { //TODO } }); return button; }
     *
     * private JButton setDefaultControlButton() { JButton button = new
     * JButton("Set default Control cohort");
     * button.setBackground(ViewUtil.getDetailsBackgroundColor());
     * button.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { //TODO } }); return button; }
     *
     */
    private JLabel removeIndividualsButton() {

        JLabel button = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE_ON_TOOLBAR));
        button.setToolTipText("Remove selected");
        //JButton button = new JButton("Remove individual(s) from cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.setOpaque(false);
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int[] rows = list.getSelectedRows();
                int[] patientIds = new int[rows.length];
                for (int i = 0; i < rows.length; i++) {
                    patientIds[i] = ((SimplePatient) list.getModel().getValueAt(rows[i], 0)).getId();
                }
                if (patientIds != null && patientIds.length > 0) {

                    if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(null, "Are you sure you want to remove these individual(s)?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        return;
                    }

                    try {
                        MedSavantClient.CohortManager.removePatientsFromCohort(LoginController.sessionId, patientIds, cohort.getId());
                    } catch (Exception ex) {
                        LOG.error("Error removing patients from cohort.", ex);
                    }

                    sw = new CohortDetailsWorker(cohort);
                    sw.execute();
                }
            }
        });

        return button;
    }

    @Override
    public JPopupMenu createPopup() {
        JPopupMenu popupMenu = new JPopupMenu();

        if (ProjectController.getInstance().getCurrentVariantTableSchema() == null) {
            popupMenu.add(new JLabel("(You must choose a variant table before filtering)"));
        } else {

            //Filter by patient
            JMenuItem filter1Item = new JMenuItem("Filter by Cohort(s)");
            filter1Item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
/*                    List<
                    for ()
                }
    public static FilterState wrapState(WhichTable t, String colName, String alias, List<String> applied) {
                    GeneticsFilterPage.getSearchBar().loadFilters(CohortFilterView.wrapState(WhichTable.COHORT, "name", "Cohort Name", applied));*/
                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }
}
