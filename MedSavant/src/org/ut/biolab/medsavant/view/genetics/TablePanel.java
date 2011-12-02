/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableModelWrapperUtils;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.component.Util.DataRetriever;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanelSub;
import org.ut.biolab.medsavant.view.genetics.filter.FilterView;
import org.ut.biolab.medsavant.view.genetics.filter.NumericFilterView;
import org.ut.biolab.medsavant.view.genetics.filter.StringListFilterView;
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
        
        showWaitCard();
        
        final TablePanel instance = this;
        MedSavantWorker worker = new MedSavantWorker(pageName){
            @Override
            protected Object doInBackground() {

                List<String> fieldNames = new ArrayList<String>();
                final List<Class> fieldClasses = new ArrayList<Class>();
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
                tablePanel.getTable().addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {  
                        
                        //check for right click
                        if(!SwingUtilities.isRightMouseButton(e)) return;
                        
                        SortableTable table = tablePanel.getTable();
                        int r = table.rowAtPoint(e.getPoint());
                        if(r < 0 || r >= table.getRowCount()) return;                       
                        table.setRowSelectionInterval(r, r);
                        int row = TableModelWrapperUtils.getActualRowAt(table.getModel(), r);
                        
                        String variantChrom = (String)table.getModel().getValueAt(row, DefaultVariantTableSchema.INDEX_OF_CHROM);
                        int variantPosition = (Integer)table.getModel().getValueAt(row, DefaultVariantTableSchema.INDEX_OF_POSITION);
                        String variantAlt = (String)table.getModel().getValueAt(row, DefaultVariantTableSchema.INDEX_OF_ALT);
                        
                        JPopupMenu popup = createPopup(variantChrom, variantPosition, variantAlt);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                });
                
                return null;
            }

            @Override
            protected void showProgress(double fraction) {
                //do nothing
            }

            @Override
            protected void showSuccess(Object result) {
                instance.add(tablePanel, CARD_SHOW);   
                showShowCard();    
                FilterController.addFilterListener(instance);  
                updateIfRequired();
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
    
    void setUpdateRequired(boolean b) {
        updateRequired = b;
    }
    
    public void updateIfRequired(){
        if(tablePanel == null) return;
        synchronized (updateLock){
            if(updateRequired){
                tablePanel.forceRefreshData();
            }
        }
    }
    
    private JPopupMenu createPopup(final String chrom, final int position, final String alt){
        JPopupMenu menu = new JPopupMenu();
        
        
        //Filter by position
        JMenuItem filter1Item = new JMenuItem("Filter by Position");
        filter1Item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                
                FilterPanel fp = startFilterBy();
                
                //clear all current position filter panels (also removes actual filters)
                for(FilterPanelSub fps : fp.getFilterPanelSubs()){
                    fps.removeFiltersById(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM);
                    fps.removeFiltersById(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION);
                }
  
                //apply position filter to each subquery
                for(FilterPanelSub fps : fp.getFilterPanelSubs()){
                    try {
                        applyStringListFilter(fps, DefaultVariantTableSchema.COLUMNNAME_OF_CHROM, AnnotationFormat.VARIANT_ALIAS_CHROM, chrom);
                        applyNumericFilter(fps, DefaultVariantTableSchema.COLUMNNAME_OF_POSITION, AnnotationFormat.VARIANT_ALIAS_POSITION, position);                                       
                    } catch (SQLException ex) {
                        Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (NonFatalDatabaseException ex) {
                        Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                GeneticsTablePage.getInstance().updateContents();
                fp.refreshSubPanels();
            }
        });
        menu.add(filter1Item);
        
        
        
        //Filter by position and alt
        JMenuItem filter2Item = new JMenuItem("Filter by Position and Alt");
        filter2Item.addActionListener(new ActionListener() {    
            
            public void actionPerformed(ActionEvent e) {
                
                FilterPanel fp = startFilterBy();
                
                //clear all current position filter panels (also removes actual filters)
                for(FilterPanelSub fps : fp.getFilterPanelSubs()){
                    fps.removeFiltersById(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM);
                    fps.removeFiltersById(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION);
                    fps.removeFiltersById(DefaultVariantTableSchema.COLUMNNAME_OF_ALT);
                }
  
                //apply position filter to each subquery
                for(FilterPanelSub fps : fp.getFilterPanelSubs()){
                    try {
                        applyStringListFilter(fps, DefaultVariantTableSchema.COLUMNNAME_OF_CHROM, AnnotationFormat.VARIANT_ALIAS_CHROM, chrom);
                        applyNumericFilter(fps, DefaultVariantTableSchema.COLUMNNAME_OF_POSITION, AnnotationFormat.VARIANT_ALIAS_POSITION, position);
                        applyStringListFilter(fps, DefaultVariantTableSchema.COLUMNNAME_OF_ALT, AnnotationFormat.VARIANT_ALIAS_ALT, alt);
                    } catch (SQLException ex) {
                        Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (NonFatalDatabaseException ex) {
                        Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                GeneticsTablePage.getInstance().updateContents();
                fp.refreshSubPanels();
            }           
        });
        menu.add(filter2Item);

        return menu;
    }

    private FilterPanel startFilterBy(){
        ThreadController.getInstance().cancelWorkers(pageName);

        //get filter panel
        FilterPanel fp = GeneticsFilterPage.getInstance().getFilterPanel();
        if(fp == null){
            GeneticsFilterPage.getInstance().getView(true);
            GeneticsFilterPage.getInstance().setUpdateRequired(false);
            fp = GeneticsFilterPage.getInstance().getFilterPanel();;
        }

        //deal with case where no sub panels
        if(fp.getFilterPanelSubs().isEmpty()){
            fp.createNewSubPanel();
        }
        
        return fp;
    }
    
    private void applyNumericFilter(FilterPanelSub fps, String column, String alias, int value) throws SQLException, NonFatalDatabaseException{
        FilterView filter = NumericFilterView.createVariantFilterView(
                ProjectController.getInstance().getCurrentTableName(), 
                column, 
                fps.getId(), 
                alias, 
                false);
        fps.addNewSubItem(filter, column);
        ((NumericFilterView)filter).applyFilter(value, value);
    }
    
    private void applyStringListFilter(FilterPanelSub fps, String column, String alias, String value) throws SQLException, NonFatalDatabaseException{
        FilterView filter = StringListFilterView.createVariantFilterView(
                ProjectController.getInstance().getCurrentTableName(), 
                column, 
                fps.getId(), 
                alias);
        fps.addNewSubItem(filter, column);
        List<String> values = new ArrayList<String>();
        values.add(value);
        ((StringListFilterView)filter).applyFilter(values);
    }
    
}
