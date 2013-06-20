/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.solr.service;

import org.apache.commons.lang3.ArrayUtils;
import org.ut.biolab.medsavant.server.vcf.VCFParser;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.util.*;

/**
 * Encapsulte the processing logic for VCF column values.
 *
 * @version $Id $
 */
public enum VCFColumn {



    /** CHROM column.  */
    CHROM,
    /** POS column.  */
    POS,
    /** REF column.  */
    REF,
    /** QUAL column.  */
    QUAL,
    /** FILTER column.  */
    FILTER,
    /** ID column.  */
    ID {
        @Override
        public List<VariantData> process(String fieldValue, List<VariantData> variantDataList) {
            String name = name().toLowerCase(Locale.ROOT);

            for (VariantData variantData: variantDataList) {
                processMultiValueField(fieldValue, name, MULTI_VALUE_SEPARATOR, variantData);
            }

            return variantDataList;
        }
    },
    /** ALT column.  */
    ALT {
        @Override
        public List<VariantData> process(String fieldValue, List<VariantData> variantDataList) {
            String name = name().toLowerCase(Locale.ROOT);

            for (VariantData variantData : variantDataList) {
                processMultiValueField(fieldValue, name, ALT_VALUE_SEPARATOR, variantData);
            }

            return variantDataList;
        }
    },
    /** INFO column.  */
    INFO {
        @Override
        public List<VariantData> process(String fieldValue, List<VariantData> variantDataList) {
            String name = name().toLowerCase(Locale.ROOT);

            for (VariantData variantData : variantDataList) {
                //FIXME might not be necessary
                processSingleValueField(fieldValue, "custom_info",variantData);

                processInfoValueFields(fieldValue, name, MULTI_VALUE_SEPARATOR, variantData);
            }

            return variantDataList;
        }
    },
    FORMAT {
        public List<VariantData> process(String fieldValue, List<VariantData> variantDataList) {

            //ignore field value for now
            return variantDataList;
        }
    },
    PATIENT {
        @Override
        public List<VariantData> process(String fieldValue, List<VariantData> variantDataList) {
            String[] dnaIds = getPatientIds().toArray(new String[] {});
            String[] fieldValues = fieldValue.trim().split("\t");

            List<VariantData> tempVariantDataList = new ArrayList<VariantData>();
            //ToDo refactor
            if (fieldValues != null && dnaIds != null && variantDataList.size() > 0) {

                for (int i = 0; i < fieldValues.length; i++) {
                    String field = fieldValues[i];
                    String dnaId = dnaIds[i];
                    VariantData variantData = processPatientDataFields(field, dnaId, PATIENT_ID_FIELD_SEPARATOR, variantDataList.get(i));
                    if (variantData != null) {
                        tempVariantDataList.add(variantData);
                    }

                }
            }

            return tempVariantDataList;
        }
    }

       ;
    public static long homoRefCount = 0;

    /** Separator for ALT valued fields. */
    private static final String ALT_VALUE_SEPARATOR = ",";

    /** Separator for INFO multi valued fields. */
    private static final String INFO_FIELD_VALUE_SEPARATOR = "=";

    /** Separator for multi valued fields. */
    private static final String MULTI_VALUE_SEPARATOR = ";";

    /** Separator for INFO Solr field names. */
    private static final String INFO_FIELD_INNER_SEPARATOR = "_";

    /** Seprator for patient dna sample id*/
    private static final String PATIENT_ID_FIELD_SEPARATOR = ":";


    /** Default value for INFO Solr document fields. */
    private static final String INFO_FIELD_DEFAULT_VALUE = "1";

    private List<String> patientIds;


    VCFColumn(List<String> patientId) {
        this.patientIds = patientId;
    }

    VCFColumn() {};

    /**
     * Process a column field value and add the value to the Variant Data object.
     *
     * This method considers the fieldValue contains a single value. This method is overridden for
     * fields that need to be parsed in a different manner.
     *
     * @param fieldValue        The column field value.
     * @param variantDataList       The Variant Data object with the column value added.
     * @return The Variant Data updated with the column value.
     */
    public List<VariantData> process(String fieldValue, List<VariantData> variantDataList) {
        String name = name().toLowerCase(Locale.ROOT);

        for (VariantData variantData : variantDataList) {
            processSingleValueField(fieldValue, name, variantData);
        }

        return variantDataList;
    }

