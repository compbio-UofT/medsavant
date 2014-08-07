/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.variantstorage.impl.schemas;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;

/**
 *
 * @author jim
 */
public class DBSchemas {
    public static final DbSchema schema = (new DbSpec()).addDefaultSchema();
    public static final SettingsTableSchema SettingsTableSchema = new SettingsTableSchema(schema);
    public static final VariantTablemapTableSchema VarianttablemapTableSchema = new VariantTablemapTableSchema(schema);    
    public static final VariantFileTableSchema VariantFileTableSchema = new VariantFileTableSchema(schema);
    public static final VariantFileTableSchema VariantFileIBTableSchema = new VariantFileTableSchema(schema, "_ib");
    public static final VariantPendingUpdateTableSchema VariantpendingupdateTableSchema = new VariantPendingUpdateTableSchema(schema);
    public static final AnnotationFormatTableSchema AnnotationFormatTableSchema = new AnnotationFormatTableSchema(schema);
    //This stores which VCF fields are active for the current (database, project, reference, update).
    public static final VariantFormatTableSchema VariantformatTableSchema = new VariantFormatTableSchema(schema);
}
