/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.table;

import org.ut.biolab.medsavant.olddb.table.TableSchema.ColumnType;

/**
 *
 * @author AndrewBrook
 */
public class ModifiableColumn {
    
    //current values
    private String columnName;
    private String shortName;
    private String description;
    private ColumnType type;
    private int length;
    private Boolean isFilter;
    
    //old values
    private String columnNameOld;
    private String shortNameOld;
    private String descriptionOld;
    private ColumnType typeOld;
    private int lengthOld = -1;
    private Boolean isFilterOld;
    
    //mandatory field
    private boolean mandatory;
    
    //modifications
    private boolean isNew;
    private boolean isRemoved;
    
    public ModifiableColumn(String columnName, String shortName, String desc, ColumnType type, int len, boolean isFilter, boolean mandatory, boolean isNew){
        this.columnName = columnName;
        this.shortName = shortName;
        this.description = desc;
        this.type = type;
        this.length = len;
        this.isFilter = isFilter;
        this.mandatory = mandatory;
        this.isNew = isNew;
    }
    
    public String getColumnName(){
        return this.columnName;
    }
    
    public void setColumnName(String s){
        if(!s.equals(columnName)){
            if(columnNameOld == null){
                this.columnNameOld = columnName;
            }
            this.columnName = s;
        }   
    }
    
    public String getShortName(){
        return this.shortName;
    }
    
    public void setShortName(String s){
        if(!s.equals(shortName)){
            if(shortNameOld == null){
                this.shortNameOld = shortName;
            }
            this.shortName = s;
        }
    }
    
    public String getDescription(){
        return this.description;
    }
    
    public void setDescription(String s){
        if(!s.equals(description)){
            if(descriptionOld == null){
                this.descriptionOld = description;
            }           
            this.description = s;
        }
    }
    
    public ColumnType getType(){
        return this.type;
    }
    
    public void setType(ColumnType ct){
        if(ct != type){
            if(typeOld == null){
                this.typeOld = type;
            }
            this.type = ct;
        }
    }
    
    public int getLength(){
        return this.length;
    }
    
    public void setLength(int len){
        if(len != length){
            if(lengthOld == -1){
                this.lengthOld = length;
            }
            this.length = len;
        }
    }
    
    public boolean isFilter(){
        return this.isFilter;
    }
    
    public void setIsFilter(boolean isFilter){
        if(isFilter != this.isFilter){
            if(isFilterOld == null){
                this.isFilterOld = this.isFilter;
            }
            this.isFilter = isFilter;
        }
    }
    
    public boolean isMandatory(){
        return this.mandatory;
    }
    
    public void setMandatory(boolean mandatory){
        this.mandatory = mandatory;
    }
    
    public boolean isModified(){
        return (columnNameOld != null || shortNameOld != null || descriptionOld != null || typeOld != null || lengthOld != -1 || isFilterOld != null);
    }
    
    public void setRemoved(boolean removed){
        this.isRemoved = removed;
    }
    
    public boolean isRemoved(){
        return this.isRemoved;
    }
    
    public boolean isNew(){
        return this.isNew;
    }
    
    public String getOldColumnName(){
        if(columnNameOld != null){
            return this.columnNameOld;
        }
        return columnName;
    }
    
    public String getOldShortName(){
        if(shortNameOld != null){
            return this.shortNameOld;
        }
        return shortName;
    }
    
    public String getOldDescription(){
        if(descriptionOld != null){
            return this.descriptionOld;
        }
        return descriptionOld;
    }
    
    public ColumnType getOldType(){
        if(typeOld != null){
            return this.typeOld;
        }
        return type;
    }
    
    public int getOldLength(){
        if(lengthOld != -1){
            return this.lengthOld;
        }
        return length;
    }
    
    public boolean getOldIsFilter(){
        if(isFilterOld != null){
            return isFilterOld;
        }
        return isFilter;
    }
    
    public void reset(){
        isNew = false;
        isRemoved = false;
        columnNameOld = null;
        shortNameOld = null;
        descriptionOld = null;
        typeOld = null;
        lengthOld = -1;
        isFilterOld = null;
    }
    
    @Override
    public String toString(){
        return this.shortName;
    }
    
}
