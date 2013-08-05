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

import org.ut.biolab.medsavant.shared.util.Entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Field mappings between MedSavant field names and solr field names.
 */
public class FieldMappings {

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
