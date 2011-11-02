/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.format;

import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.DBUtil.FieldType;

/**
 *
 * @author Andrew
 */
public class CustomField {
   
    private String columnName;
    private String columnType;
    private boolean filterable;
    private String alias;
    private String description;
    private FieldType fieldType;   
    
    public CustomField(String name, String type, boolean filterable, String alias, String description){
        this.columnName = name;
        this.columnType = type;
        this.filterable = filterable;
        this.alias = alias;
        this.description = description;
        setFieldType(columnType);
    }

    public String getAlias() {
        return alias;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFilterable() {
        return filterable;
    }
    
    private void setFieldType(String type){
        fieldType = DBUtil.getFieldType(type);
    }
    
    public FieldType getFieldType(){
        return fieldType;
    }
    
    public String generateSchema(){
        return "`" + columnName + "` " + columnType + " DEFAULT NULL,";
    }
    
    @Override
    public String toString(){
        return alias;
    }
}
