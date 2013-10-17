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
import java.util.regex.Pattern;

import org.ut.biolab.medsavant.shared.db.ColumnDef;
import org.ut.biolab.medsavant.shared.db.ColumnType;

/**
 *
 * @author Andrew
 */
public class CustomField extends ColumnDef implements Serializable {

    private final boolean filterable;
    private final String alias;
    private final String description;
   
    /**
     * Construct a new custom field definition. Extends the basic
     * <code>ColumnDef</code> class by adding a couple of human-friendly
     * presentation fields.
     *
     * @param name column name
     * @param type SQL type string (i.e. "VARCHAR(100)")
     * @param filterable true if it should show up in our filters
     * @param alias human-friendly name
     * @param description human-friendly description (not currently used)
     */
    public CustomField(String name, String typeStr, boolean filterable, String alias, String description) {
        this(name, typeStr, filterable, alias, description, false);        
    }
    
    public CustomField(String name, String typeStr, boolean filterable, String alias, String description, boolean nonNull) {
        super(name, ColumnType.fromString(typeStr), extractColumnLengthAndScale(typeStr), nonNull);
        this.filterable = filterable;
        this.alias = alias;
        this.description = description;                
    }
    

    public CustomField(String n, ColumnType t, int l, boolean autoInc, boolean notNull, boolean indexed, String dflt, boolean filterable, String alias, String description) {
        super(n, t, l, 0, autoInc, notNull, indexed, dflt);
        this.filterable = filterable;
        this.alias = alias;
        this.description = description;
    }

    public static int[] extractColumnLengthAndScale(String typeStr) {
        int posLeft = typeStr.indexOf('(');
        if (posLeft == -1) {
            return new int[]{0,0};
        }
        int posRight = typeStr.indexOf(')');
        if (posRight == -1) {
            return new int[]{0,0};
        }
        String[] parts = typeStr.substring(posLeft + 1, posRight).split(",");
        if (parts.length == 1) {
            return new int[]{Integer.parseInt(parts[0].trim()), 0};
        } else if (parts.length == 2) {
            return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
        }
        return new int[]{0,0};
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

    public Class getColumnClass() {
        switch (type) {
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

    public String generateSchema() {
        return generateSchema(false);
    }

    public String generateSchema(boolean forceLowerCase) {
        return "`" + (forceLowerCase ? name.toLowerCase() : name) + "` " + getTypeString() + " DEFAULT NULL,";
    }

    /**
     * Get the SQL string describing this type. For now, this ignores the
     * auto-increment, non-null, primary-key, and default fields. 
     *
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
                return (length>0) ? ("decimal("+length+", "+scale+")") : "decimal";            
        }        
        return type.toString();
    }

    public boolean isNumeric() {
        return type.isNumeric();
    }

    @Override
    public String toString() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !o.getClass().equals(CustomField.class)) {
            return false;
        }
        CustomField other = (CustomField) o;
        return name.equals(other.name) && type == other.type && length == other.length;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (name != null ? name.hashCode() : 0);
        hash = 89 * hash + (description != null ? description.hashCode() : 0);
        return hash;
    }
}
