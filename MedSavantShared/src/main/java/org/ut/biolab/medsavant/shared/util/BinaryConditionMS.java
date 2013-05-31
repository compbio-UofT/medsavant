/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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


    private static Object escapeChars(String value){
        return value.replaceAll("'", "''");
    }

}
