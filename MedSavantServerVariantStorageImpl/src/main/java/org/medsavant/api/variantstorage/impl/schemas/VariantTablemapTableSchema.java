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
public class VariantTablemapTableSchema extends TableSchema {
    public static final String TABLE_NAME = "variant_tablemap";

    public VariantTablemapTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
    }
    // variant_tablemap.project_id
    public static final int INDEX_OF_PROJECT_ID = 0;
    public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_PROJECT_ID = 11;
    public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
    // variant_tablemap.reference_id
    public static final int INDEX_OF_REFERENCE_ID = 1;
    public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_REFERENCE_ID = 11;
    public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
    // variant_tablemap.update_id
    public static final int INDEX_OF_UPDATE_ID = 2;
    public static final ColumnType TYPE_OF_UPDATE_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_UPDATE_ID = 11;
    public static final String COLUMNNAME_OF_UPDATE_ID = "update_id";
    // variant_tablemap.published
    public static final int INDEX_OF_PUBLISHED = 3;
    public static final ColumnType TYPE_OF_PUBLISHED = ColumnType.BOOLEAN;
    public static final int LENGTH_OF_PUBLISHED = 1;
    public static final String COLUMNNAME_OF_PUBLISHED = "published";
    // variant_tablemap.variant_tablename
    public static final int INDEX_OF_VARIANT_TABLENAME = 4;
    public static final ColumnType TYPE_OF_VARIANT_TABLENAME = ColumnType.VARCHAR;
    public static final int LENGTH_OF_VARIANT_TABLENAME = 100;
    public static final String COLUMNNAME_OF_VARIANT_TABLENAME = "variant_tablename";
    // variant_tablemap.annotation_ids
    public static final int INDEX_OF_ANNOTATION_IDS = 5;
    public static final ColumnType TYPE_OF_ANNOTATION_IDS = ColumnType.VARCHAR;
    public static final int LENGTH_OF_ANNOTATION_IDS = 500;
    public static final String COLUMNNAME_OF_ANNOTATION_IDS = "annotation_ids";
    // variant_tablemap.variant_subset_tablename
    public static final int INDEX_OF_VARIANT_SUBSET_TABLENAME = 6;
    public static final ColumnType TYPE_OF_VARIANT_SUBSET_TABLENAME = ColumnType.VARCHAR;
    public static final int LENGTH_OF_VARIANT_SUBSET_TABLENAME = 100;
    public static final String COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME = "variant_subset_tablename";
    // variant_tablemap.subset_multiplier
    public static final int INDEX_OF_SUBSET_MULTIPLIER = 7;
    public static final ColumnType TYPE_OF_SUBSET_MULTIPLIER = ColumnType.FLOAT;
    public static final int LENGTH_OF_SUBSET_MULTIPLIER = -1;
    public static final String COLUMNNAME_OF_SUBSET_MULTIPLIER = "subset_multiplier";

    private void addColumns() {
        addColumn(COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_UPDATE_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_PUBLISHED, ColumnType.BOOLEAN, 1);
        addColumn(COLUMNNAME_OF_VARIANT_TABLENAME, ColumnType.VARCHAR, 100);
        addColumn(COLUMNNAME_OF_ANNOTATION_IDS, ColumnType.VARCHAR, 500);
        addColumn(COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME, ColumnType.VARCHAR, 100);
        addColumn(COLUMNNAME_OF_SUBSET_MULTIPLIER, ColumnType.FLOAT, -1);
    }
    
}
