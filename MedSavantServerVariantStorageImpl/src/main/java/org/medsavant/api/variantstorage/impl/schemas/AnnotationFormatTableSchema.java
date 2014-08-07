/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.variantstorage.impl.schemas;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import org.medsavant.api.common.storage.ColumnType;
import org.ut.biolab.medsavant.shared.db.ColumnDefImpl;
import org.ut.biolab.medsavant.shared.db.TableSchema;

/**
 *
 * @author jim
 */
public class AnnotationFormatTableSchema extends TableSchema{
        public static final String TABLE_NAME = "annotation_format";
                
        public AnnotationFormatTableSchema(DbSchema s){
                super(s.addTable(TABLE_NAME));
                addColumn(COLUMN_NAME_OF_ANNOTATION_ID, TYPE_OF_ANNOTATION_ID, 11);
                addColumn(COLUMN_NAME_OF_POSITION, TYPE_OF_POSITION, 11);
                addColumn(COLUMN_NAME_OF_COLUMN_NAME, TYPE_OF_COLUMN_NAME, LENGTH_OF_COLUMN_NAME);
                addColumn(COLUMN_NAME_OF_COLUMN_TYPE, TYPE_OF_COLUMN_TYPE, LENGTH_OF_COLUMN_TYPE);
                addColumn(COLUMN_NAME_OF_FILTERABLE, TYPE_OF_FILTERABLE, 1);
                addColumn(COLUMN_NAME_OF_ALIAS, TYPE_OF_ALIAS, LENGTH_OF_ALIAS);
                addColumn(COLUMN_NAME_OF_DESCRIPTION, TYPE_OF_DESCRIPTION, LENGTH_OF_DESCRIPTION);
                addColumn(COLUMN_NAME_OF_TAGS, TYPE_OF_TAGS, LENGTH_OF_TAGS);
        }
        
        public static final int INDEX_OF_ANNOTATION_ID = 0;
        public static final ColumnType TYPE_OF_ANNOTATION_ID = ColumnType.INTEGER;
        public static final String COLUMN_NAME_OF_ANNOTATION_ID = "annotation_id";
        
        public static final int INDEX_OF_POSITION = 1;
        public static final ColumnType TYPE_OF_POSITION = ColumnType.INTEGER;
        public static final String COLUMN_NAME_OF_POSITION = "position";
        
        public static final int INDEX_OF_COLUMN_NAME = 2;
        public static final ColumnType TYPE_OF_COLUMN_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_COLUMN_NAME = 200;
        public static final String COLUMN_NAME_OF_COLUMN_NAME = "column_name";
        
        public static final int INDEX_OF_COLUMN_TYPE = 3;
        public static final ColumnType TYPE_OF_COLUMN_TYPE = ColumnType.VARCHAR;
        public static final String COLUMN_NAME_OF_COLUMN_TYPE = "column_type";
        public static final int LENGTH_OF_COLUMN_TYPE = 45;
        
        public static final int INDEX_OF_FILTERABLE = 4;
        public static final ColumnType TYPE_OF_FILTERABLE = ColumnType.BOOLEAN;
        public static final String COLUMN_NAME_OF_FILTERABLE = "filterable";
        
        public static final int INDEX_OF_ALIAS = 5;
        public static final ColumnType TYPE_OF_ALIAS = ColumnType.VARCHAR;
        public static final String COLUMN_NAME_OF_ALIAS = "alias";
        public static final int LENGTH_OF_ALIAS = 200;
        
        public static final int INDEX_OF_DESCRIPTION = 6;
        public static final ColumnType TYPE_OF_DESCRIPTION = ColumnType.VARCHAR;
        public static final String COLUMN_NAME_OF_DESCRIPTION = "alias";
        public static final int LENGTH_OF_DESCRIPTION = 500;
        
        public static final int INDEX_OF_TAGS = 7;
        public static final ColumnType TYPE_OF_TAGS = ColumnType.VARCHAR;
        public static final String COLUMN_NAME_OF_TAGS = "alias";
        public static final int LENGTH_OF_TAGS = 500;
                        
        
        static final ColumnDefImpl ANNOTATION_ID = new ColumnDefImpl("annotation_id", ColumnType.INTEGER, 11, true, true, true, null);
        static final ColumnDefImpl POSITION = new ColumnDefImpl("position", ColumnType.INTEGER, 11, true, true, true, null);
        static final ColumnDefImpl COLUMN_NAME = new ColumnDefImpl("column_name", ColumnType.VARCHAR, 200);
        static final ColumnDefImpl COLUMN_TYPE = new ColumnDefImpl("column_type", ColumnType.VARCHAR, 45);
        static final ColumnDefImpl FILTERABLE = new ColumnDefImpl("filterable", ColumnType.BOOLEAN, 1);
        static final ColumnDefImpl ALIAS = new ColumnDefImpl("alias", ColumnType.VARCHAR, 200);
        static final ColumnDefImpl DESCRIPTION = new ColumnDefImpl("description", ColumnType.VARCHAR, 500);
        static final ColumnDefImpl TAGS = new ColumnDefImpl("tags", ColumnType.VARCHAR, 500);
}
    

