/*
 *    Copyright 2011-2012 University of Toronto
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
/*
 *    Copyright 2011-2012 University of Toronto
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
package org.ut.biolab.medsavant.server.db.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

import com.healthmarketscience.sqlbuilder.SelectQuery;

/**
 * Helper for DBUtils.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class DBUtilsHelper {

    /**
     * Retrieves number of records in a table.
     * 
     * @param sessID
     *            session id
     * @param tablename
     *            table name
     * @return count
     */
    public int getNumRecordsInTable(String sessID, String tablename) throws SQLException, SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sessID, "SELECT COUNT(*) FROM `" + tablename + "`");
        rs.next();
        return rs.getInt(1);
    }

    /**
     * Retrieves extreme values for a given column.
     * 
     * @param sessID
     *            session ID
     * @param query
     *            query for extreme values
     * @return min and max values for the given column
     */
    public Range getExtremeValuesForColumn(String sessID, SelectQuery query) throws SQLException, SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());
        rs.next();

        double min = rs.getDouble(1);
        double max = rs.getDouble(2);

        return new Range(min, max);
    }

    /**
     * Drops table.
     * 
     * @param sessID
     *            session ID
     * @param table
     *            table to drop
     */
    public void dropTable(String sessID, String table) throws SQLException, SessionExpiredException {
        final String query = "DROP TABLE IF EXISTS " + table + ";";

        ConnectionController.executeUpdate(sessID, query);
    }
}
