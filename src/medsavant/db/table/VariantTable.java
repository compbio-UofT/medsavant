package medsavant.db.table;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

/**
 *
 * @author mfiume
 */
public class VariantTable extends TableSchema {

    public static final String TABLE_NAME = "variant";
    public static final String COL_SAMPLEID = "sample_id";
    public static final String COL_CHROM = "chrom";
    public static final String COL_POSITION = "position";
    public static final String COL_ID = "id";
    public static final String COL_REFERENCE = "ref";
    public static final String COL_ALTERNATE = "alt";
    public static final String COL_QUALITY = "qual";
    public static final String COL_FILTER = "filter";
    public static final String COL_INFORMATION = "info";

    public VariantTable(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
    }

    private void addColumns() {
        addColumn(COL_SAMPLEID,TableSchema.COLUMN_VARCHAR,10);
        addColumn(COL_CHROM,TableSchema.COLUMN_VARCHAR,5);
        addColumn(COL_POSITION,TableSchema.COLUMN_INTEGER,11);
        addColumn(COL_ID,TableSchema.COLUMN_VARCHAR,45);
        addColumn(COL_REFERENCE,TableSchema.COLUMN_VARCHAR,10);
        addColumn(COL_ALTERNATE,TableSchema.COLUMN_VARCHAR,10);
        addColumn(COL_QUALITY,TableSchema.COLUMN_FLOAT,11);
        addColumn(COL_FILTER,TableSchema.COLUMN_VARCHAR,500);
        addColumn(COL_INFORMATION,TableSchema.COLUMN_VARCHAR,500);
    }

}
