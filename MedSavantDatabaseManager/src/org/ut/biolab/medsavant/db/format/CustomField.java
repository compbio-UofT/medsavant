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
    private Category category;
    
    public static enum Category {PATIENT, GENOTYPE, PHENOTYPE, GENOME_COORDS, PLUGIN}
    
    public static String categoryToString(Category cat){
        switch(cat){
            case PATIENT:
                return "Patient & Phenotype";
            case GENOTYPE:
                return "Genotype";
            case PHENOTYPE:
                return "Variant Annotation";
            case GENOME_COORDS:
                return "Genomic Coordinates";
            case PLUGIN:
                return "Plugins";
            default:
                return "undefined";
        }
    }

    public CustomField(String name, String type, boolean filterable, String alias, String description){
        this.columnName = name;
        this.columnType = type;
        this.filterable = filterable;
        this.alias = alias;
        this.description = description;
        setFieldType(columnType);
    }
    
    public CustomField(String name, String type, boolean filterable, String alias, String description, Category category){
        this(name, type, filterable, alias, description);
        this.category = category;
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
    
    public Category getCategory() {
        return category;
    }
    
    @Override
    public String toString(){
        return alias;
    }
    
    @Override
    public boolean equals(Object o){
        if(o == null || !o.getClass().equals(CustomField.class)) return false;
        CustomField other = (CustomField)o;
        return this.getColumnName().equals(other.getColumnName()) &&
                this.getColumnType().equals(other.getColumnType());
    }

}
