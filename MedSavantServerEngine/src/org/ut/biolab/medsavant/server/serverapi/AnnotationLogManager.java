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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantPendingUpdateTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.model.AnnotationLog;
import org.ut.biolab.medsavant.model.AnnotationLog.Action;
import org.ut.biolab.medsavant.model.AnnotationLog.Status;
import org.ut.biolab.medsavant.server.db.connection.ConnectionController;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.SQLUtils;

/**
 *
 * @author Andrew
 */
public class AnnotationLogManager {

    private static AnnotationLogManager instance;

    public static synchronized AnnotationLogManager getInstance() {
        if (instance == null) {
            instance = new AnnotationLogManager();
        }
        return instance;
    }

    public int addAnnotationLogEntry(String sid,int projectId, int referenceId, Action action, String user) throws SQLException{
        return addAnnotationLogEntry(sid,projectId,referenceId,action,Status.STARTED, user);
    }

    public int addAnnotationLogEntry(String sid,int projectId, int referenceId, Action action, Status status, String user) throws SQLException {

        Timestamp sqlDate = SQLUtils.getCurrentTimestamp();

        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId);
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId);
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_ACTION), AnnotationLog.actionToInt(action));
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS), AnnotationLog.statusToInt(status));
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_TIMESTAMP), sqlDate);
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_USER), user);

        Connection c = ConnectionController.connectPooled(sid);
        PreparedStatement stmt = c.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
        stmt.execute();

        ResultSet rs = stmt.getGeneratedKeys();
        rs.next();
        c.close();
        return rs.getInt(1);
    }

    public void removeAnnotationLogEntry(String sid,int updateId) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        DeleteQuery query = new DeleteQuery(table.getTable());
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID), updateId));

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    public void setAnnotationLogStatus(String sid,int updateId, Status status) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS), AnnotationLog.statusToInt(status));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID), updateId));

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    public void setAnnotationLogStatus(String sid,int updateId, Status status, Timestamp sqlDate) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS), AnnotationLog.statusToInt(status));
        query.addSetClause(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_TIMESTAMP), sqlDate);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID), updateId));

        ConnectionController.executeUpdate(sid,  query.toString());
    }
}
