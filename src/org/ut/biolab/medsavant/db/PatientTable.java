/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

/**
 *
 * @author mfiume
 */
public class PatientTable extends TableSchema {

    public static final String TABLE_NAME = "patient";
    public static final String COL_PATIENTID = "id";
    public static final String COL_GENDER = "gender";
    public static final String COL_AGE = "age";

    public PatientTable(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
    }

    private void addColumns() {
        addColumn(COL_PATIENTID,"varchar",255);
        addColumn(COL_GENDER,"varchar",255);
        addColumn(COL_AGE,"varchar",255);
    }

}
