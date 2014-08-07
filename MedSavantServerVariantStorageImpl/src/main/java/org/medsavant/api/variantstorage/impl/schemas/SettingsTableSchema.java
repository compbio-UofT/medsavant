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
public class SettingsTableSchema extends TableSchema {

    public static final String TABLE_NAME = "settings";

    public SettingsTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
    }
    // settings.key
    public static final int INDEX_OF_KEY = 0;
    public static final ColumnType TYPE_OF_KEY = ColumnType.VARCHAR;
    public static final int LENGTH_OF_KEY = 100;
    public static final String COLUMNNAME_OF_KEY = "setting_key";
    // settings.value
    public static final int INDEX_OF_VALUE = 1;
    public static final ColumnType TYPE_OF_VALUE = ColumnType.VARCHAR;
    public static final int LENGTH_OF_VALUE = 300;
    public static final String COLUMNNAME_OF_VALUE = "setting_value";

    private void addColumns() {
        addColumn(COLUMNNAME_OF_KEY, ColumnType.VARCHAR, 100);
        addColumn(COLUMNNAME_OF_VALUE, ColumnType.VARCHAR, 100);
    }
}
