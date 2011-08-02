/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db;

import org.ut.biolab.medsavant.db.table.GeneListTableSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import org.ut.biolab.medsavant.db.table.AlignmentTableSchema;
import org.ut.biolab.medsavant.db.table.CohortTableSchema;
import org.ut.biolab.medsavant.db.table.CohortViewTableSchema;
import org.ut.biolab.medsavant.db.table.GeneListMembershipTableSchema;
import org.ut.biolab.medsavant.db.table.GeneListViewTableSchema;
import org.ut.biolab.medsavant.db.table.GenomeTableSchema;
import org.ut.biolab.medsavant.db.table.TableSchema;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;
import org.ut.biolab.medsavant.db.table.ModifiableTableSchema;
import org.ut.biolab.medsavant.db.table.PatientTableSchema;

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
    private GenomeTableSchema genomeTableSchema;
    //private PatientTableSchema patientTableSchema;
    
    private PatientTableSchema patientTableSchema;
    private ModifiableTableSchema phenotypeTableSchema;
    
    //private String[] patientDefaults = {"dnaid", "patientid", "familyid", "name", "dob", "dod", "gender"};
    private String[] phenotypeDefaults = {"patientid", "doa"}; 
    
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
        genomeTableSchema = new GenomeTableSchema(schema);
        //patientTableSchema = new PatientTableSchema(schema);
        
        //patientTableSchema1 = new ModifiableTableSchema(schema, "patient", patientDefaults);
        //patientTableSchema = new PatientTableSchema(schema);
        //phenotypeTableSchema = new ModifiableTableSchema(schema, "phenotype", phenotypeDefaults);
        refreshModifiableTables();
    }

    public TableSchema getVariantTableSchema() {
        return this.variantTableSchema;
    }
    
    public PatientTableSchema getPatientTableSchema() {
        return this.patientTableSchema;
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
    
    public TableSchema getGenomeTableSchema() {
        return genomeTableSchema;
    }
    
    public ModifiableTableSchema[] getModifiableTables(){
        return new ModifiableTableSchema[]{patientTableSchema,phenotypeTableSchema};
    }
   
    public void refreshModifiableTables(){
        patientTableSchema = new PatientTableSchema(schema);
        phenotypeTableSchema = new ModifiableTableSchema(schema, "phenotype", phenotypeDefaults);
    }
    
}
