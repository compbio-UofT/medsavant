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
package org.ut.biolab.medsavant.server.serverapi;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.SettingsTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.db.Settings;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.LockController;
import org.ut.biolab.medsavant.shared.model.exception.LockException;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.exception.UnauthorizedException;
import org.ut.biolab.medsavant.shared.serverapi.SettingsManagerAdapter;
import org.ut.biolab.medsavant.shared.util.VersionSettings;


/**
 *
 * @author Andrew
 */
public class SettingsManager extends MedSavantServerUnicastRemoteObject implements SettingsManagerAdapter {
    
    private static final Log LOG = LogFactory.getLog(SettingsManager.class);
    
    private static SettingsManager instance;
    private boolean lockReleased = false;

    private SettingsManager() throws RemoteException, SessionExpiredException {
    }

    public static synchronized SettingsManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    @Override
    public void removeSetting(String sid, String key) throws SQLException, SessionExpiredException{
        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        DeleteQuery query = new DeleteQuery(table.getTable());
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key));        
        ConnectionController.executeUpdate(sid,  query.toString());
    }
    
    @Override
    public void addSetting(String sid, String key, String value) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key);
        query.addColumn(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE), value);

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    //Method for that enables UserManager to fetch valid users (keys in the settings table with 
    //prefix UserManager.DATABASE_USER_KEY_PREFIX). This should not be used for any other purpose as it
    //may be deprecated in the near future.
    Map<String, String> getSettingsForKeyPrefix(String sid, String keyPrefix) throws SQLException, SessionExpiredException{
        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY));

        query.addColumns(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE));
        query.addCondition(BinaryConditionMS.like(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), keyPrefix+"%"));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        Map<String, String> results = new HashMap<String, String>();
        
        while(rs.next()) {
            String user = rs.getString(1);
            user=user.replace(keyPrefix, "");
            String val = rs.getString(2);            
            results.put(user, val);
            
        }   
        return results;
    }
    
    @Override
    public String getSetting(String sid, String key) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        if (rs.next()) {
            String result = rs.getString(1);
            return result;
        } else {
            return null;
        }
    }

    @Override
    public void updateSetting(String sessID, String key, String value) throws SQLException, SessionExpiredException {
        Connection conn = ConnectionController.connectPooled(sessID);
        try {
            updateSetting(conn, key, value);
        } finally {
            conn.close();
        }
    }

    private void updateSetting(Connection conn, String key, String value) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE), value);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key));

        conn.createStatement().executeUpdate(query.toString());
    }

    /*
    @Override
    public synchronized boolean getDBLock(String sessID) throws SQLException, SessionExpiredException {

        System.out.print(sessID + " getting lock");

        String value = getSetting(sessID, Settings.KEY_DB_LOCK);
        if (Boolean.parseBoolean(value) && !lockReleased) {
            System.out.println(" - FAILED");
            return false;
        }
        updateSetting(sessID, Settings.KEY_DB_LOCK, Boolean.toString(true));
        System.out.println(" - SUCCESS");
        lockReleased = false;
        return true;
    }

    public synchronized void releaseDBLock(Connection conn) throws SQLException, SessionExpiredException {
        LOG.info("Server releasing lock");
        try {
            updateSetting(conn, Settings.KEY_DB_LOCK, Boolean.toString(false));
        } finally {
            lockReleased = true;
        }
    }
    *

    @Override
    public void releaseDBLock(String sessID) throws SQLException, SessionExpiredException {
        Connection conn = ConnectionController.connectPooled(sessID);
        if (conn != null) {
            try {
                releaseDBLock(conn);
            } finally {
                conn.close();
            }
        }
    }
   */


    @Override
    public String getServerVersion() throws RemoteException, SessionExpiredException {
        return VersionSettings.getVersionString();
    }

    public String getServerVersionWhenDatabaseCreated(String sessID) throws SQLException, SessionExpiredException {
       return getSetting(sessID,Settings.KEY_SERVER_VERSION);
    }
    
    @Override
    public boolean isProjectLockedForChanges(String sessionID, int projectID) throws RemoteException, SessionExpiredException {
        return LockController.getInstance().isLocked(SessionManager.getInstance().getDatabaseForSession(sessionID), projectID);
    }
    
    @Override
    public void forceReleaseLockForProject(String sessionID, int projectID) throws LockException, RemoteException, SessionExpiredException, UnauthorizedException, SQLException {
        if (UserManager.getInstance().isAdmin(sessionID,true)) {
            LockController.getInstance().releaseLock(SessionManager.getInstance().getDatabaseForSession(sessionID),projectID, true);
        }
    }
    
}
