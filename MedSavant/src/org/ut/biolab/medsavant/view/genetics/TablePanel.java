/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import java.awt.CardLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.component.Util.DataRetriever;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
class TablePanel extends JPanel implements FiltersChangedListener {
    
    private SearchableTablePanel tablePanel;
    private static final String CARD_WAIT = "wait";
    private static final String CARD_SHOW = "show";

    private CardLayout cl;
    private boolean init = false;
    private boolean updateRequired = true;
    private final Object updateLock = new Object();
    private String pageName;
    
    public TablePanel(final String pageName) {
            
        this.pageName = pageName;
        cl = new CardLayout();
        this.setLayout(cl);
        this.add(new WaitPanel("Generating List View"), CARD_WAIT);
        
        long startTime = System.currentTimeMillis();

        showWaitCard();
        
        final TablePanel instance = this;
        MedSavantWorker worker = new MedSavantWorker(pageName){
            @Override
            protected Object doInBackground() {

                List<String> fieldNames = new ArrayList<String>();
                List<Class> fieldClasses = new ArrayList<Class>();
                List<Integer> hiddenColumns = new ArrayList<Integer>();
                            
                AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
                for(AnnotationFormat af : afs){
                    for(CustomField field : af.getCustomFields()){
                        fieldNames.add(field.getAlias());
                        switch(field.getColumnType()){
                            case INTEGER:
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
                        if(field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID) ||
                                field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID) ||
                                field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_ID)){
                            hiddenColumns.add(fieldNames.size()-1);
                        }
                    }
                }
                if(this.isThreadCancelled()) return null;
                
                DataRetriever retriever = new DataRetriever(){
                    public List<Object[]> retrieve(int start, int limit) {
                        showWaitCard();
                        List<Object[]> result = null;
                        try {
                            result = ResultController.getInstance().getFilteredVariantRecords(start, limit);
                        } catch (NonFatalDatabaseException ex) {
                            Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        showShowCard();
                        return result;
                    }

                    public int getTotalNum() {
                        showWaitCard();
                        int result = 0;
                        try {
                            result = ResultController.getInstance().getNumFilteredVariants();
                        } catch (NonFatalDatabaseException ex) {
                            Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        showShowCard();
                        return result;
                    }

                    public void retrievalComplete() {
                        synchronized (updateLock){
                            updateRequired = false;
                        }
                    }
                };
                tablePanel = new SearchableTablePanel(pageName, fieldNames, fieldClasses, hiddenColumns, 1000, retriever);
                updateIfRequired();
                
                return null;
            }

            @Override
            protected void showProgress(double fraction) {
                //do nothing
            }

            @Override
            protected void showSuccess(Object result) {
                instance.add(tablePanel, CARD_SHOW);             
                FilterController.addFilterListener(instance);  
                init = true;
            }

        };
        worker.execute();
        
    }
    
    private void showWaitCard() {
        cl.show(this, CARD_WAIT);    
    }

    private void showShowCard() {
        cl.show(this, CARD_SHOW);
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {       
        synchronized (updateLock){
            updateRequired = true;
        }
    }
    
    public boolean isInit(){
        return init;
    }
    
    public void updateIfRequired(){
        if(tablePanel == null) return;
        synchronized (updateLock){
            if(updateRequired){
                tablePanel.forceRefreshData();
            }
        }
    }
    
}
