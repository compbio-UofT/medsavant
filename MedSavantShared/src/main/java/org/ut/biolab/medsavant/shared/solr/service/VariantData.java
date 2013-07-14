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
package org.ut.biolab.medsavant.shared.solr.service;


import org.ut.biolab.medsavant.shared.solr.map.ListMap;

import java.util.Collection;
import java.util.UUID;


/**
 * Used to store term values for a document.
 * @version $Id$
 */
public class VariantData extends ListMap<String, String>
{

    /** Solr ID fields. */
    public static final String UUID_FIELD = "uuid";

    /** CHROM fields. */
    public static final String CHROM = "chrom";

    /** POS fields. */
    public static final String POS = "pos";

    /** ID fields. */
    public static final String ID = "id";

    /** REF fields. */
    public static final String REF = "ref";

    /** ALT fields. */
    public static final String ALT = "alt";

    /** QUAL fields. */
    public static final String QUAL = "qual";

    /** FILTER fields. */
    public static final String FILTER = "filter";

    /** INFO fields. */
    public static final String INFO = "info";

    /** DNA ID field */
    public static final String DNA_ID = "dna_id";

    /** Genotype field */
    public static final String GENOTYPE = "gt";

    /** Zygosity field */
    public static final String ZYGOSITY = "zygosity";

    /** Reserved info fields. */
    public static final String[] INFO_RESERVED  = {"AA", "AC", "AF", "AN", "BQ", "CIGAR",
        "DB", "DP", "END", "H2", "H3", "MQ", "MQ0", "NS", "SB", "SOMATIC", "VALIDATED", "1000G"};

    /** Id of opinion. */
    private String id;

    /**
     * Add UUID to the variant data, sine the variant id cannot be used as a primary key.
     * @return   A randomly generated UUID
     */
    public UUID addUUID() {
        UUID uuid = UUID.randomUUID();
        addTo(UUID_FIELD, uuid.toString());

        return uuid;
    }

    /**
     * Add field to Term Data.
     * @param key           key
     * @param value         value
     * @return  true if addition successful, false otherwise
     */
    @Override
    public boolean addTo(String key, String value)
    {
        return super.addTo(key, value);
    }

    /**
     * Add multi valued field.
     * @param key           key
     * @param values        value
     * @return  true if addition successful, false otherwise
     */
    @Override
    public boolean addTo(String key, Collection<String> values)
    {
        boolean result = true;
        for (String value : values) {
            result &= this.addTo(key, value);
        }
        return result;
    }


    /**
     * Getter for id.
     * @return  the id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for id .
     * @param id    the id
     */
    public void setId(String id) {
        this.id = id;
    }

    public void clearDnaSampleData() {

        //FIXME maybe do it in one list traversal only
        remove("uuid");
        remove("dna_id");
        remove("gt");
        remove("zygosity");
    }

}
