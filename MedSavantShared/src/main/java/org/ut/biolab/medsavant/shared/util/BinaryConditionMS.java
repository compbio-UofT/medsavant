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
package org.ut.biolab.medsavant.shared.util;

import com.healthmarketscience.sqlbuilder.BinaryCondition;

/**
 *
 * Replace all single quotes in string conditions.
 *
 * @author Andrew
 */
public class BinaryConditionMS {

    public static BinaryCondition equalTo(Object value1, Object value2){
        if(value1 != null && value1.getClass().equals(String.class)){
            value1 = escapeChars((String) value1);
        }
        if(value2 != null && value2.getClass().equals(String.class)){
            value2 = escapeChars((String) value2);
        }
        return BinaryCondition.equalTo(value1, value2);
    }

    public static BinaryCondition like(Object value1, Object value2){
        if(value1 != null && value1.getClass().equals(String.class)){
            value1 = escapeChars((String) value1);
        }
        if(value2 != null && value2.getClass().equals(String.class)){
            value2 = escapeChars((String) value2);
        }
        return BinaryCondition.like(value1, value2);
    }

    public static BinaryCondition notlike(Object value1, Object value2){
        if(value1 != null && value1.getClass().equals(String.class)){
            value1 = escapeChars((String) value1);
        }
        if(value2 != null && value2.getClass().equals(String.class)){
            value2 = escapeChars((String) value2);
        }
        return BinaryCondition.notLike(value1, value2);
    }


    private static Object escapeChars(String value){
        return value.replaceAll("'", "''");
    }

}
