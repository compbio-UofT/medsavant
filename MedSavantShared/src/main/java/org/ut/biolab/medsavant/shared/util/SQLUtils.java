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
