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
package org.ut.biolab.medsavant.client.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.CacheController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.DataRetriever;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import static org.ut.biolab.medsavant.shared.format.BasicPatientColumns.INDEX_OF_HOSPITAL_ID;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.ModificationType;
import org.ut.biolab.medsavant.shared.model.SimplePatient;

/**
 *
 * @author mfiume, rammar
 */
public class IndividualSelector extends JDialog implements BasicPatientColumns {

    Set<String> selectedHospitalIDs;
    private static final Log LOG = LogFactory.getLog(IndividualSelector.class);
    private JPanel middlePanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private boolean forceRefresh = false;
    private static final String[] COLUMN_NAMES = new String[]{
        PATIENT_ID.getAlias(),
        FAMILY_ID.getAlias(),
        HOSPITAL_ID.getAlias(),
        IDBIOMOM.getAlias(),
        IDBIODAD.getAlias(),
        GENDER.getAlias(),
        AFFECTED.getAlias(),
        DNA_IDS.getAlias(),
        PHENOTYPES.getAlias(),
        "Cohort(s)"};
    private static final Class[] COLUMN_CLASSES = new Class[]{
        Integer.class, // patient
        String.class, // family
        String.class, // hospital
        String.class, // mom id
        String.class, // dad id
        String.class, // gender
        String.class, // affected
        String.class, // dna ids
        String.class, // phenotypes
        String.class // cohorts
    };
    private static final int[] HIDDEN_COLUMNS = new int[]{
        0, // patient
        3, // mom
        4, // dad
        7 // dna
    };
    private SearchableTablePanel individualsSTP;
    private JLabel numselections;
    private IndividualsReceiver individualsRetriever;
    private HashSet<Integer> selectedRows;
    private JButton ok;
    private boolean hasMadeSelections;
    private boolean onlyOnePatient;

    /**
     * Creates an IndividualSelector patient chooser with the option of
     * selecting either a single patient or multiple patients.
     *
     * @param onlyOnePatient True if only selecting an individual, false
     * otherwise.
     */
    public IndividualSelector(boolean onlyOnePatient) {
        super(MedSavantFrame.getInstance(), true);
        this.onlyOnePatient = onlyOnePatient;
        setTitle(onlyOnePatient ? "Select Individual" : "Select Individual(s)");
        this.setPreferredSize(new Dimension(700, 600));
        this.setMinimumSize(new Dimension(700, 600));
        selectedHospitalIDs = new HashSet<String>();
        selectedRows = new HashSet<Integer>();
        initUI();
        refresh();
    }

    public IndividualSelector() {
        this(false);
    }

    public Set<String> getHospitalIDsOfSelectedIndividuals() {
        return selectedHospitalIDs;
    }

    public Set<String> getDNAIDsOfSelectedIndividuals() {
        Set<String> ids = new HashSet<String>();
        for (int i : selectedRows) {
            String dnaID = (String) individualsRetriever.getIndividuals().get(i)[INDEX_OF_DNA_IDS];
            ids.add(dnaID);
        }
        return ids;
    }

    public void refresh() {
        individualsSTP.forceRefreshData();
    }