    /**
     * Save value for a single valued field.
     * @param value     value
     * @param key       key
     * @param variantData  variant data structure
     * @return variant data updated with the column value.
     */
    private static VariantData processSingleValueField(String value,
                                                       String key,
                                                       VariantData variantData) {

        variantData.addTo(key, value);

        return variantData;
    }

    /**
     * Parse and save values for multiple valued fields.
     * @param value     the value
     * @param key       key
     * @param separator separator for multiple values
     * @param variantData  variant data structure for storing data
     * @return variant data structure updated with the column values.
     */
    private static VariantData processMultiValueField(String value,
                                                      String key,
                                                      String separator,
                                                      VariantData variantData) {
        String[] terms = value.split(separator);

        List<String> multipleValues = Arrays.asList(terms);

        variantData.addTo(key, multipleValues);

        return variantData;
    }

    /**
     * Parse and save values for info   fields.
     * @param value     the value
     * @param key       key
     * @param separator separator for multiple values
     * @param variantData  variant data structure for storing data
     * @return  variant data structure updated with the info column values.
     */
    private static VariantData processInfoValueFields(String value,
                                                      String key,
                                                      String separator,
                                                      VariantData variantData) {
        String[] terms = value.split(separator);

        for (String term: terms) {
            String[] innerTerms = term.split(INFO_FIELD_VALUE_SEPARATOR);

            //compare to the list of reserved value and only index those fields that are in the reserved words
            if (ArrayUtils.contains(VariantData.INFO_RESERVED, innerTerms[0])) {

                String specificKey = key + INFO_FIELD_INNER_SEPARATOR + innerTerms[0];

                if (innerTerms.length > 1) {
                    processMultiValueField(innerTerms[1], specificKey, ALT_VALUE_SEPARATOR, variantData);
                } else {
                    processSingleValueField(INFO_FIELD_DEFAULT_VALUE, specificKey, variantData);
                }
            }
        }

        return variantData;
    }

    private static VariantData processPatientDataFields(String value,
                                                        String key,
                                                        String separator,
                                                        VariantData variantData) {
        String[] terms = value.split(separator);



        if (terms != null && terms.length > 0) {
            String genotype = terms[0];


            VariantRecord.Zygosity zygosity = VCFParser.calculateZygosity(genotype);

            //if (zygosity != null && zygosity != VariantRecord.Zygosity.HomoRef) {
            if (zygosity != null) {
                processSingleValueField(key, VariantData.DNA_ID, variantData);
                processSingleValueField(genotype, VariantData.GENOTYPE, variantData);
                processSingleValueField(zygosity.toString(), VariantData.ZYGOSITY.toString(), variantData);
            } else {
                variantData = null;
            }
        } else {
            variantData = null;
        }


        return variantData;
    }

    public List<String> getPatientIds() {
        return patientIds;
    }

    public void setPatientIds(List<String> patientIds) {
        this.patientIds = patientIds;
    }

    public void addPatientID(String patientId) {
        if (patientIds == null) {
            patientIds = new ArrayList<String>();
        }

        patientIds.add(patientId);
    }

    public VariantRecord.VariantType getVariantType(String ref, String alt) {
        if(ref.startsWith("<") || alt.startsWith("<")){
            if(alt.contains("<DEL>") || ref.contains("<DEL>")){
                return VariantRecord.VariantType.Deletion;
            } else if (alt.contains("<INS>") || ref.contains("<INS>")){
                return VariantRecord.VariantType.Insertion;
            } else {
                return VariantRecord.VariantType.Unknown;
            }
        }

        VariantRecord.VariantType result = null;
        for(String s : alt.split(",")){
            if(ref == null || (s != null && s.length() > ref.length())){
                result = variantTypeHelper(result, VariantRecord.VariantType.Insertion);
            } else if (s == null || (ref != null && ref.length() > s.length())){
                result = variantTypeHelper(result, VariantRecord.VariantType.Deletion);
            } else {
                result = variantTypeHelper(result, VariantRecord.VariantType.SNP);
            }
        }
        return result;
    }

    private VariantRecord.VariantType variantTypeHelper(VariantRecord.VariantType currentType, VariantRecord.VariantType newType){
        if(currentType == null || currentType == newType){
            return newType;
        } else {
            return VariantRecord.VariantType.Various;
        }
    }

}
