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
