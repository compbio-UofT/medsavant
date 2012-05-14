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

package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jidesoft.converter.ConverterContext;
import com.jidesoft.converter.PercentConverter;
import com.jidesoft.grid.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.model.Cohort;
import org.ut.biolab.medsavant.model.SimplePatient;
import org.ut.biolab.medsavant.util.ExportTable;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author Andrew
 */
public class CohortPanelGenerator implements AggregatePanelGenerator {
    
    private static final Log LOG = LogFactory.getLog(CohortPanelGenerator.class);

    private CohortPanel panel;
    private final String pageName;
    private boolean updateRequired = false;
    private boolean init = false;
    
    
    public CohortPanelGenerator(String pageName) {
        this.pageName = pageName;
    }
    
    @Override
    public String getName() {
        return "Cohort";
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {
            try {
                panel = new CohortPanel();
            } catch (SQLException ex) {
                LOG.error("Error generating cohort panel.", ex);
            }
        } else {
            panel.finish();
        }
        return panel;
    }

    @Override
    public void run(boolean reset) {
        if (reset) {
            panel.init();
        } else if (!init) {
            return;
        } else if (updateRequired) {
            panel.update();
            updateRequired = false;
        } else {
            panel.finish();
        }
    }
    
    @Override
    public void setUpdateRequired(boolean required) {
        updateRequired = true;
    }
    
    private class CohortPanel extends JPanel {
        
        private TreeTable table;
        private SortableTreeTableModel sortableTreeTableModel;
        private CohortTreeTableModel tableModel;
        private ExecutorService threadPool = Executors.newFixedThreadPool(5);
        private List<CohortNode> nodes = new ArrayList<CohortNode>();
        //private ResortWorker resortWorker;
        private JScrollPane container;
        //private boolean resortPending = true;
        private JPanel progressPanel;
        private JButton exportButton;
        //private JProgressBar progress;
        private int numWorking = 0;
        private int numCompleted = 0;
        
        public CohortPanel() throws SQLException {                       
            init();
        }
        
