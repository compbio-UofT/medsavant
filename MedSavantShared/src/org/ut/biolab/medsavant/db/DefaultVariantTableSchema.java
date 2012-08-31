/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.db;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;


/**
 * Class which defines the default schema for variant tables.
 * Used by both client and server.
 */
public class DefaultVariantTableSchema extends TableSchema {

    public static final String TABLE_NAME = "default_variant";

    public DefaultVariantTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
    }

    public DefaultVariantTableSchema(DbSchema s, String tablename) {
        super(s.addTable(tablename));
        addColumns();
    }
    // default_variant.upload_id
    public static final int INDEX_OF_UPLOAD_ID = 0;
    public static final ColumnType TYPE_OF_UPLOAD_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_UPLOAD_ID = 11;
    public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
    // default_variant.file_id
    public static final int INDEX_OF_FILE_ID = 1;
    public static final ColumnType TYPE_OF_FILE_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_FILE_ID = 11;
    public static final String COLUMNNAME_OF_FILE_ID = "file_id";
    // default_variant.variant_id
    public static final int INDEX_OF_VARIANT_ID = 2;
    public static final ColumnType TYPE_OF_VARIANT_ID = ColumnType.INTEGER;
    public static final int LENGTH_OF_VARIANT_ID = 11;
    public static final String COLUMNNAME_OF_VARIANT_ID = "variant_id";
    // default_variant.dna_id
    public static final int INDEX_OF_DNA_ID = 3;
    public static final ColumnType TYPE_OF_DNA_ID = ColumnType.VARCHAR;
    public static final int LENGTH_OF_DNA_ID = 100;
    public static final String COLUMNNAME_OF_DNA_ID = "dna_id";
    // default_variant.chrom
    public static final int INDEX_OF_CHROM = 4;
    public static final ColumnType TYPE_OF_CHROM = ColumnType.VARCHAR;
    public static final int LENGTH_OF_CHROM = 5;
    public static final String COLUMNNAME_OF_CHROM = "chrom";
    // default_variant.position
    public static final int INDEX_OF_POSITION = 5;
    public static final ColumnType TYPE_OF_POSITION = ColumnType.INTEGER;
    public static final int LENGTH_OF_POSITION = 11;
    public static final String COLUMNNAME_OF_POSITION = "position";
    // default_variant.dbsnp_id
    public static final int INDEX_OF_DBSNP_ID = 6;
    public static final ColumnType TYPE_OF_DBSNP_ID = ColumnType.VARCHAR;
    public static final int LENGTH_OF_DBSNP_ID = 45;
    public static final String COLUMNNAME_OF_DBSNP_ID = "dbsnp_id";
    // default_variant.ref
    public static final int INDEX_OF_REF = 7;
    public static final ColumnType TYPE_OF_REF = ColumnType.VARCHAR;
    public static final int LENGTH_OF_REF = 30;
    public static final String COLUMNNAME_OF_REF = "ref";
    // default_variant.alt
    public static final int INDEX_OF_ALT = 8;
    public static final ColumnType TYPE_OF_ALT = ColumnType.VARCHAR;
    public static final int LENGTH_OF_ALT = 30;
    public static final String COLUMNNAME_OF_ALT = "alt";
    // default_variant.qual
    public static final int INDEX_OF_QUAL = 9;
    public static final ColumnType TYPE_OF_QUAL = ColumnType.FLOAT;
    public static final int LENGTH_OF_QUAL = 10;
    public static final String COLUMNNAME_OF_QUAL = "qual";
    // default_variant.filter
    public static final int INDEX_OF_FILTER = 10;
    public static final ColumnType TYPE_OF_FILTER = ColumnType.VARCHAR;
    public static final int LENGTH_OF_FILTER = 500;
    public static final String COLUMNNAME_OF_FILTER = "filter";
    // default_variant.variant_type
    public static final int INDEX_OF_VARIANT_TYPE = 11;
    public static final ColumnType TYPE_OF_VARIANT_TYPE = ColumnType.VARCHAR;
    public static final int LENGTH_OF_VARIANT_TYPE = 10;
    public static final String COLUMNNAME_OF_VARIANT_TYPE = "variant_type";
    // default_variant.zygosity
    public static final int INDEX_OF_ZYGOSITY = 12;
    public static final ColumnType TYPE_OF_ZYGOSITY = ColumnType.VARCHAR;
    public static final int LENGTH_OF_ZYGOSITY = 20;
    public static final String COLUMNNAME_OF_ZYGOSITY = "zygosity";
    // default_variant.gt
    public static final int INDEX_OF_GT = 13;
    public static final ColumnType TYPE_OF_GT = ColumnType.VARCHAR;
    public static final int LENGTH_OF_GT = 10;
    public static final String COLUMNNAME_OF_GT = "gt";
    // default_variant.custom_info
    public static final int INDEX_OF_CUSTOM_INFO = 14;
    public static final ColumnType TYPE_OF_CUSTOM_INFO = ColumnType.VARCHAR;
    public static final int LENGTH_OF_CUSTOM_INFO = 10000;
    public static final String COLUMNNAME_OF_CUSTOM_INFO = "custom_info";
    // default_variant.aa
    public static final int INDEX_OF_AA = 15;
    public static final ColumnType TYPE_OF_AA = ColumnType.VARCHAR;
    public static final int LENGTH_OF_AA = 500;
    public static final String COLUMNNAME_OF_AA = "aa";
    // default_variant.ac
    public static final int INDEX_OF_AC = 16;
    public static final ColumnType TYPE_OF_AC = ColumnType.INTEGER;
    public static final int LENGTH_OF_AC = 11;
    public static final String COLUMNNAME_OF_AC = "ac";
    // default_variant.af
    public static final int INDEX_OF_AF = 17;
    public static final ColumnType TYPE_OF_AF = ColumnType.FLOAT;
    public static final int LENGTH_OF_AF = -1;
    public static final String COLUMNNAME_OF_AF = "af";
    // default_variant.an
    public static final int INDEX_OF_AN = 18;
    public static final ColumnType TYPE_OF_AN = ColumnType.INTEGER;
    public static final int LENGTH_OF_AN = 11;
    public static final String COLUMNNAME_OF_AN = "an";
    // default_variant.bq
    public static final int INDEX_OF_BQ = 19;
    public static final ColumnType TYPE_OF_BQ = ColumnType.FLOAT;
    public static final int LENGTH_OF_BQ = -1;
    public static final String COLUMNNAME_OF_BQ = "bq";
    // default_variant.cigar
    public static final int INDEX_OF_CIGAR = 20;
    public static final ColumnType TYPE_OF_CIGAR = ColumnType.VARCHAR;
    public static final int LENGTH_OF_CIGAR = 500;
    public static final String COLUMNNAME_OF_CIGAR = "cigar";
    // default_variant.db
    public static final int INDEX_OF_DB = 21;
    public static final ColumnType TYPE_OF_DB = ColumnType.INTEGER;
    public static final int LENGTH_OF_DB = 1;
    public static final String COLUMNNAME_OF_DB = "db";
    // default_variant.dp
    public static final int INDEX_OF_DP = 22;
    public static final ColumnType TYPE_OF_DP = ColumnType.INTEGER;
    public static final int LENGTH_OF_DP = 11;
    public static final String COLUMNNAME_OF_DP = "dp";
    // default_variant.end
    public static final int INDEX_OF_END = 23;
    public static final ColumnType TYPE_OF_END = ColumnType.INTEGER;
    public static final int LENGTH_OF_END = 11;
    public static final String COLUMNNAME_OF_END = "end";
    // default_variant.h2
    public static final int INDEX_OF_H2 = 24;
    public static final ColumnType TYPE_OF_H2 = ColumnType.INTEGER;
    public static final int LENGTH_OF_H2 = 1;
    public static final String COLUMNNAME_OF_H2 = "h2";
    // default_variant.mq
    public static final int INDEX_OF_MQ = 25;
    public static final ColumnType TYPE_OF_MQ = ColumnType.FLOAT;
    public static final int LENGTH_OF_MQ = -1;
    public static final String COLUMNNAME_OF_MQ = "mq";
    // default_variant.mq0
    public static final int INDEX_OF_MQ0 = 26;
    public static final ColumnType TYPE_OF_MQ0 = ColumnType.INTEGER;
    public static final int LENGTH_OF_MQ0 = 11;
    public static final String COLUMNNAME_OF_MQ0 = "mq0";
    // default_variant.ns
    public static final int INDEX_OF_NS = 27;
    public static final ColumnType TYPE_OF_NS = ColumnType.INTEGER;
    public static final int LENGTH_OF_NS = 11;
    public static final String COLUMNNAME_OF_NS = "ns";
    // default_variant.sb
    public static final int INDEX_OF_SB = 28;
    public static final ColumnType TYPE_OF_SB = ColumnType.FLOAT;
    public static final int LENGTH_OF_SB = -1;
    public static final String COLUMNNAME_OF_SB = "sb";
    // default_variant.somatic
    public static final int INDEX_OF_SOMATIC = 29;
    public static final ColumnType TYPE_OF_SOMATIC = ColumnType.INTEGER;
    public static final int LENGTH_OF_SOMATIC = 1;
    public static final String COLUMNNAME_OF_SOMATIC = "somatic";
    // default_variant.validated
    public static final int INDEX_OF_VALIDATED = 30;
    public static final ColumnType TYPE_OF_VALIDATED = ColumnType.INTEGER;
    public static final int LENGTH_OF_VALIDATED = 1;
    public static final String COLUMNNAME_OF_VALIDATED = "validated";


    private void addColumns() {
        addColumn(COLUMNNAME_OF_UPLOAD_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_FILE_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_VARIANT_ID, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_DNA_ID, ColumnType.VARCHAR, 100);
        addColumn(COLUMNNAME_OF_CHROM, ColumnType.VARCHAR, 5);
        addColumn(COLUMNNAME_OF_POSITION, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_DBSNP_ID, ColumnType.VARCHAR, 45);
        addColumn(COLUMNNAME_OF_REF, ColumnType.VARCHAR, 30);
        addColumn(COLUMNNAME_OF_ALT, ColumnType.VARCHAR, 30);
        addColumn(COLUMNNAME_OF_QUAL, ColumnType.FLOAT, 10);
        addColumn(COLUMNNAME_OF_FILTER, ColumnType.VARCHAR, 500);
        addColumn(COLUMNNAME_OF_VARIANT_TYPE, ColumnType.VARCHAR, 10);
        addColumn(COLUMNNAME_OF_ZYGOSITY, ColumnType.VARCHAR, 20);
        addColumn(COLUMNNAME_OF_GT, ColumnType.VARCHAR, 10);
        addColumn(COLUMNNAME_OF_CUSTOM_INFO, ColumnType.VARCHAR, 10000);
        addColumn(COLUMNNAME_OF_AA, ColumnType.VARCHAR, 500);
        addColumn(COLUMNNAME_OF_AC, ColumnType.VARCHAR, 500);
        addColumn(COLUMNNAME_OF_AF, ColumnType.VARCHAR, 500);
        addColumn(COLUMNNAME_OF_AN, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_BQ, ColumnType.FLOAT, -1);
        addColumn(COLUMNNAME_OF_CIGAR, ColumnType.VARCHAR, 500);
        addColumn(COLUMNNAME_OF_DB, ColumnType.INTEGER, 1);
        addColumn(COLUMNNAME_OF_DP, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_END, ColumnType.VARCHAR, 500);
        addColumn(COLUMNNAME_OF_H2, ColumnType.INTEGER, 1);
        addColumn(COLUMNNAME_OF_MQ, ColumnType.VARCHAR, 500);
        addColumn(COLUMNNAME_OF_MQ0, ColumnType.VARCHAR, 500);
        addColumn(COLUMNNAME_OF_NS, ColumnType.INTEGER, 11);
        addColumn(COLUMNNAME_OF_SB, ColumnType.VARCHAR, 500);
        addColumn(COLUMNNAME_OF_SOMATIC, ColumnType.INTEGER, 1);
        addColumn(COLUMNNAME_OF_VALIDATED, ColumnType.INTEGER, 1);
    }
}
