/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package medsavant.db;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import medsavant.db.table.TableSchema;
import medsavant.db.table.VariantTable;

/**
 *
 * @author mfiume
 */
public class Database {

    private final DbSpec spec;
    private final DbSchema schema;
    private static Database instance;

    private VariantTable variantTableSchema;

    public static void main(String[] argv) {
        getInstance();
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public DbSchema getSchema() {
        return schema;
    }

    public Database() {
        spec = new DbSpec();
        schema = spec.addDefaultSchema();
        initTableSchemas();
    }

    private void initTableSchemas() {
        variantTableSchema = new VariantTable(schema);
    }

    public TableSchema getVariantTableSchema() {
        return this.variantTableSchema;
    }
}
