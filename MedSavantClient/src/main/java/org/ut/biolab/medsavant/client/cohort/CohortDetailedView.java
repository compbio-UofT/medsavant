/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.client.view.login.LoginController;
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
import org.ut.biolab.medsavant.client.query.QueryViewController;
import org.ut.biolab.medsavant.client.query.SearchConditionGroupItem;
import org.ut.biolab.medsavant.client.query.SearchConditionGroupItem.QueryRelation;
import org.ut.biolab.medsavant.client.query.SearchConditionItem;
import org.ut.biolab.medsavant.client.query.value.encode.StringConditionEncoder;
import org.ut.biolab.medsavant.client.view.font.FontFactory;
import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import org.ut.biolab.medsavant.client.view.util.StandardFixableWidthAppPanel;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 *
 * @author mfiume
 */
class CohortDetailedView extends DetailedView {

    private static final Log LOG = LogFactory.getLog(CohortDetailedView.class);
    private Cohort[] cohorts;
    private CohortDetailsWorker worker;
    private JTable list;
    private final BlockingPanel blockPanel;
    private final JPanel members;
    private final StandardFixableWidthAppPanel canvas;

    CohortDetailedView(String page) {
        super(page);

        canvas = new StandardFixableWidthAppPanel("Cohort");
        members = canvas.addBlock("Members");

        blockPanel = new BlockingPanel("No cohort selected", canvas);

        this.setLayout(new BorderLayout());
        this.add(blockPanel, BorderLayout.CENTER);
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
                    LoginController.getSessionID(),
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
                if (cohorts.length == 1) {
                    cohort = cohorts[0].getName();
                }
                QueryUtils.addQueryOnHospitals(hospitalIds, cohort);

                DialogUtils.displayMessage("Selected Cohort and Hospital IDS have been added to query.  Click 'Variants' to review and execute search.");
            }
        });
        popupMenu.add(filter1Item);

        return popupMenu;
    }

    public synchronized void setPatientList(List<SimplePatient> patients) {

        members.removeAll();

        final Object[][] data = new Object[patients.size()][1];

        for (int i = 0; i < patients.size(); i++) {
            data[i][0] = patients.get(i);
        }

        list = new StripyTable(data, new String[]{"Member Hospital IDs"});
        list.setFont(FontFactory.getGeneralFont().deriveFont(16));
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

        final JButton removeButton = removeIndividualsButton();
        removeButton.setEnabled(false);
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int count = list.getSelectedRowCount();
                removeButton.setEnabled(count > 0);
                if (count == 0) {
                    removeButton.setText("Remove selected member");
                } else {
                    if (count == 1) {
                        removeButton.setText("Remove selected member");
                    } else {
                        removeButton.setText(String.format("Remove %d selected members", count));
                    }
                }
            }

        });

        members.setLayout(new MigLayout("fillx, insets 0, wrap"));
        members.add(new JLabel(ViewUtil.numToString(list.getRowCount()) + " " + MiscUtils.pluralize(list.getRowCount(), "member", "members")), "split, growx 1.0");
        members.add(removeButton, "right,wrap");
        members.add(list, "width 100%");

        members.updateUI();
    }

    @Override
    public void setSelectedItem(Object[] item) {

        if (item.length == 0) {
            blockPanel.block();
        } else {

            cohorts = new Cohort[]{(Cohort) item[0]};
            canvas.setTitle(cohorts[0].getName());

            members.removeAll();
            members.updateUI();

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
                canvas.setTitle("");
            } else {
                canvas.setTitle("Multiple cohorts (" + items.size() + ")");
            }
            members.removeAll();
            members.updateUI();
        }
    }

    private JButton removeIndividualsButton() {

        JButton button = ViewUtil.getSoftButton("Remove selected member");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.setOpaque(false);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
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
                        MedSavantClient.CohortManager.removePatientsFromCohort(LoginController.getSessionID(), patientIDs, cohorts[0].getId());
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
                    DialogUtils.displayMessage("Selected Cohorts have been added to query.  Click 'Variants' to review and execute search.");

                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }
}
