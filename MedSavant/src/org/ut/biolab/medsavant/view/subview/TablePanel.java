/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import org.ut.biolab.medsavant.view.util.DialogUtil;
import fiume.table.SearchableTablePanel;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;

/**
 *
 * @author mfiume
 */
class TablePanel extends JPanel implements FiltersChangedListener {
    private final SearchableTablePanel tablePanel;

    public TablePanel() {
        tablePanel = new SearchableTablePanel(new Vector(), VariantRecordModel.getFieldNames(), VariantRecordModel.getFieldClasses());
        this.setLayout(new BorderLayout());
        this.add(tablePanel, BorderLayout.CENTER);
        try {
            updateTable();
        } catch (Exception ex) {
            Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
            DialogUtil.displayErrorMessage("Problem getting data.", ex);
        }
        FilterController.addFilterListener(this);
    }

    private void updateTable() throws SQLException, FatalDatabaseException {
        tablePanel.updateData(Util.convertVariantRecordsToVectors(ResultController.getInstance().getFilteredVariantRecords()));
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException {
        updateTable();
    }

}
