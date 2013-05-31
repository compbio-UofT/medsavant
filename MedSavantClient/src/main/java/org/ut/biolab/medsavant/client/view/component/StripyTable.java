/*
 *    Copyright 2012 University of Toronto
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
