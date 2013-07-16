package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableVariant;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps SearcheableVariant instances to VariantRecord
 */
public class VariantMapper implements ResultMapper<VariantRecord> {

    @Override
    public List<VariantRecord> map(SolrDocumentList solrDocumentList) {

        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableVariant> searcheableVariantList = binder.getBeans(SearcheableVariant.class, solrDocumentList);

        return toModelList(searcheableVariantList);
    }

    private List<VariantRecord> toModelList(List<SearcheableVariant> searcheableVariantList) {
        List<VariantRecord> variantRecordList = new ArrayList<VariantRecord>(searcheableVariantList.size());

        for (SearcheableVariant searcheableVariant : searcheableVariantList) {
            variantRecordList.add(searcheableVariant.getVariantRecord());
        }

        return variantRecordList;
    }
}
