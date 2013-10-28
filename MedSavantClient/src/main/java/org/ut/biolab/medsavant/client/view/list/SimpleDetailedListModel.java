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
package org.ut.biolab.medsavant.client.view.list;


/**
 * We have a lot of DetailedListModels which consist of just a single string column.
 *
 * @author tarkvara
 */
public abstract class SimpleDetailedListModel<T> implements DetailedListModel {
    private final String[] columnNames;
    private final Class[] columnClasses = new Class[] { String.class };

    protected SimpleDetailedListModel(String colName) {
        columnNames = new String[] { colName };
    }

    @Override
    public Object[][] getList(int limit) throws Exception {
        T[] data = getData();
        Object[][] result = new Object[data.length][];
        for (int i = 0; i < data.length; i++) {
            result[i] = new Object[] { data[i] };
        }
        return result;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public Class[] getColumnClasses() {
        return columnClasses;
    }

    @Override
    public int[] getHiddenColumns() {
        return new int[0];
    }

    public abstract T[] getData() throws Exception;
}
