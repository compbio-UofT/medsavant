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

package org.ut.biolab.medsavant.client.view.component;

import java.awt.Dimension;
import javax.swing.JTable;

import com.jidesoft.grid.TableColumnChooserDialog;
import com.jidesoft.grid.TableColumnChooserPopupMenuCustomizer;

import org.ut.biolab.medsavant.client.view.util.DialogUtils;


/**
 * Used by a <code>SearchableTablePanel</code> to select the columns to be displayed.
 *
 * @author tarkvara
 */
public class ColumnChooser extends TableColumnChooserPopupMenuCustomizer {
    private JTable table;

    public ColumnChooser(JTable t) {
        table = t;
    }

    public void hideColumns(int[] indices) {
        for (int i: indices) {
            hideColumn(table, i);
        }
    }

    public void showDialog() {
        TableColumnChooserDialog dialog = createTableColumnChooserDialog(DialogUtils.getFrontWindow(), "Choose fields to display", table);
        dialog.setPreferredSize(new Dimension(300,500));
        dialog.setSize(new Dimension(300,500));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
