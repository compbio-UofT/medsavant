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

package org.ut.biolab.medsavant.shared.format;

import java.io.Serializable;
import java.sql.Date;

import org.ut.biolab.medsavant.shared.db.ColumnDef;
import org.ut.biolab.medsavant.shared.db.ColumnType;


/**
 *
 * @author Andrew
 */
public class CustomField extends ColumnDef implements Serializable {

    private boolean filterable;
    private String alias;
    private String description;

    /**
     * Construct a new custom field definition.  Extends the basic <code>ColumnDef</code> class by adding a couple of human-friendly
     * presentation fields.
     *
     * @param name column name
     * @param type SQL type string (i.e. "VARCHAR(100)")
     * @param filterable true if it should show up in our filters
     * @param alias human-friendly name
     * @param description human-friendly description (not currently used)
     */
    public CustomField(String name, String typeStr, boolean filterable, String alias, String description) {
        super(name, ColumnType.fromString(typeStr), extractColumnLength(typeStr));
        this.filterable = filterable;
        this.alias = alias;
        this.description = description;
    }

    public CustomField(String n, ColumnType t, int l, boolean autoInc, boolean notNull, boolean indexed, String dflt, boolean filterable, String alias, String description) {
        super(n, t, l, autoInc, notNull, indexed, dflt);
        this.filterable = filterable;
        this.alias = alias;
        this.description = description;
    }

    public CustomField() {
        super();
    }

    public String getAlias() {
        return alias;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public Class getColumnClass(){
        switch (type){
            case BOOLEAN:
                return Boolean.class;
            case INTEGER:
                return Integer.class;
            case DATE:
                return Date.class;
            case DECIMAL:
                return Double.class;
            case FLOAT:
                return Float.class;
            case VARCHAR:
            default:
                return String.class;
        }
    }

    public String generateSchema(){
        return generateSchema(false);
    }

    public String generateSchema(boolean forceLowerCase){
        return "`" + (forceLowerCase ? name.toLowerCase() : name) + "` " + getTypeString() + " DEFAULT NULL,";
    }

    /**
     * Get the SQL string describing this type.  For now, this ignores the auto-increment, non-null, primary-key, and default fields.
     * Also, since we don't have any ColumnDefs which specify a precision, we don't handle FLOATs and DECIMALs quite right.
     * @return
     */
    public String getTypeString() {
        switch (type) {
            case VARCHAR:
                 return "varchar(" + length + ")";
            case BOOLEAN:
                return "int(1)";
            case INTEGER:
                return "int(" + length + ")";
            case FLOAT:
                // TODO: Honour the length field, and supply a precision.
                return "float";
            case DECIMAL:
                // TODO: Honour the length field, and supply a precision.
                return "decimal";
        }
        return type.toString();
    }

    public boolean isNumeric() {
        return type.isNumeric();
    }

    private static int extractColumnLength(String typeStr) {
        int posLeft = typeStr.indexOf('(');
        if (posLeft == -1) {
            return 0;
        }
        typeStr = typeStr.substring(posLeft + 1);
        int posRight = typeStr.indexOf(')');
        typeStr = typeStr.substring(0, posRight);
        posRight = typeStr.indexOf(',');
        if (posRight >= 0) {
            typeStr = typeStr.substring(0, posRight);
        }
        return Integer.parseInt(typeStr);
    }


    @Override
    public String toString() {
        return alias;
    }

    @Override
    public boolean equals(Object o){
        if (o == null || !o.getClass().equals(CustomField.class)) {
            return false;
        }
        CustomField other = (CustomField)o;
        return name.equals(other.name) && type == other.type && length == other.length;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (name != null ? name.hashCode() : 0);
        hash = 89 * hash + (description != null ? description.hashCode() : 0);
        return hash;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
