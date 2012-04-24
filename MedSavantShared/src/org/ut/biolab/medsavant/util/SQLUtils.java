/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.util;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Andrew
 */
public class SQLUtils {
    
    public static Timestamp getCurrentTimestamp(){
        return new Timestamp((new Date()).getTime());
    }
    
}
