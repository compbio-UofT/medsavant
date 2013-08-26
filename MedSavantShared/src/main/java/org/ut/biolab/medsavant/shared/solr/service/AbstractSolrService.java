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
package org.ut.biolab.medsavant.shared.solr.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.NamedList;
import org.ut.biolab.medsavant.shared.model.solr.FieldMappings;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.solr.decorator.EntityDecorator;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.solr.decorator.SolrInputDocumentDecorator;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * //FIXME this class is more like a DAO than a service, refactor
 */
public abstract class AbstractSolrService<T> {

    private static final Log LOG = LogFactory.getLog(AbstractSolrService.class);

    private static final String SOLR_HOST = "http://localhost:8983/solr/";

    private static final SolrInputDocumentDecorator decorator = new EntityDecorator();

    /** The Solr server instance used. */
    protected HttpSolrServer server;

    public void initialize() throws InitializationException {
        try {

            this.server = new HttpSolrServer(SOLR_HOST + this.getName() + "/");
        } catch (RuntimeException ex) {
            LOG.error("Invalid URL specified for the Solr server: {}",ex);
        }
    }

    //todo add paging and sort support
    /**
     * Get the name of the Solr "core" to be used by this service instance.
     *
     * @return the core name
     */
    protected abstract String getName();


    /**
     * Perform search by user a SolrParams object.
     * @param solrParams            The SolrParams containing the parameters.
     * @return                      A SolrDocumentList containing the query result.
     */
    public SolrDocumentList search(SolrParams solrParams) {

        SolrDocumentList result = null;

        try {
            result = this.server.query(solrParams).getResults();
        } catch (SolrServerException e) {
            LOG.error("Error executing query " + solrParams.toString());
        }

        return result;
    }

    /**
     * Perform search by user a SolrQuery object.
     * @param solrQuery             The SolrQuery to execute
     * @return                      A SolrDocumentList containing the query result.
     */
    public SolrDocumentList search(SolrQuery solrQuery) {
        QueryResponse result = null;

        try {
            result = this.server.query(solrQuery);
        } catch (SolrServerException e) {
            LOG.error("Error executing query " + solrQuery.toString());
        }

        return getDocumentList(result);
    }

    public SolrDocumentList search(SolrQuery solrQuery, Map<String, String> aggregates) {
        QueryResponse result = null;

        try {
            result = this.server.query(solrQuery);
        } catch (SolrServerException e) {
            LOG.error("Error executing query " + solrQuery.toString());
        }

        return getDocumentList(result, aggregates);
    }

    /**
     * Return the number of results for a query.
     * @param solrQuery             The SolrQuery to execute
     * @return                      the number of results
     */
    public long count(SolrQuery solrQuery) {
        QueryResponse result = null;

        int tempStart = solrQuery.getStart();
        int tempRows = solrQuery.getRows();

        //no need for any results
        solrQuery.setRows(0);
        solrQuery.setStart(0);

        try {
            result = this.server.query(solrQuery);
        } catch (SolrServerException e) {
            LOG.error("Error executing query " + solrQuery.toString());
        } finally {
            solrQuery.setStart(tempStart);
            solrQuery.setRows(tempRows);
        }

        return result.getResults().getNumFound();
    }

    public SolrDocumentList search(SimpleSolrQuery simpleSolrQuery) {

        return search(simpleSolrQuery.toSolrParams());
    }

    /**
     * Index an entity
     * @param entity           The entity.
     * @return
     */
    public int index(T entity) {
        try {
            this.server.add(getSolrInputDocument(entity));
            this.server.commit();
            return 0;
        } catch (IOException e) {
            LOG.error("Cannot connect to server");
        } catch (SolrServerException e) {
            LOG.error("Cannot index variant comment");
        }

        return 1;
    }

    /**
     * Index a list of entities
     * @param entities          The list of entities
     * @return
     */
    public int index(List<T> entities) {
        try {
            this.server.add(getSolrInputDocuments(entities));
            this.server.commit();
        } catch (IOException e) {
            LOG.error("Cannot connect to server");
        } catch (SolrServerException e) {
            LOG.error("Cannot execute query");
        }

        return 0;
    }

    /**
     * Index a SolrInputDocument. Can be also used for updates.
     * @param solrInputDocument
     * @return
     */
    public int index(SolrInputDocument solrInputDocument) {
        try {
            this.server.add(solrInputDocument);
            this.server.commit();
        } catch (SolrServerException e) {
            LOG.error("Cannot connect to server");
        } catch (IOException e) {
            LOG.error("Cannot ");
        }

        return 0;
    }

    /**
     * Index data from a certain file on the same filesystem.
     * @param file
     * @return
     */
    public int index(String file, String separator, String escape) {

        String oldBaseURL = server.getBaseURL();

        try {
            SolrQuery query = new SolrQuery();
            query.add(CommonParams.STREAM_FILE, file);
            query.add("separator", separator);
            query.add("escape", escape);


            server.setBaseURL(oldBaseURL + "/csv");

            server.query(query);

            server.setBaseURL(oldBaseURL);
        } catch (SolrServerException e) {
            LOG.error("Error uploading from document");
        } finally {
            server.setBaseURL(oldBaseURL);
        }


        return 0;
    }

    /**
     * Delete some entities by a query.
     * @param solrQuery
     */
    public void delete(SolrQuery solrQuery) {
        try {
            this.server.deleteByQuery(solrQuery.getQuery());
            this.server.commit();
        } catch (IOException e) {
            LOG.error("Cannot connect to server");
        } catch (SolrServerException e) {
            LOG.error("Cannot execute query");
        }
    }

