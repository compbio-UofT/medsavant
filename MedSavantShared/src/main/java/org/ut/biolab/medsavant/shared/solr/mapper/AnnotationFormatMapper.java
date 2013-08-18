package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableAnnotationFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping of Solr documents to AnnotationFormat entities
 */
public class AnnotationFormatMapper implements ResultMapper<AnnotationFormat> {

    @Override
    public List<AnnotationFormat> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableAnnotationFormat>  searcheableAnnotationFormatList = binder.getBeans(SearcheableAnnotationFormat.class, solrDocumentList);
        return toModelList(searcheableAnnotationFormatList);
    }

    @Override
    public List<AnnotationFormat> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<AnnotationFormat> toModelList(List<SearcheableAnnotationFormat> searcheableAnnotationFormatList) {
        List<AnnotationFormat> annotationFormatList = new ArrayList<AnnotationFormat>();
        for (SearcheableAnnotationFormat searcheableAnnotationFormat : searcheableAnnotationFormatList) {
            annotationFormatList.add(searcheableAnnotationFormat.getAnnotationFormat());
        }
        return annotationFormatList;
    }

}
