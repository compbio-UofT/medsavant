package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.model.VariantComment;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableAnnotationLog;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableVariantComment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Maps annotation log events.
 */
public class AnnotationLogMapper implements ResultMapper<AnnotationLog> {

    @Override
    public List<AnnotationLog> map(SolrDocumentList solrDocumentList) {

        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableAnnotationLog> annotationLogs = binder.getBeans(SearcheableAnnotationLog.class, solrDocumentList);
        return toModelList(annotationLogs);
    }

    @Override
    public List<AnnotationLog> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<AnnotationLog> toModelList(List<SearcheableAnnotationLog> searcheableAnnotationLogList) {
        List<AnnotationLog> annotationLogList = new ArrayList<AnnotationLog>(searcheableAnnotationLogList.size());

        for (SearcheableAnnotationLog searcheableAnnotationLog : searcheableAnnotationLogList) {
            annotationLogList.add((searcheableAnnotationLog.getLog()));
        }

        return annotationLogList;
    }
}
