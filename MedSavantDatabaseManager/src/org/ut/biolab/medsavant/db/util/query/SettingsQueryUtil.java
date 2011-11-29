/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util.query;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.SettingsTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.ConnectionController;

/**
 *
 * @author Andrew
 */
public class SettingsQueryUtil {
    
    public static void addSetting(String key, String value) throws SQLException {
        
        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key);
        query.addColumn(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE), value);
        
        ConnectionController.connectPooled().createStatement().executeUpdate(query.toString());
    }
    
    public static String getSetting(String key) throws SQLException {
        
        TableSchema table = MedSavantDatabase.SettingsTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_VALUE));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(SettingsTableSchema.COLUMNNAME_OF_KEY), key));
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        if(rs.next()){
            return rs.getString(1);
        } else {
            return null;
        }
    }
    
}
