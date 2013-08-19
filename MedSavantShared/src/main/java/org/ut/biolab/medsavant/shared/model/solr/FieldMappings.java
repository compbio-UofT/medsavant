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
package org.ut.biolab.medsavant.shared.model.solr;

import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.model.*;
import org.ut.biolab.medsavant.shared.util.Entity;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Field mappings between MedSavant field names and solr field names.
 */
public class FieldMappings {

    public static String mapToSolrCore(String entityName) {

        String coreName;
        if (ENTITY_CORE_MAP.containsKey(entityName)) {
            coreName = ENTITY_CORE_MAP.get(entityName);
        } else {
            coreName = entityName;
        }

        return coreName;
    }

    public static String mapToSolrField(String value, String entityName) {
        Map<String, String> mappings = FieldMappings.getMappings(entityName);

        String s = null;
        if (mappings != null) {
            String solrFieldName = mappings.get(value);
            s =  (solrFieldName == null) ? value : solrFieldName;
        } else {
            s = value;
        }

        return s;
    }

    public static Map<String, String> getMappings(String entity) {
        Map<String, String> result = null;

        if (Entity.VARIANT.equals(entity)) {
            result = VARIANT_FIELDS;
        }

        return result;
    }

    public static String getClassName(String entityName) {
        String className = entityName;
        if (ENTITY_CLASS_NAMES.containsKey(entityName)) {
         className = ENTITY_CLASS_NAMES.get(entityName);
        }
        return className;
    }

    //FIXME move to a property file maybe
    private static final Map<String, String> VARIANT_FIELDS = Collections.unmodifiableMap( new HashMap<String, String>() {{
        put("position", "pos");
        put("dbnsp_id", "id");
        put("custom_info", "info");
        put("aa", "INFO_AA");
        put("ac", "INFO_AC");
        put("af", "INFO_AF");
        put("an", "INFO_AN");
        put("bq", "INFO_BQ");
        put("cigar", "INFO_CIGAR");
        put("db", "INFO_DB");
        put("dp", "INFO_DP");
        put("end", "INFO_END");
        put("h2", "INFO_H2");
        put("mq", "INFO_MQ");
        put("mq0", "INFO_MQ0");
        put("ns", "INFO_NS");
        put("sb", "INFO_SB");
        put("somatic", "INFO_SOMATIC");
        put("validated", "INFO_vALIDATED");
    }}
    );

    private static final Map<String, String> ENTITY_CORE_MAP = Collections.unmodifiableMap(
        new HashMap<String, String> () {{
            put("regionset", "region_set");
            put("genomicregion", "genomic_region");
            put("variantfile", "variant_file");
            put("ontologyterm", "ontology_term");
            put("annotationlog", "annotation_log");
            put("generallog", "general_log");
            put("annotationformat", "annotation_format");
            put("annotationcolumn", "annotation_column");
        }}
    );

    private static final Map<String, String> ENTITY_CLASS_NAMES = Collections.unmodifiableMap(
            new HashMap<String, String> () {{
                put(Entity.ANNOTATION, Annotation.class.getName());
                put(Entity.ANNOTATION_COLUMN, AnnotatedColumn.class.getName());
                put(Entity.ANNOTATION_FORMAT, AnnotationFormat.class.getName());
                put(Entity.ANNOTATION_LOG, AnnotationLog.class.getName());
                put(Entity.VARIANT, VariantRecord.class.getName());
                put(Entity.VARIANT_FILE, SimpleVariantFile.class.getName());
                put(Entity.COMMENT, VariantComment.class.getName());
                put(Entity.CHROMOSOME, Chromosome.class.getName());
                put(Entity.GENE, Gene.class.getName());
                put(Entity.GENE_SET, GeneSet.class.getName());
                put(Entity.COHORT, Cohort.class.getName());
                put(Entity.PATIENT, Patient.class.getName());
                put(Entity.ONTOLOGY, Ontology.class.getName());
                put(Entity.ONTOLOGY_TERM, OntologyTerm.class.getName());
                put(Entity.REGION_SET, RegionSet.class.getName());
                put(Entity.GENOMIC_REGION, GenomicRegion.class.getName());
                put(Entity.REFERENCE, Reference.class.getName());
                put(Entity.PROJECT, ProjectDetails.class.getName());
                put(Entity.USER, User.class.getName());
                put(Entity.SETTING, Setting.class.getName());
                put(Entity.REFERENCE, Reference.class.getName());
                put(Entity.GENERAL_LOG, GeneralLog.class.getName());
            }}
    );

}
