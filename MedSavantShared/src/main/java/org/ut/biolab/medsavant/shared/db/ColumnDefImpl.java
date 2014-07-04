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
package org.ut.biolab.medsavant.shared.db;

import java.io.Serializable;
import org.medsavant.api.common.storage.ColumnDef;
import org.medsavant.api.common.storage.ColumnType;


/**
 * Small class which simplifies the process of defining a table schema's columns.
 *
 * @author tarkvara
 */
public class ColumnDefImpl implements ColumnDef {
    protected final String name;
    protected final ColumnType type;
    protected final int length;
    protected final int scale;
    protected final String defaultValue;
    protected final boolean autoIncrement;
    protected final boolean nonNull;
    protected final boolean primaryKey;

    public ColumnDefImpl(String n, ColumnType t, int length){
        this(n, t, new int[]{length, 0}, true);
    }
    
    /**
     * Construct a generic not-null column.  No auto-increment or indexing.
     * @param n column name
     * @param t SQL column type
     * @param l column length
     * @param p column precision -- used only for decimal types
     */
    public ColumnDefImpl(String n, ColumnType t, int[] lengthAndScale, boolean notNull) {
        this(n, t, lengthAndScale[0], lengthAndScale[1], false, notNull, false, null);
    }

    public ColumnDefImpl(String n, ColumnType t, int l, boolean autoInc, boolean notNull, boolean indexed, String dflt) {
        this(n, t, l, 0, autoInc, notNull, indexed, dflt);
    }
    
    public ColumnDefImpl(String n, ColumnType t, int l, int s, boolean autoInc, boolean notNull, boolean indexed, String dflt) {
        name = n;
        type = t;
        length = l;
        scale = s;
        autoIncrement = autoInc;
        nonNull = notNull;
        primaryKey = indexed;
        defaultValue = dflt;
    }


    @Override
    public int getColumnLength() {
        return length;
    }
    
    @Override
    public int getColumnScale(){
        return scale;
    }

    @Override
    public String getColumnName() {
        return name;
    }

    @Override
    public ColumnType getColumnType() {
        return type;
    }
}
