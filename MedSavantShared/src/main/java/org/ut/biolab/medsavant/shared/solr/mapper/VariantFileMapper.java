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

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableVariantFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handle the mapping of variant files.
 */
public class VariantFileMapper implements ResultMapper<SimpleVariantFile> {

    @Override
    public List<SimpleVariantFile> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableVariantFile> variantFiles = binder.getBeans(SearcheableVariantFile.class, solrDocumentList);
        return toModelList(variantFiles);
    }

    @Override
    public List<SimpleVariantFile> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<SimpleVariantFile> toModelList(List<SearcheableVariantFile> variantFiles) {
        List<SimpleVariantFile> simpleVariantFiles = new ArrayList<SimpleVariantFile>(variantFiles.size());
        for (SearcheableVariantFile searcheableVariantFile : variantFiles) {
            simpleVariantFiles.add(searcheableVariantFile.getVariantFile());
        }
        return simpleVariantFiles;
    }
}
