/*
 *    Copyright 2009-2010 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
