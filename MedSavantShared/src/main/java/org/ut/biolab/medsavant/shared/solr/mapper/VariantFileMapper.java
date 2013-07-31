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
