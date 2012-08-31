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

package org.ut.biolab.medsavant.db;

import java.io.Serializable;


/**
 * Small class which simplifies the process of defining a table schema's columns.
 * 
 * @author tarkvara
 */
public class ColumnDef implements Serializable {
    protected final String name;
    protected final ColumnType type;
    protected final int length;
    protected final String defaultValue;
    protected final boolean autoIncrement;
    protected final boolean nonNull;
    protected final boolean primaryKey;
    
    /**
     * Construct a generic not-null column.  No auto-increment or indexing.
     * @param n column name
     * @param t SQL column type
     * @param l column length
     */
    public ColumnDef(String n, ColumnType t, int l) {
        this(n, t, l, false, true, false, null);
    }
    
    public ColumnDef(String n, ColumnType t, int l, boolean autoInc, boolean notNull, boolean indexed, String dflt) {
        name = n;
        type = t;
        length = l;
        autoIncrement = autoInc;
        nonNull = notNull;
        primaryKey = indexed;
        defaultValue = dflt;
    }

    public int getColumnLength() {
        return length;
    }

    public String getColumnName() {
        return name;
    }

    public ColumnType getColumnType() {
        return type;
    }
}
