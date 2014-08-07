/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.variantstorage.impl.schemas;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import org.medsavant.api.common.storage.ColumnType;
import org.ut.biolab.medsavant.shared.db.TableSchema;

/**
 *
 * @author jim
 */
public class VariantPendingUpdateTableSchema extends TableSchema {
    public static final String TABLE_NAME = "variant_pending_update";

    public VariantPendingUpdateTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
    }
    // variant_pending_update.upload_id
    public static final int INDEX_OF_UPLOAD_ID = 0;
    public static final ColumnType TYPE_OF_UPLOAD_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_UPLOAD_ID = 11;
    public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
    // variant_pending_update.project_id
    public static final int INDEX_OF_PROJECT_ID = 1;
    public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_PROJECT_ID = 11;
    public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
    // variant_pending_update.reference_id
    public static final int INDEX_OF_REFERENCE_ID = 2;
    public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_REFERENCE_ID = 11;
    public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
    // variant_pending_update.action
    public static final int INDEX_OF_ACTION = 3;
    public static final ColumnType TYPE_OF_ACTION = ColumnType.INTEGER;
    public static final int LENGTH_OF_ACTION = 11;
    public static final String COLUMNNAME_OF_ACTION = "action";
    // variant_pending_update.status
    public static final int INDEX_OF_STATUS = 4;
    public static final ColumnType TYPE_OF_STATUS = ColumnType.INTEGER;
    public static final int LENGTH_OF_STATUS = 5;
    public static final String COLUMNNAME_OF_STATUS = "status";
    // variant_pending_update.timestamp
    public static final int INDEX_OF_TIMESTAMP = 5;
    public static final ColumnType TYPE_OF_TIMESTAMP = ColumnType.DATE;
    public static final int LENGTH_OF_TIMESTAMP = -1;
    public static final String COLUMNNAME_OF_TIMESTAMP = "timestamp";
    // variant_pending_update.user
    public static final int INDEX_OF_USER = 6;
    public static final ColumnType TYPE_OF_USER = ColumnType.VARCHAR;
    public static final int LENGTH_OF_USER = 200;
    public static final String COLUMNNAME_OF_USER = "user";

    private void addColumns() {
        addColumn(COLUMNNAME_OF_UPLOAD_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_ACTION, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_STATUS, ColumnType.INTEGER, 5);
        addColumn(COLUMNNAME_OF_TIMESTAMP, ColumnType.DATE, -1);
        addColumn(COLUMNNAME_OF_USER, ColumnType.VARCHAR, 200);
    }
    
}
