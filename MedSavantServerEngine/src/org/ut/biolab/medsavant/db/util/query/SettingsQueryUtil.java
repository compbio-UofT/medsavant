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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.ut.biolab.medsavant.serverapi.SettingsQueryUtilAdapter;

/**
 *
 * @author Andrew
 */
public class SettingsQueryUtil extends MedSavantServerUnicastRemoteObject implements SettingsQueryUtilAdapter {

    private static SettingsQueryUtil instance;
    private static boolean lockReleased = false;

    public static synchronized SettingsQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new SettingsQueryUtil();
        }
        return instance;
    }

    public SettingsQueryUtil() throws RemoteException {super();}


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
            return rs.getString(1);
        } else {
            return null;
        }
    }

    @Override
    public void updateSetting(String sid, String key, String value) throws SQLException {

        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE), value);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key));

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    private void updateSetting(Connection c, String key, String value) throws SQLException {

        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE), value);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key));

        c.createStatement().executeUpdate(query.toString());
    }

    @Override
    public synchronized boolean getDbLock(String sid) throws SQLException {

        System.out.print(sid + " getting lock");

        String value = getSetting(sid, Settings.KEY_DB_LOCK);
        if (Boolean.parseBoolean(value) && !lockReleased) {
            System.out.println(" - FAILED");
            return false;
        }
        updateSetting(sid, Settings.KEY_DB_LOCK, Boolean.toString(true));
        System.out.println(" - SUCCESS");
        lockReleased = false;
        return true;
    }

    public synchronized void releaseDbLock(Connection c) {
        System.out.println("Server releasing lock");
        try {
            updateSetting(c, Settings.KEY_DB_LOCK, Boolean.toString(false));
        } catch (SQLException ex) {
            lockReleased = true;
            Logger.getLogger(SettingsQueryUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void releaseDbLock(String sid) throws SQLException {
        Connection c = ConnectionController.connectPooled(sid);
        releaseDbLock(c);
        c.close();
    }
}
