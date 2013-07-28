package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.VariantComment;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableVariant;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableVariantComment;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Maps SearcheableVariantComment instances to VariantComment
 */
public class VariantCommentMapper implements ResultMapper<VariantComment> {

    @Override
    public List<VariantComment> map(SolrDocumentList solrDocumentList) {

        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableVariantComment> searcheableVariantCommentList = binder.getBeans(SearcheableVariantComment.class, solrDocumentList);

        return toModelList(searcheableVariantCommentList);
    }

    @Override
    public List<VariantComment> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private List<VariantComment> toModelList(List<SearcheableVariantComment> searcheableVariantCommentList) {
        List<VariantComment> variantCommentList = new ArrayList<VariantComment>(searcheableVariantCommentList.size());

        for (SearcheableVariantComment searcheableVariantComment : searcheableVariantCommentList) {
            variantCommentList.add(searcheableVariantComment.getVariantComment());
        }

        return variantCommentList;
    }
}
