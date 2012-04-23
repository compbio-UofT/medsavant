/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db;

/**
 * Enum which describes the recognised column data-types.
 */
/**
 *
 * @author zig
 */
public enum ColumnType {
    VARCHAR, BOOLEAN, INTEGER, FLOAT, DECIMAL, DATE;

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
            default:
                return "date";
        }
    }
    
}
