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

package org.ut.biolab.medsavant.db.util;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ServerLogTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;

/**
 * Logging handler which publishes log records to the database.
 *
 * @author tarkvara
 */
public class DBLoggingHandler extends Handler {
    private static final Logger LOG = Logger.getLogger(DBLoggingHandler.class.getName());
    private static String userName;
    private static String sessionId;

    @Override
    public void publish(LogRecord lr) {
        try {
            TableSchema table = MedSavantDatabase.ServerlogTableSchema;
            InsertQuery query = new InsertQuery(table.getTable());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), userName);
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_EVENT), lr.getLevel());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION), lr.getMessage());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), new Timestamp(new Date().getTime()));
            ConnectionController.executePreparedUpdate(sessionId, query.toString());
        } catch (SQLException x) {
            LOG.log(Level.SEVERE, null, x);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    public static void setUserName(String name) {
        userName = name;
    }

    public static void setSessionId(String id) {
        sessionId = id;
    }
}
