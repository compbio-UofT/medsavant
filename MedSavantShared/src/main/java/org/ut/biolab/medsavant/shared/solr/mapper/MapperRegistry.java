package org.ut.biolab.medsavant.shared.solr.mapper;

import org.ut.biolab.medsavant.shared.model.solr.SearcheableVariant;

/**
 * Mapper registry. Maintains references to all types of mappers.
 */
public class MapperRegistry {

    private static VariantMapper variantMapper;

    private static ResultRowMapper resultRowMapper;

    /**
     * Get the appropriate mapper based on the entity name.
     * @param entity    The name of the entity
     * @return          The appropriate mapper.
     */
    public static ResultMapper getMapper(String entity) {

        if ("variant".equals(entity)) {
            return getVariantMapper();
        }

        return null;
    }

    /**
     * Return the variant mapper instances. Creates one if it doesn't yet exist.
     * @return          The variant mapper instance.
     */
    private static VariantMapper getVariantMapper() {

        if (variantMapper == null) {
            variantMapper = new VariantMapper();
        }
        return variantMapper;
    }

    /**
     * Return the ResultRow mapper instances. Creates one if it doesn't yet exist.
     * @return          The ResultRow mapper instance.
     */
    public static ResultRowMapper getResultRowMapper() {
        if (resultRowMapper == null) {
            resultRowMapper = new ResultRowMapper();
        }
        return resultRowMapper;
    }

}
