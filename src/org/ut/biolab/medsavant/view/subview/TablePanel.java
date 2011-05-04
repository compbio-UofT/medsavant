/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import fiume.table.SearchableTablePanel;
import java.awt.BorderLayout;
import java.util.Vector;
import javax.swing.JPanel;
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
        updateTable();
        FilterController.addFilterListener(this);
    }

    private void updateTable() {
        tablePanel.updateData(Util.getVariantRecordsVector(ResultController.getFilteredVariantRecords()));
    }

    public void filtersChanged() {
        updateTable();
    }

}
