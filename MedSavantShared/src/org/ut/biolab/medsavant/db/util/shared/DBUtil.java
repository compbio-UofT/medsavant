/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util.shared;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Andrew
 */
public class DBUtil {
    
    public static Timestamp getCurrentTimestamp(){
        return new Timestamp((new Date()).getTime());
    }
    
}
