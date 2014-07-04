/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
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
import java.rmi.RemoteException;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantPendingUpdateTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.model.AnnotationLog.Action;
import org.ut.biolab.medsavant.shared.model.AnnotationLog.Status;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.SQLUtils;

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

    public int addAnnotationLogEntry(String sid, int projectId, int referenceId, Action action) throws SQLException, RemoteException, SessionExpiredException {
        return addAnnotationLogEntry(sid, projectId, referenceId, action, Status.STARTED);
    }

    public int addAnnotationLogEntry(String sid, int projectId, int referenceId, Action action, Status status) throws SQLException, RemoteException, SessionExpiredException {

        String user = SessionManager.getInstance().getUserForSession(sid);

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

    public void removeAnnotationLogEntry(String sid, int updateId) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        DeleteQuery query = new DeleteQuery(table.getTable());
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID), updateId));

        ConnectionController.executeUpdate(sid, query.toString());
    }

    public void setAnnotationLogStatus(String sid, int updateId, Status status) throws SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS), AnnotationLog.statusToInt(status));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID), updateId));

        ConnectionController.executeUpdate(sid, query.toString());
    }

    public void setAnnotationLogStatus(String sid, int updateId, Status status, Timestamp sqlDate) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS), AnnotationLog.statusToInt(status));
        query.addSetClause(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_TIMESTAMP), sqlDate);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID), updateId));

        ConnectionController.executeUpdate(sid, query.toString());
    }
}
