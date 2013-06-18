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
package org.ut.biolab.medsavant.client.cohort;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.SimplePatient;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.component.StripyTable;
import org.ut.biolab.medsavant.client.view.genetics.QueryUtils;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.query.QueryViewController;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem.QueryRelation;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.value.encode.StringConditionEncoder;

/**
 *
 * @author mfiume
 */
class CohortDetailedView extends DetailedView {

    private static final Log LOG = LogFactory.getLog(CohortDetailedView.class);
    private Cohort[] cohorts;
    private CohortDetailsWorker worker;
    private final JPanel details;
    private JTable list;
    private final CollapsiblePane membersPane;
    private final BlockingPanel blockPanel;

    CohortDetailedView(String page) {
        super(page);

        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        content.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

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

        blockPanel = new BlockingPanel("No cohort selected", content);
        viewContainer.add(blockPanel, BorderLayout.CENTER);
    }

    private class CohortDetailsWorker extends MedSavantWorker<List<SimplePatient>> {

        private final Cohort cohort;

        CohortDetailsWorker(Cohort coh) {
            super(getPageName());
            cohort = coh;
        }

        @Override
        protected List<SimplePatient> doInBackground() throws Exception {
            List<SimplePatient> patientList = MedSavantClient.CohortManager.getIndividualsInCohort(
                    LoginController.getInstance().getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    cohort.getId());
            return patientList;
        }

        @Override
        protected void showProgress(double ignored) {
        }

        @Override
        protected void showSuccess(List<SimplePatient> result) {
            setPatientList(result);
            blockPanel.unblock();
        }
    }

    public JPopupMenu createHospitalPopup(final String[] hospitalIds) {
        JPopupMenu popupMenu = new JPopupMenu();

        //Filter by patient

        JMenuItem filter1Item = new JMenuItem(String.format("<html>Filter by %s</html>", hospitalIds.length == 1 ? "Hospital ID <i>" + hospitalIds[0] + "</i>" : "Selected Hospital IDs"));
        filter1Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cohort = null;
                if(cohorts.length == 1){
                    cohort = cohorts[0].getName();
                }                
                QueryUtils.addQueryOnHospitals(hospitalIds, cohort);                
                MedSavantFrame.getInstance().searchAnimationFromMousePos();
            }
        });
        popupMenu.add(filter1Item);

        return popupMenu;
    }

    public synchronized void setPatientList(List<SimplePatient> patients) {

        details.removeAll();

        final Object[][] data = new Object[patients.size()][1];

        for (int i = 0; i < patients.size(); i++) {
            data[i][0] = patients.get(i);
        }

        JPanel p = ViewUtil.getClearPanel();
        //p.setBackground(Color.WHITE);
        p.setBorder(ViewUtil.getBigBorder());
        ViewUtil.applyVerticalBoxLayout(p);

        list = new StripyTable(data, new String[]{"Member Hospital IDs"});
        list.setBorder(null);
        list.setShowGrid(false);
        list.setRowHeight(21);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {

                    int[] selection = list.getSelectedRows();

                    String[] hosIds = new String[selection.length];

                    for (int i = 0; i < selection.length; i++) {
                        int j = list.convertRowIndexToModel(selection[i]);
                        hosIds[i] = ((SimplePatient) data[j][0]).getHospitalId();
                    }

                    createHospitalPopup(hosIds).show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });


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

        if (item.length == 0) {
            blockPanel.block();
        } else {

            cohorts = new Cohort[]{(Cohort) item[0]};
            membersPane.setTitle(cohorts[0].getName());

            details.removeAll();
            details.updateUI();

            if (worker != null) {
                worker.cancel(true);
            }
            worker = new CohortDetailsWorker(cohorts[0]);
            worker.execute();
        }
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        if (items.isEmpty()) {
            blockPanel.block();
        } else {
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
    }

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
                int[] patientIDs = new int[rows.length];
                for (int i = 0; i < rows.length; i++) {
                    patientIDs[i] = ((SimplePatient) list.getModel().getValueAt(rows[i], 0)).getId();
                }
                if (patientIDs != null && patientIDs.length > 0) {

                    if (DialogUtils.askYesNo("Confirm", "Are you sure you want to remove these individual(s)?") == DialogUtils.NO) {
                        return;
                    }

                    try {
                        MedSavantClient.CohortManager.removePatientsFromCohort(LoginController.getInstance().getSessionID(), patientIDs, cohorts[0].getId());
                    } catch (Exception ex) {
                        LOG.error("Error removing patients from cohort.", ex);
                    }

                    worker = new CohortDetailsWorker(cohorts[0]);
                    worker.execute();
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
            JMenuItem filter1Item = new JMenuItem(String.format("<html>Filter by %s</html>", cohorts.length == 1 ? "Cohort <i>" + cohorts[0] + "</i>" : "Selected Cohorts"));
            filter1Item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
                    SearchConditionGroupItem p = qvc.getQueryRootGroup();

                    List<SearchConditionItem> sciList = new ArrayList<SearchConditionItem>(cohorts.length);

                       
                    List<String> cohortStrings = new ArrayList(cohorts.length);
                    for (Cohort cohort : cohorts) {
                        cohortStrings.add(cohort.getName());
                        /*
                        SearchConditionItem cohortItem = new SearchConditionItem("Cohort", SearchConditionGroupItem.QueryRelation.AND, p);
                        cohortItem.setDescription(cohort.toString());
                        cohortItem.setSearchConditionEncoding(StringConditionEncoder.encodeConditions(Arrays.asList(new String[]{cohort.toString()})));
                        sciList.add(cohortItem);
                        * */
                    }
                                        
                    String description = StringConditionEncoder.getDescription(cohortStrings);
                    String encodedConditions = StringConditionEncoder.encodeConditions(cohortStrings);
                    
                    qvc.replaceFirstLevelItem("Cohort", encodedConditions, description);
/*
                    if (cohorts.length < 2) {
                        
                        SearchConditionItem sci = sciList.get(0);
                        qvc.generateItemViewAndAddToGroup(sci, p);
                    } else {
                        qvc.replaceFirstLevelGroup("Cohorts", sciList, QueryRelation.AND, true);
                    }*/
                    qvc.refreshView();
                    MedSavantFrame.getInstance().searchAnimationFromMousePos();

                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }
}
