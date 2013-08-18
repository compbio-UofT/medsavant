package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Perform the mapping of SolrDocument's representing annotation to Annotation objects.
 */
public class AnnotationMapper implements ResultMapper<Annotation> {

    @Override
    public List<Annotation> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableAnnotation> searcheableAnnotations = binder.getBeans(SearcheableAnnotation.class, solrDocumentList);
        return toModelList(searcheableAnnotations);
    }

    @Override
    public List<Annotation> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<Annotation> toModelList(List<SearcheableAnnotation> searcheableAnnotations) {
        List<Annotation> annotationList = new ArrayList<Annotation>();
        for (SearcheableAnnotation searcheableAnnotation : searcheableAnnotations) {
            annotationList.add(searcheableAnnotation.getAnnotation());
        }
        return annotationList;
    }

}
