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

package org.ut.biolab.medsavant.server.serverapi;

import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.ProjectTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.ServerLogTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantPendingUpdateTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.model.GeneralLog;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;


/**
 *
 * @author mfiume
 */
public class LogManager extends MedSavantServerUnicastRemoteObject implements LogManagerAdapter {

    private static final Log LOG = LogFactory.getLog(LogManager.class);
    private static final String SERVER_UNAME = "server";

    private static LogManager instance;

    private LogManager() throws RemoteException {
    }

    public static synchronized LogManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    @Override
    public List<GeneralLog> getClientLog(String sid, int start, int limit) throws SQLException {

        TableSchema table = MedSavantDatabase.ServerlogTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryCondition.notEqualTo(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), "server"));
        query.addOrdering(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString() + " LIMIT " + start + "," + limit);

        List<GeneralLog> result = new ArrayList<GeneralLog>();
        while(rs.next()) {
            result.add(new GeneralLog(
                    rs.getString(ServerLogTableSchema.COLUMNNAME_OF_USER),
                    rs.getString(ServerLogTableSchema.COLUMNNAME_OF_EVENT),
                    rs.getString(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION),
                    rs.getTimestamp(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP)));
        }
        return result;
    }

    @Override
    public List<GeneralLog> getServerLog(String sid, int start, int limit) throws SQLException {

        TableSchema table = MedSavantDatabase.ServerlogTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), "server"));
        query.addOrdering(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString() + " LIMIT " + start + "," + limit);

        List<GeneralLog> result = new ArrayList<GeneralLog>();
        while (rs.next()) {
            result.add(new GeneralLog(
                    rs.getString(ServerLogTableSchema.COLUMNNAME_OF_EVENT),
                    rs.getString(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION),
                    rs.getTimestamp(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP)));
        }
        return result;
    }

    @Override
    public List<AnnotationLog> getAnnotationLog(String sid, int start, int limit) throws SQLException {

        TableSchema projectTable = MedSavantDatabase.ProjectTableSchema;
        TableSchema referenceTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema updateTable = MedSavantDatabase.VariantpendingupdateTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(updateTable.getTable());
        query.addColumns(
                projectTable.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME),
                referenceTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_ACTION),
                updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS),
                updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_TIMESTAMP),
                updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_USER),
                updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID));
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER,
                updateTable.getTable(),
                projectTable.getTable(),
                BinaryConditionMS.equalTo(
                        updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_PROJECT_ID),
                        projectTable.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID)));
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER,
                updateTable.getTable(),
                referenceTable.getTable(),
                BinaryConditionMS.equalTo(
                        updateTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                        referenceTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString() + " LIMIT " + start + "," + limit);

        List<AnnotationLog> result = new ArrayList<AnnotationLog>();
        while (rs.next()) {

            Timestamp t = null;
            try {
                t = rs.getTimestamp(5);
            } catch (Exception e) {}

            result.add(new AnnotationLog(
                    rs.getString(1),
                    rs.getString(2),
                    AnnotationLog.intToAction(rs.getInt(3)),
                    AnnotationLog.intToStatus(rs.getInt(4)),
                    t,
                    rs.getString(6),
                    rs.getInt(7)));
        }
        return result;

    }

    @Override
    public int getAnnotationLogSize(String sid) throws SQLException {
        return getLogSize(sid,MedSavantDatabase.VariantpendingupdateTableSchema, null);
    }

    @Override
    public int getServerLogSize(String sid) throws SQLException {
        TableSchema table = MedSavantDatabase.ServerlogTableSchema;
        return getLogSize(sid,table, BinaryConditionMS.equalTo(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), "server"));
    }

    @Override
    public int getClientLogSize(String sid) throws SQLException {
        TableSchema table = MedSavantDatabase.ServerlogTableSchema;
        return getLogSize(sid,table, BinaryCondition.notEqualTo(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), "server"));
    }

    private static int getLogSize(String sid, TableSchema table, Condition c) throws SQLException {
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addCustomColumns(FunctionCall.countAll());
        if(c != null){
            query.addCondition(c);
        }

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void addServerLog(String sid, LogType t, String description) {
        addLog(sid,SERVER_UNAME, t, description);
    }

    @Override
    public void addLog(String sessID, String user, LogType type, String desc) {
        try {
            Timestamp sqlDate = new java.sql.Timestamp((new java.util.Date()).getTime());

            TableSchema table = MedSavantDatabase.ServerlogTableSchema;
            InsertQuery query = new InsertQuery(table.getTable());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), user);
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_EVENT), type.toString());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION), desc);
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), sqlDate);
            ConnectionController.executeUpdate(sessID, query.toString());

        } catch (SQLException ex) {
            LOG.error("Error writing to server log.", ex);
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
    }}
