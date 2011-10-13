/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.olddb;

import org.ut.biolab.medsavant.olddb.table.GeneListTableSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import org.ut.biolab.medsavant.olddb.table.AlignmentTableSchema;
import org.ut.biolab.medsavant.olddb.table.CohortTableSchema;
import org.ut.biolab.medsavant.olddb.table.CohortViewTableSchema;
import org.ut.biolab.medsavant.olddb.table.GeneListMembershipTableSchema;
import org.ut.biolab.medsavant.olddb.table.GeneListViewTableSchema;
import org.ut.biolab.medsavant.olddb.table.TableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;

/**
 *
 * @author mfiume
 */
public class MedSavantDatabase {

    private final DbSpec spec;
    private final DbSchema schema;
    private static MedSavantDatabase instance;

    private VariantTableSchema variantTableSchema;  
    
    private AlignmentTableSchema alignmentTableSchema;
    private CohortViewTableSchema cohortviewTableSchema;
    private CohortTableSchema cohortTableSchema;
    private GeneListTableSchema geneListTableSchema;
    private GeneListViewTableSchema geneListViewTableSchema;
    private GeneListMembershipTableSchema geneListMembershipTableSchema;
    //private PatientTableSchema patientTableSchema;
    
    public static void main(String[] argv) {
        getInstance();
    }

    public static MedSavantDatabase getInstance() {
        if (instance == null) {
            instance = new MedSavantDatabase();
        }
        return instance;
    }
    
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
        
        cohortviewTableSchema = new CohortViewTableSchema(schema);
        cohortTableSchema = new CohortTableSchema(schema);
        geneListTableSchema = new GeneListTableSchema(schema);
        geneListViewTableSchema = new GeneListViewTableSchema(schema);
        geneListMembershipTableSchema = new GeneListMembershipTableSchema(schema);
        alignmentTableSchema = new AlignmentTableSchema(schema);
    }

    public VariantTableSchema getVariantTableSchema() {
        return this.variantTableSchema;
    }
    
    public TableSchema getCohortViewTableSchema() {
        return this.cohortviewTableSchema;
    }
    
    public TableSchema getCohortTableSchema() {
        return this.cohortTableSchema;
    }
    
    public TableSchema getGeneListViewTableSchema() {
        return this.geneListViewTableSchema;
    }
    
    public TableSchema getGeneListTableSchema() {
        return this.geneListTableSchema;
    }

    public TableSchema getGeneListMembershipTableSchema() {
        return geneListMembershipTableSchema;
    }
    
    public TableSchema getAlignmentTableSchema() {
        return alignmentTableSchema;
    }
}
