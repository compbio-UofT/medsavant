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
package org.ut.biolab.medsavant.shared.model;

import com.healthmarketscience.sqlbuilder.CustomCondition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.io.Serializable;

/**
 *
 * @author Andrew
 */
public class RangeCondition extends CustomCondition implements Serializable {

    public RangeCondition(DbColumn column, double min, double max){
        super(getString(column, min, max));
    }

    public RangeCondition(DbColumn column, long min, long max){
        super(getString(column, min, max));
    }

    private static String getString(DbColumn column, double min, double max) {
        if (min == max) {
            return column.getTable().getAlias() + "." + column.getColumnNameSQL() + " = " + min;
        }
        return column.getTable().getAlias() + "." + column.getColumnNameSQL() + " BETWEEN " + min + " AND " + max;
    }

    private static String getString(DbColumn column, long min, long max) {
        if (min == max) {
            return column.getTable().getAlias() + "." + column.getColumnNameSQL() + " = " + min;
        }
        return column.getTable().getAlias() + "." + column.getColumnNameSQL() + " BETWEEN " + min + " AND " + max;
    }
}

