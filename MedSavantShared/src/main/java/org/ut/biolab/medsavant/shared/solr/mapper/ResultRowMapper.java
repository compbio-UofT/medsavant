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

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.query.ResultRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map result rows.
 */
public class ResultRowMapper implements ResultMapper<ResultRow> {

    @Override
    public List<ResultRow> map(SolrDocumentList solrDocumentList) {

        return toResultRowList(solrDocumentList);
    }

    @Override
    public List<ResultRow> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        return toResultRowList(solrDocumentList,aggregateFieldMap);
    }


    /**
     * Map a list of Solr documents to a list of ResultRows.
     * @param documentList          List of SolrDocument instances.
     * @return                      List of ResultRow instances.
     */
    public List<ResultRow> toResultRowList(SolrDocumentList documentList, Map<String, String> aggregateFields) {
        List<ResultRow> resultRowList = new ArrayList<ResultRow>(documentList.size());
        for (SolrDocument document : documentList ) {
            resultRowList.add(toResultRow(document, aggregateFields));
        }
        return resultRowList;
    }

    /**
     * Map a single SolrDocument to a ResultRow.
     * @param document              A SolrDocument.
     * @return                      A Result
     */
    public ResultRow toResultRow(SolrDocument document, Map<String, String> aggregateFields) {
        ResultRow resultRow = new ResultRow();
        for (String key : document.getFieldNames()) {
            Object value = document.get(key);
            resultRow.put(key, value);
        }
        return resultRow;
    }

    /**
     * Map a list of Solr documents to a list of ResultRows.
     * @param documentList          List of SolrDocument instances.
     * @return                      List of ResultRow instances.
     */
    public List<ResultRow> toResultRowList(SolrDocumentList documentList) {
        List<ResultRow> resultRowList = new ArrayList<ResultRow>(documentList.size());
        for (SolrDocument document : documentList ) {
            resultRowList.add(toResultRow(document));
        }
        return resultRowList;
    }

    /**
     * Map a single SolrDocument to a ResultRow.
     * @param document              A SolrDocument.
     * @return                      A Result
     */
    public ResultRow toResultRow(SolrDocument document) {
        ResultRow resultRow = new ResultRow();
        for (String key : document.getFieldNames()) {
            Object value = document.get(key);
            resultRow.put(key, value);
        }
        return resultRow;
    }
}
