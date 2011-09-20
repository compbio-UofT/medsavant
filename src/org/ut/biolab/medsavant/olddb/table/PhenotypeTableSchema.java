/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

/**
 *
 * @author AndrewBrook
 */
public class PhenotypeTableSchema extends ModifiableTableSchema {
    
    public static final String TABLE_NAME = "phenotype";
    
    public static final String DBFIELDNAME_PATIENTID = "patientid";
    public static final String DBFIELDNAME_DOA = "doa";
    
    public static final String ALIAS_PATIENTID = "Patient ID";
    public static final String ALIAS_DOA = "Date of Assessment";
    
    private static final String[] defaults = {
                    DBFIELDNAME_PATIENTID,
                    DBFIELDNAME_DOA};
    
    public PhenotypeTableSchema(DbSchema schema){
        super(schema, TABLE_NAME, defaults);
    }
    
}