package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.AnnotatedColumn;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableAnnotatedColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapping of annotated columns.
 */
public class AnnotationColumnMapper implements ResultMapper<AnnotatedColumn> {
    @Override
    public List<AnnotatedColumn> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableAnnotatedColumn> searcheableAnnotatedColumnList = binder.getBeans(SearcheableAnnotatedColumn.class, solrDocumentList);
        return toModelList(searcheableAnnotatedColumnList);
    }

    @Override
    public List<AnnotatedColumn> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<AnnotatedColumn> toModelList(List<SearcheableAnnotatedColumn> searcheableAnnotatedColumnList) {
        List<AnnotatedColumn> annotatedColumnList = new ArrayList<AnnotatedColumn>();
        for (SearcheableAnnotatedColumn searcheableAnnotatedColumn : searcheableAnnotatedColumnList) {
            annotatedColumnList.add(searcheableAnnotatedColumn.getColumn());
        }
        return annotatedColumnList;
    }

}
