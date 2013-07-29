package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.GeneralLog;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableGeneralLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map general log events.
 */
public class GeneralLogMapper implements ResultMapper<GeneralLog> {
    @Override
    public List<GeneralLog> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableGeneralLog> generalLogs = binder.getBeans(SearcheableGeneralLog.class, solrDocumentList);

        return toModelList(generalLogs);
    }

    @Override
    public List<GeneralLog> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<GeneralLog> toModelList(List<SearcheableGeneralLog> searcheableGeneralLogList) {
        List<GeneralLog> generalLogs = new ArrayList<GeneralLog>(searcheableGeneralLogList.size());
        for (SearcheableGeneralLog generalLog : searcheableGeneralLogList) {
            generalLogs.add(generalLog.getLog());
        }
        return generalLogs;
    }





}
