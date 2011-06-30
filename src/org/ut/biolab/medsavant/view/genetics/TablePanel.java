/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.genetics;

import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.view.util.DialogUtil;
import fiume.table.SearchableTablePanel;
import java.awt.CardLayout;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ResultController;
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
        
        tablePanel = new SearchableTablePanel(new Vector(), VariantRecordModel.getFieldNames(), VariantRecordModel.getFieldClasses(), VariantRecordModel.getDefaultColumns());
        this.add(tablePanel, CARD_SHOW);             
        this.add(new WaitPanel("Generating List View"), CARD_WAIT);

        try {
            updateTable();
        } catch (Exception ex) {
            Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
            DialogUtil.displayErrorMessage("Problem getting data.", ex);
        }
        FilterController.addFilterListener(this);
    }
    
    private synchronized void showWaitCard() {
        cl.show(this, CARD_WAIT);    
    }

    private synchronized void showShowCard() {
        cl.show(this, CARD_SHOW);
    }

    private void updateTable() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {       
        GetVariantsSwingWorker gv = new GetVariantsSwingWorker();
        gv.execute();
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        updateTable();
    }
    
    private class GetVariantsSwingWorker extends SwingWorker {
        @Override
        protected Object doInBackground() throws Exception {
            showWaitCard();
            tablePanel.updateData(Util.convertVariantRecordsToVectors(ResultController.getInstance().getFilteredVariantRecords()));
            showShowCard();
            return null;
        }
    }

}
