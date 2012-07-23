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
 * Enum which describes the recognised column data-types.
 *
 * @author tarkvara
 */
public enum ColumnType {
    VARCHAR,
    BOOLEAN,
    INTEGER,
    FLOAT,
    DECIMAL,
    DATE,
    TEXT;

    @Override
    public String toString() {
        switch (this) {
            case INTEGER:
                return "int";
            case FLOAT:
                return "float";
            case BOOLEAN:
                return "bool";
            case VARCHAR:
                return "varchar";
            case DECIMAL:
                return "decimal";
            case DATE:
                return "date";
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
        } else if (typeNameSQL.contains("tinyint")) {
            return ColumnType.INTEGER;
        } else if (typeNameSQL.contains("blob")) {
            return ColumnType.VARCHAR;
        } else if (typeNameSQL.contains("datetime")) {
            return ColumnType.DATE;
        } else if(typeNameSQL.contains("decimal")) {
            return ColumnType.DECIMAL;
        } else if (typeNameSQL.contains("date")) {
            return ColumnType.DATE;
        } else if (typeNameSQL.contains("text")) {
            return ColumnType.TEXT;
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
