/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;

/**
 *
 * @author mfiume
 */
public class DB {

    private final DbSpec spec;
    private final DbSchema schema;
    private static DB instance;
    public PatientTable patientTable;

    public static void main(String[] argv) {
        getInstance();
    }

    public static DB getInstance() {
        if (instance == null) {
            instance = new DB();
        }
        return instance;
    }

    public DbSchema getSchema() {
        return schema;
    }

    public DB() {
        spec = new DbSpec();
        schema = spec.addDefaultSchema();
        initTables();
    }

    private void initTables() {
        patientTable = new PatientTable(schema);
    }
}
