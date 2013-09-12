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
package org.ut.biolab.medsavant.shard.mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator of Hibernate mappings for variants;
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantMappingGenerator implements MappingProvider {
    // constants
    private static final String DOCTYPE = "<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD//EN\" \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">";
    private static final String PACK_TEMPLATE = "<hibernate-mapping package=\"%s\">";
    private static final String CL_TEMPLATE = "<class name=\"%s\" ";
    private static final String TABLE_TEMPLATE = "table=\"%s\">";
    private static final String FILE_END = "</class></hibernate-mapping>";

    private static final String[] DEFAULT_ID = new String[] { "position", "position", "integer" };
    private static final String[] DEFAULT_PROPERTY_NAMES = new String[] { "variant_id", "upload_id", "file_id", "dna_id", "chrom", "dbsnp_id", "ref", "alt", "qual", "filter",
            "variant_type", "zygosity", "gt", "custom_info", "aa", "ac", "af", "an", "bq", "cigar", "db", "dp", "end", "h2", "mq", "mq0", "ns", "sb", "somatic", "validated" };
    private static final String[] DEFAULT_PROPERTY_COLUMNS = new String[] { "variant_id", "upload_id", "file_id", "dna_id", "chrom", "dbsnp_id", "ref", "alt", "qual", "filter",
            "variant_type", "zygosity", "gt", "custom_info", "aa", "ac", "af", "an", "bq", "cigar", "db", "dp", "end", "h2", "mq", "mq0", "ns", "sb", "somatic", "validated" };
    private static final String[] DEFAULT_PROPERTY_TYPES = new String[] { "integer", "integer", "integer", "string", "string", "string", "string", "string", "float", "string",
            "string", "string", "string", "string", "string", "integer", "float", "integer", "float", "string", "boolean", "integer", "integer", "boolean", "float", "integer",
            "integer", "float", "boolean", "boolean" };

    private static final String DEFAULT_PACKAGE = "org.ut.biolab.medsavant.shard.variant";
    private static final String DEFAULT_CLASS = "Variant";
    private static final String DEFAULT_TABLE = "z_variant_proj1_ref3_update4";

    // variables
    private static VariantMappingGenerator instance = null;

    private String pack;
    private String cl;
    private String table;
    private MappingProperty id;
    private List<MappingProperty> properties;

    protected VariantMappingGenerator() {
        pack = DEFAULT_PACKAGE;
        cl = DEFAULT_CLASS;
        table = DEFAULT_TABLE;
        id = new MappingProperty(DEFAULT_ID[0], DEFAULT_ID[1], DEFAULT_ID[2], true);

        properties = new ArrayList<MappingProperty>();
        for (int i = 0; i < DEFAULT_PROPERTY_NAMES.length; i++) {
            properties.add(new MappingProperty(DEFAULT_PROPERTY_NAMES[i], DEFAULT_PROPERTY_COLUMNS[i], DEFAULT_PROPERTY_TYPES[i], false));
        }
    }

    public static VariantMappingGenerator getInstance() {
        if (instance == null) {
            instance = new VariantMappingGenerator();
        }

        return instance;
    }

    public String getClassName() {
        return cl;
    }

    @Override
    public void setClassName(String pack, String clazz) {
        this.pack = pack;
        this.cl = clazz;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public void addProperty(String propertyName, String columnName, String propertyType) {
        properties.add(new MappingProperty(propertyName, columnName, propertyType, false));
    }

    @Override
    public List<MappingProperty> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(List<MappingProperty> properties) {
        this.properties = properties;
    }

    @Override
    public String getMapping() {
        StringBuffer s = new StringBuffer();
        s.append(DOCTYPE);
        s.append(String.format(PACK_TEMPLATE, pack));
        s.append(String.format(CL_TEMPLATE, cl));
        s.append(String.format(TABLE_TEMPLATE, table));

        s.append(id.toString());
        for (MappingProperty p : properties) {
            s.append(p.toString());
        }

        s.append(FILE_END);

        return s.toString();
    }

    public MappingProperty getId() {
        return id;
    }

    public void setId(MappingProperty id) {
        this.id = id;
    }

}
