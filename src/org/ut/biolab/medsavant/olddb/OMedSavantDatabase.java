/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.olddb;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;

/**
 *
 * @author mfiume
 */
public class OMedSavantDatabase {
    private final DbSpec spec;
    private final DbSchema schema;
    private static OMedSavantDatabase instance;

    public static void main(String[] argv) {
        getInstance();
    }

    public static OMedSavantDatabase getInstance() {
        if (instance == null) {
            instance = new OMedSavantDatabase();
        }
        return instance;
    }
    
    public DbSchema getSchema() {
        return schema;
    }

    public OMedSavantDatabase() {
        spec = new DbSpec();
        schema = spec.addDefaultSchema();
        initTableSchemas();
    }

    private void initTableSchemas() {
    }
    
}
