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

/**
 * Enum which describes the recognised column data-types.
 *
 * @author tarkvara
 */
public enum ColumnType {
    VARCHAR,
    BLOB,
    BOOLEAN,
    INTEGER,
    FLOAT,
    DECIMAL,
    DATE,   
    TEXT,
    DATETIME,
    TIMESTAMP;

    @Override
    public String toString() {
        switch (this) {
            case INTEGER:
                return "int";
            case BLOB:
                return "blob";
            case FLOAT:
                return "float";
            case BOOLEAN:
                return "tinyint";
            case VARCHAR:
                return "varchar";
            case DECIMAL:
                return "decimal";
            case DATE:
                return "date";
            case DATETIME:
                return "datetime";
            case TIMESTAMP:
                return "timestamp";
            case TEXT:
                return "text";
            default:
                throw new IllegalArgumentException("Invalid column type.");
        }
    }

    public static ColumnType fromString(String typeNameSQL) {
        typeNameSQL = typeNameSQL.toLowerCase();
        if (typeNameSQL.contains("float")) {
            return ColumnType.FLOAT;
        } else if (typeNameSQL.contains("boolean") || (typeNameSQL.contains("int") && typeNameSQL.contains("(1)"))) {
            return ColumnType.BOOLEAN;
        } else if (typeNameSQL.contains("int")) {
            return ColumnType.INTEGER;
        } else if (typeNameSQL.contains("varchar")) {
            return ColumnType.VARCHAR;
        } else if (typeNameSQL.contains("blob")) {
            return ColumnType.BLOB;
        } else if (typeNameSQL.contains("tinyint")) {
            return ColumnType.INTEGER;
        } else if (typeNameSQL.contains("datetime")) {
            return ColumnType.DATE;
        } else if(typeNameSQL.contains("decimal")) {
            return ColumnType.DECIMAL;
        } else if (typeNameSQL.contains("date")) {
            return ColumnType.DATE;
        } else if (typeNameSQL.contains("text")) {
            return ColumnType.TEXT;
        }else if(typeNameSQL.contains("datetime")){
            return ColumnType.DATETIME;
        }else if(typeNameSQL.contains("timestamp")){
            return ColumnType.TIMESTAMP;
        }
        throw new IllegalArgumentException("Unable to parse \"" + typeNameSQL + "\" as column type.");
    }


    public boolean isNumeric() {
        return this == INTEGER || this == FLOAT || this == DECIMAL;
    }

    public boolean isInt() {
        return this == INTEGER;
    }

    public boolean isFloat() {
        return this == FLOAT || this == DECIMAL;
    }

    public boolean isBoolean() {
        return this == BOOLEAN;
    }
}
