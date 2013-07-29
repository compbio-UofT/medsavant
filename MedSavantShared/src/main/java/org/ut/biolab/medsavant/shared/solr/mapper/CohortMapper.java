package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableCohort;
import org.ut.biolab.medsavant.shared.model.solr.SearcheablePatient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map cohorts.
 */
public class CohortMapper implements ResultMapper<Cohort> {
    @Override
    public List<Cohort> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableCohort> searcheableCohortList = binder.getBeans(SearcheableCohort.class, solrDocumentList);

        return toModelList(searcheableCohortList);
    }

    @Override
    public List<Cohort> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<Cohort> toModelList(List<SearcheableCohort> searcheableCohortList) {
        List<Cohort> cohortList = new ArrayList<Cohort>(searcheableCohortList.size());
        for (SearcheableCohort searcheableCohort : searcheableCohortList) {
            cohortList.add(searcheableCohort.getCohort());
        }
        return cohortList;
    }

}