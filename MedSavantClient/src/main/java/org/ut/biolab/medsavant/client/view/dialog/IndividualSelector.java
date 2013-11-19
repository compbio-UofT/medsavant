/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.login.LoginController;
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
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.ModificationType;

/**
 *
 * @author mfiume
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
        PHENOTYPES.getAlias()};
    private static final Class[] COLUMN_CLASSES = new Class[]{
        Integer.class, // patient
        String.class, // family
        String.class, // hospital
        String.class, // mom id
        String.class, // dad id
        String.class, // gender
        String.class, // affected
        String.class, // dna ids
        String.class}; // phenotypes
    private static final int[] HIDDEN_COLUMNS = new int[]{
        0, // patient
        3, // mom
        4, // dad
        7 // dna
    };
    private SearchableTablePanel stp;
    private JLabel numselections;
    private IndividualsReceiver retriever;
    private HashSet<Integer> selectedRows;
    private JButton ok;
    private boolean hasMadeSelections;

    public IndividualSelector() {
        super(MedSavantFrame.getInstance(), true);
        setTitle("Select Individuals");
        this.setPreferredSize(new Dimension(700, 600));
        this.setMinimumSize(new Dimension(700, 600));
        selectedHospitalIDs = new HashSet<String>();
        selectedRows = new HashSet<Integer>();
        initUI();
        refresh();
    }

    public Set<String> getHospitalIDsOfSelectedIndividuals() {
        return selectedHospitalIDs;
    }

    public Set<String> getDNAIDsOfSelectedIndividuals() {
        Set<String> ids = new HashSet<String>();
        for (int i : selectedRows) {
            String dnaID = (String) retriever.getIndividuals().get(i)[INDEX_OF_DNA_IDS];
            ids.add(dnaID);
        }
        return ids;
    }

    public void refresh() {
        stp.forceRefreshData();
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

        // middle
        middlePanel.setLayout(new BorderLayout());

        retriever = new IndividualsReceiver();

        stp = new SearchableTablePanel("Individuals", COLUMN_NAMES, COLUMN_CLASSES, HIDDEN_COLUMNS,
                true, true, Integer.MAX_VALUE, false, SearchableTablePanel.TableSelectionType.ROW, Integer.MAX_VALUE, retriever);
        
        //If patients or cohorts are edited, update the searchabletable.
        CacheController.getInstance().addListener(new Listener<ModificationType>(){
            @Override
            public void handleEvent(ModificationType event) {
                if(event == ModificationType.PATIENT || event == ModificationType.COHORT){                           
                    if(!stp.isUpdating()){                        
                        forceRefresh = true;
                        stp.forceRefreshData();
                    }
                }
            }
            
        });
        stp.setExportButtonVisible(false);

        middlePanel.add(stp, BorderLayout.CENTER);

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

        stp.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {

                    int rows[] = stp.getTable().getSelectedRows();

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
                }
            }
        });

        stp.getTable().getModel().addTableModelListener(new TableModelListener() {
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

        stp.setToggledRows(selectedRows);

        numselections.setText(this.selectedHospitalIDs.size() + " individual(s) selected");

        ok.setEnabled(this.selectedHospitalIDs.size() > 0);
    }

    private void clearSelections() {
        selectedHospitalIDs.removeAll(selectedHospitalIDs);
        selectedRows.removeAll(selectedRows);

        stp.setToggledRows(null);
        refreshSelectionIndicator();
    }

    private void removeSelections(boolean all) {
        List<Object[]> toUnselect = new ArrayList<Object[]>();
        int[] rows;

        if (all) {
            int rowCount = stp.getTable().getModel().getRowCount();
            rows = new int[rowCount];
            for (int i = 0; i < rowCount; i++) {
                rows[i] = i;
            }
        } else {
            rows = stp.getTable().getSelectedRows();
        }

        for (int r : rows) {
            int realRow = stp.getActualRowAt(r);
            Object[] o = retriever.getIndividuals().get(realRow);
            toUnselect.add(o);
        }

        for (int i : rows) {
            selectedRows.remove(stp.getActualRowAt(i));
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
            int rowCount = stp.getTable().getModel().getRowCount();
            rows = new int[rowCount];
            for (int i = 0; i < rowCount; i++) {
                rows[i] = i;
            }
        } else {
            rows = stp.getTable().getSelectedRows();
        }

        for (int r : rows) {
            int realRow = stp.getActualRowAt(r);
            Object[] o = retriever.getIndividuals().get(realRow);
            selected.add(o);
        }

        for (int i : rows) {
            selectedRows.add(stp.getActualRowAt(i));
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
            List<Object[]> individuals = retriever.getIndividuals();
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
        List<Object[]> allRows = retriever.getIndividuals();
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
                }
            }
            return individuals.size();
        }

        @Override
        public void retrievalComplete() {
        }

        private void setIndividuals() throws SQLException, RemoteException {
            try {
                individuals = MedSavantClient.PatientManager.getBasicPatientInfo(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), Integer.MAX_VALUE);

                for (Object[] row : individuals) {
                    row[INDEX_OF_GENDER] = ClientMiscUtils.genderToString((Integer) row[INDEX_OF_GENDER]);
                    
                    String s;
                    Object o = row[INDEX_OF_AFFECTED];
                    if (o instanceof Boolean) {
                        Boolean b = (Boolean) o;
                        s = b ? "Yes" : "No";
                    } else if(o instanceof Integer){
                        Integer i = (Integer)o;
                        s = (i>0) ? "Yes" : "No";
                    }else{
                        s = "Unknown";
                    }
                    row[INDEX_OF_AFFECTED] = s;
                    /*Boolean b = (Boolean) row[INDEX_OF_AFFECTED];
                    String s = b ? "Yes" : "No";
                    row[INDEX_OF_AFFECTED] = s;*/
                }

            } catch (SessionExpiredException e) {
                MedSavantExceptionHandler.handleSessionExpiredException(e);
            }
        }
    }
}
