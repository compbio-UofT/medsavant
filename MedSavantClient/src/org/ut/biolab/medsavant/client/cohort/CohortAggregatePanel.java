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

import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jidesoft.converter.ConverterContext;
import com.jidesoft.converter.PercentConverter;
import com.jidesoft.grid.*;
import java.awt.BorderLayout;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.aggregate.AggregatePanel;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.SimplePatient;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.StripySortableTreeTableModel;
import org.ut.biolab.medsavant.client.view.util.WaitPanel;

/**
 *
 * @author Andrew
 */
public class CohortAggregatePanel extends AggregatePanel {

    private static final Comparator VALUE_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            try {
                Integer i1 = Integer.parseInt((String)o1);
                Integer i2 = Integer.parseInt((String)o2);
                return i1.compareTo(i2);
            } catch (NumberFormatException e) {
                if (((String)o1).startsWith("?")) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    };

    private boolean init = false;

    private TreeTable table;
    private SortableTreeTableModel sortableTreeTableModel;
    private CohortTreeTableModel tableModel;
    private List<CohortNode> nodes = new ArrayList<CohortNode>();
    private JScrollPane container;
    private JPanel progressPanel;
    private JButton exportButton;

    public CohortAggregatePanel(String page) {
        super(page);
        init();
    }

