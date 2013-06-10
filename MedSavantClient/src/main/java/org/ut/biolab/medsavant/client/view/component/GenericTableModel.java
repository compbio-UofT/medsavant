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

import java.util.List;

import com.jidesoft.grid.DefaultContextSensitiveTableModel;


/**
 *
 * @author mfiume
 */
public class GenericTableModel extends DefaultContextSensitiveTableModel {

    Class[] columnClasses;

    public GenericTableModel(Object[][] data, String[] columnNames, Class[] columnClasses) {
        super(data, columnNames);
        this.columnClasses = columnClasses;
    }

    public GenericTableModel(Object[][] data, String[] columnNames) {
        this(data, columnNames, null);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses == null || columnClasses.length == 0 ? String.class : columnClasses[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}