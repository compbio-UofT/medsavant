/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.component;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author tarkvara
 */
public class StripyTable extends JTable {

    public StripyTable(Object[][] data, String[] colNames) {
        super(data, colNames);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component comp = super.prepareRenderer(renderer, row, col);
        if (!isCellSelected(row, col)) {
            if (row % 2 == 0) {
                comp.setBackground(ViewUtil.evenRowColor);
            } else {
                comp.setBackground(ViewUtil.oddRowColor);
            }
        } else {
            comp.setBackground(ViewUtil.getMedSavantBlueColor());
        }
        return comp;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void disableSelection() {
        this.setSelectionModel(new NullSelectionModel());
    }
    
    private static class NullSelectionModel implements ListSelectionModel {

        public boolean isSelectionEmpty() {
            return true;
        }

        public boolean isSelectedIndex(int index) {
            return false;
        }

        public int getMinSelectionIndex() {
            return -1;
        }

        public int getMaxSelectionIndex() {
            return -1;
        }

        public int getLeadSelectionIndex() {
            return -1;
        }

        public int getAnchorSelectionIndex() {
            return -1;
        }

        public void setSelectionInterval(int index0, int index1) {
        }

        public void setLeadSelectionIndex(int index) {
        }

        public void setAnchorSelectionIndex(int index) {
        }

        public void addSelectionInterval(int index0, int index1) {
        }

        public void insertIndexInterval(int index, int length, boolean before) {
        }

        public void clearSelection() {
        }

        public void removeSelectionInterval(int index0, int index1) {
        }

        public void removeIndexInterval(int index0, int index1) {
        }

        public void setSelectionMode(int selectionMode) {
        }

        public int getSelectionMode() {
            return SINGLE_SELECTION;
        }

        public void addListSelectionListener(ListSelectionListener lsl) {
        }

        public void removeListSelectionListener(ListSelectionListener lsl) {
        }

        public void setValueIsAdjusting(boolean valueIsAdjusting) {
        }

        public boolean getValueIsAdjusting() {
            return false;
        }
    }
}
