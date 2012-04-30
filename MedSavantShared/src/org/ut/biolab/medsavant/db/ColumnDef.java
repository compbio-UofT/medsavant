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


/**
 * Small class which simplifies the process of defining a table schema's columns.
 * 
 * @author tarkvara
 */
public class ColumnDef {
    final String name;
    final ColumnType type;
    final int length;
    final String defaultValue;
    final boolean notNull;
    final boolean primaryKey;
    
    
    public ColumnDef(String n, ColumnType t, int l) {
        this(n, t, l, null, false, false);
    }
    
    public ColumnDef(String n, ColumnType t, int l, String dflt, boolean notNull, boolean indexed) {
        name = n;
        type = t;
        length = l;
        defaultValue = dflt;
        this.notNull = notNull;
        primaryKey = indexed;
    }

    public static class Integer extends ColumnDef {
        public Integer(String n) {
            super(n, ColumnType.INTEGER, 11);
        }
    }
}
