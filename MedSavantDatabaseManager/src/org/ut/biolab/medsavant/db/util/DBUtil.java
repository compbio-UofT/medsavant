package org.ut.biolab.medsavant.db.util;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;

/**
 *
 * @author mfiume
 */
public class DBUtil {

    /*public static enum FieldType {VARCHAR, FLOAT, INT, BOOLEAN, DECIMAL, DATE, TIMESTAMP}
    
    public static FieldType getFieldType(String type){
        String typeLower = type.toLowerCase();
        if (typeLower.contains("float")){
            return FieldType.FLOAT;
        } else if (typeLower.contains("decimal")){
            return FieldType.DECIMAL;
        } else if (typeLower.contains("int")){
            if(typeLower.contains("(1)")){
                return FieldType.BOOLEAN;
            } else {
                return FieldType.INT;
            }
        } else if (typeLower.contains("boolean")){
            return FieldType.BOOLEAN;
        } else if (typeLower.contains("date")){
            return FieldType.DATE;
        } else if (typeLower.contains("timestamp")){
            return FieldType.TIMESTAMP;
        } else {
            return FieldType.VARCHAR;
        }
    }*/
    
    public static String getColumnTypeString(String s) {
        int pos = s.indexOf("(");
        if (pos == -1) { return s; }
        else { return s.substring(0,pos); }
    }
    
    public static int getColumnLength(String s) {
        
        int fpos = s.indexOf("(");
        int rpos = s.indexOf(")");
        int cpos = s.indexOf(",");
        if(cpos != -1 && cpos < rpos){
            rpos = cpos;
        }
        
        if (fpos == -1) { return -1; }
        else { 
            return Integer.parseInt(s.substring(fpos+1,rpos)); 
        }
    }
    
    public static DbTable importTable(String tablename) throws SQLException {
        
        Connection c;
        try {
            c = ConnectionController.connectPooled();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        
        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();
        
        DbTable table = schema.addTable(tablename);
        
        Statement s = c.createStatement ();
        ResultSet rs = s.executeQuery("DESCRIBE " + tablename);

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberOfColumns = rsMetaData.getColumnCount();

        while (rs.next()) {
            table.addColumn(rs.getString(1), getColumnTypeString(rs.getString(2)), getColumnLength(rs.getString(2)));
        }

        return table;
    }
    
    public static TableSchema importTableSchema(String tablename) throws SQLException {
        
        Connection c = ConnectionController.connectPooled();
        
        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();
        
        DbTable table = schema.addTable(tablename);
        TableSchema ts = new TableSchema(table);

        Statement s = c.createStatement ();
        ResultSet rs = s.executeQuery("DESCRIBE " + tablename);

        while (rs.next()) {
            table.addColumn(rs.getString(1), getColumnTypeString(rs.getString(2)), getColumnLength(rs.getString(2)));
            ts.addColumn(rs.getString(1), rs.getString(1), TableSchema.convertStringToColumnType(getColumnTypeString(rs.getString(2))), getColumnLength(rs.getString(2)));
        }

        return ts;
    }
    
    //Returns 0 based position
    public static int getIndexOfField(TableSchema t, String columnname) throws SQLException {
        return t.getFieldIndexInDB(columnname) - 1;
    }
    
    public static int getIndexOfField(String tablename, String columnname) throws SQLException {
        return getIndexOfField(importTableSchema(tablename), columnname);
    }
    
    public static void dropTable(String tablename) throws SQLException {
        Connection c = (ConnectionController.connectPooled());

        c.createStatement().execute(
                "DROP TABLE IF EXISTS " + tablename + ";");
    }

    public static boolean tableExists(String tablename) throws SQLException {
        Statement s = ConnectionController.connectPooled().createStatement();
        
        ResultSet rs = s.executeQuery("SHOW TABLES");
        
        while(rs.next()) {
            if (rs.getString(1).equals(tablename)) {
                return true;
            }
        }
        
        return false;
    }

    public static int getNumRecordsInTable(String tablename) {
        try {
            Connection c = ConnectionController.connectPooled();
            ResultSet rs =  c.createStatement().executeQuery("SELECT COUNT(*) FROM `" + tablename + "`");
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(DBUtil.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public static Timestamp getCurrentTimestamp(){
        return new Timestamp((new Date()).getTime());
    }
}
