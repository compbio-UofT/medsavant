package org.ut.biolab.medsavant.shared.model.solr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Field mappings between MedSavant field names and solr field names.
 */
public class FieldMappings {

    public static String mapToSolrField(String value, String entityName) {
        Map<String, String> mappings = FieldMappings.getMappings(entityName);

        String solrFieldName = mappings.get(value);
        return (solrFieldName == null) ? value : solrFieldName;
    }

    public static Map<String, String> getMappings(String entity) {
        Map<String, String> result = null;

        if ("variant".equals(entity)) {
            result = VARIANT_FIELDS;
        }

        return result;
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

    }});

}
