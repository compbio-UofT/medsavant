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
package org.ut.biolab.medsavant.server.solr.service;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.io.IOException;
import java.util.*;

/**
 * Provides access to the Solr server, with the purpose of indexing genome variants from .vcf files.
 * @version $Id$
 *
 * @author Bogdan Vancea
 */
public class VariantService extends AbstractSolrService
{

    private static final Log LOG = LogFactory.getLog(VariantService.class);

    /**
     * Collection name.
     */
    private static final String NAME = "variant";

    /**
     * Buffer some variants before actually sending them to Solr
     */
    private List<VariantData> variantDataBuffer = new ArrayList<VariantData>();

    /**
     * The max number of variants to buffer before sending them Solrs
     */
    private static final int VARIANT_BUFFER_SIZE = 1500;

    @Override
    protected String getName()
    {
        return NAME;
    }

    /**
     * Index a map of variant documents to Solr.
     * @param variantDataList a list of variant data objects
     * @return {@code 0} if the indexing succeeded, {@code 1} if writing to the Solr server failed,
     */
    public int index(List<VariantData> variantDataList) {

        Collection<SolrInputDocument> allVariants = new HashSet<SolrInputDocument>();

        for (VariantData item : variantDataList) {

            SolrInputDocument doc = new SolrInputDocument();

            for (Map.Entry<String, Collection<String>> property : item.entrySet()) {
                String name = property.getKey();

                //doc.addField(name, property.getValue());
                for (String value : property.getValue()) {
                    doc.addField(name, value);
                }
            }
            allVariants.add(doc);
        }
        try {
            this.server.add(allVariants);
            this.server.commit();
            return 0;
        } catch (SolrServerException ex) {
            LOG.warn("Failed to index document: {}", ex);
        } catch (IOException ex) {
            LOG.warn("Failed to communicate with the Solr server while indexing variant: {}", ex);
        }
        return 1;
    }


    public int index(VariantData variantData) {

        variantData.addUUID();

        try {
            SolrInputDocument doc = getDocument(variantData);

            this.server.add(doc);
            return 0;
        } catch (SolrServerException ex) {
            LOG.warn("Failed to index document: {}", ex);
        } catch (IOException ex) {
            LOG.warn("Failed to communicate with the Solr server while indexing variant: {}", ex);
        }

        return 1;
    }

    public void scheduleToIndex(VariantData variantData) {
        VariantData variantDataClone = ObjectUtils.clone(variantData);

        variantDataBuffer.add(variantDataClone);

        if (variantDataBuffer.size() > VARIANT_BUFFER_SIZE) {
            indexCurrentBuffer();
        }


    }

    private SolrInputDocument getDocument(VariantData variantData) {
        SolrInputDocument doc = new SolrInputDocument();

        for (Map.Entry<String, Collection<String>> property : variantData.entrySet()) {
            String name = property.getKey();

            for (String value : property.getValue()) {
                doc.addField(name, value);
            }
        }

        return doc;
    }

    private List<SolrInputDocument> getDocuments(List<VariantData> variantDataList) {

        SolrInputDocument document = null;

        List<SolrInputDocument> solrInputDocumentList = new ArrayList<SolrInputDocument>(variantDataList.size());

        for (VariantData variantData : variantDataList) {
            document = getDocument(variantData);
            solrInputDocumentList.add(document);
        }

        return solrInputDocumentList;
    }

    public void index(VariantRecord variantRecord) {

        SolrInputDocument doc = new SolrInputDocument();

        //ToDo refactor
        doc.addField("id", variantRecord.getVariantID());
        doc.addField("chrom", variantRecord.getChrom());

        doc.addField("pos", variantRecord.getPosition());
        doc.addField("ref", variantRecord.getRef());
        doc.addField("alt", variantRecord.getAlt());
        doc.addField("qual", variantRecord.getQual());
        doc.addField("filter", variantRecord.getFilter());
        doc.addField("variant_type", variantRecord.getVariantType(variantRecord.getRef(), variantRecord.getAlt()));
        doc.addField("zygosity", variantRecord.getZygosity());
        doc.addField("gt", variantRecord.getGenotype());
        doc.addField("custom_info", variantRecord.getCustomInfo());
        doc.addField("dna_id", variantRecord.getDnaID());
        doc.addField("upload_id", variantRecord.getUploadID());
        doc.addField("file_id", variantRecord.getFileID());

        doc.addField("INFO_AA", variantRecord.getAncestralAllele());
        doc.addField("INFO_AC", variantRecord.getAlleleCount());
        doc.addField("INFO_AF", variantRecord.getAlleleFrequency());
        doc.addField("INFO_AN", variantRecord.getNumberOfAlleles());
        doc.addField("INFO_BQ", variantRecord.getBaseQuality());
        doc.addField("INFO_CIGAR", variantRecord.getCigar());
        doc.addField("INFO_DB", variantRecord.getDbSNPMembership());
        doc.addField("INFO_DP", variantRecord.getDepthOfCoverage());
        doc.addField("INFO_END", variantRecord.getEndPosition());
        doc.addField("INFO_H2", variantRecord.getHapmap2Membership());
        doc.addField("INFO_H3", variantRecord.getHapmap3Membership());
        doc.addField("INFO_MQ", variantRecord.getMappingQuality());
        doc.addField("INFO_MQ0", variantRecord.getNumberOfZeroMQ());
        doc.addField("INFO_NS", variantRecord.getNumberOfSamplesWithData());
        doc.addField("INFO_SB", variantRecord.getStrandBias());
        doc.addField("INFO_SOMATIC", variantRecord.getIsSomatic());
        doc.addField("INFO_VALIDATED", variantRecord.getIsValidated());
        doc.addField("INFO_1000G", variantRecord.getIsInThousandGenomes());
        UUID uuid = UUID.randomUUID();
        doc.addField("uuid", uuid.toString());
        try {
            this.server.add(doc);
            this.server.commit();
        } catch (SolrServerException e) {
            LOG.error("Error adding variant record to Solr");
        } catch (IOException e) {
            LOG.error("Failed to communicate with the Solr server");
        }
    }


    public void indexCurrentBuffer() {

        List<SolrInputDocument> solrInputDocumentList = getDocuments(variantDataBuffer);

        try {
            this.server.add(solrInputDocumentList);
        } catch (SolrServerException e) {
            LOG.error("Failed to send documents buffered to Solr", e);
        } catch (IOException e) {
            LOG.error("Failed to send documents buffered to Solr", e);
        } finally {
            //not sure if the buffer should be cleared in case of error
            variantDataBuffer.clear();
        }


    }
    public void commitChanges() {

        try {
            this.server.commit();
        } catch (SolrServerException e) {
            LOG.error("Failed to commit indexed data");
        } catch (IOException e) {
            LOG.error("Failed to communicate with the Solr server");
        }
    }
}
