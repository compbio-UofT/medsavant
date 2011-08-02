/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

/**
 *
 * @author AndrewBrook
 */
public class PatientTableSchema extends ModifiableTableSchema {
    
    public static final String TABLE_NAME = "patient";
    
    public static final String DBFIELDNAME_DNAID = "dnaid";
    public static final String DBFIELDNAME_PATIENTID = "patientid";
    public static final String DBFIELDNAME_FAMILYID = "familyid";
    public static final String DBFIELDNAME_NAME = "name";
    public static final String DBFIELDNAME_DOB = "dob";
    public static final String DBFIELDNAME_DOD = "dod";
    public static final String DBFIELDNAME_GENDER = "gender";
    
    public static final String ALIAS_DNAID = "DNA ID";
    public static final String ALIAS_PATIENTID = "Patient ID";
    public static final String ALIAS_FAMILYID = "Family ID";
    public static final String ALIAS_NAME = "Name";
    public static final String ALIAS_DOB = "Date of Birth";
    public static final String ALIAS_DOD = "Date of Death";
    public static final String ALIAS_GENDER = "Gender";
    
    private static final String[] defaults = {
                    DBFIELDNAME_DNAID,
                    DBFIELDNAME_PATIENTID,
                    DBFIELDNAME_FAMILYID,
                    DBFIELDNAME_NAME,
                    DBFIELDNAME_DOB,
                    DBFIELDNAME_DOD,
                    DBFIELDNAME_GENDER};
    
    public PatientTableSchema(DbSchema schema){
        super(schema, TABLE_NAME, defaults);
    }
    
}
