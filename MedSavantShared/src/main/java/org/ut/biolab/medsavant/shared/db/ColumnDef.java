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

package org.ut.biolab.medsavant.shared.db;

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
    protected final int scale;
    protected final String defaultValue;
    protected final boolean autoIncrement;
    protected final boolean nonNull;
    protected final boolean primaryKey;

    public ColumnDef(String n, ColumnType t, int length){
        this(n, t, new int[]{length, 0}, true);
    }
    
    /**
     * Construct a generic not-null column.  No auto-increment or indexing.
     * @param n column name
     * @param t SQL column type
     * @param l column length
     * @param p column precision -- used only for decimal types
     */
    public ColumnDef(String n, ColumnType t, int[] lengthAndScale, boolean notNull) {
        this(n, t, lengthAndScale[0], lengthAndScale[1], false, notNull, false, null);
    }

    public ColumnDef(String n, ColumnType t, int l, boolean autoInc, boolean notNull, boolean indexed, String dflt) {
        this(n, t, l, 0, autoInc, notNull, indexed, dflt);
    }
    
    public ColumnDef(String n, ColumnType t, int l, int s, boolean autoInc, boolean notNull, boolean indexed, String dflt) {
        name = n;
        type = t;
        length = l;
        scale = s;
        autoIncrement = autoInc;
        nonNull = notNull;
        primaryKey = indexed;
        defaultValue = dflt;
    }


    public int getColumnLength() {
        return length;
    }
    
    public int getColumnScale(){
        return scale;
    }

    public String getColumnName() {
        return name;
    }

    public ColumnType getColumnType() {
        return type;
    }
}
