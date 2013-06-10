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
