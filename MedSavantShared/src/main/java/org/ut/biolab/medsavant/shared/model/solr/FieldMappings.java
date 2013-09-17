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
import org.ut.biolab.medsavant.shared.solr.service.VariantData;
import org.ut.biolab.medsavant.shared.util.Entity;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Field mappings between MedSavant field names and org.ut.biolab.medsavant.persistence.query.solr.solr field names.
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

    public static Class getSolrClass(Class clazz) {
        Class solrClass = clazz;
        if (CLASS_MAPPINGS.containsKey(clazz)) {
            solrClass = CLASS_MAPPINGS.get(clazz);
        }
        return solrClass;
    }

    //FIXME move to a property file maybe
    private static final Map<String, String> VARIANT_FIELDS = Collections.unmodifiableMap( new HashMap<String, String>() {{
        put("position", "pos");
        put("dbsnp_id", "id");
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
        put("validated", "INFO_VALIDATED");
    }}
    );

    private static final Map<String, String> ENTITY_CORE_MAP = Collections.unmodifiableMap(
        new HashMap<String, String> () {{
            put("regionset", "region_set");
            put("genomicregion", "genomic_region");
            put("geneset", "gene_set");
            put("variantfile", "variant_file");
            put("varianttag", "variant_tag");
            put("ontologyterm", "ontology_term");
            put("annotationlog", "annotation_log");
            put("generallog", "general_log");
            put("annotationformat", "annotation_format");
            put("annotationcolumn", "annotation_column");
            put("customcolumn", "custom_column");
        }}
    );

    private static final Map<String, String> ENTITY_CLASS_NAMES = Collections.unmodifiableMap(
            new HashMap<String, String> () {{
                put(Entity.ANNOTATION, SearcheableAnnotation.class.getName());
                put(Entity.ANNOTATION_COLUMN, SearcheableAnnotatedColumn.class.getName());
                put(Entity.ANNOTATION_FORMAT, SearcheableAnnotationFormat.class.getName());
                put(Entity.ANNOTATION_LOG, SearcheableAnnotationLog.class.getName());
                put(Entity.VARIANT, SearcheableVariant.class.getName());
                put(Entity.VARIANT_FILE, SearcheableVariantFile.class.getName());
                put(Entity.VARIANT_TAG, SearcheableVariantTag.class.getName());
                put(Entity.COMMENT, SearcheableVariantComment.class.getName());
                put(Entity.CHROMOSOME, SearcheableChromosome.class.getName());
                put(Entity.GENE, SearcheableGene.class.getName());
                put(Entity.GENE_SET, SearcheableGeneSet.class.getName());
                put(Entity.COHORT, SearcheableCohort.class.getName());
                put(Entity.PATIENT, SearcheablePatient.class.getName());
                put(Entity.ONTOLOGY, SearcheableOntology.class.getName());
                put(Entity.ONTOLOGY_TERM, SearcheableOntologyTerm.class.getName());
                put(Entity.REGION_SET, SearcheableRegionSet.class.getName());
                put(Entity.GENOMIC_REGION, SearcheableGenomicRegion.class.getName());
                put(Entity.REFERENCE, SearcheableReference.class.getName());
                put(Entity.PROJECT, SearcheableProjectDetails.class.getName());
                put(Entity.USER, SearcheableUser.class.getName());
                put(Entity.SETTING, SearcheableSetting.class.getName());
                put(Entity.REFERENCE, SearcheableReference.class.getName());
                put(Entity.GENERAL_LOG, SearcheableGeneralLog.class.getName());
                put(Entity.CUSTOM_COLUMN, SearcheableCustomColumn.class.getName());
            }}
    );

    private static final Map<Class, Class> CLASS_MAPPINGS = Collections.unmodifiableMap(
            new HashMap<Class, Class> () {{
                put(Annotation.class, SearcheableAnnotation.class);
                put(AnnotatedColumn.class, SearcheableAnnotatedColumn.class);
                put(AnnotationFormat.class, SearcheableAnnotationFormat.class);
                put(AnnotationLog.class, SearcheableAnnotationLog.class);
                put(VariantData.class, SearcheableVariant.class);
                put(VariantRecord.class, SearcheableVariant.class);
                put(SimpleVariantFile.class, SearcheableVariantFile.class);
                put(VariantTag.class, SearcheableVariantTag.class);
                put(VariantComment.class, SearcheableVariantComment.class);
                put(Chromosome.class, SearcheableChromosome.class);
                put(Gene.class, SearcheableGene.class);
                put(GeneSet.class, SearcheableGeneSet.class);
                put(Cohort.class, SearcheableCohort.class);
                put(Patient.class, SearcheablePatient.class);
                put(Ontology.class, SearcheableOntology.class);
                put(OntologyTerm.class, SearcheableOntologyTerm.class);
                put(RegionSet.class, SearcheableRegionSet.class);
                put(GenomicRegion.class, SearcheableGenomicRegion.class);
                put(Reference.class, SearcheableReference.class);
                put(ProjectDetails.class, SearcheableProjectDetails.class);
                put(User.class, SearcheableUser.class);
                put(Setting.class, SearcheableSetting.class);
                put(Reference.class, SearcheableReference.class);
                put(GeneralLog.class, SearcheableGeneralLog.class);
                put(CustomColumn.class, SearcheableCustomColumn.class);
            }}
    );

}
