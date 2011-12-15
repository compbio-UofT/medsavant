/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import com.jidesoft.converter.ConverterContext;
import com.jidesoft.converter.PercentConverter;
import com.jidesoft.grid.CellStyle;
import com.jidesoft.grid.DefaultExpandableRow;
import com.jidesoft.grid.ExpandableRow;
import com.jidesoft.grid.Row;
import com.jidesoft.grid.SortableTreeTableModel;
import com.jidesoft.grid.StyleModel;
import com.jidesoft.grid.TreeTable;
import com.jidesoft.grid.TreeTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.db.model.SimplePatient;
import org.ut.biolab.medsavant.db.util.query.CohortQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author Andrew
 */
public class CohortPanelGenerator implements AggregatePanelGenerator, FiltersChangedListener {
    
    private CohortPanel panel;
    private final String pageName;
    private boolean updateRequired = false;
    
    public CohortPanelGenerator(String pageName){
        this.pageName = pageName;
        FilterController.addFilterListener(this);
    }
    
    public String getName() {
        return "Cohort";
    }

    public JPanel getPanel() {
        if (panel == null) {
            try {
                panel = new CohortPanel();
            } catch (SQLException ex) {
                Logger.getLogger(CohortPanelGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        return panel;
    }

    public void run() {
        if(updateRequired) {
            panel.update();
            updateRequired = false;
        } else {
            panel.finish();
        }
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        updateRequired = true;
    }
    
    private class CohortPanel extends JPanel {
        
        private TreeTable table;
        private SortableTreeTableModel sortableTreeTableModel;
        private CohortTreeTableModel tableModel;
        private ExecutorService threadPool = Executors.newFixedThreadPool(5);
        private List<CohortNode> nodes = new ArrayList<CohortNode>();
        private ResortWorker resortWorker;
        private JScrollPane container;
        private boolean resortPending = true;
        
        public CohortPanel() throws SQLException {
            
            this.setLayout(new BorderLayout());
            
            showWaitCard();
            
            SwingWorker initWorker = new SwingWorker() {

                @Override
                protected Object doInBackground() throws Exception {
                    container = new JScrollPane();
                    add(container, BorderLayout.CENTER);

                    List rows = new ArrayList();
                    List<Cohort> cohorts = CohortQueryUtil.getCohorts(ProjectController.getInstance().getCurrentProjectId());
                    for(Cohort c : cohorts){
                        CohortNode n = new CohortNode(c);      
                        nodes.add(n);
                        List<SimplePatient> simplePatients = CohortQueryUtil.getIndividualsInCohort(ProjectController.getInstance().getCurrentProjectId(), c.getId());               
                        for(SimplePatient sp : simplePatients){
                            n.addChild(new PatientNode(sp));
                        }
                        rows.add(n);
                    }

                    tableModel = new CohortTreeTableModel(rows);

                    sortableTreeTableModel = new StripeSortableTreeTableModel(tableModel);
                    sortableTreeTableModel.setAutoResort(false);
                    sortableTreeTableModel.setOptimized(true);
                    sortableTreeTableModel.setAlwaysUseComparators(true);
                    table = new TreeTable(sortableTreeTableModel);

                    container.getViewport().add(table);
                    return null;
                }
                
                @Override
                public void done() {
                    startResortWorker();     
                    showShowCard();
                }
            };
            initWorker.execute();

        }
        
        private void showWaitCard(){
            removeAll();
            add(new WaitPanel("Getting aggregate information"), BorderLayout.CENTER);
            updateUI();
        }

        private void showShowCard(){
            removeAll();           
            add(container, BorderLayout.CENTER);
            updateUI();
        }
        
        public void update(){
            for(CohortNode n : nodes){
                table.collapseAll();
                n.reset();
            }
            startResortWorker();
        }
        
        public void finish(){
            for(CohortNode n : nodes){
                n.finish();
            }
            startResortWorker();
        }
        
        private void startResortWorker(){
            if(resortWorker != null){
                resortWorker.cancel(true);
            }
            resortWorker = new ResortWorker(pageName);
            resortWorker.execute();
        }
        
        public void addThread(MedSavantWorker worker){
            threadPool.submit(worker);
        }
        
        public synchronized void refresh(){
            if(resortPending){
                sortableTreeTableModel.resort();
                table.repaint();
                resortPending = false;
            }
        }
        
        public synchronized void setResortPending(boolean pending){
            resortPending = pending;
        }
    }

    private class CohortNode extends DefaultExpandableRow{
        
        private Cohort cohort;       
        private int value = -1;

        public CohortNode(Cohort c) {
            this.cohort = c;
            this.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(evt.getPropertyName().equals(DefaultExpandableRow.PROPERTY_EXPANDED)){
                        if(isExpanded()){
                            expand();
                        } else {
                            collapse();
                        }
                    }
                }
            });
            run();
        }

        public Object getValueAt(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return cohort.getName();
                case 1:
                    if(value == -1){
                        return "???";
                    } else {
                        return Integer.toString(value);
                    }
            }
            return null;
        }
        
