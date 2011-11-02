/*
 *    Copyright 2011 University of Toronto
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

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;

import com.healthmarketscience.sqlbuilder.InsertQuery;

import org.ut.biolab.medsavant.db.log.DBLogger;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ServerLogTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.ConnectionController;

/**
 *
 * @author mfiume
 */
public class ServerLogQueryUtil {

    public static final String SERVER_UNAME = "server";
    public enum LogType { INFO, ERROR, LOGIN, LOGOUT };
    
    public static void addServerLog(LogType t, String description) {
        addLog(SERVER_UNAME, t, description);
    }
    
    public static void addLog(String uname, LogType t, String description) {
        try {
            Timestamp sqlDate = new java.sql.Timestamp((new Date()).getTime());
            
            TableSchema table = MedSavantDatabase.ServerlogTableSchema;
            InsertQuery query = new InsertQuery(table.getTable());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), uname);
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_EVENT), t.toString());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION), description);
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), sqlDate);
            ConnectionController.connectPooled().createStatement().execute(query.toString());
            
        } catch (SQLException ex) {
            DBLogger.log(ex.getLocalizedMessage(), Level.SEVERE);
        }
    }
}