    private void init() {
        removeAll();
        setLayout(new BorderLayout());

        /*exportButton = new JButton("Export Page");
        exportButton.setEnabled(false);
        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    ExportTable.exportTable(table);
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("A problem occurred while exporting: %s\nMake sure the output file is not in use.", ex);
                }
            }
        });
        */

        progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
        progressPanel.add(Box.createHorizontalGlue());
        //progressPanel.add(exportButton);
        progressPanel.add(Box.createRigidArea(new Dimension(10,30)));

        showWaitCard();

        new MedSavantWorker<CohortTreeTableModel>(pageName) {

            @Override
            protected CohortTreeTableModel doInBackground() throws Exception {
                container = new JScrollPane();
                add(container, BorderLayout.CENTER);

                List rows = new ArrayList();
                Cohort[] cohorts = MedSavantClient.CohortManager.getCohorts(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID());
                for (Cohort c : cohorts) {
                    List<SimplePatient> simplePatients = MedSavantClient.CohortManager.getIndividualsInCohort(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), c.getId());
                    CohortNode n = new CohortNode(c, simplePatients);
                    nodes.add(n);
                    n.addChild(new LoadingNode());
                    rows.add(n);
                }

                return new CohortTreeTableModel(rows);
            }

            @Override
            protected void showProgress(double fraction) {}

            @Override
            protected void showSuccess(CohortTreeTableModel result) {
                tableModel = result;
                sortableTreeTableModel = new StripySortableTreeTableModel(tableModel) {
                    @Override
                    public Comparator getComparator(int col) {
                        if (col == 0) {
                            return super.getComparator(col);
                        } else {
                            return VALUE_COMPARATOR;
                        }
                    }
                };
                sortableTreeTableModel.setAutoResort(false);
                sortableTreeTableModel.setOptimized(true);
                sortableTreeTableModel.setAlwaysUseComparators(true);
                table = new TreeTable(sortableTreeTableModel);

                container.getViewport().add(table);

                showShowCard();
                init = true;
            }
        }.execute();
    }

    private void showWaitCard() {
        removeAll();
        add(new WaitPanel("Getting aggregate information"), BorderLayout.CENTER);
    }

    private void showShowCard() {
        removeAll();
        add(progressPanel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
    }

    public void update() {
        if (table != null) {
            table.collapseAll();
            for (CohortNode n : nodes) {
                n.reset();
            }
        }
    }

    @Override
    public void recalculate() {
        update();
    }

    public synchronized void refresh() {
        // Added check that the model has actually been created.
        if (sortableTreeTableModel != null) {
            sortableTreeTableModel.resort();
            table.repaint();
        }
    }

    private class CohortNode extends DefaultExpandableRow {

        private Cohort cohort;
        private List<SimplePatient> patients;
        private int value = -1;

        public CohortNode(Cohort c, List<SimplePatient> p) {
            this.cohort = c;
            this.patients = p;
            this.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(DefaultExpandableRow.PROPERTY_EXPANDED)) {
                        if (isExpanded()) {
                            expand();
                        } else {
                            //collapse();
                        }
                    }
                }
            });
            getVariantCount();
        }

        @Override
        public Object getValueAt(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return cohort.getName();
                case 1:
                    return "";
                case 2:
                    if (value == -1) {
                        return "Loading...";
                    } else {
                        return Integer.toString(value);
                    }
            }
            return null;
        }

        private void getVariantCount() {
            new MedSavantWorker<Integer>(pageName) {

                @Override
                protected void showProgress(double fraction) {}

                @Override
                protected Integer doInBackground() throws Exception {
                    return MedSavantClient.CohortManager.getNumVariantsInCohort(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID(), cohort.getId(), FilterController.getInstance().getAllFilterConditions());
                }

                @Override
                protected void showSuccess(Integer result) {
                    value = result;
                    refresh();
                    repaint();
                }

            }.execute();
        }

        public void reset() {
            value = -1;
            this.removeAllChildren();
            setExpanded(false);
            addChild(new LoadingNode());
            getVariantCount();
        }

        public void finish() {
            if (value == -1) {
                reset();
            }
        }

        private void expand() {
            if (hasChildren() && !(getChildAt(0) instanceof PatientNode)) {
                new MedSavantWorker<Map<SimplePatient, Integer>>(pageName) {

                    @Override
                    protected void showProgress(double fraction) {}

                    @Override
                    protected Map<SimplePatient, Integer> doInBackground() throws Exception {
                        return MedSavantClient.VariantManager.getPatientHeatMap(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID(), FilterController.getInstance().getAllFilterConditions(), patients);
                    }

                    @Override
                    protected void showSuccess(Map<SimplePatient, Integer> result) {
                        removeAllChildren();
                        for (SimplePatient key: result.keySet()) {
                            addChild(new PatientNode(key, result.get(key)));
                        }
                        refresh();
                    }

                }.execute();
            }
        }
    }

    private static class PatientNode extends DefaultExpandableRow {

        private SimplePatient patient;
        private int value;

        public PatientNode(SimplePatient p, Integer count) {
            this.patient = p;
            this.value = count;
        }

        @Override
        public Object getValueAt(int i) {
            switch (i) {
                case 0:
                    return null;
                case 1:
                    return patient.getHospitalId();
                case 2:
                    return Integer.toString(value);
            }
            return null;
        }

    }

    private static class LoadingNode extends DefaultExpandableRow {

        @Override
        public Object getValueAt(int i) {
            return "Loading...";
        }

    }

    private static class CohortTreeTableModel extends TreeTableModel implements StyleModel {

        private CohortTreeTableModel() {
        }

        private CohortTreeTableModel(java.util.List rows) {
            super(rows);
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public ConverterContext getConverterContextAt(int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                if (rowIndex == 0) {
    //                    return super.getConverterContextAt(rowIndex, columnIndex);
                }
                return PercentConverter.CONTEXT;
            }
            return super.getConverterContextAt(rowIndex, columnIndex);
        }

        @Override
        public Class<?> getCellClassAt(int rowIndex, int columnIndex) {
            return getColumnClass(columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return String.class;
                case 2:
                    return String.class;
            }
            return Object.class;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Cohort Name";
                case 1:
                    return "Individual Id";
                case 2:
                    return "Count";
            }
            return null;
        }

        @Override
        public CellStyle getCellStyleAt(int rowIndex, int columnIndex) {
            Row row = getRowAt(rowIndex);
            if (row.getParent() == getRoot() || (row instanceof ExpandableRow && ((ExpandableRow) row).hasChildren())) {
                CellStyle bold = new CellStyle();
                bold.setFontStyle(Font.BOLD);
                return bold;
            }
            return null;
        }

        @Override
        public boolean isCellStyleOn() {
            return true;
        }
    }
}
