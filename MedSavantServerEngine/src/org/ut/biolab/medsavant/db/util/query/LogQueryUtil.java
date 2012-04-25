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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.ProjectTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.ServerLogTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.VariantPendingUpdateTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.model.AnnotationLog;
import org.ut.biolab.medsavant.model.GeneralLog;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.serverapi.LogQueryUtilAdapter;

/**
 *
 * @author mfiume
 */
public class LogQueryUtil extends MedSavantServerUnicastRemoteObject implements LogQueryUtilAdapter {

    private static LogQueryUtil instance;

    public static synchronized LogQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new LogQueryUtil();
        }
        return instance;
    }

    public LogQueryUtil() throws RemoteException {super();}


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
                    rs.getString(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_USER)),
                    rs.getString(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_EVENT)),
                    rs.getString(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION)),
                    rs.getTimestamp(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP))));
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
        while(rs.next()) {
            result.add(new GeneralLog(
                    rs.getString(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_EVENT)),
                    rs.getString(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION)),
                    rs.getTimestamp(table.getFieldAlias(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP))));
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
}
