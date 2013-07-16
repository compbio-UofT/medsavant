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

import org.apache.solr.common.SolrDocumentList;

import java.util.List;

/**
 * Generic interface for mappers.
 * @param <T>       The entity class for the mapper.
 */
public interface ResultMapper<T> {

    /**
     * Convert a list of class T entities
     * @param solrDocumentList
     * @return
     */
    public List<T> map(SolrDocumentList solrDocumentList);
}