        public void run(){           
            MedSavantWorker worker = new MedSavantWorker(pageName) {

                @Override
                protected void showProgress(double fraction) {}
                
                @Override
                protected Object doInBackground() throws Exception {
                    if(this.isThreadCancelled()) return -1;
                    return CohortQueryUtil.getNumVariantsInCohort(ProjectController.getInstance().getCurrentProjectId(), ReferenceController.getInstance().getCurrentReferenceId(), cohort.getId(), FilterController.getQueryFilterConditions());
                }

                @Override
                protected void showSuccess(Object result) {
                    value = (Integer) result;
                    panel.setResortPending(true);
                    panel.repaint();
                    cleanup();
                }
               
            };
            worker.execute();
        }
        
        public void reset(){
            value = -1;
            setExpanded(false);
            
            for(Object o : getChildren()){
                ((PatientNode)o).reset();
            }
            run();
        }
        
        public void finish(){
            if(this.isExpanded()){
                expand();
            }
        }
        
        private void expand(){
            for(Object o : getChildren()){
                ((PatientNode)o).finish();
            }
        }

        private void collapse(){
            for(Object o : getChildren()){
                ((PatientNode)o).cancel();
            }
        }

    }
    
    private class PatientNode extends DefaultExpandableRow {
        
        private SimplePatient patient;
        private int value = -1;
        private MedSavantWorker worker = null;
        
        public PatientNode(SimplePatient p){
            this.patient = p;
        }
        
        public Object getValueAt(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return patient.getHospitalId();
                case 1:
                    if(value == -1){
                        return "???";
                    } else {
                        return Integer.toString(value);
                    }
            }
            return null;
        }
        
        public void reset(){
            value = -1;
        }
        
        public void finish(){
            if(value == -1){
                run();
            }
        }
        
        public void cancel(){
            if(worker != null){
                worker.cancel(true);
            }
        }
        
        private void run(){
            worker = new MedSavantWorker(pageName) {

                @Override
                protected void showProgress(double fraction) {}
                
                @Override
                protected Object doInBackground() throws Exception {
                    if(this.isThreadCancelled()) {
                        return -1;
                    }
                    return VariantQueryUtil.getNumVariantsForDnaIds(ProjectController.getInstance().getCurrentProjectId(), ReferenceController.getInstance().getCurrentReferenceId(), FilterController.getQueryFilterConditions(), patient.getDnaIds());
                }

                @Override
                protected void showSuccess(Object result) {
                    if(this.isThreadCancelled()){
                        value = -1;
                    } else {
                        value = (Integer) result;
                    }       
                    panel.setResortPending(true);
                    panel.repaint();
                    cleanup();
                }
               
            };
            panel.addThread(worker); 
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
            }
            else {
                cellStyle.setBackground(BACKGROUND2);
            }
            return cellStyle;
        }

        @Override
        public boolean isCellStyleOn() {
            return true;
        }
        
        @Override
        public Comparator getComparator(int column){
            if(column == 0){
                return super.getComparator(column);
            } else {
                return valueComparator;
            }
        }
    }
    
    static Comparator valueComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            try {
                Integer i1 = Integer.parseInt((String)o1);
                Integer i2 = Integer.parseInt((String)o2);
                if(i1 == 114165 || i2 == 114165){
                    int a = 0;
                }
                return i1.compareTo(i2);
            } catch (NumberFormatException e){
                if(((String)o1).startsWith("?")){
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

        public int getColumnCount() {
            return 2;
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
            }
            return Object.class;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Name";
                case 1:
                    return "Count";
            }
            return null;
        }

        static CellStyle BOLD = new CellStyle();

        static {
            BOLD.setFontStyle(Font.BOLD);
        }

        public CellStyle getCellStyleAt(int rowIndex, int columnIndex) {
            Row row = getRowAt(rowIndex);
            if (row.getParent() == getRoot() || (row instanceof ExpandableRow && ((ExpandableRow) row).hasChildren())) {
                return BOLD;
            }
            return null;
        }

        public boolean isCellStyleOn() {
            return true;
        }
    }
    
    //TODO: this doesn't really need to run all the time. Only when nodes are being updated.
    private class ResortWorker extends MedSavantWorker {

        public ResortWorker(String pageName){
            super(pageName);
        }
        
        @Override
        protected void showProgress(double fraction) {}

        @Override
        protected Object doInBackground() throws Exception {
            while(true){  
                if(this.isThreadCancelled()) {
                    return null;
                }
                Thread.sleep(3000);
                panel.refresh();
            }
        }
        
        @Override
        protected void showSuccess(Object result) {}

    }
    
}