package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.query.ResultRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Map result rows.
 */
public class ResultRowMapper implements ResultMapper<ResultRow> {

    @Override
    public List<ResultRow> map(SolrDocumentList solrDocumentList) {

        return toResultRowList(solrDocumentList);
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
