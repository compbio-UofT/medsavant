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
