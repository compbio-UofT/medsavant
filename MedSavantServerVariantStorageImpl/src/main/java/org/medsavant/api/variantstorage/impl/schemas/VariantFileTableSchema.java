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
 public class VariantFileTableSchema extends TableSchema {

        public static final String TABLE_NAME_PREFIX = "variant_file";

        public VariantFileTableSchema(DbSchema s) {
            this(s, "");
        }

        public VariantFileTableSchema(DbSchema s, String suffix) {
            super(s.addTable(TABLE_NAME_PREFIX + suffix));
            addColumns();
        }
        // variant_file.upload_id
        public static final int INDEX_OF_UPLOAD_ID = 0;
        public static final ColumnType TYPE_OF_UPLOAD_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_UPLOAD_ID = 11;
        public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
        // variant_file.file_id
        public static final int INDEX_OF_FILE_ID = 1;
        public static final ColumnType TYPE_OF_FILE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_FILE_ID = 11;
        public static final String COLUMNNAME_OF_FILE_ID = "file_id";
        // variant_file.project_id
        public static final int INDEX_OF_PROJECT_ID = 1;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
        // variant_file.reference_id
        public static final int INDEX_OF_REFERENCE_ID = 1;
        public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REFERENCE_ID = 11;
        public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
        // variant_file.file_name
        public static final int INDEX_OF_FILE_NAME = 2;
        public static final ColumnType TYPE_OF_FILE_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_FILE_NAME = 500;
        public static final String COLUMNNAME_OF_FILE_NAME = "file_name";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_UPLOAD_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_FILE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_FILE_NAME, ColumnType.VARCHAR, 500);
        }
    }
   
