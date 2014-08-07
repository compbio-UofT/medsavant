package org.medsavant.api.variantstorage.impl.schemas;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import org.medsavant.api.common.storage.ColumnType;
import org.ut.biolab.medsavant.shared.db.TableSchema;

public class VariantFormatTableSchema extends TableSchema {

    public static final String TABLE_NAME = "variant_format";

    public VariantFormatTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
    }
    // patient_format.project_id
    public static final int INDEX_OF_PROJECT_ID = 0;
    public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_PROJECT_ID = 11;
    public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
    // patient_format.reference_id
    public static final int INDEX_OF_REFERENCE_ID = 1;
    public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_REFERENCE_ID = 11;
    public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
    // patient_format.update_id
    public static final int INDEX_OF_UPDATE_ID = 2;
    public static final ColumnType TYPE_OF_UPDATE_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_UPDATE_ID = 11;
    public static final String COLUMNNAME_OF_UPDATE_ID = "update_id";
    // patient_format.position
    public static final int INDEX_OF_POSITION = 3;
    public static final ColumnType TYPE_OF_POSITION = ColumnType.INTEGER;
    public static final int LENGTH_OF_POSITION = 11;
    public static final String COLUMNNAME_OF_POSITION = "position";
    // patient_format.column_name
    public static final int INDEX_OF_COLUMN_NAME = 4;
    public static final ColumnType TYPE_OF_COLUMN_NAME = ColumnType.VARCHAR;
    public static final int LENGTH_OF_COLUMN_NAME = 200;
    public static final String COLUMNNAME_OF_COLUMN_NAME = "column_name";
    // patient_format.column_type
    public static final int INDEX_OF_COLUMN_TYPE = 5;
    public static final ColumnType TYPE_OF_COLUMN_TYPE = ColumnType.VARCHAR;
    public static final int LENGTH_OF_COLUMN_TYPE = 45;
    public static final String COLUMNNAME_OF_COLUMN_TYPE = "column_type";
    // patient_format.filterable
    public static final int INDEX_OF_FILTERABLE = 6;
    public static final ColumnType TYPE_OF_FILTERABLE = ColumnType.INTEGER;
    public static final int LENGTH_OF_FILTERABLE = 1;
    public static final String COLUMNNAME_OF_FILTERABLE = "filterable";
    // patient_format.alias
    public static final int INDEX_OF_ALIAS = 7;
    public static final ColumnType TYPE_OF_ALIAS = ColumnType.VARCHAR;
    public static final int LENGTH_OF_ALIAS = 200;
    public static final String COLUMNNAME_OF_ALIAS = "alias";
    // patient_format.description
    public static final int INDEX_OF_DESCRIPTION = 8;
    public static final ColumnType TYPE_OF_DESCRIPTION = ColumnType.VARCHAR;
    public static final int LENGTH_OF_DESCRIPTION = 500;
    public static final String COLUMNNAME_OF_DESCRIPTION = "description";

    private void addColumns() {
        addColumn(COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_UPDATE_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_POSITION, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_COLUMN_NAME, ColumnType.VARCHAR, 200);
        addColumn(COLUMNNAME_OF_COLUMN_TYPE, ColumnType.VARCHAR, 45);
        addColumn(COLUMNNAME_OF_FILTERABLE, ColumnType.INTEGER, 1);
        addColumn(COLUMNNAME_OF_ALIAS, ColumnType.VARCHAR, 200);
        addColumn(COLUMNNAME_OF_DESCRIPTION, ColumnType.VARCHAR, 500);
    }
}
