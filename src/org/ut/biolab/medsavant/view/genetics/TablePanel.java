/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.util.concurrent.ExecutionException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.view.util.DialogUtil;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import java.awt.CardLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ProjectController.ProjectListener;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.oldcontroller.FilterController;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
class TablePanel extends JPanel implements FiltersChangedListener {
    
    private final SearchableTablePanel tablePanel;
    private static final String CARD_WAIT = "wait";
    private static final String CARD_SHOW = "show";
    private CardLayout cl;

    public TablePanel() {
    
        cl = new CardLayout();
        this.setLayout(cl);
        
        DbTable table = ProjectController.getInstance().getCurrentTable();
        
        List<String> fieldNames = new ArrayList<String>();
        List<Class> fieldClasses = new ArrayList<Class>();
        List<Integer> hiddenColumns = new ArrayList<Integer>();
        
        for(DbColumn col : table.getColumns()){
            fieldNames.add(col.getName());
            //fieldClasses.add(String.class);
            
            String type = col.getTypeNameSQL().toLowerCase();
            if (type.contains("int")){
                fieldClasses.add(Integer.class);
            } else if (type.contains("decimal") || type.contains("float")){
                fieldClasses.add(Double.class);
            } else {
                fieldClasses.add(String.class);
            }
        }
        
        /*for(int i = 0; i < 10; i++){
            hiddenColumns.add(i);
        } */
        
        /*tablePanel = new SearchableTablePanel(
                new Vector(), 
                VariantRecordModel.getFieldNames(), 
                VariantRecordModel.getFieldClasses(), 
                VariantRecordModel.getDefaultColumns(), 
                1000){*/
        tablePanel = new SearchableTablePanel(
                new Vector(),
                fieldNames,
                fieldClasses,
                hiddenColumns,
                1000){
            @Override
            public void forceRefreshData(){
                try {
                    updateTable();
                } catch (Exception ex) {
                    Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                    DialogUtil.displayErrorMessage("Problem getting data.", ex);
                }
            }
        };
        
        this.add(tablePanel, CARD_SHOW);             
        this.add(new WaitPanel("Generating List View"), CARD_WAIT);

        try {
            updateTable();
        } catch (Exception ex) {
            Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
            DialogUtil.displayErrorMessage("Problem getting data.", ex);
        }
        FilterController.addFilterListener(this);
        //ProjectController.getInstance().addProjectListener(this);
    }
    
    private synchronized void showWaitCard() {
        cl.show(this, CARD_WAIT);    
    }

    private synchronized void showShowCard() {
        cl.show(this, CARD_SHOW);
    }

    private void updateTable() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {       
        showWaitCard();
        GetVariantsSwingWorker gv = new GetVariantsSwingWorker();
        gv.execute();
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        updateTable();
    }

    //public void projectAdded(String projectName) {}

    //public void projectRemoved(String projectName) {}

    //public void projectChanged(String projectName) {
        /*try {
            updateTable();
        } catch (Exception e){
            e.printStackTrace();
        }*/
    //}

    /*public void projectTableRemoved(int projid, int refid) {}

    public void referenceChanged(String referenceName) {
        try {
            updateTable();
        } catch (Exception e){
            e.printStackTrace();
        }
    }*/
    
    private class GetVariantsSwingWorker extends SwingWorker {
        @Override
        protected Object doInBackground() throws Exception {
            return ResultController.getInstance().getFilteredVariantRecords(tablePanel.getRetrievalLimit());
            //return Util.convertVariantRecordsToVectors(ResultController.getInstance().getFilteredVariantRecords(tablePanel.getRetrievalLimit()));
        }
        
        @Override
        protected void done() {
            try {
                tablePanel.updateData((Vector) get());
                showShowCard();
            } catch (InterruptedException ex) {
            } catch (Exception e) {
                System.err.println("Error updating table " + e.getMessage());
                e.printStackTrace();
            }
        }  
    }

}