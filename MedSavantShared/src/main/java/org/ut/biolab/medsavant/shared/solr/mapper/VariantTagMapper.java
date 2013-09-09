package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.VariantTag;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableVariantTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Handle mapping of Variant tags.
 */
public class VariantTagMapper implements ResultMapper<VariantTag> {

    @Override
    public List<VariantTag> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableVariantTag> searcheableVariantTags = binder.getBeans(SearcheableVariantTag.class, solrDocumentList);
        return toModelList(searcheableVariantTags);
    }

    @Override
    public List<VariantTag> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<VariantTag> toModelList(List<SearcheableVariantTag> searcheableVariantTags) {
        List<VariantTag> variantTagList = new ArrayList<VariantTag>();
        for (SearcheableVariantTag searcheableVariantTag : searcheableVariantTags) {
            variantTagList.add(searcheableVariantTag.getTag());
        }
        return variantTagList;
    }
}
