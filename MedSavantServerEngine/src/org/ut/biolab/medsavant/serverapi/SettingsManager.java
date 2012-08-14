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
package org.ut.biolab.medsavant.serverapi;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.SettingsTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.db.Settings;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;


/**
 *
 * @author Andrew
 */
public class SettingsManager extends MedSavantServerUnicastRemoteObject implements SettingsManagerAdapter {
    private static SettingsManager instance;
    private boolean lockReleased = false;

    private SettingsManager() throws RemoteException {
    }

    public static synchronized SettingsManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    @Override
    public void addSetting(String sid, String key, String value) throws SQLException {

        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key);
        query.addColumn(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE), value);

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    @Override
    public String getSetting(String sid, String key) throws SQLException {

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
    public void updateSetting(String sessID, String key, String value) throws SQLException {
        Connection conn = ConnectionController.connectPooled(sessID);
        try {
            updateSetting(conn, key, value);
        } finally {
            conn.close();
        }
    }

    private void updateSetting(Connection conn, String key, String value) throws SQLException {

        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE), value);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key));

        conn.createStatement().executeUpdate(query.toString());
    }

    @Override
    public synchronized boolean getDBLock(String sessID) throws SQLException {

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

    public synchronized void releaseDBLock(Connection conn) throws SQLException {
        System.out.println("Server releasing lock");
        try {
            updateSetting(conn, Settings.KEY_DB_LOCK, Boolean.toString(false));
        } finally {
            lockReleased = true;
        }
    }

    @Override
    public void releaseDBLock(String sessID) throws SQLException {
        Connection conn = ConnectionController.connectPooled(sessID);
        if (conn != null) {
            try {
                releaseDBLock(conn);
            } finally {
                conn.close();
            }
        }
    }
}
