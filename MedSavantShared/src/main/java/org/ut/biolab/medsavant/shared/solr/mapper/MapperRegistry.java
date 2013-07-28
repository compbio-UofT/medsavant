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
package org.ut.biolab.medsavant.shared.solr.mapper;

/**
 * Mapper registry. Maintains references to all types of mappers.
 */
public class MapperRegistry {

    private static VariantMapper variantMapper;

    private static VariantCommentMapper variantCommentMapper;

    private static ResultRowMapper resultRowMapper;

    /**
     * Get the appropriate mapper based on the entity name.
     * @param entity    The name of the entity
     * @return          The appropriate mapper.
     */
    public static ResultMapper getMapper(String entity) {

        if ("variant".equals(entity)) {
            return getVariantMapper();
        } else if ("comment".equals(entity)) {
            return getVariantCommentMapper();
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
     * Return the variant comment mapper instances. Creates one if it doesn't yet exist.
     * @return          The variant comment mapper instance.
     */
    private static VariantCommentMapper getVariantCommentMapper() {

        if (variantCommentMapper == null) {
            variantCommentMapper = new VariantCommentMapper();
        }
        return variantCommentMapper;
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
