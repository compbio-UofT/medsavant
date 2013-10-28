/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.component;

import java.awt.Component;
import javax.swing.JTable;
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
        }
        return comp;
    }
}