    /**
     * Delete a set of documents by their ids.
     * @param ids                   A list of document ids that will be removed from the index.
     */
    public void delete(List<String> ids) {
        try {
            this.server.deleteById(ids);
            this.server.commit();
        } catch (IOException e) {
            LOG.error("Cannot connect to server");
        } catch (SolrServerException e) {
            LOG.error("Cannot execute query");
        }
    }

    /**
     * Delete a document by its id.
     * @param id                    The id of the document to be removed from the index.
     */
    public void delete(String id) {
        try {
            this.server.deleteById(id);
            this.server.commit();
        } catch (IOException e) {
            LOG.error("Cannot connect to server");
        } catch (SolrServerException e) {
            LOG.error("Cannot execute query");
        }
    }

    /**
     * Check if the response contains facets. If that is true, it means it is a group by statement.
     *
     * If the facet information is return into FacetFields, the group by is done only on 1 term. If the facet information
     * is returned into FacetPivotFields, the grouping is done on more than one terms. If there is no facet information
     * return the result list for the QueryResponse.
     * @param response              Query response.
     * @return                      Solr document list.
     */
    private SolrDocumentList getDocumentList(QueryResponse response) {

        return getDocumentList(response, new HashMap<String, String>());
    }

    private SolrDocumentList getDocumentList(QueryResponse response, Map<String, String> aggregates) {

        SolrDocumentList solrDocumentList = null;
        if (response.getFacetPivot() != null && response.getFacetPivot().size() > 0) {
            solrDocumentList = getDocumentListFacetPivots(response.getFacetPivot());
        } else if (response.getFacetFields() != null && response.getFacetFields().size() > 0) {
            solrDocumentList = getDocumentListFacetFields(response, aggregates);
        } else  {
            solrDocumentList = addAggregates(response.getResults(), response.getFieldStatsInfo(), aggregates);
        }

        return solrDocumentList;
    }

    private SolrDocumentList getDocumentListFacetFields(QueryResponse response, Map<String, String> aggregates) {

        SolrDocumentList documentList = new SolrDocumentList();
        for (FacetField facetField : response.getFacetFields()) {

            String key = facetField.getName();
            for (FacetField.Count count : facetField.getValues()) {
                SolrDocument solrDocument = new SolrDocument();
                solrDocument.setField(key,count.getName());
                solrDocument.setField("count", new Integer((int) count.getCount()));

                if (aggregates != null) {


                    for (Map.Entry<String, String> item : aggregates.entrySet()) {

                        Map<String, FieldStatsInfo> stats = response.getFieldStatsInfo();

                        if (stats.containsKey(item.getKey())) {
                            FieldStatsInfo fieldStatsInfo = stats.get(item.getValue());
                            if ("min".equals(item.getKey())) {
                                solrDocument.addField(item.getKey(), fieldStatsInfo.getMin());
                            } else if ("max".equals(item.getKey())) {
                                solrDocument.addField(item.getKey(), fieldStatsInfo.getMax());
                            }
                        }
                    }
                }

                documentList.add(solrDocument);
            }
        }

        return documentList;
    }

    private SolrDocumentList addAggregates(SolrDocumentList solrDocumentList, Map<String, FieldStatsInfo> stats, Map<String, String> aggregates) {
        if (aggregates != null) {
            for (SolrDocument solrDocument : solrDocumentList) {
                for (Map.Entry<String, String> item : aggregates.entrySet()) {
                    String fieldValue = FieldMappings.mapToSolrField(item.getValue(), getName());

                    if (stats.containsKey(fieldValue)) {
                        FieldStatsInfo fieldStatsInfo = stats.get(fieldValue);
                        if ("min".equals(item.getKey())) {
                            solrDocument.addField(item.getKey(), fieldStatsInfo.getMin());
                        } else if ("max".equals(item.getKey())) {
                            solrDocument.addField(item.getKey(), fieldStatsInfo.getMax());
                        }
                    }
                }
            }
        }
        return solrDocumentList;
    }

    private SolrDocumentList getDocumentListFacetPivots(NamedList<List<PivotField>> pivotFields) {

        SolrDocumentList results = new SolrDocumentList();
        for (PivotField pivotField : pivotFields.getVal(0)) {
            SolrDocumentList partialResults = constructFromPivotHierarchy(pivotField);
            results = listMerge(results, partialResults);
        }

        return results;
    }

    private SolrDocumentList constructFromPivotHierarchy(PivotField root) {

        SolrDocumentList solrDocumentList = null;
        if (root.getPivot() == null) {
            SolrDocument solrDocument = new SolrDocument();
            solrDocument.setField("count", root.getCount());
            solrDocument.setField(root.getField(), root.getValue());
            solrDocumentList = new SolrDocumentList();
            solrDocumentList.add(solrDocument);
        } else {
            solrDocumentList = new SolrDocumentList();
            for (PivotField child : root.getPivot()) {
                SolrDocumentList solrDocuments = constructFromPivotHierarchy(child);
                for (SolrDocument solrDocument : solrDocuments) {
                    solrDocument.addField(root.getField(), root.getValue());
                    solrDocumentList.add(solrDocument);
                }
            }
        }

        return solrDocumentList;
    }

    private SolrDocumentList listMerge(SolrDocumentList first, SolrDocumentList second) {
        for (SolrDocument document : second) {
            first.add(document);
        }
        return first;
    }

    private SolrInputDocument getSolrInputDocument(T entity) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        SolrInputDocument document = binder.toSolrInputDocument(entity);
        return decorator.decorate(document, entity.getClass());
    }

    private List<SolrInputDocument> getSolrInputDocuments(Collection<T> entities) {
        List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
        for (T entity : entities) {
            documents.add(getSolrInputDocument(entity));
        }
        return documents;
    }

}
