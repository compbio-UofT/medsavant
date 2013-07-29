package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableProjectDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map Project Details.
 */
public class ProjectMapper implements ResultMapper<ProjectDetails> {


    @Override
    public List<ProjectDetails> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableProjectDetails> projectDetailsList = binder.getBeans(SearcheableProjectDetails.class, solrDocumentList);
        return toModelList(projectDetailsList);
    }

    @Override
    public List<ProjectDetails> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<ProjectDetails> toModelList(List<SearcheableProjectDetails> searcheableProjectDetailsList) {

        List<ProjectDetails> projectDetailsList = new ArrayList<ProjectDetails>(searcheableProjectDetailsList.size());
        for (SearcheableProjectDetails details : searcheableProjectDetailsList) {
            projectDetailsList.add(details.getProjectDetails());
        }
        return projectDetailsList;
    }
}
