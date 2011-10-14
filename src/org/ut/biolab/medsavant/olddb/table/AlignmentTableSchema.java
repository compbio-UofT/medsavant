/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;

/**
 *
 * @author AndrewBrook
 */
public class AlignmentTableSchema extends TableSchema {
    
    public static final String TABLE_NAME = "alignment";
   
    public static int INDEX_DNAID;
    public static int INDEX_PIPELINEID;
    public static int INDEX_ALIGNMENTPATH;
    
    public static final String ALIAS_DNAID = "DNA ID";
    public static final String ALIAS_PIPELINEID = "Pipeline ID";
    public static final String ALIAS_ALIGNMENTPATH = "Alignment Path";

    public static final String DBFIELDNAME_DNAID = "dna_id";
    public static final String DBFIELDNAME_PIPELINEID = "pipeline_id";
    public static final String DBFIELDNAME_ALIGNMENTPATH = "alignment_path";

    public AlignmentTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }
    
    private void setIndexes() {
        INDEX_DNAID = this.getFieldIndexInDB(DBFIELDNAME_DNAID);
        INDEX_PIPELINEID = this.getFieldIndexInDB(DBFIELDNAME_PIPELINEID);
        INDEX_ALIGNMENTPATH = this.getFieldIndexInDB(DBFIELDNAME_ALIGNMENTPATH);
    }
 
    private void addColumns() {
        addColumn(DBFIELDNAME_DNAID,ALIAS_DNAID,TableSchema.ColumnType.VARCHAR,40);
        addColumn(DBFIELDNAME_PIPELINEID,ALIAS_PIPELINEID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_ALIGNMENTPATH,ALIAS_ALIGNMENTPATH,TableSchema.ColumnType.VARCHAR,200);
    }
    
}
