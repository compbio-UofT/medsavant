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

import java.awt.Color;

import com.jidesoft.grid.CellStyle;
import com.jidesoft.grid.SortableTreeTableModel;
import com.jidesoft.grid.StyleModel;
import com.jidesoft.grid.TreeTableModel;


/**
 * Wraps an existing TreeTableModel object, and gives it stripes.
 *
 * @author abrook
 */
public class StripySortableTreeTableModel  extends SortableTreeTableModel implements StyleModel {

    private static final Color BACKGROUND1 = Color.WHITE;
    private static final Color BACKGROUND2 = new Color(242, 245, 249);

    public StripySortableTreeTableModel(TreeTableModel model) {
        super(model);
    }

    CellStyle cellStyle = new CellStyle();

    @Override
    public CellStyle getCellStyleAt(int rowIndex, int columnIndex) {
        cellStyle.setHorizontalAlignment(-1);
        cellStyle.setForeground(Color.BLACK);
        if (rowIndex % 2 == 0) {
            cellStyle.setBackground(BACKGROUND1);
        } else {
            cellStyle.setBackground(BACKGROUND2);
        }
        return cellStyle;
    }

    @Override
    public boolean isCellStyleOn() {
        return true;
    }
}
