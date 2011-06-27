/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import org.ut.biolab.medsavant.db.table.CohortViewTableSchema;
import org.ut.biolab.medsavant.db.table.SubjectTableSchema;
import org.ut.biolab.medsavant.db.table.TableSchema;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;

/**
 *
 * @author mfiume
 */
public class MedSavantDatabase {

    private final DbSpec spec;
    private final DbSchema schema;
    private static MedSavantDatabase instance;

    private VariantTableSchema variantTableSchema;
    private SubjectTableSchema subjectTableSchema;

    
    public static void main(String[] argv) {
        getInstance();
    }

    public static MedSavantDatabase getInstance() {
        if (instance == null) {
            instance = new MedSavantDatabase();
        }
        return instance;
    }
    private CohortViewTableSchema cohortviewTableSchema;

    public DbSchema getSchema() {
        return schema;
    }

    public MedSavantDatabase() {
        spec = new DbSpec();
        schema = spec.addDefaultSchema();
        initTableSchemas();
    }

    private void initTableSchemas() {
        variantTableSchema = new VariantTableSchema(schema);
        subjectTableSchema = new SubjectTableSchema(schema);
        cohortviewTableSchema = new CohortViewTableSchema(schema);
    }

    public TableSchema getVariantTableSchema() {
        return this.variantTableSchema;
    }
    
    public TableSchema getSubjectTableSchema() {
        return this.subjectTableSchema;
    }
    
    public TableSchema getCohortViewTableSchema() {
        return this.cohortviewTableSchema;
    }
    
    
}
