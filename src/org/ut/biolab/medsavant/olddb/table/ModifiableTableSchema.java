/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.olddb.ConnectionController;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.ut.biolab.medsavant.olddb.DBUtil;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;

/**
 *
 * @author AndrewBrook
 */
public class ModifiableTableSchema extends TableSchema {
    
    private String tableName;
    private String[] mandatoryColumns;
    private List<ModifiableColumn> columns = new ArrayList<ModifiableColumn>();
    
    public ModifiableTableSchema(DbSchema s, String tableName, String[] mandatoryCols) {
        super(s.addTable(tableName));
        this.tableName = tableName;
        this.mandatoryColumns = mandatoryCols;
        
        try {
            generateColumns();
        } catch (SQLException ex) {
            Logger.getLogger(ModifiableTableSchema.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonFatalDatabaseException ex) {
            Logger.getLogger(ModifiableTableSchema.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        addColumns();
    }
    
    public String getTableName(){
        return tableName;
    }
    
    @Override
    public String toString(){
        return tableName;
    }
    
    private void generateColumns() throws SQLException, NonFatalDatabaseException {
        Connection conn = ConnectionController.connect();       
        String query = "SELECT column_name, column_comment, column_type "
                + "FROM information_schema.columns "
                + "WHERE table_name = '" +  tableName + "' AND table_schema = 'medsavantdb'"; //TODO remove table_schema
        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(query);

        while(rs.next()){
            String columnName = rs.getString(1);
            String columnComment = rs.getString(2);
            String columnType = rs.getString(3);
            
            int breakPoint1 = columnComment.indexOf(":");
            int breakPoint2 = columnComment.indexOf(":", breakPoint1+1);
            String columnAlias = columnComment.substring(0, breakPoint1);
            boolean columnIsFilter = columnComment.substring(breakPoint1+1, breakPoint2).contains("f");
            String columnDesc = columnComment.substring(breakPoint2+1);
            
            ColumnType ct = parseType(columnType);
            int columnLen = parseLength(columnType, ct);
            
            boolean mandatory = isColumnMandatory(columnName);
            columns.add(new ModifiableColumn(columnName, columnAlias, columnDesc, ct, columnLen, columnIsFilter, mandatory, false));
        }
    }
    
    private ColumnType parseType(String s){
        if(s.startsWith("varchar")){
            return ColumnType.VARCHAR;
        } else if (s.startsWith("date")){
            return ColumnType.DATE;
        } else if (s.startsWith("int") || s.startsWith("tinyint")){
            return ColumnType.INTEGER;
        } else if (s.startsWith("float")){
            return ColumnType.FLOAT;
        } else if (s.startsWith("decimal")){
            return ColumnType.DECIMAL;
        }
        return null;
    }
    
    private int parseLength(String s, ColumnType type){
        if(type == ColumnType.DATE || type == ColumnType.FLOAT) return 0;
        int parenPosition = s.indexOf("(");
        int parenPosition2 = s.indexOf(")");
        String numberString = s.substring(parenPosition+1, parenPosition2);
        return Integer.parseInt(numberString);
    }
    
    private boolean isColumnMandatory(String columnName){
        for(String s : mandatoryColumns){
            if(columnName.equals(s)){
                return true;
            }
        }
        return false;
    }
    
    private void addColumns() {
        for(ModifiableColumn c : columns){
            addColumn(c.getColumnName(), c.getShortName(), c.getType(), c.getLength());          
        }
    }
    
    public List<ModifiableColumn> getModColumns(){
        return this.columns;
    }
    
    public void addModColumn(ModifiableColumn c){
        columns.add(c);
        addColumn(c.getColumnName(), c.getShortName(), c.getType(), c.getLength());
    }
    
    public String getUntitledString(){
        String name = "untitledcolumn";
        for(ModifiableColumn c : columns){
            if(c.getColumnName().equals(name)){
                name = name + "1";
            }
        }
        return name;
    }
    
    public void removeChanges(){
        for(ModifiableColumn c : columns){
            c.reset();
        }
    }
    
    public void applyChanges() throws SQLException, NonFatalDatabaseException{
        
        //check for duplicates
        List<String> columnNames = new ArrayList<String>();
        List<String> shortNames = new ArrayList<String>();
        for(ModifiableColumn c : columns){
            if(c.isRemoved())continue;
            if(columnNames.contains(c.getColumnName())){
                JOptionPane.showMessageDialog(null, "There are multiple columns with the same name!", "Cannot commit changes", JOptionPane.ERROR_MESSAGE);
                return;     
            } else if (shortNames.contains(c.getShortName())){
                JOptionPane.showMessageDialog(null, "There are multiple columns with the same short name!", "Cannot commit changes", JOptionPane.ERROR_MESSAGE);                
                return;
            }
            columnNames.add(c.getColumnName());
            shortNames.add(c.getShortName());
        }

        //remove columns
        List<ModifiableColumn> queuedForRemoval = new ArrayList<ModifiableColumn>();
        for(ModifiableColumn c : columns){
            if(c.isRemoved()){
                if(!c.isNew()){
                    DBUtil.executeUpdate(createDrop(c.getColumnName()));
                }
                queuedForRemoval.add(c);
            }
        }
        for(ModifiableColumn c : queuedForRemoval){
            columns.remove(c);
        }
        
        //modify columns
        for(ModifiableColumn c : columns){
            if(!c.isNew() && c.isModified()){
                String isFilterString = "";
                if(c.isFilter()) isFilterString = "f";
                DBUtil.executeUpdate(createModify(c.getOldColumnName(), c.getColumnName(), c.getType(), c.getLength(), c.getShortName()+":"+isFilterString+":"+c.getDescription()));
            }
        }
        
        //create columns
        for(ModifiableColumn c : columns){
            if(c.isNew()){
                String isFilterString = "";
                if(c.isFilter()) isFilterString = "f";
                DBUtil.executeUpdate(createAdd(c.getColumnName(), c.getType(), c.getLength(), c.getShortName()+":"+isFilterString+":"+c.getDescription()));
            }
        }
        
        //reset columns
        removeChanges();
        
    }
    
    private String createDrop(String colName){
        return "ALTER TABLE `" + this.tableName + "` "
                + "DROP COLUMN `" + colName + "`; ";
    }
    
    private String createModify(String colName, String newName, ColumnType colType, int length, String newComment){
        String typeString = getColumnTypeString(colType);
        String lengthString = "";
        if(!(colType == ColumnType.DATE || colType == ColumnType.FLOAT || colType == ColumnType.BOOLEAN)){
            lengthString = "(" + String.valueOf(length) + ")";
        }
        return "ALTER TABLE `" + this.tableName + "` "
                + "CHANGE COLUMN `" + colName + "` `" + newName + "` " + typeString + lengthString + " DEFAULT NULL COMMENT '" + newComment + "'; ";
    }
    
    private String createAdd(String colName, ColumnType colType, int length, String colComment){
        String typeString = getColumnTypeString(colType);
        String lengthString = "";
        if(!(colType == ColumnType.DATE || colType == ColumnType.FLOAT || colType == ColumnType.BOOLEAN)){
            lengthString = "(" + String.valueOf(length) + ")";
        }
        return "ALTER TABLE `" + this.tableName + "` "
                + "ADD COLUMN `" + colName + "` " + typeString + lengthString + " DEFAULT NULL COMMENT '" + colComment + "'; ";
    }
}
