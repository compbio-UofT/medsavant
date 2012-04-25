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

package org.ut.biolab.medsavant.db.util.query;

import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.ServerLogTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.logging.DBLogger;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.serverapi.ServerLogQueryUtilAdapter;

/**
 * @author mfiume
 */
public class ServerLogQueryUtil extends MedSavantServerUnicastRemoteObject implements ServerLogQueryUtilAdapter {

   private static ServerLogQueryUtil instance;

    public static synchronized ServerLogQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new ServerLogQueryUtil();
        }
        return instance;
    }

    public ServerLogQueryUtil() throws RemoteException { super(); }


    public final String SERVER_UNAME = "server";

    @Override
    public void addServerLog(String sid, LogType t, String description) {
        addLog(sid,SERVER_UNAME, t, description);
    }

    @Override
    public void addLog(String sid, String uname, LogType t, String description) {
        try {
            Timestamp sqlDate = new java.sql.Timestamp((new java.util.Date()).getTime());

            TableSchema table = MedSavantDatabase.ServerlogTableSchema;
            InsertQuery query = new InsertQuery(table.getTable());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), uname);
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_EVENT), t.toString());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION), description);
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), sqlDate);
            ConnectionController.execute(sid, query.toString());

        } catch (SQLException ex) {
            DBLogger.log(ex.getLocalizedMessage(), Level.SEVERE);
        }
    }

    @Override
    public Date getDateOfLastServerLog(String sid) throws SQLException {
        TableSchema table = MedSavantDatabase.ServerlogTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());

        query.addColumns(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), "server"));
        //query.addCustomOrderings(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP));
        query.addOrdering(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString() + " LIMIT 1");

        if (rs.next()) {
            Date d = new Date(rs.getTimestamp(1).getTime());
            return d;
        } else {
            return null;
        }
    }
}
