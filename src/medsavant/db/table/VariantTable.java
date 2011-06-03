package medsavant.db.table;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import fiume.vcf.VariantRecord;
import java.util.Vector;

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

    public static final int INDEX_SAMPLEID = 0;
    public static final int INDEX_CHROM = INDEX_SAMPLEID + 1;
    public static final int INDEX_POSITION = INDEX_CHROM + 1;
    public static final int INDEX_ID = INDEX_POSITION + 1;
    public static final int INDEX_REFERENCE = INDEX_ID + 1;
    public static final int INDEX_ALTERNATE = INDEX_REFERENCE + 1;
    public static final int INDEX_QUALITY = INDEX_ALTERNATE + 1;
    public static final int INDEX_FILTER = INDEX_QUALITY + 1;
    public static final int INDEX_INFORMATION = INDEX_FILTER + 1;

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

    public static VariantRecord convertToVariantRecord(Vector dbResult) {
        return new VariantRecord(
                (String) dbResult.get(INDEX_SAMPLEID),
                (String) dbResult.get(INDEX_INFORMATION),
                (String) dbResult.get(INDEX_CHROM),
                Long.parseLong((String) dbResult.get(INDEX_POSITION)),
                (String) dbResult.get(INDEX_ID),
                (String) dbResult.get(INDEX_REFERENCE),
                (String) dbResult.get(INDEX_ALTERNATE),
                Float.parseFloat((String) dbResult.get(INDEX_QUALITY)),
                (String) dbResult.get(INDEX_FILTER),
                (String) dbResult.get(INDEX_INFORMATION),
                null
                );
    }



}
