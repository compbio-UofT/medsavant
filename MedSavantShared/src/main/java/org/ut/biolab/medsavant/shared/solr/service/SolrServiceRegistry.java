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

import org.ut.biolab.medsavant.shared.model.solr.SearcheableVariantComment;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;


/**
 * Keep track of references of Solr services.
 */
public class SolrServiceRegistry {


    private static AbstractSolrService variantService;

    private static AbstractSolrService variantCommentService;

    /**
     * Get the appropriate solr service based on the entity string.
     * @param entityName            The entity name.
     * @return                      An instance of a solr service.
     */
    public static AbstractSolrService getService(String entityName) throws InitializationException {

        AbstractSolrService service = null;

        if ("variant".equals(entityName)) {
            return getVariantService();
        } else if ("comment".equals(entityName)) {
            return getVariantCommentService();
        }

        return null;
    }

    public static AbstractSolrService getService(Class clazz) throws InitializationException {

        AbstractSolrService service = null;

        if (SearcheableVariantComment.class.getName().equals(clazz.getName())) {
            return getVariantCommentService();
        }

        return null;
    }

    private static AbstractSolrService getVariantService() throws InitializationException {
        if (variantService == null) {
            variantService =  new VariantService();
            variantService.initialize();
        }
        return variantService;
    }

    private static AbstractSolrService getVariantCommentService() throws InitializationException {
        if (variantCommentService == null) {
            variantCommentService =  new VariantCommentService();
            variantCommentService.initialize();
        }
        return variantCommentService;
    }

}
