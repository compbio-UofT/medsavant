/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.shared.util;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static int countRows(ResultSet rs) throws SQLException {
        int num = 0;
        while (rs.next()) {
            num++;
        }
        rs.beforeFirst();
        return num;
    }

    public static boolean resultHasMoreThanRows(ResultSet rs, int i) throws SQLException {
        int num = 0;
        while (rs.next()) {
            num++;
            if (num > i) { return false; }
        }
        rs.beforeFirst();
        return true;
    }

}
