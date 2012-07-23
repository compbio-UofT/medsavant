/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.view.component;

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
