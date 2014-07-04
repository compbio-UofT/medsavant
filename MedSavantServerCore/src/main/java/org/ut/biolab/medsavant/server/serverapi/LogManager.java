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
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.server.Medapi.commonServerJob;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.ServerLogTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.model.GeneralLog;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;

/**
 *
 * @author mfiume
 */
public class LogManager extends MedSavantServerUnicastRemoteObject implements LogManagerAdapter {

    private static final Log LOG = LogFactory.getLog(LogManager.class);

    private static LogManager instance;

    private LogManager() throws RemoteException, SessionExpiredException {
    }

    public static synchronized LogManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    @Override
    public List<GeneralLog> getServerLog(String sid, int start, int limit) throws SQLException, SessionExpiredException {
        return getServerLogForUser(sid, null, start, limit);
    }

    @Override
    public int getServerLogSize(String sid) throws SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.ServerlogTableSchema;
        return getLogSize(sid, table, BinaryConditionMS.equalTo(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), "server"));
    }

    private static int getLogSize(String sid, TableSchema table, Condition c) throws SQLException, SessionExpiredException {
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addCustomColumns(FunctionCall.countAll());
        if (c != null) {
            query.addCondition(c);
        }

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());
        rs.next();
        return rs.getInt(1);
    }

    public void addServerLog(String sid, LogType t, String description) throws SessionExpiredException, RemoteException {
        addLog(sid, t, description);
    }

    public void addLog(String sessID, LogType type, String desc) throws SessionExpiredException, RemoteException {
        try {
            Timestamp sqlDate = new java.sql.Timestamp((new java.util.Date()).getTime());

            TableSchema table = MedSavantDatabase.ServerlogTableSchema;
            InsertQuery query = new InsertQuery(table.getTable());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), SessionManager.getInstance().getUserForSession(sessID));
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_EVENT), type.toString());
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION), desc);
            query.addColumn(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), sqlDate);
            ConnectionController.executeUpdate(sessID, query.toString());

        } catch (SQLException ex) {
            LOG.error("Error writing to server log.", ex);
        }
    }

    @Override
    public Date getDateOfLastServerLog(String sid) throws SQLException, SessionExpiredException {
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

    @Override
    public List<GeneralLog> getServerLogForUserWithSessionID(String sid, int start, int limit) throws SQLException, RemoteException, SessionExpiredException {
        return getServerLogForUser(sid, SessionManager.getInstance().getUserForSession(sid), start, limit);
    }

    private List<GeneralLog> getServerLogForUser(String sid, String userForSession, int start, int limit) throws SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.ServerlogTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        if (userForSession != null) {
            query.addCondition(BinaryCondition.equalTo(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_USER), userForSession));
        }
        query.addOrdering(table.getDBColumn(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString() + " LIMIT " + start + "," + limit);

        List<GeneralLog> result = new ArrayList<GeneralLog>();
        while (rs.next()) {
            result.add(new GeneralLog(
                    rs.getString(ServerLogTableSchema.COLUMNNAME_OF_USER),
                    rs.getString(ServerLogTableSchema.COLUMNNAME_OF_EVENT),
                    rs.getString(ServerLogTableSchema.COLUMNNAME_OF_DESCRIPTION),
                    rs.getTimestamp(ServerLogTableSchema.COLUMNNAME_OF_TIMESTAMP)));
        }
        return result;
    }

    @Override
    public List<JobProgressMonitor> getJobProgressForUserWithSessionID(String sid) throws SQLException, RemoteException, SessionExpiredException {
        String userId = SessionManager.getInstance().getUserForSession(sid);
        return MedSavantServerJob.getJobProgressesForUser(userId);
    }
}
