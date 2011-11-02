/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.model;

import com.healthmarketscience.sqlbuilder.CustomCondition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

/**
 *
 * @author Andrew
 */
public class RangeCondition extends CustomCondition {
    
    public RangeCondition(DbColumn column, double min, double max){
        super(getString(column, min, max));
    }
    
    public RangeCondition(DbColumn column, long min, long max){
        super(getString(column, min, max));
    }
    
    private static String getString(DbColumn column, Object min, Object max){
        return column.getTable().getAlias() + ".`" + column.getColumnNameSQL() + "` BETWEEN " + min + " AND " + max;
    }
    
}

