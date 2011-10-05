/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.view.util.DialogUtil;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
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
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.util.query.AnnotationField;
import org.ut.biolab.medsavant.db.util.query.AnnotationFormat;
import org.ut.biolab.medsavant.oldcontroller.FilterController;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
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

        List<String> fieldNames = new ArrayList<String>();
        List<Class> fieldClasses = new ArrayList<Class>();
        List<Integer> hiddenColumns = new ArrayList<Integer>();
        
        AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
        for(AnnotationFormat af : afs){
            for(AnnotationField field : af.getAnnotationFields()){
                fieldNames.add(field.getAlias());
                switch(field.getFieldType()){
                    case INT:
                    case BOOLEAN:
                        fieldClasses.add(Integer.class);
                        break;
                    case FLOAT:
                    case DECIMAL:
                        fieldClasses.add(Double.class);
                        break;
                    case VARCHAR:
                    default:
                        fieldClasses.add(String.class);
                        break;
                }
            }
        }

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
    
    private class GetVariantsSwingWorker extends SwingWorker {
        @Override
        protected Object doInBackground() throws Exception {
            return ResultController.getInstance().getFilteredVariantRecords(tablePanel.getRetrievalLimit());
        }
        
        @Override
        protected void done() {
            try {
                tablePanel.updateData((Vector) get());
                showShowCard();
            } catch (InterruptedException ex) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }  
    }

}