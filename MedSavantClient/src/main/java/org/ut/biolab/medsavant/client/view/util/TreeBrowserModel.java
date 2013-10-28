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
package org.ut.biolab.medsavant.client.view.util;

import com.jidesoft.grid.CellStyle;
import com.jidesoft.grid.StyleModel;
import com.jidesoft.grid.TreeTableModel;

import java.awt.*;
import java.util.List;

public abstract class TreeBrowserModel extends TreeTableModel implements StyleModel {
    static final Color BACKGROUND = new Color(247, 247, 247);
    static final CellStyle CELL_STYLE = new CellStyle();

    static {
        CELL_STYLE.setBackground(BACKGROUND);
    }

    public TreeBrowserModel(List<TreeBrowserEntry> rows) {
        super(rows);
    }

    /**
     * Different browsers can override this to present a different list of columns to the user.
     */
    public abstract String[] getColumnNames();

    @Override
    public String getColumnName(int column) {
        return getColumnNames()[column];
    }

    @Override
    public int getColumnCount() {
        return getColumnNames().length;
    }

    @Override
    public CellStyle getCellStyleAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return CELL_STYLE;
        }
        else {
            return null;
        }
    }

    @Override
    public boolean isCellStyleOn() {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return TreeBrowserEntry.class;
            //case 1:
            //    return String.class;
            //case 2:
            //    return String.class;
            //case 3:
            //    return String.class;
            //case 4:
            //    return String.class;
        }
        return super.getColumnClass(columnIndex);
    }
}
