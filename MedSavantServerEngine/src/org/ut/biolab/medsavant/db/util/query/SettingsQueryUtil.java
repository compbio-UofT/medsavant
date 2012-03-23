/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util.query;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.SettingsTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.settings.Settings;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.shared.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.db.util.query.api.SettingsQueryUtilAdapter;

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


    public void addSetting(String sid, String key, String value) throws SQLException {

        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key);
        query.addColumn(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE), value);

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    public String getSetting(String sid, String key) throws SQLException {

        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());
        if(rs.next()){
            return rs.getString(1);
        } else {
            return null;
        }
    }

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

    public synchronized boolean getDbLock(String sid) throws SQLException {

        System.out.print(sid + " getting lock");

        String value = getSetting(sid, Settings.KEY_DB_LOCK);
        if(Boolean.parseBoolean(value) && !lockReleased){
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

    public void releaseDbLock(String sid) throws SQLException {
        Connection c = ConnectionController.connectPooled(sid);
        releaseDbLock(c);
        c.close();
    }
}