    private void initUI() {

        JPanel p = new JPanel();
        ViewUtil.applyVerticalBoxLayout(p);
        this.add(p);

        topPanel = ViewUtil.getClearPanel();
        middlePanel = ViewUtil.getClearPanel();
        bottomPanel = ViewUtil.getClearPanel();

        p.add(topPanel);
        p.add(middlePanel);
        p.add(bottomPanel);

        // Only display the bottom panel for multiple patient selection
        if (onlyOnePatient) {
            bottomPanel.setVisible(false);
        }

        // middle
        middlePanel.setLayout(new BorderLayout());

        individualsRetriever = new IndividualsReceiver();
        individualsSTP = new SearchableTablePanel("Individuals", COLUMN_NAMES, COLUMN_CLASSES, HIDDEN_COLUMNS,
                true, true, Integer.MAX_VALUE, false, SearchableTablePanel.TableSelectionType.ROW, Integer.MAX_VALUE, individualsRetriever);
        individualsSTP.setExportButtonVisible(false);

        //If patients or cohorts are edited, update the searchabletable.
        CacheController.getInstance().addListener(new Listener<ModificationType>() {
            @Override
            public void handleEvent(ModificationType event) {
                if (event == ModificationType.PATIENT || event == ModificationType.COHORT) {
                    if (!individualsSTP.isUpdating()) {
                        forceRefresh = true;
                        individualsSTP.forceRefreshData();
                    }
                }
            }

        });

        middlePanel.add(individualsSTP, BorderLayout.CENTER);

        // bottom
        ViewUtil.applyVerticalBoxLayout(bottomPanel);

        numselections = ViewUtil.getTitleLabel("0 individual(s) selected");

        JPanel text = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(text);

        JButton clearIndividuals = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CLOSE));
        clearIndividuals.setToolTipText("Clear selections");
        clearIndividuals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                clearSelections();
            }
        });

        text.add(numselections);
        text.add(Box.createHorizontalStrut(5));
        text.add(clearIndividuals);
        bottomPanel.add(ViewUtil.centerHorizontally(text));

        final JButton addAllIndividuals = ViewUtil.getSoftButton("Add All");
        bottomPanel.add(addAllIndividuals);

        final JButton addIndividuals = ViewUtil.getSoftButton("Add Selected");
        bottomPanel.add(addIndividuals);

        final JButton removeIndividuals = ViewUtil.getSoftButton("Remove Selected");
        bottomPanel.add(removeIndividuals);

        final JButton removeAllIndividuals = ViewUtil.getSoftButton("Remove All");
        bottomPanel.add(removeAllIndividuals);

        JPanel buttons = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(buttons);

        buttons.add(addAllIndividuals);
        buttons.add(addIndividuals);
        buttons.add(removeIndividuals);
        buttons.add(removeAllIndividuals);

        JPanel windowControlPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(windowControlPanel);
        windowControlPanel.add(Box.createHorizontalGlue());

        JButton cancel = new JButton("Cancel");
        bottomPanel.add(ViewUtil.centerHorizontally(buttons));

        ok = new JButton("OK");
        bottomPanel.add(ViewUtil.alignRight(ok));
        ok.setEnabled(false);

        windowControlPanel.add(cancel);
        windowControlPanel.add(ok);

        bottomPanel.add(windowControlPanel);

        final JDialog instance = this;

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                instance.setVisible(false);
                setIndividualsChosen(true);
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                instance.setVisible(false);
                setIndividualsChosen(false || hasMadeSelections);
            }
        });

        addIndividuals.setEnabled(false);
        removeIndividuals.setEnabled(false);

        individualsSTP.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {

                    int rows[] = individualsSTP.getTable().getSelectedRows();

                    boolean someSelection = rows.length > 0;

                    addIndividuals.setEnabled(someSelection);
                    removeIndividuals.setEnabled(someSelection);

                    if (someSelection) {
                        addIndividuals.setText("Add Selected (" + rows.length + ")");
                        removeIndividuals.setText("Remove Selected (" + rows.length + ")");
                    } else {
                        addIndividuals.setText("Add Selected");
                        removeIndividuals.setText("Remove Selected");
                    }

                    /* Close the dialog if only a single individual is requested. */
                    if (onlyOnePatient && rows.length == 1) {
                        selectedRows.clear();
                        selectedHospitalIDs.clear();

                        int realRow = individualsSTP.getActualRowAt(rows[0]);						
						selectedRows.add(realRow);
                        Object[] o = individualsRetriever.getIndividuals().get(realRow);
                        selectedHospitalIDs.add(o[INDEX_OF_HOSPITAL_ID].toString());
						
                        instance.setVisible(false);
                        setIndividualsChosen(true);

                        individualsSTP.getTable().clearSelection(); // if errors crop up, this line may be causing ListSelectionEvents - can be removed
                    }
                }
            }
        });

        individualsSTP.getTable().getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent tme) {
                //addAllIndividuals.setText("Add All (" + stp.getTable().getModel().getRowCount() + ")");
                //removeAllIndividuals.setText("Remove All (" + stp.getTable().getModel().getRowCount() + ")");
            }
        });

        ActionListener addAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                addSelections(false);
            }
        };

        ActionListener addAllAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                addSelections(true);
            }
        };

        ActionListener removeAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                removeSelections(false);
            }
        };

        ActionListener removeAllAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                removeSelections(true);
            }
        };

        addIndividuals.addActionListener(addAction);
        addAllIndividuals.addActionListener(addAllAction);
        removeIndividuals.addActionListener(removeAction);
        removeAllIndividuals.addActionListener(removeAllAction);

        this.pack();
        this.setLocationRelativeTo(MedSavantFrame.getInstance());
    }

    private void setIndividualsChosen(boolean b) {
        this.hasMadeSelections = b;
    }

    private void refreshSelectionIndicator() {

        String tooltipText = "<html>";

        for (String o : this.selectedHospitalIDs) {
            tooltipText += o + "<br>";
        }
        tooltipText += "</html>";

        this.numselections.setToolTipText(tooltipText);

        individualsSTP.setToggledRows(selectedRows);

        numselections.setText(this.selectedHospitalIDs.size() + " individual(s) selected");

        ok.setEnabled(this.selectedHospitalIDs.size() > 0);
    }

    private void clearSelections() {
        selectedHospitalIDs.removeAll(selectedHospitalIDs);
        selectedRows.removeAll(selectedRows);

        individualsSTP.setToggledRows(null);
        refreshSelectionIndicator();
    }

    private void removeSelections(boolean all) {
        List<Object[]> toUnselect = new ArrayList<Object[]>();
        int[] rows;

        if (all) {
            int rowCount = individualsSTP.getTable().getModel().getRowCount();
            rows = new int[rowCount];
            for (int i = 0; i < rowCount; i++) {
                rows[i] = i;
            }
        } else {
            rows = individualsSTP.getTable().getSelectedRows();
        }

        for (int r : rows) {
            int realRow = individualsSTP.getActualRowAt(r);
            Object[] o = individualsRetriever.getIndividuals().get(realRow);
            toUnselect.add(o);
        }

        for (int i : rows) {
            selectedRows.remove(individualsSTP.getActualRowAt(i));
        }

        for (Object[] s : toUnselect) {
            selectedHospitalIDs.remove(s[INDEX_OF_HOSPITAL_ID].toString());
        }

        refreshSelectionIndicator();
    }

    private void addSelections(boolean all) {

        List<Object[]> selected = new ArrayList<Object[]>();
        int[] rows;

        if (all) {
            int rowCount = individualsSTP.getTable().getModel().getRowCount();
            rows = new int[rowCount];
            for (int i = 0; i < rowCount; i++) {
                rows[i] = i;
            }
        } else {
            rows = individualsSTP.getTable().getSelectedRows();
        }

        for (int r : rows) {
            int realRow = individualsSTP.getActualRowAt(r);
            Object[] o = individualsRetriever.getIndividuals().get(realRow);
            selected.add(o);
        }

        for (int i : rows) {
            selectedRows.add(individualsSTP.getActualRowAt(i));
        }

        for (Object[] s : selected) {
            selectedHospitalIDs.add(s[INDEX_OF_HOSPITAL_ID].toString());
        }

        refreshSelectionIndicator();
    }

    public void setSelectedIndividuals(Set<String> s) {
        selectedRows.removeAll(selectedRows);
        selectedHospitalIDs = s;
        hasMadeSelections = true;

        for (String arbitraryHostpitalID : s) {
            int rowNumber = 0;
            List<Object[]> individuals = individualsRetriever.getIndividuals();
            for (Object[] inOrderRow : individuals) {
                if (inOrderRow[INDEX_OF_HOSPITAL_ID].equals(arbitraryHostpitalID)) {
                    selectedRows.add(rowNumber);
                }
                rowNumber++;
            }
        }
        refreshSelectionIndicator();
    }

    public boolean hasMadeSelection() {
        return hasMadeSelections;
    }

    public void resetSelections() {
        setSelectedIndividuals(new HashSet<String>());
        this.hasMadeSelections = false;
    }

    public Set<String> getInverseOfHospitalIDsOfSelectedIndividuals() {
        List<Object[]> allRows = individualsRetriever.getIndividuals();
        Set<String> results = new HashSet<String>();
        Set<String> selected = getHospitalIDsOfSelectedIndividuals();
        for (Object[] row : allRows) {
            String id = (String) row[INDEX_OF_HOSPITAL_ID];
            if (!selected.contains(id)) {
                results.add(id);
            }
        }
        return results;
    }

    public class IndividualsReceiver extends DataRetriever<Object[]> {
        //private DataRetriever<Object[]> getIndividualsRetriever() {

        private List<Object[]> individuals;
        private HashMap<String, List<String>> hospitalIDToCohortMap;

        public List<Object[]> getIndividuals() {
            return individuals;
        }

        @Override
        public List<Object[]> retrieve(int start, int limit) throws Exception {
            if (individuals == null || forceRefresh) {
                setIndividuals();
                forceRefresh = false;
            }
            return individuals;
        }

        @Override
        public int getTotalNum() {
            if (individuals == null) {
                try {
                    setIndividuals();
                } catch (Exception ex) {
                    LOG.error(ex);
                    ex.printStackTrace();
                }
            }
            return individuals.size();
        }

        @Override
        public void retrievalComplete() {
        }

        private void setIndividuals() throws SQLException, RemoteException {
            try {

                setCohorts();

                List<Object[]> tmpIndividuals = MedSavantClient.PatientManager.getBasicPatientInfo(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), Integer.MAX_VALUE);
                List<Object[]> updatedIndividuals = new ArrayList<Object[]>();

                for (Object[] row : tmpIndividuals) {
                    row[INDEX_OF_GENDER] = ClientMiscUtils.genderToString((Integer) row[INDEX_OF_GENDER]);

                    String s;
                    Object o = row[INDEX_OF_AFFECTED];
                    if (o instanceof Boolean) {
                        Boolean b = (Boolean) o;
                        s = b ? "Yes" : "No";
                    } else if (o instanceof Integer) {
                        Integer i = (Integer) o;
                        s = (i > 0) ? "Yes" : "No";
                    } else {
                        s = "Unknown";
                    }
                    row[INDEX_OF_AFFECTED] = s;
                    /*Boolean b = (Boolean) row[INDEX_OF_AFFECTED];
                     String s = b ? "Yes" : "No";
                     row[INDEX_OF_AFFECTED] = s;*/

                    List<String> cohorts = hospitalIDToCohortMap.get((String) row[INDEX_OF_HOSPITAL_ID]);

                    String cohortString = "";
                    if (cohorts != null) {
                        cohortString = StringUtils.join(cohorts.toArray(), ", ");
                    }

                    row = ArrayUtils.addAll(row, new String[]{cohortString});

                    updatedIndividuals.add(row);
                }

                individuals = updatedIndividuals;

            } catch (SessionExpiredException e) {
                MedSavantExceptionHandler.handleSessionExpiredException(e);
            }
        }

        private void setCohorts() throws SQLException, SessionExpiredException, RemoteException {
            hospitalIDToCohortMap = new HashMap<String, List<String>>();
            Cohort[] cohorts = MedSavantClient.CohortManager.getCohorts(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID());
            for (Cohort c : cohorts) {
                c.getId();
                List<SimplePatient> patients = MedSavantClient.CohortManager.getIndividualsInCohort(
                        LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        c.getId());
                for (SimplePatient p : patients) {
                    List<String> cohortsForPatient;
                    if (hospitalIDToCohortMap.containsKey(p.getHospitalId())) {
                        cohortsForPatient = hospitalIDToCohortMap.get(p.getHospitalId());
                    } else {
                        cohortsForPatient = new ArrayList<String>();
                    }
                    cohortsForPatient.add(c.getName());
                    hospitalIDToCohortMap.put(p.getHospitalId(), cohortsForPatient);
                }
            }
        }
    }
}