        private void init() {
            
            this.removeAll();
            this.setLayout(new BorderLayout());
            
            //progress = new JProgressBar();
            //progress.setStringPainted(true);
            
            exportButton = new JButton("Export Page");
            exportButton.setEnabled(false);
            exportButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    try {
                        ExportTable.exportTable(table);
                    } catch (Exception ex) {
                        LOG.error("Error exporting table.", ex);
                        DialogUtils.displayException("MedSavant", "<html>A problem occurred while exporting.<br>Make sure the output file is not in use.</html>", ex);
                    }
                }
            });
            
            progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
            //progressPanel.add(Box.createHorizontalStrut(10));
            //progressPanel.add(progress);
            //progressPanel.add(Box.createHorizontalStrut(10));
            progressPanel.add(Box.createHorizontalGlue());
            progressPanel.add(exportButton);           
            progressPanel.add(Box.createRigidArea(new Dimension(10,30)));            
            
            showWaitCard();
            
            MedSavantWorker initWorker = new MedSavantWorker(pageName) {

                @Override
                protected List doInBackground() throws Exception {
                    container = new JScrollPane();
                    add(container, BorderLayout.CENTER);

                    List rows = new ArrayList();
                    List<Cohort> cohorts = MedSavantClient.CohortQueryUtilAdapter.getCohorts(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId());
                    //addToWorking(cohorts.size());
                    for(Cohort c : cohorts) {
                        List<SimplePatient> simplePatients = MedSavantClient.CohortQueryUtilAdapter.getIndividualsInCohort(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), c.getId());           
                        CohortNode n = new CohortNode(c, simplePatients);      
                        nodes.add(n);
                        n.addChild(new LoadingNode()); 
                        /*for(SimplePatient sp : simplePatients) {
                            n.addChild(new PatientNode(sp));
                        }*/
                        rows.add(n);
                    }

                    
                    
                    return rows;
                }

                @Override
                protected void showProgress(double fraction) {}

                @Override
                protected void showSuccess(Object result) {
                    
                    try {
                        tableModel = new CohortTreeTableModel((List)get());
                    } catch (InterruptedException ex) {
                        LOG.warn("Creating cohort tree model interrupted.", ex);
                    } catch (ExecutionException ex) {
                        LOG.error("Error creating cohort tree model.", ex);
                    }

                    sortableTreeTableModel = new StripeSortableTreeTableModel(tableModel);
                    sortableTreeTableModel.setAutoResort(false);
                    sortableTreeTableModel.setOptimized(true);
                    sortableTreeTableModel.setAlwaysUseComparators(true);
                    table = new TreeTable(sortableTreeTableModel);

                    container.getViewport().add(table);
                    
                    showShowCard();
                    init = true;
                }
            };
            initWorker.execute();
        }
        
        private void showWaitCard() {
            removeAll();
            add(new WaitPanel("Getting aggregate information"), BorderLayout.CENTER);
            updateUI();
        }

        private void showShowCard() {
            removeAll();     
            add(progressPanel, BorderLayout.NORTH);
            add(container, BorderLayout.CENTER);
            updateUI();
        }
        
        public void update() {
            //resetProgress();
            //addToWorking(nodes.size());
            table.collapseAll();
            for(CohortNode n : nodes) {               
                n.reset();
            }
            //startResortWorker();
        }
        
        public void finish() {
            if (updateRequired) {
                update();
            } else {
                //resetProgress();
                for(CohortNode n : nodes) {
                    n.finish();
                }
            }
            //startResortWorker();
        }

        public void addThread(MedSavantWorker worker) {
            threadPool.submit(worker);
        }
        
        public synchronized void refresh() {
            sortableTreeTableModel.resort();
            table.repaint();
            //resortPending = false;
        }
        
        /* Progress */
        
        /*public synchronized void resetProgress() {
            numCompleted = 0;
            numWorking = 0;
        }
        
        public synchronized void incrementCompleted() {
            numCompleted++;
            updateProgress();
        }
        
        public synchronized void decrementWorking() {
            numWorking--;
            updateProgress();
        }
        
        public synchronized void addToWorking(int num) {
            numWorking += num;
            updateProgress();
        }
        
        public synchronized void updateProgress() {
            if (numWorking == 0) {
                numCompleted = 0;
                progress.setValue(100);
                exportButton.setEnabled(true);
            } else if (numWorking <= numCompleted) {
                numWorking = 0;
                numCompleted = 0;
                progress.setValue(100);
                exportButton.setEnabled(true);
            } else {
                progress.setValue((int)((double)numCompleted / (double)numWorking * 100.0));
                exportButton.setEnabled(false);
            }
        }*/
    }

    private class CohortNode extends DefaultExpandableRow{
        
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
            run();
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
        
        public void run() {           
            MedSavantWorker worker = new MedSavantWorker(pageName) {

                @Override
                protected void showProgress(double fraction) {}
                
                @Override
                protected Object doInBackground() throws Exception {
                    if (this.isThreadCancelled()) {
                        return -1;
                    }
                    return MedSavantClient.CohortQueryUtilAdapter.getNumVariantsInCohort(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), ReferenceController.getInstance().getCurrentReferenceId(), cohort.getId(), FilterController.getQueryFilterConditions());
                }

                @Override
                protected void showSuccess(Object result) {
                    value = (Integer) result;
                    //panel.setResortPending(true);
                    panel.refresh();
                    panel.repaint();
                    //panel.incrementCompleted();
                    cleanup();
                }
               
            };
            worker.execute();
        }
        
        public void reset() {
            value = -1;
            this.removeAllChildren();
            setExpanded(false);
            addChild(new LoadingNode()); 
            run();
        }
        
        public void finish() {
            if (value == -1) {
                reset();
            }
        }
        
        private void expand() {
            if (!this.hasChildren() || this.getChildAt(0) instanceof PatientNode) return; //already computed
            MedSavantWorker worker = new MedSavantWorker(pageName) {

                @Override
                protected void showProgress(double fraction) {}
                
                @Override
                protected Object doInBackground() throws Exception {
                    return MedSavantClient.VariantQueryUtilAdapter.getPatientHeatMap(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), ReferenceController.getInstance().getCurrentReferenceId(), FilterController.getQueryFilterConditions(), patients);
                }

                @Override
                protected void showSuccess(Object result) {
                    Map<SimplePatient, Integer> map = (Map<SimplePatient, Integer>)result;
                    removeAllChildren();
                    for(SimplePatient key : map.keySet()) {
                        addChild(new PatientNode(key, map.get(key)));
                    }
                    panel.refresh();
                }
               
            };
            worker.execute();
            
        }

    }
    
    private class PatientNode extends DefaultExpandableRow {

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
    
    private class LoadingNode extends DefaultExpandableRow {
        
        @Override
        public Object getValueAt(int i) {
            return "Loading...";
        }
        
    }

    class StripeSortableTreeTableModel extends SortableTreeTableModel implements StyleModel {
        private static final long serialVersionUID = 7707484364461990477L;

        public StripeSortableTreeTableModel(TreeTableModel model) {
            super(model);
        }

        protected final Color BACKGROUND1 = Color.white;
        protected final Color BACKGROUND2 = new Color(242, 245, 249);

        CellStyle cellStyle = new CellStyle();

        @Override
        public CellStyle getCellStyleAt(int rowIndex, int columnIndex) {
            cellStyle.setHorizontalAlignment(-1);
            cellStyle.setForeground(Color.BLACK);
            if (rowIndex % 2 == 0) {
                cellStyle.setBackground(BACKGROUND1);
            } else {
                cellStyle.setBackground(BACKGROUND2);
            }
            return cellStyle;
        }

        @Override
        public boolean isCellStyleOn() {
            return true;
        }
        
        @Override
        public Comparator getComparator(int column) {
            if (column == 0) {
                return super.getComparator(column);
            } else {
                return valueComparator;
            }
        }
    }
    
    static Comparator valueComparator = new Comparator() {
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
    
    static class CohortTreeTableModel extends TreeTableModel implements StyleModel {
        private static final long serialVersionUID = 3589523753024111735L;

        public CohortTreeTableModel() {
        }

        public CohortTreeTableModel(java.util.List rows) {
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
                    return "Patient Id";
                case 2:
                    return "Count";
            }
            return null;
        }

        static CellStyle BOLD = new CellStyle();

        static {
            BOLD.setFontStyle(Font.BOLD);
            BOLD.setBackground(new Color(50,53,59));
            BOLD.setForeground(Color.white);
        }

        @Override
        public CellStyle getCellStyleAt(int rowIndex, int columnIndex) {
            Row row = getRowAt(rowIndex);
            if (row.getParent() == getRoot() || (row instanceof ExpandableRow && ((ExpandableRow) row).hasChildren())) {
                return BOLD;
            }
            return null;
        }

        @Override
        public boolean isCellStyleOn() {
            return true;
        }
    }
    
}