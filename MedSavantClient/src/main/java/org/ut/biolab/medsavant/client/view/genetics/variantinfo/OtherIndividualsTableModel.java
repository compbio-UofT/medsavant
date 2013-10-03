/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import com.jidesoft.grid.HierarchicalTableModel;
import java.util.Set;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jim
 */
 class OtherIndividualsTableModel extends AbstractTableModel implements HierarchicalTableModel {

        private String[] dnaIDs;

        public OtherIndividualsTableModel() {
        }

        public void setValues(Set<String> dnaIDs) {
            this.dnaIDs = new String[dnaIDs.size()];
            this.dnaIDs = dnaIDs.toArray(this.dnaIDs);            
            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int col) {
            return "DNA ID";
        }

        @Override
        public int getRowCount() {
            return (dnaIDs == null) ? 0 : dnaIDs.length;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return (dnaIDs == null) ? null : dnaIDs[rowIndex];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public boolean hasChild(int row) {
            return true;
        }

        @Override
        public boolean isExpandable(int row) {
            return true;
        }

        @Override
        public boolean isHierarchical(int row) {
            return true;
        }

        @Override
        public Object getChildValueAt(int row) {
            return (dnaIDs == null) ? null : dnaIDs[row];
        }
    }

